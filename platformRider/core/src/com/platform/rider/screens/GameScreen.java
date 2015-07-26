package com.platform.rider.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.platform.rider.main.AnyDirection;
import com.platform.rider.world.WorldController;
import com.platform.rider.worldRenderer.WorldRenderer;

/**
 * Created by Gayan on 3/29/2015.
 */
public class GameScreen extends AbstractGameScreen {

    private WorldController worldController;
    private WorldRenderer worldRenderer;

    public GameScreen(Game game) {
        super(game);
    }

    @Override
    public void render(float deltaTime) {
        // Do not update game world when paused.
        if (!worldController.pause) {
            // Update game world by the time that has passed
            // since last rendered frame.
            worldController.update(deltaTime);
        }
        Gdx.gl.glClearColor(212/255f, 212/255f, 212/255f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        // Render game world to screen
        worldRenderer.render();
    }

    @Override
    public void resize(int width, int height) {
        worldController.resize(width, height);
        worldRenderer.resize(width, height);
    }

    @Override
    public void show() {
        AnyDirection.myRequestHandler.showAds(false);
        // Initialize controller and renderer
        worldController = new WorldController(game);
        worldRenderer = new WorldRenderer(worldController);
        Gdx.input.setCatchBackKey(true);
    }

    @Override
    public void hide() {
        worldRenderer.dispose();
        Gdx.input.setCatchBackKey(false);
    }

    @Override
    public void pause() {
        worldController.pause = true;
    }

    @Override
    public void resume() {
        super.resume();
        // Only called on Android!
        worldController.pause = false;
    }
}
