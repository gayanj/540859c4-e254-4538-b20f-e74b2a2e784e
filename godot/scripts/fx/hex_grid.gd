@tool
class_name HexGrid
extends Node2D
## Faint neon honeycomb drawn procedurally over the play field.
## Replaces the baked background.png; tweak radius/colour in the inspector.

@export var hex_radius := 96.0:
	set(v):
		hex_radius = v
		queue_redraw()
@export var line_width := 2.0:
	set(v):
		line_width = v
		queue_redraw()
@export var color := Color(0.25, 0.45, 0.9, 0.16):
	set(v):
		color = v
		queue_redraw()
@export var area := Rect2(0, 0, 1080, 1920):
	set(v):
		area = v
		queue_redraw()


func _draw() -> void:
	# Pointy-top hexagons. Each hex shares edges with its neighbours, so only
	# three of the six edges are drawn per cell to avoid double lines.
	var r := hex_radius
	var w := sqrt(3.0) * r
	var h := 1.5 * r
	var cols := int(area.size.x / w) + 3
	var rows := int(area.size.y / h) + 3
	for row in range(-1, rows):
		for col in range(-1, cols):
			var cx := area.position.x + col * w + (w / 2.0 if row % 2 != 0 else 0.0)
			var cy := area.position.y + row * h
			var pts := PackedVector2Array()
			for i in 6:
				var ang := deg_to_rad(60.0 * i - 30.0)
				pts.append(Vector2(cx + r * cos(ang), cy + r * sin(ang)))
			# Edges 0-1, 1-2, 2-3 (right, lower-right, lower-left) per cell.
			for i in 3:
				draw_line(pts[i], pts[i + 1], color, line_width, true)
