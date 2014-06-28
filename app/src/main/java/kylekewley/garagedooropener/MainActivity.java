package kylekewley.garagedooropener;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;


import com.kylekewley.piclient.PiClient;
import com.kylekewley.piclient.PiClientCallbacks;

import kylekewley.garagedooropener.adapter.TabsPagerAdapter;


public class MainActivity extends FragmentActivity implements ActionBar.TabListener, PiClientCallbacks {
    ///The tag to use for log messages from this class
    private static final String MAIN_ACTIVITY_TAG = "MainActivity";

    private static final int SETTINGS_RESULT = 1;


    private ViewPager viewPager;
    private TabsPagerAdapter mAdapter;
    private ActionBar actionBar;

    // Tab titles
    private String[] tabTitles = { "Opener", "History" };

    //Create the PiClient
    PiClient piClient = new PiClient();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        // Initialization
        viewPager = (ViewPager) findViewById(R.id.pager);
        actionBar = getActionBar();
        mAdapter = new TabsPagerAdapter(getSupportFragmentManager());

        viewPager.setAdapter(mAdapter);
        actionBar.setHomeButtonEnabled(false);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // Adding Tabs
        for (String tab_name : tabTitles) {
        actionBar.addTab(actionBar.newTab().setText(tab_name).
                setTabListener(this));
        }

        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                // on changing the page
                // make respected tab selected
                actionBar.setSelectedNavigationItem(position);

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);

        return true;
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

    /*
    Tab Listener
     */

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        viewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

    }

    /*
    Private Methods
     */


    private String getHostName() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

        return preferences.getString(getString(R.string.pref_host_id), getString(R.string.pref_default_host_name));
    }

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
