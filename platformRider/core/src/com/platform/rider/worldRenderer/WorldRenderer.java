package com.platform.rider.worldRenderer;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.utils.Disposable;
import com.platform.rider.sprites.Particle;
import com.platform.rider.sprites.Saw;
import com.platform.rider.utils.GameConstants;
import com.platform.rider.world.WorldController;

import java.util.Map;

/**
 * Created by Gayan on 3/23/2015.
 */
public class WorldRenderer implements Disposable {
    private static final String TAG = WorldRenderer.class.getName();
    private SpriteBatch batch;
    private WorldController worldController;
    private Box2DDebugRenderer b2debugRenderer;
    private Matrix4 debugMatrix;

    public WorldRenderer(WorldController worldController) {
        this.worldController = worldController;
        init();
    }

    private void init() {
        batch = new SpriteBatch();
        b2debugRenderer = new Box2DDebugRenderer();
    }

    public void render() {
        renderWorld();
    }

    private void renderWorld() {
        batch.setProjectionMatrix(worldController.camera.combined);
        debugMatrix = batch.getProjectionMatrix().cpy().scale(GameConstants.PIXELS_TO_METERS,
                GameConstants.PIXELS_TO_METERS, 0);
        batch.begin();
        worldController.hero.render(batch);
        renderParticles();
        renderSpikes();
        batch.end();
        b2debugRenderer.render(worldController.world, debugMatrix);
        worldController.touchPadHelper.render();
    }

    private void renderParticles(){
        for (Map.Entry<String, Particle> entry : worldController.particleHashMap.entrySet()) {
            entry.getValue().render(batch);
        }
    }

    private void renderSpikes(){
        for(Saw saw:worldController.saws){
            saw.render(batch);
        }
    }

    public void resize(int width, int height) {
        /*worldController.camera.viewportWidth = (GameConstants.APP_HEIGHT / height) *
                width;
        worldController.camera.update();*/
    }

    @Override
    public void dispose() {
        batch.dispose();
    }
}
