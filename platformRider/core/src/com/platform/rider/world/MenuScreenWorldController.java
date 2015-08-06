package com.platform.rider.world;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.platform.rider.assets.Assets;
import com.platform.rider.main.AnyDirection;
import com.platform.rider.screens.GameScreen;
import com.platform.rider.sprites.*;
import com.platform.rider.utils.AudioManager;
import com.platform.rider.utils.GameConstants;
import com.platform.rider.utils.OverlapTester;

import java.util.*;

/**
 * Created by Gayan on 8/3/2015.
 */
public class MenuScreenWorldController implements WorldControllerInterface {
    private Game game;
    public OrthographicCamera cameraGUI;
    private Viewport viewport;
    public World world;
    public PlayButton playButton;
    public PlayButtonSaw playButtonSaw;
    public AchievementButton achievementButton;
    public LeaderBoardButton leaderBoardButton;
    public ParticleBurstAnimation particleBurstAnimation;
    public HashMap<String, Particle> particleHashMap = new HashMap<String, Particle>();
    public List<String> normalParticlesForRemoval = new ArrayList<String>();
    public HashMap<String, ParticleBurstAnimation> particleBurstHashMap = new HashMap<String, ParticleBurstAnimation>();
    public List<String> particleBurstsForRemoval = new ArrayList<String>();
    int totalParticlesCreated = 0;
    int normalParticleCreationCounter = 0;
    int splitParticleCreationCounter = 0;
    int suicideParticleCreationCounter = 0;

    public MenuScreenWorldController(Game game) {
        this.game = game;
        init();
    }

    @Override
    public void init() {
        InputAdapter inputAdapter = new InputAdapter() {
            public boolean touchUp(int x, int y, int pointer, int button) {
                Vector3 touchPoint = new Vector3(x, y, 0);
                cameraGUI.unproject(touchPoint);
                Rectangle playButtonBound = playButton.getSprite().getBoundingRectangle();
                if (OverlapTester.pointInRectangle(playButtonBound, new Vector2(touchPoint.x, touchPoint.y))) {
                    destroyPlayButtonSaw();
                    game.setScreen(new GameScreen(game));
                }
                Rectangle achievementButtonBound = achievementButton.getSprite().getBoundingRectangle();
                if (OverlapTester.pointInRectangle(achievementButtonBound, new Vector2(touchPoint.x, touchPoint.y))) {
                    AnyDirection.myRequestHandler.showAchievements();
                }
                Rectangle leaderBoardButtonBound = leaderBoardButton.getSprite().getBoundingRectangle();
                if (OverlapTester.pointInRectangle(leaderBoardButtonBound, new Vector2(touchPoint.x, touchPoint.y))) {
                    AnyDirection.myRequestHandler.showScores();
                }
                return true;
            }
        };
        Gdx.input.setInputProcessor(inputAdapter);
        cameraGUI = new OrthographicCamera(GameConstants.APP_WIDTH, GameConstants.APP_HEIGHT);
        viewport = new FitViewport(GameConstants.APP_WIDTH, GameConstants.APP_HEIGHT, cameraGUI);
        initPhysics();
        world.setContactListener(new menuScreenContactListener());
        AudioManager.instance.play(Assets.instance.music.menu_music, 1);
        AnyDirection.myRequestHandler.showAds(true);
        AnyDirection.myRequestHandler.signIn();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
    }

    @Override
    public void initPhysics() {
        world = new World(new Vector2(0, 0), true);
        createPlayButtonSaw();
        createPlayButton();
        createAchievementButton();
        createLeaderBoardButton();
    }

    private void createPlayButton() {
        float x = cameraGUI.viewportWidth / 2 + 20;
        float y = cameraGUI.viewportHeight / 2;
        Vector2 position = new Vector2(x, y);
        playButton = new PlayButton(position, world);
    }

    private void createPlayButtonSaw() {
        float x = cameraGUI.viewportWidth / 2;
        float y = cameraGUI.viewportHeight / 2;
        playButtonSaw = new PlayButtonSaw(x, y, world);
    }

