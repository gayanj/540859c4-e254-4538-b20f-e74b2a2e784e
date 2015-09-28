package com.particle.assassin.utils;

import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;

/**
 * Created by Gayan on 7/11/2015.
 */
public class AudioManager {

    public static final AudioManager instance = new AudioManager();
    private Music playingMusic;
    private Music alertSound;
    private Sound playingSound;

    // singleton: prevent instantiation from other classes
    private AudioManager() {
    }

    public void play(Sound sound) {
        play(sound, 1);
    }

    public void play(Sound sound, float volume) {
        play(sound, volume, 1);
    }

    public void play(Sound sound, float volume, float pitch) {
        play(sound, volume, pitch, 0);
    }

    public void play(Sound sound, float volume, float pitch,
                     float pan) {
        sound.play(volume, pitch, pan);
    }

    public void play(Music music, float volume) {
        stopMusic();
        playingMusic = music;
        music.setLooping(true);
        music.setVolume(volume);
        music.play();
    }

    public void playParticleDeathSound(Sound sound) {
        stopSound();
        playingSound = sound;
        play(sound, 1);
    }

    public void playAlertSound(Music music, float volume) {
        if(alertSound == null) {
            alertSound = music;
            music.setLooping(true);
            music.setVolume(volume);
            music.play();
        }
    }

    public void stopMusic() {
        if (playingMusic != null) playingMusic.stop();
    }

    public void stopAlertSound() {
        if (alertSound != null && alertSound.isPlaying()){
            alertSound.stop();
            alertSound = null;
        }
    }

    public void stopSound() {
        if (playingSound != null) playingSound.stop();
    }

    /*public void onSettingsUpdated() {
        if (playingMusic == null) return;
        playingMusic.setVolume(GamePreferences.instance.volMusic);
        if (GamePreferences.instance.music) {
            if (!playingMusic.isPlaying()) playingMusic.play();
        } else {
            playingMusic.pause();
        }
    }*/
}
