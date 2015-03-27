package com.platform.rider.worldRenderer;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.platform.rider.sprites.Ball;
import com.platform.rider.sprites.Platform;
import com.platform.rider.world.World;

import java.util.ArrayList;

/**
 * Created by Gayan on 3/8/2015.
 */
public class WorldRendererOld {
    private World world;
    private OrthographicCamera cam;

    /** for debug rendering **/
    ShapeRenderer debugRenderer = new ShapeRenderer();

    public WorldRendererOld(World world) {
        this.world = world;
        this.cam = new OrthographicCamera(10, 7);
        this.cam.position.set(5, 3.5f, 0);
        this.cam.update();
    }

    public void render() {
        // render blocks
        debugRenderer.setProjectionMatrix(cam.combined);
        debugRenderer.begin(ShapeRenderer.ShapeType.Line);
        ArrayList<Platform> p = world.getPlatforms();
        for (Platform block : p) {
            Rectangle rect = block.getBounds();
            float x1 = block.getPosition().x + rect.x;
            float y1 = block.getPosition().y + rect.y;
            debugRenderer.setColor(new Color(1, 0, 0, 1));
            debugRenderer.rect(x1, y1, rect.width, rect.height);
        }
        // render Ball
        Ball bob = world.getBall();
        Rectangle rect = bob.getBounds();
        float x1 = bob.getPosition().x + rect.x;
        float y1 = bob.getPosition().y + rect.y;
        debugRenderer.setColor(new Color(0, 1, 0, 1));
        debugRenderer.rect(x1, y1, rect.width, rect.height);
        debugRenderer.end();
    }
}
