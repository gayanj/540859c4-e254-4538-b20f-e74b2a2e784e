package com.particle.assassin.sprites;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.World;
import com.particle.assassin.assets.Assets;
import com.particle.assassin.utils.GameConstants;
import net.dermetfan.gdx.graphics.g2d.AnimatedSprite;

/**
 * Created by Gayan on 8/4/2015.
 */
public class PlayButtonSaw extends AbstractGameObject {

    public PlayButtonSaw(float xscale, float yscale, World world){
        this.world = world;
        //textureRegion = Assets.instance.assetSpike.spike;
        animatedSprite = new AnimatedSprite(Assets.instance.assetAnimations.playButtonSawAnimation);
        animatedSprite.setPosition((-animatedSprite.getWidth() / 2) + xscale, (-animatedSprite.getHeight() / 2) + yscale);

        bodyDef.type = BodyDef.BodyType.StaticBody;
        bodyDef.position.set((animatedSprite.getX() + animatedSprite.getWidth() / 2) /
                        GameConstants.PIXELS_TO_METERS,
                (animatedSprite.getY() + animatedSprite.getHeight() / 2) / GameConstants.PIXELS_TO_METERS
        );

        body = world.createBody(bodyDef);
        body.setUserData("playButtonSaw");
        body.setFixedRotation(false);
        shape.setRadius((animatedSprite.getWidth() / 2) /
                GameConstants.PIXELS_TO_METERS);
        /*shape.setAsBox(sprite.getWidth() / 2 / GameConstants.PIXELS_TO_METERS, sprite.getHeight()
                / 2 / GameConstants.PIXELS_TO_METERS);
        //shape.setRadius(10);*/

        fixtureDef.shape = shape;
        fixtureDef.filter.categoryBits = GameConstants.SPRITE_3;
        fixtureDef.filter.maskBits = GameConstants.SPRITE_2 | GameConstants.SPRITE_1 | GameConstants.SPRITE_8;

        body.createFixture(fixtureDef);
        shape.dispose();
    }

    public Body getBody() {
        return body;
    }

    public AnimatedSprite getAnimatedSprite(){
        return animatedSprite;
    }

    @Override
    public void render(SpriteBatch batch) {
        animatedSprite.draw(batch);
    }
}
