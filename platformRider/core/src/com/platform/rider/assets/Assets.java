package com.platform.rider.assets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetErrorListener;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.utils.Disposable;
import com.platform.rider.utils.GameConstants;

/**
 * Created by Gayan on 3/28/2015.
 */
public class Assets implements Disposable, AssetErrorListener {
    public static final String TAG = Assets.class.getName();
    public static final Assets instance = new Assets();
    private AssetManager assetManager;

    public AssetParticle assetParticle;
    public AssetHero assetHero;
    public AssetSpike assetSpike;
    public AssetPowerup assetPowerup;
    public AssetLevelDecoration assetLevelDecoration;
    public AssetFonts fonts;
    public AssetAnimations assetAnimations;
    public AssetSounds sounds;
    public AssetMusic music;

    // singleton: prevent instantiation from other classes
    private Assets() {
    }

    public AssetManager getAssetManager(AssetManager assetManager) {
        this.assetManager = assetManager;
        return assetManager;
    }

    public void init() {
        //this.assetManager = assetManager;
        // set asset manager error handler
        /*assetManager.setErrorListener(this);
        // load texture atlas
        assetManager.load(GameConstants.TEXTURE_ATLAS_OBJECTS,
                TextureAtlas.class);
        // load texture atlas
        assetManager.load(GameConstants.TEXTURE_ATLAS_SPIKE_ANIMATION,
                TextureAtlas.class);
        // load texture atlas
        assetManager.load(GameConstants.TEXTURE_ATLAS_DEATH_SAW_ANIMATION,
                TextureAtlas.class);
        // load texture atlas
        assetManager.load(GameConstants.TEXTURE_ATLAS_SUICIDE_PARTICAL_ANIMATION,
                TextureAtlas.class);
        // load texture atlas
        assetManager.load(GameConstants.TEXTURE_ATLAS_EXPLOSION_ANIMATION,
                TextureAtlas.class);
        // load texture atlas
        assetManager.load(GameConstants.TEXTURE_ATLAS_INVISIBLE_PARTICLE_APPEARING_ANIMATION,
                TextureAtlas.class);
        // load texture atlas
        assetManager.load(GameConstants.TEXTURE_ATLAS_INVISIBLE_PARTICLE_DISAPPEARING_ANIMATION,
                TextureAtlas.class);
        // load texture atlas
        assetManager.load(GameConstants.TEXTURE_ATLAS_NORMAL_PARTICLE_DYING_ANIMATION,
                TextureAtlas.class);
        // load texture atlas
        assetManager.load(GameConstants.TEXTURE_ATLAS_SPLIT_PARTICLE_DYING_ANIMATION,
                TextureAtlas.class);
        // load texture atlas
        assetManager.load(GameConstants.TEXTURE_ATLAS_INVISIBLE_PARTICLE_DYING_ANIMATION,
                TextureAtlas.class);
        // load texture atlas
        assetManager.load(GameConstants.TEXTURE_ATLAS_SUICIDE_PARTICLE_DYING_ANIMATION,
                TextureAtlas.class);
        // load texture atlas
        assetManager.load(GameConstants.TEXTURE_ATLAS_HREO_PARTICLE_DYING_ANIMATION,
                TextureAtlas.class);
        // start loading assets and wait until finished
        assetManager.finishLoading();
        Gdx.app.debug(TAG, "# of assets loaded: "
                + assetManager.getAssetNames().size);
        for (String a : assetManager.getAssetNames())
            Gdx.app.debug(TAG, "asset: " + a);*/

        TextureAtlas atlas =
                assetManager.get(GameConstants.TEXTURE_ATLAS_OBJECTS);
        // enable texture filtering for pixel smoothing
        for (Texture t : atlas.getTextures()) {
            t.setFilter(TextureFilter.Linear, TextureFilter.Linear);
        }

        TextureAtlas spikeAnimationAtlas =
                assetManager.get(GameConstants.TEXTURE_ATLAS_SPIKE_ANIMATION);
        // enable texture filtering for pixel smoothing
        for (Texture t : spikeAnimationAtlas.getTextures()) {
            t.setFilter(TextureFilter.Linear, TextureFilter.Linear);
        }

        TextureAtlas deathSawAnimationAtlas =
                assetManager.get(GameConstants.TEXTURE_ATLAS_DEATH_SAW_ANIMATION);
        // enable texture filtering for pixel smoothing
        for (Texture t : deathSawAnimationAtlas.getTextures()) {
            t.setFilter(TextureFilter.Linear, TextureFilter.Linear);
        }

        TextureAtlas suicideParticleAnimationAtlas =
                assetManager.get(GameConstants.TEXTURE_ATLAS_SUICIDE_PARTICAL_ANIMATION);
        // enable texture filtering for pixel smoothing
        for (Texture t : suicideParticleAnimationAtlas.getTextures()) {
            t.setFilter(TextureFilter.Linear, TextureFilter.Linear);
        }

        TextureAtlas explosionAnimationAtlas =
                assetManager.get(GameConstants.TEXTURE_ATLAS_EXPLOSION_ANIMATION);
        // enable texture filtering for pixel smoothing
        for (Texture t : explosionAnimationAtlas.getTextures()) {
            t.setFilter(TextureFilter.Linear, TextureFilter.Linear);
        }

        TextureAtlas invisibleParticleAppearingAtlas =
                assetManager.get(GameConstants.TEXTURE_ATLAS_INVISIBLE_PARTICLE_APPEARING_ANIMATION);
        // enable texture filtering for pixel smoothing
        for (Texture t : explosionAnimationAtlas.getTextures()) {
            t.setFilter(TextureFilter.Linear, TextureFilter.Linear);
        }

        TextureAtlas invisibleParticleDisappearingAtlas =
                assetManager.get(GameConstants.TEXTURE_ATLAS_INVISIBLE_PARTICLE_DISAPPEARING_ANIMATION);
        // enable texture filtering for pixel smoothing
        for (Texture t : explosionAnimationAtlas.getTextures()) {
            t.setFilter(TextureFilter.Linear, TextureFilter.Linear);
        }

        TextureAtlas normalParticleDyingAnimationAtlas =
                assetManager.get(GameConstants.TEXTURE_ATLAS_NORMAL_PARTICLE_DYING_ANIMATION);
        // enable texture filtering for pixel smoothing
        for (Texture t : normalParticleDyingAnimationAtlas.getTextures()) {
            t.setFilter(TextureFilter.Linear, TextureFilter.Linear);
        }

        TextureAtlas splitParticleDyingAnimationAtlas =
                assetManager.get(GameConstants.TEXTURE_ATLAS_SPLIT_PARTICLE_DYING_ANIMATION);
        // enable texture filtering for pixel smoothing
        for (Texture t : splitParticleDyingAnimationAtlas.getTextures()) {
            t.setFilter(TextureFilter.Linear, TextureFilter.Linear);
        }

        TextureAtlas invisibleParticleDyingAnimationAtlas =
                assetManager.get(GameConstants.TEXTURE_ATLAS_INVISIBLE_PARTICLE_DYING_ANIMATION);
        // enable texture filtering for pixel smoothing
        for (Texture t : invisibleParticleDyingAnimationAtlas.getTextures()) {
            t.setFilter(TextureFilter.Linear, TextureFilter.Linear);
        }

        TextureAtlas suicideParticleDyingAnimationAtlas =
                assetManager.get(GameConstants.TEXTURE_ATLAS_SUICIDE_PARTICLE_DYING_ANIMATION);
        // enable texture filtering for pixel smoothing
        for (Texture t : suicideParticleDyingAnimationAtlas.getTextures()) {
            t.setFilter(TextureFilter.Linear, TextureFilter.Linear);
        }

        TextureAtlas heroParticleDyingAnimationAtlas =
                assetManager.get(GameConstants.TEXTURE_ATLAS_HREO_PARTICLE_DYING_ANIMATION);
        // enable texture filtering for pixel smoothing
        for (Texture t : heroParticleDyingAnimationAtlas.getTextures()) {
            t.setFilter(TextureFilter.Linear, TextureFilter.Linear);
        }

        TextureAtlas tutorialArrowAnimationAtlas =
                assetManager.get(GameConstants.TEXTURE_ATLAS_TUTORIAL_ARROW_ANIMATION);
        // enable texture filtering for pixel smoothing
        for (Texture t : tutorialArrowAnimationAtlas.getTextures()) {
            t.setFilter(TextureFilter.Linear, TextureFilter.Linear);
        }

        fonts = new AssetFonts();
        assetParticle = new AssetParticle(atlas);
        assetHero = new AssetHero(atlas);
        assetSpike = new AssetSpike(atlas);
        assetPowerup = new AssetPowerup(atlas);
        assetLevelDecoration = new AssetLevelDecoration(atlas);
        assetAnimations = new AssetAnimations(
                spikeAnimationAtlas, deathSawAnimationAtlas,
                suicideParticleAnimationAtlas, explosionAnimationAtlas,
                invisibleParticleAppearingAtlas, invisibleParticleDisappearingAtlas,
                normalParticleDyingAnimationAtlas, splitParticleDyingAnimationAtlas,
                invisibleParticleDyingAnimationAtlas, suicideParticleDyingAnimationAtlas,
                heroParticleDyingAnimationAtlas,tutorialArrowAnimationAtlas);
        sounds = new AssetSounds(this.assetManager);
        music = new AssetMusic(this.assetManager);
    }

