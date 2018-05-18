package com.example.android.bakingapp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.example.android.bakingapp.R;
import com.example.android.bakingapp.data.Ingredient;
import com.example.android.bakingapp.data.Recipe;
import com.example.android.bakingapp.data.Step;
import com.example.android.bakingapp.fragment.StepDetailFragment;

import org.parceler.Parcel;
import org.parceler.Parcels;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * An activity representing a single Step detail screen. This
 * activity is only used on narrow width devices. On tablet-size devices,
 * item details are presented side-by-side with a list of items
 * in a {@link StepListActivity}.
 */
public class StepDetailActivity extends AppCompatActivity {

    private static final String STATE_BUNDLE_KEY = StepDetailActivity.class.getSimpleName()
            + "_state_bundle_key";

    public final static String ARG_NEXT_STEPS = "next_steps";
    private List<Step> mNextSteps;

    @BindView(R.id.fab)
    FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_step_detail);
        ButterKnife.bind(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.detail_toolbar);
        setSupportActionBar(toolbar);

        // Show the Up button in the action bar.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        // savedInstanceState is non-null when there is fragment state
        // saved from previous configurations of this activity
        // (e.g. when rotating the screen from portrait to landscape).
        // In this case, the fragment will automatically be re-added
        // to its container so we don't need to manually add it.
        // For more information, see the Fragments API guide at:
        //
        // http://developer.android.com/guide/components/fragments.html
        //
        if (savedInstanceState == null) {
            // Create the detail fragment and add it to the activity
            // using a fragment transaction.

            Parcelable stepArgument = getIntent().getParcelableExtra(StepDetailFragment.ARG_STEP);
            Parcelable stepsParcel = getIntent().getParcelableExtra(StepDetailActivity.ARG_NEXT_STEPS);
            if (stepsParcel != null) {
                mNextSteps = Parcels.unwrap(stepsParcel);
            }

            Parcelable ingredientsArguments = getIntent()
                    .getParcelableExtra(StepDetailFragment.ARG_INGREDIENTS);

            setup((List<Ingredient>) Parcels.unwrap(ingredientsArguments),
                    (Step) Parcels.unwrap(stepArgument));
        } else {
            // TODO restore state and setup
            onRestoreInstanceState(savedInstanceState);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            // This ID represents the Home or Up button. In the case of this
            // activity, the Up button is shown. Use NavUtils to allow users
            // to navigate up one level in the application structure. For
            // more details, see the Navigation pattern on Android Design:
            //
            // http://developer.android.com/design/patterns/navigation.html#up-vs-back
            //
            goBack();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void goBack() {
        NavUtils.navigateUpTo(this, new Intent(this, StepListActivity.class));
    }


    private void setup(List<Ingredient> ingredients, Step step) {

        Bundle arguments = new Bundle();
        if (step != null) {
            arguments.putParcelable(StepDetailFragment.ARG_STEP, Parcels.wrap(step));
        } else if (ingredients != null) {
            arguments.putParcelable(StepDetailFragment.ARG_INGREDIENTS, Parcels.wrap(ingredients));
        }

        StepDetailFragment fragment = new StepDetailFragment();
        fragment.setArguments(arguments);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.step_detail_container, fragment)
                .commit();

        // Setup Floating Button
        setupFab();
    }

    private void setupFab() {
        if (hasNext()) {
            fab.setImageResource(R.drawable.ic_arrow_forward_black_24dp);
        } else {
            fab.setImageResource(R.drawable.ic_arrow_upward_black_24dp);
        }

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (hasNext()) {
                    setup(null, mNextSteps.remove(0));
                } else {
                    goBack();
                }
            }
        });
    }

    private boolean hasNext() {
        return mNextSteps != null && mNextSteps.size() > 0;
    }


    /**
     * A class to save the adapter's state
     */
    @Parcel
    static class SavedInstanceState {
        List<Step> nextSteps;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        SavedInstanceState state = new SavedInstanceState();
        state.nextSteps = this.mNextSteps;
        outState.putParcelable(STATE_BUNDLE_KEY, Parcels.wrap(state));
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        SavedInstanceState state = Parcels.unwrap(savedInstanceState
                .getParcelable(STATE_BUNDLE_KEY));
        this.mNextSteps = state.nextSteps;
        setupFab();
    }
}
