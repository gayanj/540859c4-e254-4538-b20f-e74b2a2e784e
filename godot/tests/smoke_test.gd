extends Node
## Headless gameplay smoke test. Drives the Game scene through every system
## (movement, kills, all power-ups, suicide blast, death saws, game over) and
## fails loudly on any script error or unmet expectation.
##
## Run:  godot --headless --path godot tests/smoke_test.tscn
## Exit code is non-zero on failure.

const GAME_SCENE := preload("res://scenes/game/game.tscn")
const PICKUP_SCENE := preload("res://scenes/entities/powerup_pickup.tscn")

var _failures: PackedStringArray = []
var _game: Game
## Where the hero is held while waiting (an idle hero gets shoved into the saws)
## and whether its energy is topped up (idle, it is chipped to death by enemies).
var _pin := Vector2(540, 960)
var _sustain := true


func _ready() -> void:
	Telemetry.source = "test"
	# Fully progressed save so every enemy type and power-up is in play.
	Prefs.first_tutorial_completed = true
	Prefs.second_tutorial_completed = true
	Prefs.spawn_split_particles = true
	Prefs.spawn_suicide_particles = true
	Prefs.spawn_invisible_particles = true
	Prefs.stage = 3
	Prefs.highscore = 0
	await _run()
	Prefs.reset()
	if _failures.is_empty():
		print("SMOKE TEST PASSED")
		get_tree().quit(0)
	else:
		for f in _failures:
			printerr("FAIL: " + f)
		get_tree().quit(1)


func _check(cond: bool, what: String) -> void:
	if not cond:
		_failures.append(what)


func _ticks(n: int) -> void:
	for _i in n:
		if is_instance_valid(_game) and _game.hero.alive:
			_game.hero.global_position = _pin
			_game.hero.linear_velocity = Vector2.ZERO
			if _sustain and _game.hero.energy < 40:
				_game.hero.energy = Constants.HERO_START_ENERGY
		await get_tree().physics_frame