    private void createAchievementButton() {
        float x = cameraGUI.viewportWidth / 2 + 200;
        float y = cameraGUI.viewportHeight / 2 - 300;
        Vector2 position = new Vector2(x, y);
        achievementButton = new AchievementButton(position, world);
    }

    private void createLeaderBoardButton() {
        float x = cameraGUI.viewportWidth / 2 - 200;
        float y = cameraGUI.viewportHeight / 2 - 300;
        Vector2 position = new Vector2(x, y);
        leaderBoardButton = new LeaderBoardButton(position, world);
    }

    private void createNewParticle(String type) {
        Random r = new Random();
        int xLow = -(Math.round(cameraGUI.viewportWidth) / 2);
        int xHigh = Math.round(cameraGUI.viewportWidth);
        int xR = r.nextInt(xHigh - xLow) + xLow;

        int yLow = -(Math.round(cameraGUI.viewportHeight) / 2);
        int yHigh = Math.round(cameraGUI.viewportHeight);
        int yR = r.nextInt(yHigh - yLow) + yLow;

        Vector2 position = new Vector2(xR, yR);
        Rectangle playButtonSawBound = playButtonSaw.getAnimatedSprite().getBoundingRectangle();
        if (!OverlapTester.pointInRectangle(playButtonSawBound, position)) {
            Particle particle = new Particle(position, world, totalParticlesCreated, type);
            Vector2 normalizedPosition = position.nor();
            int powerUpNumber = r.nextInt(4);
            if (powerUpNumber == 0) {
                particle.getBody().setLinearVelocity(-normalizedPosition.x * 7f, normalizedPosition.y * 7f);
            } else if (powerUpNumber == 1) {
                particle.getBody().setLinearVelocity(normalizedPosition.x * 7f, -normalizedPosition.y * 7f);
            } else if (powerUpNumber == 2) {
                particle.getBody().setLinearVelocity(-normalizedPosition.x * 7f, -normalizedPosition.y * 7f);
            } else if (powerUpNumber == 3) {
                particle.getBody().setLinearVelocity(normalizedPosition.x * 7f, normalizedPosition.y * 7f);
            }

            //increment the number of particles created count
            particleHashMap.put(String.valueOf(totalParticlesCreated++), particle);
        }
    }

    private void createParticleBurst(String burstIndex, Vector2 position, String type) {
        particleBurstAnimation = new ParticleBurstAnimation(burstIndex, position, type);
        particleBurstHashMap.put(String.valueOf(burstIndex), particleBurstAnimation);
    }

    private void createSplitParticles() {
        if (splitParticleCreationCounter > 10) {
            createNewParticle(GameConstants.SPLIT_PARTICLE);
            splitParticleCreationCounter = 0;
        } else {
            splitParticleCreationCounter++;
        }
    }

    private void createSuicideParticles() {
        if (suicideParticleCreationCounter > 10) {
            createNewParticle(GameConstants.SUICIDE_PARTICLE);
            suicideParticleCreationCounter = 0;
        } else {
            suicideParticleCreationCounter++;
        }
    }

    private void createNormalParticles() {
        if (normalParticleCreationCounter > 10) {
            createNewParticle(GameConstants.NORMAL_PARTICLE);
            normalParticleCreationCounter = 0;
        } else {
            normalParticleCreationCounter++;
        }
    }

    private void destroyPlayButtonSaw() {
        final Array<JointEdge> list = playButtonSaw.getBody().getJointList();
        while (list.size > 0) {
            world.destroyJoint(list.get(0).joint);
        }
        world.destroyBody(playButtonSaw.getBody());
    }

