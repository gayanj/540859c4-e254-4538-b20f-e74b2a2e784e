package com.particle.assassin.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.particle.assassin.main.AnyDirection;
import com.particle.assassin.utils.GamePreferences;
import com.particle.assassin.world.MenuScreenWorldController;
import com.particle.assassin.worldRenderer.MenuScreenWorldRenderer;

/**
 * Created by Gayan on 3/29/2015.
 */
public class MenuScreen extends AbstractGameScreen {

    MenuScreenWorldController menuScreenWorldController;
    MenuScreenWorldRenderer menuScreenWorldRenderer;
    private boolean paused;

    public MenuScreen(Game game) {
        super(game);
    }

    @Override
    public void render(float deltaTime) {
        if (!paused) {
            menuScreenWorldController.update(deltaTime);
            Gdx.gl.glClearColor(212 / 255f, 212 / 255f, 212 / 255f, 1);
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
            menuScreenWorldRenderer.render();
        }
    }

    @Override
    public void resize(int width, int height) {
        menuScreenWorldController.resize(width, height);
        menuScreenWorldRenderer.resize(width, height);
    }

    @Override
    public void show() {
        GamePreferences.instance.load();
        AnyDirection.myRequestHandler.signIn();
        menuScreenWorldController = new MenuScreenWorldController(game);
        menuScreenWorldRenderer = new MenuScreenWorldRenderer(menuScreenWorldController);
    }

    @Override
    public void hide() {
        menuScreenWorldRenderer.dispose();
    }

    @Override
    public void pause() {
        paused = true;
    }

    @Override
    public void resume() {
        super.resume();
        // Only called on Android!
        paused = false;
    }
}
