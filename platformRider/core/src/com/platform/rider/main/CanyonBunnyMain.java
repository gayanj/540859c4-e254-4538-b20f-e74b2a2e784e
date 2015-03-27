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

import java.util.ArrayList;
import java.util.List;

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
    Hero hero;

    final float SPEED = 50f;
    final float COLLISION_SPEED = 100f;

    final float PIXELS_TO_METERS = 10f;

    final short SPRITE_1 = 0x1;    // 0001
    final short SPRITE_2 = 0x1 << 1; // 0010 or 0x2 in hex

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
            float x = (float) Math.random() * 500;
            float y = (float) Math.random() * 500;
            Vector2 position = new Vector2(x, y);
            Particle particle = new Particle(position, world, i);
            particles.add(particle);
        }
        float x = (float) Math.random() * 100;
        float y = (float) Math.random() * 100;
        hero = new Hero(new Vector2(x, y), world);


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

        for (Particle particle : particles) {
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
                particle.setColliding(true);
                Vector2 inverseNormal = new Vector2(-particle.getNormaliseVector().x * COLLISION_SPEED, -particle.getNormaliseVector().y * COLLISION_SPEED);
                contact.getFixtureA().getBody().setLinearVelocity(inverseNormal);
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
