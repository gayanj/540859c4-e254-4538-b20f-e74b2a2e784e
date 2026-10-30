class_name Flames
extends Node2D
## A single procedural flame trailing behind a body: a layered teardrop
## (outer, mid, core colours from the theme) on a waving spine, pointing away
## from [member wind] (the body's velocity) or along [member rest_dir] when still.

## Local-space anchor and the direction the flame points when not moving.
var anchor := Vector2.ZERO
var rest_dir := Vector2.UP
var wind := Vector2.ZERO
var length := 90.0
var width := 30.0
var intensity := 1.0:
	set(v):
		intensity = clampf(v, 0.0, 1.0)
		visible = intensity > 0.01
var core_color := Color(1.0, 0.95, 0.6)
var mid_color := Color(1.0, 0.58, 0.12)
var outer_color := Color(0.88, 0.18, 0.05)

var _t := 0.0
const SEGMENTS := 9


func _process(delta: float) -> void:
	_t += delta * 7.0 * Game.time_scale
	queue_redraw()


func _draw() -> void:
	if intensity <= 0.0:
		return
	if length <= 0.0:
		return
	# Direction: away from motion, falling back to rest_dir when still.
	var dir := (-wind).normalized() if wind.length() > 0.08 else rest_dir
	var speed := clampf(wind.length(), 0.0, 1.0)
	var n := dir.orthogonal()
	var len := length * intensity * (0.7 + 0.5 * speed)
	var flick := 0.85 + 0.15 * sin(_t * 2.3) * sin(_t * 1.1)
	len *= flick
	# Spine: a bent polyline so the flame waves rather than sits rigid.
	var spine := PackedVector2Array()
	for i in SEGMENTS + 1:
		var f := float(i) / SEGMENTS
		var sway := sin(_t * 1.7 - f * 4.0) * width * 0.35 * f + sin(_t * 0.9 + f * 2.0) * width * 0.15 * f
		spine.append(anchor + dir * len * f + n * sway)
	_layer(spine, n, width * 1.0, outer_color, 0.85)
	_layer(spine, n, width * 0.62, mid_color, 0.95, 0.85)
	_layer(spine, n, width * 0.3, core_color, 1.0, 0.6)


## A teardrop around the spine: full width near the anchor, tapering to the tip
## at [param tip_frac] of the spine length.
func _layer(spine: PackedVector2Array, n: Vector2, w: float, col: Color, alpha: float, tip_frac := 1.0) -> void:
	var left := PackedVector2Array()
	var right := PackedVector2Array()
	var last := int(round(SEGMENTS * tip_frac))
	for i in last + 1:
		var f := float(i) / last
		# Bulge just past the root, then taper to a point.
		var half := w * 0.5 * sin(PI * (0.08 + 0.92 * f)) * (1.0 - f * 0.35)
		if i == last:
			half = 0.0
		left.append(spine[i] + n * half)
		right.append(spine[i] - n * half)
	var poly := PackedVector2Array()
	poly.append_array(left)
	right.reverse()
	poly.append_array(right)
	draw_colored_polygon(poly, Color(col.r, col.g, col.b, alpha * intensity))

