package com.particle.assassin.world;

/**
 * Created by Gayan on 8/3/2015.
 */
public interface WorldControllerInterface {
    public void init();
    public void resize(int width, int height);
    public void initPhysics();
    public void update(float deltaTime);
}
