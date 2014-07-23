package kylekewley.garagedooropener.fragments;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import net.davidcesarino.android.atlantis.ui.dialog.DatePickerDialogFragment;

import org.jetbrains.annotations.NotNull;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import kylekewley.garagedooropener.Constants;
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
        GarageHistoryView, DatePickerDialog.OnDateSetListener {

    public static final String GARAGE_HISTORY_TAG = "garage_history";


    /**
     * The array adapter for the ListView.
     */
    private GarageHistoryClient mAdapter;

    /**
     * The actual ListView being displayed.
     */
    private ListView mListView;

    private static final String ARG_DATE_BUNDLE = "date_picker_bundle";

    private DatePickerDialogFragment datePickerDialog = null;

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
        progressBar = (ProgressBar)view.findViewById(R.id.opener_progress_bar);


        mListView = (ListView)view.findViewById(R.id.list_view);
        mListView.setAdapter(mAdapter);

        loadingStatusChanged(mAdapter.isLoading());


        if (savedInstanceState != null) {
            //Show the date picker if it was previously shown.
            Bundle dateBundle = savedInstanceState.getBundle(ARG_DATE_BUNDLE);
            if (dateBundle != null) {
                showDatePickerWithBundleDate(dateBundle);
            }
        }

        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);


        //Save the datePickerDialog instance state
        if (datePickerDialog != null) {
            if (datePickerDialog.getDialog() != null && datePickerDialog.getDialog().isShowing()) {
                outState.putBundle(ARG_DATE_BUNDLE, datePickerDialog.getBundledDate());
            }
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

    private void showLoadingIndicator() {
        //Show the loadingLayout
        //Hide the textView
        //Show the progress bar
        if (loadingLayout != null && noResultTextView != null && progressBar != null) {
            loadingLayout.setVisibility(View.VISIBLE);
            noResultTextView.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);

        }
    }

    private void finishedLoadingWithData() {
        if (loadingLayout != null)
            loadingLayout.setVisibility(View.GONE);
    }

    private void finishedLoadingWithoutData() {
        if (loadingLayout != null && noResultTextView != null && progressBar != null) {

            loadingLayout.setVisibility(View.VISIBLE);
            noResultTextView.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);

            //Update the text
            String resultString = "No history for " + Constants.epochDateFormat.format(new Date(mAdapter.getTimeEpoch() * 1000L));
            noResultTextView.setText(resultString);
        }

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
                }else if (((MainActivity)getActivity()).getDataFragment().getPiClient().isConnected()) {
                    finishedLoadingWithoutData();
                }else {
                    finishedLoadingWithData();
                }
            }
        });
    }

    private void showDatePickerWithStoredDate() {
        Calendar calendar = new GregorianCalendar(Locale.getDefault());
        calendar.setTime(new Date(mAdapter.getTimeEpoch()*1000L));
        Bundle b = new Bundle();
        b.putInt(DatePickerDialogFragment.YEAR, calendar.get(Calendar.YEAR));
        b.putInt(DatePickerDialogFragment.MONTH, calendar.get(Calendar.MONTH));
        b.putInt(DatePickerDialogFragment.DATE, calendar.get(Calendar.DAY_OF_MONTH));
        showDatePickerWithBundleDate(b);
    }

    private void showDatePickerWithBundleDate(Bundle b) {
        datePickerDialog = new DatePickerDialogFragment();
        datePickerDialog.setArguments(b);
        datePickerDialog.setOnDateSetListener(this);
        datePickerDialog.show(getActivity().getSupportFragmentManager(), "frag_date_picker");

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_change_date) {
            showDatePickerWithStoredDate();
            return true;
        }
        return false;
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
        int epochTime = mAdapter.getBeginningOfDay((int) (calendar.getTimeInMillis() / 1000));
        Log.d(GARAGE_HISTORY_TAG, "" + epochTime);
        if (mAdapter.getTimeEpoch() != epochTime) {
            mAdapter.setTimeEpoch(epochTime);
            mAdapter.clearData();
            mAdapter.requestGarageHistory();
        }
    }

}
