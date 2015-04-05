package com.platform.rider.utils;

/**
 * Created by Gayan on 3/8/2015.
 */
public class GameConstants {
    public static final int APP_WIDTH = 1000;
    public static final int APP_HEIGHT = 1000;

    public static final float PIXELS_TO_METERS = 100f;
    public static final short SPRITE_1 = 0x1;    // 0001
    public static final short SPRITE_2 = 0x1 << 1; // 0010 or 0x2 in hex
    public static final short SPRITE_3 = 0x1 << 2; // 0010 or 0x3 in hex
    public static float PARTICLE_SPRITE_SCALE = 0.5f;
    public static float NORMAL_PARTICAL_SPEED = 5f;
    public static float SPLIT_PARTICAL_SPEED = 6f;
    public static float SUICIDE_PARTICAL_SPEED = 9f;
    public static float COLLISION_SPEED = 10f;
    public static float LINEAR_DAMPING = 2f;
    public static float BLAST_RADIUS = 1f;
    public static float FRAME_DURATION = 0.025f;

    //Particle Types
    public static final String NORMAL_PARTICLE = "normal_particle";
    public static final String SPLIT_PARTICLE = "split_particle";
    public static final String SUICIDE_PARTICLE = "suicide_particle";

    // Location of description file for texture atlas
    public static final String TEXTURE_ATLAS_OBJECTS =
            "gameAssets.txt";

    // Location of description file for spike animation texture atlas
    public static final String TEXTURE_ATLAS_SPIKE_ANIMATION =
            "spikeAniimations.txt";

    // Location of description file for suicide particle animation texture atlas
    public static final String TEXTURE_ATLAS_SUICIDE_PARTICAL_ANIMATION =
            "suicideParticleAnimations.txt";
}
