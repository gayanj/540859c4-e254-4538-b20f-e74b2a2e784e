extends Node
## Logs the longest frames around hero death. godot --path godot tests/frame_probe.tscn
const GAME_SCENE := preload("res://scenes/game/game.tscn")
var _last := 0
var _samples: Array = []
var _recording := false
var _label := ""
var _events: Array = []
var _draw_total := 0
var _frame := 0
var _game: Game

func _ev(what: String) -> void:
	_events.append("%s@f%d" % [what, _frame])

func _ready() -> void:
	Telemetry.source = "test"
	Prefs.first_tutorial_completed = true
	Prefs.second_tutorial_completed = true
	Prefs.stage = 3
	var game: Game = GAME_SCENE.instantiate()
	_game = game
	add_child(game)
	game.hero.particle_hit.connect(func(_p): _ev("hit"))
	game.particles.child_entered_tree.connect(func(_n): _ev("spawn"))
	game.effects.child_entered_tree.connect(func(_n): _ev("burst"))
	game.pickups.child_entered_tree.connect(func(_n): _ev("pickup"))
	game.death_saws.child_entered_tree.connect(func(_n): _ev("deathsaw"))
	game.hero.died.connect(func(): _ev("DIED"))
	for i in 120:
		await get_tree().process_frame
	_last = Time.get_ticks_usec()
	_recording = true
	_label = "pre-death idle"
	for i in 60:
		await get_tree().process_frame
	_report()
	for test in [
		["slow_mo", func(): Juice.slow_mo(0.2, 1.1)],
		["flash+chromatic", func(): Juice.flash(Color.WHITE, 0.6, 0.5); Juice.chromatic(0.02, 0.8)],
		["big burst", func(): game.effects.add_child(NeonBurst.make(Vector2(540, 700), Constants.NEON_HERO, 2.2, 90, 1.1))],
		["grid shock", func(): game.world_fx.grid.apply_explosive_force(80.0, Vector2(540, 960), 520.0)],
		["sound", func(): Audio.play_sound("hero_death")],
		["stop music", func(): Audio.stop_music()],
		["show game over", func(): game.hud.show_game_over(123)],
		["prefs save", func(): Prefs.save()],
		["shake", func(): Juice.shake(1.0)],
	]:
		_label = test[0]
		test[1].call()
		for i in 90:
			await get_tree().process_frame
		_report()
	game.hud._game_over.visible = false
	_label = "death"
	game.hero.energy = 0
	for i in 90:
		await get_tree().process_frame
	_report()
	_label = "post-death"
	for i in 60:
		await get_tree().process_frame
	_report()
	Prefs.reset()
	get_tree().quit()

func _process(_d: float) -> void:
	if not _recording:
		return
	var now := Time.get_ticks_usec()
	var ms := (now - _last) / 1000.0
	_samples.append(ms)
	_last = now
	_frame += 1
	if ms > 20.0:
		print("  spike %.1fms at f%d  scale=%.2f  grid draw=%.1fms step=%.1fms  events: %s" % [ms, _frame, Engine.time_scale,
			SpringGrid.last_draw_us / 1000.0, SpringGrid.last_step_us / 1000.0, ", ".join(_events)])
	_draw_total += SpringGrid.last_draw_us
	_events.clear()

func _report() -> void:
	_samples.sort()
	var n := _samples.size()
	print("%-16s frames=%d  median=%.1fms  p95=%.1fms  max=%.1fms  >25ms: %d   avg grid draw=%.2fms" % [_label, n, _samples[n / 2], _samples[int(n * 0.95)], _samples[n - 1], _samples.filter(func(x): return x > 25.0).size(), _draw_total / 1000.0 / n])
	_samples.clear()
	_draw_total = 0
