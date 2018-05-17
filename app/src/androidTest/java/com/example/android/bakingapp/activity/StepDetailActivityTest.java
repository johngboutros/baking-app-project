package com.example.android.bakingapp.activity;

import android.content.Intent;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.example.android.bakingapp.R;
import com.example.android.bakingapp.data.Recipe;
import com.example.android.bakingapp.fragment.StepDetailFragment;
import com.example.android.bakingapp.utils.TestUtils;
import com.google.gson.Gson;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.parceler.Parcels;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

/**
 * Created by john on 13/05/18.
 */

@RunWith(AndroidJUnit4.class)
public class StepDetailActivityTest {

    private final static String JSON_FILENAME = "baking.json";
    private final static int TEST_RECIPE_INDEX = 3;
    private final static Recipe testRecipe = getTestRecipes()[TEST_RECIPE_INDEX];

    @Rule
    // third parameter is set to false which means the activity is not started automatically
    public IntentsTestRule<StepDetailActivity> mActivityRule = new IntentsTestRule<>(
            StepDetailActivity.class, false, false);

    /**
     * Reads json test data from assets and returns it.
     *
     * @return test data
     */
    private static Recipe[] getTestRecipes() {
        String json = TestUtils.loadJSONFromAsset(InstrumentationRegistry.getContext(), JSON_FILENAME);
        Gson gson = new Gson();
        return gson.fromJson(json, Recipe[].class);
    }

    /**
     * Launch activity with intent
     */
    @Before
    public void launchActivity() {
        Intent i = new Intent();
        i.putExtra(StepDetailActivity.ARG_NEXT_STEPS, Parcels.wrap(testRecipe.getSteps()));
        i.putExtra(StepDetailFragment.ARG_INGREDIENTS, Parcels.wrap(testRecipe.getIngredients()));
        mActivityRule.launchActivity(i);
    }

    @Test
    public void checkIngredientsDisplayedFirst() {
        // Check that the steps list is displayed
        onView(withId(R.id.ingredients_container_ll)).check(matches(isDisplayed()));
    }

//    @Test
//    public void checkStepNavigation() {
//
//        List<Step> steps = testRecipe.getSteps();
//
//        for (Step step : steps) {
//            // Click on next button
//            onView(withId(R.id.fab)).perform(click());
//            // Check step is displayed correctly
////            onView(allOf(instanceOf(TextView.class), withParent(withId(R.id.toolbar))))
////                    .check(matches(withText(step.getShortDescription())));
//            onView(withId(R.id.step_header_tv)).check(matches(withText(step.getShortDescription())));
//            onView(withId(R.id.step_content_tv)).check(matches(withText(step.getDescription())));
//        }
//
//        // Finally click up to go back to step list
//        onView(withId(R.id.fab)).perform(click());
//        // FIXME Check for intend to StepListActivitty
//        intended(isInternal());
////        intended(allOf(hasComponent(hasClassName(StepListActivity.class.getName()))));
////        intended(toPackage(StepListActivity.class.getPackage().getName()));
////        intended(hasComponent(StepListActivity.class));
//
//        // Check that the steps list is displayed
////        onView(withId(R.id.step_list)).check(matches(isDisplayed()));
//
//
//    }
}
