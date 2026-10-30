# Particle Assassin — Godot 4 port

A port of the 2015 LibGDX game in `../platformRider/` to Godot 4.7 (GDScript).
Same art, music, sounds and rules; new engine.

## Run it

```bash
# Editor (open the project, press F5)
godot --path godot --editor

# Straight to the game
godot --path godot
```

Desktop controls: WASD / arrow keys / gamepad stick to move, Space (or gamepad A)
to fire the stored power-up, mouse works as a touch for the joystick and buttons.
On a phone: the joystick is bottom-right, the power button bottom-left.

## Verification

```bash
godot --headless --import --path godot                # imports assets, compiles scripts
godot --headless --path godot tests/smoke_test.tscn   # drives a full run; exit 1 on failure
godot --path godot tests/screenshots.tscn             # writes menu/game/game_over PNGs to user://screenshots
godot --path godot tests/fx_gallery.tscn [theme] [exhibit] [shot_after_s]   # stage each effect in isolation (on demand)
```

The smoke test steers the hero, kills particles on saws, builds a combo, picks up
every power-up, detonates a suicide particle, cycles an invisible particle, launches
a death saw and triggers game over. It resets the save file afterwards.

## Layout

| Path | What |
| --- | --- |
| `project.godot` | 1080x1920 portrait, `canvas_items` stretch, zero gravity, input actions, physics layer names |
| `autoload/constants.gd` | Every tunable from `GameConstants.java`, converted to px/s and 60 Hz ticks |
| `autoload/prefs.gd` | Persistent progression (`GamePreferences.java`) in `user://particlebrawlprefs.cfg` |
| `autoload/audio.gd` | Music / SFX / low-energy alert (`AudioManager.java`) |
| `autoload/services.gd` | Achievements, leaderboard, rate, ads — stubs to wire to a Play Games / AdMob plugin |
| `scenes/menu/` | Title screen with drifting particles bursting on the play saw |
| `scenes/game/game.tscn` | The arena. `game.gd` orchestrates; the old 1,300-line controller is split into: |
| `scripts/game/arena.gd` | Builds the saw ring from the original placement formulas |
| `scripts/game/spawner.gd` | When enemies, pickups and death saws appear; difficulty ramp |
| `scripts/game/powerup_manager.gd` | Stored/active power-ups and their effects |
| `scripts/game/score_tracker.gd` | Score, combo window, kill-streak announcements + achievements |
| `scenes/entities/` | `hero`, `particle` (all 4 enemy types), `saw`, `death_saw`, `play_button_saw`, `powerup_pickup`, `trail` |
| `scripts/entities/one_shot_animation.gd` | Death bursts and the suicide explosion |
| `scenes/ui/` | `hud` (score top-centre, live combo + window bar, streak banner, game over, tutorial card), `power_button` (neon ring that doubles as the stored power-up readout: icon, x N, radial timer) and `touch_joystick` |
| `scripts/ui/energy_dial.gd` | Energy as a ring around a lightning bolt, top left |
| `assets/atlases/*.png + *.txt` | The original LibGDX texture sheets, untouched |
| `assets/atlases/regions/*.tres` | Generated `AtlasTexture` per sprite |
| `assets/animations/*.tres` | Generated `SpriteFrames` per animation sheet (40 fps, trimmed frames re-padded via `margin`) |
| `tools/convert_atlases.py` | Regenerates the two folders above from the `.txt` atlases |
| `tests/` | Headless smoke test and screenshot capture |

## How the original maps onto Godot

- **Units.** Box2D used 100 px per metre, so every speed is `m/s * 100`. The original
  timers were frame counters, kept as physics ticks (Godot steps physics at 60 Hz).
- **Y axis.** LibGDX is y-up, Godot is y-down; `Constants.flip_y()` converts layout
  positions copied from the Java code.
- **Collision.** Box2D category/mask bits became physics layers 1–8 with the same
  meaning. Contact handling moved from one `ContactListener` into the entities:
  `Hero` reacts to particles/saws/deadly ghosts, `Particle` reacts to saws, pickups
  are `Area2D`s. Signals (`killed`, `exploded`, `wants_split`, `died`, `collected`)
  replace the removal lists. They are emitted deferred because Godot forbids
  creating or freeing bodies inside a physics callback.
