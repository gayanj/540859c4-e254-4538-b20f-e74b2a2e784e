package com.platform.rider.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.platform.rider.main.AnyDirection;
import com.platform.rider.utils.GameConstants;
import com.platform.rider.utils.IActivityRequestHandler;

public class DesktopLauncher implements IActivityRequestHandler {
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
}