    private void destroyParticleBursts() {
        for (String explosion : particleBurstsForRemoval) {
            particleBurstHashMap.remove(explosion);
        }
        particleBurstsForRemoval.clear();
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
        }
        normalParticlesForRemoval.clear();
    }

    @Override
    public void update(float deltaTime) {
        world.step(deltaTime, 8, 3);
        destroyParticles();
        destroyParticleBursts();
        createNormalParticles();
        createSplitParticles();
        createSuicideParticles();
        cameraGUI.update();
        updateParticles();
    }

    private void updateParticles() {
        for (Map.Entry<String, Particle> entry : particleHashMap.entrySet()) {
            Particle particle = entry.getValue();
            if (GameConstants.SUICIDE_PARTICLE.equals(particle.getType())) {
                particle.getAnimatedSprite().setPosition((particle.getBody().getPosition().x * GameConstants.PIXELS_TO_METERS) - particle.getAnimatedSprite().
                                getWidth() / 2,
                        (particle.getBody().getPosition().y * GameConstants.PIXELS_TO_METERS) - particle.getAnimatedSprite().getHeight() / 2
                );
                if (particle.getAnimatedSprite().getX() < (-particle.getAnimatedSprite().getWidth()) - GameConstants.APP_WIDTH / 2
                        || particle.getAnimatedSprite().getX() > (particle.getAnimatedSprite().getWidth()) + GameConstants.APP_WIDTH
                        || particle.getAnimatedSprite().getY() > (particle.getAnimatedSprite().getHeight()) + GameConstants.APP_HEIGHT
                        || particle.getAnimatedSprite().getY() < (-particle.getAnimatedSprite().getHeight()) - GameConstants.APP_HEIGHT / 2) {
                    if (!normalParticlesForRemoval.contains(particle.getBody().getUserData().toString())) {
                        normalParticlesForRemoval.add(particle.getBody().getUserData().toString());
                    }
                }
            } else {
                particle.getSprite().setPosition((particle.getBody().getPosition().x * GameConstants.PIXELS_TO_METERS) - particle.getSprite().
                                getWidth() / 2,
                        (particle.getBody().getPosition().y * GameConstants.PIXELS_TO_METERS) - particle.getSprite().getHeight() / 2
                );
                if (particle.getSprite().getX() < (-particle.getSprite().getWidth()) - GameConstants.APP_WIDTH / 2
                        || particle.getSprite().getX() > (particle.getSprite().getWidth()) + GameConstants.APP_WIDTH
                        || particle.getSprite().getY() > (particle.getSprite().getHeight()) + GameConstants.APP_HEIGHT
                        || particle.getSprite().getY() < (-particle.getSprite().getHeight()) - GameConstants.APP_HEIGHT / 2) {
                    if (!normalParticlesForRemoval.contains(particle.getBody().getUserData().toString())) {
                        normalParticlesForRemoval.add(particle.getBody().getUserData().toString());
                    }
                }
            }
        }
    }

    class menuScreenContactListener implements ContactListener {

        @Override
        public void beginContact(Contact contact) {

        }

        @Override
        public void endContact(Contact contact) {
            if (contact.getFixtureA().getFilterData().categoryBits == GameConstants.SPRITE_1 && contact.getFixtureB().getFilterData().categoryBits == GameConstants.SPRITE_3) {
                //remove particles
                if (!normalParticlesForRemoval.contains(contact.getFixtureA().getBody().getUserData().toString())) {
                    String type = particleHashMap.get(contact.getFixtureA().getBody().getUserData().toString()).getType();
                    createParticleBurst(contact.getFixtureA().getBody().getUserData().toString(), contact.getFixtureA().getBody().getPosition(), type);
                    normalParticlesForRemoval.add(contact.getFixtureA().getBody().getUserData().toString());
                }
            }

            if (contact.getFixtureA().getFilterData().categoryBits == GameConstants.SPRITE_3 && contact.getFixtureB().getFilterData().categoryBits == GameConstants.SPRITE_1) {
                //remove particles
                if (!normalParticlesForRemoval.contains(contact.getFixtureB().getBody().getUserData().toString())) {
                    String type = particleHashMap.get(contact.getFixtureB().getBody().getUserData().toString()).getType();
                    createParticleBurst(contact.getFixtureB().getBody().getUserData().toString(), contact.getFixtureB().getBody().getPosition(), type);
                    normalParticlesForRemoval.add(contact.getFixtureB().getBody().getUserData().toString());
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
}
