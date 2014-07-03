package kylekewley.garagedooropener.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import kylekewley.garagedooropener.MainActivity;
import kylekewley.garagedooropener.R;

/**
 * Created by Kyle Kewley on 7/1/14.
 */
public class GarageOpenerOverviewFragment extends Fragment {


    public static GarageOpenerOverviewFragment newInstance() {
        return new GarageOpenerOverviewFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        inflater.inflate(R.layout.fragment_garage_overview, container, false);

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((MainActivity)activity).onSectionAttached(getString(R.string.title_garage_overview));
    }
}
