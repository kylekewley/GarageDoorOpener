package kylekewley.garagedooropener;

import android.app.ActionBar;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;


import com.kylekewley.piclient.PiClient;
import com.kylekewley.piclient.PiClientCallbacks;

import java.util.ArrayList;
import java.util.List;

import kylekewley.garagedooropener.fragments.GarageHistoryFragment;
import kylekewley.garagedooropener.fragments.GarageOpenerFragment;
import kylekewley.garagedooropener.fragments.GarageOpenerOverviewFragment;
import kylekewley.garagedooropener.fragments.GaragePager;
import kylekewley.garagedooropener.fragments.NavigationDrawerFragment;


public class MainActivity extends FragmentActivity implements
        NavigationDrawerFragment.NavigationDrawerCallbacks,
        PiClientCallbacks {
    ///The tag to use for log messages from this class
    private static final String MAIN_ACTIVITY_TAG = "MainActivity";

    private static final int SETTINGS_RESULT = 1;

    private static final int NUM_GARAGE_DOORS = 2;

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title.
     */
    private CharSequence mTitle;


    //Create the PiClient
    PiClient piClient = new PiClient();


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

    /**
     * @return  The hostname stored in the user preferences.
     */
    private String getHostName() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

        return preferences.getString(getString(R.string.pref_host_id), getString(R.string.pref_default_host_name));
    }


    /**
     * @return  The port number stored in the user preferences.
     */
    private int getPortNumber() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

        return Integer.valueOf(preferences.getString(getString(R.string.pref_port_id), getString(R.string.pref_default_port_number)));
    }

    private void connectToServer() {
        if (piClient.isConnected()) {
            piClient.close();
        }

        piClient.connectToPiServer(getHostName(), getPortNumber());
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

        Fragment fragment = null;

        switch (position) {
            case 0:
                fragment = GaragePager.newInstance(NUM_GARAGE_DOORS, position+1);
                break;
            case 1:
                fragment = GarageHistoryFragment.newInstance();
                break;
        }

        if (fragment != null) {
            // update the main content by replacing fragments

            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.container, fragment)
                    .commit();

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


    /*
    PiClient Callbacks class
     */

    /**
     * This method is called when the client successfully connects to the PiServer host.
     *
     * @param piClient The client that made the successful connection
     */
    @Override
    public void clientConnectedToHost(PiClient piClient) {

    }

    /**
     * Called when the client attempts to connect to the PiServer.
     *
     * @param piClient The client trying to make the connection.
     */
    @Override
    public void clientTryingConnectionToHost(PiClient piClient) {

    }

    /**
     * Called when the client successfully disconnects from the host. The PiClient
     * object can now be safely destroyed.
     *
     * @param piClient The now disconnected PiClient object
     */
    @Override
    public void clientDisconnectedFromHost(PiClient piClient) {

    }

    /**
     * Called if the piClient is unable to connect to the host in the allotted timeout.
     * The piClient is no longer trying to make a connection to the host.
     *
     * @param piClient The client that was unable to make a connection.
     */
    @Override
    public void clientConnectionTimedOut(PiClient piClient) {

    }

    /**
     * Called if there is an error sent by the server. As of now, this method is not being
     * used because parsing errors are sent back and handled by the PiMessage object that caused them.
     *
     * @param piClient The client that raised the error.
     * @param error    The error code associated with the error.
     */
    @Override
    public void clientRaisedError(PiClient piClient, ClientErrorCode error) {

    }

    /**
     * Called if there is an exception raised where we don't know how to deal with it.
     * This is more of a debugging tool and will only be called for exceptions that I don't understand.
     * Hopefully after testing, I won't have to use this callback.
     *
     * @param piClient The client that raised the error.
     * @param error    The Exception that was raised.
     */
    @Override
    public void clientRaisedError(PiClient piClient, Exception error) {

    }
}
