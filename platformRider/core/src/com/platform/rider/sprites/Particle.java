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
    float speed;
    Vector2 normaliseVector = new Vector2();
    Vector2 position = new Vector2();
    boolean colliding = false;
    boolean splitParticle = false;
    boolean remove = false;
    int splitParticleCount = 0;
    int collisionCount = 0;
    int counter = 0;
    int blastTimer = 0;
    String type;

    public Particle(Vector2 position, World world, int number, String type) {
        init(position, world, number, type);
    }

    private void init(Vector2 position, World world, int number, String type) {
        this.world = world;
        this.position = position;
        this.type = type;
        if("split_particle".equals(type)){
            textureRegion = Assets.instance.assetParticle.split_particle;
            speed = GameConstants.SPLIT_PARTICAL_SPEED;
        }else if("normal_particle".equals(type)) {
            textureRegion = Assets.instance.assetParticle.particle;
            speed = GameConstants.NORMAL_PARTICAL_SPEED;
        }else if("suicide_particle".equals(type)) {
            textureRegion = Assets.instance.assetParticle.suicide_particle;
            speed = GameConstants.SUICIDE_PARTICAL_SPEED;
        }
        sprite = new Sprite(textureRegion);
        sprite.setSize(sprite.getWidth() * GameConstants.PARTICLE_SPRITE_SCALE, sprite.getHeight()*GameConstants.PARTICLE_SPRITE_SCALE);
        sprite.setPosition(-sprite.getWidth() / 2 + position.x,-sprite.getHeight() / 2 + position.y);

        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set((sprite.getX() + sprite.getWidth() / 2) /
                        GameConstants.PIXELS_TO_METERS,
                (sprite.getY() + sprite.getHeight() / 2) / GameConstants.PIXELS_TO_METERS
        );

        body = world.createBody(bodyDef);
        body.setUserData(number);
        body.setFixedRotation(true);
        /*shape.setAsBox(sprite.getWidth() / 2 / GameConstants.PIXELS_TO_METERS, sprite.getHeight()
                / 2 / GameConstants.PIXELS_TO_METERS);*/
        shape.setRadius((sprite.getWidth() / 2) /
                GameConstants.PIXELS_TO_METERS);
        fixtureDef.shape = shape;
        fixtureDef.density = 0.1f;
        fixtureDef.restitution = 0.1f;
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

    public boolean isSplitParticle() {
        return splitParticle;
    }

    public void setSplitParticle(boolean splitParticle) {
        this.splitParticle = splitParticle;
    }

    public int getSplitParticleCount() {
        return splitParticleCount;
    }

    public void setSplitParticleCount(int splitParticleCount) {
        this.splitParticleCount = splitParticleCount;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public int getBlastTimer() {
        return blastTimer;
    }

    public void setBlastTimer(int blastTimer) {
        this.blastTimer = blastTimer;
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
