package kylekewley.garagedooropener.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
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
public class GaragePager extends Fragment implements
        ViewPager.OnPageChangeListener {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_NUM_DOORS = "num_doors";
    private static final String ARG_CURRENT_DOOR = "current_door";


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
    private boolean initialized = false;


    public static GaragePager newInstance(int numDoors) {
        GaragePager fragment = new GaragePager();
        fragment.numDoors = numDoors;

        Bundle args = new Bundle();
        fragment.storeProperties(args);

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

        mPagerAdapter.notifyDataSetChanged();

        if (!initialized) {
            mPager.setCurrentItem(currentDoor);
            onPageSelected(currentDoor);

        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        currentDoor = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext()).
                getInt(getString(R.string.pref_selected_door), 0);

        Log.d(GARAGE_PAGER_TAG, "Creating");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_garage_pager, container, false);

        if (savedInstanceState != null) {
            restoreProperties(savedInstanceState);
        }

        if (v != null) {
            // Instantiate a ViewPager and a PagerAdapter.
            mPager = (ViewPager)v.findViewById(R.id.view_pager);

            mPager.setOnPageChangeListener(this);
            mPager.setClipChildren(false);
            mPager.setPageMargin(getResources().getDimensionPixelOffset(R.dimen.viewpager_margin));
            mPager.setOffscreenPageLimit(4);


            mPagerAdapter = new ScreenSlidePagerAdapter(getFragmentManager());
            mPager.setAdapter(mPagerAdapter);


        }

        return v;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mPager = null;
        mPagerAdapter = null;

        Log.d(GARAGE_PAGER_TAG, "Destroying");
    }

    private void storeProperties(Bundle bundle) {

        bundle.putInt(ARG_NUM_DOORS, numDoors);
        bundle.putInt(ARG_CURRENT_DOOR, currentDoor);

    }

    private void restoreProperties(Bundle bundle) {
        numDoors = bundle.getInt(ARG_NUM_DOORS);
    }

    @Override
    public void onStart() {
        super.onStart();

        if (initialized) {
            mPager.setCurrentItem(currentDoor);
            ((MainActivity)getActivity()).onSectionAttached(getString(R.string.title_garage_opener) + " " + (currentDoor+1));
        }else {
            ((MainActivity)getActivity()).onSectionAttached(getString(R.string.title_garage_overview));
        }

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        storeProperties(outState);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        ((MainActivity)activity).onSectionAttached(getString(R.string.title_garage_overview));
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


    /**
     * A simple pager adapter that represents 5 ScreenSlidePageFragment objects, in
     * sequence.
     */
    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
        public ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);

        }


        @Override
        public Fragment getItem(int position) {
            return GarageOpenerFragment.newInstance(position+1);
        }


        @Override
        public int getCount() {
            return numDoors;
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }
    }
}
