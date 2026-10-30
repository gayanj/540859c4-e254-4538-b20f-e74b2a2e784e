class_name Trail
extends GPUParticles2D
## Glowing particle trail. Replaces the LibGDX ParticleEffect (.p) files, which
## spawned a short burst of shrinking, tinted "normalparticle.png" sprites at the
## body position every frame. Here a single continuous emitter does the same job.
##
## The material is built in code so the look is driven by a few exported values
## instead of a hand-edited resource; tweak them in the inspector.

@export var tints: Array[Color] = [Color.WHITE]
## Diameter of a freshly spawned sprite in px (the .p "Scale" value).
@export var size_px := 50.0
## Random initial speed in px/s (hero effect used 100, enemies 0).
@export var burst_speed := 0.0
@export var particle_lifetime := 0.2
@export var particle_amount := 120
## Additive blending: overlapping sparks add up to white-hot and bloom.
@export var additive := true

const TEXTURE_SIZE := 32.0

## Process materials are shared between every trail with the same look, so a
## spawn never allocates GPU resources (and the shader is compiled once).
static var _material_cache: Dictionary = {}
static var _additive_material: CanvasItemMaterial


func _ready() -> void:
	texture = preload("res://assets/particles/normalparticle.png")
	amount = particle_amount
	lifetime = particle_lifetime
	local_coords = false
	process_material = _shared_material()
	if additive:
		if _additive_material == null:
			_additive_material = CanvasItemMaterial.new()
			_additive_material.blend_mode = CanvasItemMaterial.BLEND_MODE_ADD
		material = _additive_material


func _shared_material() -> ParticleProcessMaterial:
	var key := "%s|%.1f|%.1f" % [str(tints), size_px, burst_speed]
	if not _material_cache.has(key):
		_material_cache[key] = _build_material()
	return _material_cache[key]


func _build_material() -> ParticleProcessMaterial:
	var mat := ParticleProcessMaterial.new()
	mat.particle_flag_disable_z = true
	mat.direction = Vector3(1, 0, 0)
	mat.spread = 180.0
	mat.gravity = Vector3.ZERO
	mat.initial_velocity_min = 0.0
	mat.initial_velocity_max = burst_speed
	var s := size_px / TEXTURE_SIZE
	mat.scale_min = s
	mat.scale_max = s
	mat.scale_curve = _shrink_curve()
	mat.color_initial_ramp = _tint_ramp()
	return mat


## Linear shrink from full size to zero over the particle's life.
func _shrink_curve() -> CurveTexture:
	var curve := Curve.new()
	curve.add_point(Vector2(0.0, 1.0))
	curve.add_point(Vector2(1.0, 0.0))
	var tex := CurveTexture.new()
	tex.curve = curve
	return tex


## Picks one of [member tints] at random per particle (constant-interpolated gradient).
func _tint_ramp() -> GradientTexture1D:
	var grad := Gradient.new()
	grad.interpolation_mode = Gradient.GRADIENT_INTERPOLATE_CONSTANT
	var n := maxi(tints.size(), 1)
	var offsets := PackedFloat32Array()
	var colors := PackedColorArray()
	for i in n:
		offsets.append(float(i) / n)
		colors.append(tints[i] if i < tints.size() else Color.WHITE)
	grad.offsets = offsets
	grad.colors = colors
	var tex := GradientTexture1D.new()
	tex.gradient = grad
	return tex


func set_tint(color: Color) -> void:
	tints = [color]
	if is_node_ready():
		process_material = _shared_material()
