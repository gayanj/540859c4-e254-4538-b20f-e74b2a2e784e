package com.platform.rider.sprites;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;

/**
 * Created by Gayan on 3/26/2015.
 */
public class Particle {
    World world;
    Sprite sprite;
    Texture img;
    Body body;
    BodyDef bodyDef = new BodyDef();
    PolygonShape shape = new PolygonShape();
    FixtureDef fixtureDef = new FixtureDef();
    Vector2 normaliseVector = new Vector2();
    Vector2 position = new Vector2();
    boolean colliding = false;
    int counter = 0;

    final float SPEED = 50f;
    final float COLLISION_SPEED = 100f;
    final float PIXELS_TO_METERS = 10f;
    final short SPRITE_1 = 0x1;    // 0001
    final short SPRITE_2 = 0x1 << 1; // 0010 or 0x2 in hex

    public Particle(Vector2 position, World world, int number){
        this.world = world;
        this.position = position;
        img = new Texture("particle.png");

        sprite = new Sprite(img);
        sprite.setPosition(-sprite.getWidth() / 2 + position.x, -sprite.getHeight() / 2 + position.y);

        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set((sprite.getX() + sprite.getWidth() / 2) /
                        PIXELS_TO_METERS,
                (sprite.getY() + sprite.getHeight() / 2) / PIXELS_TO_METERS
        );

        body = world.createBody(bodyDef);
        body.setUserData(number);
        body.setFixedRotation(true);
        shape.setAsBox(sprite.getWidth() / 2 / PIXELS_TO_METERS, sprite.getHeight()
                / 2 / PIXELS_TO_METERS);
        //shape.setRadius(10);

        fixtureDef.shape = shape;
        fixtureDef.density = 0.1f;
        fixtureDef.restitution = 0.5f;
        fixtureDef.filter.categoryBits = SPRITE_1;
        fixtureDef.filter.maskBits = SPRITE_2|SPRITE_1;

        body.createFixture(fixtureDef);
        shape.dispose();
    }

    public int getCounter() {
        return counter;
    }

    public void setCounter(int counter) {
        this.counter = counter;
    }

    public boolean isColliding() {
        return colliding;
    }

    public void setColliding(boolean colliding) {
        this.colliding = colliding;
    }

    public Vector2 getPosition() {
        return position;
    }

    public void setPosition(Vector2 position) {
        this.position = position;
    }

    public Vector2 getNormaliseVector() {
        return normaliseVector;
    }

    public void setNormaliseVector(Vector2 normaliseVector) {
        this.normaliseVector = normaliseVector;
    }

    public Body getBody() {
        return body;
    }

    public void setBody(Body body) {
        this.body = body;
    }

    public Sprite getSprite() {
        return sprite;
    }

    public void setSprite(Sprite sprite) {
        this.sprite = sprite;
    }
}
