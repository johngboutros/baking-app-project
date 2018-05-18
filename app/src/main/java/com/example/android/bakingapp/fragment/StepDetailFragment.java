package com.example.android.bakingapp.fragment;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.android.bakingapp.R;
import com.example.android.bakingapp.activity.StepDetailActivity;
import com.example.android.bakingapp.activity.StepListActivity;
import com.example.android.bakingapp.activity.VideoActivity;
import com.example.android.bakingapp.data.Ingredient;
import com.example.android.bakingapp.data.Step;
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
import com.squareup.picasso.Picasso;

import org.parceler.Parcel;
import org.parceler.Parcels;

import java.util.List;

/**
 * A fragment representing a single Step detail screen.
 * This fragment is either contained in a {@link StepListActivity}
 * in two-pane mode (on tablets) or a {@link StepDetailActivity}
 * on handsets.
 */
public class StepDetailFragment extends Fragment implements Player.EventListener {

    private static final String STATE_BUNDLE_KEY = StepDetailFragment.class.getSimpleName()
            + "_state_bundle_key";

    /**
     * The fragment arguments representing a step (or ingredients) that this fragment
     * represents.
     */
    public static final String ARG_STEP = "step";
    public static final String ARG_INGREDIENTS = "ingredients";

    /**
     * The content this fragment is presenting (either a step or ingredients).
     */
    private Step mStep;
    private List<Ingredient> mIngredients;
    private String mTitle;

