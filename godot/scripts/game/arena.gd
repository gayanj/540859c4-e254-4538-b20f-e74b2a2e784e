extends Node2D
## Builds the ring of saw blades around the arena.
## Port of GameScreenWorldController.createSpikes(). The placement formulas are
## kept verbatim (they index saws by multiples of the blade's half-size) and the
## y axis is flipped from LibGDX (y-up) to Godot (y-down).

const SAW_SCENE := preload("res://scenes/entities/saw.tscn")
## Half-size of a full saw blade (first animation frame was 132 px).
const HALF := 66.0

var saws: Array[Saw] = []


func _ready() -> void:
	build()


func build() -> void:
	var w := Constants.APP_WIDTH
	var h := Constants.APP_HEIGHT
	# Left and right columns (half the blade pokes into the arena).
	for i in range(2, (h / 130) - 1):
		var yscale := 2.0 * (i + 1)
		var y_gdx := -HALF * yscale + h + HALF
		_add(Vector2(w, Constants.flip_y(y_gdx)), 1.0)
		_add(Vector2(0, Constants.flip_y(y_gdx)), 1.0)
	# Top and bottom rows.
	for i in range(2, (w / 130) - 2):
		var xscale := 2.0 * (i + 1) + 0.2
		var x := -HALF * xscale + w + HALF
		_add(Vector2(x, Constants.flip_y(h)), 1.0)
		_add(Vector2(x, Constants.flip_y(0)), 1.0)

	# Corner arcs of smaller blades. (xscale, yscale) pairs copied from the original.
	var rt := [[0.5, 2.5], [2.6, 0.5], [1.9, 1.175], [1.2, 1.85]]
	for p in rt:
		_add_corner(2 * (p[0] + 1), 2 * (p[1] + 1), 0.7, true)
		_add_corner(2 * p[0], 2 * (p[1] + 1), 0.7, false)

	var rb := [
		[2 * 2.3 - 1.1, 2 * 2.85 + 36.5],
		[2 * 2.75, 2 * 2.85 + 36.7],
		[2 * 3.6, 2 * 2.175 + 39.15],
		[2 * 4.1, 2 * 2.175 + 40.9],
		[2 * 4.15, 2 * 2.175 + 42.9],
	]
	var lb := [
		[2 * 1.3 - 1.1, 2 * 2.85 + 36.5],
		[2 * 1.75, 2 * 2.85 + 36.7],
		[2 * 2.6, 2 * 2.175 + 39.15],
		[2 * 3.1, 2 * 2.175 + 40.9],
		[2 * 3.15, 2 * 2.175 + 42.9],
	]
	for p in rb:
		_add_corner(p[0], p[1], 0.6, true)
	for p in lb:
		_add_corner(p[0], p[1], 0.6, false)
	_link_neighbours()


func _link_neighbours() -> void:
	for a in saws:
		for b in saws:
			if a != b and a.position.distance_to(b.position) <= Constants.SAW_NEIGHBOUR_DISTANCE:
				a.neighbours.append(b)


func _add_corner(xscale: float, yscale: float, size_scale: float, right_side: bool) -> void:
	var half := HALF * size_scale
	var x := (-half * xscale + Constants.APP_WIDTH + half) if right_side else (half * xscale + half)
	var y_gdx := -half * yscale + Constants.APP_HEIGHT + half
	_add(Vector2(x, Constants.flip_y(y_gdx)), size_scale)


func _add(at: Vector2, size_scale: float) -> void:
	var saw: Saw = SAW_SCENE.instantiate()
	saw.size_scale = size_scale
	saw.position = at
	saw.inward = _inward_direction(at)
	add_child(saw)
	saws.append(saw)


## Direction a blade flies when it overheats: straight in from its wall; corner
## blades aim at the arena centre.
func _inward_direction(at: Vector2) -> Vector2:
	if at.x <= 1.0:
		return Vector2.RIGHT
	if at.x >= Constants.APP_WIDTH - 1.0:
		return Vector2.LEFT
	if at.y <= 1.0:
		return Vector2.DOWN
	if at.y >= Constants.APP_HEIGHT - 1.0:
		return Vector2.UP
	return (Constants.center() - at).normalized()
