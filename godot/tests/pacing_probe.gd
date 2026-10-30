extends Node
## Frame pacing baseline: an empty scene, then just WorldFx, then the menu.
var _last := 0
var _samples: Array = []
var _rec := false

func _ready() -> void:
	await _measure("empty scene", null)
	await _measure("world_fx only", preload("res://scenes/fx/world_fx.tscn"))
	await _measure("menu", preload("res://scenes/menu/menu.tscn"))
	get_tree().quit()

func _measure(label: String, scene: PackedScene) -> void:
	var node: Node = scene.instantiate() if scene else Node.new()
	add_child(node)
	for i in 60:
		await get_tree().process_frame
	_last = Time.get_ticks_usec()
	_rec = true
	for i in 180:
		await get_tree().process_frame
	_rec = false
	_samples.sort()
	var n := _samples.size()
	print("%-14s median=%.1fms p95=%.1fms max=%.1fms >20ms: %d" % [label, _samples[n / 2], _samples[int(n * 0.95)], _samples[n - 1], _samples.filter(func(x): return x > 20.0).size()])
	_samples.clear()
	node.queue_free()
	await get_tree().process_frame

func _process(_d: float) -> void:
	if _rec:
		var now := Time.get_ticks_usec()
		_samples.append((now - _last) / 1000.0)
		_last = now
