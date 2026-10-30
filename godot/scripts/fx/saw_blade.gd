@tool
class_name SawBlade
extends Node2D
## Procedural spinning saw: dark gear body, hot glowing rim and hub.
## Replaces the 63-frame saw animation sheets; radius/teeth/colour are parameters,
## so edge saws, corner saws, death saws and the menu saw share one drawing.

@export var radius := 66.0:
	set(v):
		radius = v
		queue_redraw()
@export var teeth := 18:
	set(v):
		teeth = v
		queue_redraw()
@export var rim_color := Color(2.6, 0.5, 0.35):
	set(v):
		rim_color = v
		queue_redraw()
@export var body_color := Color(0.08, 0.07, 0.1):
	set(v):
		body_color = v
		queue_redraw()
## Radians per second at time_scale 1.
@export var spin_speed := 6.0
## Set from the theme: juicer blades are flat and unlit.
var juicer := false:
	set(v):
		juicer = v
		queue_redraw()

## Live state pushed by Saw: 0..1 heat fraction, dull (overheated) and the
## white-hot flash when overheating triggers.
var heat := 0.0:
	set(v):
		if not is_equal_approx(v, heat):
			heat = v
			queue_redraw()
var dull := false:
	set(v):
		if v != dull:
			dull = v
			queue_redraw()
## 1..0 fraction of the dull period remaining (drawn as a cooling arc).
var cooldown := 0.0:
	set(v):
		if not is_equal_approx(v, cooldown):
			cooldown = v
			queue_redraw()
var flash := 0.0:
	set(v):
		if not is_equal_approx(v, flash):
			flash = v
			queue_redraw()


func _process(delta: float) -> void:
	if Engine.is_editor_hint():
		return
	rotation += spin_speed * delta * Game.time_scale
	if heat > 0.7 and not dull:
		queue_redraw()  # gauge blinks


func _draw() -> void:
	var outer := radius
	var inner := radius * 0.82
	var pts := PackedVector2Array()
	var n := teeth * 2
	for i in n:
		var a := TAU * i / n
		# Alternate tip / root, with the leading edge of each tooth raked back.
		var r := outer if i % 2 == 0 else inner
		var skew := 0.18 if i % 2 == 0 else 0.0
		pts.append(Vector2(cos(a + skew), sin(a + skew)) * r)
	var rim := Color(rim_color.r, rim_color.g, rim_color.b, 1.0)
	if juicer:
		_draw_juicer(outer, inner, pts)
		return
	if dull:
		rim = Color(0.35, 0.4, 0.55, 1.0)
	else:
		# Warm up towards orange-white as heat builds.
		rim = rim.lerp(Color(3.0, 1.6, 0.6), heat * 0.8)
	rim = rim.lerp(Color(4.0, 4.0, 4.0), flash)
	# Outer halo.
	draw_circle(Vector2.ZERO, outer * 1.12, Color(rim.r, rim.g, rim.b, 0.035))
	draw_colored_polygon(pts, body_color)
	var closed := pts.duplicate()
	closed.append(pts[0])
	draw_polyline(closed, rim, 2.5, true)
	# Hub: ring + spokes.
	draw_arc(Vector2.ZERO, inner * 0.5, 0.0, TAU, 32, Color(rim.r, rim.g, rim.b, 0.6), 2.0, true)
	draw_circle(Vector2.ZERO, inner * 0.18, rim)
	for i in 3:
		var a := TAU * i / 3.0
		draw_line(Vector2.ZERO, Vector2(cos(a), sin(a)) * inner * 0.5, Color(rim.r, rim.g, rim.b, 0.5), 2.0, true)
	if dull and cooldown > 0.0:
		draw_set_transform(Vector2.ZERO, -rotation, Vector2.ONE)
		draw_arc(Vector2.ZERO, inner * 0.72, -PI / 2.0, -PI / 2.0 + TAU * cooldown, 32, Color(0.5, 0.65, 0.9, 0.8), 5.0, true)
		draw_set_transform(Vector2.ZERO, 0.0, Vector2.ONE)
	# Heat gauge: arc just inside the teeth, counter-rotated so it reads as static.
	if heat > 0.0 and not dull:
		draw_set_transform(Vector2.ZERO, -rotation, Vector2.ONE)
		var gauge := Color(3.0, 1.6, 0.3).lerp(Color(3.0, 0.4, 0.3), heat)
		if heat > 0.7:
			gauge.a = 0.6 + 0.4 * absf(sin(Time.get_ticks_msec() * 0.02))
		draw_arc(Vector2.ZERO, inner * 0.72, -PI / 2.0, -PI / 2.0 + TAU * clampf(heat, 0.0, 1.0), 32, gauge, 6.0, true)
		draw_set_transform(Vector2.ZERO, 0.0, Vector2.ONE)


## Fruit Ninja hazard: a steel grater/juicer disc. Same tooth polygon (so the
## silhouette matches the hitbox), flat shading, dark outline, radial slots.
func _draw_juicer(outer: float, inner: float, pts: PackedVector2Array) -> void:
	var steel := Color(0.35, 0.4, 0.48) if dull else Color(0.74, 0.77, 0.83)
	steel = steel.lerp(Color(1.0, 0.72, 0.4), heat * 0.6 if not dull else 0.0)
	steel = steel.lerp(Color(1, 1, 1), flash)
	var closed := pts.duplicate()
	closed.append(pts[0])
	draw_polyline(closed, Color(0.08, 0.05, 0.03, 1.0), 6.0, true)
	draw_colored_polygon(pts, steel)
	draw_circle(Vector2.ZERO, inner * 0.88, steel.darkened(0.12))
	for i in 8:
		var a := TAU * i / 8.0
		var d := Vector2(cos(a), sin(a))
		draw_line(d * inner * 0.3, d * inner * 0.8, Color(0.08, 0.05, 0.03, 0.9), 5.0, true)
	draw_circle(Vector2.ZERO, inner * 0.22, Color(0.08, 0.05, 0.03))
	draw_circle(Vector2.ZERO, inner * 0.12, steel)
	if dull and cooldown > 0.0:
		draw_set_transform(Vector2.ZERO, -rotation, Vector2.ONE)
		draw_arc(Vector2.ZERO, inner * 0.72, -PI / 2.0, -PI / 2.0 + TAU * cooldown, 32, Color(0.5, 0.65, 0.9, 0.9), 5.0, true)
		draw_set_transform(Vector2.ZERO, 0.0, Vector2.ONE)
	elif heat > 0.0:
		draw_set_transform(Vector2.ZERO, -rotation, Vector2.ONE)
		draw_arc(Vector2.ZERO, inner * 0.72, -PI / 2.0, -PI / 2.0 + TAU * clampf(heat, 0.0, 1.0), 32, Color(1.0, 0.45, 0.1, 0.95), 6.0, true)
		draw_set_transform(Vector2.ZERO, 0.0, Vector2.ONE)
