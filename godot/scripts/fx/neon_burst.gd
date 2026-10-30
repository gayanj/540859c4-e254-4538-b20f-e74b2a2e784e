class_name NeonBurst
extends Node2D
## One-shot shatter: an expanding ring flash plus a radial spray of additive
## sparks. Replaces the pre-rendered dying/explosion sprite sheets.

const SPARK_TEXTURE := preload("res://assets/particles/normalparticle.png")

var color := Color(2.8, 0.35, 0.3)
var size := 1.0
var spark_count := 28
var duration := 0.6

var _ring_t := 0.0

static var _material_cache: Dictionary = {}
static var _additive: CanvasItemMaterial


static func make(at: Vector2, tint: Color, scale_factor := 1.0, sparks := 28, life := 0.6) -> NeonBurst:
	var b := NeonBurst.new()
	b.global_position = at
	b.color = tint
	b.size = scale_factor
	b.spark_count = sparks
	b.duration = life
	return b


var _splat := false
var _blobs: Array = []  # [pos, radius] for the splat decal


func _ready() -> void:
	_splat = Themes.current.burst_style == GameTheme.BurstStyle.SPLAT
	if _splat:
		_ready_splat()
		return
	var p := GPUParticles2D.new()
	p.one_shot = true
	p.explosiveness = 1.0
	p.amount = spark_count
	p.lifetime = duration
	p.local_coords = false
	p.texture = SPARK_TEXTURE
	p.process_material = _shared_material()
	if _additive == null:
		_additive = CanvasItemMaterial.new()
		_additive.blend_mode = CanvasItemMaterial.BLEND_MODE_ADD
	p.material = _additive
	add_child(p)
	p.emitting = true

	var tw := create_tween()
	tw.tween_method(_set_ring, 0.0, 1.0, duration * 0.5).set_ease(Tween.EASE_OUT)
	get_tree().create_timer(duration + 0.1).timeout.connect(queue_free)


func _shared_material() -> ParticleProcessMaterial:
	var key := "%s|%.2f" % [color.to_html(), size]
	if not _material_cache.has(key):
		_material_cache[key] = _build_material()
	return _material_cache[key]


func _build_material() -> ParticleProcessMaterial:
	var mat := ParticleProcessMaterial.new()
	mat.particle_flag_disable_z = true
	mat.direction = Vector3(1, 0, 0)
	mat.spread = 180.0
	mat.gravity = Vector3.ZERO
	mat.initial_velocity_min = 220.0 * size
	mat.initial_velocity_max = 620.0 * size
	mat.damping_min = 300.0
	mat.damping_max = 500.0
	# Bigger bursts spread further and have more sparks, but sparks only grow
	# with the square root so a large explosion stays a spray, not a blob.
	mat.scale_min = 0.35 * sqrt(size)
	mat.scale_max = 0.85 * sqrt(size)
	var curve := Curve.new()
	curve.add_point(Vector2(0.0, 1.0))
	curve.add_point(Vector2(1.0, 0.0))
	var ct := CurveTexture.new()
	ct.curve = curve
	mat.scale_curve = ct
	mat.color = color
	return mat


func _set_ring(t: float) -> void:
	_ring_t = t
	queue_redraw()


func _draw() -> void:
	if _splat:
		var c := Color(color.r, color.g, color.b, 0.9)
		draw_circle(Vector2.ZERO, 22.0 * sqrt(size), c)
		for b in _blobs:
			draw_circle(b[0], b[1], c)
		return
	if _ring_t <= 0.0 or _ring_t >= 1.0:
		return
	var r := lerpf(10.0, 90.0, _ring_t) * size
	var a := 1.0 - _ring_t
	draw_arc(Vector2.ZERO, r, 0.0, TAU, 48, Color(color.r, color.g, color.b, a), 4.0 * (1.0 - _ring_t) + 1.0, true)
	draw_circle(Vector2.ZERO, r * 0.6, Color(color.r, color.g, color.b, a * 0.15))


## Fruit Ninja: juice. A few flat blobs splash outward (gravity-free, it's a
## top-down board) and a decal of droplets lingers, then fades.
func _ready_splat() -> void:
	var p := GPUParticles2D.new()
	p.one_shot = true
	p.explosiveness = 1.0
	p.amount = int(spark_count * 0.6)
	p.lifetime = duration * 0.8
	p.local_coords = false
	p.texture = SPARK_TEXTURE
	var mat := ParticleProcessMaterial.new()
	mat.particle_flag_disable_z = true
	mat.direction = Vector3(1, 0, 0)
	mat.spread = 180.0
	mat.gravity = Vector3.ZERO
	mat.initial_velocity_min = 150.0 * size
	mat.initial_velocity_max = 420.0 * size
	mat.damping_min = 500.0
	mat.damping_max = 800.0
	mat.scale_min = 0.6 * sqrt(size)
	mat.scale_max = 1.4 * sqrt(size)
	mat.color = Color(color.r, color.g, color.b, 1.0)
	p.process_material = mat
	add_child(p)
	p.emitting = true
	var rng := RandomNumberGenerator.new()
	for i in 7:
		var a := rng.randf() * TAU
		var d := rng.randf_range(10.0, 60.0) * size
		_blobs.append([Vector2(cos(a), sin(a)) * d, rng.randf_range(6.0, 16.0) * sqrt(size)])
	var tw := create_tween()
	tw.tween_interval(duration * 0.5)
	tw.tween_property(self, "modulate:a", 0.0, duration * 0.8)
	tw.tween_callback(queue_free)
	queue_redraw()
