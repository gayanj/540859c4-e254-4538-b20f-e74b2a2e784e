@tool
class_name GlowCore
extends Node2D
## A bright disc with a soft halo and a thin hot rim. The basic "body" of every
## neon entity. Colours above 1.0 bloom through the glow pass.

@export var radius := 18.0:
	set(v):
		radius = v
		queue_redraw()
@export var color := Color(2.8, 0.35, 0.3):
	set(v):
		color = v
		queue_redraw()
## 0..1 fill brightness of the disc relative to [member color] (1 = solid hot core).
@export var fill := 0.55:
	set(v):
		fill = v
		queue_redraw()
@export var halo := 2.2:
	set(v):
		halo = v
		queue_redraw()
## Dark ink outline just outside the rim (Borderlands-style separation).
@export var outline := 5.0:
	set(v):
		outline = v
		queue_redraw()
## Set by the owner from the theme: fruit cores are flat, rinded, unlit.
var fruit := false:
	set(v):
		fruit = v
		queue_redraw()
## Optional pulse: scales the halo with a sine over time (0 = off).
@export var pulse_speed := 0.0
@export var pulse_amount := 0.15

var _t := 0.0


func _process(delta: float) -> void:
	if pulse_speed > 0.0:
		_t += delta * pulse_speed
		queue_redraw()


func _draw() -> void:
	var p := 1.0 + (sin(_t) * pulse_amount if pulse_speed > 0.0 else 0.0)
	var base := Color(color.r, color.g, color.b, 1.0)
	if fruit:
		_draw_fruit(base, p)
		return
	# Halo: a few translucent discs, biggest first.
	for i in range(4, 0, -1):
		var f := float(i) / 4.0
		draw_circle(Vector2.ZERO, radius * (1.0 + (halo - 1.0) * f * p), Color(base.r, base.g, base.b, 0.08 * (1.0 - f) + 0.03))
	if outline > 0.0:
		draw_arc(Vector2.ZERO, radius + 1.5 + outline * 0.5, 0.0, TAU, 40, Color(0.0, 0.0, 0.02, 1.0), outline, true)
	draw_circle(Vector2.ZERO, radius, base * Color(fill, fill, fill, 1.0))
	draw_arc(Vector2.ZERO, radius, 0.0, TAU, 32, base, 3.0, true)
	draw_circle(Vector2.ZERO, radius * 0.35, Color(base.r * 1.3, base.g * 1.3, base.b * 1.3, 0.9))


## Flat fruit: dark outline, rind ring, flesh disc, a specular highlight and a
## little stem + leaf. Reads as "food", not "light"; in the fruit world the
## hero's blade is the only white thing.
func _draw_fruit(base: Color, p: float) -> void:
	var r := radius * 1.15 * p
	var rind := base.darkened(0.45)
	var flesh := base.lightened(0.12)
	draw_circle(Vector2.ZERO, r + outline * 0.9, Color(0.08, 0.05, 0.03, 1.0))
	draw_circle(Vector2.ZERO, r, rind)
	draw_circle(Vector2.ZERO, r * 0.8, flesh)
	draw_circle(Vector2(-r * 0.3, -r * 0.32), r * 0.22, Color(1, 1, 1, 0.55))
	draw_line(Vector2(0, -r * 0.75), Vector2(r * 0.15, -r * 1.25), Color(0.25, 0.42, 0.14), 4.0, true)
	draw_circle(Vector2(r * 0.3, -r * 1.2), r * 0.18, Color(0.35, 0.65, 0.2))
