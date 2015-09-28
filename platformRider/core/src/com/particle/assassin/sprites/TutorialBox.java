package com.particle.assassin.sprites;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.particle.assassin.assets.Assets;

/**
 * Created by Gayan on 7/24/2015.
 */
public class TutorialBox extends AbstractGameObject {

    public TutorialBox(Vector2 position, World world) {
        this.world = world;
        this.position = position;
        textureRegion = Assets.instance.assetLevelDecoration.secondTutorial;
        sprite = new Sprite(textureRegion);
        sprite.setSize(sprite.getWidth() * 2, sprite.getHeight() * 2);
        sprite.setPosition(-sprite.getWidth() / 2 + position.x, -sprite.getHeight() / 2 + position.y);
    }

    public Sprite getSprite(){
        return this.sprite;
    }

    @Override
    public void render(SpriteBatch batch) {
        batch.draw(sprite,
                sprite.getX(), sprite.getY(),
                sprite.getOriginX(), sprite.getOriginY(),
                sprite.getWidth(), sprite.getHeight(),
                sprite.getScaleX(), sprite.getScaleY(), sprite.getRotation()
        );
    }
}
