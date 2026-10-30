class_name Game
extends Node2D
## Gameplay scene orchestrator. The old GameScreenWorldController is split into
## [Spawner] (what appears when), [PowerupManager] (inventory + effects),
## [ScoreTracker] (points, combos, streaks) and this node, which owns the run
## state, wires the entity signals together and handles tutorials and game over.

## Global slow-motion factor read by enemies and saws (1.0 = normal speed).
static var time_scale := 1.0

@onready var hero: Hero = $Hero
@onready var particles: Node2D = $Particles
@onready var death_saws: Node2D = $DeathSaws
@onready var pickups: Node2D = $Pickups
@onready var effects: Node2D = $Effects
@onready var spawner: Spawner = $Spawner
@onready var powerups: PowerupManager = $PowerupManager
@onready var score: ScoreTracker = $ScoreTracker
@onready var hud: HUD = $HUD
@onready var joystick: TouchJoystick = $Controls/TouchJoystick
@onready var tutorial_arrow: AnimatedSprite2D = $TutorialArrow
@onready var game_over_saw: SawBlade = $GameOverSaw
@onready var world_fx: WorldFx = $WorldFx

## Hit-stop on ordinary particle hits. Off: at 3+ hits/s (see telemetry) even a
## rate-limited 50 ms freeze reads as stutter and the flinch/sparks/dial blink
## already carry the feedback. Blasts and death keep their hit-stop.
const HIT_STOP_ON_HIT := false
## Overheated blades throw a copy of themselves into the arena. Tests disable this.
var overheat_throws := true

## Grid force tuning (px radius, dimensionless strength).
const GRID_HERO_RADIUS := 90.0
const GRID_ENEMY_RADIUS := 70.0
const GRID_KILL_FORCE := 10.0
const GRID_KILL_RADIUS := 170.0
const GRID_BLAST_FORCE := 45.0
const GRID_BLAST_RADIUS := 340.0
const GRID_DEATH_FORCE := 80.0
const GRID_DEATH_RADIUS := 520.0

var game_over := false
var total_destroyed := 0

var _first_tutorial_ticks := 0
var _second_tutorial_ticks := 0
var _new_highscore := false


func _ready() -> void:
	time_scale = 1.0
	Juice.reset()
	Services.show_banner_ad(false)

	hero.position = Constants.center()
	hero.particle_hit.connect(_on_hero_particle_hit)
	hero.dash_hit.connect(_on_hero_dash_hit)
	hero.dashed.connect(_on_hero_dashed)
	hero.dash_interrupted.connect(func(kind): Telemetry.dash_interrupted(kind); effects.add_child(NeonBurst.make(hero.global_position, Constants.theme().hero, 0.5, 10, 0.25)))
	hero.died.connect(_on_hero_died)
	for saw in $Arena.saws:
		saw.overheated.connect(_on_saw_overheated)
	hero.energy_changed.connect(hud.set_energy)
	hud.set_energy(hero.energy)

	score.score_changed.connect(hud.set_score)
	score.score_changed.connect(_on_score_changed)
	score.combo_changed.connect(hud.set_combo)
	score.combo_changed.connect(func(_s, m, _b): Telemetry.multiplier(m))
	score.kill_streak.connect(hud.show_toast)
	score.kill_streak.connect(func(_text): Juice.shake_small(0.25, 0.4))
	score.kill_streak.connect(Telemetry.streak)
	score.combo_changed.connect(func(s, _m, _b): Telemetry.combo(s))
	powerups.buffs_changed.connect(hud.set_buffs)

	hud.dash_pressed.connect(func(): _try_dash(hero.move_input))
	hud.retry_pressed.connect(_restart)
	hud.tutorial_dismissed.connect(_on_second_tutorial_dismissed)

	for _i in Prefs.stage:
		spawner.spawn_particle(Constants.ParticleType.NORMAL)

	Telemetry.run_started(Prefs.stage)
	Telemetry.event("theme_" + Themes.current.id)
	game_over_saw.juicer = Themes.current.saw_style == GameTheme.SawStyle.JUICER
	game_over_saw.rim_color = Themes.current.saw
	tutorial_arrow.visible = not Prefs.first_tutorial_completed
	if tutorial_arrow.visible:
		tutorial_arrow.play()

	Audio.play_random_game_track()


func _physics_process(_delta: float) -> void:
	if game_over:
		return
	_update_input()
	_update_tutorials()
	spawner.tick()
	powerups.tick()
	score.tick()
	hud.set_combo_window(score.combo_window_left())
	hud.set_dash(hero.dash_cooldown_left(), powerups.shockwave_charges, hero.can_dash(), powerups.invincible_left())
	Telemetry.tick(hero.energy, _alive_particles(), score.score)
	_update_energy_alert()
	_ripple_grid()


