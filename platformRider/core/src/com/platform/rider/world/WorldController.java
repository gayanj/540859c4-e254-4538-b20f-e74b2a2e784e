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
import com.platform.rider.sprites.*;
import com.platform.rider.utils.*;

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
    public ParticleBurstAnimation particleBurstAnimation;
    public Powerups powerups;
    public HashMap<String, ParticleBurstAnimation> particleBurstHashMap = new HashMap<String, ParticleBurstAnimation>();
    public HashMap<String, Power> powerupHashMap = new HashMap<String, Power>();
    public HashMap<String, Explosion> explosionHashMap = new HashMap<String, Explosion>();
    public HashMap<String, Particle> particleHashMap = new HashMap<String, Particle>();
    public List<String> normalParticlesForRemoval = new ArrayList<String>();
    public List<String> explosionsForRemoval = new ArrayList<String>();
    public List<String> particleBurstsForRemoval = new ArrayList<String>();
    public List<String> powerupsForRemoval = new ArrayList<String>();
    public List<String> deathSawsForRemoval = new ArrayList<String>();
    public HashMap<String, Saw> spikeHashMap = new HashMap<String, Saw>();
    public HashMap<String, DeathSaw> deathSawHashMap = new HashMap<String, DeathSaw>();
    public TouchPadHelper touchPadHelper;
    int totalParticlesCreated = 0;
    int totalParticlesDestroyed = 0;
    int powerUpsCreated = 0;
    int score = 0;
    int suicideParticlesAlive = 0;
    int splitParticlesAlive = 0;
    int totalParticlesAlive = 0;
    int stage = 0;
    int invisibleParticleCounter = 0;
    int deathSawCounter = 0;
    int powerUpCounter = 0;
    boolean gameOver = false;
    Array<Vector2> splitParticlePosition = new Array<Vector2>();

    int bonusCounter = 0;
    int bonusStreak = 0;
    int particleStreakCount = 0;
    boolean startBonusCounter = false;
    boolean addStreak = false;

    boolean increaseDifficulty = true;

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
        powerups = new Powerups();
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

    private void createParticleBurst(String burstIndex, Vector2 position, String type) {
        particleBurstAnimation = new ParticleBurstAnimation(burstIndex, position, type);
        particleBurstHashMap.put(String.valueOf(burstIndex), particleBurstAnimation);
    }

    private void createParticles() {
        for (int i = 0; i < 4; i++) {
            createNewParticle(GameConstants.NORMAL_PARTICLE);
        }
    }

    private void createPowerUp(String type) {
        Random r = new Random();
        int xLow = -(GameConstants.APP_WIDTH / 2 - 100);
        int xHigh = GameConstants.APP_WIDTH / 2 - 100;
        int xR = r.nextInt(xHigh - xLow) + xLow;

        int yLow = -(GameConstants.APP_HEIGHT / 2 - 100);
        int yHigh = GameConstants.APP_HEIGHT / 2 - 100;
        int yR = r.nextInt(yHigh - yLow) + yLow;
        Vector2 position = new Vector2(xR, yR);
        Power power = new Power(position, world, type, "power" + powerUpsCreated);
        powerupHashMap.put(String.valueOf("power" + powerUpsCreated++), power);
    }

    private void createNewParticle(String type) {
        Random r = new Random();
        int xLow = -(GameConstants.APP_WIDTH / 2 - 100);
        int xHigh = GameConstants.APP_WIDTH / 2 - 100;
        int xR = r.nextInt(xHigh - xLow) + xLow;

        int yLow = -(GameConstants.APP_HEIGHT / 2 - 100);
        int yHigh = GameConstants.APP_HEIGHT / 2 - 100;
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

    private void createDeathSaw() {
        if (totalParticlesDestroyed > 5 && deathSawCounter > 500) {
            Random r = new Random();
            int side = r.nextInt(4);
            int vposition = r.nextInt((GameConstants.APP_HEIGHT / 130) - 1 - 1) + 1;
            int hposition = r.nextInt((GameConstants.APP_WIDTH / 130) - 1 - 1) + 1;
            if (side == 0) {//R
                int yscale = 2 * (vposition + 1);
                DeathSaw deathSaw = new DeathSaw(0, yscale, world, "R", "R" + vposition);
                deathSawHashMap.put("R" + vposition, deathSaw);
            } else if (side == 1) {//L
                int yscale = 2 * (vposition + 1);
                DeathSaw deathSaw = new DeathSaw(0, yscale, world, "L", "L" + vposition);
                deathSawHashMap.put("L" + vposition, deathSaw);
            } else if (side == 2) {//U
                int xscale = 2 * (hposition + 1);
                DeathSaw deathSaw = new DeathSaw(xscale, 0, world, "U", "U" + hposition);
                deathSawHashMap.put("U" + hposition, deathSaw);
            } else if (side == 3) {//D
                int xscale = 2 * (hposition + 1);
                DeathSaw deathSaw = new DeathSaw(xscale, 0, world, "D", "D" + hposition);
                deathSawHashMap.put("D" + hposition, deathSaw);
            }
            deathSawCounter = 0;
        } else {
            deathSawCounter++;
        }
    }

    public void update(float deltaTime) {
        if (gameOver) {
            handleGameOver();
        } else {
            world.step(deltaTime, 8, 3);
            destroyParticles();
            destroyExplosions();
            destroyParticleBursts();
            destroyPowerups();
            destroyDeathSaws();
            increaseDifficulty();
            checkBonusStreak();
            checkPowerups();
            createRandomPowerup();
            createSplitParticles();
            createSuicideParticles();
            createInvisibleParticles();
            createDeathSaw();
            camera.update();
            //Touch pad readings
            Vector2 touchPadVec = new Vector2(touchPadHelper.getTouchpad().getKnobPercentX() * GameConstants.HERO_SPEED, touchPadHelper.getTouchpad().getKnobPercentY() * GameConstants.HERO_SPEED);
            //If touchpad readings are zero dont alter velocity of hero
            if (touchPadVec.x != 0 && touchPadVec.y != 0) {
                hero.getBody().setLinearVelocity(touchPadVec);
            }
            handleHeroPowerUp();
            updateHero();
            updateParticles();
            updateSpikes();
            updateDeathSaws();
            updatePowerupCounter();
            updatePowerupRemovals();
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
            if (type.equals(GameConstants.SUICIDE_PARTICLE) || type.equals(GameConstants.INVISIBLE_PARTICLE)) {
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
            score++;
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
            if (startBonusCounter) {
                particleStreakCount++;
                bonusStreak++;
                bonusCounter = 0;
            } else {
                startBonusCounter = true;
            }
            increaseDifficulty = true;
        }
        normalParticlesForRemoval.clear();
    }

    private void destroyPowerups() {
        for (String powerupKey : powerupsForRemoval) {
            powerupHashMap.get(powerupKey).setSprite(null);
            final Array<JointEdge> list = powerupHashMap.get(powerupKey).getBody().getJointList();
            while (list.size > 0) {
                world.destroyJoint(list.get(0).joint);
            }
            world.destroyBody(powerupHashMap.get(powerupKey).getBody());
            powerupHashMap.remove(powerupKey);
        }
        powerupsForRemoval.clear();
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

    private void destroyDeathSaws() {
        for (String deathSawKey : deathSawsForRemoval) {
            deathSawHashMap.get(deathSawKey).setAnimatedSprite(null);
            final Array<JointEdge> list = deathSawHashMap.get(deathSawKey).getBody().getJointList();
            while (list.size > 0) {
                world.destroyJoint(list.get(0).joint);
            }
            world.destroyBody(deathSawHashMap.get(deathSawKey).getBody());
            deathSawHashMap.remove(deathSawKey);
        }
        deathSawsForRemoval.clear();
    }

    private void destroyExplosions() {
        for (String explosion : explosionsForRemoval) {
            explosionHashMap.remove(explosion);
        }
        explosionsForRemoval.clear();
    }

    private void destroyParticleBursts() {
        for (String explosion : particleBurstsForRemoval) {
            particleBurstHashMap.remove(explosion);
        }
        particleBurstsForRemoval.clear();
    }

    private void increaseDifficulty() {
        if (totalParticlesDestroyed > 0 && totalParticlesDestroyed % 10 == 0 && increaseDifficulty) {
            if (GameConstants.NORMAL_PARTICAL_SPEED <= 9) {
                GameConstants.NORMAL_PARTICAL_SPEED++;
            }
            if (GameConstants.SPLIT_PARTICAL_TIME > 90) {
                GameConstants.SPLIT_PARTICAL_TIME -= 10;
            }
            increaseDifficulty = false;
        }
    }

    private void checkBonusStreak() {
        if (startBonusCounter && bonusCounter < 100) {
            bonusCounter++;
        } else {
            startBonusCounter = false;
            bonusCounter = 0;
            addStreak = true;
        }
        if (addStreak) {
            if (bonusStreak > 0) {
                score += bonusStreak * particleStreakCount;
            }
            particleStreakCount = 0;
            bonusStreak = 0;
            addStreak = false;
        }
    }

    private void checkPowerups() {
        if (powerups.isActive() && powerups.getType().equals(GameConstants.SUPER_FORCE)) {
            GameConstants.COLLISION_SPEED = 30f;
            int powerCount = powerups.getPowerCounter();
            powerCount++;
            powerups.setPowerCounter(powerCount);
        } else {
            GameConstants.COLLISION_SPEED = 20f;
        }
    }

    private void updatePowerupCounter() {
        if (powerups.getPowerCounter() > 500) {
            powerups.setActive(false);
            powerups.setPowerCounter(0);
            if (powerups.getRemaining() == 0) {
                powerups.setPickedUp(false);
            }
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
                if ((bodyWorldCenter.sub(center)).len() >= GameConstants.BLAST_RADIUS) {
                    continue;
                }

                if ("hero".equals(body.getUserData().toString())) {
                    gameOver = true;
                }
                if (!body.getUserData().toString().contains("power".toLowerCase()) && !normalParticlesForRemoval.contains(body.getUserData().toString())) {
                    normalParticlesForRemoval.add(body.getUserData().toString());
                }
            }
            explosionHashMap.get(particle.getBody().getUserData().toString()).getAnimatedSprite().setPosition(
                    (center.x * GameConstants.PIXELS_TO_METERS) - explosionHashMap.get(particle.getBody().getUserData().toString()).getAnimatedSprite().getWidth() / 2,
                    (center.y * GameConstants.PIXELS_TO_METERS) - explosionHashMap.get(particle.getBody().getUserData().toString()).getAnimatedSprite().getHeight() / 2
            );
            explosionHashMap.get(particle.getBody().getUserData().toString()).setBlast(true);
        } else {
            int count = particle.getBlastTimer();
            count++;
            particle.setBlastTimer(count);
            particle.updateFrameDuration();
        }
    }

    private void createSplitParticles() {
        if (totalParticlesDestroyed > 0 && totalParticlesDestroyed % 10 == 0 && splitParticlesAlive == 0) {
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
        if (totalParticlesDestroyed > 0 && totalParticlesDestroyed % 15 == 0 && suicideParticlesAlive == 0) {
            for (int i = 0; i < GameConstants.SUICIDE_PARTICAL_COUNT; i++) {
                createExplosion(totalParticlesCreated);
                createNewParticle(GameConstants.SUICIDE_PARTICLE);
                suicideParticlesAlive++;
            }
            if (GameConstants.SUICIDE_PARTICAL_COUNT < 6) {
                GameConstants.SUICIDE_PARTICAL_COUNT++;
            }
        }
    }

    private void createInvisibleParticles() {
        if (totalParticlesDestroyed > 10 && invisibleParticleCounter > 500) {
            //for (int i = 0; i < stage; i++) {
            createNewParticle(GameConstants.INVISIBLE_PARTICLE);
            invisibleParticleCounter = 0;
            //}
        } else {
            invisibleParticleCounter++;
        }
    }

    private void createRandomPowerup() {
        if (totalParticlesDestroyed > 20 && powerUpCounter > 2000) {
            createPowerUp(GameConstants.SUPER_FORCE);
            powerUpCounter = 0;
        } else {
            powerUpCounter++;
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

    private void updatePowerupRemovals() {
        for (Map.Entry<String, Power> entry : powerupHashMap.entrySet()) {
            if (entry.getValue().isPickedUp()) {
                if (!powerupsForRemoval.contains(entry.getValue().getBody().getUserData().toString())) {
                    powerupsForRemoval.add(entry.getValue().getBody().getUserData().toString());
                }
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

    private void updateDeathSaws() {
        for (Map.Entry<String, DeathSaw> entry : deathSawHashMap.entrySet()) {
            DeathSaw deathSaw = entry.getValue();
            deathSaw.getAnimatedSprite().setPosition((deathSaw.getBody().getPosition().x * GameConstants.PIXELS_TO_METERS) - deathSaw.getAnimatedSprite().
                            getWidth() / 2,
                    (deathSaw.getBody().getPosition().y * GameConstants.PIXELS_TO_METERS) - deathSaw.getAnimatedSprite().getHeight() / 2
            );
            if (deathSaw.getAnimatedSprite().getX() < (-deathSaw.getAnimatedSprite().getWidth()) - GameConstants.APP_WIDTH / 2
                    || deathSaw.getAnimatedSprite().getX() > (deathSaw.getAnimatedSprite().getWidth()) + GameConstants.APP_WIDTH / 2
                    || deathSaw.getAnimatedSprite().getY() > (deathSaw.getAnimatedSprite().getHeight()) + GameConstants.APP_HEIGHT / 2
                    || deathSaw.getAnimatedSprite().getY() < (-deathSaw.getAnimatedSprite().getHeight()) - GameConstants.APP_HEIGHT / 2) {
                deathSawsForRemoval.add(deathSaw.getBody().getUserData().toString());
            }
        }
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
        if (particle.getSplitParticleCount() > GameConstants.SPLIT_PARTICAL_TIME && splitParticlesAlive < 20) {
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
                createParticleBurst(contact.getFixtureA().getBody().getUserData().toString(), contact.getFixtureA().getBody().getPosition(), particleHashMap.get(contact.getFixtureA().getBody().getUserData().toString()).getType());
                if (!normalParticlesForRemoval.contains(contact.getFixtureA().getBody().getUserData().toString())) {
                    normalParticlesForRemoval.add(contact.getFixtureA().getBody().getUserData().toString());
                }
            }
            if (contact.getFixtureA().getFilterData().categoryBits == GameConstants.SPRITE_3 && contact.getFixtureB().getFilterData().categoryBits == GameConstants.SPRITE_1) {
                //remove particles
                createParticleBurst(contact.getFixtureB().getBody().getUserData().toString(), contact.getFixtureB().getBody().getPosition(), particleHashMap.get(contact.getFixtureB().getBody().getUserData().toString()).getType());
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

            if (contact.getFixtureA().getFilterData().categoryBits == GameConstants.SPRITE_2 && contact.getFixtureB().getFilterData().categoryBits == GameConstants.SPRITE_6) {
                Power power = powerupHashMap.get(contact.getFixtureB().getBody().getUserData().toString());
                powerups.setType(power.getType());
                powerups.setRemaining(2);
                powerups.setTextureRegion(power.getTexureRegion());
                powerups.setPickedUp(true);
                if (!powerupsForRemoval.contains(contact.getFixtureB().getBody().getUserData().toString())) {
                    powerupsForRemoval.add(contact.getFixtureB().getBody().getUserData().toString());
                }
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
            GameConstants.SPLIT_PARTICAL_TIME = 200;
            GameConstants.SUICIDE_PARTICAL_COUNT = 1;

            //Reset all particle effects
            for (Map.Entry<String, Particle> entry : particleHashMap.entrySet()) {
                for (int i = entry.getValue().getPooledEffects().size - 1; i >= 0; i--) {
                    entry.getValue().getPooledEffects().get(i).free();
                }
                entry.getValue().getPooledEffects().clear();
            }
            backToMenu();
            //}
        }
    }

    public void handleHeroPowerUp() {
        for (int i = 0; i < 2; i++) {
            if (Gdx.input.isTouched(i)) {
                Vector2 touchPoint = new Vector2(Gdx.input.getX(i), Gdx.input.getY(i));
                //System.out.println(touchPoint);
                Rectangle powerupBound = new Rectangle(
                        15,
                        camera.viewportHeight - Assets.instance.assetLevelDecoration.powerbutton.getRotatedPackedHeight(),
                        Assets.instance.assetLevelDecoration.powerbutton.getRotatedPackedWidth(),
                        Assets.instance.assetLevelDecoration.powerbutton.getRotatedPackedHeight());
                if (OverlapTester.pointInRectangle(powerupBound, touchPoint)) {
                    System.out.println("Touched!");
                    deployPowerup();
                }
            }
        }
    }

    private void deployPowerup() {
        if (!powerups.isActive() && powerups.getRemaining() > 0) {
            //powerups.setType(GameConstants.SUPER_FORCE);
            powerups.setActive(true);
            int remaining = powerups.getRemaining();
            remaining--;
            powerups.setRemaining(remaining);
        }
    }

    public int getBonusStreak() {
        return bonusStreak;
    }

    public int getScore() {
        return score;
    }
}
