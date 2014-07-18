package kylekewley.garagedooropener;

import java.util.ArrayList;
import java.util.Collection;

import kylekewley.garagedooropener.protocolbuffers.GarageStatus;

public interface GarageHistoryView {
    /**
     * Tell the view to update the data.
     */
    public void notifyDataSetChanged();

    public void clearDataSet();

    public void addToDataSet(final Collection<GarageStatus.DoorStatus> collection);

    public void addToDataSet(final GarageStatus.DoorStatus item);

}
