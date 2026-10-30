extends Node
## Game-wide constants. Port of utils/GameConstants.java.
##
## Unit conversion from the original Box2D world: 1 metre = 100 px, so every
## Box2D speed (m/s) is multiplied by [constant PX_PER_M] to get px/s.
## Original timings were frame counters at ~60 Hz; they are kept as physics
## ticks (Godot's physics step is 60 Hz) so the tuning carries over unchanged.

const APP_WIDTH := 1080
const APP_HEIGHT := 1920
const PX_PER_M := 100.0

# Physics layers (bit values). Mirrors SPRITE_1..SPRITE_8 in the Java code.
const LAYER_PARTICLE := 1 << 0
const LAYER_HERO := 1 << 1
const LAYER_SAW := 1 << 2
const LAYER_INVISIBLE_DEADLY := 1 << 3
const LAYER_INVISIBLE_HIDDEN := 1 << 4
const LAYER_POWERUP := 1 << 5
const LAYER_INSTANT_POWERUP := 1 << 6
const LAYER_HERO_INVINCIBLE := 1 << 7

# Speeds in px/s (original value in m/s * 100).
const HERO_SPEED_BASE := 5.0 * PX_PER_M
const NORMAL_PARTICLE_SPEED_BASE := 5.0 * PX_PER_M
const NORMAL_PARTICLE_SPEED_MAX := 10.0 * PX_PER_M
const SPLIT_PARTICLE_SPEED := 7.0 * PX_PER_M
const SUICIDE_PARTICLE_SPEED := 9.0 * PX_PER_M
const INVISIBLE_PARTICLE_SPEED := 0.0
const COLLISION_KNOCKBACK := 20.0 * PX_PER_M
const COLLISION_KNOCKBACK_SUPER := 30.0 * PX_PER_M
const DEATH_SAW_SPEED_H := 10.0 * PX_PER_M
const DEATH_SAW_SPEED_V := 15.0 * PX_PER_M
const MENU_PARTICLE_SPEED := 7.0 * PX_PER_M
const HERO_LINEAR_DAMP := 2.0
const BLAST_RADIUS := 1.0 * PX_PER_M

const PARTICLE_SPRITE_SCALE := 0.5
const SLOW_MOTION_SCALE := 0.3

# Timings in physics ticks (60 Hz).
const SPLIT_PARTICLE_TIME_BASE := 200
const SPLIT_PARTICLE_TIME_MIN := 90
const DEATH_SAW_TIME_BASE := 500
const DEATH_SAW_TIME_MIN := 100
const SUICIDE_PARTICLE_COUNT_BASE := 1
const SUICIDE_PARTICLE_COUNT_MAX := 6
const SUICIDE_BLAST_TICKS := 500
const INVISIBLE_PHASE_TICKS := 300
const INVISIBLE_SPAWN_TICKS := 500
## Mines: at most this many on the field, never within this distance of the
## hero or of another mine, and never spawned while the hero is dashing.
const MAX_MINES_ALIVE := 3
const MINE_MIN_HERO_DISTANCE := 420.0
const MINE_MIN_MINE_DISTANCE := 300.0
const POWERUP_SPAWN_TICKS := 2000
const INSTANT_POWERUP_SPAWN_TICKS := 1000
const POWERUP_DURATION_TICKS := 500
const POWERUP_PICKUP_LIFETIME_TICKS := 300
const INVINCIBILITY_TICKS := 500
const KNOCKBACK_TICKS := 5
const COMBO_WINDOW_TICKS := 100
const KILL_STREAK_TEXT_TICKS := 100
const FIRST_TUTORIAL_MOVE_TICKS := 100
const SECOND_TUTORIAL_DELAY_TICKS := 50
const DEATH_SAW_LAUNCH_DELAY_TICKS := 25
const MAX_SPLIT_PARTICLES_ALIVE := 20

const HERO_START_ENERGY := 80
const HERO_LOW_ENERGY := 20

# Dash: a short burst with i-frames against particles (they get launched).
const DASH_SPEED := 22.0 * PX_PER_M
const DASH_TICKS := 9
const DASH_COST := 5
const DASH_COOLDOWN_TICKS := 24
const DASH_LAUNCH_SPEED := 32.0 * PX_PER_M
## A particle launched by a dash counts as a "dash kill" if it dies within this many ticks.
const DASH_KILL_WINDOW_TICKS := 60

# Saw overheat: too many kills on one saw in quick succession and it goes dull.
const SAW_HEAT_PER_KILL := 1.0
const SAW_HEAT_DECAY_PER_SEC := 0.5
const SAW_OVERHEAT_AT := 2.0
const SAW_OVERHEAT_SECONDS := 7.0
## Heat a kill adds to the two nearest neighbours of the blade that got it.
const SAW_NEIGHBOUR_HEAT := 0.5
const SAW_NEIGHBOUR_DISTANCE := 190.0

# Entity type identifiers.
enum ParticleType { NORMAL, SPLIT, SUICIDE, INVISIBLE }
enum PowerupType { SUPER_FORCE, SLOW_MOTION, MASS_DEATH, ENERGY, SPEED, INVINCIBILITY, ARMOR }

