package com.platform.rider.main;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.platform.rider.assets.Assets;
import com.platform.rider.screens.MenuScreen;

/**
 * Created by Gayan on 3/8/2015.
 */
public class AnyDirection extends Game {
    private static final String TAG = AnyDirection.class.getName();

    @Override
    public void create() {
        // Set Libgdx log level
        Gdx.app.setLogLevel(Application.LOG_DEBUG);
        // Load assets
        Assets.instance.init(new AssetManager());
        // Start game at menu screen
        setScreen(new MenuScreen(this));
    }
}
