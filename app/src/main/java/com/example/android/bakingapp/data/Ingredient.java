package com.example.android.bakingapp.data;

import org.parceler.Parcel;

/**
 * Created by john on 06/05/18.
 */

@Parcel
public class Ingredient {

    float quantity;
    String measure;
    String ingredient;

    public float getQuantity() {
        return quantity;
    }

    public void setQuantity(float quantity) {
        this.quantity = quantity;
    }

    public String getMeasure() {
        return measure;
    }

    public void setMeasure(String measure) {
        this.measure = measure;
    }

    public String getIngredient() {
        return ingredient;
    }

    public void setIngredient(String ingredient) {
        this.ingredient = ingredient;
    }
}
