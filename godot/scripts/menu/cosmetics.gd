extends Node2D
## Cosmetics screen: pick a theme and see the hero live before playing.
## The stage is the real field (WorldFx, grid, glow) with the real Hero scene
## drifting on it; the buttons fire the real effects (dash, invincibility fire,
## a kill burst) so what the player sees is exactly what they get.

const WORLD_FX := preload("res://scenes/fx/world_fx.tscn")
const HERO := preload("res://scenes/entities/hero.tscn")
const PARTICLE := preload("res://scenes/entities/particle.tscn")
const MENU_SCENE := "res://scenes/menu/menu.tscn"
const GAME_SCENE := "res://scenes/game/game.tscn"

const STAGE_CENTER := Vector2(540, 620)
const STAGE_RADIUS := 170.0

var _hero: Hero
var _t := 0.0
var _burn_left := 0.0
var _tabs: Array[Button] = []
var _world_fx: WorldFx
## Seconds the stage steering stays off after a dash, so the hero coasts out of
## it instead of being yanked back onto the path (which also reads as a cancel).
var _steer_hold := 0.0
var _path_offset := Vector2.ZERO


func _path_point() -> Vector2:
	return STAGE_CENTER + Vector2(sin(_t * 0.9), sin(_t * 1.8) * 0.5) * STAGE_RADIUS

@onready var _stage: Node2D = $Stage
@onready var _tab_row: HBoxContainer = %Tabs
@onready var _title: Label = %ThemeTitle
@onready var _desc: Label = %ThemeDesc


func _ready() -> void:
	Juice.reset()
	Game.time_scale = 1.0
	Telemetry.enabled = false
	_world_fx = WORLD_FX.instantiate()
	add_child(_world_fx)
	move_child(_world_fx, 0)
	_build_tabs()
	_spawn_hero()
	_spawn_fruit_ring()
	Themes.theme_changed.connect(_on_theme_changed)
	%DashButton.pressed.connect(_try_dash)
	%BurnButton.pressed.connect(_try_burn)
	%KillButton.pressed.connect(_try_kill)
	%BackButton.pressed.connect(func(): get_tree().change_scene_to_file(MENU_SCENE))
	%PlayButton.pressed.connect(func(): Telemetry.enabled = true; get_tree().change_scene_to_file(GAME_SCENE))
	_refresh()


func _exit_tree() -> void:
	Telemetry.enabled = true


func _build_tabs() -> void:
	for id in Themes.ORDER:
		var t: GameTheme = Themes.all[id]
		var b := Button.new()
		b.text = t.display_name
		b.toggle_mode = true
		b.custom_minimum_size = Vector2(0, 90)
		b.size_flags_horizontal = Control.SIZE_EXPAND_FILL
		b.add_theme_font_override("font", preload("res://assets/fonts/BebasNeue.otf"))
		b.add_theme_font_size_override("font_size", 44)
		b.pressed.connect(func(): Themes.select(id))
		_tab_row.add_child(b)
		_tabs.append(b)


func _spawn_hero() -> void:
	if is_instance_valid(_hero):
		_hero.free()
	_hero = HERO.instantiate()
	_hero.position = STAGE_CENTER
	_stage.add_child(_hero)
	# Purely visual on this stage: Hero._ready sets its own layers, so override
	# after it is in the tree, and make it immortal regardless.
	_hero.collision_layer = 0
	_hero.collision_mask = 0
	_hero.immortal = true


## A few frozen particles around the stage so the theme's enemies are visible too.
func _spawn_fruit_ring() -> void:
	for c in _stage.get_children():
		if c is Particle:
			c.free()
	var types := [Constants.ParticleType.NORMAL, Constants.ParticleType.SPLIT, Constants.ParticleType.SUICIDE, Constants.ParticleType.INVISIBLE]
	for i in types.size():
		var p: Particle = PARTICLE.instantiate()
		p.setup(types[i], null, 0.0)
		var a := -PI / 2.0 + TAU * i / types.size()
		p.position = STAGE_CENTER + Vector2(cos(a), sin(a)) * (STAGE_RADIUS + 150.0)
		_stage.add_child(p)
		p.collision_layer = 0
		p.collision_mask = 0
		p.freeze = true
		if types[i] == Constants.ParticleType.INVISIBLE:
			# Show it armed: drive the mine body directly since the frozen
			# particle never ticks its phase.
			p._phase = Particle.InvisiblePhase.DEADLY
			p._mine.state = MineBody.State.ARMED
			p._mine.fade = 1.0


