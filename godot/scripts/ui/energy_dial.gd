class_name EnergyDial
extends Control
## Energy readout for the HUD: a ring that depletes clockwise around a lightning
## bolt, on a dark plate. Colour follows the usual thresholds; pulses when low;
## flashes white on a hit.

@export var max_value := 80
@export var ring_width := 7.0

var value := 80:
	set(v):
		value = clampi(v, 0, max_value)
		queue_redraw()

var _blink := 0.0
var _t := 0.0
var _blink_tween: Tween

# Lightning bolt outline, unit-ish coordinates (scaled to the dial in _draw).
static var BOLT := PackedVector2Array([
	Vector2(0.22, -1.0), Vector2(-0.45, 0.08), Vector2(-0.08, 0.08),
	Vector2(-0.28, 1.0), Vector2(0.48, -0.14), Vector2(0.1, -0.14),
])


func _process(delta: float) -> void:
	if value <= Constants.HERO_LOW_ENERGY:
		_t += delta * 6.0
		queue_redraw()


func blink() -> void:
	if _blink_tween:
		_blink_tween.kill()
	_blink = 1.0
	_blink_tween = create_tween()
	_blink_tween.tween_method(func(v: float): _blink = v; queue_redraw(), 1.0, 0.0, 0.3)


func _color() -> Color:
	var c: Color
	if value >= 60:
		c = Color(0.3, 1.1, 0.55)
	elif value >= 40:
		c = Color(1.1, 0.95, 0.25)
	elif value >= 20:
		c = Color(1.15, 0.55, 0.15)
	else:
		c = Color(1.25, 0.2, 0.2)
	return c.lerp(Color(2.0, 2.0, 2.0), _blink)


func _draw() -> void:
	var c := size / 2.0
	var r := minf(size.x, size.y) / 2.0 - ring_width
	var col := _color()
	if value <= Constants.HERO_LOW_ENERGY and value > 0:
		col.a = 0.6 + 0.4 * absf(sin(_t))
	# Plate + halo.
	draw_circle(c, r * 1.14, Color(col.r, col.g, col.b, 0.05))
	draw_circle(c, r + ring_width * 0.5, Color(0.04, 0.04, 0.08, 0.9))
	# Track and arc.
	draw_arc(c, r, 0.0, TAU, 64, Color(1.0, 1.0, 1.0, 0.1), ring_width, true)
	if value > 0:
		var start := -PI / 2.0
		draw_arc(c, r, start, start + TAU * float(value) / max_value, 64, col, ring_width, true)
	# Bolt.
	var pts := PackedVector2Array()
	var s := r * 0.55
	for p in BOLT:
		pts.append(c + p * s)
	draw_colored_polygon(pts, Color(col.r, col.g, col.b, 0.95))
	var closed := pts.duplicate()
	closed.append(pts[0])
	draw_polyline(closed, Color(col.r * 1.3, col.g * 1.3, col.b * 1.3, 1.0), 2.0, true)
