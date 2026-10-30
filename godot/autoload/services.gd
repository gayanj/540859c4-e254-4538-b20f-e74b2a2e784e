extends Node
## Platform services: achievements, leaderboards, store rating, ads.
## Port of utils/IActivityRequestHandler.java.
##
## This is a stub. On desktop it just logs. When the Android export is wired up,
## connect these to a Play Games Services v2 / AdMob plugin (see README).

signal achievement_unlocked(key: String)
signal score_submitted(score: int)

const STORE_URL := "https://play.google.com/store/apps/details?id=com.particle.assassin.android"


func sign_in() -> void:
	print("[Services] sign_in (stub)")


func sign_out() -> void:
	print("[Services] sign_out (stub)")


func unlock_achievement(key: String) -> void:
	print("[Services] unlock_achievement %s (stub)" % key)
	achievement_unlocked.emit(key)


func submit_score(score: int) -> void:
	print("[Services] submit_score %d (stub)" % score)
	score_submitted.emit(score)


func show_achievements() -> void:
	print("[Services] show_achievements (stub)")


func show_leaderboard() -> void:
	print("[Services] show_leaderboard (stub)")


func rate_game() -> void:
	OS.shell_open(STORE_URL)


func show_banner_ad(_visible: bool) -> void:
	pass


func show_interstitial_ad() -> void:
	pass
