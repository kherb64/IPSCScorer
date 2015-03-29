package kherb64.android.ipscscorer;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
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
    // private Uri mScoreUri;
    private Uri mTargetUri;
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

    private static final String[] TOTAL_COLUMNS = {
            ScoreContract.TargetEntry.TABLE_NAME + "." + ScoreContract.TargetEntry._ID,
            ScoreContract.TargetEntry.COLUMN_SCORE_A,
            ScoreContract.TargetEntry.COLUMN_SCORE_B,
            ScoreContract.TargetEntry.COLUMN_SCORE_C,
            ScoreContract.TargetEntry.COLUMN_SCORE_D,
            ScoreContract.TargetEntry.COLUMN_SCORE_M
    };

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
        // mScoreUri = ScoreContract.ScoreEntry.CONTENT_URI;
        mTargetUri = ScoreContract.TargetEntry.CONTENT_URI;

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
        // new SaveTotalsAsyncTask().execute("");
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (null != mTargetUri) {
            // Now create and return a CursorLoader that will take care of
            // creating a Cursor for the data being displayed.
            return new CursorLoader(
                    getActivity(),
                    mTargetUri,
                    TOTAL_COLUMNS,
                    null,
                    null,
                    null);
        } else return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data != null && data.moveToFirst()) {
            // read score data from cursor and sum up
            mScoreA = 0;
            mScoreB = 0;
            mScoreC = 0;
            mScoreD = 0;
            mScoreM = 0;

            do {
                mScoreA += data.getInt(COL_TOTAL_A);
                mScoreB += data.getInt(COL_TOTAL_B);
                mScoreC += data.getInt(COL_TOTAL_C);
                mScoreD += data.getInt(COL_TOTAL_D);
                mScoreM += data.getInt(COL_TOTAL_M);
            } while (data.moveToNext());

            mScoreTotal = mScoreA + mScoreB + mScoreC + mScoreD + mScoreM;

            // fill the views via the viewholder

            ViewHolder viewHolder = (ViewHolder) mRootView.getTag();

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

}
