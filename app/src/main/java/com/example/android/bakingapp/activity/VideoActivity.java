package com.example.android.bakingapp.activity;

import android.annotation.SuppressLint;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;

import com.example.android.bakingapp.R;
import com.example.android.bakingapp.fragment.StepDetailFragment;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import org.parceler.Parcel;
import org.parceler.Parcels;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class VideoActivity extends AppCompatActivity implements Player.EventListener {

    private static final String STATE_BUNDLE_KEY = VideoActivity.class.getSimpleName()
            + "_state_bundle_key";

    // Player state args
    public static final String ARG_PLAYER_PLAYING = "player_playing";
    public static final String ARG_PLAYER_POSITION = "player_position";

    /**
     * ExoPlayer
     */
    private ExoPlayer mExoPlayer;
    @BindView(R.id.player_pv)
    PlayerView mPlayerView;

    /**
     * ExoPLayer state
     */
    private Boolean mIsPlaying;
    private Long mPlayerPosition;

    /**
     * Video param
     */
    public final static String ARG_VIDEO_URL = "video_url";

    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();
    private View mContentView;
    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };
    private View mControlsView;
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
            mControlsView.setVisibility(View.VISIBLE);
        }
    };
    private boolean mVisible;
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };
    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_video);
        ButterKnife.bind(this);

        mVisible = true;
        mControlsView = findViewById(R.id.fullscreen_content_controls);

        // TODO remove mContentView
        mContentView = mPlayerView;//findViewById(R.id.fullscreen_content);

        // Try to restore player state from intent
        mIsPlaying = getIntent().getBooleanExtra(ARG_PLAYER_PLAYING, true);
        mPlayerPosition = getIntent().getLongExtra(ARG_PLAYER_POSITION, 0);


        // Set up the user interaction to manually show or hide the system UI.
        mContentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggle();
            }
        });

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        findViewById(R.id.dummy_button).setOnTouchListener(mDelayHideTouchListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Acquire data
        String videoUrl = getIntent().getStringExtra(ARG_VIDEO_URL);
        // Setup player and play video
        setupPlayer(videoUrl);
    }

    @Override
    protected void onPause() {
        super.onPause();
        releasePlayer();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Stop activity if portrait orientation
        if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            finish();
        }
    }
    /**
     * A class to save the adapter's state
     */
    @Parcel
    static class SavedInstanceState {
        Long playerPosition;
        Boolean isPlaying;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        SavedInstanceState state = new SavedInstanceState();
        if (mExoPlayer != null) {
            mPlayerPosition = mExoPlayer.getCurrentPosition();
            state.playerPosition = mPlayerPosition;
            state.isPlaying = mIsPlaying;
        }
        outState.putParcelable(STATE_BUNDLE_KEY, Parcels.wrap(state));
    }

    @Override
    public void onRestoreInstanceState(@Nullable Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState == null) return;
        SavedInstanceState state = Parcels.unwrap(savedInstanceState
                .getParcelable(STATE_BUNDLE_KEY));
        this.mIsPlaying = state.isPlaying;
        this.mPlayerPosition = state.playerPosition;
    }


    /**
     * Initializes the player and plays the given url.
     *
     * @param videoURL to be played
     */
    private void setupPlayer(String videoURL) {

        if (!TextUtils.isEmpty(videoURL)) {
            // Setup player
            Uri uri = Uri.parse(videoURL);

            TrackSelector trackSelector = new DefaultTrackSelector();
            mExoPlayer = ExoPlayerFactory.newSimpleInstance(this, trackSelector);
            mPlayerView.setPlayer(mExoPlayer);

            // Set the ExoPlayer.EventListener to this activity.
            mExoPlayer.addListener(this);

            // Prepare the MediaSource.
            String userAgent = Util.getUserAgent(this, getString(R.string.app_name));
            MediaSource mediaSource = new ExtractorMediaSource
                    .Factory(new DefaultDataSourceFactory(this, userAgent))
                    .createMediaSource(uri);
            mExoPlayer.prepare(mediaSource);
            // Restore player state
            mExoPlayer.setPlayWhenReady(mIsPlaying == null ? true : mIsPlaying);
            // Restore player position
            if (mPlayerPosition != null) {
                mExoPlayer.seekTo(mPlayerPosition);
                mPlayerPosition = null;
            }

        }
    }

    /**
     * Releases ExoPlayer
     */
    private void releasePlayer() {
        if (mExoPlayer != null) {
            mExoPlayer.stop();
            mExoPlayer.release();
            mExoPlayer = null;
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }

    private void toggle() {
        if (mVisible) {
            hide();
        } else {
            show();
        }
    }

    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mControlsView.setVisibility(View.GONE);
        mVisible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    @SuppressLint("InlinedApi")
    private void show() {
        // Show the system bar
        mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        mVisible = true;

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable);
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
    }

    /**
     * Schedules a call to hide() in delay milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }

    /**
     * Called when the value returned from either {@link #getPlayWhenReady()} or
     * {@link #getPlaybackState()} changes.
     *
     * @param playWhenReady Whether playback will proceed when ready.
     * @param playbackState One of the {@code STATE} constants.
     */
    @SuppressWarnings("JavadocReference")
    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        if (playbackState == Player.STATE_READY) {
            mIsPlaying = playWhenReady;
        }
    }

    @Override
    public void onRepeatModeChanged(int repeatMode) {
    }

    @Override
    public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {
    }

    @Override
    public void onTimelineChanged(Timeline timeline, Object manifest, int reason) {
    }

    @Override
    public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
    }

    @Override
    public void onLoadingChanged(boolean isLoading) {
    }

    @Override
    public void onPlayerError(ExoPlaybackException error) {
    }

    @Override
    public void onPositionDiscontinuity(int reason) {
    }

    @Override
    public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {
    }

    @Override
    public void onSeekProcessed() {
    }
}
