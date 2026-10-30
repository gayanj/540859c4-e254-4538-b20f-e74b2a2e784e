class_name SpringGrid
extends Node2D
## Spring-mass grid that ripples under moving bodies and bulges on explosions.
## Port of utils/grid/{Grid,PointMass,Spring}.java (the 2015 code had it written
## but disabled). Same model: border points anchored hard, every third interior
## point anchored softly, neighbours linked by pull-only springs.
##
## Implementation notes:
## - Points live in packed arrays (structure-of-arrays); springs are index pairs.
## - The lattice extends [member margin_cells] beyond [member area] so camera
##   shake never reveals its edge, and the last row/column always reaches the edge.
## - Simulation runs in _physics_process; drawing interpolates between the last
##   two states with the physics interpolation fraction, so it is smooth at any
##   refresh rate. Lines are drawn as antialiased polylines per row/column.
## - Forces only visit the cells inside their radius.

@export var area := Rect2(0, 0, 1080, 1920)
## Target number of points inside [member area]; spacing is derived (original: 455).
@export var target_points := 1000
## Draw an extra line halfway between simulated rows/columns (doubles the visual
## density for free when the simulation is coarse; unnecessary at ~1000 points).
@export var mid_lines := false
## Extra rings of cells outside the visible area.
@export var margin_cells := 2
@export var color := Color(0.25, 0.45, 0.9, 0.22)
@export var thin_width := 2.0
@export var thick_width := 4.0
## Board style: every line the same weight (a cutting board, not a lattice).
var board := false
## Antialiased polylines cost extra geometry; with glow on, thin lines don't need it.
@export var antialias_thin := true
@export var antialias_thick := true
@export var stiffness := 0.28
@export var damping := 0.06
@export var point_damping := 0.98
## Anchor spring strength for the border ring (original 0.1 / 0.1).
@export var border_stiffness := 0.2
@export var border_damping := 0.2
## Scale applied to all external forces (lower = calmer grid).
@export var force_scale := 0.7

var cols := 0
var rows := 0
var spacing := 0.0
var origin := Vector2.ZERO

var _pos := PackedVector2Array()
var _prev := PackedVector2Array()
var _vel := PackedVector2Array()
var _acc := PackedVector2Array()
var _rest := PackedVector2Array()
var _damp := PackedFloat32Array()

var _s_a := PackedInt32Array()
var _s_b := PackedInt32Array()
var _s_len := PackedFloat32Array()
var _a_i := PackedInt32Array()
var _a_k := PackedFloat32Array()
var _a_d := PackedFloat32Array()

# Scratch buffers reused every frame.
var _line := PackedVector2Array()
var _mid := PackedVector2Array()
var _lerped := PackedVector2Array()
## True while any point is moving; a settled grid is not redrawn.
var _moving := true
var _settled_drawn := false

## Debug timing of the last draw / step (microseconds).
static var last_draw_us := 0
static var last_step_us := 0


func _ready() -> void:
	build()


func build() -> void:
	spacing = sqrt(area.size.x * area.size.y / target_points)
	var inner_cols := int(ceil(area.size.x / spacing)) + 1
	var inner_rows := int(ceil(area.size.y / spacing)) + 1
	cols = inner_cols + 2 * margin_cells
	rows = inner_rows + 2 * margin_cells
	origin = area.position - Vector2.ONE * spacing * margin_cells
	var n := cols * rows
	_pos.resize(n)
	_vel.resize(n)
	_acc.resize(n)
	_rest.resize(n)
	_damp.resize(n)
	for y in rows:
		for x in cols:
			var i := y * cols + x
			var p := origin + Vector2(x, y) * spacing
			_pos[i] = p
			_rest[i] = p
			_vel[i] = Vector2.ZERO
			_acc[i] = Vector2.ZERO
			_damp[i] = point_damping
	_prev = _pos.duplicate()

	_s_a.clear(); _s_b.clear(); _s_len.clear()
	_a_i.clear(); _a_k.clear(); _a_d.clear()
	for y in rows:
		for x in cols:
			var i := y * cols + x
			if x == 0 or y == 0 or x == cols - 1 or y == rows - 1:
				_add_anchor(i, border_stiffness, border_damping)
			elif x % 3 == 0 and y % 3 == 0:
				_add_anchor(i, 0.002, 0.02)
			if x > 0:
				_add_spring(i - 1, i)
			if y > 0:
				_add_spring(i - cols, i)


func _add_anchor(i: int, k: float, d: float) -> void:
	_a_i.append(i)
	_a_k.append(k)
	_a_d.append(d)


func _add_spring(a: int, b: int) -> void:
	_s_a.append(a)
	_s_b.append(b)
	_s_len.append(_pos[a].distance_to(_pos[b]) * 0.95)


func _physics_process(_delta: float) -> void:
	_step()


func _process(_delta: float) -> void:
	if _moving or not _settled_drawn:
		queue_redraw()


