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
    public AssetLevelDecoration assetLevelDecoration;
    public AssetFonts fonts;
    public AssetAnimations assetAnimations;

    // singleton: prevent instantiation from other classes
    private Assets() {
    }

    public void init(AssetManager assetManager) {
        this.assetManager = assetManager;
        // set asset manager error handler
        assetManager.setErrorListener(this);
        // load texture atlas
        assetManager.load(GameConstants.TEXTURE_ATLAS_OBJECTS,
                TextureAtlas.class);
        // load texture atlas
        assetManager.load(GameConstants.TEXTURE_ATLAS_SPIKE_ANIMATION,
                TextureAtlas.class);
        // start loading assets and wait until finished
        assetManager.finishLoading();
        Gdx.app.debug(TAG, "# of assets loaded: "
                + assetManager.getAssetNames().size);
        for (String a : assetManager.getAssetNames())
            Gdx.app.debug(TAG, "asset: " + a);

        TextureAtlas atlas =
                assetManager.get(GameConstants.TEXTURE_ATLAS_OBJECTS);
        // enable texture filtering for pixel smoothing
        for (Texture t : atlas.getTextures()) {
            t.setFilter(TextureFilter.Linear, TextureFilter.Linear);
        }

        TextureAtlas animationAtlas =
                assetManager.get(GameConstants.TEXTURE_ATLAS_SPIKE_ANIMATION);
        // enable texture filtering for pixel smoothing
        for (Texture t : animationAtlas.getTextures()) {
            t.setFilter(TextureFilter.Linear, TextureFilter.Linear);
        }

        fonts = new AssetFonts();
        assetParticle = new AssetParticle(atlas);
        assetHero = new AssetHero(atlas);
        assetSpike = new AssetSpike(atlas);
        assetLevelDecoration = new AssetLevelDecoration(atlas);
        assetAnimations = new AssetAnimations(animationAtlas);
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

        public AssetAnimations(TextureAtlas atlas) {
            spikeAnimation = new Animation(0.025f,atlas.getRegions());
            spikeAnimation.setPlayMode(Animation.PlayMode.LOOP);
        }
    }
}
