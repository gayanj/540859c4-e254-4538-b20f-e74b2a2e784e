package com.platform.rider.worldRenderer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.platform.rider.assets.Assets;
import com.platform.rider.sprites.*;
import com.platform.rider.utils.GameConstants;
import com.platform.rider.utils.GamePreferences;
import com.platform.rider.world.GameScreenWorldController;

import javax.microedition.khronos.opengles.GL10;
import java.util.Map;

/**
 * Created by Gayan on 3/23/2015.
 */
public class GameScreenWorldRenderer implements WorldRendererInterface {
    private static final String TAG = GameScreenWorldRenderer.class.getName();
    private SpriteBatch batch;
    private GameScreenWorldController gameScreenWorldController;
    private Box2DDebugRenderer b2debugRenderer;
    private Matrix4 debugMatrix;
    private OrthographicCamera cameraGUI;
    private Viewport viewport;
    private static ShapeRenderer debugRenderer;

    public GameScreenWorldRenderer(GameScreenWorldController gameScreenWorldController) {
        this.gameScreenWorldController = gameScreenWorldController;
        init();
    }

    public void init() {
        batch = new SpriteBatch();
        debugRenderer = new ShapeRenderer();
        b2debugRenderer = new Box2DDebugRenderer();
        cameraGUI = gameScreenWorldController.camera;
        viewport = gameScreenWorldController.viewport;
        cameraGUI.position.set(0, 0, 0);
        cameraGUI.setToOrtho(false, viewport.getWorldWidth(), viewport.getWorldHeight()); // flip y-axis
        cameraGUI.update();
    }

    public void render() {
        renderWorld();
        renderGui();
    }

    private void renderWorld() {
        batch.setProjectionMatrix(gameScreenWorldController.camera.combined);
        debugMatrix = batch.getProjectionMatrix().cpy().scale(GameConstants.PIXELS_TO_METERS,
                GameConstants.PIXELS_TO_METERS, 0);
        batch.begin();
        debugRenderer.begin(ShapeRenderer.ShapeType.Line);
        //gameScreenWorldController.grid.Draw(cameraGUI.combined);
        debugRenderer.end();
        batch.end();
        //renderBackground(batch);
        batch.begin();
        if (!gameScreenWorldController.isGameOver()) {
            gameScreenWorldController.hero.render(batch);
        }
        renderParticles();
        renderSpikes();
        renderDeathSaws();
        renderPowerups();
        renderExplosion();
        renderParticleBurst();
        batch.end();
        //b2debugRenderer.render(worldController.world, debugMatrix);
        gameScreenWorldController.touchPadHelper.render();
    }

    private void renderParticles() {
        for (Map.Entry<String, Particle> entry : gameScreenWorldController.particleHashMap.entrySet()) {
            entry.getValue().render(batch);
        }
    }

    private void renderSpikes() {
        for (Map.Entry<String, Saw> entry : gameScreenWorldController.spikeHashMap.entrySet()) {
            entry.getValue().render(batch);
        }
    }

    private void renderDeathSaws() {
        for (Map.Entry<String, DeathSaw> entry : gameScreenWorldController.deathSawHashMap.entrySet()) {
            entry.getValue().render(batch);
        }
    }

    private void renderPowerups() {
        for (Map.Entry<String, Power> entry : gameScreenWorldController.powerupHashMap.entrySet()) {
            entry.getValue().render(batch);
        }
    }

    public void resize(int width, int height) {
        viewport.update(width, height);
    }

    public void dispose() {
        //batch.dispose();
    }

    private void renderGui() {
        batch.setProjectionMatrix(cameraGUI.combined);
        batch.begin();
        renderGuiScore();
        renderBonusSreak();
        if (gameScreenWorldController.isGameOver() && gameScreenWorldController.isPlayButtonSawCreated()) {
            renderGuiGameOverMessage();
            renderPlayButtonSaw();
            renderRetryButton();
            renderAchievementButton();
            renderLeaderBoardButton();
            renderRatingButton();
            renderHighScore();
        }
        renderPowerButton();
        renderPowerupInfo();
        renderHeroEnergy();
        if (!gameScreenWorldController.isGameOver()) {
            renderKillStreakText();
        }
        if (GamePreferences.instance.renderFirstTutorial) {
            renderTutorialArrow();
        }
        if (GamePreferences.instance.firstTutorialCompleted && !GamePreferences.instance.secondTutorialCompleted) {
            if (gameScreenWorldController.secondTutorialCounter > 50) {
                renderTutorialBox();
                gameScreenWorldController.pause = true;
            } else {
                gameScreenWorldController.secondTutorialCounter++;
            }
        }
        batch.end();
    }

