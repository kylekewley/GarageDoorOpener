package kylekewley.garagedooropener;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.kylekewley.piclient.PiClient;
import com.kylekewley.piclient.PiClientCallbacks;

import org.jetbrains.annotations.NotNull;


/**
 * Created by Kyle Kewley on 7/14/14.
 *
 */
public class DataFragment extends Fragment implements PiClientCallbacks {

    @NotNull
    private final PiClient piClient;

    private boolean enteredBackground = true;

    private Activity activity;

    private AlertDialog currentDialog;

    /**
     * Used as a back end for the GaragePager fragment
     */
    @NotNull
    final GarageOpenerClient garageOpenerClient;

    @NotNull
    final GarageHistoryClient garageHistoryClient;

    public DataFragment() {
        piClient = new PiClient(this);
        garageOpenerClient = new GarageOpenerClient(piClient);
        garageHistoryClient = new GarageHistoryClient(piClient);

    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        this.activity = activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (currentDialog != null) {
            currentDialog.cancel();
            currentDialog = null;
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    /*
    Private Methods
     */





    /*
    Getters and Setters
     */

    /**
     * @return A not null piClient instance.
     */
    @NotNull
    public PiClient getPiClient() {
        return piClient;
    }


    /**
     * @return true if the view has entered the background
     */
    public boolean hasEnteredBackground() {
        return enteredBackground;
    }

    /**
     * The MainActivity sets this to true if the app is in the background.
     * @param enteredBackground True if the app is running in the background.
     */
    public void setEnteredBackground(boolean enteredBackground) {
        this.enteredBackground = enteredBackground;
    }

    /**
     * @return  The garageOpenerClient object in charge of updating the opener view.
     */
    @NotNull
    public GarageOpenerClient getGarageOpenerClient() {
        return garageOpenerClient;
    }


    /**
     * @return  The garageHistoryClient object in charge of updating the history view.
     */
    @NotNull
    public GarageHistoryClient getGarageHistoryClient() {
        return garageHistoryClient;
    }

    /*
    Creating a connection.
     */

    /**
     * @return  The hostname stored in the user preferences.
     */
    private String getHostName() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(activity);

        return preferences.getString(activity.getString(R.string.pref_host_id), activity.getString(R.string.pref_default_host_name));
    }


    /**
     * @return  The port number stored in the user preferences.
     */
    private int getPortNumber() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(activity);

        return Integer.valueOf(preferences.getString(activity.getString(R.string.pref_port_id), activity.getString(R.string.pref_default_port_number)));
    }

    public void connectToServer() {
        if (piClient.isConnected()) {
            piClient.close();
            garageHistoryClient.clearData();
            garageOpenerClient.clearDoors();
            stopLoadingScreens();
        }

        piClient.connectToPiServer(getHostName(), getPortNumber());
        piClient.addToGroup(Constants.GARAGE_GROUP_ID);
        garageOpenerClient.requestGarageDoorStatus();
        garageHistoryClient.requestGarageHistory();
        Log.d("TAG", "Actually requesting history");
    }

    public void stopLoadingScreens() {
        garageHistoryClient.setLoading(false);
        garageOpenerClient.setLoading(false);
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
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(activity, "Connected", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Called when the client attempts to connect to the PiServer.
     *
     * @param piClient The client trying to make the connection.
     */
    @Override
    public void clientTryingConnectionToHost(PiClient piClient) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(activity, "Connecting", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Called when the client successfully disconnects from the host. The PiClient
     * object can now be safely destroyed.
     *
     * @param piClient The now disconnected PiClient object
     */
    @Override
    public void clientDisconnectedFromHost(PiClient piClient) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(activity, "Disconnected", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Called if the piClient is unable to connect to the host in the allotted timeout.
     * The piClient is no longer trying to make a connection to the host.
     *
     * @param piClient The client that was unable to make a connection.
     */
    @Override
    public void clientConnectionTimedOut(PiClient piClient) {
        stopLoadingScreens();
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                currentDialog = new AlertDialog.Builder(activity)
                    .setMessage("The connection to the host timed out. Try again?")
                        .setPositiveButton("Retry", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                connectToServer();
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //Do nothing
                                stopLoadingScreens();
                            }
                        }).setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        stopLoadingScreens();
                    }
                }).create();
                currentDialog.show();
            }
        });
    }

    /**
     * Called if there is an error sent by the server. As of now, this method is not being
     * used because parsing errors are sent back and handled by the PiMessage object that caused them.
     *
     * @param piClient The client that raised the error.
     * @param error    The error code associated with the error.
     */
    @Override
    public void clientRaisedError(@NotNull final PiClient piClient, @NotNull final PiClientCallbacks.ClientErrorCode error) {
        piClient.close();
        stopLoadingScreens();
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                currentDialog = new AlertDialog.Builder(activity)
                        .setMessage(error.getErrorMessage()
                                + " Do you want to reconnect to the server?")
                        .setPositiveButton("Reconnect", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                connectToServer();
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                piClient.close();
                                stopLoadingScreens();
                            }
                        }).setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        piClient.close();
                        stopLoadingScreens();
                    }
                }).create();
                currentDialog.show();
            }
        });
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
    public void clientRaisedError(@NotNull final PiClient piClient, Exception error) {
        piClient.close();
        stopLoadingScreens();
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                currentDialog =  new AlertDialog.Builder(activity)
                    .setMessage("The client encountered an unknown error. " +
                        "Do you want to reconnect to the server?")
                        .setPositiveButton("Reconnect", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                connectToServer();
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //Disconnect
                                piClient.close();
                                stopLoadingScreens();
                            }
                        }).setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        piClient.close();
                        stopLoadingScreens();
                    }
                }).create();
                currentDialog.show();
            }
        });

    }

}
