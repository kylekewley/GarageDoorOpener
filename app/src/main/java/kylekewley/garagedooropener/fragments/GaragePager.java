package kylekewley.garagedooropener.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import kylekewley.garagedooropener.MainActivity;
import kylekewley.garagedooropener.R;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link GaragePager#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class GaragePager extends Fragment {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_NUM_DOORS = "num_doors";
    private static final String ARG_CURRENT_DOOR = "current_door";

    private int numDoors;
    private int currentDoor;


    public static GaragePager newInstance(int numDoors, int currentDoor) {
        GaragePager fragment = new GaragePager();
        fragment.numDoors = numDoors;
        fragment.currentDoor = currentDoor;
        Bundle args = new Bundle();
        args.putInt(ARG_NUM_DOORS, numDoors);
        args.putInt(ARG_CURRENT_DOOR, currentDoor);
        fragment.setArguments(args);
        return fragment;
    }
    public GaragePager() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            numDoors = getArguments().getInt(ARG_NUM_DOORS);
            currentDoor = getArguments().getInt(ARG_CURRENT_DOOR);
            Log.d("Pager", Integer.toString(currentDoor));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_garage_pager, container, false);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        ((MainActivity)activity).onSectionAttached(getString(R.string.title_garage_opener) + " " + currentDoor);
    }
}
