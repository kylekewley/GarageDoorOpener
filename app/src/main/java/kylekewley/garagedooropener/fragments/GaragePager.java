package kylekewley.garagedooropener.fragments;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;

import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import kylekewley.garagedooropener.MainActivity;
import kylekewley.garagedooropener.R;
import kylekewley.garagedooropener.PagerAdapter;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link GaragePager#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class GaragePager extends Fragment implements
        ViewPager.OnPageChangeListener {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_NUM_DOORS = "num_doors";


    public static final String GARAGE_PAGER_TAG = "garage_pager";

    /**
     * The total number of garage doors to display
     */
    private volatile int numDoors;

    /**
     * The currentDoor being viewed
     */
    private int currentDoor;

    /**
     * The pager widget, which handles animation and allows swiping horizontally to access previous
     * and next wizard steps.
     */
    private ViewPager mPager;

    /**
     * The pager adapter, which provides the pages to the view pager widget.
     */
    private PagerAdapter mPagerAdapter;

    /**
     * Is set to true the first time setNumDoors() is called.
     */
    private boolean initialized;


    public static GaragePager newInstance(int numDoors) {
        GaragePager fragment = new GaragePager();

        Bundle args = new Bundle();
        args.putInt(ARG_NUM_DOORS, numDoors);

        fragment.setArguments(args);
        return fragment;
    }


    public GaragePager() {
        // Required empty public constructor
    }


    /**
     * Sets the number of doors and updates the view
     *
     * @param numDoors  The number of garage doors to display.
     */
    public void setNumDoors(int numDoors) {
        this.numDoors = numDoors;

        mPagerAdapter.setDoorCount(numDoors);

        if (!initialized) {
            mPager.setCurrentItem(currentDoor);
            initialized = true;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        currentDoor = PreferenceManager.getDefaultSharedPreferences(getActivity()
                .getApplicationContext()).getInt(getString(R.string.pref_selected_door), 0);

        if (getArguments() != null) {
            numDoors = getArguments().getInt(ARG_NUM_DOORS);
        }
        if (savedInstanceState != null) {
            numDoors = savedInstanceState.getInt(ARG_NUM_DOORS);
        }

    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_garage_pager, container, false);

        if (v == null) return v;

        // Instantiate a ViewPager and a PagerAdapter.
        mPager = (ViewPager)v.findViewById(R.id.view_pager);

        if (mPager == null) return v;

        mPagerAdapter = new PagerAdapter(getChildFragmentManager(), numDoors);
        mPager.setAdapter(mPagerAdapter);

        mPager.setOnPageChangeListener(this);

        //Set the current door if it is valid. This is used for when the activity is recreated after
        //switching from the history tab back to the opener tab.
        if (currentDoor < numDoors)
            mPager.setCurrentItem(currentDoor, false);

        mPager.setClipChildren(false);
        mPager.setPageMargin(getResources().getDimensionPixelOffset(R.dimen.viewpager_margin));
        mPager.setOffscreenPageLimit(4);



        return v;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(ARG_NUM_DOORS, numDoors);
    }

    @Override
    public void onPageScrolled(int i, float v, int i2) {

    }

    @Override
    public void onPageSelected(int i) {
        currentDoor = i;
        ((MainActivity)getActivity()).onSectionAttached(getString(R.string.title_garage_opener) + " " + (currentDoor+1));
        PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext()).edit().
                putInt(getString(R.string.pref_selected_door), i).commit();
    }

    @Override
    public void onPageScrollStateChanged(int i) {

    }
}