    /**
     * ExoPlayer
     */
    private ExoPlayer mExoPlayer;
    private PlayerView mPlayerView;
    private View mPlayerLoading;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public StepDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_STEP)) {
            // Load the dummy content specified by the fragment
            // arguments. In a real-world scenario, use a Loader
            // to load content from a content provider.
            mStep = Parcels.unwrap(getArguments().getParcelable(ARG_STEP));
            mTitle = mStep.getShortDescription();
            // Start video in fullscreen IF exists AND landscape AND phone
            boolean tabletMode = getActivity().findViewById(R.id.step_list) != null;
            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE
                    && !tabletMode && !TextUtils.isEmpty(mStep.getVideoURL())) {
                goFullscreenVideo(mStep.getVideoURL());
            }
        } else if (getArguments().containsKey(ARG_INGREDIENTS)) {
            mIngredients = Parcels.unwrap(getArguments().getParcelable(ARG_INGREDIENTS));
            mTitle = getString(R.string.ingredients_label);
        }

        setupTitle(mTitle);
    }

    private void setupTitle(String title) {
        Activity activity = this.getActivity();
        CollapsingToolbarLayout appBarLayout = activity.findViewById(R.id.toolbar_layout);
        if (appBarLayout != null) {
            appBarLayout.setTitle(title);
        }
    }

    /**
     * Starts video in full screen.
     *
     * @param videoUrl
     */
    private void goFullscreenVideo(String videoUrl) {
        Intent i = new Intent(getContext(), VideoActivity.class);
        i.putExtra(VideoActivity.ARG_VIDEO_URL, videoUrl);
        getActivity().startActivity(i);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = null;

        if (mStep != null) {
            rootView = getStepLayout(inflater, container, mStep);
        } else if (mIngredients != null) {
            rootView = getIngredientsLayout(inflater, container, mIngredients);
        }

        return rootView;
    }

    @Override
    public void onDestroyView() {
        releasePlayer();
        super.onDestroyView();
    }

    private ViewGroup getIngredientsLayout(LayoutInflater inflater, ViewGroup container, List<Ingredient> ingredients) {
        int layoutResId = R.layout.fragment_step_detail_ingredients;

        ViewGroup layout = (ViewGroup) inflater.inflate(layoutResId,
                container, false);

        // Setup ingredients layout
        LinearLayout ingredientsContainer = layout.findViewById(R.id.ingredients_container_ll);
        displayIngredients(inflater, ingredientsContainer, ingredients);

        return layout;
    }

    /**
     * Sets up the layout for the given step and returns a view that represent it.
     *
     * @param container the layout container
     * @param step      the given step to render
     * @return view represents the layout for the given step
     */
    private ViewGroup getStepLayout(LayoutInflater inflater, ViewGroup container, Step step) {

        ViewGroup layout = (ViewGroup) inflater.inflate(R.layout.fragment_step_detail_step,
                container, false);

        // Setup step layout
        ImageView thumbnailIv = layout.findViewById(R.id.step_thumbnail_iv);
        if (!TextUtils.isEmpty(step.getThumbnailURL())) {
            thumbnailIv.setVisibility(View.VISIBLE);
            Picasso.with(getContext()).load(step.getThumbnailURL())
                    .fit()
                    .centerCrop()
                    .placeholder(R.drawable.bg_recipe_thumb)
                    .into(thumbnailIv);
        } else {
            thumbnailIv.setVisibility(View.GONE);
        }

        mPlayerView = layout.findViewById(R.id.step_video_pv);
        mPlayerLoading = layout.findViewById(R.id.step_video_loading_pb);

        if (!TextUtils.isEmpty(step.getVideoURL())) {
            // Setup player
            mPlayerLoading.setVisibility(View.VISIBLE);
            setupPlayer(mPlayerView, step.getId(), step.getVideoURL());
        } else {
            // Hide player
            mPlayerView.setVisibility(View.GONE);
        }

        TextView contentTv = layout.findViewById(R.id.step_content_tv);
        if (!TextUtils.isEmpty(step.getDescription())) {
            contentTv.setText(step.getDescription());
        } else {
            contentTv.setText(getString(R.string.step_no_description));
        }


        return layout;
    }

    private void setupPlayer(PlayerView playerView, int stepId, String videoURL) {

        Uri uri = Uri.parse(videoURL);

        TrackSelector trackSelector = new DefaultTrackSelector();
        mExoPlayer = ExoPlayerFactory.newSimpleInstance(getContext(), trackSelector);
        playerView.setPlayer(mExoPlayer);

        // Set the ExoPlayer.EventListener to this activity.
        mExoPlayer.addListener(this);

        // Prepare the MediaSource.
        String userAgent = Util.getUserAgent(getContext(), getString(R.string.app_name));
        MediaSource mediaSource = new ExtractorMediaSource
                .Factory(new DefaultDataSourceFactory(getContext(), userAgent))
                .createMediaSource(uri);
        mExoPlayer.prepare(mediaSource);
        mExoPlayer.setPlayWhenReady(true);
    }

    private void releasePlayer() {
        if (mExoPlayer != null) {
            mExoPlayer.stop();
            mExoPlayer.release();
            mExoPlayer = null;
        }
    }

    private void displayIngredients(LayoutInflater inflater, ViewGroup container,
                                    List<Ingredient> ingredients) {

        for (Ingredient ingredient : ingredients) {

            View ingredientItem = inflater
                    .inflate(R.layout.fragment_step_detail_ingredients_item, container,
                            false);

            TextView quantityTv = ingredientItem.findViewById(R.id.quantity_tv);

            String formattedQuantity = String.format(getString(R.string.ingredient_quantity_template),
                    String.valueOf(ingredient.getQuantity()), ingredient.getMeasure());
            quantityTv.setText(formattedQuantity);

            TextView ingredientTv = ingredientItem.findViewById(R.id.ingredient_tv);

            ingredientTv.setText(ingredient.getIngredient());

            container.addView(ingredientItem);
        }
    }

    /**
     * Called when the value returned from either {@link #getPlayWhenReady()} or
     * {@link #getPlaybackState()} changes.
     *
     * @param playWhenReady Whether playback will proceed when ready.
     * @param playbackState One of the {@code STATE} constants.
     */
    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        if (playbackState == Player.STATE_READY) {
            mPlayerLoading.setVisibility(View.GONE);
            mPlayerView.setVisibility(View.VISIBLE);
        }
    }

    /**
     * A class to save the adapter's state
     */
    @Parcel
    static class SavedInstanceState {
        String title;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        SavedInstanceState state = new SavedInstanceState();
        state.title = this.mTitle;
        outState.putParcelable(STATE_BUNDLE_KEY, Parcels.wrap(state));
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState == null) return;
        SavedInstanceState state = Parcels.unwrap(savedInstanceState
                .getParcelable(STATE_BUNDLE_KEY));
        this.mTitle = state.title;
        setupTitle(mTitle);
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
