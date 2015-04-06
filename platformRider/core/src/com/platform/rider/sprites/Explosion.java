package com.platform.rider.sprites;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.platform.rider.assets.Assets;
import net.dermetfan.gdx.graphics.g2d.AnimatedSprite;

/**
 * Created by Gayan on 4/6/2015.
 */
public class Explosion extends AbstractGameObject{

    public AnimatedSprite getAnimatedSprite() {
        return animatedSprite;
    }

    public void setAnimatedSprite(AnimatedSprite animatedSprite) {
        this.animatedSprite = animatedSprite;
    }

    public Explosion(){
        animatedSprite = new AnimatedSprite(Assets.instance.assetAnimations.explosionParticleAnimation);
        animatedSprite.setSize(animatedSprite.getWidth() * 2, animatedSprite.getHeight() * 2);
        animatedSprite.setPosition(-animatedSprite.getWidth() / 2, -animatedSprite.getHeight() / 2);
    }
    @Override
    public void render(SpriteBatch batch) {
        animatedSprite.draw(batch);
    }
}
