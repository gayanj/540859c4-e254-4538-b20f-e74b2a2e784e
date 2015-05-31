package com.platform.rider.assets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetErrorListener;
import com.badlogic.gdx.assets.AssetManager;
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
                heroParticleDyingAnimationAtlas);
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

        public AssetPowerup(TextureAtlas atlas) {
            super_force = atlas.findRegion("super_force");
            slow_motion = atlas.findRegion("slow_motion");
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

        public AssetLevelDecoration(TextureAtlas atlas) {
            powerbutton = atlas.findRegion("powerbutton");

        }
    }

    public class AssetFonts {
        public final BitmapFont defaultSmall;
        public final BitmapFont defaultNormal;
        public final BitmapFont defaultBig;

        public AssetFonts() {
            // create three fonts for different sizes
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

        public AssetAnimations(TextureAtlas spikeAtlas, TextureAtlas deathSawAtlas, TextureAtlas suicideParticleAtlas,
                               TextureAtlas explosionAtlas, TextureAtlas invisibleParticleAppearingAtlas,
                               TextureAtlas invisibleParticleDisappearingAtlas, TextureAtlas normalParticleDyingAnimationAtlas,
                               TextureAtlas splitParticleDyingAnimationAtlas, TextureAtlas invisibleParticleDyingAnimationAtlas,
                               TextureAtlas suicideParticleDyingAnimationAtlas, TextureAtlas heroParticleDyingAnimationAtlas) {
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
        }
    }
}
