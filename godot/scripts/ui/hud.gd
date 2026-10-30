class_name HUD
extends CanvasLayer
## In-game overlay: energy dial (top left), score (top centre) with the live
## combo beneath it, kill-streak banner, power button (bottom left), tutorial
## card and the game-over panel.

signal dash_pressed
signal retry_pressed
signal tutorial_dismissed

const COMBO_BAR_WIDTH := 220.0

@onready var _energy_dial: EnergyDial = %EnergyDial
@onready var _score_value: Label = %ScoreValue
@onready var _combo: Control = %Combo
@onready var _combo_value: Label = %ComboValue
@onready var _combo_bar: ColorRect = %ComboBar
@onready var _combo_blades: Label = %ComboBlades
var _points_settings := LabelSettings.new()
@onready var _toast: Label = %Toast
var _toast_tween: Tween
@onready var _dash_button: DashButton = %DashButton
@onready var _buff_bar: BuffBar = %BuffBar
@onready var _game_over: Control = %GameOver
@onready var _best_label: Label = %BestLabel
@onready var _tutorial: Control = %Tutorial

var _score_tween: Tween


func _ready() -> void:
	process_mode = Node.PROCESS_MODE_ALWAYS
	_combo.visible = false
	_toast.visible = false
	_game_over.visible = false
	_tutorial.visible = false
	_energy_dial.max_value = Constants.HERO_START_ENERGY
	_points_settings.font = preload("res://assets/fonts/BebasNeue.otf")
	_points_settings.font_size = 34
	_points_settings.shadow_size = 6
	_points_settings.shadow_color = Color(0, 0, 0, 0.7)
	_dash_button.pressed.connect(dash_pressed.emit)
	%RetryButton.pressed.connect(retry_pressed.emit)
	%TrophyButton.pressed.connect(Services.show_achievements)
	%LeaderboardButton.pressed.connect(Services.show_leaderboard)
	%RateButton.pressed.connect(Services.rate_game)
	%TutorialCard.pressed.connect(_on_tutorial_pressed)


func set_score(value: int) -> void:
	_score_value.text = str(value)
	# Small pop so score gains register in peripheral vision.
	if _score_tween:
		_score_tween.kill()
	_score_value.scale = Vector2.ONE * 1.12
	_score_tween = create_tween()
	_score_tween.tween_property(_score_value, "scale", Vector2.ONE, 0.15).set_ease(Tween.EASE_OUT)


func set_energy(value: int) -> void:
	_energy_dial.value = value


func blink_energy() -> void:
	_energy_dial.blink()


func set_combo(streak: int, multiplier: int, blades: int) -> void:
	_combo.visible = streak > 0
	_combo_value.text = "x%d" % multiplier
	_combo_blades.text = "%d BLADE%s" % [maxi(blades, 1), "" if blades <= 1 else "S"]
	_combo_blades.modulate = Color(1.6, 1.3, 0.4) if blades >= 2 else Color(1, 1, 1, 0.6)


## Floating "+N" at a kill, scaled with the multiplier so big combos look big.
func show_points(world_pos: Vector2, points: int, multiplier: int, dash_kill := false, perfect := false) -> void:
	var label := Label.new()
	label.text = "+%d" % points
	label.label_settings = _points_settings
	label.position = world_pos + Vector2(-60, -70)
	label.size = Vector2(120, 40)
	label.horizontal_alignment = HORIZONTAL_ALIGNMENT_CENTER
	label.pivot_offset = Vector2(60, 20)
	label.scale = Vector2.ONE * clampf(0.65 + multiplier * 0.02, 0.65, 1.4)
	label.modulate = Color(1.6, 1.3, 0.4) if multiplier > 1 else Color(1.0, 1.0, 1.0, 0.8)
	if perfect:
		label.text = "PERFECT +%d" % points
		label.modulate = Color(2.4, 2.4, 2.6)
		label.scale *= 1.2
	%Points.add_child(label)
	var tw := create_tween().set_parallel(true)
	tw.tween_property(label, "position:y", label.position.y - 50.0, 0.45).set_ease(Tween.EASE_OUT)
	tw.tween_property(label, "modulate:a", 0.0, 0.3).set_delay(0.15)
	tw.chain().tween_callback(label.queue_free)


## Fraction (0..1) of the combo window remaining; drives the draining bar.
func set_combo_window(frac: float) -> void:
	_combo_bar.size.x = COMBO_BAR_WIDTH * clampf(frac, 0.0, 1.0)


func set_dash(cooldown_left: float, charges: int, can_dash: bool, invincible_left := 0.0) -> void:
	_dash_button.set_state(cooldown_left, charges, can_dash, invincible_left)


func set_buffs(passives: Dictionary, _charges: int) -> void:
	_buff_bar.set_passives(passives)


## Passive picked up: toast under the score ("AFTERBURNER II").
func show_buff_text(name: String, stacks: int) -> void:
	show_toast("%s %s" % [name, "I".repeat(stacks)], Color(0.5, 1.9, 2.3))


## One-line announcement in the slot under the score / combo block. Kill streaks
## and buff pickups share it; the voice line carries the moment, the text just
## confirms it without covering the arena.
func show_toast(text: String, color := Color(1.6, 0.42, 0.38)) -> void:
	_toast.text = text
	_toast.modulate = Color(color.r, color.g, color.b, 0.0)
	_toast.visible = true
	_toast.scale = Vector2(0.85, 0.85)
	if _toast_tween:
		_toast_tween.kill()
	_toast_tween = create_tween()
	_toast_tween.set_parallel(true)
	_toast_tween.tween_property(_toast, "modulate:a", 1.0, 0.12)
	_toast_tween.tween_property(_toast, "scale", Vector2.ONE, 0.22).set_trans(Tween.TRANS_BACK).set_ease(Tween.EASE_OUT)
	_toast_tween.chain().tween_property(_toast, "modulate:a", 0.0, 0.35).set_delay(1.1)
	_toast_tween.chain().tween_callback(func(): _toast.visible = false)


func show_game_over(best: int) -> void:
	_toast.visible = false
	_combo.visible = false
	_best_label.text = "BEST %d" % best
	_game_over.visible = true


func show_tutorial() -> void:
	_tutorial.visible = true


func tutorial_visible() -> bool:
	return _tutorial.visible


func _on_tutorial_pressed() -> void:
	_tutorial.visible = false
	tutorial_dismissed.emit()