func _alive_particles() -> int:
	var n := 0
	for p in particles.get_children():
		if p is Particle and p.alive:
			n += 1
	return n


## Moving bodies drag the spring grid along with them.
func _ripple_grid() -> void:
	var grid := world_fx.grid
	if hero.alive:
		grid.apply_directed_force(hero.linear_velocity, hero.global_position, GRID_HERO_RADIUS)
	for p in particles.get_children():
		if p is Particle and p.alive and p.type != Constants.ParticleType.INVISIBLE:
			grid.apply_directed_force(p.linear_velocity, p.global_position, GRID_ENEMY_RADIUS)


func _unhandled_input(event: InputEvent) -> void:
	if game_over:
		return
	if event.is_action_pressed("dash") or event.is_action_pressed("deploy_powerup"):
		_try_dash(hero.move_input)
	elif event is InputEventScreenTouch and event.pressed:
		# Tap on the arena (controls consume their own touches): dash along the
		# stick if it is held, otherwise toward the tap.
		var dir := hero.move_input
		if dir == Vector2.ZERO:
			var world: Vector2 = get_canvas_transform().affine_inverse() * event.position
			dir = world - hero.global_position
		_try_dash(dir)


## Applies the current dash upgrades, then dashes.
func _try_dash(dir: Vector2) -> bool:
	hero.dash_cost = powerups.dash_cost()
	hero.dash_length_ticks = powerups.dash_ticks()
	hero.dash_cooldown_ticks = powerups.dash_cooldown_ticks()
	if not hero.can_dash():
		return false
	hero.shockwave_dash = powerups.on_dash_started(hero.dash_length_ticks)
	return hero.dash(dir)


func _update_input() -> void:
	var input: Vector2 = joystick.vector
	if input == Vector2.ZERO:
		input = Input.get_vector("move_left", "move_right", "move_up", "move_down")
	hero.move_input = input


## First tutorial: show the arrow until the player has steered for a while, then
## drop the first enemy in. Second tutorial: pause and show the "how to play" card.
func _update_tutorials() -> void:
	if not Prefs.first_tutorial_completed:
		if hero.move_input != Vector2.ZERO:
			tutorial_arrow.visible = false
			_first_tutorial_ticks += 1
			if _first_tutorial_ticks > Constants.FIRST_TUTORIAL_MOVE_TICKS:
				Prefs.first_tutorial_completed = true
				Prefs.stage += 1
				spawner.spawn_particle(Constants.ParticleType.NORMAL)
	elif not Prefs.second_tutorial_completed:
		_second_tutorial_ticks += 1
		if _second_tutorial_ticks > Constants.SECOND_TUTORIAL_DELAY_TICKS and not hud.tutorial_visible():
			hud.show_tutorial()
			get_tree().paused = true


func _on_second_tutorial_dismissed() -> void:
	Prefs.second_tutorial_completed = true
	Prefs.save()
	get_tree().paused = false


func _update_energy_alert() -> void:
	if hero.energy <= 0:
		hero.die("energy")
	elif hero.energy <= Constants.HERO_LOW_ENERGY:
		Audio.start_alert()
	else:
		Audio.stop_alert()


# --- particles -------------------------------------------------------------

## Spawner calls this for every new particle so the Game hears its events.
func register_particle(p: Particle) -> void:
	p.killed.connect(_on_particle_killed)
	p.exploded.connect(_on_particle_exploded)
	p.wants_split.connect(_on_particle_wants_split)


func _on_hero_particle_hit(p: Particle) -> void:
	p.knock_back(hero.knockback_speed)
	hero.take_hit()
	Telemetry.hit(p.type, hero.energy)
	# Feedback stays on the hero (flinch) and the dial (blink). No contact burst:
	# at 5 hits/s the sparks were the main source of clutter around the hero.
	hud.blink_energy()
	if HIT_STOP_ON_HIT:
		Juice.hit_stop(0.05)
	Juice.shake_small(0.06, 0.2)


func _on_hero_dashed(dir: Vector2) -> void:
	var tint := Constants.theme().shockwave if hero.shockwave_dash else Constants.theme().hero
	effects.add_child(NeonBurst.make(hero.global_position - dir * 20.0, tint, 0.7, 16, 0.3))
	Audio.play_sound("speed", 0.5)
	Juice.shake_small(0.1, 0.25)
	Telemetry.dash()


