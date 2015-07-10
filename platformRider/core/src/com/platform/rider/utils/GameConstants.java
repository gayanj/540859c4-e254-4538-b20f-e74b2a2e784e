package com.platform.rider.utils;

/**
 * Created by Gayan on 3/8/2015.
 */
public class GameConstants {
    public static final int APP_WIDTH = 1080;
    public static final int APP_HEIGHT = 1920;

    public static final float PIXELS_TO_METERS = 100f;
    public static final short SPRITE_1 = 0x1;    // 0001 - Normal particle
    public static final short SPRITE_2 = 0x1 << 1; // 0010 or 0x2 in hex - Hero Particle
    public static final short SPRITE_3 = 0x1 << 2; // 0010 or 0x3 in hex - Spikes
    public static final short SPRITE_4 = 0x1 << 3; // 0010 or 0x4 in hex - Invisible particle deadly
    public static final short SPRITE_5 = 0x1 << 4; // 0010 or 0x4 in hex - Invisible particle invulnerable
    public static final short SPRITE_6 = 0x1 << 5; // 0010 or 0x4 in hex - Powerup
    public static final short SPRITE_7 = 0x1 << 6; // 0010 or 0x4 in hex - Instant Powerup
    public static final short SPRITE_8 = 0x1 << 7; // 0010 or 0x4 in hex - Invincibility
    public static float PARTICLE_SPRITE_SCALE = 0.5f;
    public static float NORMAL_PARTICAL_SPEED = 5f;
    public static float SPLIT_PARTICAL_SPEED = 7f;
    public static float SUICIDE_PARTICAL_SPEED = 9f;
    public static float HERO_SPEED = 5f;
    public static float INVISIBLE_PARTICLE_SPEED = 0f;
    public static float COLLISION_SPEED = 20f;
    public static float LINEAR_DAMPING = 2f;
    public static float BLAST_RADIUS = 1f;
    public static float FRAME_DURATION = 0.025f;
    public static float SPLIT_PARTICAL_TIME = 200;
    public static float SUICIDE_PARTICAL_COUNT = 1;
    public static float DEATH_SAW_TIME = 500;

    //Particle Types
    public static final String HERO_PARTICLE = "hero";
    public static final String NORMAL_PARTICLE = "normal_particle";
    public static final String SPLIT_PARTICLE = "split_particle";
    public static final String SUICIDE_PARTICLE = "suicide_particle";
    public static final String INVISIBLE_PARTICLE = "invisible_particle";

    // Location of description file for texture atlas
    public static final String TEXTURE_ATLAS_OBJECTS =
            "gameAssets.txt";

    // Location of description file for spike animation texture atlas
    public static final String TEXTURE_ATLAS_SPIKE_ANIMATION =
            "spikeAniimations.txt";

    // Location of description file for death spike animation texture atlas
    public static final String TEXTURE_ATLAS_DEATH_SAW_ANIMATION =
            "deathSpikeAnimation.txt";

    // Location of description file for suicide particle animation texture atlas
    public static final String TEXTURE_ATLAS_SUICIDE_PARTICAL_ANIMATION =
            "suicideParticleAnimations.txt";

    // Location of description file for explosion animation texture atlas
    public static final String TEXTURE_ATLAS_EXPLOSION_ANIMATION =
            "explosionAnimation.txt";

    // Location of description file for invisible particle animation texture atlas
    public static final String TEXTURE_ATLAS_INVISIBLE_PARTICLE_APPEARING_ANIMATION =
            "invisibleParticleAppearingAnimation.txt";

    // Location of description file for invisible particle animation texture atlas
    public static final String TEXTURE_ATLAS_INVISIBLE_PARTICLE_DISAPPEARING_ANIMATION =
            "invisibleParticleDisappearingAnimation.txt";

    // Location of description file for normal particle dying animation texture atlas
    public static final String TEXTURE_ATLAS_NORMAL_PARTICLE_DYING_ANIMATION =
            "normalParticleDyingAnimation.txt";

    // Location of description file for split particle dying animation texture atlas
    public static final String TEXTURE_ATLAS_SPLIT_PARTICLE_DYING_ANIMATION =
            "splitParticleDyingAnimation.txt";

    // Location of description file for invisible particle dying animation texture atlas
    public static final String TEXTURE_ATLAS_INVISIBLE_PARTICLE_DYING_ANIMATION =
            "invisibleParticleDyingAnimation.txt";

    // Location of description file for suicide particle dying animation texture atlas
    public static final String TEXTURE_ATLAS_SUICIDE_PARTICLE_DYING_ANIMATION =
            "suicideParticleDyingAnimation.txt";

    // Location of description file for hero particle dying animation texture atlas
    public static final String TEXTURE_ATLAS_HREO_PARTICLE_DYING_ANIMATION =
            "heroParticleDyingAnimation.txt";

    //Power Up Types
    public static final String SUPER_FORCE = "super_force";
    public static final String SLOW_MOTION = "slow_motion";
    public static final String MASS_DEATH = "mass_death";

    //Instant Power Up Types
    public static final String ENERGY = "energy";
    public static final String SPEED = "speed";
    public static final String INVINCIBILITY = "invincibility";
    public static final String ARMOR = "armor";
}
