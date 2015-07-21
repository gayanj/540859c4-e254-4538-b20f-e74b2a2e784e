package com.platform.rider.utils;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * Created by Gayan on 4/12/2015.
 */
public class Powerups {
    String type = "none";
    int remaining = 0;
    boolean active = false;
    boolean pickedUp = false;
    int powerCounter = 0;
    Sprite sprite;

    public Powerups(){

    }
    public Powerups(String type, int remaining, TextureRegion textureRegion, boolean pickedUp){
        this.type = type;
        this.remaining = remaining;
        createSprite(textureRegion);
        this.pickedUp = pickedUp;
    }
    public void createSprite(TextureRegion textureRegion){
        sprite = new Sprite(textureRegion);
        sprite.setFlip(false,true);
    }

    public Sprite getSprite() {
        return sprite;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getRemaining() {
        return remaining;
    }

    public void setRemaining(int remaining) {
        this.remaining = remaining;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public int getPowerCounter() {
        return powerCounter;
    }

    public void setPowerCounter(int powerCounter) {
        this.powerCounter = powerCounter;
    }

    public boolean isPickedUp() {
        return pickedUp;
    }

    public void setPickedUp(boolean pickedUp) {
        this.pickedUp = pickedUp;
    }
}
