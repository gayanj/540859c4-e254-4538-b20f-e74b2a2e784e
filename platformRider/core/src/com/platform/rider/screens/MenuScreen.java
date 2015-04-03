package com.platform.rider.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.platform.rider.assets.Assets;

/**
 * Created by Gayan on 3/29/2015.
 */
public class MenuScreen extends AbstractGameScreen{

    private SpriteBatch batch;
    private OrthographicCamera cameraGUI;
    public MenuScreen (Game game) {
        super(game);
    }
    @Override
    public void render(float deltaTime) {
        Gdx.gl.glClearColor(1, 1, 1, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        batch.setProjectionMatrix(cameraGUI.combined);
        batch.begin();
        renderGuiPlayButton(batch);
        if(Gdx.input.justTouched())
            game.setScreen(new GameScreen(game));
        batch.end();
    }

    @Override
    public void resize(int width, int height) {

    }

    @Override
    public void show() {
        batch = new SpriteBatch();
        cameraGUI = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.
                getHeight());
        cameraGUI.position.set(0, 0, 0);
        cameraGUI.setToOrtho(true); // flip y-axis
        cameraGUI.update();
    }

    @Override
    public void hide() {
        //batch.dispose();
    }

    @Override
    public void pause() {

    }

    private void renderGuiPlayButton (SpriteBatch batch) {
        float x = cameraGUI.viewportWidth / 2;
        float y = cameraGUI.viewportHeight / 2;
        BitmapFont fontGameOver = Assets.instance.fonts.defaultBig;
        fontGameOver.drawMultiLine(batch, "PLAY", x, y, 0,
                BitmapFont.HAlignment.CENTER);
        fontGameOver.setColor(1, 1, 1, 1);
    }
}
