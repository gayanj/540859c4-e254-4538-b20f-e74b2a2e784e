package com.particle.assassin.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.particle.assassin.world.GameScreenWorldController;
import com.particle.assassin.worldRenderer.GameScreenWorldRenderer;

/**
 * Created by Gayan on 3/29/2015.
 */
public class GameScreen extends AbstractGameScreen {

    private GameScreenWorldController gameScreenWorldController;
    private GameScreenWorldRenderer gameScreenWorldRenderer;

    public GameScreen(Game game) {
        super(game);
    }

    @Override
    public void render(float deltaTime) {
        // Do not update game world when paused.
        if (!gameScreenWorldController.pause) {
            // Update game world by the time that has passed
            // since last rendered frame.
            gameScreenWorldController.update(deltaTime);
        }
        Gdx.gl.glClearColor(212/255f, 212/255f, 212/255f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        // Render game world to screen
        gameScreenWorldRenderer.render();
    }

    @Override
    public void resize(int width, int height) {
        gameScreenWorldController.resize(width, height);
        gameScreenWorldRenderer.resize(width, height);
    }

    @Override
    public void show() {
        // Initialize controller and renderer
        gameScreenWorldController = new GameScreenWorldController(game);
        gameScreenWorldRenderer = new GameScreenWorldRenderer(gameScreenWorldController);
        Gdx.input.setCatchBackKey(true);
    }

    @Override
    public void hide() {
        gameScreenWorldRenderer.dispose();
        Gdx.input.setCatchBackKey(false);
    }

    @Override
    public void pause() {
        gameScreenWorldController.pause = true;
    }

    @Override
    public void resume() {
        super.resume();
        // Only called on Android!
        gameScreenWorldController.pause = false;
    }
}
