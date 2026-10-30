class_name Spawner
extends Node
## Enemy, power-up and death-saw spawning plus the difficulty ramp.
## Port of the create*/increaseDifficulty methods in GameScreenWorldController.
##
## Progression gates live in [Prefs] (they persist between sessions); the
## per-run tunables (speeds, timers) live here and reset with every new game.

const PARTICLE_SCENE := preload("res://scenes/entities/particle.tscn")
const DEATH_SAW_SCENE := preload("res://scenes/entities/death_saw.tscn")
const PICKUP_SCENE := preload("res://scenes/entities/powerup_pickup.tscn")

## Spawn area margins (px) copied from the original random ranges.
const SPAWN_MARGIN_X := 100
const SPAWN_MARGIN_Y := 350
## Enemies never appear closer than this to the hero.
const MIN_SPAWN_DISTANCE := 320.0
## Mines in one wave appear one at a time, this many ticks apart.
const WAVE_STAGGER_TICKS := 30

var _pending_mines := 0
var _stagger_ticks := 0

var game: Game

# Per-run difficulty state.
var normal_speed := Constants.NORMAL_PARTICLE_SPEED_BASE
var split_time := Constants.SPLIT_PARTICLE_TIME_BASE
var death_saw_time := Constants.DEATH_SAW_TIME_BASE
var suicide_count := Constants.SUICIDE_PARTICLE_COUNT_BASE

var split_alive := 0
var suicide_alive := 0

var _difficulty_pending := true
var _invisible_ticks := 0
var _death_saw_ticks := 0
var _powerup_ticks := 0
var _instant_powerup_ticks := 0


func _ready() -> void:
	game = get_parent() as Game


## Called once per physics tick while the game is running.
func tick() -> void:
	_increase_difficulty()
	_maybe_spawn_timed_powerup()
	_maybe_spawn_instant_powerup()
	_maybe_spawn_split()
	_maybe_spawn_suicide()
	_maybe_spawn_invisible()
	_maybe_spawn_death_saw()
	for p in game.particles.get_children():
		if p is Particle and p.type == Constants.ParticleType.SPLIT and split_alive < Constants.MAX_SPLIT_PARTICLES_ALIVE:
			p.poll_split(split_time)


## Bookkeeping when a particle leaves the arena (killed or exploded).
func on_particle_removed(type: Constants.ParticleType) -> void:
	match type:
		Constants.ParticleType.SPLIT:
			split_alive -= 1
		Constants.ParticleType.SUICIDE:
			suicide_alive -= 1
	_difficulty_pending = true


func random_spawn_position() -> Vector2:
	var hero_pos: Vector2 = game.hero.global_position if game.hero else Constants.center()
	var best := Vector2.ZERO
	var best_d := -1.0
	for _i in 12:
		var p := Vector2(
			randi_range(SPAWN_MARGIN_X, Constants.APP_WIDTH - SPAWN_MARGIN_X),
			randi_range(SPAWN_MARGIN_Y, Constants.APP_HEIGHT - SPAWN_MARGIN_Y)
		)
		var d := p.distance_to(hero_pos)
		if d >= MIN_SPAWN_DISTANCE:
			return p
		if d > best_d:
			best_d = d
			best = p
	return best  # arena too crowded near the hero; take the farthest candidate


func spawn_particle(type: Constants.ParticleType, at: Variant = null) -> Particle:
	var p: Particle = PARTICLE_SCENE.instantiate()
	var speed := normal_speed
	match type:
		Constants.ParticleType.SPLIT:
			speed = Constants.SPLIT_PARTICLE_SPEED
			split_alive += 1
		Constants.ParticleType.SUICIDE:
			speed = Constants.SUICIDE_PARTICLE_SPEED
			suicide_alive += 1
		Constants.ParticleType.INVISIBLE:
			speed = Constants.INVISIBLE_PARTICLE_SPEED
	p.setup(type, game.hero, speed)
	p.position = at if at != null else random_spawn_position()
	game.register_particle(p)
	game.particles.add_child(p)
	Telemetry.spawn(type, game.particles.get_child_count(), p.position.distance_to(game.hero.global_position), suicide_alive)
	return p


func spawn_pickup(type: Constants.PowerupType) -> void:
	var pickup: PowerupPickup = PICKUP_SCENE.instantiate()
	pickup.setup(type)
	pickup.position = random_spawn_position()
	pickup.collected.connect(game.powerups.collect)
	game.pickups.add_child(pickup)


func _increase_difficulty() -> void:
	var destroyed := game.total_destroyed
	if not (Prefs.spawn_suicide_particles and destroyed > 0 and destroyed % 10 == 0 and _difficulty_pending):
		return
	if normal_speed <= 9.0 * Constants.PX_PER_M:
		normal_speed += 1.0 * Constants.PX_PER_M
	if split_time > Constants.SPLIT_PARTICLE_TIME_MIN:
		split_time -= 10
	if death_saw_time > Constants.DEATH_SAW_TIME_MIN:
		death_saw_time -= 10
	_difficulty_pending = false


func _maybe_spawn_split() -> void:
	var destroyed := game.total_destroyed
	if Prefs.spawn_split_particles and destroyed > 0 and destroyed % 10 == 0 and split_alive == 0:
		spawn_particle(Constants.ParticleType.SPLIT)


