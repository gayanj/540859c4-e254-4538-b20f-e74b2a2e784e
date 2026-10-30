extends Node2D
## FX gallery: stages each visual effect in isolation on the live field so it
## can be judged without playing for it. Not part of any automated run.
##
##   godot --path godot tests/fx_gallery.tscn            # cycles every exhibit, ~5 s each
##   godot --path godot tests/fx_gallery.tscn -- fruit   # start in a theme
##   godot --path godot tests/fx_gallery.tscn -- fruit invincible   # one exhibit, loops
##
## Keys: Space/Right = next, Left = previous, T = cycle theme, Esc = quit.
## Exhibits: invincible, dash, perfect, mine_fuse, mine_stationary, overheat,
##           death, shockwave, combo

const WORLD_FX := preload("res://scenes/fx/world_fx.tscn")
const HERO := preload("res://scenes/entities/hero.tscn")
const PARTICLE := preload("res://scenes/entities/particle.tscn")
const SAW := preload("res://scenes/entities/saw.tscn")

const EXHIBITS := ["invincible", "dash", "perfect", "mine_fuse", "mine_stationary", "overheat", "death", "shockwave", "combo"]
const HOLD := 5.0

var _index := 0
var _only := ""
var _stage: Node2D
var _label: Label
var _timer := 0.0
var _hero: Hero
var _t := 0.0
var _shot_after := -1.0


func _ready() -> void:
	Telemetry.enabled = false
	var args := OS.get_cmdline_user_args()
	if args.size() > 0 and Themes.all.has(args[0]):
		Themes.select(args[0], false)
	if args.size() > 1 and args[1] in EXHIBITS:
		_only = args[1]
		_index = EXHIBITS.find(_only)
	# Optional third arg: write a screenshot after N seconds and quit (used by review scripts).
	if args.size() > 2:
		_shot_after = float(args[2])
	add_child(WORLD_FX.instantiate())
	_stage = Node2D.new()
	add_child(_stage)
	var layer := CanvasLayer.new()
	add_child(layer)
	_label = Label.new()
	_label.position = Vector2(30, 1800)
	_label.size = Vector2(1020, 80)
	_label.label_settings = LabelSettings.new()
	_label.label_settings.font = preload("res://assets/fonts/BebasNeue.otf")
	_label.label_settings.font_size = 48
	_label.label_settings.font_color = Color(1, 1, 1, 0.85)
	layer.add_child(_label)
	_show(_index)


func _unhandled_input(event: InputEvent) -> void:
	if event is InputEventKey and event.pressed:
		match event.keycode:
			KEY_SPACE, KEY_RIGHT:
				_show(_index + 1)
			KEY_LEFT:
				_show(_index - 1)
			KEY_T:
				Themes.next()
				_show(_index)
			KEY_ESCAPE:
				get_tree().quit()


func _process(delta: float) -> void:
	_t += delta
	_timer += delta
	if _shot_after > 0.0 and _t >= _shot_after:
		_shot_after = -1.0
		await RenderingServer.frame_post_draw
		var dir := OS.get_user_data_dir() + "/screenshots"
		DirAccess.make_dir_recursive_absolute(dir)
		get_viewport().get_texture().get_image().save_png("%s/fx_%s_%s.png" % [dir, Themes.current.id, EXHIBITS[_index]])
		get_tree().quit()
		return
	if _timer >= HOLD:
		_show(_index + 1 if _only == "" else _index)
	for c in _stage.get_children():
		if c is Particle and c.has_meta("pin"):
			c.global_position = c.get_meta("pin")
			c.linear_velocity = Vector2.ZERO
	# Keep a staged hero drifting in a slow figure-eight so flames/trails have wind.
	if is_instance_valid(_hero) and _hero.alive and _hero.get_meta("drift", false):
		_hero.move_input = Vector2(cos(_t * 1.1), sin(_t * 2.2) * 0.6)
		_hero.global_position = Vector2(540, 900) + Vector2(sin(_t * 1.1), sin(_t * 2.2) * 0.5) * 220.0
		_hero.linear_velocity = _hero.move_input * Constants.HERO_SPEED_BASE


