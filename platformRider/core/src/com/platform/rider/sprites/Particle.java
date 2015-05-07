package com.platform.rider.sprites;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.ParticleEffectPool;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.platform.rider.assets.Assets;
import com.platform.rider.utils.GameConstants;
import net.dermetfan.gdx.graphics.g2d.AnimatedSprite;

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
    boolean invisible = true;
    boolean vulnerable = false;
    int splitParticleCount = 0;
    int invisibleCount = 0;
    int counter = 0;
    int blastTimer = 0;
    float frameDuration = 0.025f;
    String type;

    ParticleEffect particleEffect = new ParticleEffect();
    ParticleEffectPool particleEffectPool;
    Array<ParticleEffectPool.PooledEffect> pooledEffects = new Array<ParticleEffectPool.PooledEffect>();

    public Particle(Vector2 position, World world, int number, String type) {
        init(position, world, number, type);
    }

    private void init(Vector2 position, World world, int number, String type) {
        this.world = world;
        this.position = position;
        this.type = type;
        if (GameConstants.SPLIT_PARTICLE.equals(type)) {
            particleEffect.load(Gdx.files.internal("particleEffects/splitParticleEffect.p"), Gdx.files.internal("particleEffects/"));
            particleEffectPool = new ParticleEffectPool(particleEffect, 1, 2);
            textureRegion = Assets.instance.assetParticle.split_particle;
            speed = GameConstants.SPLIT_PARTICAL_SPEED;
        } else if (GameConstants.NORMAL_PARTICLE.equals(type)) {
            particleEffect.load(Gdx.files.internal("particleEffects/normalParticleEffect.p"), Gdx.files.internal("particleEffects/"));
            particleEffectPool = new ParticleEffectPool(particleEffect, 1, 2);
            textureRegion = Assets.instance.assetParticle.particle;
            speed = GameConstants.NORMAL_PARTICAL_SPEED;
        } else if (GameConstants.SUICIDE_PARTICLE.equals(type)) {
            particleEffect.load(Gdx.files.internal("particleEffects/suicideParticleEffect.p"), Gdx.files.internal("particleEffects/"));
            particleEffectPool = new ParticleEffectPool(particleEffect, 1, 2);
            animatedSprite = new AnimatedSprite(Assets.instance.assetAnimations.suicideParticleAnimation);
            speed = GameConstants.SUICIDE_PARTICAL_SPEED;
        } else if (GameConstants.INVISIBLE_PARTICLE.equals(type)) {
            animatedSprite = new AnimatedSprite(Assets.instance.assetAnimations.invisibleParticleAppearingAnimation);
            speed = GameConstants.INVISIBLE_PARTICLE_SPEED;
        }
        if (GameConstants.SUICIDE_PARTICLE.equals(type) || GameConstants.INVISIBLE_PARTICLE.equals(type)) {
            animatedSprite.setSize(animatedSprite.getWidth() * GameConstants.PARTICLE_SPRITE_SCALE, animatedSprite.getHeight() * GameConstants.PARTICLE_SPRITE_SCALE);
            animatedSprite.setPosition(-animatedSprite.getWidth() / 2 + position.x, -animatedSprite.getHeight() / 2 + position.y);
            bodyDef.type = BodyDef.BodyType.DynamicBody;
            bodyDef.position.set((animatedSprite.getX() + animatedSprite.getWidth() / 2) /
                            GameConstants.PIXELS_TO_METERS,
                    (animatedSprite.getY() + animatedSprite.getHeight() / 2) / GameConstants.PIXELS_TO_METERS
            );

            body = world.createBody(bodyDef);
            body.setUserData(number);
            body.setFixedRotation(true);
            shape.setRadius((animatedSprite.getWidth() / 2) /
                    GameConstants.PIXELS_TO_METERS);
        } else {
            sprite = new Sprite(textureRegion);
            sprite.setSize(sprite.getWidth() * GameConstants.PARTICLE_SPRITE_SCALE, sprite.getHeight() * GameConstants.PARTICLE_SPRITE_SCALE);
            sprite.setPosition(-sprite.getWidth() / 2 + position.x, -sprite.getHeight() / 2 + position.y);

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
        }

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

    public Sprite getSprite() {
        return sprite;
    }

    public void setSprite(Sprite sprite) {
        this.sprite = sprite;
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

    public int getBlastTimer() {
        return blastTimer;
    }

    public void setBlastTimer(int blastTimer) {
        this.blastTimer = blastTimer;
    }

    public AnimatedSprite getAnimatedSprite() {
        return animatedSprite;
    }

    public ParticleEffect getParticleEffect() {
        return particleEffect;
    }

    public void setParticleEffect(ParticleEffect particleEffect) {
        this.particleEffect = particleEffect;
    }

    public ParticleEffectPool getParticleEffectPool() {
        return particleEffectPool;
    }

    public void setParticleEffectPool(ParticleEffectPool particleEffectPool) {
        this.particleEffectPool = particleEffectPool;
    }

    public Array<ParticleEffectPool.PooledEffect> getPooledEffects() {
        return pooledEffects;
    }

    public void setPooledEffects(Array<ParticleEffectPool.PooledEffect> pooledEffects) {
        this.pooledEffects = pooledEffects;
    }

    public void setAnimatedSprite(AnimatedSprite animatedSprite) {
        this.animatedSprite = animatedSprite;
    }

    public void updateFrameDuration() {
        this.animatedSprite.getAnimation().setFrameDuration(frameDuration -= 0.000025f);
    }

    private void changeVisibility() {
        invisible = !invisible;
    }

    private void changeCollisionFilter(short filterType) {
        Filter filter = body.getFixtureList().get(0).getFilterData();
        filter.categoryBits = filterType;
        body.getFixtureList().get(0).setFilterData(filter);
    }

    @Override
    public void render(SpriteBatch batch) {
        //Draw Sprite
        if (GameConstants.INVISIBLE_PARTICLE.equals(type)) {
            if (invisibleCount < 300) {
                if (invisible) {
                    animatedSprite.setAnimation(Assets.instance.assetAnimations.invisibleParticleAppearingAnimation);
                    animatedSprite.draw(batch);
                    if (animatedSprite.isAnimationFinished()) {
                        animatedSprite.stop();
                    }
                    if (!animatedSprite.isPlaying()) {
                        vulnerable = false;
                        changeCollisionFilter(GameConstants.SPRITE_4);
                    } else {
                        vulnerable = true;
                        changeCollisionFilter(GameConstants.SPRITE_1);
                    }
                } else {
                    animatedSprite.setAnimation(Assets.instance.assetAnimations.invisibleParticleDisappearingAnimation);
                    animatedSprite.draw(batch);
                    if (animatedSprite.isAnimationFinished()) {
                        animatedSprite.stop();
                    }
                    if (!animatedSprite.isPlaying()) {
                        vulnerable = false;
                        animatedSprite.stop();
                        changeCollisionFilter(GameConstants.SPRITE_5);
                    } else {
                        vulnerable = true;
                        changeCollisionFilter(GameConstants.SPRITE_1);
                    }
                }
                invisibleCount++;
            } else {
                invisibleCount = 0;
                changeVisibility();
                animatedSprite.play();
            }
        } else if (GameConstants.SUICIDE_PARTICLE.equals(type)) {
            //Draw Particle Effect
            ParticleEffectPool.PooledEffect effect = particleEffectPool.obtain();
            effect.setPosition(animatedSprite.getX() + animatedSprite.getWidth() / 2, animatedSprite.getY() + animatedSprite.getHeight() / 2);
            pooledEffects.add(effect);

            for (int i = pooledEffects.size - 1; i >= 0; i--) {
                ParticleEffectPool.PooledEffect peffect = pooledEffects.get(i);
                peffect.draw(batch, Gdx.graphics.getDeltaTime());
                if (peffect.isComplete()) {
                    peffect.free();
                    pooledEffects.removeIndex(i);
                }
            }
            //animatedSprite.draw(batch);
        } else if (GameConstants.NORMAL_PARTICLE.equals(type)) {
            //Draw Particle Effect
            ParticleEffectPool.PooledEffect effect = particleEffectPool.obtain();
            effect.setPosition(sprite.getX() + sprite.getWidth() / 2, sprite.getY() + sprite.getHeight() / 2);
            pooledEffects.add(effect);

            for (int i = pooledEffects.size - 1; i >= 0; i--) {
                ParticleEffectPool.PooledEffect peffect = pooledEffects.get(i);
                peffect.draw(batch, Gdx.graphics.getDeltaTime());
                if (peffect.isComplete()) {
                    peffect.free();
                    pooledEffects.removeIndex(i);
                }
            }
            /*batch.draw(sprite,
                    sprite.getX(), sprite.getY(),
                    sprite.getOriginX(), sprite.getOriginY(),
                    sprite.getWidth(), sprite.getHeight(),
                    sprite.getScaleX(), sprite.getScaleY(), sprite.getRotation()
            );*/
        } else if (GameConstants.SPLIT_PARTICLE.equals(type)) {
            //Draw Particle Effect
            ParticleEffectPool.PooledEffect effect = particleEffectPool.obtain();
            effect.setPosition(sprite.getX() + sprite.getWidth() / 2, sprite.getY() + sprite.getHeight() / 2);
            pooledEffects.add(effect);

            for (int i = pooledEffects.size - 1; i >= 0; i--) {
                ParticleEffectPool.PooledEffect peffect = pooledEffects.get(i);
                peffect.draw(batch, Gdx.graphics.getDeltaTime());
                if (peffect.isComplete()) {
                    peffect.free();
                    pooledEffects.removeIndex(i);
                }
            }
        }
    }
}
