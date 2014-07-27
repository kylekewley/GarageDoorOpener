package kylekewley.garagedooropener;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.TwoLineListItem;

import com.kylekewley.piclient.OrderedUniqueArrayList;
import com.kylekewley.piclient.PiClient;
import com.kylekewley.piclient.PiMessage;
import com.kylekewley.piclient.PiMessageCallbacks;
import com.kylekewley.piclient.protocolbuffers.ParseError;
import com.squareup.wire.Message;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

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
    final OrderedUniqueArrayList<GarageStatus.DoorStatus> statusList = new OrderedUniqueArrayList<GarageStatus.DoorStatus>(new Comparator<GarageStatus.DoorStatus>() {
        @Override
        public int compare(GarageStatus.DoorStatus lhs, GarageStatus.DoorStatus rhs) {
            return rhs.uniqueId - lhs.uniqueId;
        }
    });

    /**
     * Tells whether or not the client is trying to load data.
     */
    private boolean isLoading = false;

    /**
     * The current time we are looking at.
     * Initialize with the beginning of the current day
     */
    private int timeEpoch = getBeginningOfDay(epochTime());

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
        requestGarageHistory(timeEpoch, SECONDS_PER_DAY);
    }

    @NotNull
    public boolean isLoading() {
        return isLoading;
    }

    public void setLoading(@NotNull boolean isLoading) {
        this.isLoading = isLoading;
        if (historyView != null)
            historyView.loadingStatusChanged(isLoading);
    }

    public static int getBeginningOfDay(int dayEpoch) {
        Calendar mCalendar = new GregorianCalendar(Locale.getDefault());
        mCalendar.setTime(new Date(dayEpoch*1000L));
        mCalendar.set(Calendar.HOUR, 0);
        mCalendar.set(Calendar.MINUTE, 0);
        mCalendar.set(Calendar.SECOND, 0);
        mCalendar.set(Calendar.MILLISECOND, 0);
        mCalendar.set(Calendar.AM_PM, Calendar.AM);


        return (int)(mCalendar.getTimeInMillis()/1000);
    }

    /**
     * Requests garage history from day selected by the user.
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
        final GarageHistoryRequest historyRequest = new GarageHistoryRequest(startTime, interval);
        PiMessage message = new PiMessage(Constants.ServerParserId.GARAGE_HISTORY_ID.getId(), historyRequest);

        message.setMessageCallbacks(new PiMessageCallbacks(GarageStatus.class) {
            @Override
            public void serverReturnedData(byte[] data, PiMessage message) {
                Log.w(TAG, "Problem...Server returned data. This should have returned a message");
                setLoading(false);

            }

            @Override
            public void serverRepliedWithMessage(Message response, PiMessage sentMessage) {
                setLoading(false);

                GarageStatus statusData = (GarageStatus)response;
                if (statusData != null) {
                    final List<GarageStatus.DoorStatus> doorStatuses = statusData.doors;

                    Runnable r = new Runnable() {
                        @Override
                        public void run() {
                            synchronized (statusList) {
                                for (GarageStatus.DoorStatus door : doorStatuses) {
                                    statusList.add(door);
                                }

                                notifyDataSetChanged();
                            }
                    }};

                    if (historyView != null && historyView.getActivity() != null) {
                        historyView.getActivity().runOnUiThread(r);
                    }else {
                        r.run();
                    }

                }
            }

            @Override
            public void serverSuccessfullyParsedMessage(PiMessage message) {
                setLoading(false);
            }

            @Override
            public void serverReturnedErrorForMessage(ParseError parseError, PiMessage message) {
                Log.e(TAG, "oops. We got an error: " + parseError.errorMessage);
                setLoading(false);
            }
        });


        Log.d(TAG, "Sending message");
        client.sendMessage(message);
        setLoading(true);
    }

    private int epochTime() {
        return (int)(System.currentTimeMillis()/1000);
    }

    private void addToDataSet(final GarageStatus.DoorStatus door) {
        synchronized (statusList) {
            statusList.add(door);
        }
    }

    /**
     * Clears out all existing data
     */
    public void clearData() {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                synchronized (statusList) {
                    statusList.clear();
                    notifyDataSetChanged();
                }
            }
        };
        if (historyView != null && historyView.getActivity() != null)
            historyView.getActivity().runOnUiThread(r);
        else
            r.run();
    }

    @Nullable
    public GarageHistoryView getHistoryView() {
        return historyView;
    }

    public int getTimeEpoch() {
        return timeEpoch;
    }

    public void setTimeEpoch(int timeEpoch) {
        this.timeEpoch = timeEpoch;
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
        TextView mainText = (TextView) convertView.findViewById(R.id.titleText);
        TextView subText = (TextView) convertView.findViewById(R.id.subtitleText);
        // Populate the data into the template view using the data object

        String topText = "Door " + change.garageId + (change.isClosed ? " Closed" : " Opened");

        // Return the completed view to render on screen
        String bottomText = Constants.epochDateTimeFormat.format(new Date((long)change.timestamp*1000L));

        mainText.setText(topText);
        subText.setText(bottomText);
        return convertView;
    }
}
