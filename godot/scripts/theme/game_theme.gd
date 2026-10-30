class_name GameTheme
extends Resource
## A visual skin: palette + which procedural shape each entity uses. Every
## drawer in the game (hero body, particle cores, saw blades, bursts, grid,
## HUD accents) reads from the active theme via the Themes autoload, so a
## theme is purely data. Rules, speeds and hitboxes never change with a theme.

enum HeroStyle { ORB, BLADE }
enum ParticleStyle { ORB, FRUIT }
enum SawStyle { GEAR, JUICER }
enum BurstStyle { SPARKS, SPLAT }
enum GridStyle { SPRINGS, BOARD }

@export var id := "neon"
@export var display_name := "NEON"

@export_group("Styles")
@export var hero_style := HeroStyle.ORB
@export var particle_style := ParticleStyle.ORB
@export var saw_style := SawStyle.GEAR
@export var burst_style := BurstStyle.SPARKS
@export var grid_style := GridStyle.SPRINGS

@export_group("Palette")
@export var clear_color := Color(0.03, 0.03, 0.06)
@export var grid_color := Color(0.22, 0.42, 1.25, 0.42)
@export var hero := Color(0.35, 1.7, 2.1)
@export var hero_invincible := Color(3.0, 2.6, 1.0)
@export var normal := Color(2.3, 0.3, 0.26)
@export var split := Color(0.4, 1.3, 3.0)
@export var suicide := Color(3.0, 2.4, 0.3)
@export var invisible := Color(0.5, 2.6, 1.8)
@export var saw := Color(1.15, 0.26, 0.2)
@export var death_saw := Color(3.0, 0.3, 1.2)
@export var explosion := Color(3.0, 1.4, 0.3)
@export var ui := Color(1.6, 0.42, 0.38)
@export var shockwave := Color(2.4, 2.0, 0.6)

@export_group("Fire")
## Invincibility flame, outer -> core.
@export var flame_outer := Color(0.88, 0.18, 0.05)
@export var flame_mid := Color(1.0, 0.58, 0.12)
@export var flame_core := Color(1.0, 0.95, 0.6)

@export_group("Rendering")
## Glow pass strength; themes with flat shading want it low.
@export var glow_intensity := 0.85
@export var glow_bloom := 0.08
## Multiplier on ribbon/trail alpha; flat themes use shorter, fainter trails.
@export var trail_alpha := 1.0
@export var ribbon_points := 22


func color_for(type: Constants.ParticleType) -> Color:
	match type:
		Constants.ParticleType.SPLIT:
			return split
		Constants.ParticleType.SUICIDE:
			return suicide
		Constants.ParticleType.INVISIBLE:
			return invisible
	return normal