## Dash impact: the particle is launched instead of hurting the hero; with a
## shockwave charge it simply dies.
func _on_hero_dash_hit(p: Particle) -> void:
	if hero.shockwave_dash:
		if not hero.shockwave_spent:
			hero.shockwave_spent = true
			powerups.consume_shockwave()
			effects.add_child(NeonBurst.make(hero.global_position, Color(2.4, 2.0, 0.6), 1.6, 40, 0.5))
			Audio.play_sound("bomb", 0.6)
		p.launched_ticks = Constants.DASH_KILL_WINDOW_TICKS
		p.kill()
		Juice.shake_small(0.15, 0.35)
		return
	var to_target := (p.global_position - hero.global_position).normalized()
	if to_target == Vector2.ZERO:
		to_target = hero.facing
	# Impact angle: head-on hits fly fastest and straight; glancing hits are
	# deflected along the contact normal with less force.
	var alignment := clampf(hero.facing.dot(to_target), 0.0, 1.0)
	Telemetry.dash_alignment(alignment)
	var perfect := alignment >= Constants.PERFECT_HIT_COS
	var force := Constants.PERFECT_LAUNCH_MULT if perfect else maxf(alignment * alignment, Constants.GLANCE_MIN_LAUNCH)
	var dir := hero.facing if perfect else to_target
	p.launch(dir, powerups.launch_speed() * force, perfect)
	Juice.shake_small(0.04, 0.2)
	if perfect:
		effects.add_child(NeonBurst.make(p.global_position, Color(2.6, 2.6, 2.8), 0.9, 14, 0.3))
		Telemetry.perfect_hit()
	Telemetry.dash_hit()


## An overheated wall blade throws a copy of itself straight into the arena,
## through whoever has been camping it. The heat gauge is the warning.
func _on_saw_overheated(saw: Saw) -> void:
	effects.add_child(NeonBurst.make(saw.global_position, Color(3.0, 2.2, 1.2), 1.2, 24, 0.5))
	Audio.play_sound("alert", 0.8)
	Juice.shake(0.25)
	Telemetry.overheat()
	if not overheat_throws:
		return
	var thrown: DeathSaw = Spawner.DEATH_SAW_SCENE.instantiate()
	thrown.position = saw.global_position
	var dir := saw.inward if saw.inward != Vector2.ZERO else (Constants.center() - saw.global_position).normalized()
	var speed := Constants.DEATH_SAW_SPEED_V if absf(dir.y) > absf(dir.x) else Constants.DEATH_SAW_SPEED_H
	thrown.velocity = dir * speed
	thrown.from_overheat = true
	thrown.launch_delay = 42
	thrown.scale = saw.scale
	death_saws.add_child(thrown)
	Telemetry.overheat_saw_thrown()


func _on_particle_killed(p: Particle) -> void:
	var blade_id := -1
	if p.killed_by is Saw:
		(p.killed_by as Saw).register_kill()
		blade_id = p.killed_by.get_index()
		Telemetry.saw_kill(blade_id)
	var dash_kill := p.launched_ticks > 0
	var perfect := dash_kill and p.launched_perfect
	if dash_kill:
		Telemetry.dash_kill()
	if perfect:
		Telemetry.perfect_kill()
	effects.add_child(NeonBurst.make(p.global_position, Constants.neon_for(p.type)))
	world_fx.grid.apply_explosive_force(GRID_KILL_FORCE, p.global_position, GRID_KILL_RADIUS)
	Juice.shake_small(0.08)
	if not game_over:
		Audio.play_particle_death()
	var before := hero.energy
	hero.reward_for_kill(p.type, dash_kill)
	Telemetry.energy_gained(hero.energy - before)
	Telemetry.kill(p.type)
	_remove_particle(p, blade_id, dash_kill, perfect)


func _on_particle_wants_split(p: Particle) -> void:
	spawner.spawn_particle(Constants.ParticleType.SPLIT, p.global_position)


## Suicide particle fuse ran out: everything within the blast radius dies.
func _on_particle_exploded(p: Particle) -> void:
	var center := p.global_position
	effects.add_child(NeonBurst.make(center, Constants.theme().explosion, 2.6, 110, 0.9))
	Audio.play_sound("bomb")
	Telemetry.blast()
	world_fx.grid.apply_explosive_force(GRID_BLAST_FORCE, center, GRID_BLAST_RADIUS)
	Juice.shake(0.45)
	Juice.hit_stop(0.07)
	Juice.chromatic(0.008, 0.35)
	if hero.alive and not hero.invincible and hero.global_position.distance_to(center) < Constants.BLAST_RADIUS:
		hero.die("blast")
	for other in particles.get_children():
		if other is Particle and other != p and other.alive and other.global_position.distance_to(center) < Constants.BLAST_RADIUS:
			other.alive = false
			Telemetry.kill(other.type)
			_remove_particle(other)
	_remove_particle(p)


