class_name Particle
extends RigidBody2D
## Enemy particle. Port of sprites/Particle.java and the per-particle update
## logic from GameScreenWorldController.updateParticles / checkSuicideParticle.
##
## All four enemy types share this scene; [member type] selects behaviour:
## - NORMAL   chases the hero; dies on saws; respawns as a new normal.
## - SPLIT    faster; periodically spawns another split particle next to itself.
## - SUICIDE  fastest; explodes after a fuse, killing everything in the blast radius.
## - INVISIBLE stationary; cycles visible (vulnerable) -> solid & deadly -> fading -> gone.

signal killed(particle: Particle)
signal exploded(particle: Particle)
signal wants_split(particle: Particle)

enum InvisiblePhase { APPEARING, DEADLY, DISAPPEARING, HIDDEN }

## Ticks for the fade in/out of an invisible particle (the original 33-frame
## animation at 40 fps). While fading it is vulnerable; once solid it is deadly.
const INVISIBLE_FADE_TICKS := 75

var type: Constants.ParticleType = Constants.ParticleType.NORMAL
var speed := Constants.NORMAL_PARTICLE_SPEED_BASE
var alive := true
## The saw that killed this particle (set before [signal killed]).
var killed_by: Node = null
## Ticks remaining during which a death counts as a dash kill.
var launched_ticks := 0
## The launch was a head-on "perfect" dash hit.
var launched_perfect := false
## Unit vector from this particle to the hero, refreshed every tick (used for knockback).
var to_hero := Vector2.ZERO

## Menu mode: no hero to chase, just drift in a straight line until off-screen.
var drift := false
var _drift_velocity := Vector2.ZERO

var _hero: Node2D
var _mine: MineBody
var _blink := 0.0
var _knockback_ticks := -1
var _split_ticks := 0
var _fuse_ticks := 0
var _phase_ticks := 0
var _phase := InvisiblePhase.APPEARING

@onready var _trail: Trail = $Trail
@onready var _core: GlowCore = $Core
@onready var _ribbon: Ribbon = $Ribbon


## Call before adding to the tree.
func setup(particle_type: Constants.ParticleType, hero: Node2D, base_speed: float) -> void:
	type = particle_type
	_hero = hero
	speed = base_speed


## Menu decoration: fly in a straight line, die on the play-button saw or off-screen.
func setup_drift(particle_type: Constants.ParticleType, velocity: Vector2) -> void:
	type = particle_type
	drift = true
	_drift_velocity = velocity


func _ready() -> void:
	mass = 1.0
	lock_rotation = true
	contact_monitor = true
	max_contacts_reported = 8
	_set_vulnerable_layers()
	body_entered.connect(_on_body_entered)
	var th := Themes.current
	_core.fruit = th.particle_style == GameTheme.ParticleStyle.FRUIT
	_ribbon.max_points = mini(th.ribbon_points, 16)
	_ribbon.modulate.a = th.trail_alpha
	match type:
		Constants.ParticleType.NORMAL:
			_set_look(th.normal)
		Constants.ParticleType.SPLIT:
			_set_look(th.split)
			_core.radius = 16.0
		Constants.ParticleType.SUICIDE:
			_set_look(th.suicide)
			_core.pulse_speed = 6.0
			_core.pulse_amount = 0.25
		Constants.ParticleType.INVISIBLE:
			_trail.visible = false
			_trail.emitting = false
			_ribbon.visible = false
			_core.visible = false
			_mine = MineBody.new()
			_mine.cactus = Themes.current.particle_style == GameTheme.ParticleStyle.FRUIT
			add_child(_mine)
			# A mine is a fixture: nothing (hero, crowd, launched projectiles) may shove it.
			freeze_mode = RigidBody2D.FREEZE_MODE_STATIC
			freeze = true
	add_to_group("particles")
	if drift:
		linear_velocity = _drift_velocity


func _physics_process(_delta: float) -> void:
	if not alive:
		return
	if drift:
		_tick_drift()
		return
	if _hero == null:
		return
	var scale_factor: float = Game.time_scale
	to_hero = (_hero.global_position - global_position).normalized()

	if launched_ticks > 0:
		launched_ticks -= 1  # flying from a dash: keep going, no re-aim
		if launched_ticks == 0:
			_set_vulnerable_layers()  # back to colliding with the crowd
	elif _knockback_ticks >= 0:
		_knockback_ticks += 1
		if _knockback_ticks > Constants.KNOCKBACK_TICKS:
			_knockback_ticks = -1
	else:
		linear_velocity = to_hero * speed * scale_factor

	match type:
		Constants.ParticleType.SPLIT:
			_split_ticks += 1
		Constants.ParticleType.SUICIDE:
			_tick_fuse()
		Constants.ParticleType.INVISIBLE:
			_tick_invisible()


## Sparkle emitters are off for enemies by default: one GPU emitter per enemy
## was the single biggest frame-time cost. Flip this on to bring them back.
@export var sparkles := false


func _set_look(tint: Color) -> void:
	_core.color = tint
	_ribbon.color = tint
	_trail.visible = sparkles
	_trail.emitting = sparkles
	if sparkles:
		_trail.set_tint(tint)


