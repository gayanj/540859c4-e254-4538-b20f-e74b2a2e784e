package com.particle.assassin.sprites;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.particle.assassin.assets.Assets;
import net.dermetfan.gdx.graphics.g2d.AnimatedSprite;

/**
 * Created by Gayan on 7/23/2015.
 */
public class TutorialArrow extends AbstractGameObject {

    public TutorialArrow(Vector2 position, World world) {
        this.world = world;
        this.position = position;
        animatedSprite = new AnimatedSprite(Assets.instance.assetAnimations.tutorialArrowAnimation);
        animatedSprite.setSize(animatedSprite.getWidth(), animatedSprite.getHeight());
        animatedSprite.setPosition(-animatedSprite.getWidth() / 2 + position.x, -animatedSprite.getHeight() / 2 + position.y);
    }

    @Override
    public void render(SpriteBatch batch) {
        animatedSprite.draw(batch);
    }
}
