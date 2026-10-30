extends SceneTree
## Micro-benchmark: godot --headless --path godot -s tests/grid_bench.gd
func _init() -> void:
	for target in [455, 700, 1000, 1400, 2000]:
		var g := SpringGrid.new()
		g.target_points = target
		g.build()
		var t0 := Time.get_ticks_usec()
		for i in 240:
			g.apply_directed_force(Vector2(500, 0), Vector2(540, 960), 90.0)
			for k in 12:
				g.apply_directed_force(Vector2(500, 300), Vector2(200 + k * 70, 700), 70.0)
			if i % 30 == 0:
				g.apply_explosive_force(10.0, Vector2(300, 300), 170.0)
			g._step()
		var dt := (Time.get_ticks_usec() - t0) / 240.0
		print("target=%d points=%d springs=%d spacing=%.0fpx  step+forces: %.3f ms/tick" % [target, g._pos.size(), g._s_a.size(), g.spacing, dt / 1000.0])
	quit()
