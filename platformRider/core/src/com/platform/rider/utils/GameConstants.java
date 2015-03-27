package com.platform.rider.utils;

import com.badlogic.gdx.math.Vector2;

/**
 * Created by Gayan on 3/8/2015.
 */
public class GameConstants {
    public static final int APP_WIDTH = 800;
    public static final int APP_HEIGHT = 480;

    public static final Vector2 WORLD_GRAVITY = new Vector2(0, -10);

    public static final float GROUND_X = 0;
    public static final float GROUND_Y = 0;
    public static final float GROUND_WIDTH = 50f;
    public static final float GROUND_HEIGHT = 2f;
    public static final float GROUND_DENSITY = 0f;

    // Visible game world is 5 meters wide
    public static final float VIEWPORT_WIDTH = 5.0f;
    // Visible game world is 5 meters tall
    public static final float VIEWPORT_HEIGHT = 5.0f;
}
