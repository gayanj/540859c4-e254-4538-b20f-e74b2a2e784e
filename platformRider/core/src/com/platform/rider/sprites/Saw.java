package com.platform.rider.sprites;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.World;
import com.platform.rider.assets.Assets;
import com.platform.rider.utils.GameConstants;
import com.platform.rider.world.WorldController;
import net.dermetfan.gdx.graphics.g2d.AnimatedBox2DSprite;
import net.dermetfan.gdx.graphics.g2d.AnimatedSprite;

/**
 * Created by Gayan on 3/27/2015.
 */
public class Saw extends AbstractGameObject {
    public Saw(float xscale, float yscale, World world, String side) {
        this.world = world;
        //textureRegion = Assets.instance.assetSpike.spike;
        animatedSprite = new AnimatedSprite(Assets.instance.assetAnimations.spikeAnimation);
        //sprite = new Sprite(textureRegion);
        if (xscale == 0 && side.equals("R")) {
            animatedSprite.setPosition((-animatedSprite.getWidth() / 2) + GameConstants.APP_WIDTH / 2, (-animatedSprite.getHeight() / 2) * yscale + GameConstants.APP_HEIGHT / 2);
        } else if (xscale == 0 && side.equals("L")) {
            animatedSprite.setPosition((-animatedSprite.getWidth() / 2) - GameConstants.APP_WIDTH / 2, (-animatedSprite.getHeight() / 2) * yscale + GameConstants.APP_HEIGHT / 2);
        } else if (yscale == 0 && side.equals("D")) {
            animatedSprite.setPosition((-animatedSprite.getWidth() / 2) * xscale + GameConstants.APP_WIDTH / 2, (-animatedSprite.getHeight() / 2) - GameConstants.APP_HEIGHT / 2);
        } else if (yscale == 0 && side.equals("U")) {
            animatedSprite.setPosition((-animatedSprite.getWidth() / 2) * xscale + GameConstants.APP_WIDTH / 2, (-animatedSprite.getHeight() / 2) + GameConstants.APP_HEIGHT / 2);
        } else if (side.equals("RT")) {
            animatedSprite.setSize(animatedSprite.getWidth() * 0.7f, animatedSprite.getHeight() * 0.7f);
            animatedSprite.setPosition((-animatedSprite.getWidth() / 2) * xscale + GameConstants.APP_WIDTH / 2, (-animatedSprite.getHeight() / 2) * yscale + GameConstants.APP_HEIGHT / 2);
        } else if (side.equals("LT")) {
            animatedSprite.setSize(animatedSprite.getWidth() * 0.7f, animatedSprite.getHeight() * 0.7f);
            animatedSprite.setPosition((-animatedSprite.getWidth() / 2) * -xscale - GameConstants.APP_WIDTH / 2, (-animatedSprite.getHeight() / 2) * yscale + GameConstants.APP_HEIGHT / 2);
        } else if (side.equals("RB")) {
            animatedSprite.setSize(animatedSprite.getWidth() * 0.6f, animatedSprite.getHeight() * 0.6f);
            animatedSprite.setPosition((-animatedSprite.getWidth() / 2) * xscale + GameConstants.APP_WIDTH / 2, (-animatedSprite.getHeight() / 2) * yscale + GameConstants.APP_HEIGHT / 2);
        } else if (side.equals("LB")) {
            animatedSprite.setSize(animatedSprite.getWidth() * 0.6f, animatedSprite.getHeight() * 0.6f);
            animatedSprite.setPosition((-animatedSprite.getWidth() / 2) * -xscale - GameConstants.APP_WIDTH / 2, (-animatedSprite.getHeight() / 2) * yscale + GameConstants.APP_HEIGHT / 2);
        }

        bodyDef.type = BodyDef.BodyType.StaticBody;
        bodyDef.position.set((animatedSprite.getX() + animatedSprite.getWidth() / 2) /
                        GameConstants.PIXELS_TO_METERS,
                (animatedSprite.getY() + animatedSprite.getHeight() / 2) / GameConstants.PIXELS_TO_METERS
        );

        body = world.createBody(bodyDef);
        //body.setUserData(animatedBox2DSprite);
        body.setFixedRotation(false);
        shape.setRadius((animatedSprite.getWidth() / 2) /
                GameConstants.PIXELS_TO_METERS);
        /*shape.setAsBox(sprite.getWidth() / 2 / GameConstants.PIXELS_TO_METERS, sprite.getHeight()
                / 2 / GameConstants.PIXELS_TO_METERS);
        //shape.setRadius(10);*/

        fixtureDef.shape = shape;
        fixtureDef.filter.categoryBits = GameConstants.SPRITE_3;
        fixtureDef.filter.maskBits = GameConstants.SPRITE_2 | GameConstants.SPRITE_1;

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

    public AnimatedBox2DSprite getAnimatedSprite() {
        return this.animatedBox2DSprite;
    }

    public void setAnimatedSprite(AnimatedBox2DSprite animatedBox2DSprite1) {
        this.animatedBox2DSprite = animatedBox2DSprite1;
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
        //animatedBox2DSprite.draw(batch,this.getBody());
        //box2DSprite.draw(batch, body);
        //box2DSprite.draw(batch, fixture);
        animatedSprite.getAnimation().setFrameDuration(GameConstants.FRAME_DURATION / WorldController.scale);
        animatedSprite.draw(batch);
        //AnimatedBox2DSprite.draw(batch,getWorld());
    }
}
