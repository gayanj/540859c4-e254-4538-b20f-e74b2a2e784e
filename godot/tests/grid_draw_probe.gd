extends Node
## Measures SpringGrid draw time per frame at several densities (windowed).
var _grid: SpringGrid
var _targets := [1000, 1400]
var _i := 0
var _samples: Array = []
var _frames := 0

func _ready() -> void:
	_next()

func _next() -> void:
	if _grid: _grid.queue_free()
	if _i >= _targets.size():
		get_tree().quit(); return
	_grid = SpringGrid.new()
	_grid.target_points = _targets[_i]
	add_child(_grid)
	_samples.clear(); _frames = 0

func _process(_d: float) -> void:
	if _grid == null: return
	_grid.apply_directed_force(Vector2(500, 300), Vector2(400 + 300 * sin(_frames * 0.05), 900), 90.0)
	_frames += 1
	if _frames > 10: _samples.append(SpringGrid.last_draw_us)
	if _frames == 130:
		_samples.sort()
		print("target=%d points=%d  draw median %.2f ms  p95 %.2f ms" % [_targets[_i], _grid._pos.size(), _samples[_samples.size()/2]/1000.0, _samples[int(_samples.size()*0.95)]/1000.0])
		_i += 1; _next()
