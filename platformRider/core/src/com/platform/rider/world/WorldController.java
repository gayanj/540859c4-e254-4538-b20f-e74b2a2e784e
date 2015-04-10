package com.platform.rider.world;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.platform.rider.assets.Assets;
import com.platform.rider.screens.MenuScreen;
import com.platform.rider.sprites.Explosion;
import com.platform.rider.sprites.Hero;
import com.platform.rider.sprites.Particle;
import com.platform.rider.sprites.Saw;
import com.platform.rider.utils.BlastRadiusCallBack;
import com.platform.rider.utils.GameConstants;
import com.platform.rider.utils.OverlapTester;
import com.platform.rider.utils.TouchPadHelper;

import java.util.*;

/**
 * Created by Gayan on 3/23/2015.
 */
public class WorldController {
    private static final String TAG = WorldController.class.getName();
    private Game game;
    public static float scaledWidth;
    public static float scaledHeight;
    public OrthographicCamera camera;
    private Viewport viewport;
    public World world;
    public Hero hero;
    public Explosion explosion;
    public HashMap<String, Explosion> explosionHashMap = new HashMap<String, Explosion>();
    public HashMap<String, Particle> particleHashMap = new HashMap<String, Particle>();
    public List<String> normalParticlesForRemoval = new ArrayList<String>();
    public List<String> explosionsForRemoval = new ArrayList<String>();
    public List<String> splitParticlesForRemoval = new ArrayList<String>();
    public HashMap<String, Saw> spikeHashMap = new HashMap<String, Saw>();
    public TouchPadHelper touchPadHelper;
    int totalParticlesCreated = 0;
    public int totalParticlesDestroyed = 0;
    int suicideParticlesAlive = 0;
    int splitParticlesAlive = 0;
    int totalParticlesAlive = 0;
    int stage = 0;
    boolean gameOver = false;
    Array<Vector2> splitParticlePosition = new Array<Vector2>();

    public WorldController(Game game) {
        scaledWidth = GameConstants.APP_WIDTH * 1.5f;
        scaledHeight = GameConstants.APP_HEIGHT * 1.5f;
        this.game = game;
        init();
    }

    private void init() {
        initTouchpad();
        Gdx.input.setInputProcessor(touchPadHelper.getStage());
        camera = new OrthographicCamera(GameConstants.APP_WIDTH, GameConstants.APP_HEIGHT);
        viewport = new FitViewport(GameConstants.APP_WIDTH, GameConstants.APP_HEIGHT, camera);
        initPhysics();
        world.setContactListener(new reactorContactListener());
    }

    public void resize(int width, int height) {
        viewport.update(width, height);
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
        hero = new Hero(new Vector2(0, 0), world);
    }

    private void createExplosion(int explosionIndex) {
        explosion = new Explosion(explosionIndex);
        explosionHashMap.put(String.valueOf(explosionIndex), explosion);
    }

    private void createParticles() {
        for (int i = 0; i < 4; i++) {
            createNewParticle(GameConstants.NORMAL_PARTICLE);
        }
    }

    private void createNewParticle(String type) {
        Random r = new Random();
        int xLow = -(GameConstants.APP_WIDTH / 2 - 100);
        int xHigh = GameConstants.APP_WIDTH / 2 - 100;
        int xR = r.nextInt(xHigh - xLow) + xLow;

        int yLow = -(GameConstants.APP_WIDTH / 2 - 100);
        int yHigh = GameConstants.APP_WIDTH / 2 - 100;
        int yR = r.nextInt(yHigh - yLow) + yLow;
        Vector2 position = new Vector2(xR, yR);
        Particle particle = new Particle(position, world, totalParticlesCreated, type);
        //increment the number of particles created count
        particleHashMap.put(String.valueOf(totalParticlesCreated++), particle);
        totalParticlesAlive++;
    }

    private void createSplitParticle(Vector2 position) {
        Particle particle = new Particle(position, world, totalParticlesCreated, GameConstants.SPLIT_PARTICLE);
        //increment the number of particles created count
        particleHashMap.put(String.valueOf(totalParticlesCreated++), particle);
        totalParticlesAlive++;
    }

