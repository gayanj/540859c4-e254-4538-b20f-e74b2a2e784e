package com.platform.rider.sprites;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.platform.rider.assets.Assets;
import com.platform.rider.utils.GameConstants;
import net.dermetfan.gdx.graphics.g2d.AnimatedSprite;

/**
 * Created by Gayan on 4/14/2015.
 */
public class ParticleBurstAnimation extends AbstractGameObject {

    String hashMapIndex;
    boolean burst = true;

    public AnimatedSprite getAnimatedSprite() {
        return animatedSprite;
    }

    public void setAnimatedSprite(AnimatedSprite animatedSprite) {
        this.animatedSprite = animatedSprite;
    }

    public boolean isBurst() {
        return burst;
    }

    public void setBurst(boolean burst) {
        this.burst = burst;
    }

    public String getHashMapIndex() {
        return hashMapIndex;
    }

    public ParticleBurstAnimation(String hashMapIndex, Vector2 position) {
        this.hashMapIndex = hashMapIndex;
        animatedSprite = new AnimatedSprite(Assets.instance.assetAnimations.particleDyingAnimationAnimation);
        animatedSprite.setSize(animatedSprite.getWidth(), animatedSprite.getHeight());
        animatedSprite.setPosition(
                (position.x * GameConstants.PIXELS_TO_METERS) - animatedSprite.getWidth() / 2,
                (position.y * GameConstants.PIXELS_TO_METERS) - animatedSprite.getHeight() / 2
        );
    }

    @Override
    public void render(SpriteBatch batch) {
        animatedSprite.draw(batch);
    }
}
