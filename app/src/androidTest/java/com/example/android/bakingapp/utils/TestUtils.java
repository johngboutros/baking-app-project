package com.example.android.bakingapp.utils;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.test.espresso.matcher.BoundedMatcher;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

import static android.support.test.internal.util.Checks.checkNotNull;

/**
 * Created by john on 14/05/18.
 */

public class TestUtils {

    public static class RecyclerViewMatcher {

        public static Matcher<View> atPosition(final int position, @NonNull final Matcher<View> itemMatcher) {
            checkNotNull(itemMatcher);
            return new BoundedMatcher<View, RecyclerView>(RecyclerView.class) {
                @Override
                public void describeTo(Description description) {
                    description.appendText("has item at position " + position + ": ");
                    itemMatcher.describeTo(description);
                }

                @Override
                protected boolean matchesSafely(final RecyclerView view) {
                    RecyclerView.ViewHolder viewHolder = view.findViewHolderForAdapterPosition(position);
                    if (viewHolder == null) {
                        // has no item on such position
                        return false;
                    }
                    return itemMatcher.matches(viewHolder.itemView);
                }
            };
        }
    }

    public static String loadJSONFromAsset(Context context, String jsonFilename) {
        String json = null;
        try {
            InputStream is = context.getAssets().open(jsonFilename);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }

    /**
     *
     * Usage:
     *
        try {
            JSONObject obj = new JSONObject(loadJSONFromAsset());
            JSONArray m_jArry = obj.getJSONArray("formules");
            ArrayList<HashMap<String, String>> formList = new ArrayList<HashMap<String, String>>();
            HashMap<String, String> m_li;

            for (int i = 0; i < m_jArry.length(); i++) {
                JSONObject jo_inside = m_jArry.getJSONObject(i);
                Log.d("Details-->", jo_inside.getString("formule"));
                String formula_value = jo_inside.getString("formule");
                String url_value = jo_inside.getString("url");

                //Add your values in your `ArrayList` as below:
                m_li = new HashMap<String, String>();
                m_li.put("formule", formula_value);
                m_li.put("url", url_value);

                formList.add(m_li);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
     */
}
