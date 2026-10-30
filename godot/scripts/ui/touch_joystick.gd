class_name TouchJoystick
extends Control
## Touch joystick anchored to the bottom-right corner.
## Replaces the scene2d Touchpad from utils/TouchPadHelper.java.
##
## [member vector] is the knob offset as a fraction of the pad radius, in screen
## space (x right, y down), so it can be fed straight into a 2D velocity.

const DEADZONE := 10.0

var vector: Vector2 = Vector2.ZERO
var _touch_index := -1

@export var ring_color := Color(1.15, 0.26, 0.2)

@onready var _knob: TextureRect = $Knob
@onready var _center: Vector2 = size / 2.0
@onready var _radius: float = minf(size.x, size.y) / 2.0 - _knob.size.x / 2.0


func _ready() -> void:
	# Drawn procedurally to match the DashButton; the textures are kept hidden
	# as layout references for the knob size.
	$Background.visible = false
	_knob.visible = false
	_reset_knob()


func _draw() -> void:
	var r := minf(size.x, size.y) / 2.0 - 6.0
	var active := _touch_index != -1
	draw_circle(_center, r * 1.12, Color(ring_color.r, ring_color.g, ring_color.b, 0.06))
	draw_circle(_center, r, Color(0.06, 0.05, 0.09, 0.85))
	draw_arc(_center, r, 0.0, TAU, 64, Color(ring_color.r, ring_color.g, ring_color.b, 1.0 if active else 0.7), 4.0, true)
	var knob_r := _knob.size.x / 2.0
	var kc := _knob.position + _knob.size / 2.0
	draw_circle(kc, knob_r * 1.3, Color(ring_color.r, ring_color.g, ring_color.b, 0.12))
	draw_circle(kc, knob_r, Color(1.3, 0.45, 0.4) if active else Color(0.8, 0.3, 0.27))


func _gui_input(event: InputEvent) -> void:
	if event is InputEventScreenTouch:
		var touch := event as InputEventScreenTouch
		if touch.pressed and _touch_index == -1:
			_touch_index = touch.index
			_update(touch.position)
			accept_event()
		elif not touch.pressed and touch.index == _touch_index:
			release()
			accept_event()
	elif event is InputEventScreenDrag:
		var drag := event as InputEventScreenDrag
		if drag.index == _touch_index:
			_update(drag.position)
			accept_event()


func _input(event: InputEvent) -> void:
	# Drags that leave the control's rect still arrive here; keep tracking them.
	if _touch_index == -1:
		return
	if event is InputEventScreenDrag and event.index == _touch_index:
		_update(event.position - global_position)
	elif event is InputEventScreenTouch and not event.pressed and event.index == _touch_index:
		release()


func _update(local_pos: Vector2) -> void:
	var offset := local_pos - _center
	if offset.length() < DEADZONE:
		vector = Vector2.ZERO
	else:
		vector = (offset / _radius).limit_length(1.0)
	_knob.position = _center + vector * _radius - _knob.size / 2.0
	queue_redraw()


func release() -> void:
	_touch_index = -1
	vector = Vector2.ZERO
	_reset_knob()


func _reset_knob() -> void:
	_knob.position = _center - _knob.size / 2.0
	queue_redraw()
