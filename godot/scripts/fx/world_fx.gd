class_name WorldFx
extends Node2D
## Shared look for every screen: dark field, honeycomb, and the glow pass.
## Needs `rendering/viewport/hdr_2d = true` (set in project.godot) so colours
## above 1.0 bloom.

@export var glow_intensity := 0.85
@export var glow_strength := 0.95
@export var glow_bloom := 0.08
@export var glow_threshold := 1.0

## Static honeycomb (off by default) or the reactive spring grid.
@export var use_spring_grid := true

@onready var _env: WorldEnvironment = $WorldEnvironment
@onready var _hex: HexGrid = $HexGrid
@onready var grid: SpringGrid = $SpringGrid
@onready var camera: Camera2D = $Camera2D


func _ready() -> void:
	apply_theme(Themes.current)
	Themes.theme_changed.connect(apply_theme)
	_hex.visible = not use_spring_grid
	grid.visible = use_spring_grid
	grid.set_physics_process(use_spring_grid)
	camera.add_to_group("game_camera")
	camera.make_current()
	_warm_up()


func apply_theme(t: GameTheme) -> void:
	RenderingServer.set_default_clear_color(t.clear_color)
	_hex.color = t.grid_color
	grid.color = t.grid_color
	grid.board = t.grid_style == GameTheme.GridStyle.BOARD
	grid.queue_redraw()
	glow_intensity = t.glow_intensity
	glow_bloom = t.glow_bloom
	if _env.environment:
		_env.environment.glow_intensity = glow_intensity
		_env.environment.glow_bloom = glow_bloom


## First-use shader compilation for the particle materials would otherwise land
## on the first kill or the hero's death. Emit each kind once, off-screen.
func _warm_up() -> void:
	var off := Vector2(-4000, -4000)
	add_child(NeonBurst.make(off, Color.WHITE, 1.0, 4, 0.2))
	var trail: Trail = preload("res://scenes/entities/trail.tscn").instantiate()
	trail.position = off
	trail.additive = true
	add_child(trail)
	var ribbon := Ribbon.new()
	trail.add_child(ribbon)
	await get_tree().process_frame
	await get_tree().process_frame
	await get_tree().process_frame
	trail.queue_free()
	var env := Environment.new()
	env.background_mode = Environment.BG_CANVAS
	env.glow_enabled = true
	env.glow_intensity = glow_intensity
	env.glow_strength = glow_strength
	env.glow_bloom = glow_bloom
	env.glow_hdr_threshold = glow_threshold
	env.glow_blend_mode = Environment.GLOW_BLEND_MODE_ADDITIVE
	_env.environment = env