- **Particle effects.** The `.p` files spawned a shrinking tinted `normalparticle.png`
  burst every frame; `trail.gd` builds an equivalent continuous `GPUParticles2D`
  (random-tint ramp for the hero's multi-emitter effect).
- **Slow motion.** The original stepped Box2D with `dt * 0.3`; here `Game.time_scale`
  scales enemy/death-saw velocity and saw animation speed, hero input is unaffected
  (matching the original's `1/scale` compensation).
- **Text.** `BebasNeue.otf` is loaded directly; the `|`-glyph energy fonts became a
  `ColorRect` bar with the same colour thresholds.

## Visual style (neon pass)

The 2015 flat-design look was replaced with a Nova Drift / Geometry Wars style:
dark field, procedural honeycomb, HDR colours (> 1.0) picked up by a
`WorldEnvironment` glow pass, additive trails and particle bursts.

| Piece | Where |
| --- | --- |
| Glow + clear colour + honeycomb | `scenes/fx/world_fx.tscn` (`world_fx.gd`, `hex_grid.gd`) — instanced in menu and game |
| Entity bodies | `scripts/fx/glow_core.gd` — disc + halo + rim, optional pulse |
| Motion ribbons | `scripts/fx/ribbon.gd` — `Line2D` fed by the parent's recent positions |
| Sparkle trails | `scenes/entities/trail.tscn` — `GPUParticles2D`, additive |
| Saw blades | `scripts/fx/saw_blade.gd` — procedural gear; radius/teeth/colour per scene |
| Deaths / explosion | `scripts/fx/neon_burst.gd` — ring flash + radial sparks |
| Icon buttons | `scripts/fx/neon_disc.gd` backing + `assets/shaders/silhouette.gdshader` turning the dark atlas icons into glowing silhouettes |
| Spring grid | `scripts/fx/spring_grid.gd` — port of your `utils/grid/` (pull-only springs, anchored border); bodies drag it, kills/blasts/death shove it |
| Game feel | `autoload/juice.gd` — trauma camera shake, hit-stop, slow-mo, flash + chromatic aberration (`assets/shaders/screen_fx.gdshader`) |
| Palette | `Constants.NEON_*` |

Juice hooks (all in `game.gd`): particle hit → 50 ms hit-stop + red tick;
kill → small shake + grid pop; suicide blast → shake, hit-stop, RGB split; mass
death → white flash + grid implosion; hero death → full shake, white flash,
aberration, 0.9 s slow-mo. Grid force constants sit at the top of `game.gd`.

Enemy colours are unchanged (red normal, blue split, yellow suicide, teal
invisible) so the original's colour language still teaches the player.
The old sprite sheets are still imported (the tutorial arrow uses one); the rest
can be deleted once you're sure you won't go back. `hex_grid.gd` is kept as a
static alternative (`WorldFx.use_spring_grid = false`).

## Gameplay additions (prototype, 2026)

Both exist to break the "camp an edge and body-block" dominant strategy the
telemetry exposed. Tunables are in `Constants`.

- **Dash** (`Hero.dash`): Shift / gamepad X, or tap the arena on touch. Direction
  is the stick if held, else toward the tap, else the last facing. 9 ticks at
  22 m/s, costs 5 energy, 0.4 s cooldown. Particles hit during a dash are
  *launched* (`Particle.launch`) instead of damaging you; saws still kill.
  Kills of launched particles within 1 s are logged as `dash_kills`.
- **Saw overheat** (`Saw.register_kill`): each kill adds 1 heat, heat decays
  0.6/s. At 2.5 the blade flashes, **throws a copy of itself straight into the
  arena** (a `DeathSaw` with `from_overheat`, 0.7 s telegraph, death cause
  `overheat_saw`) and (threshold 2.0 heat, decay 0.5/s) goes *dull* for 7 s (grey rim, a blue cooldown arc, slow spin): particles
  bounce off it, nothing dies there, it still kills the hero. The heat gauge is
  an arc inside the teeth that turns red and blinks past 70%. Sustained >1
  kill/s on one blade overheats it in ~4 s; one kill every 1.7 s or slower
  never does. `Game.overheat_throws` turns the throw off (tests).
- **Power-ups are dash upgrades** (`powerup_manager.gd`). Everything applies on
  pickup; the stored/deploy system is gone and the bottom-left control is the
  **Dash button** (`dash_button.gd`: rim = cooldown, badge = shockwave charges).
  Passives stack per run, capped at 3, shown top-right (`buff_bar.gd`) with pips:
  Speed icon = *Afterburner* (+25% dash length/stack), Shield = *Reinforced*
  (dash cost -1 and cooldown -25% per stack), Fist = *Impact* (+50% launch
  speed/stack), Clock = *Focus* (world at 40% during the dash and 0.5 s/stack
  after). Consumables: Bolt refills energy, Skull adds a *Shockwave* charge
  (dashes kill what they touch while a charge is held; the charge is spent on
  first contact, so an empty dash costs nothing; max 3), Invincibility
  unchanged. A dash kill counts as an extra blade in the combo chain and pays
  double energy (10/12/20/30 by type, up to full), so dashing is energy-positive. Note a 3-stack Afterburner dash from the
  centre reaches the side walls.
- **Combo = movement.** Each kill is worth `10 x multiplier` immediately, where
  multiplier = `(1 + streak / 5) x distinct blades used in the chain` (blade
  bonus capped at 4). Camping one blade gives x1, x2, x3...; the same kills
  spread over four blades are worth four times as much. The HUD shows the
  multiplier, the blade count and the chain window; kills pop a floating
  "+N". (Replaces the original streak^2 payout at chain end, which multiplied
  nothing visibly.)
- **Heat spreads**: a kill also warms the blade's neighbours (within 190 px) by
  0.5, so a camped section of wall overheats together.
- **Death saw telegraph**: the lane is drawn as a pulsing line for 0.75 s
  before launch.
- Telemetry adds `dashes`, `dash_hits`, `dash_kills`, `overheats`, per-saw
  kill counts, `saws_used` and `top_saw_share` (camping metric).

## Themes

Everything on screen is drawn in code, so a theme is pure data:
`scripts/theme/game_theme.gd` (a `GameTheme` resource: palette + a style enum
per drawable + glow/trail settings), instances in `assets/themes/*.tres`, and
the `Themes` autoload (registry, `current`, `theme_changed`, persisted in
Prefs). Rules, speeds and hitboxes never change with a theme. Every drawer
reads `Themes.current`; `Constants.neon_for()` / `Constants.theme()` forward to it.
Menu: COSMETICS opens `scenes/menu/cosmetics.tscn`, a live preview on the
real field: the hero drifts on stage with one of each enemy around it, tabs
pick the theme (persisted), and TRY DASH / TRY INVINCIBLE / TRY KILL fire the
real effects. Play starts the game from there. Each run logs `theme_<id>`.

| Theme | hero | particles | saws | bursts | field |
| --- | --- | --- | --- | --- | --- |
| `neon` (default) | glow orb + nose | glow orbs | gear blades | spark rings | blue spring lattice, bloom |
| `fruit` | white kitchen knife (`HeroBody._draw_blade`) | flat fruit with rind, highlight, stem (`GlowCore._draw_fruit`) | steel graters (`SawBlade._draw_juicer`) | juice splats that linger (`NeonBurst._ready_splat`) | dark cutting board, no bloom |

Invincibility sets the hero on fire (`scripts/fx/flames.gd`): one tail flame
streaming away from the velocity, drawn behind the body, in the theme's
`flame_outer/mid/core` palette (orange fire in fruit, cyan-white plasma in
neon); the knife's steel goes white-hot with the theme's mid colour as its
edge (`HeroBody.burning`). Ignites in 0.25 s, burns out in 0.6 s. Placeholder
until the art pass; the hook points are the theme palette and `Flames`.

**FX gallery** (`tests/fx_gallery.tscn`, never run automatically): stages
each effect alone on the live field for 5 s each so it can be judged without
playing for it. Exhibits: invincible, dash, perfect, mine_fuse,
mine_stationary, overheat, death, shockwave, combo. Space/arrows step,
T cycles theme, Esc quits. `-- fruit invincible` opens one exhibit in a theme
and loops it; a third arg writes `user://screenshots/fx_<theme>_<exhibit>.png`
after that many seconds and quits. In the fruit theme the stationary mine is a round cactus pad
(`MineBody._draw_cactus`, Stardew-style): one flat green disc, bold pale
spikes, a red flower when armed; scales up while forming, down while fading.

Adding a theme: copy a `.tres`, change the palette/styles, add its id to
`Themes.ORDER`. New *shapes* mean a new enum value plus a `_draw_*` branch in
the relevant drawer.

## Telemetry (for balancing)

`autoload/telemetry.gd` records every run to `user://telemetry/run_*.json`
(kills and hits by type, energy gained/lost, power-ups, max combo, streaks,
difficulty values at death, an energy/alive/score sample per simulated second,
and how much real time was spent frozen by hit-stop/slow-mo) and prints a
one-line summary at game over. Aggregate with:

```bash
python3 godot/tools/analyze_runs.py            # all runs
python3 godot/tools/analyze_runs.py --last 10
```

Spawns are logged too (`spawns`, `spawn_log` with time/type/alive/distance to
hero, `max_alive`, `max_mines_alive`), so questions like "did I get surrounded"
can be answered from the file.

`tests/bot_run.tscn` plays a repeatable, deliberately dumb run (stage 5, hero
circling, no power-ups) for A/B comparisons: `godot --path godot tests/bot_run.tscn -- <hit_stop_cooldown>`.

## Deliberate changes

- Movement input is applied when either axis is non-zero (the original required
  both, so straight horizontal/vertical pushes were ignored).
- The invincibility drop no longer also requires `score % 10000 == 0`; it drops
  once "BEYOND GODLIKE" has been earned.
- Burst/explosion animations are drawn at a sensible scale (the original squashed
  640x480 frames into ~100 px boxes).
- Keyboard and gamepad input added for desktop.
- Enemies never spawn within 320 px of the hero, and mine waves arrive one mine
  every 0.5 s instead of all at once (the original dropped up to six on one tick).
- The stationary "invisible" particle is drawn as a spiked sea-mine
  (`scripts/fx/mine_body.gd`): dim while forming, red cross and long spikes
  while armed, so it can't be read as a pickup.
- Invincibility is true i-frames: blades bounce you instead of killing, and
  touching an armed stationary mine shatters it.
- Stationary mines are frozen static bodies; nothing can shove them. Mines are
  capped at 3 alive, never spawn within 420 px of the hero, 300 px of another
  mine, or while the hero is dashing; the forming (vulnerable) phase is 1.25 s.
- Invincibility shows on the dash button as a draining gold outer ring with
  sparks; it flickers fast in the last quarter.
- No burst at hero/particle contact (it was the main source of clutter at
  5 hits/s); the flinch, dial blink and small shake carry the hit. Dash-hit
  launch bursts are gone too; only perfect hits flash. "+N" pops no longer
  carry a DASH tag (nearly every kill is a dash kill).
- Invincibility also ignores mine blasts.
- Kill-streak and buff announcements are a one-line toast under the score
  (`HUD.show_toast`), not a centre banner; floating "+N" numbers are smaller and
  briefer. The voice lines are unchanged.
- Passive chips live in the dead pocket behind the top-right saw arc
  (`buff_bar.gd` SLOTS, placed by clearance from the blades), stacks as ring segments.
- The spring grid simulates ~1400 points (46 px cells, about one hero across)
  with no fake mid-lines. Cost on this Mac: 1.2 ms step + 0.75 ms draw. Draw
  interpolates each point once and skips entirely while the grid is settled;
  `tests/grid_bench.gd` and `tests/grid_draw_probe.tscn` measure both halves.
- The hero is a saturated cyan body on a dark occlusion disc with a white nose
  (`scripts/fx/hero_body.gd`), so it stays visible inside a crowd of glows.
- The yellow mine's body *is* the fuse: it blinks dim-yellow/hot-white, faster
  toward detonation (2 -> 10 Hz) and shifts red; no outer ring. Dash-launched
  particles fly until they hit a blade (no re-aim during the launch window).
- Every glow core has a dark ink outline, ribbons use normal blending and the
  glow pass is tamer, so overlapping particles stay separable (the earlier
  additive ribbons summed to white exactly where particles crowded).
- Mines wore a spinning dashed hazard ring that grows to the blast radius as the
  fuse burns; pickups sit on a white plate so the two can't be confused.
- Camera shake from kills/hits goes through a capped channel (`Juice.shake_small`);
  only blasts and death can shake hard.
- High score now follows every point, including combo payouts (the original only
  checked on kills, so a combo bonus right before dying was lost).

## Not ported yet

- Google Play Games (achievements/leaderboard) and AdMob: `Services` is a stub.
- Android/iOS export presets (Godot's one-click export; needs the Android SDK).
- The Geometry Wars spring grid in `utils/grid/` was disabled in the original too.