    @Override
    public void dispose() {
        assetManager.dispose();
        fonts.defaultSmall.dispose();
        fonts.defaultNormal.dispose();
        fonts.defaultBig.dispose();
    }

    public void error(String filename, Class type,
                      Throwable throwable) {
        Gdx.app.error(TAG, "Couldn't load asset '"
                + filename + "'", (Exception) throwable);
    }

    @Override
    public void error(AssetDescriptor asset, Throwable throwable) {
        Gdx.app.error(TAG, "Couldn't load asset '" +
                asset.fileName + "'", (Exception) throwable);
    }

    public class AssetParticle {
        public final TextureAtlas.AtlasRegion particle;
        public final TextureAtlas.AtlasRegion split_particle;
        public final TextureAtlas.AtlasRegion suicide_particle;

        public AssetParticle(TextureAtlas atlas) {
            particle = atlas.findRegion("particle");
            split_particle = atlas.findRegion("splitparticle");
            suicide_particle = atlas.findRegion("suicideparticle");
        }
    }

    public class AssetHero {
        public final TextureAtlas.AtlasRegion hero;

        public AssetHero(TextureAtlas atlas) {
            hero = atlas.findRegion("hero");
        }
    }

    public class AssetPowerup {
        public final TextureAtlas.AtlasRegion super_force;
        public final TextureAtlas.AtlasRegion slow_motion;
        public final TextureAtlas.AtlasRegion mass_death;
        public final TextureAtlas.AtlasRegion energy;
        public final TextureAtlas.AtlasRegion speed;
        public final TextureAtlas.AtlasRegion invincibility;
        public final TextureAtlas.AtlasRegion armor;

