package com.particle.assassin.android;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.google.android.gms.ads.*;
import com.google.android.gms.games.Games;
import com.google.example.games.basegameutils.GameHelper;
import com.google.example.games.basegameutils.GameHelper.GameHelperListener;
import com.particle.assassin.main.AnyDirection;
import com.particle.assassin.utils.GameConstants;
import com.particle.assassin.utils.GamePreferences;
import com.particle.assassin.utils.IActivityRequestHandler;

public class AndroidLauncher extends AndroidApplication implements IActivityRequestHandler {
    private static final String BANNER_AD_UNIT_ID = "ca-app-pub-8464762813805843/5625420417";
    private static final String INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-8464762813805843/7102153616";
    private final int SHOW_ADS = 1;
    private final int HIDE_ADS = 0;
    protected AdView adView;
    protected InterstitialAd interstitialAd;
    protected View gameView;
    private GameHelper _gameHelper;
    private final static int REQUEST_CODE_UNUSED = 9002;
    private boolean isSignedIn = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);

        _gameHelper = new GameHelper(this, GameHelper.CLIENT_GAMES);
        _gameHelper.enableDebugLog(false);
        GameHelperListener gameHelperListener = new GameHelper.GameHelperListener() {
            @Override
            public void onSignInSucceeded() {
                isSignedIn = true;
                uploadPreviousGameData();
            }

            @Override
            public void onSignInFailed() {
                isSignedIn = false;
            }
        };
        _gameHelper.setMaxAutoSignInAttempts(0);
        _gameHelper.setup(gameHelperListener);

        RelativeLayout layout = new RelativeLayout(this);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        layout.setLayoutParams(params);

        AdView admobView = createAdView();
        layout.addView(admobView);
        View gameView = createGameView(config);
        layout.addView(gameView);

        setContentView(layout);
        startAdvertising(admobView);

        interstitialAd = new InterstitialAd(this);
        interstitialAd.setAdUnitId(INTERSTITIAL_AD_UNIT_ID);
        requestNewInterstitial();

        interstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                requestNewInterstitial();
            }
        });

    }

    protected Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SHOW_ADS: {
                    adView.setVisibility(View.VISIBLE);
                    break;
                }
                case HIDE_ADS: {
                    adView.setVisibility(View.GONE);
                    break;
                }
            }
        }
    };

    @Override
    public void showAds(boolean show) {
        handler.sendEmptyMessage(show ? SHOW_ADS : HIDE_ADS);
    }

    @Override
    public void showInterstitialAd() {
        try {
            runOnUiThread(new Runnable() {
                public void run() {
                    if (interstitialAd.isLoaded()) {
                        interstitialAd.show();
                    }
                }
            });
        } catch (Exception e) {
        }
    }

    private void requestNewInterstitial() {
        AdRequest adRequest = new AdRequest.Builder().build();

        interstitialAd.loadAd(adRequest);
    }

    private AdView createAdView() {
        adView = new AdView(this);
        adView.setAdSize(AdSize.SMART_BANNER);
        adView.setAdUnitId(BANNER_AD_UNIT_ID);
        adView.setId(12345);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
        params.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
        adView.setLayoutParams(params);
        adView.setBackgroundColor(Color.BLACK);
        return adView;
    }

    private View createGameView(AndroidApplicationConfiguration cfg) {
        gameView = initializeForView(new AnyDirection(this), cfg);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
        params.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
        params.addRule(RelativeLayout.BELOW, adView.getId());
        gameView.setLayoutParams(params);
        return gameView;
    }

    private void startAdvertising(AdView adView) {
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
        adView.setVisibility(View.GONE);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (adView != null) adView.resume();
    }

    @Override
    public void onPause() {
        if (adView != null) adView.pause();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        if (adView != null) adView.destroy();
        super.onDestroy();
    }

    @Override
    public void signIn() {
        try {
            runOnUiThread(new Runnable() {
                //@Override
                public void run() {
                    _gameHelper.beginUserInitiatedSignIn();
                }
            });
        } catch (Exception e) {
            Gdx.app.log("MainActivity", "Log in failed: " + e.getMessage() + ".");
        }
    }

    @Override
    public void signOut() {
        try {
            runOnUiThread(new Runnable() {
                //@Override
                public void run() {
                    _gameHelper.signOut();
                }
            });
        } catch (Exception e) {
            Gdx.app.log("MainActivity", "Log out failed: " + e.getMessage() + ".");
        }
    }

    @Override
    public void rateGame() {
        // Replace the end of the URL with the package of your game
        String str = "https://play.google.com/store/apps/details?id=com.particle.assassin.android";
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(str)));
    }

    @Override
    public void submitScore(long score) {
        if (isSignedIn()) {
            Games.Leaderboards.submitScore(_gameHelper.getApiClient(), getString(R.string.leaderboard_id), score);
        }
    }

    @Override
    public void showScores() {
        if (isSignedIn())
            startActivityForResult(Games.Leaderboards.getLeaderboardIntent(_gameHelper.getApiClient(), getString(R.string.leaderboard_id)), REQUEST_CODE_UNUSED);
        else {
            // Maybe sign in here then redirect to showing scores?
            this.signIn();
        }
    }

    @Override
    public void unlockAchievement(String achievementId) {
        if (isSignedIn()) {
            if (GameConstants.KILLING_SPREE_ACHIEVEMENT.equals(achievementId)) {
                Games.Achievements.unlock(_gameHelper.getApiClient(), getString(R.string.achievement_killing_spree));
            } else if (GameConstants.DOMINATING_ACHIEVEMENT.equals(achievementId)) {
                Games.Achievements.unlock(_gameHelper.getApiClient(), getString(R.string.achievement_dominating));
            } else if (GameConstants.MEGAKILL_ACHIEVEMENT.equals(achievementId)) {
                Games.Achievements.unlock(_gameHelper.getApiClient(), getString(R.string.achievement_mega_kill));
            } else if (GameConstants.UNSTOPPABLE_ACHIEVEMENT.equals(achievementId)) {
                Games.Achievements.unlock(_gameHelper.getApiClient(), getString(R.string.achievement_unstoppable));
            } else if (GameConstants.WICKEDSICK_ACHIEVEMENT.equals(achievementId)) {
                Games.Achievements.unlock(_gameHelper.getApiClient(), getString(R.string.achievement_wicked_sick));
            } else if (GameConstants.MONSTERKILL_ACHIEVEMENT.equals(achievementId)) {
                Games.Achievements.unlock(_gameHelper.getApiClient(), getString(R.string.achievement_monster_kill));
            } else if (GameConstants.GODLIKE_ACHIEVEMENT.equals(achievementId)) {
                Games.Achievements.unlock(_gameHelper.getApiClient(), getString(R.string.achievement_god_like));
            } else if (GameConstants.ULTRAKILL_ACHIEVEMENT.equals(achievementId)) {
                Games.Achievements.unlock(_gameHelper.getApiClient(), getString(R.string.achievement_ultra_kill));
            } else if (GameConstants.RAMPAGE_ACHIEVEMENT.equals(achievementId)) {
                Games.Achievements.unlock(_gameHelper.getApiClient(), getString(R.string.achievement_rampage));
            } else if (GameConstants.HOLYSHIT_ACHIEVEMENT.equals(achievementId)) {
                Games.Achievements.unlock(_gameHelper.getApiClient(), getString(R.string.achievement_beyond_god_like));
            }
        }
    }

    @Override
    public void showAchievements() {
        if (isSignedIn())
            startActivityForResult(Games.Achievements.getAchievementsIntent(_gameHelper.getApiClient()), REQUEST_CODE_UNUSED);
        else {
            // Maybe sign in here then redirect to showing scores?
            this.signIn();
        }
    }

    @Override
    public boolean isSignedIn() {
        return isSignedIn;
    }

    @Override
    public void uploadPreviousGameData() {
        if (GamePreferences.instance.highscore > 0) {
            submitScore(GamePreferences.instance.highscore);
        }
        if (GamePreferences.instance.killingSpreeAchievementUnlocked) {
            unlockAchievement(GameConstants.KILLING_SPREE_ACHIEVEMENT);
        }
        if (GamePreferences.instance.dominatingAchievementUnlocked) {
            unlockAchievement(GameConstants.DOMINATING_ACHIEVEMENT);
        }
        if (GamePreferences.instance.megaKillAchievementUnlocked) {
            unlockAchievement(GameConstants.MEGAKILL_ACHIEVEMENT);
        }
        if (GamePreferences.instance.unstoppableAchievementUnlocked) {
            unlockAchievement(GameConstants.UNSTOPPABLE_ACHIEVEMENT);
        }
        if (GamePreferences.instance.wickedSickAchievementUnlocked) {
            unlockAchievement(GameConstants.WICKEDSICK_ACHIEVEMENT);
        }
        if (GamePreferences.instance.monsterKillAchievementUnlocked) {
            unlockAchievement(GameConstants.MONSTERKILL_ACHIEVEMENT);
        }
        if (GamePreferences.instance.godLikeAchievementUnlocked) {
            unlockAchievement(GameConstants.GODLIKE_ACHIEVEMENT);
        }
        if (GamePreferences.instance.ultraKillAchievementUnlocked) {
            unlockAchievement(GameConstants.ULTRAKILL_ACHIEVEMENT);
        }
        if (GamePreferences.instance.rampageAchievementUnlocked) {
            unlockAchievement(GameConstants.RAMPAGE_ACHIEVEMENT);
        }
        if (GamePreferences.instance.holyShitAchievementUnlocked) {
            unlockAchievement(GameConstants.HOLYSHIT_ACHIEVEMENT);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        _gameHelper.onStart(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        _gameHelper.onStop();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        _gameHelper.onActivityResult(requestCode, resultCode, data);
    }
}
