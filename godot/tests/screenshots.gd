extends Node
## Captures reference screenshots of the menu and a mid-game frame to
## user://screenshots/ (needs a window; not headless).
##
## Run:  godot --path godot tests/screenshots.tscn

const MENU_SCENE := preload("res://scenes/menu/menu.tscn")
const GAME_SCENE := preload("res://scenes/game/game.tscn")

var _out_dir := OS.get_user_data_dir() + "/screenshots"
var _sustain_hero: Hero


func _physics_process(_d: float) -> void:
	if is_instance_valid(_sustain_hero) and _sustain_hero.alive:
		_sustain_hero.energy = 80


func _ready() -> void:
	Telemetry.source = "test"
	DirAccess.make_dir_recursive_absolute(_out_dir)
	Prefs.first_tutorial_completed = true
	Prefs.second_tutorial_completed = true
	Prefs.spawn_split_particles = true
	Prefs.spawn_suicide_particles = true
	Prefs.spawn_invisible_particles = true
	Prefs.stage = 3

	var menu := MENU_SCENE.instantiate()
	add_child(menu)
	await _frames(600)
	await _capture("menu.png")
	menu.queue_free()
	await _frames(2)

	var game: Game = GAME_SCENE.instantiate()
	add_child(game)
	await _frames(5)
	# Pretend to hold a power-up and steer a little so the HUD is populated.
	for i in 120:
		var a := i * 0.05
		game.joystick.vector = Vector2(cos(a), sin(a))
		await get_tree().process_frame
	game.spawner.spawn_particle(Constants.ParticleType.SUICIDE, Vector2(300, 500))
	game.spawner.spawn_particle(Constants.ParticleType.INVISIBLE, Vector2(800, 1300))
	game.spawner.spawn_pickup(Constants.PowerupType.SUPER_FORCE)
	game.spawner._spawn_death_saw()
	game.hero.energy = 35
	for t in [Constants.PowerupType.SPEED, Constants.PowerupType.SPEED, Constants.PowerupType.SLOW_MOTION, Constants.PowerupType.MASS_DEATH]:
		var pk: PowerupPickup = preload("res://scenes/entities/powerup_pickup.tscn").instantiate()
		pk.setup(t)
		game.pickups.add_child(pk)
		game.powerups.collect(pk)
		pk.queue_free()
	for k in 7:
		game.score.add_kill(k % 3)
	await _frames(45)
	await _capture("game.png")
	# Hit feedback frame: push an enemy into the hero and grab the next frames.
	var attacker: Particle = game.particles.get_child(0)
	attacker.global_position = game.hero.global_position + Vector2(40, 0)
	attacker.linear_velocity = Vector2(-900, 0)
	await _frames(6)
	await _capture("hit.png")
	# Saw heat + overheat + death-saw telegraph + dash.
	var arena_saws: Array = game.get_node("Arena").saws
	var hot: Saw = arena_saws[3]
	hot.heat = 1.8
	var cold: Saw = arena_saws[4]
	cold.register_kill(); cold.register_kill(); cold.register_kill()
	game.spawner._spawn_death_saw()
	game.hero.global_position = Vector2(540, 1100)
	game.hero.linear_velocity = Vector2.ZERO
	game.hero.dash(Vector2.UP)
	await _frames(5)
	await _capture("mechanics.png")
	for s in game.death_saws.get_children():
		s.free()  # the thrown blade would run through the centre
	game.overheat_throws = false
	await _frames(30)
	game.hero.global_position = Vector2(540, 960)
	game.hero.linear_velocity = Vector2.ZERO
	# Crowd + stationary mines frame.
	var armed := game.spawner.spawn_particle(Constants.ParticleType.INVISIBLE, Vector2(760, 1300))
	armed._phase_ticks = Particle.INVISIBLE_FADE_TICKS - 1
	var forming := game.spawner.spawn_particle(Constants.ParticleType.INVISIBLE, Vector2(300, 1400))
	forming._phase_ticks = 20
	game.hero.energy = 80
	game.hero.global_position = Vector2(540, 960)
	game.hero.linear_velocity = Vector2.ZERO
	for k in 7:
		var crowd := game.spawner.spawn_particle(Constants.ParticleType.NORMAL, Vector2(540, 960) + Vector2(62, 0).rotated(k * 0.9))
		crowd._hero = null
		crowd.linear_velocity = Vector2.ZERO
		crowd.freeze = true
	for k in 12:
		game.hero.global_position = Vector2(540, 960)
		game.hero.linear_velocity = Vector2.ZERO
		await get_tree().process_frame
	await _capture("crowd.png")
	for c in game.particles.get_children():
		if c is Particle and c.freeze:
			c.queue_free()
	# Mine readability frame: a mine 60% through its fuse beside a pickup.
	var mine := game.spawner.spawn_particle(Constants.ParticleType.SUICIDE, Vector2(300, 1200))
	mine._fuse_ticks = int(Constants.SUICIDE_BLAST_TICKS * 0.6)
	mine._hero = null  # hold still for the shot
	var item: PowerupPickup = preload("res://scenes/entities/powerup_pickup.tscn").instantiate()
	item.setup(Constants.PowerupType.ENERGY)
	item.position = Vector2(700, 1200)
	game.pickups.add_child(item)
	await _frames(8)
	await _capture("mine.png")
	# Mid-blast frame: suicide particle detonating next to the hero's old spot.
	var bomb := game.spawner.spawn_particle(Constants.ParticleType.SUICIDE, Vector2(540, 1100))
	bomb._fuse_ticks = Constants.SUICIDE_BLAST_TICKS - 1
	await _frames(14)
	await _capture("blast.png")
	await _frames(40)
	game.hero.energy = 0
	await _frames(40)
	await _capture("game_over.png")
	game.free()
	# Fruit theme pass: menu + a busy game frame.
	Themes.select("fruit", false)
	var menu2 := MENU_SCENE.instantiate()
	add_child(menu2)
	await _frames(300)
	await _capture("fruit_menu.png")
	menu2.queue_free()
	await _frames(2)
	Prefs.stage = 4
	var g2: Game = GAME_SCENE.instantiate()
	add_child(g2)
	g2.hero.invincible = true
	_sustain_hero = g2.hero
	for i in 40:
		var a := i * 0.05
		g2.joystick.vector = Vector2(cos(a), sin(a))
		await get_tree().process_frame
	g2.hero.energy = 80
	g2.spawner.spawn_particle(Constants.ParticleType.SUICIDE, Vector2(200, 1500))
	var fm := g2.spawner.spawn_particle(Constants.ParticleType.INVISIBLE, Vector2(760, 1300))
	fm._phase_ticks = Particle.INVISIBLE_FADE_TICKS - 1
	g2.spawner.spawn_pickup(Constants.PowerupType.SPEED)
	g2.hero.energy = 50
	g2.hero.energy = 80
	g2.hero.invincible = true
	for c in g2.particles.get_children():
		if c is Particle and c.type == Constants.ParticleType.NORMAL:
			c.global_position = Vector2(200, 1500)
	for k in 30:
		g2.hero.global_position = Vector2(540, 760)
		g2.hero.linear_velocity = Vector2(600, -300)
		g2.hero.linear_velocity = Vector2.ZERO
		g2.hero.facing = Vector2(1, -0.4).normalized()
		g2.hero._body.facing = g2.hero.facing
		await get_tree().process_frame
	await _capture("fruit_game.png")
	_sustain_hero = null
	g2.hero.invincible = false
	g2.hero.energy = 0
	await _frames(40)
	await _capture("fruit_game_over.png")
	g2.queue_free()
	Themes.select("neon", false)
	Prefs.reset()
	print("Screenshots written to " + _out_dir)
	get_tree().quit()


func _frames(n: int) -> void:
	for _i in n:
		await get_tree().process_frame


func _capture(name: String) -> void:
	await RenderingServer.frame_post_draw
	var img := get_viewport().get_texture().get_image()
	img.save_png(_out_dir + "/" + name)