        public AssetPowerup(TextureAtlas atlas) {
            super_force = atlas.findRegion("super_force");
            slow_motion = atlas.findRegion("slow_motion");
            mass_death = atlas.findRegion("mass_death");
            energy = atlas.findRegion("energy");
            speed = atlas.findRegion("speed");
            invincibility = atlas.findRegion("invincibility");
            armor = atlas.findRegion("armor");

        }
    }

    public class AssetSpike {
        public final TextureAtlas.AtlasRegion spike;

        public AssetSpike(TextureAtlas atlas) {
            spike = atlas.findRegion("spike");
        }
    }

    public class AssetLevelDecoration {
        public final TextureAtlas.AtlasRegion powerbutton;
        public final TextureAtlas.AtlasRegion secondTutorial;
        public final TextureAtlas.AtlasRegion energyLogo;
        public final TextureAtlas.AtlasRegion background;

        public AssetLevelDecoration(TextureAtlas atlas) {
            powerbutton = atlas.findRegion("powerbutton");
            secondTutorial = atlas.findRegion("secondTutorial");
            energyLogo = atlas.findRegion("energyLogo");
            background = atlas.findRegion("background");
        }
    }

    public class AssetFonts {
        public final BitmapFont energyRed;
        public final BitmapFont energyGreen;
        public final BitmapFont energyYellow;
        public final BitmapFont energyOrange;
        public final BitmapFont defaultSmall;
        public final BitmapFont defaultNormal;
        public final BitmapFont defaultBig;

