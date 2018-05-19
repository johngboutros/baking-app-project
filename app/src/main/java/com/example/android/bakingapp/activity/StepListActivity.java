package com.example.android.bakingapp.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.android.bakingapp.R;
import com.example.android.bakingapp.data.Ingredient;
import com.example.android.bakingapp.data.Recipe;
import com.example.android.bakingapp.data.Step;
import com.example.android.bakingapp.fragment.StepDetailFragment;

import org.parceler.Parcel;
import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * An activity representing a list of Steps. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link StepDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class StepListActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String STATE_BUNDLE_KEY = StepListActivity.class.getSimpleName()
            + "_state_bundle_key";
    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;

    // Recipe Intent Extra param
    public static final String RECIPE_EXTRA_PARAM = "recipe";

    // Recipe
    private Recipe recipe;

    // Current Step
    private Step currentStep;

    @BindView(R.id.step_list)
    RecyclerView stepsRecyclerView;

    @BindView(R.id.fab)
    FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_step_list);
        ButterKnife.bind(this);

        if (savedInstanceState != null) {
            // restore saved instance
            restoreInstanceState(savedInstanceState);
        } else {
            // Keep recipe
            this.recipe = Parcels.unwrap(getIntent().getParcelableExtra(RECIPE_EXTRA_PARAM));
        }

        // Set title
        setTitle(this.recipe.getName());

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());

//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!mTwoPane) {
                    // Start detail activity
                    startIngredientsActivity(StepListActivity.this, recipe.getIngredients()
                        , recipe.getSteps());
                } else {
                    startNextFragment();
                }
            }
        });

        // Show the Up button in the action bar.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        if (findViewById(R.id.step_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;
        }

