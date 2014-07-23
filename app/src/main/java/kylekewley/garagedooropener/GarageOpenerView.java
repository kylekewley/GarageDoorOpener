package kylekewley.garagedooropener;

import android.app.Activity;

public interface GarageOpenerView {
    public Activity getActivity();

    public void loadingStatusChanged(boolean loading);

}
