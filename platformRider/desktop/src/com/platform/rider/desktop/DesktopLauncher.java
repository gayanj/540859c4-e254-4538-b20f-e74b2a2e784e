package com.platform.rider.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.platform.rider.main.AnyDirection;
import com.platform.rider.utils.GameConstants;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        config.height = GameConstants.APP_HEIGHT;
        config.width = GameConstants.APP_WIDTH;
		new LwjglApplication(new AnyDirection(), config);
	}
}
