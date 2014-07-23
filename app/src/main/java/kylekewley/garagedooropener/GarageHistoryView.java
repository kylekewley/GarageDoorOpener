package kylekewley.garagedooropener;

import android.app.Activity;


public interface GarageHistoryView  {
    /**
     * Tell the view to update the data.
     */
    public Activity getActivity();


    public void loadingStatusChanged(boolean loading);

}
