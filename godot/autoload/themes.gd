extends Node
## Theme registry and the active theme. Selection persists in Prefs.
## `Themes.current` is read by every drawer; `theme_changed` lets live scenes
## re-skin (the menu does; the game applies the theme at scene start).

signal theme_changed(theme: GameTheme)

const THEME_DIR := "res://assets/themes/"
const ORDER := ["neon", "fruit"]

var all: Dictionary = {}
var current: GameTheme


func _ready() -> void:
	for id in ORDER:
		var t: GameTheme = load(THEME_DIR + id + ".tres")
		if t:
			all[id] = t
	select(Prefs.theme if all.has(Prefs.theme) else ORDER[0], false)


func select(id: String, persist := true) -> void:
	if not all.has(id):
		push_warning("Themes: unknown theme %s" % id)
		return
	current = all[id]
	if persist:
		Prefs.theme = id
		Prefs.save()
	theme_changed.emit(current)


func next() -> void:
	var i := ORDER.find(current.id)
	select(ORDER[(i + 1) % ORDER.size()])
