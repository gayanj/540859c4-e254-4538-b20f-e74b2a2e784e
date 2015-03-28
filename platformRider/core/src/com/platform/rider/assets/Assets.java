package com.platform.rider.assets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetErrorListener;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.utils.Disposable;
import com.platform.rider.utils.GameConstants;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;

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

        assetParticle = new AssetParticle(atlas);
        assetHero = new AssetHero(atlas);
        assetSpike = new AssetSpike(atlas);
        assetLevelDecoration = new AssetLevelDecoration(atlas);
    }

    @Override
    public void dispose() {
        assetManager.dispose();
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
        public AssetParticle (TextureAtlas atlas) {
            particle = atlas.findRegion("particle");
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
        public final TextureAtlas.AtlasRegion background;

        public AssetLevelDecoration (TextureAtlas atlas) {
            background = atlas.findRegion("background");

        }
    }
}
