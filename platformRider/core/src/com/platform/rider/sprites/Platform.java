package com.platform.rider.sprites;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

/**
 * Created by Gayan on 3/8/2015.
 */
public class Platform {
    static final float SIZE = 1f;

    Vector2 	position = new Vector2();
    Rectangle 	bounds = new Rectangle();

    public Platform(Vector2 pos) {
        this.position = pos;
        this.bounds.width = SIZE;
        this.bounds.height = SIZE;
    }

    public Rectangle getBounds() {
        return bounds;
    }

    public Vector2 getPosition() {
        return position;
    }
}