    private void renderGuiScore() {
        float x = 10;
        float y = cameraGUI.viewportHeight;
        Assets.instance.fonts.defaultNormal.draw(batch,
                "SCORE",
                x, y);
        Assets.instance.fonts.defaultNormal.draw(batch, "" + gameScreenWorldController.getScore(),
                x, y - 40);
    }

    private void renderBonusSreak() {
        float x = cameraGUI.viewportWidth / 2 + 300;
        float y = cameraGUI.viewportHeight;
        if (gameScreenWorldController.getBonusStreak() > 0) {
            Assets.instance.fonts.defaultNormal.draw(batch,
                    "COMBO",
                    x, y);
            Assets.instance.fonts.defaultNormal.draw(batch,
                    gameScreenWorldController.getBonusStreak() + "X",
                    x, y - 40);
        }
    }

    private void renderGuiGameOverMessage() {
        float x = cameraGUI.viewportWidth / 2;
        float y = cameraGUI.viewportHeight / 2 + 300;
        BitmapFont fontGameOver = Assets.instance.fonts.defaultBig;
        fontGameOver.drawMultiLine(batch, "GAME OVER", x, y, 0,
                BitmapFont.HAlignment.CENTER);
    }

    private void renderHighScore() {
        float x = cameraGUI.viewportWidth / 2;
        float y = cameraGUI.viewportHeight / 2 - 500;
        BitmapFont fontHighScore = Assets.instance.fonts.defaultBig;
        fontHighScore.drawMultiLine(batch, "BEST " + GamePreferences.instance.highscore, x, y, 0,
                BitmapFont.HAlignment.CENTER);
    }

    private void renderPowerButton() {
        gameScreenWorldController.powerupButton.render(batch);
    }

    private void renderRetryButton() {
        gameScreenWorldController.retryButton.render(batch);
    }

    private void renderPlayButtonSaw() {
        gameScreenWorldController.playButtonSaw.render(batch);
    }

    private void renderAchievementButton(){
        gameScreenWorldController.achievementButton.render(batch);
    }

    private void renderLeaderBoardButton(){
        gameScreenWorldController.leaderBoardButton.render(batch);
    }

    private void renderRatingButton(){
        gameScreenWorldController.ratingButton.render(batch);
    }

    private void renderTutorialBox() {
        gameScreenWorldController.tutorialBox.render(batch);
    }

    private void renderExplosion() {
        for (Map.Entry<String, Explosion> entry : gameScreenWorldController.explosionHashMap.entrySet()) {
            if (entry.getValue().isBlast()) {
                entry.getValue().render(batch);
                if (entry.getValue().getAnimatedSprite().isAnimationFinished()) {
                    entry.getValue().setBlast(false);
                    gameScreenWorldController.explosionsForRemoval.add(Integer.toString(entry.getValue().getHashMapIndex()));
                }
            }
        }
    }

    private void renderParticleBurst() {
        for (Map.Entry<String, ParticleBurstAnimation> entry : gameScreenWorldController.particleBurstHashMap.entrySet()) {
            if (entry.getValue().isBurst()) {
                entry.getValue().render(batch);
                if (entry.getValue().getAnimatedSprite().isAnimationFinished()) {
                    entry.getValue().setBurst(false);
                    gameScreenWorldController.particleBurstsForRemoval.add(entry.getValue().getHashMapIndex());
                }
            }
        }
    }

    private void renderPowerupInfo() {
        float x = cameraGUI.viewportWidth / 2 + 380;
        float y = cameraGUI.viewportHeight - 80;
        if (gameScreenWorldController.powerups.isPickedUp() && gameScreenWorldController.powerups.getRemaining() >= 0) {
            Sprite sprite = gameScreenWorldController.powerups.getSprite();
            batch.draw(sprite,
                    x, y - 37 - sprite.getHeight(),
                    sprite.getOriginX(), sprite.getOriginY(),
                    sprite.getWidth(), sprite.getHeight(),
                    sprite.getScaleX(), sprite.getScaleY(), sprite.getRotation()
            );
            Assets.instance.fonts.defaultNormal.draw(batch,
                    "X " + gameScreenWorldController.powerups.getRemaining(),
                    x + 60, y - 37);
            //if(worldController.powerups.isActive()) {
            int remainingTime = 50 - (gameScreenWorldController.powerups.getPowerCounter() / 10);
            String timer = new String(new char[remainingTime]).replace("\0", "|");
            Assets.instance.fonts.energyOrange.draw(batch,
                    timer,
                    x + 60, y - 100);
            //}
        }
    }

