extends Node
## Per-run gameplay telemetry for balancing. Records what happened during a run
## (kills, hits, energy over time, power-ups, difficulty at death, how much real
## time was spent frozen by hit-stop/slow-mo) and writes one JSON file per run to
## user://telemetry/. A summary is printed to the console at game over.
##
## Aggregate many runs with tools/analyze_runs.py.

const DIR := "user://telemetry"
const SAMPLE_TICKS := 60  # one sample per simulated second

var enabled := true
## "player" for real sessions; tests and bots set "test" / "bot" so the analyzer
## can keep them out of balance numbers.
var source := "player"

var _run: Dictionary = {}
var _active := false
var _tick := 0
var _real_start_ms := 0
var _real_frozen := 0.0
var _real_total := 0.0
var _last_hit_time := -1.0
var _hit_gaps: Array = []
var _last_wall_us := 0


func _ready() -> void:
	process_mode = Node.PROCESS_MODE_ALWAYS
	DirAccess.make_dir_recursive_absolute(ProjectSettings.globalize_path(DIR))


func _process(_delta: float) -> void:
	if not _active:
		return
	# Wall clock: `delta` is not a reliable function of Engine.time_scale.
	var now := Time.get_ticks_usec()
	var real := (now - _last_wall_us) / 1_000_000.0
	_last_wall_us = now
	_real_total += real
	if Engine.time_scale < 0.5:
		_real_frozen += real


# --- run lifecycle -------------------------------------------------------------

func run_started(stage: int) -> void:
	if not enabled:
		return
	_active = true
	_tick = 0
	_real_start_ms = Time.get_ticks_msec()
	_last_wall_us = Time.get_ticks_usec()
	_real_frozen = 0.0
	_real_total = 0.0
	_last_hit_time = -1.0
	_hit_gaps = []
	_run = {
		"version": 2,
		"source": source,
		"started_at": Time.get_datetime_string_from_system(),
		"stage": stage,
		"unlocks": {
			"split": Prefs.spawn_split_particles,
			"suicide": Prefs.spawn_suicide_particles,
			"invisible": Prefs.spawn_invisible_particles,
		},
		"kills": {"normal": 0, "split": 0, "suicide": 0, "invisible": 0},
		"hits": 0,
		"hits_by_type": {"normal": 0, "split": 0, "suicide": 0, "invisible": 0},
		"energy_gained": 0,
		"powerups_collected": {},
		"powerups_deployed": 0,
		"max_combo": 0,
		"max_multiplier": 1,
		"streaks": [],
		"blasts": 0,
		"death_saws": 0,
		"spawns": {"normal": 0, "split": 0, "suicide": 0, "invisible": 0},
		"spawn_log": [],  # [sim_sec, type, alive_after, distance_to_hero]
		"max_alive": 0,
		"max_mines_alive": 0,
		"dashes": 0,
		"dash_hits": 0,
		"dash_kills": 0,
		"perfect_hits": 0,
		"dash_brakes": 0,
		"dash_cancels": 0,
		"perfect_kills": 0,
		"overheats": 0,
		"saw_kills": {},  # saw index -> kills on that blade
		"samples": [],  # [sim_sec, energy, alive, score, time_scale]
		"events": [],   # notable moments with a sim-second stamp
	}


## Called once per physics tick by the Game.
func tick(energy: int, alive: int, score: int) -> void:
	if not _active:
		return
	_tick += 1
	if _tick % SAMPLE_TICKS == 0:
		_run["samples"].append([_tick / 60, energy, alive, score, snappedf(Engine.time_scale, 0.01)])


