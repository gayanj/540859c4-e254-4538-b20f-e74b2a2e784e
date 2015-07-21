package com.platform.rider.world;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.platform.rider.assets.Assets;
import com.platform.rider.main.AnyDirection;
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
    public InstantPowerups instantPowerups;
    public PowerupButton powerupButton;
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
    int instantPowerUpCounter = 0;
    int gameOverCounter = 0;
    boolean gameOver = false;
    Array<Vector2> splitParticlePosition = new Array<Vector2>();

    int bonusCounter = 0;
    int bonusStreak = 0;
    int particleStreakCount = 0;
    boolean startBonusCounter = false;
    boolean addStreak = false;

    boolean increaseDifficulty = true;

    public static float scale = 1;

    public WorldController(Game game) {
        scaledWidth = GameConstants.APP_WIDTH * 1.5f;
        scaledHeight = GameConstants.APP_HEIGHT * 1.5f;
        this.game = game;
        init();
    }

    private void init() {
        initTouchpad();
        InputMultiplexer multiplexer = new InputMultiplexer();
        multiplexer.addProcessor(touchPadHelper.getStage());
        multiplexer.addProcessor(new InputAdapter() {
            public boolean touchUp(int x, int y, int pointer, int button) {
                float xTouchScale = camera.viewportWidth / Gdx.graphics.getWidth();
                float yTouchScale = camera.viewportHeight / Gdx.graphics.getHeight();
                Vector2 touchPoint = new Vector2(x * xTouchScale, y * yTouchScale);
                Rectangle powerupBound = powerupButton.getSprite().getBoundingRectangle();
                if (OverlapTester.pointInRectangle(powerupBound, touchPoint)) {
                    deployPowerup();
                }
                return true;
            }
        });
        Gdx.input.setInputProcessor(multiplexer);
        camera = new OrthographicCamera(GameConstants.APP_WIDTH, GameConstants.APP_HEIGHT);
        viewport = new FitViewport(GameConstants.APP_WIDTH, GameConstants.APP_HEIGHT, camera);
        initPhysics();
        powerups = new Powerups(GameConstants.SUPER_FORCE, 2, Assets.instance.assetPowerup.super_force, true);
        instantPowerups = new InstantPowerups();
        world.setContactListener(new reactorContactListener());
        AudioManager.instance.play(Assets.instance.music.background_music, 1);
    }

    public void resize(int width, int height) {
        viewport.update(width, height);
    }

    private void initPhysics() {
        world = new World(new Vector2(0, 0), true);
        createHero();
        createParticles();
        createSpikes();
        createPowerButton();
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

        int yLow = -(GameConstants.APP_HEIGHT / 2 - 350);
        int yHigh = GameConstants.APP_HEIGHT / 2 - 350;
        int yR = r.nextInt(yHigh - yLow) + yLow;
        Vector2 position = new Vector2(xR, yR);
        Power power = new Power(position, world, type, "power" + powerUpsCreated);
        powerupHashMap.put(String.valueOf("power" + powerUpsCreated++), power);
    }

    private void createPowerButton() {
        float x = Assets.instance.assetLevelDecoration.powerbutton.packedWidth;
        float y = camera.viewportHeight - Assets.instance.assetLevelDecoration.powerbutton.packedHeight;
        Vector2 position = new Vector2(x, y);
        powerupButton = new PowerupButton(position, world);
    }

    private void createNewParticle(String type) {
        Random r = new Random();
        int xLow = -(GameConstants.APP_WIDTH / 2 - 100);
        int xHigh = GameConstants.APP_WIDTH / 2 - 100;
        int xR = r.nextInt(xHigh - xLow) + xLow;

        int yLow = -(GameConstants.APP_HEIGHT / 2 - 350);
        int yHigh = GameConstants.APP_HEIGHT / 2 - 350;
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
        for (int i = 2; i < (GameConstants.APP_HEIGHT / 130) - 1; i++) {
            float yscale = 2 * (i + 1);
            Saw saw = new Saw(0, yscale, world, "R");
            spikeHashMap.put("R" + i, saw);
        }
        for (int i = 2; i < (GameConstants.APP_HEIGHT / 130) - 1; i++) {
            float yscale = 2 * (i + 1);
            Saw saw = new Saw(0, yscale, world, "L");
            spikeHashMap.put("L" + i, saw);
        }
        for (int i = 2; i < (GameConstants.APP_WIDTH / 130) - 2; i++) {
            float xscale = (2 * (i + 1)) + 0.2f;
            Saw saw = new Saw(xscale, 0, world, "U");
            spikeHashMap.put("U" + i, saw);
        }
        for (int i = 2; i < (GameConstants.APP_WIDTH / 130) - 2; i++) {
            float xscale = (2 * (i + 1)) + 0.2f;
            Saw saw = new Saw(xscale, 0, world, "D");
            spikeHashMap.put("D" + i, saw);
        }

        createSmallSaw(2 * (0.5f + 1), 2 * (2.5f + 1), "RT", 1);
        createSmallSaw(2 * (2.6f + 1), 2 * (0.5f + 1), "RT", 2);
        createSmallSaw(2 * (1.9f + 1), 2 * (1.175f + 1), "RT", 3);
        createSmallSaw(2 * (1.2f + 1), 2 * (1.85f + 1), "RT", 4);

        createSmallSaw(2 * (0.5f), 2 * (2.5f + 1), "LT", 1);
        createSmallSaw(2 * (2.6f), 2 * (0.5f + 1), "LT", 2);
        createSmallSaw(2 * (1.9f), 2 * (1.175f + 1), "LT", 3);
        createSmallSaw(2 * (1.2f), 2 * (1.85f + 1), "LT", 4);

        createSmallSaw(2 * (1.3f + 1) - 1.1f, 2 * (1.85f + 1) + 36.5f, "RB", 1);
        createSmallSaw(2 * (1.75f + 1), 2 * (1.85f + 1) + 36.7f, "RB", 2);
        createSmallSaw(2 * (2.6f + 1), 2 * (1.175f + 1) + 39.15f, "RB", 3);
        createSmallSaw(2 * (3.1f + 1), 2 * (1.175f + 1) + 40.9f, "RB", 4);
        createSmallSaw(2 * (3.15f + 1), 2 * (1.175f + 1) + 42.9f, "RB", 5);

        createSmallSaw(2 * (1.3f + 0) - 1.1f, 2 * (1.85f + 1) + 36.5f, "LB", 1);
        createSmallSaw(2 * (1.75f + 0), 2 * (1.85f + 1) + 36.7f, "LB", 2);
        createSmallSaw(2 * (2.6f + 0), 2 * (1.175f + 1) + 39.15f, "LB", 3);
        createSmallSaw(2 * (3.1f + 0), 2 * (1.175f + 1) + 40.9f, "LB", 4);
        createSmallSaw(2 * (3.15f + 0), 2 * (1.175f + 1) + 42.9f, "LB", 5);
    }

    private void createSmallSaw(float xscale, float yscale, String side, int index) {
        Saw saw = new Saw(xscale, yscale, world, side);
        spikeHashMap.put(side + index, saw);
    }

    private void createDeathSaw() {
        if (totalParticlesDestroyed > 5 && deathSawCounter > GameConstants.DEATH_SAW_TIME) {
            Random r = new Random();
            int side = r.nextInt(4);
            int vposition = r.nextInt((GameConstants.APP_HEIGHT / 130) - 1 - 2) + 2;
            int hposition = r.nextInt((GameConstants.APP_WIDTH / 130) - 2 - 2) + 2;
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
            AudioManager.instance.stopMusic();
            AudioManager.instance.stopAlertSound();
            handleGameOver();
            gameOverCounter++;
        } else {
            world.step(deltaTime * scale, 8, 3);
            destroyParticles();
            destroyExplosions();
            destroyParticleBursts();
            destroyPowerups();
            destroyDeathSaws();
            increaseDifficulty();
            checkBonusStreak();
            checkPowerups();
            checkHeroEnergy();
            createRandomPowerup();
            createRandomInstantPowerup();
            createSplitParticles();
            createSuicideParticles();
            createInvisibleParticles();
            createDeathSaw();
            camera.update();
            //Touch pad readings
            Vector2 touchPadVec = new Vector2(touchPadHelper.getTouchpad().getKnobPercentX() * GameConstants.HERO_SPEED * (1 / scale), touchPadHelper.getTouchpad().getKnobPercentY() * GameConstants.HERO_SPEED * (1 / scale));
            //If touchpad readings are zero dont alter velocity of hero
            if (touchPadVec.x != 0 && touchPadVec.y != 0) {
                hero.getBody().setLinearVelocity(touchPadVec);
            }
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

    private void updateDeltaTime(float scaleFactor) {
        this.scale = scaleFactor;
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
            if (GameConstants.DEATH_SAW_TIME > 100) {
                GameConstants.DEATH_SAW_TIME -= 10;
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
            activateSuperForcePowerup();
        } else {
            GameConstants.COLLISION_SPEED = 20f;
        }

        if (powerups.isActive() && powerups.getType().equals(GameConstants.SLOW_MOTION)) {
            activateSlowMotionPowerup();
        } else {
            updateDeltaTime(1);
        }

        if (powerups.isActive() && powerups.getType().equals(GameConstants.MASS_DEATH)) {
            activateMassDeathPowerup();
        }

        if (instantPowerups.isActive() && instantPowerups.getType().equals(GameConstants.ENERGY)) {
            instantPowerups.setActive(false);
            activateEnergyBoost();
        }

        if (instantPowerups.isActive() && instantPowerups.getType().equals(GameConstants.SPEED)) {
            instantPowerups.setActive(false);
            activateSpeedBoost();
        }

        if (instantPowerups.isActive() && instantPowerups.getType().equals(GameConstants.INVINCIBILITY)) {
            activateInvincibility();
        }

        if (instantPowerups.isActive() && instantPowerups.getType().equals(GameConstants.ARMOR)) {
            instantPowerups.setActive(false);
            activateArmorBoost();
        }
    }

    private void checkHeroEnergy() {
        if (hero.getEnergy() <= 0) {
            createParticleBurst("-1", hero.getBody().getPosition(), GameConstants.HERO_PARTICLE);
            AudioManager.instance.play(Assets.instance.sounds.hero_death);
            gameOver = true;
        } else if (hero.getEnergy() <= 20) {
            AudioManager.instance.playAlertSound(Assets.instance.music.alert, 0.3f);
        } else {
            AudioManager.instance.stopAlertSound();
        }
    }

    private void activateSuperForcePowerup() {
        GameConstants.COLLISION_SPEED = 30f;
        powerups.setPowerCounter(powerups.getPowerCounter() + 1);
    }

    private void activateSlowMotionPowerup() {
        updateDeltaTime(0.3f);
        powerups.setPowerCounter(powerups.getPowerCounter() + 1);
    }

    private void activateMassDeathPowerup() {
        for (Map.Entry<String, Particle> entry : particleHashMap.entrySet()) {
            createParticleBurst(entry.getValue().getBody().getUserData().toString(), entry.getValue().getBody().getPosition(), entry.getValue().getType());
            if (!normalParticlesForRemoval.contains(entry.getValue().getBody().getUserData().toString())) {
                normalParticlesForRemoval.add(entry.getValue().getBody().getUserData().toString());
            }
            if (hero.getEnergy() < 85) {
                hero.setEnergy(hero.getEnergy() + 5);
            }
        }
        powerups.setPowerCounter(powerups.getPowerCounter() + 1);
    }

    private void activateEnergyBoost() {
        hero.setEnergy(90);
    }

    private void activateSpeedBoost() {
        GameConstants.HERO_SPEED += 1;
    }

    private void activateInvincibility() {
        if (instantPowerups.getPowerCounter() > 500) {
            instantPowerups.setActive(false);
            hero.changeCollisionFilter(GameConstants.SPRITE_2);
            hero.setInvincible(false);
        } else {
            hero.changeCollisionFilter(GameConstants.SPRITE_8);
            hero.setInvincible(true);
            instantPowerups.setPowerCounter(instantPowerups.getPowerCounter() + 1);
        }
    }

    private void activateArmorBoost() {
        hero.increaseArmor();
    }

    private void updatePowerupCounter() {
        if (powerups.getPowerCounter() > 500) {
            powerups.setActive(false);
            if (GameConstants.MASS_DEATH.equals(powerups.getType())) {
                powerups.setPowerCounter(500);
            } else {
                powerups.setPowerCounter(0);
            }
            int remaining = powerups.getRemaining();
            remaining--;
            powerups.setRemaining(remaining);
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
            AudioManager.instance.play(Assets.instance.sounds.bomb);
        } else {
            particle.setBlastTimer(particle.getBlastTimer() + 1);
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
        if (powerUpCounter > 1000) {
            Random r = new Random();
            int powerUpNumber = r.nextInt(3);
            if (powerUpNumber == 0) {
                createPowerUp(GameConstants.SLOW_MOTION);
            } else if (powerUpNumber == 1) {
                createPowerUp(GameConstants.SUPER_FORCE);
            } else if (powerUpNumber == 2) {
                createPowerUp(GameConstants.MASS_DEATH);
            }
            powerUpCounter = 0;
        } else {
            powerUpCounter++;
        }
    }

    private void createRandomInstantPowerup() {
        if (instantPowerUpCounter > 1000) {
            Random r = new Random();
            int powerUpNumber = r.nextInt(4);
            if (powerUpNumber == 0) {
                createPowerUp(GameConstants.ENERGY);
            } else if (powerUpNumber == 1) {
                createPowerUp(GameConstants.SPEED);
            } else if (powerUpNumber == 2) {
                createPowerUp(GameConstants.INVINCIBILITY);
            } else if (powerUpNumber == 3) {
                createPowerUp(GameConstants.ARMOR);
            }
            instantPowerUpCounter = 0;
        } else {
            instantPowerUpCounter++;
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
                Vector2 inverseNormal = new Vector2(-particle.getNormaliseVector().x * GameConstants.COLLISION_SPEED * (1 / scale), -particle.getNormaliseVector().y * GameConstants.COLLISION_SPEED * (1 / scale));
                contact.getFixtureB().getBody().setLinearVelocity(inverseNormal);
                particle.setColliding(true);
                hero.setEnergy(hero.getEnergy() - 1);
            }

            if (contact.getFixtureA().getFilterData().categoryBits == GameConstants.SPRITE_1 && contact.getFixtureB().getFilterData().categoryBits == GameConstants.SPRITE_3) {
                //remove particles
                if (!normalParticlesForRemoval.contains(contact.getFixtureA().getBody().getUserData().toString())) {
                    createParticleBurst(contact.getFixtureA().getBody().getUserData().toString(), contact.getFixtureA().getBody().getPosition(), particleHashMap.get(contact.getFixtureA().getBody().getUserData().toString()).getType());
                    AudioManager.instance.playParticleDeathSound(Assets.instance.sounds.particle_death);
                    normalParticlesForRemoval.add(contact.getFixtureA().getBody().getUserData().toString());
                    if (hero.getEnergy() < 85) {
                        hero.setEnergy(hero.getEnergy() + 5);
                    }
                }
            }

            if (contact.getFixtureA().getFilterData().categoryBits == GameConstants.SPRITE_3 && contact.getFixtureB().getFilterData().categoryBits == GameConstants.SPRITE_1) {
                //remove particles
                if (!normalParticlesForRemoval.contains(contact.getFixtureB().getBody().getUserData().toString())) {
                    createParticleBurst(contact.getFixtureB().getBody().getUserData().toString(), contact.getFixtureB().getBody().getPosition(), particleHashMap.get(contact.getFixtureB().getBody().getUserData().toString()).getType());
                    AudioManager.instance.playParticleDeathSound(Assets.instance.sounds.particle_death);
                    normalParticlesForRemoval.add(contact.getFixtureB().getBody().getUserData().toString());
                    if (hero.getEnergy() < 85) {
                        hero.setEnergy(hero.getEnergy() + 5);
                    }
                }
            }

            if (contact.getFixtureA().getFilterData().categoryBits == GameConstants.SPRITE_2 && contact.getFixtureB().getFilterData().categoryBits == GameConstants.SPRITE_3) {
                if (!gameOver) {
                    createParticleBurst("-1", contact.getFixtureA().getBody().getPosition(), GameConstants.HERO_PARTICLE);
                    AudioManager.instance.play(Assets.instance.sounds.hero_death);
                    gameOver = true;
                }
            }

            if (contact.getFixtureA().getFilterData().categoryBits == GameConstants.SPRITE_2 && contact.getFixtureB().getFilterData().categoryBits == GameConstants.SPRITE_4) {
                if (!gameOver) {
                    createParticleBurst("-1", contact.getFixtureA().getBody().getPosition(), GameConstants.HERO_PARTICLE);
                    AudioManager.instance.play(Assets.instance.sounds.hero_death);
                    gameOver = true;
                }
            }

            if (contact.getFixtureA().getFilterData().categoryBits == GameConstants.SPRITE_2 && contact.getFixtureB().getFilterData().categoryBits == GameConstants.SPRITE_6) {
                if (!powerupsForRemoval.contains(contact.getFixtureB().getBody().getUserData().toString())) {
                    Power power = powerupHashMap.get(contact.getFixtureB().getBody().getUserData().toString());
                    powerups.setType(power.getType());
                    powerups.setRemaining(2);
                    powerups.createSprite(power.getTexureRegion());
                    if (GameConstants.MASS_DEATH.equals(power.getType())) {
                        powerups.setPowerCounter(500);
                    } else {
                        powerups.setPowerCounter(0);
                    }
                    powerups.setPickedUp(true);
                    AudioManager.instance.play(Assets.instance.sounds.pickup);
                    powerupsForRemoval.add(contact.getFixtureB().getBody().getUserData().toString());
                }
            }

            if (contact.getFixtureA().getFilterData().categoryBits == GameConstants.SPRITE_2 && contact.getFixtureB().getFilterData().categoryBits == GameConstants.SPRITE_7) {
                if (!powerupsForRemoval.contains(contact.getFixtureB().getBody().getUserData().toString())) {
                    Power power = powerupHashMap.get(contact.getFixtureB().getBody().getUserData().toString());
                    instantPowerups.setType(power.getType());
                    instantPowerups.createSprite(power.getTexureRegion());
                    instantPowerups.setPowerCounter(0);
                    instantPowerups.setPickedUp(true);
                    instantPowerups.setActive(true);
                    playInstantPowerupSound(power.getType());
                    powerupsForRemoval.add(contact.getFixtureB().getBody().getUserData().toString());
                }
            }

            if (contact.getFixtureA().getFilterData().categoryBits == GameConstants.SPRITE_8 && contact.getFixtureB().getFilterData().categoryBits == GameConstants.SPRITE_1) {
                //remove particles
                if (!normalParticlesForRemoval.contains(contact.getFixtureB().getBody().getUserData().toString())) {
                    createParticleBurst(contact.getFixtureB().getBody().getUserData().toString(), contact.getFixtureB().getBody().getPosition(), particleHashMap.get(contact.getFixtureB().getBody().getUserData().toString()).getType());
                    AudioManager.instance.playParticleDeathSound(Assets.instance.sounds.particle_death);
                    normalParticlesForRemoval.add(contact.getFixtureB().getBody().getUserData().toString());
                    if (hero.getEnergy() < 85) {
                        hero.setEnergy(hero.getEnergy() + 5);
                    }
                }
            }

            if (contact.getFixtureA().getFilterData().categoryBits == GameConstants.SPRITE_8 && contact.getFixtureB().getFilterData().categoryBits == GameConstants.SPRITE_3) {
                if (!gameOver) {
                    createParticleBurst("-1", contact.getFixtureA().getBody().getPosition(), GameConstants.HERO_PARTICLE);
                    AudioManager.instance.play(Assets.instance.sounds.hero_death);
                    gameOver = true;
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
        AnyDirection.myRequestHandler.showInterstitialAd();
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
            GameConstants.DEATH_SAW_TIME = 500;
            GameConstants.HERO_SPEED = 5f;

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

    private void deployPowerup() {
        if (!powerups.isActive() && powerups.getRemaining() > 0) {
            //powerups.setType(GameConstants.SUPER_FORCE);
            powerups.setActive(true);
        } else {
            powerups.setActive(false);
        }
    }

    private void playInstantPowerupSound(String type) {
        if (GameConstants.ENERGY.equals(type)) {
            AudioManager.instance.play(Assets.instance.sounds.energy);
        } else if (GameConstants.SPEED.equals(type)) {
            AudioManager.instance.play(Assets.instance.sounds.speed);
        } else if (GameConstants.INVINCIBILITY.equals(type)) {
            AudioManager.instance.play(Assets.instance.sounds.invincible);
        } else if (GameConstants.ARMOR.equals(type)) {
            AudioManager.instance.play(Assets.instance.sounds.armor);
        }
    }

    public int getBonusStreak() {
        return bonusStreak;
    }

    public int getScore() {
        return score;
    }
}