func _run() -> void:
	_game = GAME_SCENE.instantiate()
	add_child(_game)
	await _ticks(2)
	_check(_game.particles.get_child_count() == 3, "3 normal particles spawned for stage 3")
	_check(_game.hero.energy == Constants.HERO_START_ENERGY, "hero starts with 80 energy")

	# Steer the hero in a circle for a while; nothing should error.
	for i in 240:
		var a := i * 0.05
		_game.joystick.vector = Vector2(cos(a), sin(a))
		await get_tree().physics_frame
	_game.joystick.vector = Vector2.ZERO
	_check(_game.hero.alive, "hero survives steering")

	# Force a kill on a saw: teleport a particle onto one.
	var victim: Particle = _game.particles.get_child(0)
	var saw: Saw = _game.get_node("Arena").saws[0]
	victim.global_position = saw.global_position
	await _ticks(5)
	_check(_game.total_destroyed >= 1, "particle killed on saw (destroyed=%d)" % _game.total_destroyed)
	_check(_game.score.score >= 1, "score incremented")

	# Overheat: three quick kills on one blade make it dull; the next particle bounces.
	for _i in 2:
		for p in _game.particles.get_children():
			if p is Particle and p.alive and p.type == Constants.ParticleType.NORMAL:
				p.global_position = saw.global_position
				break
		await _ticks(3)
	_check(saw.dull, "saw overheats after 3 quick kills (heat=%.1f)" % saw.heat)
	_check(_game.death_saws.get_child_count() >= 1 and (_game.death_saws.get_child(0) as DeathSaw).from_overheat, "overheated saw threw a blade")
	for s in _game.death_saws.get_children():
		s.free()
	_game.overheat_throws = false  # the pinned hero cannot dodge thrown blades
	var destroyed_at_dull := _game.total_destroyed
	for p in _game.particles.get_children():
		if p is Particle and p.alive and p.type == Constants.ParticleType.NORMAL:
			p.global_position = saw.global_position + Vector2(40, 0)
			p.linear_velocity = Vector2(-600, 0)
			break
	await _ticks(10)
	_check(_game.total_destroyed == destroyed_at_dull, "dull saw does not kill")

	# Rack up a combo by killing many particles quickly, rotating blades.
	var saws: Array = _game.get_node("Arena").saws
	for i in 25:
		var blade: Saw = saws[(i + 7) % saws.size()]
		for p in _game.particles.get_children():
			if p is Particle and p.alive and p.type == Constants.ParticleType.NORMAL:
				p.global_position = blade.global_position
				break
		await _ticks(3)
	_check(_game.score.streak >= 10, "combo streak reached 10 (streak=%d)" % _game.score.streak)
	_check(_game.hud._toast.visible, "kill streak toast shown")
	_check(_game.hud._energy_dial.value == _game.hero.energy, "energy dial tracks energy")

	# From here on the hero idles at the pin, so stop the spawner's death saws and
	# invisible particles from sweeping through it; those are exercised explicitly below.
	Prefs.spawn_suicide_particles = false
	Prefs.spawn_invisible_particles = false
	# ...and keep the unlock counters below their thresholds so kills made by the
	# rest of the test cannot re-enable them.
	Prefs.split_particles_destroyed = 0
	Prefs.suicide_particles_destroyed = 0
	for s in _game.death_saws.get_children():
		s.queue_free()

	# The pinned hero cannot dodge blades thrown by overheats, so clear any heat and
	# stray blades accumulated by the combo phase.
	for s in saws:
		s.heat = 0.0
	for s in _game.death_saws.get_children():
		s.free()
	await _ticks(2)

	# Dash: costs energy, launches particles instead of taking damage.
	_sustain = false
	_game.hero.energy = 60
	var e0 := _game.hero.energy
	var target := _game.spawner.spawn_particle(Constants.ParticleType.NORMAL, _pin + Vector2(120, 0))
	target._hero = null
	target.linear_velocity = Vector2.ZERO
	target.collision_mask = Constants.LAYER_HERO  # crowd can't nudge it off the dash line
	for other in _game.particles.get_children():
		if other is Particle and other != target and other.alive:
			other.global_position = Vector2(200, 300)  # clear the lane
	await get_tree().physics_frame
	_game.hero.global_position = _pin
	_game.hero.linear_velocity = Vector2.ZERO
	_game.hero.facing = Vector2.RIGHT
	target.global_position = _pin + Vector2(120, 0)
	target.linear_velocity = Vector2.ZERO
	_check(_game.hero.dash(Vector2.RIGHT), "dash accepted")
	_check(_game.hero.energy <= e0 - Constants.DASH_COST, "dash costs %d energy (%d -> %d)" % [Constants.DASH_COST, e0, _game.hero.energy])
	_check(not _game.hero.can_dash(), "dash on cooldown")
	# Contacts that began the step before the dash are reported one step late;
	# take the baseline once the dash is actually in flight.
	await get_tree().physics_frame
	var hits_before: int = Telemetry.last_run()["hits"]
	for _i in Constants.DASH_TICKS - 3:
		await get_tree().physics_frame
	_check(_game.hero.dashing, "still dashing mid-window")
	_check(Telemetry.last_run()["dash_hits"] >= 1, "dash launched a particle")
	# The hero can be nudged off-axis by the crowd before the dash; accept near-perfect.
	var hist: Array = Telemetry.last_run().get("alignment_hist", [])
	_check(Telemetry.last_run()["perfect_hits"] >= 1 or (hist.size() == 10 and hist[9] + hist[8] >= 1), "head-on dash is (near-)perfect")
	_check(Telemetry.last_run()["hits"] == hits_before, "no damage taken while dashing (hits %d->%d)" % [hits_before, Telemetry.last_run()["hits"]])
	for _i in 4:
		await get_tree().physics_frame
	_check(not _game.hero.dashing, "dash ended")
	if is_instance_valid(target) and target.alive:
		for _i in 20:
			await get_tree().physics_frame
		_check(not is_instance_valid(target) or not target.alive or target.linear_velocity.x > 1000.0,
			"launched particle keeps flying away (vx=%.0f)" % (target.linear_velocity.x if is_instance_valid(target) else 0.0))
	_sustain = true

	# Consumables.
	_game.hero.energy = 30
	_collect(Constants.PowerupType.ENERGY)
	_check(_game.hero.energy == Constants.HERO_START_ENERGY, "energy pickup refills")
	_collect(Constants.PowerupType.INVINCIBILITY)
	_check(_game.hero.invincible, "invincibility active")
	_check(_game.hero.collision_layer == Constants.LAYER_HERO_INVINCIBLE, "invincible layer set")
	# True i-frames: an armed mine shatters, a blade does not kill.
	var mine := _game.spawner.spawn_particle(Constants.ParticleType.INVISIBLE, _pin + Vector2(0, -60))
	mine._phase_ticks = Particle.INVISIBLE_FADE_TICKS - 1
	await _ticks(3)
	_check(mine.is_deadly(), "mine armed")
	_check(mine.freeze, "stationary mine is frozen in place")
	_game.hero.global_position = mine.global_position + Vector2(0, 40)
	_game.hero.linear_velocity = Vector2(0, -400)
	for _i in 6:
		await get_tree().physics_frame
	_check(not is_instance_valid(mine) or not mine.alive, "invincible hero shatters an armed mine")
	_check(_game.hero.alive, "hero survives the mine")
	var wall_saw: Saw = saws[0]
	_pin = wall_saw.global_position
	await _ticks(6)
	_check(_game.hero.alive, "invincible hero survives a blade")
	_pin = Vector2(540, 960)
	await _ticks(Constants.INVINCIBILITY_TICKS)
	_check(not _game.hero.invincible, "invincibility expires")

	# Dash brake: pulling against the dash stops it; cancel: turning hard ends it.
	_sustain = false
	_game.hero.energy = 60
	await get_tree().physics_frame
	_game.joystick.vector = Vector2.ZERO
	_check(_game.hero.dash(Vector2.UP), "brake test dash")
	for _i in 3:
		await get_tree().physics_frame
	_game.joystick.vector = Vector2.DOWN
	await get_tree().physics_frame
	_check(not _game.hero.dashing and _game.hero.linear_velocity.y > -100.0, "pulling back brakes the dash (vy=%.0f)" % _game.hero.linear_velocity.y)
	_check(Telemetry.last_run().get("dash_brakes", 0) >= 1, "brake logged")
	_game.joystick.vector = Vector2.ZERO
	await _ticks(Constants.DASH_COOLDOWN_TICKS + 12)
	_check(_game.hero.dash(Vector2.UP), "cancel test dash")
	for _i in 3:
		await get_tree().physics_frame
	_game.joystick.vector = Vector2.RIGHT
	await get_tree().physics_frame
	_check(not _game.hero.dashing, "turning hard cancels the dash")
	_check(Telemetry.last_run().get("dash_cancels", 0) >= 1, "cancel logged")
	_game.joystick.vector = Vector2.ZERO
	_sustain = true
	await _ticks(Constants.DASH_COOLDOWN_TICKS + 12)

	# Passive dash buffs stack and cap.
	var cost0 := _game.powerups.dash_cost()
	_collect(Constants.PowerupType.ARMOR)
	_check(_game.powerups.dash_cost() == cost0 - 1, "Reinforced lowers dash cost")
	for _i in 5:
		_collect(Constants.PowerupType.SPEED)
	_check(_game.powerups.stacks(Constants.PowerupType.SPEED) == Constants.PASSIVE_MAX_STACKS, "passives cap at %d" % Constants.PASSIVE_MAX_STACKS)
	_check(_game.powerups.dash_ticks() > Constants.DASH_TICKS, "Afterburner lengthens dash")
	_collect(Constants.PowerupType.SUPER_FORCE)
	_check(_game.powerups.launch_speed() > Constants.DASH_LAUNCH_SPEED, "Impact raises launch speed")
	_collect(Constants.PowerupType.SLOW_MOTION)
	_check(_game.powerups.focus_ticks() > 0, "Focus adds slow-mo ticks")
	_check(_game.hud._buff_bar._passives.size() == 4, "buff bar shows 4 passives")

	# Shockwave charge: next dash kills what it touches.
	_collect(Constants.PowerupType.MASS_DEATH)
	_check(_game.powerups.shockwave_charges == 1, "shockwave charge stored")
	_sustain = false
	_game.hero.energy = 60
	# A 3-stack Afterburner dash from the centre reaches the side walls; go up.
	var victim2 := _game.spawner.spawn_particle(Constants.ParticleType.NORMAL, _pin + Vector2(0, -120))
	victim2._hero = null
	victim2.linear_velocity = Vector2.ZERO
	await get_tree().physics_frame
	var kills_before: int = Telemetry.last_run()["dash_kills"]
	_check(_game._try_dash(Vector2.UP), "shockwave dash accepted")
	_check(_game.powerups.shockwave_charges == 1, "charge not spent until contact")
	_check(is_equal_approx(Game.time_scale, Constants.FOCUS_SCALE), "Focus slows the world during dash")
	for _i in _game.hero.dash_length_ticks + 2:
		await get_tree().physics_frame
	_check(Telemetry.last_run()["dash_kills"] > kills_before, "shockwave dash killed a particle")
	_check(_game.powerups.shockwave_charges == 0, "charge spent on contact")
	await _ticks(Constants.FOCUS_TICKS_PER_STACK + 5)
	_check(is_equal_approx(Game.time_scale, 1.0), "Focus ends")
	_sustain = true

	# Suicide particle: spawn one next to a dummy and fast-forward its fuse.
	var suicide := _game.spawner.spawn_particle(Constants.ParticleType.SUICIDE, Vector2(300, 600))
	suicide._fuse_ticks = Constants.SUICIDE_BLAST_TICKS - 2
	var bystander := _game.spawner.spawn_particle(Constants.ParticleType.NORMAL, Vector2(330, 620))
	_pin = Vector2(900, 1500)  # keep the hero out of the blast
	await _ticks(6)
	_check(not is_instance_valid(suicide) or suicide.is_queued_for_deletion(), "suicide particle removed after blast")
	_check(not is_instance_valid(bystander) or bystander.is_queued_for_deletion(), "bystander killed by blast")
	_check(_game.hero.alive, "hero outside blast survives")

	# Invisible particle cycles through its phases.
	var ghost := _game.spawner.spawn_particle(Constants.ParticleType.INVISIBLE, Vector2(200, 1000))
	await _ticks(Particle.INVISIBLE_FADE_TICKS + 10)
	_check(ghost.is_deadly(), "invisible particle becomes deadly after appearing")
	await _ticks(Constants.INVISIBLE_PHASE_TICKS + 60)
	_check(ghost.collision_layer == Constants.LAYER_INVISIBLE_HIDDEN, "invisible particle hides")
	ghost.kill()

	# Death saw: force one and let it travel.
	_game.spawner._spawn_death_saw()
	await _ticks(2)
	_check(_game.death_saws.get_child_count() >= 1, "death saw spawned")
	var death_saw: DeathSaw = _game.death_saws.get_child(_game.death_saws.get_child_count() - 1)
	var start := death_saw.global_position
	# Keep the pinned hero out of the blade's lane.
	_pin = Vector2(900, 1500) if (absf(start.y - 1500.0) > 150.0 and absf(start.x - 900.0) > 150.0) else Vector2(300, 600)
	await _ticks(death_saw.launch_delay + 60)
	_check(not is_instance_valid(death_saw) or death_saw.global_position.distance_to(start) > 100.0, "death saw launched and moving")

	# Game over via energy loss.
	_sustain = false
	_game.hero.energy = 0
	await _ticks(5)
	_check(_game.game_over, "game over triggered at zero energy")
	var run := Telemetry.last_run()
	_check(run.get("death_cause", "") == "energy", "telemetry death cause recorded (%s)" % run.get("death_cause", ""))
	_check(run.get("hits", 0) > 0 and run["kills"]["normal"] > 0, "telemetry counted hits and kills")
	_check(FileAccess.file_exists(run.get("_path", "")), "telemetry file written")
	_check(_game.hud._game_over.visible, "game over panel shown")
	_check(Prefs.highscore >= _game.score.score, "highscore persisted")
	_check(is_equal_approx(Game.time_scale, 1.0), "time scale reset on game over")

	# Retry reloads cleanly.
	_game.queue_free()
	await _ticks(2)
	_sustain = true
	_game = GAME_SCENE.instantiate()
	add_child(_game)
	await _ticks(30)
	_check(_game.hero.alive and not _game.game_over, "fresh game after retry")
	_game.queue_free()
	await _ticks(2)


func _collect(type: Constants.PowerupType) -> void:
	var pickup: PowerupPickup = PICKUP_SCENE.instantiate()
	pickup.setup(type)
	_game.pickups.add_child(pickup)
	_game.powerups.collect(pickup)
	pickup.queue_free()
