package com.platform.rider.worldRenderer;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.platform.rider.utils.GameConstants;
import com.platform.rider.world.MenuScreenWorldController;

/**
 * Created by Gayan on 8/3/2015.
 */
public class MenuScreenWorldRenderer implements WorldRendererInterface {
    private SpriteBatch batch;
    private MenuScreenWorldController menuScreenWorldController;
    private OrthographicCamera cameraGUI;
    private Viewport viewport;

    public MenuScreenWorldRenderer(MenuScreenWorldController menuScreenWorldController) {
        this.menuScreenWorldController = menuScreenWorldController;
        init();
    }

    @Override
    public void init() {
        batch = new SpriteBatch();
        cameraGUI = menuScreenWorldController.cameraGUI;
        viewport = new FitViewport(GameConstants.APP_WIDTH, GameConstants.APP_HEIGHT, cameraGUI);
        cameraGUI.position.set(0, 0, 0);
        cameraGUI.setToOrtho(false, viewport.getWorldWidth(), viewport.getWorldHeight()); // flip y-axis
        cameraGUI.update();
    }

    @Override
    public void render() {
        renderWorld();
    }

    private void renderWorld() {
        batch.setProjectionMatrix(menuScreenWorldController.cameraGUI.combined);
        batch.begin();
        renderPlayButtonSaw();
        renderGuiPlayButton();
        batch.end();
    }

    private void renderGuiPlayButton () {
        menuScreenWorldController.playButton.render(batch);
    }

    private void renderPlayButtonSaw(){
        menuScreenWorldController.playButtonSaw.render(batch);
    }

    @Override
    public void resize(int width, int height){
        viewport.update(width, height);
    }

    @Override
    public void dispose() {

    }
}
