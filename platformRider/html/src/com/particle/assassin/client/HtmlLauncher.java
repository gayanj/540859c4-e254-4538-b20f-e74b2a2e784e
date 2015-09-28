package com.particle.assassin.client;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.backends.gwt.GwtApplication;
import com.badlogic.gdx.backends.gwt.GwtApplicationConfiguration;
import com.particle.assassin.main.AnyDirection;
import com.particle.assassin.utils.IActivityRequestHandler;

public class HtmlLauncher extends GwtApplication implements IActivityRequestHandler {

        @Override
        public GwtApplicationConfiguration getConfig () {
                return new GwtApplicationConfiguration(480, 320);
        }

        @Override
        public ApplicationListener getApplicationListener () {
                return new AnyDirection(this);
        }

    @Override
    public void showAds(boolean show) {

    }

    @Override
    public void showInterstitialAd() {

    }

    @Override
    public void signIn() {

    }

    @Override
    public void signOut() {

    }

    @Override
    public void rateGame() {

    }

    @Override
    public void submitScore(long score) {

    }

    @Override
    public void showScores() {

    }

    @Override
    public void unlockAchievement(String achievementId) {

    }

    @Override
    public void showAchievements() {

    }

    @Override
    public boolean isSignedIn() {
        return false;
    }
}