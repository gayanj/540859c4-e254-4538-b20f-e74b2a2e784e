class_name Hero
extends RigidBody2D
## The player's particle. Port of sprites/Hero.java plus the hero-related
## branches of the Box2D contact listener in GameScreenWorldController.
##
## Movement: while there is input, velocity is set directly (as the original did
## from the touchpad); when input stops, linear damping brings it to rest.

signal energy_changed(energy: int)
## [param cause]: "saw", "death_saw", "invisible", "blast" or "energy".
signal died(cause: String)
## A living enemy particle touched the hero (hero loses energy, enemy is knocked back).
signal particle_hit(particle: Particle)
## A particle was rammed during a dash (no damage taken; the Game launches it).
signal dash_hit(particle: Particle)
signal dashed(direction: Vector2)
## Dash ended early: "brake" (stick pulled against the dash) or "cancel" (stick turned away).
signal dash_interrupted(kind: String)

const BASE_MASS := 2.0
const ARMOR_MASS_STEP := 1.0

var energy := Constants.HERO_START_ENERGY:
	set(value):
		var clamped := maxi(value, 0)
		if clamped != energy:
			energy = clamped
			energy_changed.emit(energy)
var speed := Constants.HERO_SPEED_BASE
var knockback_speed := Constants.COLLISION_KNOCKBACK
var invincible := false:
	set(value):
		if value == invincible:
			return
		invincible = value
		collision_layer = Constants.LAYER_HERO_INVINCIBLE if invincible else Constants.LAYER_HERO
		var orb := _body == null or not _body.blade
		_trail.visible = not invincible and orb
		_trail.emitting = not invincible and orb
		_invincible_trail.visible = false
		_invincible_trail.emitting = false
		if _flames:
			if _fire_tween:
				_fire_tween.kill()
			_fire_tween = create_tween()
			# Ignite fast, burn out slow.
			_fire_tween.tween_property(_flames, "intensity", 1.0 if invincible else 0.0, 0.25 if invincible else 0.6).set_ease(Tween.EASE_OUT)
		if _core:
			_core.color = Constants.theme().hero_invincible if invincible else Constants.theme().hero
			_ribbon.color = _core.color
var alive := true
var death_cause := ""
## Preview stages set this; die() becomes a no-op.
var immortal := false
var dashing := false
var _dash_ticks := 0
var _dash_cooldown := 0
var _dash_dir := Vector2.ZERO
## Last non-zero movement direction; dash falls back to it.
var facing := Vector2.UP
## Direction/magnitude of the current movement input, set by the Game each tick.
var move_input := Vector2.ZERO

@onready var _trail: Trail = $Trail
@onready var _invincible_trail: Trail = $InvincibleTrail
@onready var _core: GlowCore = $Core
@onready var _ribbon: Ribbon = $Ribbon
@onready var _body: HeroBody = $Body
var _flames: Flames

var _flinch_tween: Tween
var _fire_tween: Tween


func _ready() -> void:
	mass = BASE_MASS
	linear_damp = Constants.HERO_LINEAR_DAMP
	lock_rotation = true
	contact_monitor = true
	max_contacts_reported = 16
	collision_layer = Constants.LAYER_HERO
	collision_mask = (
		Constants.LAYER_PARTICLE | Constants.LAYER_SAW | Constants.LAYER_INVISIBLE_DEADLY
		| Constants.LAYER_POWERUP | Constants.LAYER_INSTANT_POWERUP
	)
	body_entered.connect(_on_body_entered)
	_apply_theme(Themes.current)
	_flames = Flames.new()
	_flames.intensity = 0.0
	_flames.z_index = -1  # tail behind the body
	add_child(_flames)
	_flames.outer_color = Themes.current.flame_outer
	_flames.mid_color = Themes.current.flame_mid
	_flames.core_color = Themes.current.flame_core
	_invincible_trail.visible = false
	_invincible_trail.emitting = false


