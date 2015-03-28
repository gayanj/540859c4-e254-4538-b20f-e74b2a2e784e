package com.platform.rider.main;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.GL20;
import com.platform.rider.assets.Assets;
import com.platform.rider.world.WorldController;
import com.platform.rider.worldRenderer.WorldRenderer;

/**
 * Created by Gayan on 3/8/2015.
 */
public class AnyDirection implements ApplicationListener {

    private static final String TAG = AnyDirection.class.getName();

    private WorldController worldController;
    private WorldRenderer worldRenderer;
    private boolean paused;

    @Override
    public void create() {
        // Set Libgdx log level to DEBUG
        Gdx.app.setLogLevel(Application.LOG_DEBUG);
        // Load assets
        Assets.instance.init(new AssetManager());
        // Initialize controller and renderer
        worldController = new WorldController();
        worldRenderer = new WorldRenderer(worldController);
        // Game world is active on start
        paused = false;
    }

    @Override
    public void resize(int width, int height) {
        worldRenderer.resize(width, height);
    }

    @Override
    public void render() {
        // Do not update game world when paused.
        if (!paused) {
            // Update game world by the time that has passed
            // since last rendered frame.
            worldController.update(Gdx.graphics.getDeltaTime());
        }
        Gdx.gl.glClearColor(1, 1, 1, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        // Render game world to screen
        worldRenderer.render();
    }

    @Override
    public void pause() {
        paused = true;
    }

    @Override
    public void resume() {
        Assets.instance.init(new AssetManager());
        paused = false;
    }

    @Override
    public void dispose() {
        worldRenderer.dispose();
        Assets.instance.dispose();
    }
}
