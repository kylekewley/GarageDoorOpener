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

    /**
     * The object used to send server requests
     */
    private GarageOpenerClient garageOpenerClient;


    @NotNull
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


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        currentDoor = PreferenceManager.getDefaultSharedPreferences(getActivity()
                .getApplicationContext()).getInt(getString(R.string.pref_selected_door), 0);

        if (getArguments() != null && !initialized) {
            numDoors = getArguments().getInt(ARG_NUM_DOORS);
        }
        if (savedInstanceState != null) {
            numDoors = savedInstanceState.getInt(ARG_NUM_DOORS);
        }
    }



    @Nullable
    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        garageOpenerClient = ((MainActivity)getActivity()).getBackgroundFragment().getGarageOpenerClient();

        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_garage_pager, container, false);

        if (v == null) return null;

        // Instantiate a ViewPager and a PagerAdapter.
        mPager = (ViewPager)v.findViewById(R.id.view_pager);

        if (mPager == null) return v;

        mPagerAdapter = new PagerAdapter(getChildFragmentManager());

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
    public void onStart() {
        super.onStart();
        updateActionBarTitle();
        if (garageOpenerClient == null)
            Log.d(GARAGE_PAGER_TAG, "Garage opener client null");
    }

    @Override
    public void onSaveInstanceState(@NotNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(ARG_NUM_DOORS, numDoors);
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

    @Override
    public void setGarageOpenerClient(GarageOpenerClient openerClient) {
        this.garageOpenerClient = openerClient;
    }

    @Override
    public void updateGarageView(final int index, final GarageOpenerClient.DoorPosition status) {
        //TODO: Implement method when we get garage pictures.

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //TODO: Make sure we're getting the right fragment
                if (getChildFragmentManager() == null ||
                        getChildFragmentManager().getFragments() == null) return;

                GarageOpenerFragment f = (GarageOpenerFragment)getChildFragmentManager()
                        .getFragments().get(index);

                if (f != null) {
                    TextView textView = f.getTextView();

                    if (textView != null) {

                        String text;

                        if (status == GarageOpenerClient.DoorPosition.DOOR_CLOSED) text = "Closed";
                        else if (status == GarageOpenerClient.DoorPosition.DOOR_MOVING) text = "Moving";
                        else text = "Open";

                        textView.setText(text);
                    }
                }
            }
        });
    }

    private void setNumDoors(int numDoors) {
        this.numDoors = numDoors;
    }

    @Override
    public void setGarageDoorCount(final int garageDoorCount) {
        if (getActivity() == null) {
            //Not yet attached. We will be updated then.
            return;
        }

        //Don't bother updating to the same value
        if (numDoors == garageDoorCount) return;


        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setNumDoors(garageDoorCount);

                if (mPagerAdapter != null) {
                    mPagerAdapter.notifyDataSetChanged();

                    if (!initialized) {
                        mPager.setCurrentItem(currentDoor);
                    }
                }

                initialized = true;
            }
        });
    }



    public class PagerAdapter extends FragmentStatePagerAdapter {

        public PagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @NotNull
        @Override
        public Fragment getItem(int position) {
            boolean closed = garageOpenerClient.getDoorStatusAtIndex(position) == GarageOpenerClient.DoorPosition.DOOR_CLOSED;
            return GarageOpenerFragment.newInstance(closed ? 1 : 0);
        }

        @Override
        public int getCount() {
            if (garageOpenerClient == null) return 0;
            return numDoors;
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }
    }

}