func _maybe_spawn_suicide() -> void:
	var destroyed := game.total_destroyed
	# Waves are staggered: the original dropped up to six mines on the same tick,
	# which could box the player in with no warning.
	if _pending_mines > 0:
		_stagger_ticks += 1
		if _stagger_ticks >= WAVE_STAGGER_TICKS:
			_stagger_ticks = 0
			_pending_mines -= 1
			spawn_particle(Constants.ParticleType.SUICIDE)
		return
	if Prefs.spawn_suicide_particles and destroyed > 0 and destroyed % 15 == 0 and suicide_alive == 0:
		spawn_particle(Constants.ParticleType.SUICIDE)
		_pending_mines = suicide_count - 1
		_stagger_ticks = 0
		Telemetry.event("mine_wave_%d" % suicide_count)
		if suicide_count < Constants.SUICIDE_PARTICLE_COUNT_MAX:
			suicide_count += 1


func _maybe_spawn_invisible() -> void:
	_invisible_ticks += 1
	if not (Prefs.spawn_invisible_particles and game.total_destroyed > 10 and _invisible_ticks > Constants.INVISIBLE_SPAWN_TICKS):
		return
	if game.hero.dashing or _mines_alive() >= Constants.MAX_MINES_ALIVE:
		return  # try again next tick
	var pos := _mine_position()
	if pos == Vector2.INF:
		return
	spawn_particle(Constants.ParticleType.INVISIBLE, pos)
	_invisible_ticks = 0


func _mines_alive() -> int:
	var n := 0
	for p in game.particles.get_children():
		if p is Particle and p.alive and p.type == Constants.ParticleType.INVISIBLE:
			n += 1
	return n


## A spot far from the hero and from other mines, or INF if none found.
func _mine_position() -> Vector2:
	var hero_pos: Vector2 = game.hero.global_position
	for _i in 16:
		var p := Vector2(
			randi_range(SPAWN_MARGIN_X + 80, Constants.APP_WIDTH - SPAWN_MARGIN_X - 80),
			randi_range(SPAWN_MARGIN_Y, Constants.APP_HEIGHT - SPAWN_MARGIN_Y)
		)
		if p.distance_to(hero_pos) < Constants.MINE_MIN_HERO_DISTANCE:
			continue
		var ok := true
		for m in game.particles.get_children():
			if m is Particle and m.type == Constants.ParticleType.INVISIBLE and m.global_position.distance_to(p) < Constants.MINE_MIN_MINE_DISTANCE:
				ok = false
				break
		if ok:
			return p
	return Vector2.INF


func _maybe_spawn_death_saw() -> void:
	if Prefs.spawn_suicide_particles and game.total_destroyed > 20 and _death_saw_ticks > death_saw_time:
		_spawn_death_saw()
		_death_saw_ticks = 0
	else:
		_death_saw_ticks += 1


## Death saws start half off-screen at a random edge slot and sweep across.
func _spawn_death_saw() -> void:
	var saw: DeathSaw = DEATH_SAW_SCENE.instantiate()
	var half := 66.0
	var w := Constants.APP_WIDTH
	var h := Constants.APP_HEIGHT
	var side := randi() % 4
	var vslot := randi_range(2, (h / 130) - 2)
	var hslot := randi_range(2, (w / 130) - 3)
	var y_gdx := -half * (2 * (vslot + 1)) + h + half
	var x := -half * (2 * (hslot + 1)) + w + half
	match side:
		0:  # from the right, moving left
			saw.position = Vector2(w, Constants.flip_y(y_gdx))
			saw.velocity = Vector2(-Constants.DEATH_SAW_SPEED_H, 0)
		1:  # from the left, moving right
			saw.position = Vector2(0, Constants.flip_y(y_gdx))
			saw.velocity = Vector2(Constants.DEATH_SAW_SPEED_H, 0)
		2:  # from the top, moving down
			saw.position = Vector2(x, -half)
			saw.velocity = Vector2(0, Constants.DEATH_SAW_SPEED_V)
		3:  # from the bottom, moving up
			saw.position = Vector2(x, h + half)
			saw.velocity = Vector2(0, -Constants.DEATH_SAW_SPEED_V)
	game.death_saws.add_child(saw)
	Telemetry.death_saw()


func _maybe_spawn_timed_powerup() -> void:
	if Prefs.spawn_split_particles and _powerup_ticks > Constants.POWERUP_SPAWN_TICKS:
		spawn_pickup(Constants.PASSIVE_POWERUPS[randi() % Constants.PASSIVE_POWERUPS.size()])
		_powerup_ticks = 0
	else:
		_powerup_ticks += 1


func _maybe_spawn_instant_powerup() -> void:
	if not (Prefs.spawn_split_particles and _instant_powerup_ticks > Constants.INSTANT_POWERUP_SPAWN_TICKS):
		_instant_powerup_ticks += 1
		return
	_instant_powerup_ticks = 0
	match randi() % 4:
		0, 1:
			if game.hero.energy < 70:
				spawn_pickup(Constants.PowerupType.ENERGY)
		2:
			# Original also required score % 10000 == 0, which made it effectively
			# unobtainable; now it drops once "BEYOND GODLIKE" has been earned.
			if Prefs.is_achievement_unlocked("beyond_god_like"):
				spawn_pickup(Constants.PowerupType.INVINCIBILITY)
		3:
			spawn_pickup(Constants.PowerupType.MASS_DEATH)
