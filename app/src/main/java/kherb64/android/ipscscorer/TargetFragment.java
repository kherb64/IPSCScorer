package kherb64.android.ipscscorer;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import kherb64.android.ipscscorer.data.ScoreContract;


/**
 * A placeholder fragment containing a simple view.
 */
public class TargetFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<Cursor> {

    private final String LOG_TAG = TargetFragment.class.getSimpleName();
    private static final int SETTINGS_REQUEST = 1;
    private static final String SELECTED_KEY = "selected_position";
    private static final String FIRST_VISIBLE_POSITION = "first_visible_position";

    // These indices are tied to SCORE_COLUMNS. If SCORE_COLUMNS changes, these
    // must change.
    static final int COL_TARGET_ID = 0;
    static final int COL_TARGET_NUM = 1;
    static final int COL_TARGET_TYPE = 2;
    static final int COL_SCORE_A = 3;
    static final int COL_SCORE_B = 4;
    static final int COL_SCORE_C = 5;
    static final int COL_SCORE_D = 6;
    static final int COL_SCORE_M = 7;

    private static final String[] TARGET_COLUMNS = {
            ScoreContract.TargetEntry.TABLE_NAME + "." + ScoreContract.TargetEntry._ID,
            ScoreContract.TargetEntry.COLUMN_TARGET_NUMBER,
            ScoreContract.TargetEntry.COLUMN_TARGET_TYPE,
            ScoreContract.TargetEntry.COLUMN_SCORE_A,
            ScoreContract.TargetEntry.COLUMN_SCORE_B,
            ScoreContract.TargetEntry.COLUMN_SCORE_C,
            ScoreContract.TargetEntry.COLUMN_SCORE_D,
            ScoreContract.TargetEntry.COLUMN_SCORE_M
    };

    private final int TARGET_LOADER = 0;
    private TargetAdapter mTargetAdapter;
    private ListView mListView;
    private Context mContext;
    MainActivity mMainActivity;
    private int mFirstVisiblePosition;
    private int mPosition;


    public TargetFragment() {
        mPosition = ListView.INVALID_POSITION;
        mFirstVisiblePosition = ListView.INVALID_POSITION;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(TARGET_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mContext = getActivity();
        mMainActivity = (MainActivity) mContext;

        View rootView = inflater.inflate(R.layout.fragment_targets, container, false);
        mListView = (ListView) rootView.findViewById(R.id.listview_targets);

        mTargetAdapter = new TargetAdapter(mContext, null, 0, mListView, this);
        mListView.setAdapter(mTargetAdapter);

        // Clicking happens in Adapter
        // mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(SELECTED_KEY))
                mPosition = savedInstanceState.getInt(SELECTED_KEY);
            if (savedInstanceState.containsKey(FIRST_VISIBLE_POSITION))
                mFirstVisiblePosition = savedInstanceState.getInt(FIRST_VISIBLE_POSITION);
        }

        mMainActivity.onTargetSelected(mPosition);
        mMainActivity.buildInitialTargets();

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_targets, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_build_targets) {
            showBuildTargetsDialog();
            return true;
        } else if (id == R.id.action_clear_scores) {
            mMainActivity.clearAllTargetScores();
            return true;
        } else if (id == R.id.action_settings) {
            Intent settings = new Intent(getActivity(), SettingsActivity.class);
            startActivityForResult(settings, SETTINGS_REQUEST);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // get first position on screen
        mFirstVisiblePosition = mListView.getFirstVisiblePosition();
        outState.putInt(FIRST_VISIBLE_POSITION, mFirstVisiblePosition);
        mPosition = mMainActivity.selectedTarget();
        outState.putInt(SELECTED_KEY, mPosition);
        super.onSaveInstanceState(outState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Sort order; ascending, by date
        String sortOrder = ScoreContract.TargetEntry.COLUMN_TARGET_NUMBER + " ASC";
        Uri allTargetsUri = ScoreContract.TargetEntry.CONTENT_URI;

        return new CursorLoader(getActivity(),
                allTargetsUri,
                TARGET_COLUMNS,
                null,
                null,
                sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Swap the new cursor in. (The framework will take care of closing the
        // old cursor once we return.)
        mTargetAdapter.swapCursor(data);
        int mainPosition = mMainActivity.selectedTarget();
        if (mainPosition != ListView.INVALID_POSITION)
            mPosition = mMainActivity.selectedTarget();

        // invalidated in MainActivity.onCreate
        if (mPosition != ListView.INVALID_POSITION) {
            // use when clicking
            mListView.smoothScrollToPosition(mPosition);
        } else if (mFirstVisiblePosition != ListView.INVALID_POSITION) {
            // use for orientation change
            mListView.smoothScrollToPosition(mFirstVisiblePosition);
        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        try {
            mTargetAdapter.swapCursor(null);
        } catch (NullPointerException e) {
            // nothing
        }
    }

    /*
    public void setPosition (int position) {
        mPosition = position;
    }
    */

    private void showBuildTargetsDialog () {
        DialogFragment dialog = new BuildTargetsDialogFragment();
        dialog.show(getFragmentManager(), "bertl");
    }

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callback {
        /**
         * DetailFragmentCallback for when an item has been selected.
         */
        void onTargetSelected(int position);
    }

}