func run_ended(cause: String, score: int, highscore: int, spawner: Spawner) -> void:
	if not _active:
		return
	_active = false
	var sim_sec := _tick / 60.0
	var real_sec := (Time.get_ticks_msec() - _real_start_ms) / 1000.0
	_run["death_cause"] = cause
	_run["score"] = score
	_run["highscore"] = highscore
	_run["sim_seconds"] = snappedf(sim_sec, 0.1)
	_run["real_seconds"] = snappedf(real_sec, 0.1)
	_run["frozen_fraction"] = snappedf(_real_frozen / maxf(_real_total, 0.001), 0.001)
	_run["hits_per_sim_minute"] = snappedf(_run["hits"] / maxf(sim_sec / 60.0, 0.001), 0.1)
	_run["median_hit_gap_sec"] = _median(_hit_gaps)
	# Camping metric: share of saw kills on the single most-used blade.
	var sk: Dictionary = _run["saw_kills"]
	var total_sk := 0
	var top_sk := 0
	for v in sk.values():
		total_sk += v
		top_sk = maxi(top_sk, v)
	_run["top_saw_share"] = snappedf(float(top_sk) / maxf(total_sk, 1), 0.01)
	_run["saws_used"] = sk.size()
	_run["difficulty_at_death"] = {
		"normal_speed": spawner.normal_speed,
		"split_time": spawner.split_time,
		"death_saw_time": spawner.death_saw_time,
		"suicide_count": spawner.suicide_count,
	}
	_write()
	_print_summary()


# --- events --------------------------------------------------------------------

func spawn(type: Constants.ParticleType, alive_after: int, distance_to_hero: float, mines_alive := 0) -> void:
	if not _active:
		return
	var name := _type_name(type)
	_run["spawns"][name] += 1
	_run["spawn_log"].append([snappedf(_tick / 60.0, 0.1), name, alive_after, int(distance_to_hero)])
	_run["max_alive"] = maxi(_run["max_alive"], alive_after)
	_run["max_mines_alive"] = maxi(_run["max_mines_alive"], mines_alive)


func dash() -> void:
	if _active:
		_run["dashes"] += 1


func dash_hit() -> void:
	if _active:
		_run["dash_hits"] += 1


func dash_kill() -> void:
	if _active:
		_run["dash_kills"] += 1


func dash_interrupted(kind: String) -> void:
	if _active:
		var key := "dash_brakes" if kind == "brake" else "dash_cancels"
		_run[key] = _run.get(key, 0) + 1


## Alignment histogram of dash hits (0..1 in 0.1 buckets) for tuning the perfect cone.
func dash_alignment(a: float) -> void:
	if _active:
		var hist: Array = _run.get("alignment_hist", [0,0,0,0,0,0,0,0,0,0])
		hist[clampi(int(a * 10.0), 0, 9)] += 1
		_run["alignment_hist"] = hist


func perfect_hit() -> void:
	if _active:
		_run["perfect_hits"] += 1


func perfect_kill() -> void:
	if _active:
		_run["perfect_kills"] += 1


func overheat() -> void:
	if _active:
		_run["overheats"] += 1


func saw_kill(saw_index: int) -> void:
	if _active:
		var key := str(saw_index)
		_run["saw_kills"][key] = _run["saw_kills"].get(key, 0) + 1


func kill(type: Constants.ParticleType) -> void:
	if _active:
		_run["kills"][_type_name(type)] += 1


func hit(type: Constants.ParticleType, energy_after: int) -> void:
	if not _active:
		return
	_run["hits"] += 1
	_run["hits_by_type"][_type_name(type)] += 1
	var t := _tick / 60.0
	if _last_hit_time >= 0.0:
		_hit_gaps.append(t - _last_hit_time)
	_last_hit_time = t
	if energy_after <= Constants.HERO_LOW_ENERGY and energy_after + 1 > Constants.HERO_LOW_ENERGY:
		event("low_energy")


func energy_gained(amount: int) -> void:
	if _active:
		_run["energy_gained"] += amount


func powerup_collected(type: Constants.PowerupType) -> void:
	if not _active:
		return
	var key: String = Constants.PowerupType.keys()[type].to_lower()
	_run["powerups_collected"][key] = _run["powerups_collected"].get(key, 0) + 1


