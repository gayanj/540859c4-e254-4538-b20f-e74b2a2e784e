package com.platform.rider.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.platform.rider.main.AnyDirection;
import com.platform.rider.utils.GameConstants;
import com.platform.rider.utils.IActivityRequestHandler;
import com.platform.rider.utils.IGoogleServices;

public class DesktopLauncher implements IActivityRequestHandler, IGoogleServices{
    private static DesktopLauncher application;
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        config.height = GameConstants.APP_HEIGHT;
        config.width = GameConstants.APP_WIDTH;
        if (application == null) {
            application = new DesktopLauncher();
        }
		new LwjglApplication(new AnyDirection(application), config);
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
