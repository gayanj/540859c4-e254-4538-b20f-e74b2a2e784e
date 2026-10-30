extends Node
## Repeatable "bot" run for balance comparisons: a progressed save, the hero
## steering in a slow circle around the centre, no power-up use. Plays until
## death or 90 s and prints the Telemetry summary.
##   godot --path godot tests/bot_run.tscn -- [hit_stop_cooldown]
const GAME_SCENE := preload("res://scenes/game/game.tscn")

func _ready() -> void:
	Telemetry.source = "bot"
	seed(1234)
	var args := OS.get_cmdline_user_args()
	if args.size() > 0:
		Juice.hit_stop_cooldown = float(args[0])
	Prefs.first_tutorial_completed = true
	Prefs.second_tutorial_completed = true
	Prefs.spawn_split_particles = true
	Prefs.spawn_suicide_particles = true
	Prefs.spawn_invisible_particles = true
	Prefs.stage = 5
	var game: Game = GAME_SCENE.instantiate()
	add_child(game)
	var t := 0.0
	while not game.game_over and t < 90.0:
		t += get_physics_process_delta_time()
		game.joystick.vector = Vector2(cos(t * 1.2), sin(t * 1.2)) * 0.9
		await get_tree().physics_frame
	await get_tree().create_timer(0.5, true, false, true).timeout
	Prefs.reset()
	get_tree().quit()
