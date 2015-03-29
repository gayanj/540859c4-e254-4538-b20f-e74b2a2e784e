package com.platform.rider.main;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Touchpad;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.platform.rider.sprites.Hero;
import com.platform.rider.sprites.Particle;
import com.platform.rider.sprites.Saw;
import com.platform.rider.utils.GameConstants;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by Gayan on 3/23/2015.
 */
public class CanyonBunnyMain implements ApplicationListener {
    SpriteBatch batch;
    World world;
    Box2DDebugRenderer debugRenderer;
    Matrix4 debugMatrix;

    OrthographicCamera camera;
    List<Particle> particles = new ArrayList<Particle>();
    List<Saw> saws = new ArrayList<Saw>();
    Hero hero;
    Saw saw;

    final float SPEED = 50f;
    final float COLLISION_SPEED = 100f;

    final float PIXELS_TO_METERS = 10f;

    final short SPRITE_1 = 0x1;    // 0001
    final short SPRITE_2 = 0x1 << 1; // 0010 or 0x2 in hex
    final short SPRITE_3 = 0x1 << 2; // 0010 or 0x3 in hex

    private Stage stage;
    private Touchpad touchpad;
    private Touchpad.TouchpadStyle touchpadStyle;
    private Skin touchpadSkin;
    private Drawable touchBackground;
    private Drawable touchKnob;

    @Override
    public void create() {
        world = new World(new Vector2(0, 0), true);
        batch = new SpriteBatch();
        for (int i = 0; i < 4; i++) {
            Random r = new Random();
            int xLow = 66;
            int xHigh = Gdx.graphics.getWidth() - 66;
            int xR = r.nextInt(xHigh - xLow) + xLow;

            int yLow = 66;
            int yHigh = Gdx.graphics.getHeight() - 66;
            int yR = r.nextInt(yHigh - yLow) + yLow;
            Vector2 position = new Vector2(xR, yR);
            Particle particle = new Particle(position, world, i, GameConstants.NORMAL_PARTICLE);
            particles.add(particle);
        }
        float x = (float) Math.random() * 100;
        float y = (float) Math.random() * 100;
        hero = new Hero(new Vector2(x, y), world);

        for (int i = 0; i < (Gdx.graphics.getHeight() / 130) + 1; i++) {
            int yscale = 2 * (i + 1);
            Saw saw = new Saw(0, yscale, world, "R");
            saws.add(saw);
        }
        for (int i = 0; i < (Gdx.graphics.getHeight() / 130) + 1; i++) {
            int yscale = 2 * (i + 1);
            Saw saw = new Saw(0, yscale, world, "L");
            saws.add(saw);
        }
        for (int i = 0; i < (Gdx.graphics.getWidth() / 130) + 1; i++) {
            int xscale = 2 * (i + 1);
            Saw saw = new Saw(xscale, 0, world, "U");
            saws.add(saw);
        }
        for (int i = 0; i < (Gdx.graphics.getWidth() / 130) + 1; i++) {
            int xscale = 2 * (i + 1);
            Saw saw = new Saw(xscale, 0, world, "D");
            saws.add(saw);
        }

        debugRenderer = new Box2DDebugRenderer();
        camera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.
                getHeight());
        world.setContactListener(new reactorContactListener());

        //Create a touchpad skin
        touchpadSkin = new Skin();
        //Set background image
        touchpadSkin.add("touchBackground", new Texture("data/touchBackground.png"));
        //Set knob image
        touchpadSkin.add("touchKnob", new Texture("data/touchKnob.png"));
        //Create TouchPad Style
        touchpadStyle = new Touchpad.TouchpadStyle();
        //Create Drawable's from TouchPad skin
        touchBackground = touchpadSkin.getDrawable("touchBackground");
        touchKnob = touchpadSkin.getDrawable("touchKnob");
        //Apply the Drawables to the TouchPad Style
        touchpadStyle.background = touchBackground;
        touchpadStyle.knob = touchKnob;
        //Create new TouchPad with the created style
        touchpad = new Touchpad(10, touchpadStyle);
        //setBounds(x,y,width,height)
        touchpad.setBounds(Gdx.graphics.getWidth() - 215, 15, 200, 200);

