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
    public static final String PREFERENCES_FILE = "particlebrawlprefs";

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
            "spikeAnimations.txt";

    // Location of description file for death spike animation texture atlas
    public static final String TEXTURE_ATLAS_DEATH_SAW_ANIMATION =
            "deathSpikeAnimation.txt";

    // Location of description file for play button spike animation texture atlas
    public static final String TEXTURE_ATLAS_PLAY_BUTTON_SAW_ANIMATION =
            "playButtonSawAnimation.txt";

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

    // Location of description file for tutorial arrow animation texture atlas
    public static final String TEXTURE_ATLAS_TUTORIAL_ARROW_ANIMATION =
            "tutorialArrowAnimation.txt";

    // Location of description file for particle death sound effect
    public static final String PARTICLE_DEATH_SOUND =
            "sounds/particle_death.ogg";

    // Location of description file for hero death sound effect
    public static final String HERO_DEATH_SOUND =
            "sounds/hero_death.ogg";

    // Location of description file for pickup sound effect
    public static final String PICKUP_SOUND =
            "sounds/pickup.ogg";

    // Location of description file for bomb sound effect
    public static final String BOMB_SOUND =
            "sounds/bomb.ogg";

    // Location of description file for armor sound effect
    public static final String ARMOR_SOUND =
            "sounds/armor.ogg";

    // Location of description file for speed sound effect
    public static final String SPEED_SOUND =
            "sounds/speed.ogg";

    // Location of description file for energy sound effect
    public static final String ENERGY_SOUND =
            "sounds/energy.ogg";

    // Location of description file for invincible sound effect
    public static final String INVINCIBLE_SOUND =
            "sounds/invincible.ogg";

    // Location of description file for alert sound effect
    public static final String ALERT_SOUND =
            "sounds/alert.ogg";

    // Kill streak sounds
    public static final String KILLINGSPREE =
            "sounds/killingspree.ogg";

    public static final String DOMINATING =
            "sounds/dominating.ogg";

    public static final String MEGAKILL =
            "sounds/megakill.ogg";

    public static final String UNSTOPPABLE =
            "sounds/unstoppable.ogg";

    public static final String WICKEDSICK =
            "sounds/wickedsick.ogg";

    public static final String MONSTERKILL =
            "sounds/monsterkill.ogg";

    public static final String GODLIKE =
            "sounds/godlike.ogg";

    public static final String ULTRAKILL =
            "sounds/ultrakill.ogg";

    public static final String RAMPAGE =
            "sounds/rampage.ogg";

    public static final String HOLYSHIT =
            "sounds/holyshit.ogg";

    // Location of description file for background music
    public static final String BACKGROUND_MUSIC =
            "music/background.ogg";

    public static final String BACKGROUND_MUSIC2 =
            "music/background2.ogg";

    public static final String BACKGROUND_MUSIC3 =
            "music/background3.ogg";

    public static final String BACKGROUND_MUSIC4 =
            "music/background4.ogg";

    public static final String BACKGROUND_MUSIC5 =
            "music/background5.ogg";

    public static final String BACKGROUND_MUSIC6 =
            "music/background6.ogg";

    // Location of description file for menu music
    public static final String MENU_MUSIC =
            "music/menu.ogg";

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
