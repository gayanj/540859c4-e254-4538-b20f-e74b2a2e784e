package com.particle.assassin.worldRenderer;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.particle.assassin.sprites.Particle;
import com.particle.assassin.sprites.ParticleBurstAnimation;
import com.particle.assassin.world.MenuScreenWorldController;

import java.util.Map;

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
        viewport = menuScreenWorldController.viewport;
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
        renderBackground();
        renderPlayButtonSaw();
        renderGuiPlayButton();
        renderParticles();
        renderAchievementButton();
        renderLeaderBoardButton();
        renderRatingButton();
        renderParticleBurst();
        renderLogo();
        batch.end();
    }

    private void renderGuiPlayButton () {
        menuScreenWorldController.playButton.render(batch);
    }

    private void renderAchievementButton(){
        menuScreenWorldController.achievementButton.render(batch);
    }

    private void renderLeaderBoardButton(){
        menuScreenWorldController.leaderBoardButton.render(batch);
    }

    private void renderRatingButton(){
        menuScreenWorldController.ratingButton.render(batch);
    }

    private void renderPlayButtonSaw(){
        menuScreenWorldController.playButtonSaw.render(batch);
    }

    private void renderLogo(){
        menuScreenWorldController.logo.render(batch);
    }

    private void renderBackground(){
        menuScreenWorldController.background.render(batch);
    }

    private void renderParticles() {
        for (Map.Entry<String, Particle> entry : menuScreenWorldController.particleHashMap.entrySet()) {
            entry.getValue().render(batch);
        }
    }

    private void renderParticleBurst() {
        for (Map.Entry<String, ParticleBurstAnimation> entry : menuScreenWorldController.particleBurstHashMap.entrySet()) {
            if (entry.getValue().isBurst()) {
                entry.getValue().render(batch);
                if (entry.getValue().getAnimatedSprite().isAnimationFinished()) {
                    entry.getValue().setBurst(false);
                    menuScreenWorldController.particleBurstsForRemoval.add(entry.getValue().getHashMapIndex());
                }
            }
        }
    }

    @Override
    public void resize(int width, int height){
        viewport.update(width, height);
    }

    @Override
    public void dispose() {

    }
}
