class_name MineBody
extends Node2D
## Body of the stationary "invisible" particle: a spiked sea-mine silhouette so
## it can never be mistaken for a pickup. Three looks, driven by [member state]:
## forming (dim, spikes retracted), armed (spikes out, red pulse), fading.

enum State { FORMING, ARMED, FADING, HIDDEN }

var state := State.FORMING:
	set(v):
		state = v
		queue_redraw()
## 0..1 progress of the forming / fading transition.
var fade := 0.0:
	set(v):
		fade = clampf(v, 0.0, 1.0)
		queue_redraw()

const SPIKES := 8
const BODY_R := 20.0

var _t := 0.0
## Fruit theme: a potted cactus instead of a sea mine.
var cactus := false


func _process(delta: float) -> void:
	if state == State.ARMED:
		_t += delta * 7.0 * Game.time_scale
		queue_redraw()


func _draw() -> void:
	if state == State.HIDDEN:
		return
	var alpha := fade if state == State.FORMING else (1.0 - fade if state == State.FADING else 1.0)
	if alpha <= 0.01:
		return
	if cactus:
		_draw_cactus(alpha)
		return
	var armed := state == State.ARMED
	var pulse := 0.5 + 0.5 * sin(_t) if armed else 0.0
	var teal := Themes.current.invisible
	var rim := teal.lerp(Color(3.0, 0.4, 0.3), pulse * 0.8) if armed else teal
	rim.a = alpha
	var spike_len := lerpf(6.0, 16.0, 1.0 if armed else fade * 0.3) + pulse * 3.0
	# Halo + dark body.
	draw_circle(Vector2.ZERO, BODY_R * 1.9, Color(rim.r, rim.g, rim.b, 0.07 * alpha))
	draw_circle(Vector2.ZERO, BODY_R, Color(0.05, 0.07, 0.1, 0.95 * alpha))
	# Spikes.
	for i in SPIKES:
		var a := TAU * i / SPIKES + (_t * 0.1 if armed else 0.0)
		var dir := Vector2(cos(a), sin(a))
		var base := dir * (BODY_R - 2.0)
		var side := dir.orthogonal() * 5.0
		draw_colored_polygon(PackedVector2Array([base + dir * spike_len, base + side, base - side]), rim)
	draw_arc(Vector2.ZERO, BODY_R, 0.0, TAU, 32, rim, 3.0, true)
	# Core light: dim while forming, hot red when armed.
	var core := Color(rim.r * 1.3, rim.g * 1.3, rim.b * 1.3, alpha)
	draw_circle(Vector2.ZERO, BODY_R * (0.3 + 0.15 * pulse), core)
	if armed:
		# Danger cross.
		var c := Color(3.0, 0.5, 0.4, alpha * (0.5 + 0.5 * pulse))
		draw_line(Vector2(-9, -9), Vector2(9, 9), c, 3.0, true)
		draw_line(Vector2(-9, 9), Vector2(9, -9), c, 3.0, true)


## Fruit Ninja mine: a round cactus pad (Stardew-style). One flat green disc
## with a darker edge, a ring of bold pale spikes, and a red flower on top
## when armed. Scales up while forming (vulnerable), down while fading.
func _draw_cactus(alpha: float) -> void:
	var armed := state == State.ARMED
	var grow := 1.0 if armed else (fade if state == State.FORMING else 1.0 - fade)
	var pulse := 0.5 + 0.5 * sin(_t) if armed else 0.0
	var r := 22.0 * (0.4 + 0.6 * grow)
	var ink := Color(0.08, 0.05, 0.03, alpha)
	var green := Color(0.33, 0.6, 0.28, alpha)
	var dark := Color(0.2, 0.42, 0.2, alpha)
	var spike := Color(1.0, 0.98, 0.85, alpha)
	# Spikes first so the pad's outline covers their roots.
	var n := 10
	var spike_len := 7.0 + 5.0 * grow + 2.0 * pulse
	for i in n:
		var a := TAU * i / n + 0.3
		var d := Vector2(cos(a), sin(a))
		draw_line(d * (r - 2.0), d * (r + spike_len + 2.0), ink, 5.0, true)
		draw_line(d * (r - 2.0), d * (r + spike_len), spike, 2.5, true)
	# Pad: outline, body, a lighter inner lobe, and two curved ribs.
	draw_circle(Vector2.ZERO, r + 3.0, ink)
	draw_circle(Vector2.ZERO, r, dark)
	draw_circle(Vector2(-r * 0.12, -r * 0.1), r * 0.78, green)
	draw_arc(Vector2.ZERO, r * 0.5, PI * 0.6, PI * 1.4, 12, dark, 2.5, true)
	draw_arc(Vector2.ZERO, r * 0.5, PI * -0.4, PI * 0.4, 12, dark, 2.5, true)
	# Flower when armed: five red petals, yellow heart, sitting on the top edge.
	if armed:
		var fc := Vector2(0, -r * 0.85)
		var red := Color(0.92, 0.15, 0.22, alpha)
		for i in 5:
			var a := TAU * i / 5.0 - PI / 2.0
			draw_circle(fc + Vector2(cos(a), sin(a)) * (5.5 + 1.5 * pulse), 5.0, ink)
		for i in 5:
			var a := TAU * i / 5.0 - PI / 2.0
			draw_circle(fc + Vector2(cos(a), sin(a)) * (5.5 + 1.5 * pulse), 4.0, red)
		draw_circle(fc, 3.2, Color(1.0, 0.85, 0.2, alpha))