func _show(i: int) -> void:
	_index = posmod(i, EXHIBITS.size())
	_timer = 0.0
	for c in _stage.get_children():
		c.free()
	_hero = null
	Juice.reset()
	Game.time_scale = 1.0
	var name: String = EXHIBITS[_index]
	_label.text = "%s  [%s]   space: next  T: theme  esc: quit" % [name.to_upper().replace("_", " "), Themes.current.display_name]
	call("_x_" + name)


# --- exhibits ----------------------------------------------------------------------

func _spawn_hero(pos := Vector2(540, 900), drift := true) -> Hero:
	var h: Hero = HERO.instantiate()
	h.position = pos
	h.set_meta("drift", drift)
	_stage.add_child(h)
	_hero = h
	return h


func _spawn_particle(type: Constants.ParticleType, pos: Vector2) -> Particle:
	var p: Particle = PARTICLE.instantiate()
	p.setup(type, null, 0.0)
	p.position = pos
	_stage.add_child(p)
	p.freeze = true
	return p


func _x_invincible() -> void:
	var h := _spawn_hero()
	h.invincible = true


func _x_dash() -> void:
	var h := _spawn_hero(Vector2(540, 1200), false)
	h.facing = Vector2.UP
	h.move_input = Vector2.ZERO
	# Repeated dashes up and back.
	var tw := create_tween().set_loops()
	tw.tween_callback(func(): if is_instance_valid(h): h.global_position = Vector2(540, 1250); h.linear_velocity = Vector2.ZERO; h.dash(Vector2.UP))
	tw.tween_interval(1.2)
	tw.tween_callback(func(): if is_instance_valid(h): h.global_position = Vector2(540, 650); h.linear_velocity = Vector2.ZERO; h.dash(Vector2.DOWN))
	tw.tween_interval(1.2)


func _x_perfect() -> void:
	# Burst a line of perfect-hit flashes, one per second, and the white "PERFECT" tint.
	var tw := create_tween().set_loops()
	for i in 4:
		var pos := Vector2(300 + i * 160, 900)
		tw.tween_callback(func(): _stage.add_child(NeonBurst.make(pos, Color(2.6, 2.6, 2.8), 0.9, 14, 0.3)))
		tw.tween_interval(0.7)


func _x_mine_fuse() -> void:
	# Three suicide particles at different fuse points; fuses tick, bodies are
	# pinned each frame (the fuse needs a target to count down).
	var anchor := Node2D.new()
	anchor.position = Vector2(540, 1500)
	_stage.add_child(anchor)
	for i in 3:
		var p := _spawn_particle(Constants.ParticleType.SUICIDE, Vector2(300 + i * 240, 900))
		p._hero = anchor
		p._fuse_ticks = int(Constants.SUICIDE_BLAST_TICKS * (0.2 + 0.3 * i))
		p.freeze = false
		p.set_meta("pin", p.position)
		p.exploded.connect(func(q):
			_stage.add_child(NeonBurst.make(q.global_position, Constants.theme().explosion, 2.6, 110, 0.9))
			q.queue_free())


func _x_mine_stationary() -> void:
	var labels := ["forming", "armed", "fading"]
	var ticks := [10, Particle.INVISIBLE_FADE_TICKS + 5, 0]
	for i in 3:
		var p := _spawn_particle(Constants.ParticleType.INVISIBLE, Vector2(300 + i * 240, 900))
		p._phase_ticks = ticks[i]
		if i == 2:
			p._phase = Particle.InvisiblePhase.DISAPPEARING
			p._phase_ticks = 20