        public AssetFonts() {
            // create three fonts for different sizes
            energyRed = new BitmapFont(
                    Gdx.files.internal("fonts/energyRed.fnt"), true);
            energyGreen = new BitmapFont(
                    Gdx.files.internal("fonts/energyGreen.fnt"), true);
            energyYellow = new BitmapFont(
                    Gdx.files.internal("fonts/energyYellow.fnt"), true);
            energyOrange = new BitmapFont(
                    Gdx.files.internal("fonts/energyOrange.fnt"), true);
            defaultSmall = new BitmapFont(
                    Gdx.files.internal("fonts/bitmap.fnt"), true);
            defaultNormal = new BitmapFont(
                    Gdx.files.internal("fonts/bitmap.fnt"), true);
            defaultBig = new BitmapFont(
                    Gdx.files.internal("fonts/bitmap.fnt"), true);
            // set font sizes
            defaultSmall.setScale(0.75f);
            defaultNormal.setScale(1.0f);
            defaultBig.setScale(2.0f);
            // enable linear texture filtering for smooth fonts
            energyRed.getRegion().getTexture().setFilter(
                    TextureFilter.Linear, TextureFilter.Linear);
            defaultSmall.getRegion().getTexture().setFilter(
                    TextureFilter.Linear, TextureFilter.Linear);
            defaultNormal.getRegion().getTexture().setFilter(
                    TextureFilter.Linear, TextureFilter.Linear);
            defaultBig.getRegion().getTexture().setFilter(
                    TextureFilter.Linear, TextureFilter.Linear);
        }
    }

    public class AssetAnimations {
        public final Animation spikeAnimation;
        public final Animation deathSawAnimation;
        public final Animation suicideParticleAnimation;
        public final Animation explosionParticleAnimation;
        public final Animation invisibleParticleAppearingAnimation;
        public final Animation invisibleParticleDisappearingAnimation;
        public final Animation normalParticleDyingAnimation;
        public final Animation splitParticleDyingAnimation;
        public final Animation invisibleParticleDyingAnimation;
        public final Animation suicideParticleDyingAnimation;
        public final Animation heroParticleDyingAnimation;
        public final Animation tutorialArrowAnimation;

        public AssetAnimations(TextureAtlas spikeAtlas, TextureAtlas deathSawAtlas, TextureAtlas suicideParticleAtlas,
                               TextureAtlas explosionAtlas, TextureAtlas invisibleParticleAppearingAtlas,
                               TextureAtlas invisibleParticleDisappearingAtlas, TextureAtlas normalParticleDyingAnimationAtlas,
                               TextureAtlas splitParticleDyingAnimationAtlas, TextureAtlas invisibleParticleDyingAnimationAtlas,
                               TextureAtlas suicideParticleDyingAnimationAtlas, TextureAtlas heroParticleDyingAnimationAtlas,
                               TextureAtlas tutorialArrowAnimationAtlas) {
            spikeAnimation = new Animation(GameConstants.FRAME_DURATION, spikeAtlas.getRegions());
            spikeAnimation.setPlayMode(Animation.PlayMode.LOOP);

            deathSawAnimation = new Animation(GameConstants.FRAME_DURATION, deathSawAtlas.getRegions());
            deathSawAnimation.setPlayMode(Animation.PlayMode.LOOP);

            suicideParticleAnimation = new Animation(GameConstants.FRAME_DURATION, suicideParticleAtlas.getRegions());
            suicideParticleAnimation.setPlayMode(Animation.PlayMode.LOOP);

            explosionParticleAnimation = new Animation(GameConstants.FRAME_DURATION, explosionAtlas.getRegions());
            explosionParticleAnimation.setPlayMode(Animation.PlayMode.NORMAL);

            invisibleParticleAppearingAnimation = new Animation(GameConstants.FRAME_DURATION, invisibleParticleAppearingAtlas.getRegions());
            invisibleParticleAppearingAnimation.setPlayMode(Animation.PlayMode.NORMAL);

            invisibleParticleDisappearingAnimation = new Animation(GameConstants.FRAME_DURATION, invisibleParticleDisappearingAtlas.getRegions());
            invisibleParticleDisappearingAnimation.setPlayMode(Animation.PlayMode.NORMAL);

            normalParticleDyingAnimation = new Animation(GameConstants.FRAME_DURATION, normalParticleDyingAnimationAtlas.getRegions());
            normalParticleDyingAnimation.setPlayMode(Animation.PlayMode.NORMAL);

            splitParticleDyingAnimation = new Animation(GameConstants.FRAME_DURATION, splitParticleDyingAnimationAtlas.getRegions());
            splitParticleDyingAnimation.setPlayMode(Animation.PlayMode.NORMAL);

            invisibleParticleDyingAnimation = new Animation(GameConstants.FRAME_DURATION, invisibleParticleDyingAnimationAtlas.getRegions());
            invisibleParticleDyingAnimation.setPlayMode(Animation.PlayMode.NORMAL);

            suicideParticleDyingAnimation = new Animation(GameConstants.FRAME_DURATION, suicideParticleDyingAnimationAtlas.getRegions());
            suicideParticleDyingAnimation.setPlayMode(Animation.PlayMode.NORMAL);

            heroParticleDyingAnimation = new Animation(GameConstants.FRAME_DURATION, heroParticleDyingAnimationAtlas.getRegions());
            heroParticleDyingAnimation.setPlayMode(Animation.PlayMode.NORMAL);

            tutorialArrowAnimation = new Animation(GameConstants.FRAME_DURATION, tutorialArrowAnimationAtlas.getRegions());
            tutorialArrowAnimation.setPlayMode(Animation.PlayMode.LOOP);
        }
    }