    private void createSpikes() {
        for (int i = 0; i < (GameConstants.APP_HEIGHT / 130) + 1; i++) {
            int yscale = 2 * (i + 1);
            Saw saw = new Saw(0, yscale, world, "R");
            spikeHashMap.put("R" + i, saw);
        }
        for (int i = 0; i < (GameConstants.APP_HEIGHT / 130) + 1; i++) {
            int yscale = 2 * (i + 1);
            Saw saw = new Saw(0, yscale, world, "L");
            spikeHashMap.put("L" + i, saw);
        }
        for (int i = 0; i < (GameConstants.APP_WIDTH / 130) + 1; i++) {
            int xscale = 2 * (i + 1);
            Saw saw = new Saw(xscale, 0, world, "U");
            spikeHashMap.put("U" + i, saw);
        }
        for (int i = 0; i < (GameConstants.APP_WIDTH / 130) + 1; i++) {
            int xscale = 2 * (i + 1);
            Saw saw = new Saw(xscale, 0, world, "D");
            spikeHashMap.put("D" + i, saw);
        }
    }

    public void update(float deltaTime) {
        if (gameOver) {
            handleGameOver();
        } else {
            world.step(deltaTime, 8, 3);
            destroyParticles();
            destroyExplosions();
            checkStage();
            splitParticles();
            createSuicideParticles();
            camera.update();
            //Touch pad readings
            Vector2 touchPadVec = new Vector2(touchPadHelper.getTouchpad().getKnobPercentX() * 10f, touchPadHelper.getTouchpad().getKnobPercentY() * 10f);
            //If touchpad readings are zero dont alter velocity of hero
            if (touchPadVec.x != 0 && touchPadVec.y != 0) {
                hero.getBody().setLinearVelocity(touchPadVec);
            }
            //handleHeroPowerUp();
            updateHero();
            updateParticles();
            updateSpikes();
        }
    }

    private void updateHero() {
        hero.getSprite().setPosition((hero.getBody().getPosition().x * GameConstants.PIXELS_TO_METERS) - hero.getSprite().
                        getWidth() / 2,
                (hero.getBody().getPosition().y * GameConstants.PIXELS_TO_METERS) - hero.getSprite().getHeight() / 2
        );
    }

    private void destroyParticles() {
        for (String particleKey : normalParticlesForRemoval) {
            String type = particleHashMap.get(particleKey).getType();
            if (type.equals(GameConstants.SUICIDE_PARTICLE)) {
                particleHashMap.get(particleKey).setAnimatedSprite(null);
            } else {
                particleHashMap.get(particleKey).setSprite(null);
            }
            final Array<JointEdge> list = particleHashMap.get(particleKey).getBody().getJointList();
            while (list.size > 0) {
                world.destroyJoint(list.get(0).joint);
            }
            world.destroyBody(particleHashMap.get(particleKey).getBody());
            particleHashMap.remove(particleKey);
            //increment the number of destroyed particles
            totalParticlesDestroyed++;
            totalParticlesAlive--;
            //create a new particle
            if (type.equals(GameConstants.NORMAL_PARTICLE)) {
                createNewParticle(GameConstants.NORMAL_PARTICLE);
            } else if (type.equals(GameConstants.SPLIT_PARTICLE)) {
                splitParticlesAlive--;
            } else if (type.equals(GameConstants.SUICIDE_PARTICLE)) {
                GameConstants.FRAME_DURATION = 0.025f;
                suicideParticlesAlive--;
            }
        }
        normalParticlesForRemoval.clear();
    }

    private void destroyAllParticles() {
        for (Map.Entry<String, Particle> entry : particleHashMap.entrySet()) {
            String type = entry.getValue().getType();
            if (type.equals(GameConstants.SUICIDE_PARTICLE)) {
                entry.getValue().setAnimatedSprite(null);
            } else {
                entry.getValue().setSprite(null);
            }
            final Array<JointEdge> particleJointList = entry.getValue().getBody().getJointList();
            while (particleJointList.size > 0) {
                world.destroyJoint(particleJointList.get(0).joint);
            }
            world.destroyBody(entry.getValue().getBody());
        }
        particleHashMap.clear();
        destroyHero();
        destroySaws();
    }

