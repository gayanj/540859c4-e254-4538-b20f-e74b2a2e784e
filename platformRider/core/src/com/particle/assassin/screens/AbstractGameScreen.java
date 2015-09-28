package com.particle.assassin.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.particle.assassin.assets.Assets;

/**
 * Created by Gayan on 3/29/2015.
 */
public abstract class AbstractGameScreen implements Screen {
    protected Game game;
    public AbstractGameScreen (Game game) {
        this.game = game;
    }
    public abstract void render (float deltaTime);
    public abstract void resize (int width, int height);
    public abstract void show ();
    public abstract void hide ();
    public abstract void pause ();
    public void resume () {
        Assets.instance.getAssetManager(new AssetManager());
        //setScreen(new LoadingScreen(this));
        //Assets.instance.init();
    }
    public void dispose () {
        Assets.instance.dispose();
    }
}
