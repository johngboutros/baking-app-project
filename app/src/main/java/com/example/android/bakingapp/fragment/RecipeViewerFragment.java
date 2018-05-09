package com.example.android.bakingapp.fragment;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.android.bakingapp.R;
import com.example.android.bakingapp.adapter.RecipeStepsAdapter;
import com.example.android.bakingapp.adapter.RecipeStepsPagerAdapter;
import com.example.android.bakingapp.data.Recipe;

import org.parceler.Parcels;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link RecipeViewerFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link RecipeViewerFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RecipeViewerFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String RECIPE_PARAM = "recipe";

//    @BindView(R.id.recipe_steps_rv)
//    RecyclerView recyclerView;

    @BindView(R.id.recipe_steps_vp)
    ViewPager viewPager;

    // TODO: Rename and change types of parameters
    private Recipe recipe;

    private OnFragmentInteractionListener mListener;

    public RecipeViewerFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param recipe Parameter 1.
     * @return A new instance of fragment RecipeViewerFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static RecipeViewerFragment newInstance(Recipe recipe) {
        RecipeViewerFragment fragment = new RecipeViewerFragment();
        Bundle args = new Bundle();
        args.putParcelable(RECIPE_PARAM, Parcels.wrap(recipe));
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            recipe = Parcels.unwrap(getArguments().getParcelable(RECIPE_PARAM));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater
                .inflate(R.layout.fragment_recipe_viewer, container, false);
        ButterKnife.bind(this, rootView);

//        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(),
//                LinearLayoutManager.HORIZONTAL, false);
//
//        recyclerView.setLayoutManager(layoutManager);
//        recyclerView.setHasFixedSize(true);
//
//        RecipeStepsAdapter stepsAdapter = new RecipeStepsAdapter(getContext());
//        recyclerView.setAdapter(stepsAdapter);
//
//        stepsAdapter.setIngredients(recipe.getIngredients());
//        stepsAdapter.setSteps(recipe.getSteps());

        viewPager.setAdapter(new RecipeStepsPagerAdapter(getContext(), recipe));

        return rootView;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
//        if (context instanceof OnFragmentInteractionListener) {
//            mListener = (OnFragmentInteractionListener) context;
//        } else {
//            throw new RuntimeException(context.toString()
//                    + " must implement OnFragmentInteractionListener");
//        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
