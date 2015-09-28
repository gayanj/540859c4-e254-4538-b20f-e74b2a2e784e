package com.particle.assassin.utils;

/**
 * Created by Gayan on 7/16/2015.
 */
public interface IActivityRequestHandler {
    public void showAds(boolean show);

    public void showInterstitialAd();

    public void signIn();

    public void signOut();

    public void rateGame();

    public void submitScore(long score);

    public void showScores();

    public void unlockAchievement(String achievementId);

    public void showAchievements();

    public boolean isSignedIn();
}
