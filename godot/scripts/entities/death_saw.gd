class_name DeathSaw
extends AnimatableBody2D
## A saw that launches from one edge and sweeps across the arena.
## Port of sprites/DeathSaw.java. Spawned by the Spawner once suicide particles
## are unlocked; it waits a few ticks at the edge, then moves at constant speed
## and frees itself once off-screen.

var velocity := Vector2.ZERO
var _delay_ticks := 0
var _launched := false
## Ticks the saw waits at the edge with its lane telegraphed.
const LAUNCH_DELAY_TICKS := 45
var launch_delay := LAUNCH_DELAY_TICKS
## True when this blade was thrown by an overheated wall saw.
var from_overheat := false


func cause_name() -> String:
	return "overheat_saw" if from_overheat else "death_saw"

func _ready() -> void:
	add_to_group("saw")
	sync_to_physics = true
	collision_layer = Constants.LAYER_SAW
	collision_mask = Constants.LAYER_PARTICLE | Constants.LAYER_HERO | Constants.LAYER_HERO_INVINCIBLE
	var blade: SawBlade = $Blade
	blade.juicer = Themes.current.saw_style == GameTheme.SawStyle.JUICER
	blade.rim_color = Themes.current.death_saw


func _physics_process(delta: float) -> void:
	if not _launched:
		_delay_ticks += 1
		queue_redraw()
		if _delay_ticks > launch_delay:
			_launched = true
			queue_redraw()
		return
	global_position += velocity * delta * Game.time_scale
	var margin := 200.0
	if (
		global_position.x < -margin or global_position.x > Constants.APP_WIDTH + margin
		or global_position.y < -margin or global_position.y > Constants.APP_HEIGHT + margin
	):
		queue_free()


## Lane telegraph: a pulsing line along the path, brighter as launch nears.
func _draw() -> void:
	if _launched or velocity == Vector2.ZERO:
		return
	var t := float(_delay_ticks) / launch_delay
	var dir := velocity.normalized()
	var length := 3000.0
	var base := Themes.current.death_saw
	var col := Color(base.r, base.g, base.b, 0.15 + 0.5 * t * (0.6 + 0.4 * absf(sin(_delay_ticks * 0.5))))
	draw_line(Vector2.ZERO, dir * length, col, 6.0 + 60.0 * (1.0 - t) * 0.2, true)
	draw_line(Vector2.ZERO, dir * length, Color(col.r, col.g, col.b, col.a * 0.25), 60.0, false)
