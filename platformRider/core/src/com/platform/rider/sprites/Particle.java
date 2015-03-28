package com.platform.rider.sprites;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.World;
import com.platform.rider.assets.Assets;
import com.platform.rider.utils.GameConstants;

/**
 * Created by Gayan on 3/26/2015.
 */
public class Particle extends AbstractGameObject {
    Vector2 normaliseVector = new Vector2();
    Vector2 position = new Vector2();
    boolean colliding = false;
    boolean remove = false;
    int collisionCount = 0;
    int counter = 0;

    public Particle(Vector2 position, World world, int number) {
        init(position, world, number);
    }

    private void init(Vector2 position, World world, int number) {
        this.world = world;
        this.position = position;
        textureRegion = Assets.instance.assetParticle.particle;
        sprite = new Sprite(textureRegion);
        sprite.setPosition(position.x / 2 / GameConstants.PIXELS_TO_METERS, position.y / 2 / GameConstants.PIXELS_TO_METERS);

        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set((sprite.getX() + sprite.getWidth() / 2) /
                        GameConstants.PIXELS_TO_METERS,
                (sprite.getY() + sprite.getHeight() / 2) / GameConstants.PIXELS_TO_METERS
        );

        body = world.createBody(bodyDef);
        body.setUserData(number);
        body.setFixedRotation(true);
        shape.setAsBox(sprite.getWidth() / 2 / GameConstants.PIXELS_TO_METERS, sprite.getHeight()
                / 2 / GameConstants.PIXELS_TO_METERS);
        //shape.setRadius(10);

        fixtureDef.shape = shape;
        fixtureDef.density = 0.1f;
        fixtureDef.restitution = 0.5f;
        fixtureDef.filter.categoryBits = GameConstants.SPRITE_1;
        fixtureDef.filter.maskBits = GameConstants.SPRITE_2 | GameConstants.SPRITE_1 | GameConstants.SPRITE_3;

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

    public int getCollisionCount() {
        return collisionCount;
    }

    public void setCollisionCount(int collisionCount) {
        this.collisionCount = collisionCount;
    }

    public boolean isRemove() {
        return remove;
    }

    public void setRemove(boolean remove) {
        this.remove = remove;
    }

    @Override
    public void render(SpriteBatch batch) {
        //Draw Sprite
        batch.draw(sprite,
                sprite.getX(), sprite.getY(),
                sprite.getOriginX(),sprite.getOriginY(),
                sprite.getWidth(), sprite.getHeight(),
                sprite.getScaleX(), sprite.getScaleY(), sprite.getRotation()
        );
    }
}
