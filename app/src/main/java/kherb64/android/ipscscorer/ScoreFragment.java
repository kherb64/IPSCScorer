package kherb64.android.ipscscorer;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import kherb64.android.ipscscorer.data.ScoreContract;

/**
 * Fragment for non target based scores.
 */
public class ScoreFragment extends Fragment
    implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = ScoreFragment.class.getSimpleName();
    private final int SCORE_LOADER = 1;
    static final String SCORE_URI = "URI";
    private Uri mScoreUri;
    private Context mContext;
    private ShareActionProvider mShareActionProvider;
    private View mRootView;

    private int mScoreFactor;
    private int mScorePT;
    private int mScorePRC;
    private int mScoreDQ;

    // These indices are tied to SCORE_COLUMNS. If SCORE_COLUMNS changes, these
    // must change.
    static final int COL_SCORE_ID       = 0;
    static final int COL_TOTAL_PT       = 1;
    static final int COL_TOTAL_PRC      = 2;
    static final int COL_TOTAL_DQ       = 3;
    static final int COL_SHOOTER        = 4;
    static final int COL_FACTOR         = 5;
    static final int COL_NUM_SHOTS      = 6;
    static final int COL_TIME           = 7;
    static final int COL_COMMENT        = 8;

    private static final String[] SCORE_COLUMNS = {
            ScoreContract.ScoreEntry.TABLE_NAME + "." + ScoreContract.ScoreEntry._ID,
            ScoreContract.ScoreEntry.COLUMN_TOTAL_PT,
            ScoreContract.ScoreEntry.COLUMN_TOTAL_PRC,
            ScoreContract.ScoreEntry.COLUMN_TOTAL_DQ,
            ScoreContract.ScoreEntry.COLUMN_SHOOTER,
            ScoreContract.ScoreEntry.COLUMN_FACTOR,
            ScoreContract.ScoreEntry.COLUMN_NUM_SHOTS,
            ScoreContract.ScoreEntry.COLUMN_TIME,
            ScoreContract.ScoreEntry.COLUMN_COMMENT };

    /**
     * Cache of the children views for a forecast list item.
     */
    public static class ViewHolder {
        public final EditText shooter;
        public final Button btn_factor;
        public final EditText time;
        public final EditText num_shots;
        public final EditText comment;
        public final Button btn_pt;
        public final Button btn_prc;
        public final Button btn_dq;

        public ViewHolder(View view) {
            shooter = (EditText) view.findViewById(R.id.score_shooter);
            btn_factor = (Button) view.findViewById(R.id.btn_score_factor);
            time = (EditText) view.findViewById(R.id.score_time);
            num_shots = (EditText) view.findViewById(R.id.score_num_shots);
            comment = (EditText) view.findViewById(R.id.score_comment);
            btn_pt = (Button) view.findViewById(R.id.btn_score_pt);
            btn_prc = (Button) view.findViewById(R.id.btn_score_prc);
            btn_dq = (Button) view.findViewById(R.id.btn_score_dq);
        }
    }

    public ScoreFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // orig return super.onCreateView(inflater, container, savedInstanceState);
        View rootView = inflater.inflate(R.layout.fragment_score, container, false);

        // Base ist etwas sicherer, immer mitgeben is auch nicht schlecht.
        mContext = getActivity().getBaseContext();

        /* Bundle args = getArguments();
        if (args != null) {
            mScoreUri = args.getParcelable(ScoreFragment.SCORE_URI);
        } */
        mScoreUri = ScoreContract.ScoreEntry.CONTENT_URI;

        ViewHolder viewHolder = new ViewHolder(rootView);
        rootView.setTag(viewHolder);
        mRootView = rootView;

        viewHolder.btn_factor.setOnClickListener(onScoreBtnClickListener);
        viewHolder.btn_pt.setOnClickListener(onScoreBtnClickListener);
        viewHolder.btn_prc.setOnClickListener(onScoreBtnClickListener);
        viewHolder.btn_dq.setOnClickListener(onScoreBtnClickListener);

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_score, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_clear_score) {
            new ClearScoreAsyncTask().execute("");
            return true;
        }
        // TODO add sharing content
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        getLoaderManager().initLoader(SCORE_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    private View.OnClickListener onScoreBtnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Button button = (Button) v;
            increaseScore(v);
        }
    };

    /**
     * Increases the score on the given view.
     * @param view the view whos value weill be increased
     */
    private void increaseScore(View view) {
        ViewHolder viewHolder = (ViewHolder) mRootView.getTag();
        if (view.getId() == R.id.btn_score_pt) {
            mScorePT++;
            viewHolder.btn_pt.setText(mScorePT + " " + mContext.getString(R.string.score_pt));
        }
        if (view.getId() == R.id.btn_score_prc) {
            mScorePRC++;
            viewHolder.btn_prc.setText(mScorePRC + " " + mContext.getString(R.string.score_prc));
        }
        if (view.getId() == R.id.btn_score_dq) {
            mScoreDQ = 1 - mScoreDQ;
            viewHolder.btn_dq.setText(mScoreDQ + " " + mContext.getString(R.string.score_dq));
        }
        if (view.getId() == R.id.btn_score_factor) {
            mScoreFactor = 1 - mScoreFactor;
            viewHolder.btn_factor.setText(factorName(mScoreFactor));
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        new SaveScoreAsyncTask().execute("");
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (mScoreUri != null)
            return new CursorLoader(
                    getActivity(),
                    mScoreUri,
                    SCORE_COLUMNS,
                    null,
                    null,
                    null);
        else return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data != null && data.moveToFirst()) {
            // read score data from cursor and fill the views via the viewholder
            ViewHolder viewHolder = (ViewHolder) mRootView.getTag();

            viewHolder.shooter.setText(data.getString(COL_SHOOTER));
            mScoreFactor = data.getInt(COL_FACTOR);
            viewHolder.btn_factor.setText(factorName(mScoreFactor));

            viewHolder.time.setText(Integer.toString(data.getInt(COL_TIME)));
            viewHolder.num_shots.setText(Integer.toString(data.getInt(COL_NUM_SHOTS)));
            viewHolder.comment.setText(data.getString(COL_COMMENT));

            mScorePT = data.getInt(COL_TOTAL_PT);
            mScorePRC = data.getInt(COL_TOTAL_PRC);
            mScoreDQ = data.getInt(COL_TOTAL_DQ);
            viewHolder.btn_pt.setText(mScorePT + " " + mContext.getString(R.string.score_pt));
            viewHolder.btn_prc.setText(mScorePRC + " " + mContext.getString(R.string.score_prc));
            viewHolder.btn_dq.setText(mScoreDQ + " " + mContext.getString(R.string.score_dq));
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    private String factorName (int factor) {
        if (factor == ScoreContract.ScoreEntry.FACTOR_MINOR)
            return mContext.getString(R.string.factor_minor);
        else if (factor == ScoreContract.ScoreEntry.FACTOR_MAJOR)
            return mContext.getString(R.string.factor_major);
        else return "Factor <" + factor + ">";
    }

    /**
     * Saves entered screen data to the database.
     * @return return true if successful.
     */
    private boolean saveScore() {
        Log.d(LOG_TAG, "Saving score");
        Uri scoreUri = ScoreContract.ScoreEntry.CONTENT_URI;

        ViewHolder viewHolder = (ViewHolder) mRootView.getTag();

        ContentValues scoreValues = new ContentValues();

        scoreValues.put(ScoreContract.ScoreEntry.COLUMN_SHOOTER, viewHolder.shooter.getText().toString());
        scoreValues.put(ScoreContract.ScoreEntry.COLUMN_TOTAL_PT, mScorePT);
        scoreValues.put(ScoreContract.ScoreEntry.COLUMN_TOTAL_PRC, mScorePRC);
        scoreValues.put(ScoreContract.ScoreEntry.COLUMN_TOTAL_DQ, mScoreDQ);
        scoreValues.put(ScoreContract.ScoreEntry.COLUMN_FACTOR, mScoreFactor);
        scoreValues.put(ScoreContract.ScoreEntry.COLUMN_NUM_SHOTS, viewHolder.num_shots.getText().toString());
        scoreValues.put(ScoreContract.ScoreEntry.COLUMN_TIME, viewHolder.time.getText().toString());
        scoreValues.put(ScoreContract.ScoreEntry.COLUMN_COMMENT, viewHolder.comment.getText().toString());

        // score available?
        Cursor cursor = mContext.getContentResolver().query(scoreUri, null,
                null, null, null);
        if (cursor.moveToFirst()) {
            String targetType = cursor.getString(TargetFragment.COL_TARGET_TYPE);

            // update score column
            // no selection, so update all rows
            mContext.getContentResolver().update(scoreUri, scoreValues, null, null);
            Log.d(LOG_TAG, "Score updated");
        } else {
            mContext.getContentResolver().insert(scoreUri, scoreValues);
            Log.d(LOG_TAG, "Score inserted");
        }
        cursor.close();
        return true;
    }

    /**
     * Clears the score value from the database.
     */
    private void clearScore() {
        // get score
        Uri scoreUri = ScoreContract.ScoreEntry.CONTENT_URI;
        Cursor cursor = mContext.getContentResolver().query(scoreUri, null, null, null, null);

        // is there a score in the database?
        if (cursor.moveToFirst()) {

            // update score columns
            ContentValues scoreValues = new ContentValues();

            scoreValues.put(ScoreContract.ScoreEntry.COLUMN_SHOOTER, "");
            scoreValues.put(ScoreContract.ScoreEntry.COLUMN_TOTAL_PT, 0);
            scoreValues.put(ScoreContract.ScoreEntry.COLUMN_TOTAL_PRC, 0);
            scoreValues.put(ScoreContract.ScoreEntry.COLUMN_TOTAL_DQ, 0);
            scoreValues.put(ScoreContract.ScoreEntry.COLUMN_FACTOR, ScoreContract.ScoreEntry.FACTOR_MINOR);
            scoreValues.put(ScoreContract.ScoreEntry.COLUMN_NUM_SHOTS, 0);
            scoreValues.put(ScoreContract.ScoreEntry.COLUMN_TIME, 0);
            scoreValues.put(ScoreContract.ScoreEntry.COLUMN_COMMENT, "");

            // no selection, so update all rows
            mContext.getContentResolver().update(scoreUri, scoreValues, null, null);
        }
        cursor.close();
    }

    class SaveScoreAsyncTask extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... strings) {
            saveScore();
            return null;
        }
    }

    class ClearScoreAsyncTask extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... strings) {
            clearScore();
            return null;
        }
    }
}
