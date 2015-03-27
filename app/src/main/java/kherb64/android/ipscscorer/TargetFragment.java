package kherb64.android.ipscscorer;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

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
    private int mPosition;
    private TargetAdapter mTargetAdapter;
    private ListView mListView;
    private Context mContext;
    private int mFirstVisiblePosition;


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

        // this late?
        buildInitialTargets();
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
            // TODO add input dialog and save to preferences
            // starting point: custom alert dialog
            Toast.makeText(mContext, mContext.getString(R.string.use_settings_for_buildTargets),
                    Toast.LENGTH_SHORT).show();
            // buildTargets();
            return true;
        } else if (id == R.id.action_clear_scores) {
            clearAllTargetScores();
            return true;
        } else if (id == R.id.action_settings) {
            Intent settings = new Intent(getActivity(), SettingsActivity.class);
            startActivityForResult(settings, SETTINGS_REQUEST);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SETTINGS_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {
                boolean targetsChanged = data.getBooleanExtra(SettingsActivity.TARGETS_CHANGED, false);
                Log.v(LOG_TAG, "Targets changed? " + targetsChanged);
                if (targetsChanged) buildTargets();
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // get first position on screen
        mFirstVisiblePosition = mListView.getFirstVisiblePosition();
        if (mFirstVisiblePosition != ListView.INVALID_POSITION) {
            outState.putInt(FIRST_VISIBLE_POSITION, mFirstVisiblePosition);
        }
        // do not use for orientation change
        if (mPosition != ListView.INVALID_POSITION) {
            outState.putInt(SELECTED_KEY, mPosition);
        }
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

        // TODO check scrolling after clicking AND after orientation change
        // invalidatt on scroll?
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

    /**
     * Shows targets stored in database. If non exist, then targets according to
     * preferences are built.
     */
    // TODO check is necessary: No!
    private void showTargets() {
        Log.d(LOG_TAG, "Showing targets");
        Context context = getActivity();

        // are there any targets in the database?
        Uri targetUri = ScoreContract.TargetEntry.CONTENT_URI;
        Cursor cursor = context.getContentResolver().query(targetUri, null, null, null, null);
        if (cursor.moveToFirst()) {
            Log.d(LOG_TAG, cursor.getCount() + " Targets found in database");
        } else {
            buildTargets();

            // Call Loader
            getLoaderManager().restartLoader(TARGET_LOADER, null, this);
        }
    }

    /**
     * Builds targets when there are none in the database. This helps showing some targets when
     * the application is run for the first time.
     */
    private void buildInitialTargets() {
            int numSteelDb = numTargetsDb(ScoreContract.TargetEntry.TARGET_TYPE_STEEL);
            int numPaperDb = numTargetsDb(ScoreContract.TargetEntry.TARGET_TYPE_PAPER);
            int numSteelPrefs = numTargetsPrefs(ScoreContract.TargetEntry.TARGET_TYPE_STEEL);
            int numPaperPRefs = numTargetsPrefs(ScoreContract.TargetEntry.TARGET_TYPE_PAPER);
            if (numSteelDb == 0 && numSteelPrefs > 0
                    || numPaperDb == 0 && numPaperPRefs > 0) buildTargets();
            else if (numSteelDb != numSteelPrefs
                    || numPaperDb != numPaperPRefs)
                Log.w(LOG_TAG, "Your number of targets does not match your settings");
    }

    /**
     * Returns the number of targets in the database.
     *
     * @param targetType name of target type defined in ScoreContract.TargetEntry.
     * @return returns the number of targets.
     */
    private int numTargetsDb(String targetType) {
            Uri targetUri = ScoreContract.TargetEntry.CONTENT_URI;
            Cursor cursor = mContext.getContentResolver().query(targetUri, null,
                    null, null, null);
            if (cursor.moveToFirst()) {
                return cursor.getCount();
            } else return 0;
    }

    /**
     * Returns the number of targets in the settings.
     *
     * @param targetType name of target type defined in ScoreContract.TargetEntry.
     * @return returns the number of targets.
     */
    private int numTargetsPrefs(String targetType) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        String defaultValue = "";
        String prefValue = "";
        if (targetType.equals(ScoreContract.TargetEntry.TARGET_TYPE_STEEL)) {
            defaultValue = mContext.getString(R.string.pref_steel_targets_default);
            prefValue = prefs.getString(mContext.getString(R.string.pref_steel_targets_key), defaultValue);

        // TODO improve coding for preferences integer value retrieval
            // so!
            //String prefName =  mContext.getString(R.string.pref_steel_targets_key);
            // so net! int i = prefs.getInt(prefName, 1);
            return Integer.parseInt(prefValue);
        } else if (targetType.equals(ScoreContract.TargetEntry.TARGET_TYPE_PAPER)) {
            defaultValue = mContext.getString(R.string.pref_paper_targets_default);
            prefValue = prefs.getString(mContext.getString(R.string.pref_paper_targets_key), defaultValue);
            return Integer.parseInt(prefValue);
        }
        return 0;
    }


    /**
     * Builds targets in the database taking the numbers form the settings.
     *
     * @return Returns number of targets built.
     */
    public int buildTargets() {
        Log.d(LOG_TAG, "Building targets");

        // invalidate clicked position
        mPosition = ListView.INVALID_POSITION;
        int steelCount = numTargetsPrefs(ScoreContract.TargetEntry.TARGET_TYPE_STEEL);
        int paperCount = numTargetsPrefs(ScoreContract.TargetEntry.TARGET_TYPE_PAPER);

        // call the build function
        int numTargets = rebuildTargets(steelCount, paperCount);
        if (numTargets == 0)
            Toast.makeText(mContext, R.string.no_targets_built, Toast.LENGTH_SHORT).show();
        // no need to toast the user when targets have been created, because he sees them.
        return numTargets;
    }


    /**
     * Builds targets in the database taking the given numbers. Deletes any old targets in advance.
     *
     * @param steelCount number of steel targets to be built.
     * @param paperCount number of paper targets to be built.
     * @return returns number of targets created.
     */
    public int rebuildTargets(int steelCount, int paperCount) {
        Log.d(LOG_TAG, "Rebuilding " + steelCount + " + " + paperCount + " targets");
        Uri targetUri = ScoreContract.TargetEntry.CONTENT_URI;

        // remove old targets
        mContext.getContentResolver().delete(targetUri, null, null);
        Log.d(LOG_TAG, "Old Targets deleted");

        // create new targets
        ContentValues targetValues = new ContentValues();

        // TODO improve performace by bulk load!
        // check mor than one row in Contenvalues
        int targetsCreated = 0;
        for (int i = 0; i < steelCount; i++) {
            targetValues.put(ScoreContract.TargetEntry.COLUMN_TARGET_NUMBER, i + 1);
            targetValues.put(ScoreContract.TargetEntry.COLUMN_TARGET_TYPE,
                    ScoreContract.TargetEntry.TARGET_TYPE_STEEL);
            mContext.getContentResolver().insert(targetUri, targetValues);
            targetsCreated++;
        }
        for (int i = steelCount; i < steelCount + paperCount; i++) {
            targetValues.put(ScoreContract.TargetEntry.COLUMN_TARGET_NUMBER, i + 1);
            targetValues.put(ScoreContract.TargetEntry.COLUMN_TARGET_TYPE,
                    ScoreContract.TargetEntry.TARGET_TYPE_PAPER);
            mContext.getContentResolver().insert(targetUri, targetValues);
            targetsCreated++;
        }
        Log.d(LOG_TAG, targetsCreated + " targets created");
        return targetsCreated;
    }

    /**
     * Clears each score from each target in the database.
     */
    private void clearAllTargetScores() {
        Log.d(LOG_TAG, "Clearing all scores");
        mPosition = ListView.INVALID_POSITION;

        // are there any targets in the database?
        Uri targetUri = ScoreContract.TargetEntry.CONTENT_URI;
        Cursor cursor = mContext.getContentResolver().query(targetUri, null, null, null, null);

        // any targets available?
        if (cursor.moveToFirst()) {

            // update score columns
            ContentValues targetValues = new ContentValues();

            targetValues.put(ScoreContract.TargetEntry.COLUMN_SCORE_A, 0);
            targetValues.put(ScoreContract.TargetEntry.COLUMN_SCORE_B, 0);
            targetValues.put(ScoreContract.TargetEntry.COLUMN_SCORE_C, 0);
            targetValues.put(ScoreContract.TargetEntry.COLUMN_SCORE_D, 0);
            targetValues.put(ScoreContract.TargetEntry.COLUMN_SCORE_M, 0);

            // no selection, so update all rows
            mContext.getContentResolver().update(targetUri, targetValues, null, null);
        }
    }

    /**
     * Clears each score from the given target number in the database.
     */
    private void clearTargetScores(int targetNum) {
        Log.d(LOG_TAG, "Clearing scores of target " + targetNum);

        Uri targetUri = ScoreContract.TargetEntry.CONTENT_URI;
        String selection = ScoreContract.TargetEntry.COLUMN_TARGET_NUMBER + " = ? ";
        String[] args = {Integer.toString(targetNum)};
        Cursor cursor = mContext.getContentResolver().query(targetUri, null,
                selection, args, null);
        // any targets available?
        if (cursor.moveToFirst()) {

            // update score columns
            ContentValues targetValues = new ContentValues();

            targetValues.put(ScoreContract.TargetEntry.COLUMN_SCORE_A, 0);
            targetValues.put(ScoreContract.TargetEntry.COLUMN_SCORE_B, 0);
            targetValues.put(ScoreContract.TargetEntry.COLUMN_SCORE_C, 0);
            targetValues.put(ScoreContract.TargetEntry.COLUMN_SCORE_D, 0);
            targetValues.put(ScoreContract.TargetEntry.COLUMN_SCORE_M, 0);

            // no selection, so update all rows
            mContext.getContentResolver().update(targetUri, targetValues, selection, args);
        }
        cursor.close();
    }

    public void setPosition(int position) {
        mPosition = position;
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
        public void onItemSelected(Uri targetUri);
    }

}
