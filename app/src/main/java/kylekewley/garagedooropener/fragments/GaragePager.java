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
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.font.TextAttribute;
import java.util.Date;

import kylekewley.garagedooropener.Constants;
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

    /**
     * The relative layout for the loading view
     */
    private RelativeLayout loadingLayout;

    /**
     * The textView for displaying the no result string
     */
    private TextView noResultTextView;

    /**
     * The loading indicator
     */
    private ProgressBar progressBar;


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

        View view = inflater.inflate(R.layout.fragment_garage_pager, container, false);

        if (view == null) return null;

        mPager = (ViewPager)view.findViewById(R.id.view_pager);

        if (mPager == null) return view;

        loadingLayout = (RelativeLayout)view.findViewById(R.id.loading_parent);
        noResultTextView = (TextView)view.findViewById(R.id.loaded_text);
        progressBar = (ProgressBar)view.findViewById(R.id.opener_progress_bar);

        mPagerAdapter = new PagerAdapter(getChildFragmentManager());
        garageOpenerClient = ((MainActivity)getActivity()).getDataFragment().getGarageOpenerClient();
        garageOpenerClient.setViewPager(mPager);
        garageOpenerClient.setOpenerView(this);

        mPager.setAdapter(mPagerAdapter);
        mPager.setOnPageChangeListener(this);

        loadingStatusChangedHelper(garageOpenerClient.isLoading());


        //Set the current door if it is valid. This is used for when the activity is recreated after
        //switching from the history tab back to the opener tab.
        if (currentDoor < garageOpenerClient.getNumberOfGarageDoors())
            mPager.setCurrentItem(currentDoor, false);

        mPager.setClipChildren(false);
//        mPager.setPageMargin(getResources().getDimensionPixelOffset(R.dimen.viewpager_margin));
        mPager.setOffscreenPageLimit(4);



        return view;
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
        ((MainActivity)getActivity()).onSectionAttached(getString(R.string.title_garage_overview));

//        if (garageOpenerClient.getNumberOfGarageDoors() == 0) {
//        }else {
//            ((MainActivity)getActivity()).onSectionAttached(getString(R.string.title_garage_opener) + " " + (currentDoor+1));
//
//        }

    }

    @Override
    public void onPageScrolled(int i, float v, int i2) {

    }

    @Override
    public void onPageSelected(int i) {
        currentDoor = i;
        PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext()).edit().
                putInt(getString(R.string.pref_selected_door), i).commit();

//        updateActionBarTitle();
    }

    @Override
    public void onPageScrollStateChanged(int i) {

    }


    public class PagerAdapter extends FragmentStatePagerAdapter {

        public PagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @NotNull
        @Override
        public Fragment getItem(int position) {
            GarageOpenerClient.DoorPosition doorPosition = garageOpenerClient.getDoorStatusAtIndex(position);
            GarageOpenerFragment fragment = GarageOpenerFragment.newInstance(position);

            fragment.setDoorPosition(doorPosition);
            return fragment;
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
//            updateActionBarTitle();

            if (!initialized) {
                mPager.setCurrentItem(currentDoor, false);
            }
        }

        @Override
        public float getPageWidth(int position) {
            return 0.5f;
        }
    }

    private void loadingStatusChangedHelper(boolean loading) {
        if (loading) {
            showLoadingIndicator();
        }else if (garageOpenerClient.getNumberOfGarageDoors() > 0) {
            finishedLoadingWithData();
        }else if (((MainActivity)getActivity()).getDataFragment().getPiClient().isConnected()) {
            finishedLoadingWithoutData();
        }else {
            finishedLoadingWithData();
        }
    }
    @Override
    public void loadingStatusChanged(final boolean loading) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                loadingStatusChangedHelper(loading);
            }
        });
    }

    private void showLoadingIndicator() {
        //Show the loadingLayout
        //Hide the textView
        //Show the progress bar

        loadingLayout.setVisibility(View.VISIBLE);
        noResultTextView.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
    }

    private void finishedLoadingWithData() {
        loadingLayout.setVisibility(View.GONE);
    }

    private void finishedLoadingWithoutData() {
        loadingLayout.setVisibility(View.VISIBLE);
        noResultTextView.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);

        //Update the text
        String resultString = "No doors detected.";
        noResultTextView.setText(resultString);

    }

}