func buffs_at_death(passives: Dictionary, charges: int) -> void:
	if not _active:
		return
	var out := {}
	for k in passives:
		out[Constants.PowerupType.keys()[k].to_lower()] = passives[k]
	_run["buffs_at_death"] = out
	_run["shockwave_charges_at_death"] = charges


func powerup_deployed() -> void:
	if _active:
		_run["powerups_deployed"] += 1


func combo(streak: int) -> void:
	if _active and streak > _run["max_combo"]:
		_run["max_combo"] = streak


func multiplier(value: int) -> void:
	if _active and value > _run["max_multiplier"]:
		_run["max_multiplier"] = value


func streak(text: String) -> void:
	if _active:
		_run["streaks"].append([_tick / 60.0, text])


func blast() -> void:
	if _active:
		_run["blasts"] += 1


func death_saw() -> void:
	if _active:
		_run["death_saws"] += 1


func overheat_saw_thrown() -> void:
	if _active:
		_run["overheat_saws_thrown"] = _run.get("overheat_saws_thrown", 0) + 1


func event(name: String) -> void:
	if _active:
		_run["events"].append([snappedf(_tick / 60.0, 0.1), name])


# --- output ----------------------------------------------------------------------

func _write() -> void:
	var stamp := Time.get_datetime_string_from_system(false, true).replace(":", "-").replace(" ", "_")
	var path := "%s/run_%s_%d.json" % [DIR, stamp, Time.get_ticks_msec() % 1000]
	var f := FileAccess.open(path, FileAccess.WRITE)
	if f == null:
		push_warning("Telemetry: cannot write %s" % path)
		return
	f.store_string(JSON.stringify(_run, "  "))
	f.close()
	_run["_path"] = path


func _print_summary() -> void:
	var k: Dictionary = _run["kills"]
	var total_kills: int = k["normal"] + k["split"] + k["suicide"] + k["invisible"]
	print("[Telemetry] run: %s | score %d | %.0fs sim / %.0fs real | frozen %.0f%%" % [
		_run["death_cause"], _run["score"], _run["sim_seconds"], _run["real_seconds"], _run["frozen_fraction"] * 100.0])
	print("[Telemetry]   kills %d (n%d s%d x%d i%d) | hits %d (%.1f/min, median gap %.2fs) | energy +%d | max combo %d" % [
		total_kills, k["normal"], k["split"], k["suicide"], k["invisible"], _run["hits"], _run["hits_per_sim_minute"],
		_run["median_hit_gap_sec"], _run["energy_gained"], _run["max_combo"]])
	print("[Telemetry]   dashes %d (hits %d, dash kills %d, perfect %d/%d) | overheats %d | saws used %d, top saw %.0f%% of kills" % [
		_run["dashes"], _run["dash_hits"], _run["dash_kills"], _run["perfect_kills"], _run["perfect_hits"], _run["overheats"], _run["saws_used"], _run["top_saw_share"] * 100.0])
	var sp: Dictionary = _run["spawns"]
	print("[Telemetry]   spawns n%d s%d x%d i%d | max alive %d | max mines %d | blasts %d | death saws %d" % [
		sp["normal"], sp["split"], sp["suicide"], sp["invisible"], _run["max_alive"], _run["max_mines_alive"], _run["blasts"], _run["death_saws"]])
	print("[Telemetry]   -> %s" % ProjectSettings.globalize_path(_run.get("_path", "")))


func last_run() -> Dictionary:
	return _run


func _type_name(type: Constants.ParticleType) -> String:
	return Constants.ParticleType.keys()[type].to_lower()


func _median(values: Array) -> float:
	if values.is_empty():
		return 0.0
	var sorted := values.duplicate()
	sorted.sort()
	return snappedf(sorted[sorted.size() / 2], 0.01)
