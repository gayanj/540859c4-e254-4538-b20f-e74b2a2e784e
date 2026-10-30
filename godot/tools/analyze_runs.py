#!/usr/bin/env python3
"""Aggregate Particle Assassin telemetry runs.

    python3 godot/tools/analyze_runs.py            # all runs in the default user dir
    python3 godot/tools/analyze_runs.py --last 10  # most recent N
    python3 godot/tools/analyze_runs.py path/to/dir_or_file.json ...

Prints per-run lines, then aggregates: death causes, run length, hit rate,
energy economy (gained vs lost), kills by type, power-up usage, and an
average energy-over-time curve so you can see *when* runs fall apart.
"""
from __future__ import annotations

import json
import statistics
import sys
from pathlib import Path

DEFAULT_DIRS = [
    Path.home() / "Library/Application Support/Godot/app_userdata/Particle Assassin/telemetry",
    Path.home() / ".local/share/godot/app_userdata/Particle Assassin/telemetry",
    Path.home() / "AppData/Roaming/Godot/app_userdata/Particle Assassin/telemetry",
]


def load(paths: list[Path]) -> list[dict]:
    runs = []
    for p in paths:
        files = sorted(p.glob("run_*.json")) if p.is_dir() else [p]
        for f in files:
            try:
                runs.append(json.loads(f.read_text()) | {"_file": f.name})
            except (OSError, json.JSONDecodeError) as e:  # noqa: PERF203
                print(f"skip {f}: {e}", file=sys.stderr)
    return runs


def med(xs):
    return statistics.median(xs) if xs else 0.0


def main(argv: list[str]) -> None:
    last = None
    source = "player"
    paths: list[Path] = []
    it = iter(argv)
    for a in it:
        if a == "--last":
            last = int(next(it))
        elif a == "--source":  # player (default) | bot | test | all
            source = next(it)
        else:
            paths.append(Path(a))
    if not paths:
        paths = [d for d in DEFAULT_DIRS if d.exists()]
    runs = load(paths)
    if source != "all":
        runs = [r for r in runs if r.get("source", "player") == source]
    runs.sort(key=lambda r: r.get("started_at", ""))
    if last:
        runs = runs[-last:]
    if not runs:
        print("no runs found")
        return

    print(f"{'started':19} {'cause':12} {'score':>6} {'sim s':>6} {'hits/min':>8} {'mult':>5} {'saws':>4} {'top%':>4} kills")
    for r in runs:
        k = r["kills"]
        print(
            f"{r['started_at']:19} {r['death_cause']:12} {r['score']:6d} {r['sim_seconds']:6.0f} "
            f"{r['hits_per_sim_minute']:8.1f} x{r.get('max_multiplier', 0):<4d} {r.get('saws_used', 0):4d} {r.get('top_saw_share', 0) * 100:4.0f} "
            f"n{k['normal']} s{k['split']} x{k['suicide']} i{k['invisible']}"
        )

    n = len(runs)
    print(f"\n== {n} runs ==")
    causes: dict[str, int] = {}
    for r in runs:
        causes[r["death_cause"]] = causes.get(r["death_cause"], 0) + 1
    print("death causes:  " + ", ".join(f"{c} {v} ({v / n:.0%})" for c, v in sorted(causes.items(), key=lambda x: -x[1])))
    print(f"run length:    median {med([r['sim_seconds'] for r in runs]):.0f}s  (min {min(r['sim_seconds'] for r in runs):.0f}, max {max(r['sim_seconds'] for r in runs):.0f})")
    print(f"score:         median {med([r['score'] for r in runs]):.0f}  best {max(r['score'] for r in runs)}  | max multiplier median x{med([r.get('max_multiplier', 0) for r in runs]):.0f}, best x{max(r.get('max_multiplier', 0) for r in runs)}")
    print(f"hits/min:      median {med([r['hits_per_sim_minute'] for r in runs]):.1f}   median hit gap {med([r['median_hit_gap_sec'] for r in runs]):.2f}s")
    print(f"frozen time:   median {med([r['frozen_fraction'] for r in runs]) * 100:.0f}% of real time")
    gained = sum(r["energy_gained"] for r in runs)
    lost = sum(r["hits"] for r in runs)
    print(f"energy:        +{gained} gained vs -{lost} lost over all runs  (ratio {gained / max(lost, 1):.2f})")
    totals = {t: sum(r["kills"][t] for r in runs) for t in ("normal", "split", "suicide", "invisible")}
    print("kills by type: " + ", ".join(f"{t} {v}" for t, v in totals.items()))
    hits = {t: sum(r["hits_by_type"][t] for r in runs) for t in ("normal", "split", "suicide", "invisible")}
    print("hits by type:  " + ", ".join(f"{t} {v}" for t, v in hits.items()))
    sp_runs = [r for r in runs if "spawns" in r]
    if sp_runs:
        sp = {t: sum(r["spawns"][t] for r in sp_runs) for t in ("normal", "split", "suicide", "invisible")}
        print("spawns:        " + ", ".join(f"{t} {v}" for t, v in sp.items())
              + f"  | max alive median {med([r['max_alive'] for r in sp_runs]):.0f}, max mines median {med([r['max_mines_alive'] for r in sp_runs]):.0f}")
        near = [e for r in sp_runs for e in r["spawn_log"] if e[3] < 320]
        total = sum(len(r["spawn_log"]) for r in sp_runs)
        print(f"spawns <320px from hero: {len(near)} of {total}")
    dr = [r for r in runs if "dashes" in r]
    if dr:
        print(f"dash:          {sum(r['dashes'] for r in dr)} dashes, {sum(r['dash_hits'] for r in dr)} hits, {sum(r['dash_kills'] for r in dr)} dash kills, perfect {sum(r.get('perfect_kills', 0) for r in dr)}/{sum(r.get('perfect_hits', 0) for r in dr)}, brakes {sum(r.get('dash_brakes', 0) for r in dr)}, cancels {sum(r.get('dash_cancels', 0) for r in dr)}"
              f"  | overheats {sum(r['overheats'] for r in dr)}  | saws used median {med([r['saws_used'] for r in dr]):.0f}, top-saw share median {med([r['top_saw_share'] for r in dr]) * 100:.0f}%")
    pu: dict[str, int] = {}
    for r in runs:
        for k_, v in r["powerups_collected"].items():
            pu[k_] = pu.get(k_, 0) + v
    deployed = sum(r["powerups_deployed"] for r in runs)
    print(f"power-ups:     collected {sum(pu.values())} ({', '.join(f'{k_} {v}' for k_, v in sorted(pu.items()))}), deployed {deployed}")

    # Average energy curve (per simulated 10 s bucket) across runs still alive at that time.
    buckets: dict[int, list[int]] = {}
    for r in runs:
        for sec, energy, _alive, _score, _ts in r["samples"]:
            buckets.setdefault(int(sec) // 10 * 10, []).append(energy)
    if buckets:
        print("\nenergy over time (avg of runs alive at that point):")
        for b in sorted(buckets):
            xs = buckets[b]
            bar = "#" * int(statistics.mean(xs) / 2)
            print(f"  {b:4d}s  {statistics.mean(xs):5.1f}  n={len(xs):<3d} {bar}")


if __name__ == "__main__":
    main(sys.argv[1:])
