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

import kylekewley.garagedooropener.DoorStatusChange;
import kylekewley.garagedooropener.GarageHistoryClient;
import kylekewley.garagedooropener.GarageHistoryView;
import kylekewley.garagedooropener.MainActivity;
import kylekewley.garagedooropener.R;

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
     * The object that holds our data.
     */
    private GarageHistoryClient garageHistoryClient;

    /**
     * The array adapter for the ListView.
     */
    private DoorArrayAdapter mAdapter;

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

        garageHistoryClient = ((MainActivity)getActivity()).getDataFragment().getGarageHistoryClient();

        mAdapter = new DoorArrayAdapter(getActivity());
        mAdapter.addAll(garageHistoryClient.getStatusList());

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_garage_history, container, false);

        mListView = (ListView)view.findViewById(R.id.list_view);
        mListView.setAdapter(mAdapter);

        return view;
    }


    /*
    Garage History View implementation
     */

    @Override
    public void notifyDataSetChanged() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mAdapter != null)
                    mAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void setDataSet(final ArrayList<DoorStatusChange> statusChanges) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mListView != null && mAdapter != null) {
                    mAdapter = new DoorArrayAdapter(getActivity(), statusChanges);
                    mListView.setAdapter(mAdapter);
                }
            }
        });
    }

    public void clearDataSet() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mAdapter != null) {
                    mAdapter.clear();
                    mAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    public void addToDataSet(final Collection<DoorStatusChange> collection) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mAdapter != null) {
                    mAdapter.addAll(collection);
                    mAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    public void addToDataSet(final DoorStatusChange item) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mAdapter != null) {
                    mAdapter.add(item);
                    mAdapter.notifyDataSetChanged();
                }
            }
        });
    }


    private class DoorArrayAdapter extends ArrayAdapter<DoorStatusChange> {
        public DoorArrayAdapter(Context context) {
            super(context, R.layout.history_list_item);
        }
        public DoorArrayAdapter(Context context, ArrayList<DoorStatusChange> statusList) {
            super(context, R.layout.history_list_item, statusList);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            DoorStatusChange change = getItem(position);
            // Check if an existing view is being reused, otherwise inflate the view
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.history_list_item, parent, false);
            }
            // Lookup view for data population
            TextView tvName = (TextView) convertView.findViewById(R.id.tvName);
            TextView tvHome = (TextView) convertView.findViewById(R.id.tvHome);
            // Populate the data into the template view using the data object
            tvName.setText(Integer.toString(change.doorId));
            tvHome.setText(Long.toString(change.changeTime));
            // Return the completed view to render on screen
            return convertView;

        }


    }
}
