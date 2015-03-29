package kherb64.android.ipscscorer;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

import kherb64.android.ipscscorer.data.ScoreContract;


public class MainActivity extends ActionBarActivity
    implements BuildTargetsDialogFragment.BuildTargetsDialogListener,
        TargetFragment.Callback {

    public static final int MAX_NUM_TARGETS = 99;
    private final String LOG_TAG = MainActivity.class.getSimpleName();
    private static int mPosition;
    private boolean mTwoPane = false;
    private Context mContext;

    // sad, but it doesn't work otherwise
    public static final boolean USE_ADAPTER_CLICKS = true;
    private TargetFragment mTargetFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getApplication();
        setContentView(R.layout.activity_main);

        // determine twoPane layout
        if (findViewById(R.id.fragment_score) != null) {
            mTwoPane = true;
        }

        mPosition = ListView.INVALID_POSITION;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_score) {
            if (!mTwoPane) {
                Intent scoreIntent = new Intent(this, ScoreActivity.class);
                startActivity(scoreIntent);
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog, int steelCount, int paperCount) {
        setNumTargetPrefs(ScoreContract.TargetEntry.TARGET_TYPE_STEEL, steelCount);
        setNumTargetPrefs(ScoreContract.TargetEntry.TARGET_TYPE_PAPER, paperCount);
        rebuildTargets(steelCount, paperCount);
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
        Toast.makeText(this, "Targets remain unchanged", Toast.LENGTH_SHORT).show();
    }

    /**
     * Returns the number of targets in the settings.
     *
     * @param targetType name of target type defined in ScoreContract.TargetEntry.
     * @return returns the number of targets.
     */
    public int numTargetsPrefs(String targetType) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        String defaultValue;
        String prefValue;
        if (targetType.equals(ScoreContract.TargetEntry.TARGET_TYPE_STEEL)) {
            defaultValue = mContext.getString(R.string.pref_steel_targets_default);
            prefValue = prefs.getString(mContext.getString(R.string.pref_steel_targets_key), defaultValue);
            return Integer.parseInt(prefValue);
        } else if (targetType.equals(ScoreContract.TargetEntry.TARGET_TYPE_PAPER)) {
            defaultValue = mContext.getString(R.string.pref_paper_targets_default);
            prefValue = prefs.getString(mContext.getString(R.string.pref_paper_targets_key), defaultValue);
            return Integer.parseInt(prefValue);
        }
        return 0;
    }

    public void setNumTargetPrefs(String targetType, int count) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        SharedPreferences.Editor editor = prefs.edit();
        if (targetType.equals(ScoreContract.TargetEntry.TARGET_TYPE_STEEL)) {
            editor.putString(mContext.getString(R.string.pref_steel_targets_key),
                    Integer.toString(count));
        }
        if (targetType.equals(ScoreContract.TargetEntry.TARGET_TYPE_PAPER)) {
            editor.putString(mContext.getString(R.string.pref_paper_targets_key),
                    Integer.toString(count));
        }
        editor.commit();
    }

    /**
     * Builds targets when there are none in the database. This helps showing some targets when
     * the application is run for the first time.
     */
    public void buildInitialTargets() {
        int numSteelDb = numTargetsDb(ScoreContract.TargetEntry.TARGET_TYPE_STEEL);
        int numPaperDb = numTargetsDb(ScoreContract.TargetEntry.TARGET_TYPE_PAPER);
        int numSteelPrefs = numTargetsPrefs(ScoreContract.TargetEntry.TARGET_TYPE_STEEL);
        int numPaperPRefs = numTargetsPrefs(ScoreContract.TargetEntry.TARGET_TYPE_PAPER);
        if (numSteelDb == 0 && numSteelPrefs > 0
                || numPaperDb == 0 && numPaperPRefs > 0) {
            buildTargets();
        }
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
    public int numTargetsDb(String targetType) {
        int cnt = 0;
        Uri targetUri = ScoreContract.TargetEntry.CONTENT_URI;
        String selection = ScoreContract.TargetEntry.COLUMN_TARGET_TYPE + " = ?";
        String[] selectionArgs = { targetType };
        Cursor cursor = mContext.getContentResolver().query(targetUri, null,
                selection, selectionArgs, null);
        if (cursor.moveToFirst()) {
            cnt = cursor.getCount();
        }
        cursor.close();
        return cnt;
    }

    /**
     * Builds targets in the database taking the numbers form the settings.
     */
    public void buildTargets() {
        Log.d(LOG_TAG, "Building targets");

        // invalidate clicked position
        mPosition = ListView.INVALID_POSITION;
        int steelCount = numTargetsPrefs(ScoreContract.TargetEntry.TARGET_TYPE_STEEL);
        int paperCount = numTargetsPrefs(ScoreContract.TargetEntry.TARGET_TYPE_PAPER);

        // call the build function
        rebuildTargets(steelCount, paperCount);
    }

    /**
     * Builds targets in the database taking the given numbers. Deletes any old targets in advance.
     *
     * @param steelCount number of steel targets to be built.
     * @param paperCount number of paper targets to be built.
     */
    public void rebuildTargets(int steelCount, int paperCount) {
        new RebuildTargetsAsyncTask(steelCount, paperCount).execute("");
    }

    private int rebuildTargets2(int steelCount, int paperCount) {
        Log.d(LOG_TAG, "Rebuilding " + steelCount + " + " + paperCount + " targets");
        Uri targetUri = ScoreContract.TargetEntry.CONTENT_URI;

        // remove score
        Uri scoreUri = ScoreContract.ScoreEntry.CONTENT_URI;
        mContext.getContentResolver().delete(scoreUri, null, null);
        Log.d(LOG_TAG, "score deleted");

        // remove old targets
        int deleted = mContext.getContentResolver().delete(targetUri, null, null);
        Log.d(LOG_TAG, deleted + " old Targets deleted");

        // create new targets
        // do not use bulk load, because it would use the db directly
        // instead of using the content provider
        int targetsCreated = 0;

        ArrayList<ContentValues> targetValueList = new ArrayList<>();
        // Add steel targets to list
        for (int i = 0; i < steelCount; i++) {
            ContentValues targetValues = new ContentValues();
            targetValues.put(ScoreContract.TargetEntry.COLUMN_TARGET_NUMBER, i + 1);
            targetValues.put(ScoreContract.TargetEntry.COLUMN_TARGET_TYPE,
                    ScoreContract.TargetEntry.TARGET_TYPE_STEEL);
            targetValueList.add(targetValues);
        }
        // Add paper targets to list
        for (int i = steelCount; i < steelCount + paperCount; i++) {
            ContentValues targetValues = new ContentValues();
            targetValues.put(ScoreContract.TargetEntry.COLUMN_TARGET_NUMBER, i + 1);
            targetValues.put(ScoreContract.TargetEntry.COLUMN_TARGET_TYPE,
                    ScoreContract.TargetEntry.TARGET_TYPE_PAPER);
            targetValueList.add(targetValues);
        }
        // Insert each target of he list nto the database using the content provider
        for (ContentValues targetValues : targetValueList) {
            mContext.getContentResolver().insert(targetUri, targetValues);
            targetsCreated++;
        }
        Log.d(LOG_TAG, targetsCreated + " targets created");
        try {
            Toast.makeText(mContext, targetsCreated + " targets have been built", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            // nothing
        }
        return targetsCreated;
    }

    /**
     * Clears each score from each target in the database.
     */
    public void clearAllTargetScores() {
        Log.d(LOG_TAG, "Clearing all target scores");
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
            int updated = mContext.getContentResolver().update(targetUri, targetValues, null, null);
            Log.d(LOG_TAG, updated + " target scores cleared");
        }
        cursor.close();
    }

    /**
     * Clears each score from the given target number in the database.
     */
    public void clearTargetScores(int targetNum) {
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

    class ClearAllTargetScoresAsyncTask extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... strings) {
            clearAllTargetScores();
            return null;
        }
    }

    class ClearTargetScoresAsyncTask extends AsyncTask<String, String, String> {

        private final int mTargetNum;

        ClearTargetScoresAsyncTask(int targetNum) {
            mTargetNum = targetNum;
        }
        @Override
        protected String doInBackground(String... strings) {
            clearTargetScores(mTargetNum);
            return null;
        }
    }

    class BuildTargetsAsyncTask extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... strings) {
            buildTargets();
            return null;
        }
    }

    class RebuildTargetsAsyncTask extends AsyncTask<String, String, String> {

        private int msteelCount;
        private int mPaperCount;

        RebuildTargetsAsyncTask (int steelCount, int paperCount) {
            msteelCount = steelCount;
            mPaperCount = paperCount;
        }
        @Override
        protected String doInBackground(String... strings) {
            rebuildTargets2(msteelCount, mPaperCount);
            return null;
        }
    }

    class BuildInitialTargetsAsyncTask extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... strings) {
            buildInitialTargets();
            return null;
        }
    }

    @Override
    public void onTargetSelected(int position) {
        mPosition = position;
    }

    public int selectedTarget() {
        return mPosition;
    }
}

