package kylekewley.garagedooropener;

import android.app.ActionBar;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;


import com.kylekewley.piclient.*;
import com.kylekewley.piclient.protocolbuffers.ParseError;
import com.squareup.wire.Message;

import java.util.ArrayList;
import java.util.List;

import kylekewley.garagedooropener.fragments.GarageHistoryFragment;
import kylekewley.garagedooropener.fragments.GaragePager;
import kylekewley.garagedooropener.fragments.NavigationDrawerFragment;
import kylekewley.garagedooropener.protocolbuffers.GarageMetaData;


public class MainActivity extends FragmentActivity implements
        NavigationDrawerFragment.NavigationDrawerCallbacks {
    ///The tag to use for log messages from this class
    private static final String MAIN_ACTIVITY_TAG = "MainActivity";

    private static final String DOOR_COUNT_KEY = "num_doors";

    private static final int SETTINGS_RESULT = 1;

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title.
     */
    private CharSequence mTitle;

    /**
     * Used to store the PiClient object
     */
    private BackgroundFragment backgroundFragment;


    /**
     * The number of doors able to be controlled by the server.
     * This number is updated at launch with the requestGarageMetaData() method.
     * After the PiMessage gets the data and sets the instance variables, the
     * metaDataUpdated() method is called.
     */
    private int garageDoorCount = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

        //Get saved values
        if (savedInstanceState != null) {
            garageDoorCount = savedInstanceState.getInt(DOOR_COUNT_KEY);
        }

        recoverBackgroundFragment();
    }



    private void recoverBackgroundFragment() {
        final String backgroundFragmentTag = "background_fragment";

        android.app.FragmentManager manager = getFragmentManager();

        // find the retained fragment on activity restarts
        backgroundFragment = (BackgroundFragment)manager.findFragmentByTag(backgroundFragmentTag);


        if (backgroundFragment == null) {
            backgroundFragment = new BackgroundFragment();

            manager.beginTransaction().add(backgroundFragment, backgroundFragmentTag).commit();
        }
    }


    @Override
    protected void onStop() {
        super.onStop();
        if (isApplicationBroughtToBackground()) {
            backgroundFragment.getPiClient().close();
            backgroundFragment.setEnteredBackground(true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.main, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            //Open the user preferences menu
            Intent i = new Intent(getApplicationContext(), SettingsActivity.class);
            startActivityForResult(i, SETTINGS_RESULT);

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(DOOR_COUNT_KEY, garageDoorCount);
    }

    /**
     * Fragments must call this method from their onAttached() method.
     * This sets the title of the action bar.
     *
     * @param title The title to display for the fragment.
     */
    public void onSectionAttached(String title) {
        mTitle = title;
        getActionBar().setTitle(mTitle);
    }

    public void restoreActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }


    /*
    Private Methods
     */


    private boolean isApplicationBroughtToBackground() {
        ActivityManager am = (ActivityManager) this.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> tasks = am.getRunningTasks(1);
        if (!tasks.isEmpty()) {
            ComponentName topActivity = tasks.get(0).topActivity;
            if (!topActivity.getPackageName().equals(this.getPackageName())) {
                return true;
            }
        }

        return false;
    }







    /*
    NavigationDrawerCallbacks
     */


    /**
     * Called when an item in the navigation drawer is selected.
     *
     * @param position  The selected position.
     */
    @Override
    public void onNavigationDrawerItemSelected(int position) {

        FragmentManager fragmentManager = getSupportFragmentManager();

        Fragment fragment = null;
        String tag = null;

        switch (position) {
            case 0:
                tag = getString(R.string.tag_garage_pager);
                break;
            case 1:
                tag = getString(R.string.tag_garage_history);
                break;
        }

        fragment = fragmentManager.findFragmentByTag(tag);

        if (fragment == null) {
            switch (position) {
                case 0:
                    fragment = GaragePager.newInstance(garageDoorCount);
                    backgroundFragment.setGarageOpenerView((GaragePager)fragment);
                    break;
                case 1:
                    fragment = GarageHistoryFragment.newInstance();
                    break;
            }
        }

        if (fragment != null) {
            if (tag == null) {
                tag = Integer.toString(fragment.getId());
            }


            fragmentManager.beginTransaction().
                    replace(R.id.container, fragment, tag).
                    commit();

            fragmentManager.executePendingTransactions();
        }

    }

    /**
     * Used by the NavigationDrawerFragment to get the titles for each item
     * in the navigation drawer.
     *
     * @return  A list of titles that should be displayed in the navigation drawer
     */
    @Override
    public List<String> arrayAdapterTitles() {
        ArrayList<String> titles = new ArrayList<String>();

        titles.add(getString(R.string.title_garage_overview));

        titles.add(getString(R.string.title_garage_history));

        return titles;
    }




}
