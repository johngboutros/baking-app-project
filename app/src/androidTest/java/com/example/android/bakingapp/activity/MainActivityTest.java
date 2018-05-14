package com.example.android.bakingapp.activity;

import android.support.test.espresso.Espresso;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.example.android.bakingapp.R;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

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
public class MainActivityTest {

    @Rule
    public ActivityTestRule<MainActivity> mActivityTestRule = new ActivityTestRule<>(MainActivity.class);

    // Registers any resource that needs to be synchronized with Espresso before
    // the test is run.
    @Before
    public void registerIdlingResource() {
        Espresso.registerIdlingResources(mActivityTestRule.getActivity().getIdlingResource());
    }

    @Test
    public void clickRecipe_displaySteps() {

        // Click on the first recipe
        onView(withId(R.id.recipe_list_rv))
                .perform(actionOnItemAtPosition(0, click()));

        // Check that the steps list is displayed
        onView(withId(R.id.step_list)).check(matches(isDisplayed()));
    }

    @Test
    public void clickRecipe_displayIngredientsFirst() {

        // Click on the first recipe
        onView(withId(R.id.recipe_list_rv))
                .perform(actionOnItemAtPosition(0, click()));

        // Check that the first step is ingredients
        onView(withId(R.id.step_list))
                .check(matches(atPosition(0, hasDescendant(withText("Ingredients")))));
    }


    @Test
    public void clickRecipeThenBack_displayRecipes() {

        // Click on the first recipe
        onView(withId(R.id.recipe_list_rv))
                .perform(actionOnItemAtPosition(0, click()));

        Espresso.pressBack();

        // Check that the steps list is displayed
        onView(withId(R.id.recipe_list_rv)).check(matches(isDisplayed()));
    }
}
