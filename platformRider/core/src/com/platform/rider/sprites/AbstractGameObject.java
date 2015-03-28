package com.platform.rider.sprites;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;

/**
 * Created by Gayan on 3/28/2015.
 */
public abstract class AbstractGameObject {
    World world;
    Sprite sprite;
    TextureRegion textureRegion;
    Body body;
    BodyDef bodyDef = new BodyDef();
    PolygonShape shape = new PolygonShape();
    FixtureDef fixtureDef = new FixtureDef();
    Vector2 position = new Vector2();

    public void update(float deltaTime) {
    }

    public abstract void render(SpriteBatch batch);
}

