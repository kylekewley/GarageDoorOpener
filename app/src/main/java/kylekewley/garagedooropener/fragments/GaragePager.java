package kylekewley.garagedooropener.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
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
        ViewPager.OnPageChangeListener{
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_NUM_DOORS = "num_doors";
    private static final String ARG_CURRENT_DOOR = "current_door";

    private static final String GARAGE_PAGER_TAG = "garage_pager";

    /**
     * The total number of garage doors to display
     */
    private int numDoors;

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



    public static GaragePager newInstance(int numDoors, int currentDoor) {
        GaragePager fragment = new GaragePager();
        fragment.numDoors = numDoors;
        fragment.currentDoor = currentDoor;

        Bundle args = new Bundle();
        fragment.storeProperties(args);

        fragment.setArguments(args);
        return fragment;
    }

    public static String tag() {
        return GARAGE_PAGER_TAG;
    }

    public GaragePager() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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
            mPager.setOffscreenPageLimit(numDoors-1);
            mPagerAdapter = new ScreenSlidePagerAdapter(getFragmentManager());
            mPager.setAdapter(mPagerAdapter);

        }

        return v;
    }

    private void storeProperties(Bundle bundle) {

        bundle.putInt(ARG_NUM_DOORS, numDoors);
        bundle.putInt(ARG_CURRENT_DOOR, currentDoor);

    }

    private void restoreProperties(Bundle bundle) {
        numDoors = bundle.getInt(ARG_NUM_DOORS);
        currentDoor = bundle.getInt(ARG_CURRENT_DOOR);
    }

    @Override
    public void onStart() {
        super.onStart();
        mPager.setCurrentItem(currentDoor);

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        storeProperties(outState);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        ((MainActivity)activity).onSectionAttached(getString(R.string.title_garage_opener) + " " + (currentDoor+1));
    }

    @Override
    public void onPageScrolled(int i, float v, int i2) {

    }

    @Override
    public void onPageSelected(int i) {
        currentDoor = i;
        ((MainActivity)getActivity()).onSectionAttached(getString(R.string.title_garage_opener) + " " + (currentDoor+1));
    }

    @Override
    public void onPageScrollStateChanged(int i) {

    }

    /**
     * A simple pager adapter that represents 5 ScreenSlidePageFragment objects, in
     * sequence.
     */
    private class ScreenSlidePagerAdapter extends FragmentPagerAdapter {
        public ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);

        }


        @Override
        public Fragment getItem(int position) {
            Log.d("Tag", "NEW fragment");
            return GarageOpenerFragment.newInstance(position+1);
        }



        @Override
        public int getCount() {
            return numDoors;
        }
    }
}