func _process(_delta: float) -> void:
	if _flames == null or not _flames.visible:
		return
	# Flames live in hero-local space; the knife is drawn along `facing`, so
	# express wind and the spine in that frame.
	_flames.wind = linear_velocity / Constants.HERO_SPEED_BASE * 0.5
	if _body.blade:
		# Rooted at the heel; streams back past the handle when moving, and
		# rests pointing back along the handle when still.
		_flames.anchor = facing * -6.0
		_flames.rest_dir = -facing
		_flames.length = 130.0
		_flames.width = 46.0
	else:
		_flames.anchor = Vector2.ZERO
		_flames.rest_dir = Vector2.UP
		_flames.length = 110.0
		_flames.width = 50.0
	_body.burning = invincible


func _physics_process(_delta: float) -> void:
	if not alive:
		return
	if _dash_cooldown > 0:
		_dash_cooldown -= 1
	if dashing:
		# Steering out of a dash: pull against it to brake dead, or turn hard to
		# cancel into normal movement. First two ticks are ignored so the stick
		# position that triggered the dash can't cancel it.
		var elapsed := dash_length_ticks - _dash_ticks
		if elapsed >= 2 and move_input.length() > 0.5:
			var dot := move_input.normalized().dot(_dash_dir)
			if dot < -0.5:
				_end_dash()
				linear_velocity = Vector2.ZERO
				dash_interrupted.emit("brake")
				return
			elif dot < 0.5:
				_end_dash()
				linear_velocity = move_input.limit_length(1.0) * speed
				dash_interrupted.emit("cancel")
				return
		linear_velocity = _dash_dir * Constants.DASH_SPEED
		_dash_ticks -= 1
		if _dash_ticks <= 0:
			_end_dash()
		return
	# Original quirk: it only applied input when BOTH axes were non-zero, which
	# silently ignored pure horizontal/vertical pushes. Fixed here.
	if move_input.length_squared() > 0.0:
		facing = move_input.normalized()
		_body.facing = facing
		linear_velocity = move_input.limit_length(1.0) * speed


## Dash parameters, set by the Game from the PowerupManager before each dash.
var dash_cost := Constants.DASH_COST
var dash_length_ticks := Constants.DASH_TICKS
var dash_cooldown_ticks := Constants.DASH_COOLDOWN_TICKS
var shockwave_dash := false
var shockwave_spent := false


func _apply_theme(t: GameTheme) -> void:
	if _flames:
		_flames.outer_color = t.flame_outer
		_flames.mid_color = t.flame_mid
		_flames.core_color = t.flame_core
	_core.color = t.hero
	_ribbon.color = t.hero
	_ribbon.max_points = t.ribbon_points
	_ribbon.modulate.a = t.trail_alpha
	_body.blade = t.hero_style == GameTheme.HeroStyle.BLADE
	_body.tint = t.hero
	# A blade is its own silhouette; hide the orb core and sparkle trail.
	_core.visible = not _body.blade
	_trail.visible = not _body.blade
	_trail.emitting = not _body.blade
	if _body.blade:
		_ribbon.ribbon_width = 14.0
		_ribbon.width = 14.0


func can_dash() -> bool:
	return alive and not dashing and _dash_cooldown <= 0 and energy > dash_cost


## 0..1 fraction of the cooldown remaining (for the HUD).
func dash_cooldown_left() -> float:
	return clampf(float(_dash_cooldown) / (dash_cooldown_ticks + dash_length_ticks), 0.0, 1.0)


