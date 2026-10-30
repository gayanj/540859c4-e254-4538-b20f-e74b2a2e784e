class_name HeroBody
extends Node2D
## Drawn under the hero's glow core: a dark occlusion disc that cuts a hole in
## whatever additive mess is piling up around the hero, plus a nose triangle
## pointing along the facing direction so the hero has a readable shape.

var facing := Vector2.UP:
	set(v):
		if v.length_squared() > 0.0:
			facing = v.normalized()
			queue_redraw()

const DISC_R := 34.0
## Set by the Hero from the theme.
var blade := false:
	set(v):
		blade = v
		queue_redraw()
var tint := Color(1, 1, 1)
## While invincible the steel glows white-hot with an orange edge.
var burning := false:
	set(v):
		if v != burning:
			burning = v
			queue_redraw()


func _draw() -> void:
	if blade:
		_draw_blade()
		return
	draw_circle(Vector2.ZERO, DISC_R, Color(0.02, 0.02, 0.05, 0.95))
	draw_arc(Vector2.ZERO, DISC_R, 0.0, TAU, 48, Color(0.0, 0.0, 0.0, 1.0), 6.0, true)
	# Nose.
	var tip := facing * (DISC_R + 10.0)
	var base := facing * (DISC_R - 8.0)
	var side := facing.orthogonal() * 9.0
	draw_colored_polygon(PackedVector2Array([tip, base + side, base - side]), Color(2.2, 2.2, 2.2, 0.95))


## Fruit Ninja hero: a kitchen knife pointing along [member facing]. Long and
## thin so it is the only non-round thing in the arena; white steel with a dark
## outline so it is the brightest thing too.
func _draw_blade() -> void:
	var f := facing
	var n := f.orthogonal()
	# Blade: tip at +70, heel at -4, 20 px wide with a curved belly.
	var tip := f * 70.0
	var pts := PackedVector2Array([
		tip, f * 40.0 + n * 11.0, f * 10.0 + n * 12.0, f * -4.0 + n * 10.0,
		f * -4.0 - n * 7.0, f * 30.0 - n * 7.0,
	])
	var outline := pts.duplicate()
	outline.append(pts[0])
	draw_polyline(outline, Color(0.05, 0.04, 0.03, 1.0), 7.0, true)
	draw_colored_polygon(pts, Color(1.0, 0.95, 0.85) if burning else Color(0.86, 0.88, 0.92))
	if burning:
		var edge := Themes.current.flame_mid
		draw_polyline(outline, Color(edge.r, edge.g, edge.b, 0.9), 3.0, true)
	# Edge highlight along the sharp side.
	draw_line(f * -2.0 + n * 11.0, tip, Color(1.0, 1.0, 1.0), 2.5, true)
	# Bolster + handle behind the heel.
	draw_line(f * -4.0, f * -34.0, Color(0.05, 0.04, 0.03, 1.0), 20.0, true)
	draw_line(f * -6.0, f * -32.0, Color(0.32, 0.2, 0.1), 14.0, true)
	draw_circle(f * -14.0, 2.5, Color(0.8, 0.8, 0.85))
	draw_circle(f * -24.0, 2.5, Color(0.8, 0.8, 0.85))