## Passive dash buffs: stack per run up to PASSIVE_MAX_STACKS.
const PASSIVE_POWERUPS: Array[PowerupType] = [
	PowerupType.SPEED, PowerupType.ARMOR, PowerupType.SUPER_FORCE, PowerupType.SLOW_MOTION
]
## Consumables: apply (or charge) immediately.
const CONSUMABLE_POWERUPS: Array[PowerupType] = [
	PowerupType.ENERGY, PowerupType.MASS_DEATH, PowerupType.INVINCIBILITY
]
const PASSIVE_MAX_STACKS := 3
const PASSIVE_NAMES := {
	PowerupType.SPEED: "AFTERBURNER", PowerupType.ARMOR: "REINFORCED",
	PowerupType.SUPER_FORCE: "IMPACT", PowerupType.SLOW_MOTION: "FOCUS",
}
const AFTERBURNER_DISTANCE_PER_STACK := 0.25
const REINFORCED_COST_PER_STACK := 1
const REINFORCED_COOLDOWN_PER_STACK := 0.25
const IMPACT_LAUNCH_PER_STACK := 0.5
const FOCUS_SCALE := 0.4
const FOCUS_TICKS_PER_STACK := 20
## Focus never holds the world slow longer than this after a dash, however many
## dashes are chained (otherwise 3 stacks + 30 dashes/run = permanent slow-mo).
const FOCUS_MAX_TICKS := 60

# Dash impact angle. Alignment = dot(dash direction, hero->target).
## Within this cone the hit is "perfect": full launch, bonus points.
const PERFECT_HIT_COS := 0.966  # cos 15 deg
const PERFECT_LAUNCH_MULT := 1.3
const PERFECT_POINTS_MULT := 1.5
## Glancing hits launch at alignment^2 of full speed, never below this fraction.
const GLANCE_MIN_LAUNCH := 0.35
const SHOCKWAVE_MAX_CHARGES := 3
# Kept for compatibility with older code paths.
const TIMED_POWERUPS: Array[PowerupType] = []
const INSTANT_POWERUPS: Array[PowerupType] = CONSUMABLE_POWERUPS

const POWERUP_ICONS := {
	PowerupType.SUPER_FORCE: "res://assets/atlases/regions/super_force.tres",
	PowerupType.SLOW_MOTION: "res://assets/atlases/regions/slow_motion.tres",
	PowerupType.MASS_DEATH: "res://assets/atlases/regions/mass_death.tres",
	PowerupType.ENERGY: "res://assets/atlases/regions/energy.tres",
	PowerupType.SPEED: "res://assets/atlases/regions/speed.tres",
	PowerupType.INVINCIBILITY: "res://assets/atlases/regions/invincibility.tres",
	PowerupType.ARMOR: "res://assets/atlases/regions/armor.tres",
}

# Kill-streak thresholds -> (label, sound, prefs key). Index 0 is unused.
const KILL_STREAKS := [
	null,
	{"text": "KILLING SPREE!", "sound": "killingspree", "achievement": "killing_spree"},
	{"text": "DOMINATING!", "sound": "dominating", "achievement": "dominating"},
	{"text": "MEGA KILL!", "sound": "megakill", "achievement": "mega_kill"},
	{"text": "UNSTOPPABLE!", "sound": "unstoppable", "achievement": "unstoppable"},
	{"text": "WICKED SICK!", "sound": "wickedsick", "achievement": "wicked_sick"},
	{"text": "MONSTER KILL!", "sound": "monsterkill", "achievement": "monster_kill"},
	{"text": "GODLIKE!", "sound": "godlike", "achievement": "god_like"},
	{"text": "ULTRA KILL!", "sound": "ultrakill", "achievement": "ultra_kill"},
	{"text": "RAMPAGE!", "sound": "rampage", "achievement": "rampage"},
	{"text": "BEYOND GODLIKE!", "sound": "holyshit", "achievement": "beyond_god_like"},
]

# Palette. Values above 1.0 are intentional: the viewport is HDR and the glow
# pass picks up anything brighter than white.
const CLEAR_COLOR := Color(0.03, 0.03, 0.06)
const GRID_COLOR := Color(0.22, 0.42, 1.25, 0.42)
const FONT_COLOR := Color(1.6, 0.42, 0.38)
const NEON_HERO := Color(0.35, 1.7, 2.1)
const NEON_NORMAL := Color(2.3, 0.3, 0.26)
const NEON_SPLIT := Color(0.4, 1.3, 3.0)
const NEON_SUICIDE := Color(3.0, 2.4, 0.3)
const NEON_INVISIBLE := Color(0.5, 2.6, 1.8)
const NEON_SAW := Color(1.15, 0.26, 0.2)
const NEON_DEATH_SAW := Color(3.0, 0.3, 1.2)
const NEON_EXPLOSION := Color(3.0, 1.4, 0.3)
const NEON_UI := Color(2.2, 0.45, 0.4)

# Particle trail tints (from the .p effect files).
const TINT_NORMAL := Color(0.851, 0.0745, 0.0157)
const TINT_SPLIT := Color(0.0, 0.3216, 0.6118)
const TINT_SUICIDE := Color(0.9961, 0.9098, 0.0039)
const TINT_HERO := [
	Color(0.6, 0.0, 0.7725), Color(0.4431, 0.9216, 0.8706), Color(0.851, 0.0667, 0.0157),
	Color(0.9961, 0.9098, 0.0039), Color.WHITE
]


## Palette lookups go through the active theme; the NEON_* constants above are
## the neon theme's defaults and remain for the tests.
static func neon_for(type: ParticleType) -> Color:
	return Themes.current.color_for(type)


static func theme() -> GameTheme:
	return Themes.current


## Convert a y coordinate from the original LibGDX (y-up) world to Godot (y-down).
static func flip_y(y: float) -> float:
	return APP_HEIGHT - y


static func center() -> Vector2:
	return Vector2(APP_WIDTH / 2.0, APP_HEIGHT / 2.0)
