class_name BuffBar
extends Control
## Passive dash buffs, tucked into the dead pocket behind the top-right saw arc.
## Each passive has a fixed slot; stacks are drawn as three arc segments around
## the ring so a chip stays compact.

const R := 36.0
## Slots along the pocket's diagonal, chosen for clearance from the blades.
const SLOTS: Array[Vector2] = [Vector2(880, 36), Vector2(1016, 52), Vector2(948, 124), Vector2(1028, 192)]
const ORDER: Array = [
	Constants.PowerupType.SPEED, Constants.PowerupType.ARMOR,
	Constants.PowerupType.SUPER_FORCE, Constants.PowerupType.SLOW_MOTION,
]

var _passives: Dictionary = {}
var _icons: Dictionary = {}


func set_passives(passives: Dictionary) -> void:
	_passives = passives.duplicate()
	for i in ORDER.size():
		var t: Constants.PowerupType = ORDER[i]
		if not _icons.has(t):
			var tr := TextureRect.new()
			tr.texture = load(Constants.POWERUP_ICONS[t])
			tr.expand_mode = TextureRect.EXPAND_IGNORE_SIZE
			tr.stretch_mode = TextureRect.STRETCH_KEEP_ASPECT_CENTERED
			tr.mouse_filter = Control.MOUSE_FILTER_IGNORE
			add_child(tr)
			_icons[t] = tr
		var tr: TextureRect = _icons[t]
		var n: int = _passives.get(t, 0)
		tr.visible = n > 0
		tr.size = Vector2(44, 44)
		tr.position = SLOTS[i] - tr.size / 2.0
	queue_redraw()


func _draw() -> void:
	for i in ORDER.size():
		var n: int = _passives.get(ORDER[i], 0)
		if n <= 0:
			continue
		var c := SLOTS[i]
		draw_circle(c, R + 6.0, Color(0.0, 0.0, 0.0, 0.5))
		draw_circle(c, R, Color(0.06, 0.05, 0.09, 0.9))
		draw_arc(c, R, 0.0, TAU, 48, Color(0.35, 1.7, 2.1, 0.35), 3.0, true)
		# Stack segments: three arcs around the ring, lit per stack.
		var seg := TAU / Constants.PASSIVE_MAX_STACKS
		for k in Constants.PASSIVE_MAX_STACKS:
			var a0 := -PI / 2.0 + k * seg + 0.12
			var on := k < n
			draw_arc(c, R, a0, a0 + seg - 0.24, 16, Color(0.35, 1.7, 2.1, 1.0) if on else Color(1, 1, 1, 0.12), 5.0, true)
