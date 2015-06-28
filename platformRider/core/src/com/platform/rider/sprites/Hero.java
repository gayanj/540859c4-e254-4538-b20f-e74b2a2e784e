package com.platform.rider.sprites;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.ParticleEffectPool;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.platform.rider.assets.Assets;
import com.platform.rider.utils.GameConstants;

/**
 * Created by Gayan on 3/26/2015.
 */
public class Hero extends AbstractGameObject {
    private static final String TAG = Hero.class.getName();

    Vector2 position = new Vector2();

    ParticleEffect particleEffect = new ParticleEffect();
    ParticleEffectPool particleEffectPool;
    Array<ParticleEffectPool.PooledEffect> pooledEffects = new Array<ParticleEffectPool.PooledEffect>();

    public Hero(Vector2 position, World world) {
        init(position, world);
    }

    private void init(Vector2 position, World world) {
        this.world = world;
        this.position = position;
        textureRegion = Assets.instance.assetHero.hero;
        particleEffect.load(Gdx.files.internal("particleEffects/heroParticleEffect.p"), Gdx.files.internal("particleEffects/"));
        particleEffectPool = new ParticleEffectPool(particleEffect, 1, 2);
        sprite = new Sprite(textureRegion);
        sprite.setSize(sprite.getWidth() * GameConstants.PARTICLE_SPRITE_SCALE, sprite.getHeight()*GameConstants.PARTICLE_SPRITE_SCALE);
        sprite.setPosition(-sprite.getWidth() / 2, -sprite.getHeight() / 2);

        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set((sprite.getX() + sprite.getWidth() / 2) /
                        GameConstants.PIXELS_TO_METERS,
                (sprite.getY() + sprite.getHeight() / 2) / GameConstants.PIXELS_TO_METERS
        );

        body = world.createBody(bodyDef);
        body.setFixedRotation(true);
        body.setUserData("hero");
        body.setLinearDamping(GameConstants.LINEAR_DAMPING);
        shape.setRadius((sprite.getWidth() / 2) /
                GameConstants.PIXELS_TO_METERS);
        /*shape.setAsBox(sprite.getWidth() / 2 / GameConstants.PIXELS_TO_METERS, sprite.getHeight()
                / 2 / GameConstants.PIXELS_TO_METERS);*/

        fixtureDef.shape = shape;
        fixtureDef.density = 0.2f;
        fixtureDef.restitution = 0.5f;
        fixtureDef.filter.categoryBits = GameConstants.SPRITE_2;
        fixtureDef.filter.maskBits = GameConstants.SPRITE_1 | GameConstants.SPRITE_3 | GameConstants.SPRITE_4 | GameConstants.SPRITE_6 | GameConstants.SPRITE_7;

        body.createFixture(fixtureDef);
        shape.dispose();
    }

    public Sprite getSprite() {
        return sprite;
    }

    public void setSprite(Sprite sprite) {
        this.sprite = sprite;
    }

    public Body getBody() {
        return body;
    }

    public void setBody(Body body) {
        this.body = body;
    }

    @Override
    public void render(SpriteBatch batch) {
        //Draw Sprite
        /*batch.draw(sprite,
                sprite.getX(), sprite.getY(),
                sprite.getOriginX(),sprite.getOriginY(),
                sprite.getWidth(), sprite.getHeight(),
                sprite.getScaleX(), sprite.getScaleY(), sprite.getRotation()
        );*/
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