func _process(delta: float) -> void:
	_t += delta
	if _burn_left > 0.0:
		_burn_left -= delta
		if _burn_left <= 0.0 and is_instance_valid(_hero):
			_hero.invincible = false
	# Keep the stage particles where they are (physics still ticks them).
	for c in _stage.get_children():
		if c is Particle:
			c.linear_velocity = Vector2.ZERO


func _physics_process(delta: float) -> void:
	if not (is_instance_valid(_hero) and _hero.alive):
		return
	if _hero.dashing or _steer_hold > 0.0:
		_steer_hold -= delta
		_hero.move_input = Vector2.ZERO
		_world_fx.grid.apply_directed_force(_hero.linear_velocity, _hero.global_position, 90.0)
		if not _hero.dashing and _steer_hold <= 0.0:
			# Re-anchor the figure-eight on wherever the dash landed, so the
			# hero drifts on from here instead of being pulled back.
			_path_offset = _hero.global_position - _path_point()
		return
	# Steer the hero along a lazy figure-eight through its stick input, exactly
	# as the joystick would, so it moves under its own physics and the ribbon
	# records a smooth path (setting position each frame produced stair-steps:
	# the body only lands once per physics tick).
	# The offset decays so the path slowly recentres on the stage.
	_path_offset = _path_offset.lerp(Vector2.ZERO, clampf(delta * 0.5, 0.0, 1.0))
	var target := _path_point() + _path_offset
	var to_target := target - _hero.global_position
	var strength := clampf(to_target.length() / 60.0, 0.0, 1.0)
	_hero.move_input = to_target.normalized() * strength
	# Same grid drag the game applies, so the stage behaves like the arena.
	_world_fx.grid.apply_directed_force(_hero.linear_velocity, _hero.global_position, 90.0)


func _on_theme_changed(_t: GameTheme) -> void:
	# The field re-skins itself; entities read the theme at spawn, so respawn them.
	_spawn_hero()
	_spawn_fruit_ring()
	_refresh()


func _refresh() -> void:
	var cur := Themes.current
	for i in _tabs.size():
		_tabs[i].button_pressed = Themes.ORDER[i] == cur.id
	_title.text = cur.display_name
	_desc.text = _describe(cur)


func _describe(t: GameTheme) -> String:
	match t.id:
		"fruit":
			return "Kitchen knife vs. fruit. Juice splats, steel graters, a cutting board. Flat colours, no glow."
		_:
			return "Glow orbs, neon ribbons, gear blades, spark bursts on a spring lattice."


# --- try-it buttons -----------------------------------------------------------------

func _try_dash() -> void:
	if not is_instance_valid(_hero):
		return
	_world_fx.grid.apply_explosive_force(10.0, _hero.global_position, 170.0)
	# Dash along the current heading (as a player would), unless that would
	# leave the stage, in which case aim back across it. Steering stays off
	# for a moment afterwards so the dash ends naturally.
	_hero.dash_cost = 0
	_hero.dash_length_ticks = 7
	var dir := _hero.facing
	var landing := _hero.global_position + dir * Constants.DASH_SPEED * (7.0 / 60.0)
	if landing.distance_to(STAGE_CENTER) > STAGE_RADIUS + 160.0:
		dir = (STAGE_CENTER - _hero.global_position).normalized()
	_hero.dash(dir)
	_steer_hold = 0.35


func _try_burn() -> void:
	if not is_instance_valid(_hero):
		return
	_hero.invincible = true
	_burn_left = 3.0


func _try_kill() -> void:
	# A kill burst of each enemy colour, plus a perfect flash, around the hero.
	for i in 3:
		var a := TAU * i / 3.0 + _t
		var pos := STAGE_CENTER + Vector2(cos(a), sin(a)) * 120.0
		var type: Constants.ParticleType = [Constants.ParticleType.NORMAL, Constants.ParticleType.SPLIT, Constants.ParticleType.SUICIDE][i]
		_stage.add_child(NeonBurst.make(pos, Constants.neon_for(type)))
		_world_fx.grid.apply_explosive_force(10.0, pos, 170.0)
	_stage.add_child(NeonBurst.make(STAGE_CENTER + Vector2(0, -120), Color(2.6, 2.6, 2.8), 0.9, 14, 0.3))
	Juice.shake_small(0.15, 0.3)