    private void renderHeroEnergy() {
        float x = 10;
        float y = cameraGUI.viewportHeight;
        int remainingEnergy = gameScreenWorldController.hero.getEnergy();
        if (remainingEnergy >= 0) {
            String energy = new String(new char[remainingEnergy]).replace("\0", "|");
            Assets.instance.fonts.defaultNormal.draw(batch,
                    "ENERGY",
                    x, y - 100);
            if (remainingEnergy >= 60) {
                Assets.instance.fonts.energyGreen.draw(batch,
                        energy,
                        x, y - 140);
            } else if (remainingEnergy >= 40) {
                Assets.instance.fonts.energyYellow.draw(batch,
                        energy,
                        x, y - 140);
            } else if (remainingEnergy >= 20) {
                Assets.instance.fonts.energyOrange.draw(batch,
                        energy,
                        x, y - 140);
            } else if (remainingEnergy >= 0) {
                Assets.instance.fonts.energyRed.draw(batch,
                        energy,
                        x, y - 140);
            }
        }
    }

    private void renderTutorialArrow() {
        gameScreenWorldController.tutorialArrow.render(batch);
    }

    public static void DrawDebugLine(Vector2 start, Vector2 end, float lineWidth, Color color, Matrix4 projectionMatrix) {
        Gdx.gl.glEnable(GL10.GL_BLEND);
        Gdx.gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
        Gdx.gl.glLineWidth(1);
        debugRenderer.setProjectionMatrix(projectionMatrix);
        //debugRenderer.begin(ShapeRenderer.ShapeType.Line);
        debugRenderer.setColor(color);
        debugRenderer.line(start, end);
        //debugRenderer.end();
        Gdx.gl.glLineWidth(1);
        Gdx.gl.glDisable(GL10.GL_BLEND);
    }

    public void renderKillStreakText() {
        float x = cameraGUI.viewportWidth / 2;
        float y = cameraGUI.viewportHeight / 2;
        if (gameScreenWorldController.isRenderKillStreak()) {
            BitmapFont fontKillStreak = Assets.instance.fonts.defaultBig;
            if (gameScreenWorldController.getKillStreakToRender() == 1) {
                fontKillStreak.drawMultiLine(batch, "KILLING SPREE!", x, y, 0,
                        BitmapFont.HAlignment.CENTER);
            } else if (gameScreenWorldController.getKillStreakToRender() == 2) {
                fontKillStreak.drawMultiLine(batch, "DOMINATING!", x, y, 0,
                        BitmapFont.HAlignment.CENTER);
            } else if (gameScreenWorldController.getKillStreakToRender() == 3) {
                fontKillStreak.drawMultiLine(batch, "MEGA KILL!", x, y, 0,
                        BitmapFont.HAlignment.CENTER);
            } else if (gameScreenWorldController.getKillStreakToRender() == 4) {
                fontKillStreak.drawMultiLine(batch, "UNSTOPPABLE!", x, y, 0,
                        BitmapFont.HAlignment.CENTER);
            } else if (gameScreenWorldController.getKillStreakToRender() == 5) {
                fontKillStreak.drawMultiLine(batch, "WICKED SICK!", x, y, 0,
                        BitmapFont.HAlignment.CENTER);
            } else if (gameScreenWorldController.getKillStreakToRender() == 6) {
                fontKillStreak.drawMultiLine(batch, "MONSTER KILL!", x, y, 0,
                        BitmapFont.HAlignment.CENTER);
            } else if (gameScreenWorldController.getKillStreakToRender() == 7) {
                fontKillStreak.drawMultiLine(batch, "GODLIKE!", x, y, 0,
                        BitmapFont.HAlignment.CENTER);
            } else if (gameScreenWorldController.getKillStreakToRender() == 8) {
                fontKillStreak.drawMultiLine(batch, "ULTRA KILL!", x, y, 0,
                        BitmapFont.HAlignment.CENTER);
            } else if (gameScreenWorldController.getKillStreakToRender() == 9) {
                fontKillStreak.drawMultiLine(batch, "RAMPAGE!", x, y, 0,
                        BitmapFont.HAlignment.CENTER);
            } else if (gameScreenWorldController.getKillStreakToRender() == 10) {
                fontKillStreak.drawMultiLine(batch, "BEYOND GODLIKE!", x, y, 0,
                        BitmapFont.HAlignment.CENTER);
            }
        }
    }
}
