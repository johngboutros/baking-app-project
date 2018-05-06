package com.example.android.bakingapp.data;

import java.util.List;

/**
 * Created by john on 06/05/18.
 */

public class Recipe {

    long id;
    String name;
    List<Ingredient> ingredients;
    List<Step> steps;
    int servings;
    String image;
}