func _x_overheat() -> void:
	# Three blades: cold, hot (gauge near full), dull (cooling).
	var states := [0.0, 1.8, -1.0]
	for i in 3:
		var s: Saw = SAW.instantiate()
		s.position = Vector2(300 + i * 240, 900)
		_stage.add_child(s)
		if states[i] < 0.0:
			s.register_kill(); s.register_kill(); s.register_kill()
		else:
			s.heat = states[i]
	# Re-heat the middle one so it visibly climbs and overheats.
	var tw := create_tween().set_loops()
	tw.tween_interval(2.0)
	tw.tween_callback(func():
		var s: Saw = _stage.get_child(1) if _stage.get_child_count() > 1 else null
		if s: s.register_kill())


func _x_death() -> void:
	var h := _spawn_hero(Vector2(540, 900), false)
	var tw := create_tween().set_loops()
	tw.tween_interval(0.8)
	tw.tween_callback(func():
		if not is_instance_valid(h): return
		_stage.add_child(NeonBurst.make(h.global_position, Constants.theme().hero, 2.2, 90, 1.1))
		Juice.shake(1.0)
		var flat := Themes.current.glow_intensity < 0.5
		Juice.flash(Color.WHITE, 0.25 if flat else 0.6, 0.4)
		Juice.chromatic(0.006 if flat else 0.02, 0.3)
		Juice.slow_mo(0.2, 1.1))
	tw.tween_interval(2.5)


func _x_shockwave() -> void:
	var h := _spawn_hero(Vector2(540, 1250), false)
	h.facing = Vector2.UP
	var tw := create_tween().set_loops()
	tw.tween_callback(func():
		if not is_instance_valid(h): return
		for c in _stage.get_children():
			if c is Particle: c.free()
		for i in 4:
			var p := _spawn_particle(Constants.ParticleType.NORMAL, Vector2(540, 1050 - i * 110))
			p.freeze = false
			p.linear_velocity = Vector2.ZERO
		h.global_position = Vector2(540, 1250)
		h.linear_velocity = Vector2.ZERO
		h.shockwave_dash = true
		h.dash_cost = 0
		h.dash(Vector2.UP))
	tw.tween_interval(2.0)


func _x_combo() -> void:
	# The "+N" pops and the kill splat/burst at a few multipliers.
	var tw := create_tween().set_loops()
	for i in 4:
		var pos := Vector2(250 + i * 190, 900)
		var mult: int = [1, 8, 32, 120][i]
		tw.tween_callback(func():
			_stage.add_child(NeonBurst.make(pos, Constants.neon_for(Constants.ParticleType.NORMAL)))
			_pop(pos, 10 * mult, mult))
		tw.tween_interval(0.5)
	tw.tween_interval(1.5)


func _pop(pos: Vector2, points: int, mult: int) -> void:
	var l := Label.new()
	l.text = "+%d" % points
	l.label_settings = LabelSettings.new()
	l.label_settings.font = preload("res://assets/fonts/BebasNeue.otf")
	l.label_settings.font_size = 34
	l.label_settings.shadow_size = 6
	l.label_settings.shadow_color = Color(0, 0, 0, 0.7)
	l.position = pos + Vector2(-60, -70)
	l.size = Vector2(120, 40)
	l.horizontal_alignment = HORIZONTAL_ALIGNMENT_CENTER
	l.pivot_offset = Vector2(60, 20)
	l.scale = Vector2.ONE * clampf(0.65 + mult * 0.02, 0.65, 1.4)
	l.modulate = Color(1.6, 1.3, 0.4) if mult > 1 else Color(1, 1, 1, 0.8)
	_stage.add_child(l)
	var tw := create_tween().set_parallel(true)
	tw.tween_property(l, "position:y", l.position.y - 50.0, 0.45).set_ease(Tween.EASE_OUT)
	tw.tween_property(l, "modulate:a", 0.0, 0.3).set_delay(0.15)
	tw.chain().tween_callback(l.queue_free)
