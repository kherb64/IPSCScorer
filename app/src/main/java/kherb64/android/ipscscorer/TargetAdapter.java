package kherb64.android.ipscscorer;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import kherb64.android.ipscscorer.data.ScoreContract;

/**
 * Exposes list of targets
 * from a {@link android.database.Cursor} to a {@link android.widget.ListView}.
 */
public class TargetAdapter extends CursorAdapter
    implements TargetFragment.Callback {

    private static final String LOG_TAG = TargetAdapter.class.getSimpleName();

    private static final int VIEW_TYPE_STEEL = 0;
    private static final int VIEW_TYPE_PAPER = 1;
    private static final int VIEW_TYPE_COUNT = 2;
    private static final boolean USE_STEEL_LAYOUT = false;
    private final TargetFragment mTargetFragment;

    // TODO check for improvement for clicking
    // bsser im fragment
    // for clicking
    private Context mContext;
    private ListView mListView;

    /**
     * Cache of the children views for a forecast list item.
     */
    public static class ViewHolder {
        public final TextView target;
        public final Button scoreBtnA;
        public final Button scoreBtnB;
        public final Button scoreBtnC;
        public final Button scoreBtnD;
        public final Button scoreBtnM;

        public ViewHolder(View view) {
            target = (TextView) view.findViewById(R.id.list_item_target);
            scoreBtnA = (Button) view.findViewById(R.id.list_item_btn_score_a);
            scoreBtnB = (Button) view.findViewById(R.id.list_item_btn_score_b);
            scoreBtnC = (Button) view.findViewById(R.id.list_item_btn_score_c);
            scoreBtnD = (Button) view.findViewById(R.id.list_item_btn_score_d);
            scoreBtnM = (Button) view.findViewById(R.id.list_item_btn_score_m);
        }
    }

    public TargetAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
        mContext = context;
        mListView = null;
        mTargetFragment = null;
    }

    public TargetAdapter(Context context, Cursor c, int flags,
                         ListView listView, Fragment fragment) {
        super(context, c, flags);
        mContext = context;
        mListView = listView;
        // TODO pfui
        mTargetFragment = (TargetFragment) fragment;
    }

    public int getItemViewType(Cursor cursor) {
        if (cursor.getString(TargetFragment.COL_TARGET_TYPE).equals(
                ScoreContract.TargetEntry.TARGET_TYPE_STEEL))
            return VIEW_TYPE_STEEL;
        else return VIEW_TYPE_PAPER;
    }

    @Override
    public int getViewTypeCount() {
        return VIEW_TYPE_COUNT;
    }

    @Override
    public View newView(Context context, final Cursor cursor, ViewGroup viewGroup) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_target, viewGroup, false);

        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);

        viewHolder.scoreBtnA.setOnClickListener(onScoreBtnClickListener);
        viewHolder.scoreBtnB.setOnClickListener(onScoreBtnClickListener);
        viewHolder.scoreBtnC.setOnClickListener(onScoreBtnClickListener);
        viewHolder.scoreBtnD.setOnClickListener(onScoreBtnClickListener);
        viewHolder.scoreBtnM.setOnClickListener(onScoreBtnClickListener);

        return view;
    }

    private View.OnClickListener onScoreBtnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // TODO move clicking from adapter to fragment
            Button button = (Button) v;
            int position = mListView.getPositionForView((View) v.getParent());

            // save to mPosition in TargetFragment
            // TODO do not directly communicate with fragment but rather with the activity,
            // which will inform the fragment.
            mTargetFragment.setPosition(position);

            // move cursor to position
            Cursor cursor = getCursor();
            cursor.moveToPosition(position);
            int targetNum = cursor.getInt(TargetFragment.COL_TARGET_NUM);
            Log.v (LOG_TAG, "Target " + targetNum + " Button " + button.getText());
            increaseScore(targetNum, v);
            cursor.close();
        }
    };

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        ViewHolder viewHolder = (ViewHolder) view.getTag();

        // Read target Type from cursor
        int viewType = getItemViewType(cursor);
        // Read target number from cursor
        viewHolder.target.setText(cursor.getString(TargetFragment.COL_TARGET_TYPE)
                + cursor.getInt(TargetFragment.COL_TARGET_NUM));

        showScores(viewHolder, context, cursor, viewType);

    }

    private void showScores(ViewHolder viewHolder, Context context, Cursor cursor,
                            int viewType) {

        showScore(context, viewHolder, viewType,
                context.getString(R.string.score_a),
                cursor.getInt(TargetFragment.COL_SCORE_A));
        showScore(context, viewHolder, viewType,
                context.getString(R.string.score_b),
                cursor.getInt(TargetFragment.COL_SCORE_B));
        showScore(context, viewHolder, viewType,
                context.getString(R.string.score_c),
                cursor.getInt(TargetFragment.COL_SCORE_C));
        showScore(context, viewHolder, viewType,
                context.getString(R.string.score_d),
                cursor.getInt(TargetFragment.COL_SCORE_D));
        showScore(context, viewHolder, viewType,
                context.getString(R.string.score_m),
                cursor.getInt(TargetFragment.COL_SCORE_M));
    }

    private void showScore(Context context, ViewHolder viewHolder, int viewType,
                           String name, int score) {
        // get button
        Button button = viewHolderButton(context, viewHolder, name);

        // get visibility, text and color
        int visibility = scoreVisibility(context, viewType, name);
        String scoreText = scoreText(viewType, name, score);
        int color = scoreColor(score);

        // set visibility
        // weird, must use the if construct cannot not use my visibility variable!
        if (visibility == View.VISIBLE && button.getVisibility() != View.VISIBLE)
            button.setVisibility(View.VISIBLE);
        if (visibility == View.INVISIBLE && button.getVisibility() != View.INVISIBLE)
            button.setVisibility(View.INVISIBLE);

        // set text
        if (!button.getText().equals(scoreText)) button.setText(scoreText);

        // set color
        button.setTextColor(color);
    }

    private String scoreText(int viewType, String name, int score) {
        String value = "";
        if (score > 0) {
            if (viewType == VIEW_TYPE_STEEL) {
                if (score <= 1) value = name;
                else value = score + " " + name;
            } else {
                if (score <= 1) value = name;
                else if (score == 2) value = name + name;
                else value = score + " " + name;
            }
        }
        return value;
    }

    private Button viewHolderButton(Context context, ViewHolder viewHolder, String name) {
        // determine target button from name
        if (name.equals(context.getString(R.string.score_a))) {
            return viewHolder.scoreBtnA;
        } else if (name.equals(context.getString(R.string.score_b))) {
            return viewHolder.scoreBtnB;
        } else if (name.equals(context.getString(R.string.score_c))) {
            return viewHolder.scoreBtnC;
        } else if (name.equals(context.getString(R.string.score_d))) {
            return viewHolder.scoreBtnD;
        } else if (name.equals(context.getString(R.string.score_m))) {
            return viewHolder.scoreBtnM;
        } else return null;
    }

    private int scoreVisibility(Context context, int viewType, String name) {
        if (viewType == VIEW_TYPE_STEEL) {
            if (name.equals(context.getString(R.string.score_b))
                    || name.equals(context.getString(R.string.score_c))
                    || name.equals(context.getString(R.string.score_d)))
                return View.INVISIBLE;
            else return View.VISIBLE;
        }
        else return View.VISIBLE;
    }

    private int scoreColor(int score) {
        if (score > 0) return Color.DKGRAY;
        else return Color.GRAY;
    }

    private void increaseScore(int targetNum, View view) {
        Uri targetUri = ScoreContract.TargetEntry.CONTENT_URI;
        String selection = ScoreContract.TargetEntry.COLUMN_TARGET_NUMBER + " = ? ";
        String[] args = { Integer.toString(targetNum) };
        Cursor cursor = mContext.getContentResolver().query(targetUri, null,
                selection, args, null);

        // any targets available?
        if (cursor.moveToFirst()) {
            String targetType = cursor.getString(TargetFragment.COL_TARGET_TYPE);

            // update score column
            ContentValues targetValues = new ContentValues();

            if (view.getId() == R.id.list_item_btn_score_a) {
                int score = cursor.getInt(TargetFragment.COL_SCORE_A);
                if (score >= maxScore(targetType)) score = 0; else score++;
                targetValues.put(ScoreContract.TargetEntry.COLUMN_SCORE_A, score);
            }
            if (view.getId() == R.id.list_item_btn_score_b) {
                int score = cursor.getInt(TargetFragment.COL_SCORE_B);
                if (score >= maxScore(targetType)) score = 0; else score++;
                targetValues.put(ScoreContract.TargetEntry.COLUMN_SCORE_B, score);
            }
            if (view.getId() == R.id.list_item_btn_score_c) {
                int score = cursor.getInt(TargetFragment.COL_SCORE_C);
                if (score >= maxScore(targetType)) score = 0; else score++;
                targetValues.put(ScoreContract.TargetEntry.COLUMN_SCORE_C, score);
            }
            if (view.getId() == R.id.list_item_btn_score_d) {
                int score = cursor.getInt(TargetFragment.COL_SCORE_D);
                if (score >= maxScore(targetType)) score = 0; else score++;
                targetValues.put(ScoreContract.TargetEntry.COLUMN_SCORE_D, score);
            }
            if (view.getId() == R.id.list_item_btn_score_m) {
                int score = cursor.getInt(TargetFragment.COL_SCORE_M);
                if (score >= maxScore(targetType)) score = 0; else score++;
                targetValues.put(ScoreContract.TargetEntry.COLUMN_SCORE_M, score);
            }

            // no selection, so update all rows
            mContext.getContentResolver().update(targetUri, targetValues, selection, args);
        }
        cursor.close();
    }

    private int maxScore(String targetType) {
        if (targetType.equals(ScoreContract.TargetEntry.TARGET_TYPE_STEEL))
            return ScoreContract.TargetEntry.MAX_SCORE_STEEL;
        else return ScoreContract.TargetEntry.MAX_SCORE_PAPER;
    }

    @Override
    public void onItemSelected(Uri targetUri) {

    }
}
