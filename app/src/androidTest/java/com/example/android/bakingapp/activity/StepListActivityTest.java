package com.example.android.bakingapp.activity;

import android.content.Context;
import android.content.Intent;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.Espresso;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.mock.MockContext;

import com.example.android.bakingapp.R;
import com.example.android.bakingapp.data.Recipe;
import com.example.android.bakingapp.utils.TestUtils;
import com.google.gson.Gson;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.parceler.Parcels;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition;
import static android.support.test.espresso.matcher.ViewMatchers.hasDescendant;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.example.android.bakingapp.utils.TestUtils.RecyclerViewMatcher.atPosition;

/**
 * Created by john on 13/05/18.
 */

@RunWith(AndroidJUnit4.class)
public class StepListActivityTest {

    private final static String JSON_FILENAME = "baking.json";
    private final static int TEST_RECIPE_INDEX = 3;

    @Rule
    // third parameter is set to false which means the activity is not started automatically
    public ActivityTestRule<StepListActivity> mActivityTestRule =
            new ActivityTestRule<>(StepListActivity.class, false, false);

    /**
     * Reads json test data from assets and returns it.
     *
     * @return test data
     */
    private Recipe[] getTestRecipes() {
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
        i.putExtra(StepListActivity.RECIPE_EXTRA_PARAM,
                Parcels.wrap(getTestRecipes()[TEST_RECIPE_INDEX]));
        mActivityTestRule.launchActivity(i);
    }

    @Test
    public void clickIngredientsTest() {

        // Click on the first recipe
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

        // Click on the first recipe
        onView(withId(R.id.fab)).perform(click());

        // Check that the first step is ingredients
        onView(withId(R.id.ingredients_container_ll)).check(matches(isDisplayed()));

    }

//
//    @Test
//    public void clickRecipeThenBack_displayRecipes() {
//
//        // Click on the first recipe
//        onView(withId(R.id.recipe_list_rv))
//                .perform(actionOnItemAtPosition(0, click()));
//
//        Espresso.pressBack();
//
//        // Check that the steps list is displayed
//        onView(withId(R.id.recipe_list_rv)).check(matches(isDisplayed()));
//    }
}