//        View recyclerView = findViewById(R.id.step_list);
//        assert recyclerView != null;
//        setupRecyclerView((RecyclerView) recyclerView);
        assert stepsRecyclerView != null;
        setupRecyclerView(stepsRecyclerView);

        if (savedInstanceState == null) {
            if (mTwoPane) {
                startIngredientsFragment(recipe.getIngredients());
            }
        } else {
            restoreInstanceState(savedInstanceState);
        }
    }

    private void setupRecyclerView(@NonNull RecyclerView recyclerView) {
        recyclerView.setAdapter(
                new SimpleItemRecyclerViewAdapter(this, recipe, this));
    }

    public static class SimpleItemRecyclerViewAdapter
            extends RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder> {

        private final StepListActivity mParentActivity;
        private final Recipe mRecipe;
        private final View.OnClickListener mOnClickListener;
        private Integer highlightedPosition = null;

        private interface Type {
            int NORMAL = 0;
            int HIGHLIGHTED = 1;
        }

        SimpleItemRecyclerViewAdapter(StepListActivity parent,
                                      Recipe recipe,
                                      View.OnClickListener onClickListener) {
            mRecipe = recipe;
            mParentActivity = parent;
            mOnClickListener = onClickListener;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.step_list_content, parent, false);

            if (viewType == Type.HIGHLIGHTED) {
                view.setBackgroundColor(mParentActivity
                        .getResources().getColor(R.color.colorPrimaryLight));
            }

            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {

            int stepPosition = position;

            if (!hasIngredients()) {
                if (!hasSteps()) {
                    // TODO Handle empty recipe
                    return;
                }
            } else {
                stepPosition--;
            }


            // Select ingredients text or step title based on position
            String text;
            if (hasIngredients() && position == 0) {
                // 1st Page is ingredients
                text = mParentActivity.getString(R.string.show_ingredients);
                holder.itemView.setTag(mRecipe.getIngredients());
                holder.mIdView.setText(" ");
            } else {
                // Step page
                Step step = mRecipe.getSteps().get(stepPosition);
                text = step.getShortDescription();
                holder.itemView.setTag(mRecipe.getSteps().get(stepPosition));
                holder.mIdView.setText(String.valueOf(stepPosition + 1));
            }

            holder.mContentView.setText(text);
            holder.itemView.setOnClickListener(mOnClickListener);
        }

        @Override
        public int getItemCount() {
            return (hasIngredients() ? 1 : 0) + (hasSteps() ? mRecipe.getSteps().size() : 0);
        }

        public void highlight(Step currentStep) {
            if (currentStep == null) {
                highlightedPosition = 0;
            } else if (mRecipe.getSteps().contains(currentStep)) {
                highlightedPosition = mRecipe.getSteps().indexOf(currentStep) + 1;
            }
            notifyDataSetChanged();
        }

        @Override
        public int getItemViewType(int position) {
            return (highlightedPosition != null && highlightedPosition.equals(position)) ?
                    Type.HIGHLIGHTED : Type.NORMAL;
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            final TextView mIdView;
            final TextView mContentView;

            ViewHolder(View view) {
                super(view);
                mIdView = view.findViewById(R.id.id_text);
                mContentView = view.findViewById(R.id.content);
            }
        }


        private boolean hasIngredients() {
            return mRecipe.getIngredients() != null && mRecipe.getIngredients().size() > 0;
        }

        private boolean hasSteps() {
            return mRecipe.getSteps() != null && mRecipe.getSteps().size() > 0;
        }
    }

    private void startStepFragment(Step step) {

        Bundle arguments = new Bundle();
        arguments.putParcelable(StepDetailFragment.ARG_STEP, Parcels.wrap(step));

        startDetailFragment(arguments);
    }

    private void startIngredientsFragment(List<Ingredient> ingredients) {

        Bundle arguments = new Bundle();
        arguments.putParcelable(StepDetailFragment.ARG_INGREDIENTS, Parcels.wrap(ingredients));

        startDetailFragment(arguments);
    }

    private void startDetailFragment(Bundle arguments) {
        StepDetailFragment fragment = new StepDetailFragment();
        fragment.setArguments(arguments);
        this.getSupportFragmentManager().beginTransaction()
                .replace(R.id.step_detail_container, fragment)
                .commit();
        refreshView(currentStep);
    }

    private static void startIngredientsActivity(Context context, List<Ingredient> ingredients
            , List<Step> nextSteps) {
        if (ingredients != null) {
            Bundle bundle = new Bundle();
            bundle.putParcelable(StepDetailFragment.ARG_INGREDIENTS, Parcels.wrap(ingredients));
            bundle.putParcelable(StepDetailActivity.ARG_NEXT_STEPS, Parcels.wrap(nextSteps));
            startDetailActivity(context, bundle);
        }
    }

    private static void startStepActivity(Context context, Step step, List<Step> nextSteps) {
        if (step != null) {
            Bundle bundle = new Bundle();
            bundle.putParcelable(StepDetailFragment.ARG_STEP, Parcels.wrap(step));
            bundle.putParcelable(StepDetailActivity.ARG_NEXT_STEPS, Parcels.wrap(nextSteps));
            startDetailActivity(context, bundle);
        }
    }

    private static void startDetailActivity(Context context, Bundle extras) {
        Intent intent = new Intent(context, StepDetailActivity.class);
        intent.putExtras(extras);
        context.startActivity(intent);
    }

    private static List<Step> getNextSteps(Step mCurrentStep, List<Step> steps) {
        List<Step> nextSteps = new ArrayList<>();
        // Should implement Step hashcode(0 & equals()?
        int stepIndex = steps.indexOf(mCurrentStep);
        if (stepIndex > -1 && stepIndex < steps.size() - 1) {
            for (int i = stepIndex + 1; i < steps.size(); i++) {
                nextSteps.add(steps.get(i));
            }
        }
        return nextSteps;
    }

    private static Step getNextStep(Step currentStep, List<Step> steps) {

        if (steps == null || steps.isEmpty()) return null;

        if (currentStep == null) return steps.get(0);

        // Should implement Step hashcode(0 & equals()?
        int stepIndex = steps.indexOf(currentStep);
        if (stepIndex > -1 && stepIndex < steps.size() - 1) {
            return steps.get(stepIndex + 1);
        }
        return null;
    }

    private void startNextFragment() {
        Step nextStep = getNextStep(currentStep, recipe.getSteps());

        currentStep = nextStep;
        if (nextStep != null) {
            startStepFragment(nextStep);
        } else {
            startIngredientsFragment(recipe.getIngredients());
        }
    }

    private void refreshView(Step currentStep) {
        // If no more steps change "next" button to "up"!
        if (getNextStep(currentStep, recipe.getSteps()) == null) {
            fab.setImageResource(R.drawable.ic_arrow_upward_black_24dp);
        } else {
            fab.setImageResource(R.drawable.ic_arrow_forward_black_24dp);
        }

        SimpleItemRecyclerViewAdapter adapter =
                (SimpleItemRecyclerViewAdapter) stepsRecyclerView.getAdapter();

        adapter.highlight(currentStep);
    }

    /**
     * A class to save the adapter's state
     */
    @Parcel
    static class SavedInstanceState {
        // Discovered recipes list
        Recipe recipe;
        Step currentStep;
        Parcelable stepsListLayoutState;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        Parcelable state = saveInstanceState();
        outState.putParcelable(STATE_BUNDLE_KEY, state);
    }

    /**
     * Generates the adapter's state as a {@link Parcelable}
     *
     * @return the adapter's instance state
     */
    private Parcelable saveInstanceState() {

        SavedInstanceState state = new SavedInstanceState();

        state.recipe = this.recipe;
        state.currentStep = this.currentStep;
        state.stepsListLayoutState = stepsRecyclerView.getLayoutManager().onSaveInstanceState();

        return Parcels.wrap(state);
    }

    /**
     * Restores the adapter's state using a {@link Parcelable} generated by
     * saveInstanceState()
     *
     * @param savedInstanceState Bundle with a {@link Parcelable} generated by saveInstanceState()
     */
    private void restoreInstanceState(Bundle savedInstanceState) {

        SavedInstanceState state = Parcels.unwrap(savedInstanceState
                .getParcelable(STATE_BUNDLE_KEY));

        this.recipe = state.recipe;
        this.currentStep = state.currentStep;
        stepsRecyclerView.getLayoutManager().onRestoreInstanceState(state.stepsListLayoutState);
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
            NavUtils.navigateUpTo(this, new Intent(this,
                    MainActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {
        List<Ingredient> ingredients = null;

        if (view.getTag() instanceof Step) {
            currentStep = (Step) view.getTag();
        } else {
            currentStep = null;
            ingredients = (List<Ingredient>) view.getTag();
        }

        if (mTwoPane) {

            if (currentStep != null) {
                startStepFragment(currentStep);
            } else if (ingredients != null) {
                startIngredientsFragment(ingredients);
            } else {
                // TODO handle empty recipe
            }

        } else {
            if (currentStep != null) {
                List<Step> nextSteps = getNextSteps(currentStep, recipe.getSteps());
                startStepActivity(view.getContext(), currentStep, nextSteps);
            } else if (ingredients != null) {
                startIngredientsActivity(view.getContext(), ingredients, recipe.getSteps());
            } else {
                // TODO handle empty recipe
            }
        }
    }
}
