class_name PowerupPickup
extends Area2D
## A power-up icon lying in the arena. Port of sprites/Power.java.
## Disappears after [constant Constants.POWERUP_PICKUP_LIFETIME_TICKS] if not collected.

signal collected(pickup: PowerupPickup)

var type: Constants.PowerupType = Constants.PowerupType.SUPER_FORCE
var _ticks := 0
var _collected := false

@onready var _sprite: Sprite2D = $Sprite2D
@onready var _core: GlowCore = $Core

const COLOR_INSTANT := Color(1.8, 1.8, 1.9)
const COLOR_TIMED := Color(0.5, 1.7, 2.4)  # passives: dash-cyan plate


func setup(powerup_type: Constants.PowerupType) -> void:
	type = powerup_type


func is_passive() -> bool:
	return type in Constants.PASSIVE_POWERUPS


func is_instant() -> bool:
	return not is_passive()


func icon() -> Texture2D:
	return load(Constants.POWERUP_ICONS[type])


func _ready() -> void:
	_sprite.texture = icon()
	_core.color = COLOR_INSTANT if is_instant() else COLOR_TIMED
	collision_layer = Constants.LAYER_INSTANT_POWERUP
	collision_mask = Constants.LAYER_HERO | Constants.LAYER_HERO_INVINCIBLE
	body_entered.connect(_on_body_entered)


func _physics_process(_delta: float) -> void:
	_ticks += 1
	if _ticks > Constants.POWERUP_PICKUP_LIFETIME_TICKS:
		queue_free()


func _on_body_entered(body: Node) -> void:
	if _collected or not body is Hero:
		return
	_collected = true
	collected.emit.call_deferred(self)
	queue_free()