        //Create a Stage and add TouchPad
        stage = new Stage();
        stage.addActor(touchpad);
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void render() {
        camera.update();
        // Step the physics simulation forward at a rate of 60hz
        world.step(1f / 60f, 6, 2);

        Gdx.gl.glClearColor(1, 1, 1, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.setProjectionMatrix(camera.combined);
        debugMatrix = batch.getProjectionMatrix().cpy().scale(PIXELS_TO_METERS,
                PIXELS_TO_METERS, 0);

        //Touch pad readings
        Vector2 touchPadVec = new Vector2(touchpad.getKnobPercentX() * 100f, touchpad.getKnobPercentY() * 100f);
        //If touchpad readings are zero dont alter velocity of hero
        if (touchPadVec.x != 0 && touchPadVec.y != 0) {
            hero.getBody().setLinearVelocity(touchPadVec);
        }

        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        for (Saw saw : saws) {
            saw.getSprite().setPosition((saw.getBody().getPosition().x * PIXELS_TO_METERS) - saw.getSprite().
                            getWidth() / 2,
                    (saw.getBody().getPosition().y * PIXELS_TO_METERS) - saw.getSprite().getHeight() / 2
            );
            batch.draw(saw.getSprite(), saw.getSprite().getX(), saw.getSprite().getY(), saw.getSprite().getOriginX(),
                    saw.getSprite().getOriginY(),
                    saw.getSprite().getWidth(), saw.getSprite().getHeight(), saw.getSprite().getScaleX(), saw.getSprite().
                            getScaleY(), saw.getSprite().getRotation()
            );
        }

        for (Particle particle : particles) {
            /*Vector2 heroPos1 = hero.getBody().getWorldCenter();
            Vector2 particlePos1 = particle.getBody().getWorldCenter();
            Vector2 distance1 = new Vector2(0, 0);
            distance1.add(particlePos1).sub(heroPos1);
            Vector2 invDist = new Vector2(-distance1.x,-distance1.y);
            particle.getBody().applyForce(invDist,particlePos1,true);*/

            Vector2 heroPos = hero.getBody().getPosition();
            Vector2 particlePos = particle.getBody().getPosition();
            Vector2 distance = new Vector2(0, 0);
            distance.add(heroPos).sub(particlePos);

            particle.setNormaliseVector(distance.nor());

            if (!particle.isColliding()) {
                particle.getBody().setLinearVelocity(particle.getNormaliseVector().x * SPEED, particle.getNormaliseVector().y * SPEED);
            }

            if (particle.isColliding()) {
                int count = particle.getCounter();
                count++;
                particle.setCounter(count);
            }

            if (particle.getCounter() > 10) {
                particle.setColliding(false);
                particle.setCounter(0);
            }

            particle.getSprite().setPosition((particle.getBody().getPosition().x * PIXELS_TO_METERS) - particle.getSprite().
                            getWidth() / 2,
                    (particle.getBody().getPosition().y * PIXELS_TO_METERS) - particle.getSprite().getHeight() / 2
            );

            //particle.getSprite().setRotation((float) Math.toDegrees(particle.getBody().getAngle()));

            batch.draw(particle.getSprite(), particle.getSprite().getX(), particle.getSprite().getY(), particle.getSprite().getOriginX(),
                    particle.getSprite().getOriginY(),
                    particle.getSprite().getWidth(), particle.getSprite().getHeight(), particle.getSprite().getScaleX(), particle.getSprite().
                            getScaleY(), particle.getSprite().getRotation()
            );
        }

        hero.getSprite().setPosition((hero.getBody().getPosition().x * PIXELS_TO_METERS) - hero.getSprite().
                        getWidth() / 2,
                (hero.getBody().getPosition().y * PIXELS_TO_METERS) - hero.getSprite().getHeight() / 2
        );
        //hero.getSprite().setRotation((float) Math.toDegrees(hero.getBody().getAngle()));


        batch.draw(hero.getSprite(), hero.getSprite().getX(), hero.getSprite().getY(), hero.getSprite().getOriginX(),
                hero.getSprite().getOriginY(),
                hero.getSprite().getWidth(), hero.getSprite().getHeight(), hero.getSprite().getScaleX(), hero.getSprite().
                        getScaleY(), hero.getSprite().getRotation()
        );

        batch.end();
        debugRenderer.render(world, debugMatrix);
        stage.act(Gdx.graphics.getDeltaTime());
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        //worldRenderer.resize(width, height);
    }

    @Override
    public void pause() {
        //paused = true;
    }

    @Override
    public void resume() {
        //paused = false;
    }

    @Override
    public void dispose() {

        world.dispose();
    }

    class reactorContactListener implements ContactListener {

        @Override
        public void beginContact(Contact contact) {

        }

        @Override
        public void endContact(Contact contact) {
            if (contact.getFixtureA().getFilterData().categoryBits == SPRITE_1 && contact.getFixtureB().getFilterData().categoryBits == SPRITE_2) {
                Particle particle = particles.get(Integer.parseInt(contact.getFixtureA().getBody().getUserData().toString()));
                Vector2 inverseNormal = new Vector2(-particle.getNormaliseVector().x * COLLISION_SPEED, -particle.getNormaliseVector().y * COLLISION_SPEED);
                contact.getFixtureA().getBody().setLinearVelocity(inverseNormal);
                particle.setColliding(true);
            }
            if (contact.getFixtureA().getFilterData().categoryBits == SPRITE_1 && contact.getFixtureB().getFilterData().categoryBits == SPRITE_1) {
                Particle particle = particles.get(Integer.parseInt(contact.getFixtureA().getBody().getUserData().toString()));
                particle.setColliding(true);
                if (particle.getCollisionCount() > 3) {
                    Vector2 inverseNormal = new Vector2(-particle.getNormaliseVector().x * COLLISION_SPEED, -particle.getNormaliseVector().y * COLLISION_SPEED);
                    contact.getFixtureA().getBody().setLinearVelocity(inverseNormal);
                    particle.setCollisionCount(0);
                } else {
                    int count = particle.getCollisionCount();
                    count++;
                    particle.setCollisionCount(count);
                }
            }
            if (contact.getFixtureA().getFilterData().categoryBits == SPRITE_1 && contact.getFixtureB().getFilterData().categoryBits == SPRITE_3) {
                //remove particles
                Particle particle = particles.get(Integer.parseInt(contact.getFixtureA().getBody().getUserData().toString()));
                particle.setRemove(true);
            }
            if (contact.getFixtureA().getFilterData().categoryBits == SPRITE_2 && contact.getFixtureB().getFilterData().categoryBits == SPRITE_3) {
                //game over
            }
        }

        @Override
        public void preSolve(Contact contact, Manifold oldManifold) {

        }

        @Override
        public void postSolve(Contact contact, ContactImpulse impulse) {

        }
    }
}