## Mass-death power-up: wipe every enemy, each one restores a little energy.
func kill_all_particles() -> void:
	Juice.shake(0.5)
	Juice.flash(Color.WHITE, 0.12 if Themes.current.glow_intensity < 0.5 else 0.25, 0.3)
	world_fx.grid.apply_implosive_force(60.0, hero.global_position, 700.0)
	for other in particles.get_children():
		if other is Particle and other.alive:
			other.alive = false
			effects.add_child(NeonBurst.make(other.global_position, Constants.neon_for(other.type)))
			if hero.energy < 75:
				hero.energy += 5
				Telemetry.energy_gained(5)
			Telemetry.kill(other.type)
			_remove_particle(other)


## Shared bookkeeping for any particle leaving the arena: score, progression
## unlocks, respawn, difficulty.
func _remove_particle(p: Particle, blade_id := -1, dash_kill := false, perfect := false) -> void:
	var type := p.type
	p.queue_free()
	total_destroyed += 1
	if not game_over:
		var points := score.add_kill(blade_id, dash_kill, perfect)
		hud.show_points(p.global_position, points, score.multiplier, dash_kill, perfect)
	spawner.on_particle_removed(type)

	# Arena grows by one permanent particle at high-score milestones 10 / 15 / 20.
	if Prefs.highscore > 20 and not Prefs.third_stage_cleared:
		Prefs.third_stage_cleared = true
		Prefs.spawn_split_particles = true
		_grow_arena()
	elif Prefs.highscore > 15 and not Prefs.second_stage_cleared:
		Prefs.second_stage_cleared = true
		_grow_arena()
	elif Prefs.highscore > 10 and not Prefs.first_stage_cleared:
		Prefs.first_stage_cleared = true
		_grow_arena()

	match type:
		Constants.ParticleType.NORMAL:
			Prefs.normal_particles_destroyed += 1
			if not game_over:
				spawner.spawn_particle(Constants.ParticleType.NORMAL)
		Constants.ParticleType.SPLIT:
			Prefs.split_particles_destroyed += 1
		Constants.ParticleType.SUICIDE:
			Prefs.suicide_particles_destroyed += 1
		Constants.ParticleType.INVISIBLE:
			Prefs.invisible_particles_destroyed += 1

	if not Prefs.spawn_suicide_particles and Prefs.split_particles_destroyed > 20:
		Prefs.spawn_suicide_particles = true
	if not Prefs.spawn_invisible_particles and Prefs.suicide_particles_destroyed > 20:
		Prefs.spawn_invisible_particles = true


## High score follows every point, including combo bonuses (the original only
## checked on kills, so a combo payout right before dying was lost).
func _on_score_changed(value: int) -> void:
	if value > Prefs.highscore:
		Prefs.highscore = value
		_new_highscore = true


func _grow_arena() -> void:
	Prefs.stage += 1
	if not game_over:
		spawner.spawn_particle(Constants.ParticleType.NORMAL)


# --- game over ---------------------------------------------------------------

func _on_hero_died(cause: String) -> void:
	if game_over:
		return
	game_over = true
	Telemetry.buffs_at_death(powerups.passives, powerups.shockwave_charges)
	Telemetry.run_ended(cause, score.score, Prefs.highscore, spawner)
	effects.add_child(NeonBurst.make(hero.global_position, Constants.theme().hero, 2.2, 90, 1.1))
	world_fx.grid.apply_explosive_force(GRID_DEATH_FORCE, hero.global_position, GRID_DEATH_RADIUS)
	Juice.shake(1.0)
	var flat := Themes.current.glow_intensity < 0.5
	Juice.flash(Color.WHITE, 0.25 if flat else 0.6, 0.4)
	Juice.chromatic(0.006 if flat else 0.02, 0.3)
	Juice.slow_mo(0.2, 1.1)
	Audio.play_sound("hero_death")
	Audio.stop_music()
	Audio.stop_alert()
	powerups.clear_effects()
	_save_progress()

	# Freeze the arena; effects keep playing.
	particles.process_mode = Node.PROCESS_MODE_DISABLED
	death_saws.process_mode = Node.PROCESS_MODE_DISABLED
	pickups.process_mode = Node.PROCESS_MODE_DISABLED
	joystick.release()

	game_over_saw.visible = true
	hud.show_game_over(Prefs.highscore)


func _save_progress() -> void:
	Prefs.save()
	if _new_highscore:
		Services.submit_score(Prefs.highscore)
		_new_highscore = false


func _restart() -> void:
	Juice.reset()
	Services.show_interstitial_ad()
	get_tree().paused = false
	get_tree().reload_current_scene()


func _notification(what: int) -> void:
	# Mobile: save when the app is backgrounded (original saved in pause()).
	if what == NOTIFICATION_APPLICATION_PAUSED or what == NOTIFICATION_WM_CLOSE_REQUEST:
		Prefs.save(true)