## Burst in [param direction] (falls back to [member facing]). Costs energy.
func dash(direction: Vector2 = Vector2.ZERO) -> bool:
	if not can_dash():
		return false
	_dash_dir = direction.normalized() if direction.length_squared() > 0.0 else facing
	facing = _dash_dir
	_body.facing = facing
	dashing = true
	_dash_ticks = dash_length_ticks
	_dash_cooldown = dash_cooldown_ticks + dash_length_ticks
	energy -= dash_cost
	linear_velocity = _dash_dir * Constants.DASH_SPEED
	var w := 28.0 if _body.blade else 44.0
	_ribbon.ribbon_width = w
	_ribbon.width = w
	_core.scale = Vector2(1.5, 0.8)
	_core.rotation = _dash_dir.angle()
	if shockwave_dash:
		_core.color = Color(2.4, 2.0, 0.6)
	dashed.emit(_dash_dir)
	return true


func _end_dash() -> void:
	dashing = false
	shockwave_dash = false
	shockwave_spent = false
	linear_velocity = _dash_dir * speed
	var w2 := 14.0 if _body.blade else 22.0
	_ribbon.ribbon_width = w2
	_ribbon.width = w2
	_core.color = Constants.theme().hero_invincible if invincible else Constants.theme().hero
	var tw := create_tween().set_parallel(true)
	tw.tween_property(_core, "scale", Vector2.ONE, 0.12).set_ease(Tween.EASE_OUT)
	tw.tween_property(_core, "rotation", 0.0, 0.12)


func _on_body_entered(body: Node) -> void:
	if not alive:
		return
	if body is Particle:
		var p := body as Particle
		if p.is_deadly():
			if invincible:
				p.kill()  # armed mine shattered by an invincible hero
			else:
				die("invisible")
		elif dashing:
			dash_hit.emit(p)
		elif invincible:
			p.kill()
			# Original restored +5 energy per kill while invincible, capped at 75.
			if energy < 75:
				energy += 5
		else:
			particle_hit.emit(p)
	elif body.is_in_group("saw"):
		if invincible:
			return  # true i-frames: blades just bounce you
		die(body.cause_name() if body is DeathSaw else "saw")


## Restore energy when an enemy of the given type dies on a saw (original caps differ per type).
## A dash kill pays double and ignores the caps (up to full), so dashing is energy-positive.
func reward_for_kill(type: Constants.ParticleType, dash_kill := false) -> void:
	if dash_kill:
		var amount := 10
		match type:
			Constants.ParticleType.SPLIT: amount = 12
			Constants.ParticleType.SUICIDE: amount = 20
			Constants.ParticleType.INVISIBLE: amount = 30
		energy = mini(energy + amount, Constants.HERO_START_ENERGY)
		return
	match type:
		Constants.ParticleType.NORMAL:
			if energy < 75:
				energy += 5
		Constants.ParticleType.SPLIT:
			if energy < 74:
				energy += 6
		Constants.ParticleType.SUICIDE:
			if energy < 70:
				energy += 10
		Constants.ParticleType.INVISIBLE:
			if energy < 60:
				energy += 20


func take_hit() -> void:
	energy -= 1
	flinch()


## Local hit feedback: the core flashes white and punches up in scale.
func flinch() -> void:
	if _flinch_tween:
		_flinch_tween.kill()
	var base := Constants.theme().hero_invincible if invincible else Constants.theme().hero
	_core.color = Color(2.4, 2.4, 2.4)
	_core.scale = Vector2.ONE * 1.35
	_flinch_tween = create_tween().set_parallel(true)
	_flinch_tween.tween_property(_core, "color", base, 0.18).set_ease(Tween.EASE_OUT)
	_flinch_tween.tween_property(_core, "scale", Vector2.ONE, 0.16).set_trans(Tween.TRANS_BACK).set_ease(Tween.EASE_OUT)


func add_armor() -> void:
	mass += ARMOR_MASS_STEP


func die(cause := "unknown") -> void:
	if not alive or immortal:
		return
	alive = false
	death_cause = cause
	_trail.emitting = false
	_invincible_trail.emitting = false
	visible = false
	# Freeze in place; the Game spawns the death burst and shows game over.
	set_deferred("freeze", true)
	died.emit.call_deferred(cause)
