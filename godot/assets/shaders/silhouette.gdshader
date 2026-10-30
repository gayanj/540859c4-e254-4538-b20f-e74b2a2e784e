// Draws a texture as a flat, glowing silhouette: the texture's alpha is kept,
// the colour is replaced by `tint` (multiplied by modulate). Used for the flat
// dark UI icons from the original atlas so they read on the dark field.
shader_type canvas_item;

uniform vec4 tint : source_color = vec4(2.2, 0.45, 0.4, 1.0);

void fragment() {
	// COLOR arrives as texture * modulate; only the texture's alpha is wanted.
	float a = texture(TEXTURE, UV).a;
	COLOR = vec4(tint.rgb, a * tint.a);
}
