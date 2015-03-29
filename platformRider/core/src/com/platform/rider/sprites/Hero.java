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
public class Hero extends AbstractGameObject {
    private static final String TAG = Hero.class.getName();

    Vector2 position = new Vector2();

    public Hero(Vector2 position, World world) {
        init(position, world);
    }

    private void init(Vector2 position, World world) {
        this.world = world;
        this.position = position;
        textureRegion = Assets.instance.assetHero.hero;
        sprite = new Sprite(textureRegion);
        sprite.setPosition(-sprite.getWidth() / 2, -sprite.getHeight() / 2);

        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set((sprite.getX() + sprite.getWidth() / 2) /
                        GameConstants.PIXELS_TO_METERS,
                (sprite.getY() + sprite.getHeight() / 2) / GameConstants.PIXELS_TO_METERS
        );

        body = world.createBody(bodyDef);
        body.setFixedRotation(true);
        body.setLinearDamping(GameConstants.LINEAR_DAMPING);
        shape.setRadius((sprite.getWidth() / 2) /
                GameConstants.PIXELS_TO_METERS);
        /*shape.setAsBox(sprite.getWidth() / 2 / GameConstants.PIXELS_TO_METERS, sprite.getHeight()
                / 2 / GameConstants.PIXELS_TO_METERS);*/

        fixtureDef.shape = shape;
        fixtureDef.density = 0.2f;
        fixtureDef.restitution = 0.5f;
        fixtureDef.filter.categoryBits = GameConstants.SPRITE_2;
        fixtureDef.filter.maskBits = GameConstants.SPRITE_1 | GameConstants.SPRITE_3;

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
        batch.draw(sprite,
                sprite.getX(), sprite.getY(),
                sprite.getOriginX(),sprite.getOriginY(),
                sprite.getWidth(), sprite.getHeight(),
                sprite.getScaleX(), sprite.getScaleY(), sprite.getRotation()
        );
    }
}