    private void destroyHero() {
        hero.setSprite(null);
        final Array<JointEdge> heroJointList = hero.getBody().getJointList();
        while (heroJointList.size > 0) {
            world.destroyJoint(heroJointList.get(0).joint);
        }
        world.destroyBody(hero.getBody());
    }

    private void destroySaws() {
        for (Map.Entry<String, Saw> entry : spikeHashMap.entrySet()) {
            entry.getValue().setAnimatedSprite(null);
            final Array<JointEdge> sawJointList = entry.getValue().getBody().getJointList();
            while (sawJointList.size > 0) {
                world.destroyJoint(sawJointList.get(0).joint);
            }
            world.destroyBody(entry.getValue().getBody());
        }
    }

    private void destroyExplosions() {
        for (String explosion : explosionsForRemoval) {
            explosionHashMap.remove(explosion);
        }
        explosionsForRemoval.clear();
    }

    private void destroySplitParticles() {
        for (String particleKey : splitParticlesForRemoval) {
            particleHashMap.get(particleKey).setSprite(null);
            final Array<JointEdge> list = particleHashMap.get(particleKey).getBody().getJointList();
            while (list.size > 0) {
                world.destroyJoint(list.get(0).joint);
            }
            world.destroyBody(particleHashMap.get(particleKey).getBody());
            particleHashMap.remove(particleKey);
            //increment the number of destroyed particles
            totalParticlesDestroyed++;
            splitParticlesAlive--;
            totalParticlesAlive--;
        }
        splitParticlesForRemoval.clear();
    }

    private void checkStage() {
        if (totalParticlesDestroyed > 0 && totalParticlesDestroyed % 10 == 0 && GameConstants.NORMAL_PARTICAL_SPEED <= 7) {
            stage++;
            GameConstants.NORMAL_PARTICAL_SPEED++;
        }
    }

    private void checkSuicideParticle(Particle particle) {
        if (particle.getBlastTimer() > 500) {
            BlastRadiusCallBack blastRadiusCallBack = new BlastRadiusCallBack();
            Vector2 center = particle.getBody().getPosition();
            Vector2 aabbLowerBound = new Vector2(center);
            Vector2 aabbUpperBound = new Vector2(center);
            aabbLowerBound = aabbLowerBound.sub(new Vector2(GameConstants.BLAST_RADIUS, GameConstants.BLAST_RADIUS));
            aabbUpperBound = aabbUpperBound.add(new Vector2(GameConstants.BLAST_RADIUS, GameConstants.BLAST_RADIUS));


            world.QueryAABB(blastRadiusCallBack, aabbLowerBound.x, aabbLowerBound.y, aabbUpperBound.x, aabbUpperBound.y);
            for (int i = 0; i < blastRadiusCallBack.getFoundBodies().size(); i++) {
                Body body = blastRadiusCallBack.getFoundBodies().get(i);
                Vector2 bodyWorldCenter = body.getWorldCenter();

                //ignore bodies outside the blast range
                if ((bodyWorldCenter.sub(center)).len() >= GameConstants.BLAST_RADIUS)
                    continue;

                if ("hero".equals(body.getUserData().toString())) {
                    gameOver = true;
                }
                if (!normalParticlesForRemoval.contains(body.getUserData().toString())) {
                    normalParticlesForRemoval.add(body.getUserData().toString());
                }
            }
            System.out.println("Particle index: " + particle.getBody().getUserData());
            System.out.println("Explosion index: " + explosionHashMap.keySet());
            explosionHashMap.get(particle.getBody().getUserData().toString()).setBlast(true);
            explosionHashMap.get(particle.getBody().getUserData().toString()).setBlastPosition(center);
        } else {
            int count = particle.getBlastTimer();
            count++;
            particle.setBlastTimer(count);
            particle.updateFrameDuration();
        }
    }

