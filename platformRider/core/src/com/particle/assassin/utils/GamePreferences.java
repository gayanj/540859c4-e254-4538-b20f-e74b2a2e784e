package com.particle.assassin.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;

/**
 * Created by Gayan on 7/23/2015.
 */
public class GamePreferences {
    public static final GamePreferences instance =
            new GamePreferences();

    public boolean firstTutorialCompleted;
    public boolean secondTutorialCompleted;
    public boolean renderFirstTutorial;
    public boolean spawnSplitParticles;
    public boolean spawnSuicideParticles;
    public boolean spawnInvisibleParticles;
    public boolean firstStageCleared;
    public boolean secondStageCleared;
    public boolean thirdStageCleared;
    public boolean killingSpreeAchievementUnlocked;
    public boolean dominatingAchievementUnlocked;
    public boolean megaKillAchievementUnlocked;
    public boolean unstoppableAchievementUnlocked;
    public boolean wickedSickAchievementUnlocked;
    public boolean monsterKillAchievementUnlocked;
    public boolean godLikeAchievementUnlocked;
    public boolean ultraKillAchievementUnlocked;
    public boolean rampageAchievementUnlocked;
    public boolean holyShitAchievementUnlocked;
    public int stage;
    public int highscore;
    public int normalParticlesDestroyed;
    public int splitParticlesDestroyed;
    public int suicideParticlesDestroyed;
    public int invisibleParticlesDestroyed;

    private Preferences prefs;

    // singleton: prevent instantiation from other classes
    private GamePreferences() {
        prefs = Gdx.app.getPreferences(GameConstants.PREFERENCES_FILE);
    }

    public void load() {
        firstTutorialCompleted = prefs.getBoolean("firstTutorialCompleted", false);
        secondTutorialCompleted = prefs.getBoolean("secondTutorialCompleted", false);
        renderFirstTutorial = prefs.getBoolean("renderFirstTutorial", false);
        spawnSplitParticles = prefs.getBoolean("spawnSplitParticles", false);
        spawnSuicideParticles = prefs.getBoolean("spawnSuicideParticles", false);
        spawnInvisibleParticles = prefs.getBoolean("spawnInvisibleParticles", false);
        firstStageCleared = prefs.getBoolean("firstStageCleared", false);
        secondStageCleared = prefs.getBoolean("secondStageCleared", false);
        thirdStageCleared = prefs.getBoolean("thirdStageCleared", false);
        killingSpreeAchievementUnlocked = prefs.getBoolean("killingSpreeAchievementUnlocked", false);
        dominatingAchievementUnlocked = prefs.getBoolean("dominatingAchievementUnlocked", false);
        megaKillAchievementUnlocked = prefs.getBoolean("megaKillAchievementUnlocked", false);
        unstoppableAchievementUnlocked = prefs.getBoolean("unstoppableAchievementUnlocked", false);
        wickedSickAchievementUnlocked = prefs.getBoolean("wickedSickAchievementUnlocked", false);
        monsterKillAchievementUnlocked = prefs.getBoolean("monsterKillAchievementUnlocked", false);
        godLikeAchievementUnlocked = prefs.getBoolean("godLikeAchievementUnlocked", false);
        ultraKillAchievementUnlocked = prefs.getBoolean("ultraKillAchievementUnlocked", false);
        rampageAchievementUnlocked = prefs.getBoolean("rampageAchievementUnlocked", false);
        holyShitAchievementUnlocked = prefs.getBoolean("holyShitAchievementUnlocked", false);
        stage = prefs.getInteger("stage", 0);
        highscore = prefs.getInteger("highscore", 0);
        normalParticlesDestroyed = prefs.getInteger("normalParticlesDestroyed", 0);
        splitParticlesDestroyed = prefs.getInteger("splitParticlesDestroyed", 0);
        suicideParticlesDestroyed = prefs.getInteger("suicideParticlesDestroyed", 0);
        invisibleParticlesDestroyed = prefs.getInteger("invisibleParticlesDestroyed", 0);
    }

    public void save() {
        prefs.putBoolean("firstTutorialCompleted", firstTutorialCompleted);
        prefs.putBoolean("secondTutorialCompleted", secondTutorialCompleted);
        prefs.putBoolean("renderFirstTutorial", renderFirstTutorial);
        prefs.putBoolean("spawnSplitParticles", spawnSplitParticles);
        prefs.putBoolean("spawnSuicideParticles", spawnSuicideParticles);
        prefs.putBoolean("spawnInvisibleParticles", spawnInvisibleParticles);
        prefs.putBoolean("firstStageCleared", firstStageCleared);
        prefs.putBoolean("secondStageCleared", secondStageCleared);
        prefs.putBoolean("thirdStageCleared", thirdStageCleared);
        prefs.putBoolean("killingSpreeAchievementUnlocked", killingSpreeAchievementUnlocked);
        prefs.putBoolean("dominatingAchievementUnlocked", dominatingAchievementUnlocked);
        prefs.putBoolean("megaKillAchievementUnlocked", megaKillAchievementUnlocked);
        prefs.putBoolean("unstoppableAchievementUnlocked", unstoppableAchievementUnlocked);
        prefs.putBoolean("wickedSickAchievementUnlocked", wickedSickAchievementUnlocked);
        prefs.putBoolean("monsterKillAchievementUnlocked", monsterKillAchievementUnlocked);
        prefs.putBoolean("godLikeAchievementUnlocked", godLikeAchievementUnlocked);
        prefs.putBoolean("ultraKillAchievementUnlocked", ultraKillAchievementUnlocked);
        prefs.putBoolean("rampageAchievementUnlocked", rampageAchievementUnlocked);
        prefs.putBoolean("holyShitAchievementUnlocked", holyShitAchievementUnlocked);
        prefs.putInteger("stage", stage);
        prefs.putInteger("highscore", highscore);
        prefs.putInteger("normalParticlesDestroyed", normalParticlesDestroyed);
        prefs.putInteger("splitParticlesDestroyed", splitParticlesDestroyed);
        prefs.putInteger("suicideParticlesDestroyed", suicideParticlesDestroyed);
        prefs.putInteger("invisibleParticlesDestroyed", invisibleParticlesDestroyed);
        prefs.flush();
    }
}
