package com.platform.rider.worldRenderer;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.platform.rider.assets.Assets;
import com.platform.rider.sprites.Explosion;
import com.platform.rider.sprites.Particle;
import com.platform.rider.sprites.Saw;
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
        viewport = new FitViewport(1080, 1920, cameraGUI);
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
        }
        renderParticles();
        renderSpikes();
        renderExplosion(batch);
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
        // draw game over text
        renderGuiGameOverMessage(batch);
        //renderPowerButton(batch);
        batch.end();
    }

    private void renderGuiScore(SpriteBatch batch) {
        float x = 15;
        float y = 15;
        batch.draw(Assets.instance.assetParticle.particle,
                x, y, 50, 50, 100, 100, 0.35f, -0.35f, 0);
        Assets.instance.fonts.defaultNormal.draw(batch,
                "" + worldController.totalParticlesDestroyed,
                x + 75, y + 37);
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
        float x = 15;
        float y = cameraGUI.viewportHeight - Assets.instance.assetLevelDecoration.powerbutton.getRotatedPackedHeight();
        if (!worldController.isGameOver()) {
            batch.draw(Assets.instance.assetLevelDecoration.powerbutton,
                    x, y, 50, 50, 100, 100, 1, 1, 0);
        }
    }

    private void renderExplosion(SpriteBatch batch) {
        for (Map.Entry<String, Explosion> entry : worldController.explosionHashMap.entrySet()) {
            if (entry.getValue().isBlast()) {
                entry.getValue().getAnimatedSprite().setPosition((entry.getValue().getBlastPosition().x * GameConstants.PIXELS_TO_METERS) - entry.getValue().getAnimatedSprite().
                                getWidth() / 2,
                        (entry.getValue().getBlastPosition().y * GameConstants.PIXELS_TO_METERS) - entry.getValue().getAnimatedSprite().getHeight() / 2
                );
                entry.getValue().render(batch);
                if (entry.getValue().getAnimatedSprite().isAnimationFinished()) {
                    entry.getValue().setBlast(false);
                    worldController.explosionsForRemoval.add(Integer.toString(entry.getValue().getHashMapIndex()));
                }
            }
        }
    }
}
