package com.particle.assassin.sprites;

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
import com.particle.assassin.assets.Assets;
import com.particle.assassin.utils.GameConstants;

/**
 * Created by Gayan on 3/26/2015.
 */
public class Hero extends AbstractGameObject {

    Vector2 position = new Vector2();
    int energy;
    boolean invincible = false;

    ParticleEffect particleEffect = new ParticleEffect();
    ParticleEffectPool particleEffectPool;

    ParticleEffect invincibleParticleEffect = new ParticleEffect();
    ParticleEffectPool invincibleParticleEffectPool;

    Array<ParticleEffectPool.PooledEffect> pooledEffects = new Array<ParticleEffectPool.PooledEffect>();

    public Hero(Vector2 position, World world) {
        init(position, world);
    }

    private void init(Vector2 position, World world) {
        this.world = world;
        this.position = position;
        this.energy = 80;
        textureRegion = Assets.instance.assetHero.hero;
        particleEffect.load(Gdx.files.internal("particleEffects/heroParticleEffect.p"), Gdx.files.internal("particleEffects/"));
        particleEffectPool = new ParticleEffectPool(particleEffect, 1, 2);
        invincibleParticleEffect.load(Gdx.files.internal("particleEffects/invincibleParticleEffect.p"), Gdx.files.internal("particleEffects/"));
        invincibleParticleEffectPool = new ParticleEffectPool(invincibleParticleEffect, 1, 2);
        sprite = new Sprite(textureRegion);
        sprite.setSize(sprite.getWidth() * GameConstants.PARTICLE_SPRITE_SCALE, sprite.getHeight()*GameConstants.PARTICLE_SPRITE_SCALE);
        sprite.setPosition(-sprite.getWidth() / 2 + position.x, -sprite.getHeight() / 2 + position.y);

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

    public int getEnergy() {
        return energy;
    }

    public void setEnergy(int energy) {
        this.energy = energy;
    }

    public boolean isInvincible() {
        return invincible;
    }

    public void setInvincible(boolean invincible) {
        this.invincible = invincible;
    }

    public void changeCollisionFilter(short filterType) {
        Filter filter = body.getFixtureList().get(0).getFilterData();
        if(filter.categoryBits != filterType) {
            filter.categoryBits = filterType;
            body.getFixtureList().get(0).setFilterData(filter);
        }
    }

    public void increaseArmor() {
        body.getFixtureList().get(0).setDensity(body.getFixtureList().get(0).getDensity() + 0.1f);
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
        ParticleEffectPool.PooledEffect effect;
        if(isInvincible()) {
            effect = invincibleParticleEffectPool.obtain();
        }else {
            effect = particleEffectPool.obtain();
        }
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
