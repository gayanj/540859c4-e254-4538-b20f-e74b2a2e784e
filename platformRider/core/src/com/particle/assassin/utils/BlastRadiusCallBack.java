package com.particle.assassin.utils;

import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.QueryCallback;

import java.util.Vector;

/**
 * Created by Gayan on 4/4/2015.
 */
public class BlastRadiusCallBack implements QueryCallback {

    Vector<Body> foundBodies = new Vector<Body>();
    @Override
    public boolean reportFixture(Fixture fixture) {
        foundBodies.add(fixture.getBody());
        return true;
    }

    public Vector<Body> getFoundBodies() {
        return foundBodies;
    }

    public void setFoundBodies(Vector<Body> foundBodies) {
        this.foundBodies = foundBodies;
    }
}
