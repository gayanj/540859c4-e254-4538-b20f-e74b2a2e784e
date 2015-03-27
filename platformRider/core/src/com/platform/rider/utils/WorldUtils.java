package com.platform.rider.utils;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;

/**
 * Created by Gayan on 3/8/2015.
 */
public class WorldUtils {
    public static World createWorld() {
        return new World(GameConstants.WORLD_GRAVITY, true);
    }

    public static Body createGround(World world) {
        BodyDef bodyDef = new BodyDef();
        bodyDef.position.set(new Vector2(GameConstants.GROUND_X, GameConstants.GROUND_Y));
        Body body = world.createBody(bodyDef);
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(GameConstants.GROUND_WIDTH / 2, GameConstants.GROUND_HEIGHT / 2);
        body.createFixture(shape, GameConstants.GROUND_DENSITY);
        shape.dispose();
        return body;
    }
}
