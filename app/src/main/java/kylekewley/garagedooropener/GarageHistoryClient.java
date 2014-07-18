package kylekewley.garagedooropener;

import com.kylekewley.piclient.PiClient;
import com.kylekewley.piclient.PiMessage;
import com.kylekewley.piclient.PiMessageCallbacks;
import com.kylekewley.piclient.protocolbuffers.ParseError;
import com.squareup.wire.Message;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import kylekewley.garagedooropener.protocolbuffers.GarageHistoryRequest;
import kylekewley.garagedooropener.protocolbuffers.GarageStatus;

/**
 * Created by kylekewley on 7/8/14.
 */
public class GarageHistoryClient {
    /**
     * The client that will be used for sending and receiving messages.
     */
    @NotNull
    private final PiClient client;

    /**
     * The view that will be used to update the history.
     */
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
    }

    /*
    Custom getters and setters
     */

    /**
     * Set and update the historyView.
     *
     * @param historyView   The view used to show the history data.
     */
    public void setHistoryView(GarageHistoryView historyView) {
        this.historyView = historyView;
    }

    public ArrayList<GarageStatus.DoorStatus> getStatusList() {
        return statusList;
    }

    /*
    Getting data
     */
    private void requestGarageHistory(int startTime, int interval) {
        final GarageHistoryRequest historyRequest = new GarageHistoryRequest(startTime, interval);
        PiMessage message = new PiMessage(Constants.ServerParserId.GARAGE_HISTORY_ID.getId(), historyRequest);

        message.setMessageCallbacks(new PiMessageCallbacks(GarageStatus.class) {
            @Override
            public void serverReturnedData(byte[] data, PiMessage message) {

            }

            @Override
            public void serverRepliedWithMessage(Message response, PiMessage sentMessage) {
                GarageStatus statusData = (GarageStatus)response;
                if (statusData != null) {
                    List<GarageStatus.DoorStatus> doorStatuses = statusData.doors;
                    //TODO: This is very inefficient.
                    for (GarageStatus.DoorStatus door : doorStatuses) {
                        if (historyView != null)
                            historyView.addToDataSet(door);
                        statusList.add(door);
                    }
                }
            }

            @Override
            public void serverSuccessfullyParsedMessage(PiMessage message) {

            }

            @Override
            public void serverReturnedErrorForMessage(ParseError parseError, PiMessage message) {

            }
        });

        client.sendMessage(message);
    }

}
