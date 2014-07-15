package kylekewley.garagedooropener;

import android.os.Bundle;
import android.app.Fragment;

import com.kylekewley.piclient.PiClient;



/**
 * Created by kylekewley on 7/14/14.
 */
public class BackgroundFragment extends Fragment {

    private PiClient piClient = new PiClient();

    private boolean enteredBackground = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }


    /*
    Private Methods
     */


    public PiClient getPiClient() {
        return piClient;
    }


    public boolean hasEnteredBackground() {
        return enteredBackground;
    }

    public void setEnteredBackground(boolean enteredBackground) {
        this.enteredBackground = enteredBackground;
    }
}
