extends Node
## Game feel: camera shake, hit-stop, slow motion, screen flash and chromatic
## aberration. Everything is fire-and-forget and safe to call from anywhere.
##
## Shake uses the "trauma" model: calls add trauma, offset = trauma^2 * max,
## trauma decays linearly. Time effects use Engine.time_scale with real-time
## timers so they always restore.

const SCREEN_FX_SHADER := preload("res://assets/shaders/screen_fx.gdshader")

@export var max_shake_offset := 28.0
@export var max_shake_roll := 0.03
@export var trauma_decay := 1.6

var _trauma := 0.0
var _noise := FastNoiseLite.new()
var _t := 0.0
var _hit_stops := 0
## Real-time seconds between hit-stops. Without this, hits taken in quick
## succession chain their freezes together and the player is stun-locked.
@export var hit_stop_cooldown := 0.3
var _hit_stop_ready_at := 0.0
var _slow_scale := 1.0
var _slow_tween: Tween
## Chromatic aberration overlay (screen-texture shader: a backbuffer copy per
## frame while visible, so it is only shown while an effect is running).
var _overlay: ColorRect
var _mat: ShaderMaterial
## Plain colour flash; no screen read, essentially free.
var _flash: ColorRect
var _flash_tween: Tween
var _aberration_tween: Tween


func _ready() -> void:
	process_mode = Node.PROCESS_MODE_ALWAYS
	_noise.seed = randi()
	_noise.frequency = 2.0
	_build_overlay()
	_warm_up()


func _build_overlay() -> void:
	var layer := CanvasLayer.new()
	layer.layer = 100
	add_child(layer)
	_overlay = ColorRect.new()
	_overlay.mouse_filter = Control.MOUSE_FILTER_IGNORE
	_overlay.set_anchors_and_offsets_preset(Control.PRESET_FULL_RECT)
	_mat = ShaderMaterial.new()
	_mat.shader = SCREEN_FX_SHADER
	_overlay.material = _mat
	_overlay.visible = false
	layer.add_child(_overlay)
	_flash = ColorRect.new()
	_flash.mouse_filter = Control.MOUSE_FILTER_IGNORE
	_flash.set_anchors_and_offsets_preset(Control.PRESET_FULL_RECT)
	_flash.color = Color(1, 1, 1, 0)
	_flash.visible = false
	layer.add_child(_flash)


## Compile the post-effect shader now instead of on the first hit/death
## (GL Compatibility compiles synchronously; first use would drop a frame).
func _warm_up() -> void:
	_overlay.visible = true
	_mat.set_shader_parameter("aberration", 0.0001)
	await get_tree().process_frame
	await get_tree().process_frame
	_mat.set_shader_parameter("aberration", 0.0)
	_overlay.visible = false


var _last_wall_us := 0


func _process(_delta: float) -> void:
	# Shake decays in wall-clock time so it also settles during hit-stop.
	var now := Time.get_ticks_usec()
	var real_delta := clampf((now - _last_wall_us) / 1_000_000.0, 0.0, 0.1)
	_last_wall_us = now
	if _trauma <= 0.0:
		return
	_t += real_delta * 60.0
	_trauma = maxf(_trauma - trauma_decay * real_delta, 0.0)
	var cam := _camera()
	if cam == null:
		return
	var s := _trauma * _trauma
	cam.offset = Vector2(_noise.get_noise_2d(_t, 0.0), _noise.get_noise_2d(0.0, _t)) * max_shake_offset * s
	cam.rotation = _noise.get_noise_2d(_t, 100.0) * max_shake_roll * s
	if _trauma == 0.0:
		cam.offset = Vector2.ZERO
		cam.rotation = 0.0


func _camera() -> Camera2D:
	return get_tree().get_first_node_in_group("game_camera") as Camera2D


# --- public API ----------------------------------------------------------------

## [param amount] 0..1; stacks, capped at 1. Use for rare, big events.
func shake(amount: float) -> void:
	_trauma = minf(_trauma + amount, 1.0)


## Shake for frequent events (kills, hits). Adds [param amount] but never pushes
## trauma above [param cap], so a high kill rate cannot saturate the camera.
func shake_small(amount: float, cap := 0.3) -> void:
	if _trauma >= cap:
		return
	_trauma = minf(_trauma + amount, cap)


## Freeze the world for [param seconds] of real time (e.g. 0.06 on a hit).
func hit_stop(seconds: float) -> void:
	var now := Time.get_ticks_msec() / 1000.0
	if _hit_stops > 0 or now < _hit_stop_ready_at:
		return
	_hit_stop_ready_at = now + seconds + hit_stop_cooldown
	_hit_stops += 1
	_apply_time_scale()
	await get_tree().create_timer(seconds, true, false, true).timeout
	_hit_stops = maxi(_hit_stops - 1, 0)
	_apply_time_scale()


## Drop the world to [param scale] and ease back to full speed over [param seconds]
## of real time. Eased rather than stepped so the return never reads as a stutter.
func slow_mo(scale: float, seconds: float) -> void:
	if _slow_tween:
		_slow_tween.kill()
	_slow_scale = scale
	_apply_time_scale()
	_slow_tween = create_tween().set_ignore_time_scale(true)
	_slow_tween.tween_method(_set_slow_scale, scale, 1.0, seconds).set_trans(Tween.TRANS_QUAD).set_ease(Tween.EASE_IN)


func _set_slow_scale(v: float) -> void:
	_slow_scale = v
	_apply_time_scale()


func _apply_time_scale() -> void:
	Engine.time_scale = 0.05 if _hit_stops > 0 else _slow_scale


## Solid colour flash fading out over [param seconds].
func flash(color: Color, strength: float, seconds: float) -> void:
	if _flash_tween:
		_flash_tween.kill()
	_flash.color = Color(color.r, color.g, color.b, strength)
	_flash.visible = true
	_flash_tween = create_tween().set_ignore_time_scale(true)
	_flash_tween.tween_property(_flash, "color:a", 0.0, seconds).set_ease(Tween.EASE_OUT)
	_flash_tween.finished.connect(func(): _flash.visible = false)


## RGB split of [param amount] (0.01-0.03 is plenty) fading out over [param seconds].
## Keep it short: the overlay copies the screen every frame it is visible.
func chromatic(amount: float, seconds: float) -> void:
	_overlay.visible = true
	if _aberration_tween:
		_aberration_tween.kill()
	_mat.set_shader_parameter("aberration", amount)
	_aberration_tween = create_tween().set_ignore_time_scale(true)
	_aberration_tween.tween_method(_set_aberration, amount, 0.0, seconds).set_ease(Tween.EASE_OUT)
	_aberration_tween.finished.connect(func(): _overlay.visible = false)


## Reset everything (scene changes, restart).
func reset() -> void:
	_trauma = 0.0
	if _slow_tween:
		_slow_tween.kill()
	_slow_scale = 1.0
	_hit_stops = 0
	_hit_stop_ready_at = 0.0
	Engine.time_scale = 1.0
	_mat.set_shader_parameter("aberration", 0.0)
	_overlay.visible = false
	_flash.visible = false
	var cam := _camera()
	if cam:
		cam.offset = Vector2.ZERO
		cam.rotation = 0.0


func _set_aberration(a: float) -> void:
	_mat.set_shader_parameter("aberration", a)
