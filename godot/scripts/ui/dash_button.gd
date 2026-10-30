class_name DashButton
extends Control
## Bottom-left control: a neon ring that dashes in the facing direction when
## pressed. The rim fills as the cooldown recovers; a badge shows shockwave
## charges; the rim turns gold while a charge is loaded.

signal pressed

@export var ring_color := Color(0.35, 1.7, 2.1)
@export var charged_color := Color(2.4, 2.0, 0.6)

var _cooldown := 0.0
var _charges := 0
var _ready_to_dash := true
var _pressed := false
var _invincible := 0.0
var _t := 0.0

@onready var _count: Label = $Count


func _ready() -> void:
	mouse_filter = Control.MOUSE_FILTER_STOP
	_count.mouse_filter = Control.MOUSE_FILTER_IGNORE
	set_state(0.0, 0, true)


func _process(delta: float) -> void:
	if _invincible > 0.0:
		_t += delta * 14.0
		queue_redraw()


func set_state(cooldown_left: float, charges: int, can_dash: bool, invincible_left := 0.0) -> void:
	_cooldown = cooldown_left
	_charges = charges
	_ready_to_dash = can_dash
	_invincible = invincible_left
	_count.visible = charges > 0
	_count.text = "x%d" % charges
	queue_redraw()


func _gui_input(event: InputEvent) -> void:
	if event is InputEventScreenTouch:
		if event.pressed:
			_pressed = true
			pressed.emit()
			queue_redraw()
			accept_event()
		elif _pressed:
			_pressed = false
			queue_redraw()
			accept_event()


func _draw() -> void:
	var c := size / 2.0
	var r := minf(size.x, size.y) / 2.0 - 6.0
	var rim := charged_color if _charges > 0 else ring_color
	var alpha := 1.0 if _ready_to_dash else 0.45
	draw_circle(c, r * 1.12, Color(rim.r, rim.g, rim.b, 0.06 * alpha))
	draw_circle(c, r, Color(0.06, 0.05, 0.09, 0.95 if _pressed else 0.85))
	# Rim = cooldown gauge: full ring when ready, refilling clockwise otherwise.
	var start := -PI / 2.0
	draw_arc(c, r, 0.0, TAU, 64, Color(rim.r, rim.g, rim.b, 0.18), 5.0, true)
	draw_arc(c, r, start, start + TAU * (1.0 - _cooldown), 64, Color(rim.r, rim.g, rim.b, alpha), 5.0, true)
	# Dash glyph: three chevrons.
	var col := Color(rim.r, rim.g, rim.b, alpha)
	for i in 3:
		var x := c.x - 26.0 + i * 22.0
		var pts := PackedVector2Array([Vector2(x - 8, c.y - 22), Vector2(x + 8, c.y), Vector2(x - 8, c.y + 22)])
		draw_polyline(pts, col, 5.0, true)
	if _pressed:
		draw_circle(c, r * 0.92, Color(rim.r, rim.g, rim.b, 0.12))
	if _invincible > 0.0:
		# Invincibility: a gold outer ring that drains as it runs out, with
		# flickering sparks around it (fast flicker in the last 25%).
		var gold := Color(3.0, 2.4, 0.6)
		var urgent := _invincible < 0.25
		var flick := 0.6 + 0.4 * absf(sin(_t * (3.0 if urgent else 1.0)))
		draw_arc(c, r + 14.0, 0.0, TAU, 64, Color(gold.r, gold.g, gold.b, 0.15), 8.0, true)
		draw_arc(c, r + 14.0, -PI / 2.0, -PI / 2.0 + TAU * _invincible, 64, Color(gold.r, gold.g, gold.b, flick), 8.0, true)
		for i in 10:
			var a := _t * 0.35 + TAU * i / 10.0
			var len := 10.0 + 10.0 * absf(sin(_t + i * 1.7))
			var p0 := c + Vector2(cos(a), sin(a)) * (r + 20.0)
			draw_line(p0, p0 + Vector2(cos(a), sin(a)) * len, Color(gold.r, gold.g, gold.b, 0.8 * flick), 3.0, true)
