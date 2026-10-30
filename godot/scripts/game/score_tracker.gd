class_name ScoreTracker
extends Node
## Score, combo multiplier and kill-streak announcements.
##
## Combo rules:
## - Kills within [constant Constants.COMBO_WINDOW_TICKS] of each other chain.
## - Multiplier = (1 + streak / 5) * distinct blades used in this chain (capped).
##   Feeding one blade from one corner gives x1, x2, x3...; spreading the same
##   kills over four blades gives four times that. Movement is the score.
## - Each kill is worth BASE_POINTS * multiplier immediately (no end payout).
## - Every 10th kill in a chain announces a kill-streak and unlocks its achievement.

signal score_changed(score: int)
## Emitted when the multiplier, streak or blade count change.
signal combo_changed(streak: int, multiplier: int, blades: int)
signal kill_streak(text: String)
signal kill_streak_ended

const BASE_POINTS := 10
const MAX_BLADE_BONUS := 4

var score := 0:
	set(value):
		if value != score:
			score = value
			score_changed.emit(score)
var streak := 0
var multiplier := 1
var blades_used: Dictionary = {}

var _combo_armed := false
var _combo_ticks := 0
var _streak_text_ticks := -1


## 1.0 right after a kill, 0.0 when the chain is about to break.
func combo_window_left() -> float:
	if not _combo_armed:
		return 0.0
	return 1.0 - float(_combo_ticks) / Constants.COMBO_WINDOW_TICKS


## [param blade_id] identifies the saw that got the kill (-1 for blasts etc.).
func add_kill(blade_id := -1, dash_kill := false, perfect := false) -> int:
	_combo_armed = true
	_combo_ticks = 0
	streak += 1
	if blade_id >= 0:
		blades_used[blade_id] = true
	if dash_kill:
		blades_used["dash"] = true  # a dash kill counts as one extra blade
	_update_multiplier()
	var points := BASE_POINTS * multiplier
	if perfect:
		points = int(points * Constants.PERFECT_POINTS_MULT)
	score += points
	_announce_streak(streak)
	return points


func tick() -> void:
	if _combo_armed:
		_combo_ticks += 1
		if _combo_ticks >= Constants.COMBO_WINDOW_TICKS:
			_end_combo()
	if _streak_text_ticks >= 0:
		_streak_text_ticks += 1
		if _streak_text_ticks > Constants.KILL_STREAK_TEXT_TICKS:
			_streak_text_ticks = -1
			kill_streak_ended.emit()


func blade_bonus() -> int:
	return clampi(blades_used.size(), 1, MAX_BLADE_BONUS)


func _update_multiplier() -> void:
	multiplier = (1 + streak / 5) * blade_bonus()
	combo_changed.emit(streak, multiplier, blades_used.size())


func _end_combo() -> void:
	streak = 0
	multiplier = 1
	blades_used.clear()
	_combo_armed = false
	_combo_ticks = 0
	combo_changed.emit(0, 1, 0)


func _announce_streak(value: int) -> void:
	if value <= 0 or value % 10 != 0:
		return
	var level := (value / 10) % 10
	if level == 0:
		level = 10  # 100, 200, ...: "BEYOND GODLIKE!"
	var info: Dictionary = Constants.KILL_STREAKS[level]
	Audio.play_sound(info["sound"])
	_streak_text_ticks = 0
	kill_streak.emit(info["text"])
	var key: String = info["achievement"]
	if not Prefs.is_achievement_unlocked(key):
		Prefs.unlock_achievement(key)
		Services.unlock_achievement(key)
