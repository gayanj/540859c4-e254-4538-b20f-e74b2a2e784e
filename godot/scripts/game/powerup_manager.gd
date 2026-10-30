class_name PowerupManager
extends Node
## Power-ups as dash upgrades. Every pickup applies on contact:
## - passives (Afterburner, Reinforced, Impact, Focus) stack per run, capped;
## - consumables: Energy refills, Skull adds a Shockwave charge (next dash kills
##   what it touches), Invincibility runs its timer.
## The hero reads its dash parameters from here before every dash.

## Emitted when passives or charges change (HUD).
signal buffs_changed(passives: Dictionary, shockwave_charges: int)

var game: Game
var passives: Dictionary = {}  # PowerupType -> stacks
var shockwave_charges := 0
var _invincible_ticks := 0
var _focus_ticks := 0


func _ready() -> void:
	game = get_parent() as Game


func stacks(type: Constants.PowerupType) -> int:
	return passives.get(type, 0)


# --- dash parameters ---------------------------------------------------------------

func dash_cost() -> int:
	return maxi(Constants.DASH_COST - stacks(Constants.PowerupType.ARMOR) * Constants.REINFORCED_COST_PER_STACK, 2)


func dash_cooldown_ticks() -> int:
	return int(Constants.DASH_COOLDOWN_TICKS * (1.0 - stacks(Constants.PowerupType.ARMOR) * Constants.REINFORCED_COOLDOWN_PER_STACK))


func dash_ticks() -> int:
	return int(round(Constants.DASH_TICKS * (1.0 + stacks(Constants.PowerupType.SPEED) * Constants.AFTERBURNER_DISTANCE_PER_STACK)))


func launch_speed() -> float:
	return Constants.DASH_LAUNCH_SPEED * (1.0 + stacks(Constants.PowerupType.SUPER_FORCE) * Constants.IMPACT_LAUNCH_PER_STACK)


func focus_ticks() -> int:
	return stacks(Constants.PowerupType.SLOW_MOTION) * Constants.FOCUS_TICKS_PER_STACK


## Called by the Game when a dash starts. The dash is a shockwave dash while a
## charge is held; the charge is only spent when it actually hits something
## (see [method consume_shockwave]), so a charged dash into empty space is free.
func on_dash_started(dash_len_ticks: int) -> bool:
	var shockwave := shockwave_charges > 0
	if focus_ticks() > 0:
		_focus_ticks = mini(dash_len_ticks + focus_ticks(), Constants.FOCUS_MAX_TICKS)
		Game.time_scale = Constants.FOCUS_SCALE
	return shockwave


# --- pickups -----------------------------------------------------------------------

func collect(pickup: PowerupPickup) -> void:
	Telemetry.powerup_collected(pickup.type)
	var type := pickup.type
	match type:
		Constants.PowerupType.ENERGY:
			game.hero.energy = Constants.HERO_START_ENERGY
			Audio.play_sound("energy")
		Constants.PowerupType.MASS_DEATH:
			shockwave_charges = mini(shockwave_charges + 1, Constants.SHOCKWAVE_MAX_CHARGES)
			Audio.play_sound("pickup")
		Constants.PowerupType.INVINCIBILITY:
			game.hero.invincible = true
			_invincible_ticks = 0
			Audio.play_sound("invincible")
		_:
			passives[type] = mini(stacks(type) + 1, Constants.PASSIVE_MAX_STACKS)
			Audio.play_sound("armor" if type == Constants.PowerupType.ARMOR else "speed")
			game.hud.show_buff_text(Constants.PASSIVE_NAMES[type], passives[type])
	_emit()


## 1..0 fraction of invincibility remaining (0 when not invincible).
func invincible_left() -> float:
	if not game.hero.invincible:
		return 0.0
	return 1.0 - float(_invincible_ticks) / Constants.INVINCIBILITY_TICKS


func tick() -> void:
	if game.hero.invincible:
		_invincible_ticks += 1
		if _invincible_ticks > Constants.INVINCIBILITY_TICKS:
			game.hero.invincible = false
	if _focus_ticks > 0:
		_focus_ticks -= 1
		Game.time_scale = Constants.FOCUS_SCALE
	else:
		Game.time_scale = 1.0


## First shockwave contact in a dash: spend the charge.
func consume_shockwave() -> void:
	if shockwave_charges > 0:
		shockwave_charges -= 1
		_emit()


func _emit() -> void:
	buffs_changed.emit(passives, shockwave_charges)


func clear_effects() -> void:
	Game.time_scale = 1.0
	_focus_ticks = 0
