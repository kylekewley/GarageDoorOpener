package kylekewley.garagedooropener;

import com.kylekewley.piclient.PiClient;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

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
    final ArrayList<DoorStatusChange> statusList = new ArrayList<DoorStatusChange>();


    /**
     * Create the HistoryClient with a piClient object.
     *
     * @param piClient  The client used to request data.
     */
    GarageHistoryClient(@NotNull PiClient piClient) {
        this.client = piClient;
        statusList.add(new DoorStatusChange(1, 1, true));
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

    public ArrayList<DoorStatusChange> getStatusList() {
        return statusList;
    }

}
