package com.platform.rider.worldRenderer;

/**
 * Created by Gayan on 8/3/2015.
 */
public interface WorldRendererInterface {
    public void init();
    public void render();
    public void resize(int width, int height);
    public void dispose();
}
