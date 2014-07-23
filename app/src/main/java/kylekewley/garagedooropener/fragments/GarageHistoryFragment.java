package kylekewley.garagedooropener.fragments;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

import kylekewley.garagedooropener.Constants;
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
        GarageHistoryView, DatePickerDialog.OnDateSetListener, DatePickerDialog.OnDismissListener {

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
     * The day selected by the user to view at 12:00:00 AM in milliseconds.
     */
    private int dayEpochSelected = getBeginningOfDay((int)(System.currentTimeMillis()/1000));

    private static final String ARG_DAY_SELECTED = "day_selected";

    private static final String ARG_DATE_BUNDLE = "date_picker_bundle";

    private DatePickerDialog datePickerDialog = null;

    /**
     * The relative layout for the loading view
     */
    private RelativeLayout loadingLayout;

    /**
     * The textView for displaying the no result string
     */
    private TextView noResultTextView;

    /**
     * The loading indicator
     */
    private ProgressBar progressBar;

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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);


    }

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //Set the title for the activity
        ((MainActivity)getActivity()).onSectionAttached(getString(R.string.title_garage_history));

        mAdapter = ((MainActivity)getActivity()).getDataFragment().getGarageHistoryClient();
        mAdapter.setHistoryView(this);

        View view = inflater.inflate(R.layout.fragment_garage_history, container, false);

        loadingLayout = (RelativeLayout)view.findViewById(R.id.loading_layout);
        noResultTextView = (TextView)view.findViewById(R.id.no_history_text);
        progressBar = (ProgressBar)view.findViewById(R.id.progress_bar);


        mListView = (ListView)view.findViewById(R.id.list_view);
        mListView.setAdapter(mAdapter);

        loadingStatusChanged(mAdapter.isLoading());


        if (savedInstanceState != null) {
            dayEpochSelected = savedInstanceState.getInt(ARG_DAY_SELECTED);


            //Show the date picker if it was previously shown.
            Bundle dateBundle = savedInstanceState.getBundle(ARG_DATE_BUNDLE);
            if (dateBundle != null) {
                datePickerDialog = new DatePickerDialog(getActivity(), this, 0, 0, 0);
                datePickerDialog.setOnDismissListener(this);
                datePickerDialog.onRestoreInstanceState(dateBundle);
                datePickerDialog.show();
            }
        }

        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(ARG_DAY_SELECTED, dayEpochSelected);

        //Save the datePickerDialog instance state
        if (datePickerDialog != null) {
            datePickerDialog.dismiss();
            outState.putBundle(ARG_DATE_BUNDLE, datePickerDialog.onSaveInstanceState());
            datePickerDialog = null;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mAdapter.destroyHistoryView();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, @NotNull MenuInflater inflater) {
        if (!((MainActivity)getActivity()).getNavigationDrawerFragment().isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.

            inflater.inflate(R.menu.history_menu, menu);
            super.onCreateOptionsMenu(menu, inflater);
            ((MainActivity)getActivity()).restoreActionBar();
        }
        super.onCreateOptionsMenu(menu, inflater);

    }

    private static int getBeginningOfDay(int dayEpoch) {
        Calendar mCalendar = new GregorianCalendar(Locale.getDefault());
        mCalendar.setTime(new Date(dayEpoch*1000L));
        mCalendar.set(Calendar.HOUR, 0);
        mCalendar.set(Calendar.MINUTE, 0);
        mCalendar.set(Calendar.SECOND, 0);
        mCalendar.set(Calendar.MILLISECOND, 0);
        mCalendar.set(Calendar.AM_PM, Calendar.AM);


        return (int)(mCalendar.getTimeInMillis()/1000);
    }

    private void showLoadingIndicator() {
        //Show the loadingLayout
        //Hide the textView
        //Show the progress bar

        loadingLayout.setVisibility(View.VISIBLE);
        noResultTextView.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
    }

    private void finishedLoadingWithData() {
        loadingLayout.setVisibility(View.GONE);
    }

    private void finishedLoadingWithoutData() {
        loadingLayout.setVisibility(View.VISIBLE);
        noResultTextView.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);

        //Update the text
        String resultString = "No history for " + Constants.epochDateFormat.format(new Date(dayEpochSelected*1000L));
        noResultTextView.setText(resultString);

    }

    @Override
    public void loadingStatusChanged(final boolean loading) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (loading) {
                    showLoadingIndicator();
                }else if (mAdapter.getCount() > 0) {
                    finishedLoadingWithData();
                }else {
                    finishedLoadingWithoutData();
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_change_date) {
            Calendar calendar = new GregorianCalendar(Locale.getDefault());
            calendar.setTime(new Date(dayEpochSelected*1000L));

            datePickerDialog = new DatePickerDialog(getActivity(), this, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
            datePickerDialog.setOnDismissListener(this);
            datePickerDialog.show();
            return true;
        }
        return false;
    }


    @Override
    public int getDaySelected() {
        return dayEpochSelected;
    }

    /*
    DatePickerDialog.OnDateSetListener Methods
     */

    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        Log.d(GARAGE_HISTORY_TAG, "Date set to " + monthOfYear + "/" + dayOfMonth + "/" + year);
        Calendar calendar = new GregorianCalendar(Locale.getDefault());
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, monthOfYear);
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        int epochTime = getBeginningOfDay((int) (calendar.getTimeInMillis() / 1000));
        Log.d(GARAGE_HISTORY_TAG, "" + epochTime);
        if (dayEpochSelected != epochTime) {
            dayEpochSelected = epochTime;
            mAdapter.clearData();
            mAdapter.requestGarageHistory();
        }
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        datePickerDialog = null;
    }

}
