package com.particle.assassin.utils;

/**
 * Created by Gayan on 7/31/2015.
 */
public interface IGoogleServices {
    public void signIn();

    public void signOut();

    public void rateGame();

    public void submitScore(long score);

    public void showScores();

    public boolean isSignedIn();
}
