package kylekewley.garagedooropener.fragments;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import kylekewley.garagedooropener.GarageOpenerClient;
import kylekewley.garagedooropener.GarageOpenerView;
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
        ViewPager.OnPageChangeListener,
        GarageOpenerView {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_NUM_DOORS = "num_doors";


    private static final String GARAGE_PAGER_TAG = "garage_pager";

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
     * The class that will handle data updates
     */
    private GarageOpenerClient garageOpenerClient;

    /**
     * The pager adapter, which provides the pages to the view pager widget.
     */
    private PagerAdapter mPagerAdapter;

    /**
     * Is set to true the first time setNumDoors() is called.
     */
    private boolean initialized = false;


    @NotNull
    public static GaragePager newInstance() {
        GaragePager fragment = new GaragePager();

        return fragment;
    }


    public GaragePager() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        currentDoor = PreferenceManager.getDefaultSharedPreferences(getActivity()
                .getApplicationContext()).getInt(getString(R.string.pref_selected_door), 0);

    }



    @Nullable
    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_garage_pager, container, false);

        if (v == null) return null;

        mPager = (ViewPager)v.findViewById(R.id.view_pager);

        if (mPager == null) return v;

        mPagerAdapter = new PagerAdapter(getChildFragmentManager());
        garageOpenerClient = ((MainActivity)getActivity()).getDataFragment().getGarageOpenerClient();
        garageOpenerClient.setViewPager(mPager);
        garageOpenerClient.setOpenerView(this);

        mPager.setAdapter(mPagerAdapter);
        mPager.setOnPageChangeListener(this);


        //Set the current door if it is valid. This is used for when the activity is recreated after
        //switching from the history tab back to the opener tab.
        if (currentDoor < garageOpenerClient.getNumberOfGarageDoors())
            mPager.setCurrentItem(currentDoor, false);

        mPager.setClipChildren(false);
        mPager.setPageMargin(getResources().getDimensionPixelOffset(R.dimen.viewpager_margin));
        mPager.setOffscreenPageLimit(4);



        return v;
    }


    @Override
    public void onStart() {
        super.onStart();
        updateActionBarTitle();
        if (garageOpenerClient == null)
            Log.d(GARAGE_PAGER_TAG, "Garage opener client null");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        garageOpenerClient.setOpenerView(null);

    }

    @Override
    public void onSaveInstanceState(@NotNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }


    private void updateActionBarTitle() {
        ((MainActivity)getActivity()).onSectionAttached(getString(R.string.title_garage_opener) + " " + (currentDoor+1));

    }

    @Override
    public void onPageScrolled(int i, float v, int i2) {

    }

    @Override
    public void onPageSelected(int i) {
        currentDoor = i;
        PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext()).edit().
                putInt(getString(R.string.pref_selected_door), i).commit();

        updateActionBarTitle();
    }

    @Override
    public void onPageScrollStateChanged(int i) {

    }

//    public void updateGarageView(final int index, final GarageOpenerClient.DoorPosition status) {
//        //TODO: Implement method when we get garage pictures.
//
//        getActivity().runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                //TODO: Make sure we're getting the right fragment
//                if (getChildFragmentManager() == null ||
//                        getChildFragmentManager().getFragments() == null) return;
//
//                GarageOpenerFragment f = (GarageOpenerFragment)getChildFragmentManager()
//                        .getFragments().get(index);
//
//                if (f != null) {
//                    TextView textView = f.getTextView();
//
//                    if (textView != null) {
//
//                        String text;
//
//                        if (status == GarageOpenerClient.DoorPosition.DOOR_CLOSED) text = "Closed";
//                        else if (status == GarageOpenerClient.DoorPosition.DOOR_MOVING) text = "Moving";
//                        else text = "Open";
//
//                        textView.setText(text);
//                    }
//                }
//            }
//        });
//    }

    public class PagerAdapter extends FragmentStatePagerAdapter {

        public PagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @NotNull
        @Override
        public Fragment getItem(int position) {
            //boolean closed = garageOpenerClient.getDoorStatusAtIndex(position) == GarageOpenerClient.DoorPosition.DOOR_CLOSED;
            return GarageOpenerFragment.newInstance(garageOpenerClient.getDoorStatusAtIndex(position) == GarageOpenerClient.DoorPosition.DOOR_CLOSED ? 1 : 0);
        }

        @Override
        public int getCount() {
            return garageOpenerClient.getNumberOfGarageDoors();
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

        @Override
        public void notifyDataSetChanged() {
            super.notifyDataSetChanged();

            if (!initialized) {
                mPager.setCurrentItem(currentDoor, false);
            }
        }
    }

}
