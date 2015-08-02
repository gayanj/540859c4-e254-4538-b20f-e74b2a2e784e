package com.platform.rider.client;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.backends.gwt.GwtApplication;
import com.badlogic.gdx.backends.gwt.GwtApplicationConfiguration;
import com.platform.rider.main.AnyDirection;
import com.platform.rider.utils.IActivityRequestHandler;

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
    public boolean isSignedIn() {
        return false;
    }
}