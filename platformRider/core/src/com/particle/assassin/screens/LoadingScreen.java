package com.particle.assassin.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGeneratorLoader;
import com.badlogic.gdx.graphics.g2d.freetype.FreetypeFontLoader;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.particle.assassin.assets.Assets;
import com.particle.assassin.utils.GameConstants;
import com.particle.assassin.utils.LoadingBar;

/**
 * @author Mats Svensson
 */
public class LoadingScreen extends AbstractGameScreen {

    AssetManager assetManager;

    private Stage stage;

    private Image logo;
    private Image loadingFrame;
    private Image loadingBarHidden;
    private Image screenBg;
    private Image loadingBg;

    private float startX, endX;
    private float percent;

    private Actor loadingBar;

    public LoadingScreen(Game game) {
        super(game);
        this.assetManager = Assets.instance.getAssetManager(new AssetManager());
    }

    @Override
    public void show() {
        // Tell the manager to load assets for the loading screen
        assetManager.load("data/loading.pack", TextureAtlas.class);
        assetManager.load("data/logo.txt", TextureAtlas.class);
        // Wait until they are finished loading
        assetManager.finishLoading();

        // Initialize the stage where we will place everything
        stage = new Stage();

        // Get our textureatlas from the manager
        TextureAtlas atlas = assetManager.get("data/loading.pack", TextureAtlas.class);
        TextureAtlas atlas2 = assetManager.get("data/logo.txt", TextureAtlas.class);

        // Grab the regions from the atlas and create some images
        logo = new Image(atlas2.findRegion("Splashscreentext"));
        loadingFrame = new Image(atlas.findRegion("loading-frame"));
        loadingBarHidden = new Image(atlas.findRegion("loading-bar-hidden"));
        screenBg = new Image(atlas.findRegion("screen-bg"));
        loadingBg = new Image(atlas.findRegion("loading-frame-bg"));

        // Add the loading bar animation
        Animation anim = new Animation(0.05f, atlas.findRegions("loading-bar-anim"));
        anim.setPlayMode(PlayMode.LOOP_REVERSED);
        loadingBar = new LoadingBar(anim);

        // Or if you only need a static bar, you can do
        // loadingBar = new Image(atlas.findRegion("loading-bar1"));

        // Add all the actors to the stage
        stage.addActor(screenBg);
        stage.addActor(loadingBar);
        stage.addActor(loadingBg);
        stage.addActor(loadingBarHidden);
        stage.addActor(loadingFrame);
        stage.addActor(logo);

        // Add everything to be loaded, for instance:
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
        assetManager.load(GameConstants.TEXTURE_ATLAS_PLAY_BUTTON_SAW_ANIMATION,
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
        // load texture atlas
        assetManager.load(GameConstants.TEXTURE_ATLAS_TUTORIAL_ARROW_ANIMATION,
                TextureAtlas.class);
        // load sound effects
        assetManager.load(GameConstants.PARTICLE_DEATH_SOUND,
                Sound.class);
        assetManager.load(GameConstants.HERO_DEATH_SOUND,
                Sound.class);
        assetManager.load(GameConstants.PICKUP_SOUND,
                Sound.class);
        assetManager.load(GameConstants.BOMB_SOUND,
                Sound.class);
        assetManager.load(GameConstants.ARMOR_SOUND,
                Sound.class);
        assetManager.load(GameConstants.SPEED_SOUND,
                Sound.class);
        assetManager.load(GameConstants.ENERGY_SOUND,
                Sound.class);
        assetManager.load(GameConstants.INVINCIBLE_SOUND,
                Sound.class);
        assetManager.load(GameConstants.ALERT_SOUND,
                Music.class);
        assetManager.load(GameConstants.BACKGROUND_MUSIC,
                Music.class);
        assetManager.load(GameConstants.BACKGROUND_MUSIC2,
                Music.class);
        assetManager.load(GameConstants.BACKGROUND_MUSIC3,
                Music.class);
        assetManager.load(GameConstants.BACKGROUND_MUSIC4,
                Music.class);
        assetManager.load(GameConstants.BACKGROUND_MUSIC5,
                Music.class);
        assetManager.load(GameConstants.MENU_MUSIC,
                Music.class);
        //Kill streak sounds
        assetManager.load(GameConstants.KILLINGSPREE,
                Sound.class);
        assetManager.load(GameConstants.DOMINATING,
                Sound.class);
        assetManager.load(GameConstants.MEGAKILL,
                Sound.class);
        assetManager.load(GameConstants.UNSTOPPABLE,
                Sound.class);
        assetManager.load(GameConstants.WICKEDSICK,
                Sound.class);
        assetManager.load(GameConstants.MONSTERKILL,
                Sound.class);
        assetManager.load(GameConstants.GODLIKE,
                Sound.class);
        assetManager.load(GameConstants.ULTRAKILL,
                Sound.class);
        assetManager.load(GameConstants.RAMPAGE,
                Sound.class);
        assetManager.load(GameConstants.HOLYSHIT,
                Sound.class);

        FileHandleResolver resolver = new InternalFileHandleResolver();
        assetManager.setLoader(FreeTypeFontGenerator.class, new FreeTypeFontGeneratorLoader(resolver));
        assetManager.setLoader(BitmapFont.class, ".otf", new FreetypeFontLoader(resolver));

        FreetypeFontLoader.FreeTypeFontLoaderParameter size1Params = new FreetypeFontLoader.FreeTypeFontLoaderParameter();
        size1Params.fontFileName = "fonts/BebasNeue.otf";
        size1Params.fontParameters.size = 65;
        size1Params.fontParameters.color = new Color(189 / 255f, 26 / 255f, 24 / 255f, 1);
        assetManager.load("bebasSmall.otf", BitmapFont.class, size1Params);

        FreetypeFontLoader.FreeTypeFontLoaderParameter size2Params = new FreetypeFontLoader.FreeTypeFontLoaderParameter();
        size2Params.fontFileName = "fonts/BebasNeue.otf";
        size2Params.fontParameters.size = 200;
        size2Params.fontParameters.color = new Color(189 / 255f, 26 / 255f, 24 / 255f, 1);
        assetManager.load("bebasBig.otf", BitmapFont.class, size2Params);
    }

    @Override
    public void resize(int width, int height) {
        // Set our screen to always be XXX x 480 in size
        /*width = 480 * width / height;
        height = 480;*/
        stage.getViewport().update(width, height, false);

        // Make the background fill the screen
        screenBg.setSize(width, height);

        // Place the logo in the middle of the screen and 100 px up
        logo.setX((width - logo.getWidth()) / 2);
        logo.setY((height - logo.getHeight()) / 2 + 100);

        // Place the loading frame in the middle of the screen
        loadingFrame.setX((stage.getWidth() - loadingFrame.getWidth()) / 2);
        loadingFrame.setY((stage.getHeight() - loadingFrame.getHeight()) / 2);

        // Place the loading bar at the same spot as the frame, adjusted a few px
        loadingBar.setX(loadingFrame.getX() + 15);
        loadingBar.setY(loadingFrame.getY() + 5);

        // Place the image that will hide the bar on top of the bar, adjusted a few px
        loadingBarHidden.setX(loadingBar.getX() + 35);
        loadingBarHidden.setY(loadingBar.getY() - 3);
        // The start position and how far to move the hidden loading bar
        startX = loadingBarHidden.getX();
        endX = 440;

        // The rest of the hidden bar
        loadingBg.setSize(450, 50);
        loadingBg.setX(loadingBarHidden.getX() + 30);
        loadingBg.setY(loadingBarHidden.getY() + 3);
    }

    @Override
    public void render(float delta) {
        // Clear the screen
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (assetManager.update()) { // Load some, will return true if done loading
            //if (Gdx.input.isTouched()) { // If the screen is touched after the game is done loading, go to the main menu screen
            Assets.instance.init();
            game.setScreen(new MenuScreen(game));
            //}
        }

        // Interpolate the percentage to make it more smooth
        percent = Interpolation.linear.apply(percent, assetManager.getProgress(), 0.1f);

        // Update positions (and size) to match the percentage
        loadingBarHidden.setX(startX + endX * percent);
        loadingBg.setX(loadingBarHidden.getX() + 30);
        loadingBg.setWidth(450 - 450 * percent);
        loadingBg.invalidate();

        // Show the loading screen
        stage.act();
        stage.draw();
    }

    @Override
    public void hide() {
        // Dispose the loading assets as we no longer need them
        assetManager.unload("data/loading.pack");
        assetManager.unload("data/logo.txt");
    }

    @Override
    public void pause() {

    }
}
