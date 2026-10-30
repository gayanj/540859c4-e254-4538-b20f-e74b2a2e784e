class_name Saw
extends StaticBody2D
## Static spinning saw blade lining the arena. Port of sprites/Saw.java, plus
## heat: kills in quick succession on the same blade overheat it, and an
## overheated blade is dull for a few seconds (particles bounce off; it is still
## lethal to the hero). This is the anti-camping rule.

signal overheated(saw: Saw)

## 1.0 for edge saws, 0.7 / 0.6 for the smaller corner saws.
@export var size_scale := 1.0
## Set by the Arena: where a launched copy of this blade flies.
var inward := Vector2.ZERO
## Adjacent blades (set by the Arena); kills here warm them too.
var neighbours: Array[Saw] = []

var heat := 0.0
var dull := false
var kills := 0
var _dull_left := 0.0
var _flash := 0.0

@onready var _blade: SawBlade = $Blade


func _ready() -> void:
	add_to_group("saw")
	collision_layer = Constants.LAYER_SAW
	collision_mask = Constants.LAYER_PARTICLE | Constants.LAYER_HERO | Constants.LAYER_HERO_INVINCIBLE
	scale = Vector2.ONE * size_scale
	_blade.juicer = Themes.current.saw_style == GameTheme.SawStyle.JUICER
	_blade.rim_color = Themes.current.saw


func _process(delta: float) -> void:
	var d := delta * Game.time_scale
	if dull:
		_dull_left -= d
		if _dull_left <= 0.0:
			dull = false
			heat = 0.0
	elif heat > 0.0:
		heat = maxf(heat - Constants.SAW_HEAT_DECAY_PER_SEC * d, 0.0)
	_flash = maxf(_flash - delta * 3.0, 0.0)
	_blade.heat = 0.0 if dull else heat / Constants.SAW_OVERHEAT_AT
	_blade.dull = dull
	_blade.cooldown = _dull_left / Constants.SAW_OVERHEAT_SECONDS if dull else 0.0
	_blade.flash = _flash
	_blade.spin_speed = 1.5 if dull else 6.0


## Called by the Game when a particle dies on this blade.
func register_kill() -> void:
	kills += 1
	add_heat(Constants.SAW_HEAT_PER_KILL)
	for n in neighbours:
		n.add_heat(Constants.SAW_NEIGHBOUR_HEAT)


func add_heat(amount: float) -> void:
	if dull:
		return
	heat += amount
	if heat >= Constants.SAW_OVERHEAT_AT:
		dull = true
		_dull_left = Constants.SAW_OVERHEAT_SECONDS
		_flash = 1.0
		overheated.emit(self)
