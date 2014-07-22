package kylekewley.garagedooropener;

import android.app.Application;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.kylekewley.piclient.PiClient;
import com.kylekewley.piclient.PiMessage;
import com.kylekewley.piclient.PiMessageCallbacks;
import com.kylekewley.piclient.protocolbuffers.ParseError;
import com.squareup.wire.Message;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import kylekewley.garagedooropener.protocolbuffers.GarageHistoryRequest;
import kylekewley.garagedooropener.protocolbuffers.GarageStatus;

/**
 * Created by kylekewley on 7/8/14.
 */
public class GarageHistoryClient extends BaseAdapter {
    private final String TAG = "history_client";
    private final int SECONDS_PER_DAY = 86400;
    /**
     * The client that will be used for sending and receiving messages.
     */
    @NotNull
    private final PiClient client;

    /**
     * The view that will be used to update the history.
     */
    @Nullable
    GarageHistoryView historyView;

    /**
     * The list of statuses to display.
     */
    @NotNull
    final ArrayList<GarageStatus.DoorStatus> statusList = new ArrayList<GarageStatus.DoorStatus>();


    /**
     * Create the HistoryClient with a piClient object.
     *
     * @param piClient  The client used to request data.
     */
    GarageHistoryClient(@NotNull PiClient piClient) {

        this.client = piClient;
        Log.d("History", "Requesting history");
    }

    /*
    Custom getters and setters
     */

    /**
     * Set the history view. This should be called after the onAttach
     * method of the history view, but before the adapter is used by
     * a list view.
     *
     * @param historyView   The view used to show the history data.
     */
    public void setHistoryView(GarageHistoryView historyView) {
        //Wait until the background thread is done with statusList.
        //Any updates after this will be done on the UI thread.
        synchronized (statusList) {
            this.historyView = historyView;
        }
    }

    /**
     * Sets the historyView to null. This should be called in the
     * onDestroy method of the historyView. Updates after this will
     * be done in the background.
     */
    public void destroyHistoryView() {
        historyView = null;
    }

    /*
    Getting data
     */

    /**
     * Requests the garage history for the last 24 hours.
     */
    public void requestGarageHistory() {
        requestGarageHistory(epochTime()-SECONDS_PER_DAY, SECONDS_PER_DAY);
    }

    /**
     * Requests garage history from the the current time to the current time minus interval seconds.
     *
     * @param interval  How many seconds back to request the history for.
     */
    public void requestGarageHistory(int interval) {
        requestGarageHistory(epochTime()-interval, interval);

    }

    /**
     * Requests the garage history from the given start time, to startTime+interval.
     * The interval is allowed to be negative.
     * @param startTime     The start epoch time for the history.
     * @param interval      How many seconds to request the history for.
     */
    public void requestGarageHistory(int startTime, int interval) {
        GarageHistoryRequest historyRequest = new GarageHistoryRequest(startTime, interval);
        PiMessage message = new PiMessage(Constants.ServerParserId.GARAGE_HISTORY_ID.getId(), historyRequest);

        message.setMessageCallbacks(new PiMessageCallbacks(GarageStatus.class) {
            @Override
            public void serverReturnedData(byte[] data, PiMessage message) {
                Log.d(TAG, "Problem...Server returned data");
            }

            @Override
            public void serverRepliedWithMessage(Message response, PiMessage sentMessage) {
                GarageStatus statusData = (GarageStatus)response;
                if (statusData != null) {
                    final List<GarageStatus.DoorStatus> doorStatuses = statusData.doors;
                    //TODO: This is very inefficient.
                    if (historyView != null && historyView.getActivity() != null) {
                        historyView.getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                synchronized (statusList) {
                                    for (GarageStatus.DoorStatus door : doorStatuses) {
                                        statusList.add(door);
                                    }
                                    notifyDataSetChanged();
                                }
                            }
                        });
                    }else {
                        synchronized (statusList) {
                            for (GarageStatus.DoorStatus door : doorStatuses) {
                                statusList.add(door);
                            }
                        }
                        Log.d(TAG, "Added " + doorStatuses.size() + " items in the background. Total size: " + statusList.size());
                    }

                }
            }

            @Override
            public void serverSuccessfullyParsedMessage(PiMessage message) {
                Log.d(TAG, "History parsed successfully?");
            }

            @Override
            public void serverReturnedErrorForMessage(ParseError parseError, PiMessage message) {
                Log.d(TAG, "oops. We got an error: " + parseError.errorMessage);
            }
        });


        Log.d(TAG, "Sending message");
        client.sendMessage(message);
    }

    private int epochTime() {
        return (int)(System.currentTimeMillis()/1000);
    }

    private void addToDataSet(final GarageStatus.DoorStatus door) {
        if (historyView != null && historyView.getActivity() != null) {
            historyView.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    synchronized (statusList) {
                        statusList.add(door);
                        notifyDataSetChanged();
                    }
                }
            });
        }else {
            synchronized (statusList) {
                statusList.add(door);
            }
        }
    }
    /*
    Implementing abstract methods
     */
    @Override
    public int getCount() {
        synchronized (statusList) {
            return statusList.size();
        }
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        synchronized (statusList) {
            return statusList.get(position).uniqueId;
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        GarageStatus.DoorStatus change;
        synchronized (statusList) {
            change = statusList.get(position);
        }
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            if (historyView == null) return null;
            if (historyView.getActivity() == null) return null;
            convertView = LayoutInflater.from(historyView.getActivity()).inflate(R.layout.history_list_item, parent, false);
        }
        // Lookup view for data population
        TextView tvName = (TextView) convertView.findViewById(R.id.tvName);
        TextView tvHome = (TextView) convertView.findViewById(R.id.tvHome);
        // Populate the data into the template view using the data object
        tvName.setText(Integer.toString(change.garageId));
        tvHome.setText(Long.toString(change.timestamp));
        // Return the completed view to render on screen
        return convertView;
    }
}
