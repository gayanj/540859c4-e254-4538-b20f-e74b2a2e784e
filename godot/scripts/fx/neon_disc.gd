@tool
class_name NeonDisc
extends Control
## Glowing circular backing plate for icon buttons. Add as a child of a
## TextureButton; it draws behind the parent and ignores input.

@export var ring_color := Color(2.2, 0.45, 0.4):
	set(v):
		ring_color = v
		queue_redraw()
@export var fill_color := Color(0.1, 0.08, 0.12, 0.9):
	set(v):
		fill_color = v
		queue_redraw()
@export var padding := 10.0:
	set(v):
		padding = v
		queue_redraw()


func _ready() -> void:
	show_behind_parent = true
	mouse_filter = Control.MOUSE_FILTER_IGNORE
	set_anchors_and_offsets_preset(Control.PRESET_FULL_RECT)


func _draw() -> void:
	var c := size / 2.0
	var r := minf(size.x, size.y) / 2.0 + padding
	draw_circle(c, r * 1.15, Color(ring_color.r, ring_color.g, ring_color.b, 0.08))
	draw_circle(c, r, fill_color)
	draw_arc(c, r, 0.0, TAU, 48, ring_color, 4.0, true)
