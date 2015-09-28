package com.particle.assassin.sprites;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.World;
import com.particle.assassin.assets.Assets;
import com.particle.assassin.utils.GameConstants;

/**
 * Created by Gayan on 4/12/2015.
 */
public class Power extends AbstractGameObject {
    String type;
    boolean pickedUp = false;
    int powerUpVisibleCount = 0;

    public Power(Vector2 position, World world, String type,String powerIndex) {
        init(position, world, type, powerIndex);
    }

    private void init(Vector2 position, World world, String type, String powerIndex) {
        this.world = world;
        this.position = position;
        this.type = type;
        if(GameConstants.SUPER_FORCE.equals(type)) {
            textureRegion = Assets.instance.assetPowerup.super_force;
        }else if(GameConstants.SLOW_MOTION.equals(type)){
            textureRegion = Assets.instance.assetPowerup.slow_motion;
        }else if(GameConstants.MASS_DEATH.equals(type)){
            textureRegion = Assets.instance.assetPowerup.mass_death;
        }else if(GameConstants.ENERGY.equals(type)){
            textureRegion = Assets.instance.assetPowerup.energy;
        }else if(GameConstants.SPEED.equals(type)){
            textureRegion = Assets.instance.assetPowerup.speed;
        }else if(GameConstants.INVINCIBILITY.equals(type)){
            textureRegion = Assets.instance.assetPowerup.invincibility;
        }else if(GameConstants.ARMOR.equals(type)){
            textureRegion = Assets.instance.assetPowerup.armor;
        }
        sprite = new Sprite(textureRegion);
        //sprite.setSize(sprite.getWidth() * GameConstants.PARTICLE_SPRITE_SCALE, sprite.getHeight() * GameConstants.PARTICLE_SPRITE_SCALE);
        sprite.setPosition(-sprite.getWidth() / 2 + position.x, -sprite.getHeight() / 2 + position.y);

        bodyDef.type = BodyDef.BodyType.StaticBody;
        bodyDef.position.set((sprite.getX() + sprite.getWidth() / 2) /
                        GameConstants.PIXELS_TO_METERS,
                (sprite.getY() + sprite.getHeight() / 2) / GameConstants.PIXELS_TO_METERS
        );

        body = world.createBody(bodyDef);
        body.setFixedRotation(true);
        body.setUserData(powerIndex);
        body.setLinearDamping(GameConstants.LINEAR_DAMPING);
        shape.setRadius((sprite.getWidth() / 2) /
                GameConstants.PIXELS_TO_METERS);
        /*shape.setAsBox(sprite.getWidth() / 2 / GameConstants.PIXELS_TO_METERS, sprite.getHeight()
                / 2 / GameConstants.PIXELS_TO_METERS);*/

        fixtureDef.shape = shape;
        fixtureDef.density = 0.2f;
        fixtureDef.restitution = 0.5f;
        if(GameConstants.SUPER_FORCE.equals(type) || GameConstants.SLOW_MOTION.equals(type) || GameConstants.MASS_DEATH.equals(type)) {
            fixtureDef.filter.categoryBits = GameConstants.SPRITE_6;
        }else if(GameConstants.ENERGY.equals(type) || GameConstants.SPEED.equals(type) || GameConstants.INVINCIBILITY.equals(type) || GameConstants.ARMOR.equals(type)){
            fixtureDef.filter.categoryBits = GameConstants.SPRITE_7;
        }
        fixtureDef.filter.maskBits = GameConstants.SPRITE_2 | GameConstants.SPRITE_8;

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

    public String getType() {
        return type;
    }

    public boolean isPickedUp() {
        return pickedUp;
    }

    public TextureRegion getTexureRegion(){
        return textureRegion;
    }

    @Override
    public void render(SpriteBatch batch) {
        //Draw Sprite
        batch.draw(sprite,
                sprite.getX(), sprite.getY(),
                sprite.getOriginX(), sprite.getOriginY(),
                sprite.getWidth(), sprite.getHeight(),
                sprite.getScaleX(), sprite.getScaleY(), sprite.getRotation()
        );
        if(powerUpVisibleCount > 300){
            pickedUp = true;
        }else{
            powerUpVisibleCount++;
        }
    }
}
