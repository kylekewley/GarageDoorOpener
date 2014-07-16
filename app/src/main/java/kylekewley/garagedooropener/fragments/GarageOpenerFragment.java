package kylekewley.garagedooropener.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import kylekewley.garagedooropener.GarageOpenerClient;
import kylekewley.garagedooropener.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link GarageOpenerFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class GarageOpenerFragment extends Fragment {

    /**
     * The tag used for debugging this class
     */
    private static final String GARAGE_OPENER_TAG = "garage_opener";

    /**
     * The fragment argument representing the garage ID number for this
     * fragment.
     */
    private static final String ARG_GARAGE_ID = "garage_id";

    private int garageId;

    private TextView textView;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment GarageOpenerFragment.
     */
    public static GarageOpenerFragment newInstance(int garageID) {
        GarageOpenerFragment fragment = new GarageOpenerFragment();

        Bundle args = new Bundle();
        args.putInt(ARG_GARAGE_ID, garageID);

        fragment.setArguments(args);

        return fragment;
    }
    public GarageOpenerFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            garageId = getArguments().getInt(ARG_GARAGE_ID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_garage_opener, container, false);

        textView = (TextView)view.findViewById(R.id.page_number_text);

        if (textView != null) {
            textView.setText(Integer.toString(garageId));
        }

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        if (getView() != null) {
            textView = (TextView)getView().findViewById(R.id.page_number_text);
        }
    }

    public TextView getTextView() {
        return textView;
    }

}
