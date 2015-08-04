package com.platform.rider.world;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.JointEdge;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.platform.rider.assets.Assets;
import com.platform.rider.main.AnyDirection;
import com.platform.rider.screens.GameScreen;
import com.platform.rider.sprites.PlayButton;
import com.platform.rider.sprites.PlayButtonSaw;
import com.platform.rider.utils.AudioManager;
import com.platform.rider.utils.GameConstants;
import com.platform.rider.utils.OverlapTester;

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

    public MenuScreenWorldController(Game game) {
        this.game = game;
        init();
    }

    @Override
    public void init() {
        InputAdapter inputAdapter = new InputAdapter() {
            public boolean touchUp(int x, int y, int pointer, int button) {
                float xTouchScale = cameraGUI.viewportWidth / Gdx.graphics.getWidth();
                float yTouchScale = cameraGUI.viewportHeight / Gdx.graphics.getHeight();
                Vector2 touchPoint = new Vector2(x * xTouchScale, y * yTouchScale);
                Rectangle powerupBound = playButton.getSprite().getBoundingRectangle();
                if (OverlapTester.pointInRectangle(powerupBound, touchPoint)) {
                    destroyPlayButtonSaw();
                    game.setScreen(new GameScreen(game));
                }
                return true;
            }
        };
        Gdx.input.setInputProcessor(inputAdapter);
        cameraGUI = new OrthographicCamera(GameConstants.APP_WIDTH, GameConstants.APP_HEIGHT);
        viewport = new FitViewport(GameConstants.APP_WIDTH, GameConstants.APP_HEIGHT, cameraGUI);
        initPhysics();
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

    private void destroyPlayButtonSaw() {
        final Array<JointEdge> list = playButtonSaw.getBody().getJointList();
        while (list.size > 0) {
            world.destroyJoint(list.get(0).joint);
        }
        world.destroyBody(playButtonSaw.getBody());
    }

    @Override
    public void update(float deltaTime) {
    }
}
