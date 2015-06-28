package com.platform.rider.sprites;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.World;
import com.platform.rider.assets.Assets;
import com.platform.rider.utils.GameConstants;

import java.util.ArrayList;

/**
 * Created by Gayan on 6/27/2015.
 */
public class ForceField extends AbstractGameObject {

    ArrayList<Vector2> vertices = new ArrayList<Vector2>();
    float theta;  // angle that will be increased each loop
    float radius = 0;
    float h = 0;      // x coordinate of circle center
    float k = 0;      // y coordinate of circle center
    float step = 15 * MathUtils.degreesToRadians;  // amount to add to theta each time (degrees)

    public ForceField(Vector2 position, World world) {
        init(position, world);
    }

    private void init(Vector2 position, World world) {
        this.world = world;
        this.position = position;
        textureRegion = Assets.instance.assetForceField.forceField;
        sprite = new Sprite(textureRegion);
        sprite.setSize(sprite.getWidth() * GameConstants.PARTICLE_SPRITE_SCALE, sprite.getHeight()*GameConstants.PARTICLE_SPRITE_SCALE);
        sprite.setPosition(-sprite.getWidth() / 2, -sprite.getHeight() / 2);
        radius = (sprite.getWidth() / 2) / GameConstants.PIXELS_TO_METERS;
        //createCircleVertices();
        //createChainLoop();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(0,0);

        body = world.createBody(bodyDef);
        body.setFixedRotation(true);
        body.setUserData("forceField");
        //body.setLinearDamping(GameConstants.LINEAR_DAMPING);
        shape.setRadius((sprite.getWidth() / 2) /
                GameConstants.PIXELS_TO_METERS);
        /*shape.setAsBox(sprite.getWidth() / 2 / GameConstants.PIXELS_TO_METERS, sprite.getHeight()
                / 2 / GameConstants.PIXELS_TO_METERS);*/

        fixtureDef.shape = shape;
        //fixtureDef.density = 0.2f;
        //fixtureDef.restitution = 0.5f;
        fixtureDef.filter.categoryBits = GameConstants.SPRITE_7;
        fixtureDef.filter.maskBits = GameConstants.SPRITE_1 | GameConstants.SPRITE_2 | GameConstants.SPRITE_3 | GameConstants.SPRITE_4 | GameConstants.SPRITE_6;

        body.createFixture(fixtureDef);
        chainShape.dispose();
    }

    private void createCircleVertices(){
        for(theta = 0; theta < 360 * MathUtils.degreesToRadians; theta+=step){
            float x = h + radius* MathUtils.cos(theta);
            float y = k - radius* MathUtils.sin(theta);
            Vector2 vertex = new Vector2(x,y);
            vertices.add(vertex);
        }
    }

    private void createChainLoop(){
        Vector2[] vector2Array = new Vector2[vertices.size()];
        for(int i = 0; i < vertices.size(); i++){
            vector2Array[i] = vertices.get(i);
        }
        chainShape.createChain(vector2Array);
    }

    public void changeCollisionFilter(short filterType) {
        Filter filter = body.getFixtureList().get(0).getFilterData();
        filter.categoryBits = filterType;
        body.getFixtureList().get(0).setFilterData(filter);
    }

    public Body getBody() {
        return body;
    }

    public Sprite getSprite() {
        return sprite;
    }

    @Override
    public void render(SpriteBatch batch) {
        batch.draw(sprite,
                sprite.getX(), sprite.getY(),
                sprite.getOriginX(),sprite.getOriginY(),
                sprite.getWidth(), sprite.getHeight(),
                sprite.getScaleX(), sprite.getScaleY(), sprite.getRotation()
        );
    }
}
