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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import kherb64.android.ipscscorer.data.ScoreContract;

/**
 * Fragment holding the total target scores
 */
public class TotalFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<Cursor> {

    private final String LOG_TAG = TotalFragment.class.getSimpleName();
    private final int TOTAL_LOADER = 2;
    static final String SCORE_URI = "URI";
    private Uri mScoreUri;
    private Context mContext;
    private View mRootView;

    private int mScoreA;
    private int mScoreB;
    private int mScoreC;
    private int mScoreD;
    private int mScoreM;
    private int mScoreTotal;

    // These indices are tied to TOTAL_COLUMNS. If TOTAL_COLUMNS changes, these
    // must change.
    static final int COL_SCORE_ID = 0;
    static final int COL_TOTAL_A = 1;
    static final int COL_TOTAL_B = 2;
    static final int COL_TOTAL_C = 3;
    static final int COL_TOTAL_D = 4;
    static final int COL_TOTAL_M = 5;
    static final int COL_TOTAL_TARGET = 6;

    private static final String[] TOTAL_COLUMNS = {
            ScoreContract.ScoreEntry.TABLE_NAME + "." + ScoreContract.ScoreEntry._ID,
            ScoreContract.ScoreEntry.COLUMN_TOTAL_A,
            ScoreContract.ScoreEntry.COLUMN_TOTAL_B,
            ScoreContract.ScoreEntry.COLUMN_TOTAL_C,
            ScoreContract.ScoreEntry.COLUMN_TOTAL_D,
            ScoreContract.ScoreEntry.COLUMN_TOTAL_M,
            ScoreContract.ScoreEntry.COLUMN_TOTAL_TARGET};

    /**
     * Cache of the children views for a forecast list item.
     */
    public static class ViewHolder {
        public final TextView total_a;
        public final TextView total_b;
        public final TextView total_c;
        public final TextView total_d;
        public final TextView total_m;
        public final TextView total_target;

        public ViewHolder(View view) {
            total_a = (TextView) view.findViewById(R.id.score_total_a);
            total_b = (TextView) view.findViewById(R.id.score_total_b);
            total_c = (TextView) view.findViewById(R.id.score_total_c);
            total_d = (TextView) view.findViewById(R.id.score_total_d);
            total_m = (TextView) view.findViewById(R.id.score_total_m);
            total_target = (TextView) view.findViewById(R.id.score_total_target);
        }
    }

    public TotalFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // orig return super.onCreateView(inflater, container, savedInstanceState);
        mRootView = inflater.inflate(R.layout.target_total, container, false);
        mContext = getActivity();
        mScoreUri = ScoreContract.ScoreEntry.CONTENT_URI;

        ViewHolder viewHolder = new ViewHolder(mRootView);
        mRootView.setTag(viewHolder);

        return mRootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        getLoaderManager().initLoader(TOTAL_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onPause() {
        super.onPause();
        new SaveTotalsAsyncTask().execute("");
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (null != mScoreUri) {
            // No create and return a CursorLoader that will take care of
            // creating a Cursor for the data being displayed.
            return new CursorLoader(
                    getActivity(),
                    mScoreUri,
                    TOTAL_COLUMNS,
                    null,
                    null,
                    null);
        } else return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data != null && data.moveToFirst()) {
            // read score data from cursor and fill the views via the viewholder
            ViewHolder viewHolder = (ViewHolder) mRootView.getTag();

            mScoreA = data.getInt(COL_TOTAL_A);
            mScoreB = data.getInt(COL_TOTAL_B);
            mScoreC = data.getInt(COL_TOTAL_C);
            mScoreD = data.getInt(COL_TOTAL_D);
            mScoreM = data.getInt(COL_TOTAL_M);
            mScoreTotal = data.getInt(COL_TOTAL_TARGET);

            viewHolder.total_a.setText(mScoreA + " A");
            viewHolder.total_b.setText(mScoreB + " B");
            viewHolder.total_c.setText(mScoreC + " C");
            viewHolder.total_d.setText(mScoreD + " D");
            viewHolder.total_m.setText(mScoreM + " M");
            viewHolder.total_target.setText(Integer.toString(mScoreTotal));
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    /**
     * Saves entered screen data to the database.
     * @return return true if successful.
     */
    public boolean saveTotals() {
        Log.d(LOG_TAG, "Saving totals");
        Uri totalUri = ScoreContract.ScoreEntry.CONTENT_URI;

        ContentValues totalValues = new ContentValues();

        totalValues.put(ScoreContract.ScoreEntry.COLUMN_TOTAL_A, mScoreA);
        totalValues.put(ScoreContract.ScoreEntry.COLUMN_TOTAL_B, mScoreB);
        totalValues.put(ScoreContract.ScoreEntry.COLUMN_TOTAL_C, mScoreC);
        totalValues.put(ScoreContract.ScoreEntry.COLUMN_TOTAL_D, mScoreD);
        totalValues.put(ScoreContract.ScoreEntry.COLUMN_TOTAL_M, mScoreM);
        totalValues.put(ScoreContract.ScoreEntry.COLUMN_TOTAL_TARGET, mScoreTotal);

        // score available?
        Cursor cursor = mContext.getContentResolver().query(totalUri, null,
                null, null, null);
        if (cursor.moveToFirst()) {
            // update score columns
            mContext.getContentResolver().update(totalUri, totalValues, null, null);
            Log.d(LOG_TAG, "Totals updated");
        } else {
            mContext.getContentResolver().insert(totalUri, totalValues);
            Log.d(LOG_TAG, "Totals inserted");
        }
        return true;
    }

    class SaveTotalsAsyncTask extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... strings) {
            saveTotals();
            return null;
        }
    }

}
