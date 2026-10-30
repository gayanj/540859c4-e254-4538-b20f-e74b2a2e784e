class_name Ribbon
extends Line2D
## Motion ribbon: records the parent's recent positions and draws a tapering,
## fading line through them. Points live in world space (top_level), so the
## ribbon stays behind as the parent moves.

@export var max_points := 18
@export var color := Color(2.8, 0.35, 0.3):
	set(v):
		color = v
		_apply_gradient()
@export var ribbon_width := 26.0
## Minimum movement before a new point is recorded (px).
@export var min_step := 2.0

var _parent: Node2D


func _ready() -> void:
	top_level = true
	_parent = get_parent() as Node2D
	width = ribbon_width
	joint_mode = Line2D.LINE_JOINT_ROUND
	begin_cap_mode = Line2D.LINE_CAP_ROUND
	end_cap_mode = Line2D.LINE_CAP_ROUND
	antialiased = true
	var wc := Curve.new()
	wc.add_point(Vector2(0.0, 1.0))
	wc.add_point(Vector2(1.0, 0.05))
	width_curve = wc
	# Normal (mix) blending: additive ribbons summed to white wherever several
	# particles crossed, which is exactly where the player needs to see.
	_apply_gradient()
	clear_points()


func _apply_gradient() -> void:
	var g := Gradient.new()
	g.offsets = PackedFloat32Array([0.0, 1.0])
	g.colors = PackedColorArray([Color(color.r, color.g, color.b, 0.75), Color(color.r, color.g, color.b, 0.0)])
	gradient = g


func _process(_delta: float) -> void:
	if _parent == null:
		return
	var pos := _parent.global_position
	if get_point_count() == 0 or pos.distance_to(get_point_position(0)) >= min_step:
		add_point(pos, 0)
	elif get_point_count() > 0:
		set_point_position(0, pos)
	while get_point_count() > max_points:
		remove_point(get_point_count() - 1)


## Drop the history (e.g. after a teleport).
func reset() -> void:
	clear_points()
