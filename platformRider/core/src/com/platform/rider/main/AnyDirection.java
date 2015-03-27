package com.platform.rider.main;

import com.badlogic.gdx.Game;
import com.platform.rider.screens.GameScreen;

/**
 * Created by Gayan on 3/8/2015.
 */
public class AnyDirection extends Game {
    @Override
    public void create() {
        setScreen(new GameScreen());
    }
}
