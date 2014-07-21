package kylekewley.garagedooropener.fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;

import kylekewley.garagedooropener.GarageHistoryClient;
import kylekewley.garagedooropener.GarageHistoryView;
import kylekewley.garagedooropener.MainActivity;
import kylekewley.garagedooropener.R;
import kylekewley.garagedooropener.protocolbuffers.GarageStatus;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link GarageHistoryFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class GarageHistoryFragment extends Fragment implements
        GarageHistoryView {

    public static final String GARAGE_HISTORY_TAG = "garage_history";


    /**
     * The array adapter for the ListView.
     */
    private GarageHistoryClient mAdapter;

    /**
     * The actual ListView being displayed.
     */
    private ListView mListView;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment GarageHistoryFragment.
     */
    @NotNull
    public static GarageHistoryFragment newInstance() {
        GarageHistoryFragment fragment = new GarageHistoryFragment();
        Log.d(GARAGE_HISTORY_TAG, "Creating new instance");
        return fragment;
    }


    public GarageHistoryFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //Set the title for the activity
        ((MainActivity)getActivity()).onSectionAttached(getString(R.string.title_garage_history));

        mAdapter = ((MainActivity)getActivity()).getDataFragment().getGarageHistoryClient();
        mAdapter.setHistoryView(this);

        View view = inflater.inflate(R.layout.fragment_garage_history, container, false);

        mListView = (ListView)view.findViewById(R.id.list_view);
        mListView.setAdapter(mAdapter);


        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mAdapter.destroyHistoryView();
    }


}
