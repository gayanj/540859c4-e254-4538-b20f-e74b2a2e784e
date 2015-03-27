package com.platform.rider.world;

import com.badlogic.gdx.math.Vector2;
import com.platform.rider.sprites.Ball;
import com.platform.rider.sprites.Platform;

import java.util.ArrayList;

/**
 * Created by Gayan on 3/8/2015.
 */
public class World {
    /**
     * The blocks making up the world *
     */
    ArrayList<Platform> blocks = new ArrayList<Platform>();
    /**
     * Our player controlled hero *
     */
    Ball bob;

    // Getters -----------
    public ArrayList getPlatforms() {
        return blocks;
    }

    public Ball getBall() {
        return bob;
    }
    // --------------------

    public World() {
        createDemoWorld();
    }

    private void createDemoWorld() {
        bob = new Ball(new Vector2(7, 2));

        for (int i = 0; i < 10; i++) {
            blocks.add(new Platform(new Vector2(i, 0)));
            blocks.add(new Platform(new Vector2(i, 6)));
            if (i > 2)
                blocks.add(new Platform(new Vector2(i, 1)));
        }
        blocks.add(new Platform(new Vector2(9, 2)));
        blocks.add(new Platform(new Vector2(9, 3)));
        blocks.add(new Platform(new Vector2(9, 4)));
        blocks.add(new Platform(new Vector2(9, 5)));

        blocks.add(new Platform(new Vector2(6, 3)));
        blocks.add(new Platform(new Vector2(6, 4)));
        blocks.add(new Platform(new Vector2(6, 5)));
    }
}
