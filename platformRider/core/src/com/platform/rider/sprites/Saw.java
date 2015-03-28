package com.platform.rider.sprites;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.World;
import com.platform.rider.assets.Assets;
import com.platform.rider.utils.GameConstants;

/**
 * Created by Gayan on 3/27/2015.
 */
public class Saw extends AbstractGameObject{
    public Saw(int xscale,int yscale, World world, String side){
        this.world = world;
        textureRegion = Assets.instance.assetSpike.spike;
        sprite = new Sprite(textureRegion);
        if(xscale == 0 && side.equals("R")) {
            sprite.setPosition((-sprite.getWidth() / 2) + Gdx.graphics.getWidth() / 2, (-sprite.getHeight() / 2) * yscale + Gdx.graphics.getHeight() / 2);
        }else if(xscale == 0 && side.equals("L")){
            sprite.setPosition((-sprite.getWidth() / 2) - Gdx.graphics.getWidth() / 2, (-sprite.getHeight() / 2) * yscale + Gdx.graphics.getHeight() / 2);
        }
        if(yscale == 0 && side.equals("D")) {
            sprite.setPosition((-sprite.getWidth() / 2) * xscale + Gdx.graphics.getWidth() / 2, (-sprite.getHeight() / 2) - Gdx.graphics.getHeight() / 2);
        }else if(yscale == 0 && side.equals("U")){
            sprite.setPosition((-sprite.getWidth() / 2) * xscale + Gdx.graphics.getWidth() / 2, (-sprite.getHeight() / 2) + Gdx.graphics.getHeight() / 2);
        }

        bodyDef.type = BodyDef.BodyType.StaticBody;
        bodyDef.position.set((sprite.getX() + sprite.getWidth() / 2) /
                        GameConstants.PIXELS_TO_METERS,
                (sprite.getY() + sprite.getHeight() / 2) / GameConstants.PIXELS_TO_METERS
        );

        body = world.createBody(bodyDef);
        body.setFixedRotation(true);
        shape.setAsBox(sprite.getWidth() / 2 / GameConstants.PIXELS_TO_METERS, sprite.getHeight()
                / 2 / GameConstants.PIXELS_TO_METERS);
        //shape.setRadius(10);

        fixtureDef.shape = shape;
        fixtureDef.filter.categoryBits = GameConstants.SPRITE_3;
        fixtureDef.filter.maskBits = GameConstants.SPRITE_2|GameConstants.SPRITE_1;

        body.createFixture(fixtureDef);
        shape.dispose();
    }

    public World getWorld() {
        return world;
    }

    public void setWorld(World world) {
        this.world = world;
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

    public Vector2 getPosition() {
        return position;
    }

    public void setPosition(Vector2 position) {
        this.position = position;
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
