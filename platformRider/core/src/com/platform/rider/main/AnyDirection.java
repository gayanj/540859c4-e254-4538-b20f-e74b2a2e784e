package com.platform.rider.main;

import com.badlogic.gdx.Game;
import com.platform.rider.screens.LoadingScreen;
import com.platform.rider.utils.IActivityRequestHandler;

/**
 * Created by Gayan on 3/8/2015.
 */
public class AnyDirection extends Game {
    private static final String TAG = AnyDirection.class.getName();
    public static IActivityRequestHandler myRequestHandler;

    public AnyDirection(IActivityRequestHandler myRequestHandler) {
        AnyDirection.myRequestHandler = myRequestHandler;
    }

    @Override
    public void create() {
        /*// Set Libgdx log level
        Gdx.app.setLogLevel(Application.LOG_DEBUG);
        // Load assets
        Assets.instance.init(new AssetManager());
        // Start game at menu screen
        setScreen(new MenuScreen(this));*/

        setScreen(new LoadingScreen(this));
    }
}