func _step() -> void:
	var t0 := Time.get_ticks_usec()
	_prev = _pos.duplicate()
	var k := stiffness
	var d := damping
	# Springs between neighbours: pull only, damped along the spring.
	for s in _s_a.size():
		var a := _s_a[s]
		var b := _s_b[s]
		var x := _pos[a] - _pos[b]
		var length := x.length()
		var target := _s_len[s]
		if length <= target:
			continue
		var force := x * ((length - target) / length * k) - (_vel[b] - _vel[a]) * d
		_acc[a] -= force
		_acc[b] += force
	# Anchors: spring back to the fixed rest position.
	for s in _a_i.size():
		var i := _a_i[s]
		_acc[i] += (_rest[i] - _pos[i]) * _a_k[s] - _vel[i] * _a_d[s]
	# Integrate (all points have unit mass).
	var any_moving := false
	for i in _pos.size():
		var v := _vel[i] + _acc[i]
		_acc[i] = Vector2.ZERO
		if v.length_squared() < 0.0004:
			v = Vector2.ZERO
		else:
			any_moving = true
		_pos[i] += v
		_vel[i] = v * _damp[i]
		_damp[i] = point_damping
	if any_moving:
		_settled_drawn = false
	_moving = any_moving
	last_step_us = Time.get_ticks_usec() - t0


# --- forces ------------------------------------------------------------------

## Push points near [param position] along [param force] (px/s velocity of the body).
func apply_directed_force(force: Vector2, position: Vector2, radius: float) -> void:
	var f := force * (0.2 * force_scale)  # original took m/s (= px/s / 100) * 20
	var r2 := radius * radius
	var b := _bounds(position, radius)
	for y in range(b.position.y, b.end.y):
		var row := y * cols
		for x in range(b.position.x, b.end.x):
			var i := row + x
			var d2 := position.distance_squared_to(_pos[i])
			if d2 < r2:
				_acc[i] += f / (10.0 + sqrt(d2))


func apply_explosive_force(force: float, position: Vector2, radius: float) -> void:
	var r2 := radius * radius
	var f := 100.0 * force * force_scale
	var b := _bounds(position, radius)
	for y in range(b.position.y, b.end.y):
		var row := y * cols
		for x in range(b.position.x, b.end.x):
			var i := row + x
			var d2 := position.distance_squared_to(_pos[i])
			if d2 < r2:
				_acc[i] += (_pos[i] - position) * (f / (10000.0 + d2))
				_damp[i] *= 0.6


func apply_implosive_force(force: float, position: Vector2, radius: float) -> void:
	var r2 := radius * radius
	var f := 10.0 * force * force_scale
	var b := _bounds(position, radius)
	for y in range(b.position.y, b.end.y):
		var row := y * cols
		for x in range(b.position.x, b.end.x):
			var i := row + x
			var d2 := position.distance_squared_to(_pos[i])
			if d2 < r2:
				_acc[i] += (position - _pos[i]) * (f / (100.0 + d2))
				_damp[i] *= 0.6


## Index rectangle (end exclusive) of cells whose rest position may lie within radius.
func _bounds(position: Vector2, radius: float) -> Rect2i:
	var local := position - origin
	var x0 := clampi(int((local.x - radius) / spacing), 0, cols - 1)
	var x1 := clampi(int((local.x + radius) / spacing) + 2, 0, cols)
	var y0 := clampi(int((local.y - radius) / spacing), 0, rows - 1)
	var y1 := clampi(int((local.y + radius) / spacing) + 2, 0, rows)
	return Rect2i(x0, y0, maxi(x1 - x0, 0), maxi(y1 - y0, 0))


# --- drawing -----------------------------------------------------------------

func _draw() -> void:
	if _pos.is_empty():
		return
	var t0 := Time.get_ticks_usec()
	var t := Engine.get_physics_interpolation_fraction() if _moving else 1.0
	var n := _pos.size()
	# Interpolate every point once (the old code did it ~6 times per point).
	_lerped.resize(n)
	if _moving:
		for i in n:
			_lerped[i] = _prev[i].lerp(_pos[i], t)
	else:
		_lerped = _pos.duplicate()
	var faint := Color(color.r * 0.8, color.g * 0.8, color.b * 0.8, color.a * 0.6)
	# Rows: continuous polylines, plus a mid-row line between each pair of rows.
	_line.resize(cols)
	_mid.resize(cols)
	for y in rows:
		var row := y * cols
		for x in cols:
			_line[x] = _lerped[row + x]
		var thick := y % 3 == 0 and not board
		draw_polyline(_line, color if thick or board else faint, thick_width if thick else thin_width, antialias_thick if thick else antialias_thin)
		if mid_lines and y > 0:
			var prev_row := row - cols
			for x in cols:
				_mid[x] = (_lerped[row + x] + _lerped[prev_row + x]) * 0.5
			draw_polyline(_mid, faint, thin_width, antialias_thin)
	# Columns.
	_line.resize(rows)
	_mid.resize(rows)
	for x in cols:
		for y in rows:
			_line[y] = _lerped[y * cols + x]
		var thick := x % 3 == 0 and not board
		draw_polyline(_line, color if thick or board else faint, thick_width if thick else thin_width, antialias_thick if thick else antialias_thin)
		if mid_lines and x > 0:
			for y in rows:
				_mid[y] = (_lerped[y * cols + x] + _lerped[y * cols + x - 1]) * 0.5
			draw_polyline(_mid, faint, thin_width, antialias_thin)
	if not _moving:
		_settled_drawn = true
	last_draw_us = Time.get_ticks_usec() - t0


