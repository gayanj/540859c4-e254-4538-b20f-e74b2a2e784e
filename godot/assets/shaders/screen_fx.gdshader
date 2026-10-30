// Full-screen post effect driven by the Juice autoload: chromatic aberration
// (RGB split radiating from the centre). Reads the screen, so it is only shown
// while an effect is active.
shader_type canvas_item;

uniform sampler2D screen_tex : hint_screen_texture, filter_linear, repeat_disable;
uniform float aberration = 0.0;

void fragment() {
	vec2 dir = SCREEN_UV - vec2(0.5);
	float r = texture(screen_tex, SCREEN_UV + dir * aberration).r;
	float g = texture(screen_tex, SCREEN_UV).g;
	float b = texture(screen_tex, SCREEN_UV - dir * aberration).b;
	COLOR = vec4(r, g, b, 1.0);
}
