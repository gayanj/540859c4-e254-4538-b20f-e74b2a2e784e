package com.platform.rider.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.platform.rider.world.World;
import com.platform.rider.worldRenderer.WorldRendererOld;

import javax.microedition.khronos.opengles.GL10;

/**
 * Created by Gayan on 3/8/2015.
 */
public class GameScreen implements Screen {
    private WorldRendererOld renderer;
    @Override
    public void show() {
        World world = new World();
        renderer = new WorldRendererOld(world);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.1f, 1);
        Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
        renderer.render();
    }

    @Override
    public void resize(int width, int height) {

    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {

    }
}