func _tick_drift() -> void:
	linear_velocity = _drift_velocity
	var margin := 200.0
	if (
		global_position.x < -margin or global_position.x > Constants.APP_WIDTH + margin
		or global_position.y < -margin or global_position.y > Constants.APP_HEIGHT + margin
	):
		queue_free()


## Split particles ask the Game to spawn a sibling once their timer passes [param split_time].
func poll_split(split_time: int) -> void:
	if _split_ticks > split_time:
		_split_ticks = 0
		wants_split.emit(self)


func _tick_fuse() -> void:
	_fuse_ticks += 1
	# Visual warning: the trail pulses faster the closer the fuse is to detonation
	# (the original sped up a sprite animation that was never drawn).
	var t := float(_fuse_ticks) / Constants.SUICIDE_BLAST_TICKS
	_trail.speed_scale = 1.0 + t
	# The body itself is the fuse: it blinks between dim yellow and hot white,
	# faster as detonation nears (2 Hz -> 10 Hz), and shifts toward red.
	_blink += (TAU / 60.0) * (2.0 + 8.0 * t * t)
	var on := 0.5 + 0.5 * sin(_blink)
	var base := Themes.current.suicide.lerp(Color(3.0, 0.7, 0.2) if not _core.fruit else Color(0.9, 0.25, 0.1), t)
	_core.color = base.lerp(Color(3.2, 3.2, 3.0) if not _core.fruit else Color(1.0, 1.0, 0.9), on * (0.5 + 0.5 * t))
	_core.fill = lerpf(0.25, 1.0, on)
	_core.scale = Vector2.ONE * (1.0 + 0.25 * on * t)
	_ribbon.color = base
	if t > 0.8:
		_core.pulse_speed = 0.0
		_core.queue_redraw()
	if _fuse_ticks > Constants.SUICIDE_BLAST_TICKS:
		alive = false
		exploded.emit(self)


func _tick_invisible() -> void:
	_phase_ticks += 1
	var fade := clampf(float(_phase_ticks) / INVISIBLE_FADE_TICKS, 0.0, 1.0)
	match _phase:
		InvisiblePhase.APPEARING:
			_mine.state = MineBody.State.FORMING
			_mine.fade = fade
			if _phase_ticks >= INVISIBLE_FADE_TICKS:
				_phase = InvisiblePhase.DEADLY
				_mine.state = MineBody.State.ARMED
				_set_deadly_layers()
		InvisiblePhase.DISAPPEARING:
			_mine.state = MineBody.State.FADING
			_mine.fade = fade
			if _phase_ticks >= INVISIBLE_FADE_TICKS:
				_phase = InvisiblePhase.HIDDEN
				_mine.state = MineBody.State.HIDDEN
				_set_hidden_layers()
	if _phase_ticks > Constants.INVISIBLE_PHASE_TICKS:
		_phase_ticks = 0
		if _phase == InvisiblePhase.APPEARING or _phase == InvisiblePhase.DEADLY:
			_phase = InvisiblePhase.DISAPPEARING
		else:
			_phase = InvisiblePhase.APPEARING
		_set_vulnerable_layers()


func is_deadly() -> bool:
	return type == Constants.ParticleType.INVISIBLE and _phase == InvisiblePhase.DEADLY


func _set_vulnerable_layers() -> void:
	collision_layer = Constants.LAYER_PARTICLE
	collision_mask = (
		Constants.LAYER_PARTICLE | Constants.LAYER_HERO | Constants.LAYER_SAW
		| Constants.LAYER_HERO_INVINCIBLE
	)


func _set_deadly_layers() -> void:
	collision_layer = Constants.LAYER_INVISIBLE_DEADLY
	collision_mask = Constants.LAYER_HERO


func _set_hidden_layers() -> void:
	collision_layer = Constants.LAYER_INVISIBLE_HIDDEN
	collision_mask = 0


## Shove the particle away from the hero and pause its chase for a few ticks.
func knock_back(knockback_speed: float) -> void:
	linear_velocity = -to_hero * knockback_speed
	_knockback_ticks = 0


## Dash impact: fire the particle along [param direction]. While launched it is a
## projectile: it ignores other particles (no stalling in a crowd) and only
## stops on blades or the hero.
func launch(direction: Vector2, launch_speed: float, perfect := false) -> void:
	linear_velocity = direction.normalized() * launch_speed
	# No re-aim for the whole launch window: a shoved particle flies until it hits a blade.
	_knockback_ticks = -1
	launched_ticks = Constants.DASH_KILL_WINDOW_TICKS
	launched_perfect = perfect
	if type != Constants.ParticleType.INVISIBLE:
		collision_mask = Constants.LAYER_HERO | Constants.LAYER_SAW | Constants.LAYER_HERO_INVINCIBLE


func _on_body_entered(body: Node) -> void:
	if not alive or not body.is_in_group("saw"):
		return
	if body is Saw and body.dull:
		return  # overheated blade: bounce, no kill
	killed_by = body
	kill()


func kill() -> void:
	if not alive:
		return
	alive = false
	# Deferred: kill() is often called from a physics callback, and the Game
	# spawns/frees bodies in response, which is not allowed mid-flush.
	killed.emit.call_deferred(self)