    public class AssetSounds {
        public final Sound particle_death;
        public final Sound hero_death;
        public final Sound pickup;
        public final Sound bomb;
        public final Sound armor;
        public final Sound speed;
        public final Sound energy;
        public final Sound invincible;

        public final Sound killingspree;
        public final Sound dominating;
        public final Sound megakill;
        public final Sound unstoppable;
        public final Sound wickedsick;
        public final Sound monsterkill;
        public final Sound godlike;
        public final Sound ultrakill;
        public final Sound rampage;
        public final Sound holyshit;

        public AssetSounds (AssetManager assetManager) {
            particle_death = assetManager.get(GameConstants.PARTICLE_DEATH_SOUND, Sound.class);
            hero_death = assetManager.get(GameConstants.HERO_DEATH_SOUND, Sound.class);
            pickup = assetManager.get(GameConstants.PICKUP_SOUND, Sound.class);
            bomb = assetManager.get(GameConstants.BOMB_SOUND, Sound.class);
            armor = assetManager.get(GameConstants.ARMOR_SOUND, Sound.class);
            speed = assetManager.get(GameConstants.SPEED_SOUND, Sound.class);
            energy = assetManager.get(GameConstants.ENERGY_SOUND, Sound.class);
            invincible = assetManager.get(GameConstants.INVINCIBLE_SOUND, Sound.class);

            killingspree = assetManager.get(GameConstants.KILLINGSPREE, Sound.class);
            dominating = assetManager.get(GameConstants.DOMINATING, Sound.class);
            megakill = assetManager.get(GameConstants.MEGAKILL, Sound.class);
            unstoppable = assetManager.get(GameConstants.UNSTOPPABLE, Sound.class);
            wickedsick = assetManager.get(GameConstants.WICKEDSICK, Sound.class);
            monsterkill = assetManager.get(GameConstants.MONSTERKILL, Sound.class);
            godlike = assetManager.get(GameConstants.GODLIKE, Sound.class);
            ultrakill = assetManager.get(GameConstants.ULTRAKILL, Sound.class);
            rampage = assetManager.get(GameConstants.RAMPAGE, Sound.class);
            holyshit = assetManager.get(GameConstants.HOLYSHIT, Sound.class);
        }
    }

    public class AssetMusic {
        public final Music background_music;
        public final Music background_music2;
        public final Music background_music3;
        public final Music background_music4;
        public final Music background_music5;
        public final Music menu_music;
        public final Music alert;

        public AssetMusic (AssetManager assetManager) {
            background_music = assetManager.get(GameConstants.BACKGROUND_MUSIC, Music.class);
            background_music2 = assetManager.get(GameConstants.BACKGROUND_MUSIC2, Music.class);
            background_music3 = assetManager.get(GameConstants.BACKGROUND_MUSIC3, Music.class);
            background_music4 = assetManager.get(GameConstants.BACKGROUND_MUSIC4, Music.class);
            background_music5 = assetManager.get(GameConstants.BACKGROUND_MUSIC5, Music.class);
            menu_music = assetManager.get(GameConstants.MENU_MUSIC, Music.class);
            alert = assetManager.get(GameConstants.ALERT_SOUND, Music.class);
        }
    }
}
