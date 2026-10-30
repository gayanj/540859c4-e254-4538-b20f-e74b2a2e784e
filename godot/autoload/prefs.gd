extends Node
## Persistent player progress. Port of utils/GamePreferences.java.
##
## The original game persisted *progression* (which enemy types are unlocked,
## how many particles the arena starts with) across sessions, not just the high
## score. That is preserved here: a returning player picks up where they left off.

const SAVE_PATH := "user://particlebrawlprefs.cfg"
const SECTION := "progress"

var first_tutorial_completed := false
var second_tutorial_completed := false
var spawn_split_particles := false
var spawn_suicide_particles := false
var spawn_invisible_particles := false
var first_stage_cleared := false
var second_stage_cleared := false
var third_stage_cleared := false
## Number of normal particles alive at the start of a run.
var stage := 0
var highscore := 0
var normal_particles_destroyed := 0
var split_particles_destroyed := 0
var suicide_particles_destroyed := 0
var invisible_particles_destroyed := 0
## Achievement keys (see Constants.KILL_STREAKS) that were unlocked.
var achievements_unlocked: Dictionary = {}
var theme := "neon"

var _config := ConfigFile.new()
var _save_task := -1


func _ready() -> void:
	load_prefs()


func _notification(what: int) -> void:
	if what == NOTIFICATION_WM_CLOSE_REQUEST or what == NOTIFICATION_APPLICATION_PAUSED or what == NOTIFICATION_PREDELETE:
		_finish_pending_save()


func load_prefs() -> void:
	var err := _config.load(SAVE_PATH)
	if err != OK and err != ERR_FILE_NOT_FOUND:
		push_warning("Prefs: could not load %s (%s)" % [SAVE_PATH, error_string(err)])
	first_tutorial_completed = _config.get_value(SECTION, "first_tutorial_completed", false)
	second_tutorial_completed = _config.get_value(SECTION, "second_tutorial_completed", false)
	spawn_split_particles = _config.get_value(SECTION, "spawn_split_particles", false)
	spawn_suicide_particles = _config.get_value(SECTION, "spawn_suicide_particles", false)
	spawn_invisible_particles = _config.get_value(SECTION, "spawn_invisible_particles", false)
	first_stage_cleared = _config.get_value(SECTION, "first_stage_cleared", false)
	second_stage_cleared = _config.get_value(SECTION, "second_stage_cleared", false)
	third_stage_cleared = _config.get_value(SECTION, "third_stage_cleared", false)
	stage = _config.get_value(SECTION, "stage", 0)
	highscore = _config.get_value(SECTION, "highscore", 0)
	normal_particles_destroyed = _config.get_value(SECTION, "normal_particles_destroyed", 0)
	split_particles_destroyed = _config.get_value(SECTION, "split_particles_destroyed", 0)
	suicide_particles_destroyed = _config.get_value(SECTION, "suicide_particles_destroyed", 0)
	invisible_particles_destroyed = _config.get_value(SECTION, "invisible_particles_destroyed", 0)
	achievements_unlocked = _config.get_value(SECTION, "achievements_unlocked", {})
	theme = _config.get_value(SECTION, "theme", "neon")


## Writes progress. The disk write runs on a worker thread so it never lands
## on a gameplay frame (the death frame used to pay ~30 ms for it); pass
## [param sync] when the process is about to exit.
func save(sync := false) -> void:
	_finish_pending_save()
	_config.set_value(SECTION, "first_tutorial_completed", first_tutorial_completed)
	_config.set_value(SECTION, "second_tutorial_completed", second_tutorial_completed)
	_config.set_value(SECTION, "spawn_split_particles", spawn_split_particles)
	_config.set_value(SECTION, "spawn_suicide_particles", spawn_suicide_particles)
	_config.set_value(SECTION, "spawn_invisible_particles", spawn_invisible_particles)
	_config.set_value(SECTION, "first_stage_cleared", first_stage_cleared)
	_config.set_value(SECTION, "second_stage_cleared", second_stage_cleared)
	_config.set_value(SECTION, "third_stage_cleared", third_stage_cleared)
	_config.set_value(SECTION, "stage", stage)
	_config.set_value(SECTION, "highscore", highscore)
	_config.set_value(SECTION, "normal_particles_destroyed", normal_particles_destroyed)
	_config.set_value(SECTION, "split_particles_destroyed", split_particles_destroyed)
	_config.set_value(SECTION, "suicide_particles_destroyed", suicide_particles_destroyed)
	_config.set_value(SECTION, "invisible_particles_destroyed", invisible_particles_destroyed)
	_config.set_value(SECTION, "achievements_unlocked", achievements_unlocked)
	_config.set_value(SECTION, "theme", theme)
	if sync:
		_write()
	else:
		_save_task = WorkerThreadPool.add_task(_write)


func _write() -> void:
	var err := _config.save(SAVE_PATH)
	if err != OK:
		push_warning("Prefs: could not save %s (%s)" % [SAVE_PATH, error_string(err)])


func _finish_pending_save() -> void:
	if _save_task != -1:
		WorkerThreadPool.wait_for_task_completion(_save_task)
		_save_task = -1


func is_achievement_unlocked(key: String) -> bool:
	return achievements_unlocked.get(key, false)


func unlock_achievement(key: String) -> void:
	achievements_unlocked[key] = true


## Wipe all progress (useful while testing; not exposed in the UI yet).
func reset() -> void:
	_finish_pending_save()
	_config.clear()
	_config.save(SAVE_PATH)
	load_prefs()
