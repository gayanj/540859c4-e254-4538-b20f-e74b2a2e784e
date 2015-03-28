package com.platform.rider.world;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;
import com.platform.rider.sprites.Hero;
import com.platform.rider.sprites.Particle;
import com.platform.rider.sprites.Saw;
import com.platform.rider.utils.GameConstants;
import com.platform.rider.utils.TouchPadHelper;

import java.util.*;

/**
 * Created by Gayan on 3/23/2015.
 */
public class WorldController {
    private static final String TAG = WorldController.class.getName();
    public OrthographicCamera camera;
    public World world;
    public Hero hero;
    public HashMap<String, Particle> particleHashMap = new HashMap<String, Particle>();
    public List<String> particlesForRemoval = new ArrayList<String>();
    public List<Saw> saws = new ArrayList<Saw>();
    public TouchPadHelper touchPadHelper;

    public WorldController() {
        init();
    }

    private void init() {
        initTouchpad();
        Gdx.input.setInputProcessor(touchPadHelper.getStage());
        camera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.
                getHeight());
        initPhysics();
        world.setContactListener(new reactorContactListener());
    }

    private void initPhysics() {
        world = new World(new Vector2(0, 0), true);
        createHero();
        createParticles();
        createSpikes();
    }

    private void initTouchpad() {
        touchPadHelper = new TouchPadHelper();
    }

    private void createHero() {
        float x = (float) Math.random() * 100;
        float y = (float) Math.random() * 100;
        hero = new Hero(new Vector2(x, y), world);
    }

    private void createParticles() {
        for (int i = 0; i < 4; i++) {
            Random r = new Random();
            int xLow = 66;
            int xHigh = Gdx.graphics.getWidth() - 66;
            int xR = r.nextInt(xHigh - xLow) + xLow;

            int yLow = 66;
            int yHigh = Gdx.graphics.getHeight() - 66;
            int yR = r.nextInt(yHigh - yLow) + yLow;
            Vector2 position = new Vector2(xR, yR);
            Particle particle = new Particle(position, world, i);
            particleHashMap.put(String.valueOf(i), particle);
            //particles.add(particle);
        }
    }

    private void createSpikes() {
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
    }

    public void update(float deltaTime) {
        world.step(deltaTime, 8, 3);
        destroyParticles();
        camera.update();
        //Touch pad readings
        Vector2 touchPadVec = new Vector2(touchPadHelper.getTouchpad().getKnobPercentX() * 10f, touchPadHelper.getTouchpad().getKnobPercentY() * 10f);
        //If touchpad readings are zero dont alter velocity of hero
        if (touchPadVec.x != 0 && touchPadVec.y != 0) {
            hero.getBody().setLinearVelocity(touchPadVec);
        }
        updateHero();
        updateParticles();
        updateSpikes();
    }

    private void updateHero() {
        hero.getSprite().setPosition((hero.getBody().getPosition().x * GameConstants.PIXELS_TO_METERS) - hero.getSprite().
                        getWidth() / 2,
                (hero.getBody().getPosition().y * GameConstants.PIXELS_TO_METERS) - hero.getSprite().getHeight() / 2
        );
    }

    private void destroyParticles() {
        for (String particleKey : particlesForRemoval) {
            particleHashMap.get(particleKey).setSprite(null);
            final Array<JointEdge> list = particleHashMap.get(particleKey).getBody().getJointList();
            while (list.size > 0) {
                world.destroyJoint(list.get(0).joint);
            }
            world.destroyBody(particleHashMap.get(particleKey).getBody());
            particleHashMap.remove(particleKey);
        }
        particlesForRemoval.clear();
    }

    private void updateParticles() {
        for (Map.Entry<String, Particle> entry : particleHashMap.entrySet()) {
            Particle particle = entry.getValue();
            Vector2 heroPos = hero.getBody().getPosition();
            Vector2 particlePos = particle.getBody().getPosition();
            Vector2 distance = new Vector2(0, 0);
            distance.add(heroPos).sub(particlePos);

            particle.setNormaliseVector(distance.nor());

            if (!particle.isColliding()) {
                particle.getBody().setLinearVelocity(particle.getNormaliseVector().x * GameConstants.SPEED, particle.getNormaliseVector().y * GameConstants.SPEED);
            }

            if (particle.isColliding()) {
                int count = particle.getCounter();
                count++;
                particle.setCounter(count);
            }

            if (particle.getCounter() > 5) {
                particle.setColliding(false);
                particle.setCounter(0);
            }

            particle.getSprite().setPosition((particle.getBody().getPosition().x * GameConstants.PIXELS_TO_METERS) - particle.getSprite().
                            getWidth() / 2,
                    (particle.getBody().getPosition().y * GameConstants.PIXELS_TO_METERS) - particle.getSprite().getHeight() / 2
            );
        }
        /*for (Particle particle : particles) {
            *//*Vector2 heroPos1 = hero.getBody().getWorldCenter();
            Vector2 particlePos1 = particle.getBody().getWorldCenter();
            Vector2 distance1 = new Vector2(0, 0);
            distance1.add(particlePos1).sub(heroPos1);
            Vector2 invDist = new Vector2(-distance1.x,-distance1.y);
            particle.getBody().applyForce(invDist,particlePos1,true);*//*
            Vector2 heroPos = hero.getBody().getPosition();
            Vector2 particlePos = particle.getBody().getPosition();
            Vector2 distance = new Vector2(0, 0);
            distance.add(heroPos).sub(particlePos);

            particle.setNormaliseVector(distance.nor());

            if (!particle.isColliding()) {
                particle.getBody().setLinearVelocity(particle.getNormaliseVector().x * GameConstants.SPEED, particle.getNormaliseVector().y * GameConstants.SPEED);
            }

            if (particle.isColliding()) {
                int count = particle.getCounter();
                count++;
                particle.setCounter(count);
            }

            if (particle.getCounter() > 5) {
                particle.setColliding(false);
                particle.setCounter(0);
            }

            particle.getSprite().setPosition((particle.getBody().getPosition().x * GameConstants.PIXELS_TO_METERS) - particle.getSprite().
                            getWidth() / 2,
                    (particle.getBody().getPosition().y * GameConstants.PIXELS_TO_METERS) - particle.getSprite().getHeight() / 2
            );

            //particle.getSprite().setRotation((float) Math.toDegrees(particle.getBody().getAngle()));
        }*/
    }

    private void updateSpikes() {
        for (Saw saw : saws) {
            saw.getSprite().setPosition((saw.getBody().getPosition().x * GameConstants.PIXELS_TO_METERS) - saw.getSprite().
                            getWidth() / 2,
                    (saw.getBody().getPosition().y * GameConstants.PIXELS_TO_METERS) - saw.getSprite().getHeight() / 2
            );
        }
    }

    class reactorContactListener implements ContactListener {

        @Override
        public void beginContact(Contact contact) {

        }

        @Override
        public void endContact(Contact contact) {
            if (contact.getFixtureA().getFilterData().categoryBits == GameConstants.SPRITE_2 && contact.getFixtureB().getFilterData().categoryBits == GameConstants.SPRITE_1) {
                Particle particle = particleHashMap.get(contact.getFixtureB().getBody().getUserData().toString());
                Vector2 inverseNormal = new Vector2(-particle.getNormaliseVector().x * GameConstants.COLLISION_SPEED, -particle.getNormaliseVector().y * GameConstants.COLLISION_SPEED);
                contact.getFixtureB().getBody().setLinearVelocity(inverseNormal);
                particle.setColliding(true);
            }
            if (contact.getFixtureA().getFilterData().categoryBits == GameConstants.SPRITE_1 && contact.getFixtureB().getFilterData().categoryBits == GameConstants.SPRITE_1) {
                Particle particle = particleHashMap.get(contact.getFixtureB().getBody().getUserData().toString());
                particle.setColliding(true);
                if (particle.getCollisionCount() > 3) {
                    Vector2 inverseNormal = new Vector2(-particle.getNormaliseVector().x * GameConstants.COLLISION_SPEED, -particle.getNormaliseVector().y * GameConstants.COLLISION_SPEED);
                    contact.getFixtureA().getBody().setLinearVelocity(inverseNormal);
                    particle.setCollisionCount(0);
                } else {
                    int count = particle.getCollisionCount();
                    count++;
                    particle.setCollisionCount(count);
                }
            }
            if (contact.getFixtureA().getFilterData().categoryBits == GameConstants.SPRITE_1 && contact.getFixtureB().getFilterData().categoryBits == GameConstants.SPRITE_3) {
                //remove particles
                if (!particlesForRemoval.contains(contact.getFixtureA().getBody().getUserData().toString())) {
                    particlesForRemoval.add(contact.getFixtureA().getBody().getUserData().toString());
                }
            }
            if (contact.getFixtureA().getFilterData().categoryBits == GameConstants.SPRITE_2 && contact.getFixtureB().getFilterData().categoryBits == GameConstants.SPRITE_3) {
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
