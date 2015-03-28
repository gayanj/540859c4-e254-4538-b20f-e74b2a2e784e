package com.platform.rider.utils;

/**
 * Created by Gayan on 3/8/2015.
 */
public class GameConstants {
    public static final int APP_WIDTH = 800;
    public static final int APP_HEIGHT = 480;

    public static final float PIXELS_TO_METERS = 100f;
    public static final short SPRITE_1 = 0x1;    // 0001
    public static final short SPRITE_2 = 0x1 << 1; // 0010 or 0x2 in hex
    public static final short SPRITE_3 = 0x1 << 2; // 0010 or 0x3 in hex
    public static float SPEED = 10f;
    public static float COLLISION_SPEED = 10f;

    // Location of description file for texture atlas
    public static final String TEXTURE_ATLAS_OBJECTS =
            "gameAssets.txt";
}
