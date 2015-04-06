package com.platform.rider.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Touchpad;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.viewport.StretchViewport;

/**
 * Created by Gayan on 3/28/2015.
 */
public class TouchPadHelper {
    private static final String TAG = TouchPadHelper.class.getName();
    private Stage stage;
    private Touchpad touchpad;
    private Touchpad.TouchpadStyle touchpadStyle;
    private Skin touchpadSkin;
    private Drawable touchBackground;
    private Drawable touchKnob;

    public TouchPadHelper(){
        init();
    }

    private void init(){
        //Create a touchpad skin
        touchpadSkin = new Skin();
        //Set powerButton image
        touchpadSkin.add("touchBackground", new Texture("data/touchBackground.png"));
        //Set knob image
        touchpadSkin.add("touchKnob", new Texture("data/touchKnob.png"));
        //Create TouchPad Style
        touchpadStyle = new Touchpad.TouchpadStyle();
        //Create Drawable's from TouchPad skin
        touchBackground = touchpadSkin.getDrawable("touchBackground");
        touchKnob = touchpadSkin.getDrawable("touchKnob");
        //Apply the Drawables to the TouchPad Style
        touchpadStyle.background = touchBackground;
        touchpadStyle.knob = touchKnob;
        //Create new TouchPad with the created style
        touchpad = new Touchpad(10, touchpadStyle);
        //setBounds(x,y,width,height)
        touchpad.setBounds(GameConstants.APP_WIDTH - 215, 15, 200, 200);

        //Create a Stage and add TouchPad
        stage = new Stage(new StretchViewport(GameConstants.APP_WIDTH, GameConstants.APP_HEIGHT));
        stage.addActor(touchpad);
        //stage.getViewport().update(GameConstants.APP_WIDTH, GameConstants.APP_HEIGHT, false);
    }

    public void render(){
        stage.act(Gdx.graphics.getDeltaTime());
        stage.draw();
    }

    public Stage getStage() {
        return stage;
    }

    public Touchpad getTouchpad() {
        return touchpad;
    }
}