    private void splitParticles() {
        if (totalParticlesDestroyed > 0 && totalParticlesDestroyed % 10 == 0 && splitParticlesAlive == 0 && totalParticlesAlive < 20) {
            createNewParticle(GameConstants.SPLIT_PARTICLE);
            splitParticlesAlive++;
        }
        for (Vector2 plarticlePosition : splitParticlePosition) {
            //increment the number of particles created count
            createSplitParticle(plarticlePosition);
            splitParticlesAlive++;
        }
        splitParticlePosition.clear();
    }

    private void createSuicideParticles() {
        if (totalParticlesDestroyed > 0 && totalParticlesDestroyed % 10 == 0 && suicideParticlesAlive == 0) {
            //for (int i = 0; i < stage; i++) {
            createExplosion(totalParticlesCreated);
            createNewParticle(GameConstants.SUICIDE_PARTICLE);
            suicideParticlesAlive++;
            createNewParticle(GameConstants.INVISIBLE_PARTICLE);
            //}
        }
    }

    private void createInvisibleParticles() {
        if (totalParticlesDestroyed > 0 && totalParticlesDestroyed % 20 == 0) {
            //for (int i = 0; i < stage; i++) {
            createNewParticle(GameConstants.INVISIBLE_PARTICLE);
            //}
        }
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
                particle.getBody().setLinearVelocity(particle.getNormaliseVector().x * particle.getSpeed(), particle.getNormaliseVector().y * particle.getSpeed());
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

            if (GameConstants.SPLIT_PARTICLE.equals(particle.getType())) {
                updateSplitParticleCount(particle);
            } else if (GameConstants.SUICIDE_PARTICLE.equals(particle.getType())) {
                checkSuicideParticle(particle);
            }

            if (GameConstants.INVISIBLE_PARTICLE.equals(particle.getType())) {
                updateInvisibleParticles(particle);
            } else if (GameConstants.SUICIDE_PARTICLE.equals(particle.getType())) {
                updateSuicideParticles(particle);
            } else {
                particle.getSprite().setPosition((particle.getBody().getPosition().x * GameConstants.PIXELS_TO_METERS) - particle.getSprite().
                                getWidth() / 2,
                        (particle.getBody().getPosition().y * GameConstants.PIXELS_TO_METERS) - particle.getSprite().getHeight() / 2
                );
            }
        }
    }

    private void updateSpikes() {
        /*for (Map.Entry<String, Saw> entry : spikeHashMap.entrySet()) {
            Saw saw = entry.getValue();
            saw.getAnimatedSprite().setPosition((saw.getBody().getPosition().x * GameConstants.PIXELS_TO_METERS) - saw.getAnimatedSprite().
                            getWidth() / 2,
                    (saw.getBody().getPosition().y * GameConstants.PIXELS_TO_METERS) - saw.getAnimatedSprite().getHeight() / 2
            );
        }*/
    }

    private void updateSuicideParticles(Particle particle) {
        particle.getAnimatedSprite().setPosition((particle.getBody().getPosition().x * GameConstants.PIXELS_TO_METERS) - particle.getAnimatedSprite().
                        getWidth() / 2,
                (particle.getBody().getPosition().y * GameConstants.PIXELS_TO_METERS) - particle.getAnimatedSprite().getHeight() / 2
        );
    }

    private void updateInvisibleParticles(Particle particle) {
        particle.getAnimatedSprite().setPosition((particle.getBody().getPosition().x * GameConstants.PIXELS_TO_METERS) - particle.getAnimatedSprite().
                        getWidth() / 2,
                (particle.getBody().getPosition().y * GameConstants.PIXELS_TO_METERS) - particle.getAnimatedSprite().getHeight() / 2
        );
    }

    private void updateSplitParticleCount(Particle particle) {
        if (stage >= 2 && particle.getSplitParticleCount() > 200 && splitParticlesAlive < 20) {
            //set split particle position
            Vector2 particlePosition = new Vector2(particle.getBody().getPosition().x * GameConstants.PIXELS_TO_METERS - particle.getSprite().
                    getWidth() / 2, particle.getBody().getPosition().y * GameConstants.PIXELS_TO_METERS - particle.getSprite().
                    getHeight() / 2);
            splitParticlePosition.add(particlePosition);
            particle.setSplitParticleCount(0);
        } else if (splitParticlesAlive < 20) {
            int count = particle.getSplitParticleCount();
            count++;
            particle.setSplitParticleCount(count);
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

            if (contact.getFixtureA().getFilterData().categoryBits == GameConstants.SPRITE_1 && contact.getFixtureB().getFilterData().categoryBits == GameConstants.SPRITE_3) {
                //remove particles
                if (!normalParticlesForRemoval.contains(contact.getFixtureA().getBody().getUserData().toString())) {
                    normalParticlesForRemoval.add(contact.getFixtureA().getBody().getUserData().toString());
                }
            }
            if (contact.getFixtureA().getFilterData().categoryBits == GameConstants.SPRITE_3 && contact.getFixtureB().getFilterData().categoryBits == GameConstants.SPRITE_1) {
                /*//remove particles
                if (!splitParticlesForRemoval.contains(contact.getFixtureB().getBody().getUserData().toString())) {
                    splitParticlesForRemoval.add(contact.getFixtureB().getBody().getUserData().toString());
                }*/
                //remove particles
                if (!normalParticlesForRemoval.contains(contact.getFixtureB().getBody().getUserData().toString())) {
                    normalParticlesForRemoval.add(contact.getFixtureB().getBody().getUserData().toString());
                }
            }
            if (contact.getFixtureA().getFilterData().categoryBits == GameConstants.SPRITE_2 && contact.getFixtureB().getFilterData().categoryBits == GameConstants.SPRITE_3) {
                gameOver = true;
            }
            if (contact.getFixtureA().getFilterData().categoryBits == GameConstants.SPRITE_2 && contact.getFixtureB().getFilterData().categoryBits == GameConstants.SPRITE_4) {
                gameOver = true;
            }
        }

        @Override
        public void preSolve(Contact contact, Manifold oldManifold) {

        }

        @Override
        public void postSolve(Contact contact, ContactImpulse impulse) {

        }
    }

    private void backToMenu() {
        // switch to menu screen
        game.setScreen(new MenuScreen(game));
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public void handleGameOver() {
        if (gameOver && Gdx.input.justTouched()) {
            Vector2 touchPoint = new Vector2(Gdx.input.getX(), Gdx.input.getY());
            Rectangle gameoverBound = new Rectangle(
                    (GameConstants.APP_WIDTH / 2) - (Assets.instance.fonts.defaultBig.getCache().getBounds().width / 2),
                    (GameConstants.APP_HEIGHT / 2),
                    Assets.instance.fonts.defaultBig.getCache().getBounds().width,
                    Assets.instance.fonts.defaultBig.getCache().getBounds().height);
            //if (OverlapTester.pointInRectangle(gameoverBound, touchPoint)) {
            destroyAllParticles();
            GameConstants.NORMAL_PARTICAL_SPEED = 5f;
            backToMenu();
            //}
        }
    }

    public void handleHeroPowerUp() {
        for (int i = 0; i < 2; i++) {
            if (Gdx.input.isTouched(i)) {
                Vector2 touchPoint = new Vector2(Gdx.input.getX(i), Gdx.input.getY(i));
                Rectangle gameoverBound = new Rectangle(
                        15,
                        (1278 - Assets.instance.assetLevelDecoration.powerbutton.getRotatedPackedHeight()),
                        Assets.instance.assetLevelDecoration.powerbutton.getRotatedPackedWidth(),
                        Assets.instance.assetLevelDecoration.powerbutton.getRotatedPackedHeight());
                if (OverlapTester.pointInRectangle(gameoverBound, touchPoint)) {
                    GameConstants.COLLISION_SPEED = 20f;
                }
            } else {
                GameConstants.COLLISION_SPEED = 10f;
            }
        }
    }
}
