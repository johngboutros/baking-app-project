package com.example.android.bakingapp.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.android.bakingapp.R;
import com.example.android.bakingapp.data.Ingredient;
import com.example.android.bakingapp.data.Recipe;
import com.example.android.bakingapp.data.Step;
import com.example.android.bakingapp.fragment.StepDetailFragment;

import org.parceler.Parcels;

import java.util.List;

/**
 * An activity representing a list of Steps. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link StepDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class StepListActivity extends AppCompatActivity {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;

    // Recipe Intent Extra param
    public static final String RECIPE_EXTRA_PARAM = "recipe";

    // Recipe
    private Recipe recipe;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_step_list);

        // Keep recipe
        this.recipe = Parcels.unwrap(getIntent().getParcelableExtra(RECIPE_EXTRA_PARAM));

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        if (findViewById(R.id.step_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;
        }

        View recyclerView = findViewById(R.id.step_list);
        assert recyclerView != null;
        setupRecyclerView((RecyclerView) recyclerView);
    }

    private void setupRecyclerView(@NonNull RecyclerView recyclerView) {
        recyclerView.setAdapter(new SimpleItemRecyclerViewAdapter(this, recipe, mTwoPane));
    }

    public static class SimpleItemRecyclerViewAdapter
            extends RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder> {

        private final StepListActivity mParentActivity;
        private final Recipe mRecipe;
        private final boolean mTwoPane;
        private final View.OnClickListener mOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Step step = null;
                List<Ingredient> ingredients = null;

                if (view.getTag() instanceof Step) {
                    step = (Step) view.getTag();
                } else {
                    ingredients = (List<Ingredient>) view.getTag();
                }

                if (mTwoPane) {
                    Bundle arguments = new Bundle();

                    if (step != null) {
                        arguments.putParcelable(StepDetailFragment.ARG_STEP, Parcels.wrap(step));
                    } else {
                        arguments.putParcelable(StepDetailFragment.ARG_INGREDIENTS,
                                Parcels.wrap(ingredients));
                    }

                    StepDetailFragment fragment = new StepDetailFragment();
                    fragment.setArguments(arguments);
                    mParentActivity.getSupportFragmentManager().beginTransaction()
                            .replace(R.id.step_detail_container, fragment)
                            .commit();
                } else {
                    Context context = view.getContext();
                    Intent intent = new Intent(context, StepDetailActivity.class);

                    if (step != null) {
                        intent.putExtra(StepDetailFragment.ARG_STEP, Parcels.wrap(step));
                    } else {
                        intent.putExtra(StepDetailFragment.ARG_INGREDIENTS,
                                Parcels.wrap(ingredients));
                    }

                    context.startActivity(intent);
                }
            }
        };

        SimpleItemRecyclerViewAdapter(StepListActivity parent,
                                      Recipe recipe,
                                      boolean twoPane) {
            mRecipe = recipe;
            mParentActivity = parent;
            mTwoPane = twoPane;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.step_list_content, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {

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
                text = mParentActivity.getString(R.string.ingredients_label);
                holder.itemView.setTag(mRecipe.getIngredients());
            } else {
                // Step page
                Step step = mRecipe.getSteps().get(stepPosition);
                text = step.getShortDescription();
                holder.itemView.setTag(mRecipe.getSteps().get(stepPosition));
            }

            holder.mIdView.setText(String.valueOf(position));
            holder.mContentView.setText(text);

//            holder.itemView.setTag(mRecipe.getSteps().get(stepPosition));
            holder.itemView.setOnClickListener(mOnClickListener);
        }

        @Override
        public int getItemCount() {
            return (hasIngredients() ? 1 : 0) + (hasSteps() ? mRecipe.getSteps().size() : 0);
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            final TextView mIdView;
            final TextView mContentView;

            ViewHolder(View view) {
                super(view);
                mIdView = (TextView) view.findViewById(R.id.id_text);
                mContentView = (TextView) view.findViewById(R.id.content);
            }
        }


        private boolean hasIngredients() {
            return mRecipe.getIngredients() != null && mRecipe.getIngredients().size() > 0;
        }

        private boolean hasSteps() {
            return mRecipe.getSteps() != null && mRecipe.getSteps().size() > 0;
        }
    }
}
