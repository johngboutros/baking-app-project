package com.example.android.bakingapp.activity;

import android.content.Intent;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.example.android.bakingapp.R;
import com.example.android.bakingapp.data.Recipe;
import com.example.android.bakingapp.data.Step;
import com.example.android.bakingapp.utilities.RecipesUtils;
import com.example.android.bakingapp.utils.TestUtils;
import com.google.gson.Gson;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.parceler.Parcels;

import java.util.List;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.example.android.bakingapp.utils.TestUtils.isScreenSw600dp;
import static org.junit.Assume.assumeTrue;

/**
 * {@link StepListActivity} test.
 *
 * Created by john on 13/05/18.
 */

@RunWith(AndroidJUnit4.class)
public class StepListActivityTest {

    private final static String JSON_FILENAME = "baking.json";
    private final static int TEST_RECIPE_INDEX = 3;
    private final static Recipe testRecipe = getTestRecipes()[TEST_RECIPE_INDEX];

    @Rule
    // third parameter is set to false which means the activity is not started automatically
    public final ActivityTestRule<StepListActivity> mActivityRule =
            new ActivityTestRule<>(StepListActivity.class, false, false);

    /**
     * Reads json test data from assets and returns it.
     *
     * @return test data
     */
    private static Recipe[] getTestRecipes() {
        String json = TestUtils.loadJSONFromUrl(InstrumentationRegistry.getContext(),
                RecipesUtils.RECIPES_URL);
        Gson gson = new Gson();
        return gson.fromJson(json, Recipe[].class);
    }

    /**
     * Launch activity with intent
     */
    @Before
    public void launchActivity() {
        Intent i = new Intent();
        i.putExtra(StepListActivity.RECIPE_EXTRA_PARAM, Parcels.wrap(testRecipe));
        mActivityRule.launchActivity(i);
    }

    @Test
    public void clickIngredientsTest() {

        // Click on the first item (ingredients)
        onView(withId(R.id.step_list))
                .perform(actionOnItemAtPosition(0, click()));

        // Check that the steps list is displayed
        onView(withId(R.id.ingredients_container_ll)).check(matches(isDisplayed()));
    }

    @Test
    public void clickStepTest() {

        // Click on the first recipe
        onView(withId(R.id.step_list))
                .perform(actionOnItemAtPosition(1, click()));

        // Check that the steps list is displayed
        onView(withId(R.id.step_content_tv)).check(matches(isDisplayed()));
    }

    @Test
    public void clickNext_displayIngredients() {
        // Test only for phones
        assumeTrue(!isScreenSw600dp(mActivityRule.getActivity()));

        // Click the next button
        onView(withId(R.id.fab)).perform(click());

        // Check that the first step is ingredients
        onView(withId(R.id.ingredients_container_ll)).check(matches(isDisplayed()));

    }


    @Test
    public void checkStepNavigation() {

        // Click on the first item (ingredients)
        onView(withId(R.id.step_list))
                .perform(actionOnItemAtPosition(0, click()));

        List<Step> steps = testRecipe.getSteps();

        for (Step step : steps) {
            // Click on next button
            onView(withId(R.id.fab)).perform(click());
            // Check step is displayed correctly
            onView(withId(R.id.step_content_tv)).check(matches(withText(step.getDescription())));
        }

        // Finally click up to go back to step list
        onView(withId(R.id.fab)).perform(click());
        // Check that the steps list is displayed
        onView(withId(R.id.step_list)).check(matches(isDisplayed()));


    }
}
