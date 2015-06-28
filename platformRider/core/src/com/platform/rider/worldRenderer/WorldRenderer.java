package com.platform.rider.worldRenderer;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.platform.rider.assets.Assets;
import com.platform.rider.sprites.*;
import com.platform.rider.utils.GameConstants;
import com.platform.rider.world.WorldController;

import java.util.Map;

/**
 * Created by Gayan on 3/23/2015.
 */
public class WorldRenderer {
    private static final String TAG = WorldRenderer.class.getName();
    private SpriteBatch batch;
    private WorldController worldController;
    private Box2DDebugRenderer b2debugRenderer;
    private Matrix4 debugMatrix;
    private OrthographicCamera cameraGUI;
    private Viewport viewport;

    public WorldRenderer(WorldController worldController) {
        this.worldController = worldController;
        init();
    }

    private void init() {
        batch = new SpriteBatch();
        b2debugRenderer = new Box2DDebugRenderer();
        cameraGUI = new OrthographicCamera(GameConstants.APP_WIDTH, GameConstants.APP_HEIGHT);
        viewport = new FitViewport(GameConstants.APP_WIDTH, GameConstants.APP_HEIGHT, cameraGUI);
        cameraGUI.position.set(0, 0, 0);
        cameraGUI.setToOrtho(true, viewport.getWorldWidth(), viewport.getWorldHeight()); // flip y-axis
        cameraGUI.update();
    }

    public void render() {
        renderWorld();
        renderGui(batch);
    }

    private void renderWorld() {
        batch.setProjectionMatrix(worldController.camera.combined);
        debugMatrix = batch.getProjectionMatrix().cpy().scale(GameConstants.PIXELS_TO_METERS,
                GameConstants.PIXELS_TO_METERS, 0);
        batch.begin();
        if (!worldController.isGameOver()) {
            worldController.hero.render(batch);
            worldController.forceField.render(batch);
        }
        renderParticles();
        renderSpikes();
        renderDeathSaws();
        renderPowerups();
        renderExplosion(batch);
        renderParticleBurst(batch);
        batch.end();
        b2debugRenderer.render(worldController.world, debugMatrix);
        worldController.touchPadHelper.render();
    }

    private void renderParticles() {
        for (Map.Entry<String, Particle> entry : worldController.particleHashMap.entrySet()) {
            entry.getValue().render(batch);
        }
    }

    private void renderSpikes() {
        for (Map.Entry<String, Saw> entry : worldController.spikeHashMap.entrySet()) {
            entry.getValue().render(batch);
        }
    }

    private void renderDeathSaws() {
        for (Map.Entry<String, DeathSaw> entry : worldController.deathSawHashMap.entrySet()) {
            entry.getValue().render(batch);
        }
    }

    private void renderPowerups() {
        for (Map.Entry<String, Power> entry : worldController.powerupHashMap.entrySet()) {
            entry.getValue().render(batch);
        }
    }

    public void resize(int width, int height) {
        viewport.update(width, height);
    }

    public void dispose() {
        //batch.dispose();
    }

    private void renderGui(SpriteBatch batch) {
        batch.setProjectionMatrix(cameraGUI.combined);
        batch.begin();
        // draw collected gold coins icon + text
        // (anchored to top left edge)
        renderGuiScore(batch);
        renderBonusSreak(batch);
        // draw game over text
        renderGuiGameOverMessage(batch);
        renderPowerButton(batch);
        renderPowerupInfo(batch);
        batch.end();
    }

    private void renderGuiScore(SpriteBatch batch) {
        float x = 0;
        float y = 0;
        Assets.instance.fonts.defaultNormal.draw(batch,
                "SCORE",
                x, y);
        Assets.instance.fonts.defaultNormal.draw(batch, "" + worldController.getScore(),
                x, y + 40);
    }

    private void renderBonusSreak(SpriteBatch batch) {
        float x = cameraGUI.viewportWidth / 2 + 300;
        float y = 0;
        if (worldController.getBonusStreak() > 0) {
            Assets.instance.fonts.defaultNormal.draw(batch,
                    "COMBO",
                    x, y);
            Assets.instance.fonts.defaultNormal.draw(batch,
                    worldController.getBonusStreak() + "X",
                    x, y + 40);
        }
    }

    private void renderGuiGameOverMessage(SpriteBatch batch) {
        float x = cameraGUI.viewportWidth / 2;
        float y = cameraGUI.viewportHeight / 2;
        if (worldController.isGameOver()) {
            BitmapFont fontGameOver = Assets.instance.fonts.defaultBig;
            fontGameOver.drawMultiLine(batch, "GAME OVER", x, y, 0,
                    BitmapFont.HAlignment.CENTER);
            fontGameOver.setColor(1, 1, 1, 1);
        }
    }

    private void renderPowerButton(SpriteBatch batch) {
        //float x = 50;
        //float y = cameraGUI.viewportHeight - Assets.instance.assetLevelDecoration.powerbutton.getRotatedPackedHeight() - 50;
        //if (!worldController.isGameOver()) {
        worldController.powerupButton.render(batch);
            /*batch.draw(Assets.instance.assetLevelDecoration.powerbutton,
                    x, y, 50, 50, 100, 100, 2, 2, 0);*/
        //}
    }

    private void renderExplosion(SpriteBatch batch) {
        for (Map.Entry<String, Explosion> entry : worldController.explosionHashMap.entrySet()) {
            if (entry.getValue().isBlast()) {
                entry.getValue().render(batch);
                if (entry.getValue().getAnimatedSprite().isAnimationFinished()) {
                    entry.getValue().setBlast(false);
                    worldController.explosionsForRemoval.add(Integer.toString(entry.getValue().getHashMapIndex()));
                }
            }
        }
    }

    private void renderParticleBurst(SpriteBatch batch) {
        for (Map.Entry<String, ParticleBurstAnimation> entry : worldController.particleBurstHashMap.entrySet()) {
            if (entry.getValue().isBurst()) {
                entry.getValue().render(batch);
                if (entry.getValue().getAnimatedSprite().isAnimationFinished()) {
                    entry.getValue().setBurst(false);
                    worldController.particleBurstsForRemoval.add(entry.getValue().getHashMapIndex());
                }
            }
        }
    }

    private void renderPowerupInfo(SpriteBatch batch) {
        float x = cameraGUI.viewportWidth / 2 + 380;
        float y = 80;
        if (worldController.powerups.isPickedUp() && worldController.powerups.getRemaining() >= 0) {
            Sprite sprite = worldController.powerups.getSprite();
            batch.draw(sprite,
                    x, y + 37,
                    sprite.getOriginX(), sprite.getOriginY(),
                    sprite.getWidth(), sprite.getHeight(),
                    sprite.getScaleX(), sprite.getScaleY(), sprite.getRotation()
            );
            Assets.instance.fonts.defaultNormal.draw(batch,
                    "X " + worldController.powerups.getRemaining(),
                    x + 60, y + 37);
            //if(worldController.powerups.isActive()) {
            int remainingTime = 10 - (worldController.powerups.getPowerCounter() / 50);
            String timer = new String(new char[remainingTime]).replace("\0", "|");
            Assets.instance.fonts.defaultSmall.draw(batch,
                    timer,
                    x + 60, y + 100);
            //}
        }
    }
}
