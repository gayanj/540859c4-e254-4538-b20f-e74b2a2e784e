package com.platform.rider.sprites;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.platform.rider.assets.Assets;
import net.dermetfan.gdx.graphics.g2d.AnimatedSprite;

/**
 * Created by Gayan on 4/6/2015.
 */
public class Explosion extends AbstractGameObject{

    int hashMapIndex;
    boolean blast = false;
    Vector2 blastPosition;

    public AnimatedSprite getAnimatedSprite() {
        return animatedSprite;
    }

    public void setAnimatedSprite(AnimatedSprite animatedSprite) {
        this.animatedSprite = animatedSprite;
    }

    public boolean isBlast() {
        return blast;
    }

    public void setBlast(boolean blast) {
        this.blast = blast;
    }

    public Vector2 getBlastPosition() {
        return blastPosition;
    }

    public void setBlastPosition(Vector2 blastPosition) {
        this.blastPosition = blastPosition;
    }

    public int getHashMapIndex() {
        return hashMapIndex;
    }

    public Explosion(int hashMapIndex){
        this.hashMapIndex = hashMapIndex;
        animatedSprite = new AnimatedSprite(Assets.instance.assetAnimations.explosionParticleAnimation);
        animatedSprite.setSize(animatedSprite.getWidth() * 2, animatedSprite.getHeight() * 2);
        animatedSprite.setPosition(-animatedSprite.getWidth() / 2, -animatedSprite.getHeight() / 2);
    }
    @Override
    public void render(SpriteBatch batch) {
        animatedSprite.draw(batch);
    }
}