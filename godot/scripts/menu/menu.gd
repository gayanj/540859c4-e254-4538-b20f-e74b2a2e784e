extends Node2D
## Title screen. Port of MenuScreenWorldController / MenuScreenWorldRenderer.
## Enemy particles fly across the screen and burst on the spinning play-button saw.

const PARTICLE_SCENE := preload("res://scenes/entities/particle.tscn")
const GAME_SCENE := "res://scenes/game/game.tscn"
## The original spawned one particle of each type every 11 frames.
const SPAWN_INTERVAL_TICKS := 11
## Half-size of the play-button saw; particles never spawn inside it.
const SAW_HALF := 151.0

var _ticks := 0

@onready var _particles: Node2D = $Particles
@onready var _effects: Node2D = $Effects
@onready var _play_saw: Node2D = $PlayButtonSaw
@onready var _world_fx: WorldFx = $WorldFx


func _ready() -> void:
	Game.time_scale = 1.0
	Juice.reset()
	%PlayButton.pressed.connect(_start_game)
	%TrophyButton.pressed.connect(Services.show_achievements)
	%LeaderboardButton.pressed.connect(Services.show_leaderboard)
	%RateButton.pressed.connect(Services.rate_game)
	%ThemeButton.pressed.connect(func(): Juice.reset(); get_tree().change_scene_to_file("res://scenes/menu/cosmetics.tscn"))
	_refresh_theme_label()
	Audio.play_music("menu")
	Services.show_banner_ad(true)


func _physics_process(_delta: float) -> void:
	_ticks += 1
	for p in _particles.get_children():
		if p is Particle and p.alive:
			_world_fx.grid.apply_directed_force(p.linear_velocity, p.global_position, 70.0)
	if _ticks % SPAWN_INTERVAL_TICKS == 0:
		_spawn(Constants.ParticleType.NORMAL)
		_spawn(Constants.ParticleType.SPLIT)
		_spawn(Constants.ParticleType.SUICIDE)


func _spawn(type: Constants.ParticleType) -> void:
	var pos := Vector2(randf() * Constants.APP_WIDTH, randf() * Constants.APP_HEIGHT)
	var saw_rect := Rect2(_play_saw.position - Vector2.ONE * SAW_HALF, Vector2.ONE * SAW_HALF * 2)
	if saw_rect.has_point(pos):
		return
	# Original: direction = normalised position with random sign flips, 7 m/s.
	var dir := pos.normalized()
	if dir == Vector2.ZERO:
		dir = Vector2.ONE.normalized()
	dir.x *= -1 if randi() % 2 == 0 else 1
	dir.y *= -1 if randi() % 2 == 0 else 1
	var p: Particle = PARTICLE_SCENE.instantiate()
	p.setup_drift(type, dir * Constants.MENU_PARTICLE_SPEED)
	p.position = pos
	p.killed.connect(_on_particle_killed)
	_particles.add_child(p)


func _on_particle_killed(p: Particle) -> void:
	_effects.add_child(NeonBurst.make(p.global_position, Constants.neon_for(p.type)))
	_world_fx.grid.apply_explosive_force(10.0, p.global_position, 170.0)
	p.queue_free()


func _refresh_theme_label() -> void:
	%ThemeLabel.text = "COSMETICS: %s" % Themes.current.display_name


func _start_game() -> void:
	Juice.reset()
	get_tree().change_scene_to_file(GAME_SCENE)
