package kylekewley.garagedooropener;

import java.util.ArrayList;
import java.util.Collection;

public interface GarageHistoryView {
    /**
     * Tell the view to update the data.
     */
    public void notifyDataSetChanged();

    /**
     * Give the view a data set to back it.
     * @param statusChanges The data set to back the view.
     */
    public void setDataSet(final ArrayList<DoorStatusChange> statusChanges);

    public void clearDataSet();

    public void addToDataSet(final Collection<DoorStatusChange> collection);

    public void addToDataSet(final DoorStatusChange item);

}
