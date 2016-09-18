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
class TargetAdapter extends CursorAdapter {

    private static final String LOG_TAG = TargetAdapter.class.getSimpleName();

    private static final int VIEW_TYPE_STEEL = 0;
    private static final int VIEW_TYPE_PAPER = 1;
    private static final int VIEW_TYPE_COUNT = 2;
    private final Fragment mFragment;
    // private static final boolean USE_STEEL_LAYOUT = false;

    private Context mContext;
    private ListView mListView;

    /**
     * Cache of the children views for a forecast list item.
     */
    private static class ViewHolder {
        int targetNum;
        final TextView target;
        final Button scoreBtnA;
        final Button scoreBtnB;
        final Button scoreBtnC;
        final Button scoreBtnD;
        final Button scoreBtnM;

        ViewHolder(View view) {
            target = (TextView) view.findViewById(R.id.list_item_target);
            scoreBtnA = (Button) view.findViewById(R.id.list_item_btn_score_a);
            scoreBtnB = (Button) view.findViewById(R.id.list_item_btn_score_b);
            scoreBtnC = (Button) view.findViewById(R.id.list_item_btn_score_c);
            scoreBtnD = (Button) view.findViewById(R.id.list_item_btn_score_d);
            scoreBtnM = (Button) view.findViewById(R.id.list_item_btn_score_m);
        }
    }

    TargetAdapter(Context context, Cursor c, int flags,
                  ListView listView, Fragment fragment) {
        super(context, c, flags);
        mContext = context;
        mListView = listView;
        mFragment = fragment;
    }

    private int getItemViewType(String targetType) {
        if (targetType.equals(ScoreContract.TargetEntry.TARGET_TYPE_STEEL))
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

        if (MainActivity.USE_ADAPTER_CLICKS) {
            viewHolder.scoreBtnA.setOnClickListener(onScoreBtnClickListener);
            viewHolder.scoreBtnB.setOnClickListener(onScoreBtnClickListener);
            viewHolder.scoreBtnC.setOnClickListener(onScoreBtnClickListener);
            viewHolder.scoreBtnD.setOnClickListener(onScoreBtnClickListener);
            viewHolder.scoreBtnM.setOnClickListener(onScoreBtnClickListener);
        }
        return view;
    }

    private View.OnClickListener onScoreBtnClickListener = new View.OnClickListener() {
        // TODO move clicking from adapter to fragmet, but this is too difficult for me now.
        @Override
        public void onClick(View v) {
            Button button = (Button) v;
            int position = mListView.getPositionForView((View) v.getParent());

            MainActivity activity = (MainActivity) mContext;
            activity.onTargetSelected(position);

            // move cursor to position
            Cursor cursor = getCursor();
            if (cursor != null) {
                if (cursor.moveToPosition(position)) {
                    int targetNum = cursor.getInt(TargetFragment.COL_TARGET_NUM);
                    Log.v(LOG_TAG, "Target " + targetNum + " Button " + button.getText());
                    increaseScore(cursor, targetNum, v);
                }
            }
        }
    };

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        // Read target info from cursor
        String targetType = cursor.getString(TargetFragment.COL_TARGET_TYPE);
        int viewType = getItemViewType(targetType);
        int targetNum = cursor.getInt(TargetFragment.COL_TARGET_NUM);

        // store target number in viewholer. Why?
        ViewHolder viewHolder = (ViewHolder) view.getTag();
        viewHolder.targetNum = targetNum;

        // Write target label
        viewHolder.target.setText(targetType + targetNum);

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
        if (button != null) {

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
        } else return View.VISIBLE;
    }

    private int scoreColor(int score) {
        if (score > 0) return Color.DKGRAY;
        else return Color.GRAY;
    }

    /**
     * Increases the score of the given target.
     *
     * @param cursor    Cursor pointing to the target. Has to be open.
     * @param targetNum Number of the target to be updated
     * @param view      Button that has been clicked. Indicates the score.
     */
    private void increaseScore(Cursor cursor, int targetNum, View view) {
        Uri targetUri = ScoreContract.TargetEntry.CONTENT_URI;

        String targetType = cursor.getString(TargetFragment.COL_TARGET_TYPE);

        // update score column
        ContentValues targetValues = new ContentValues();

        if (view.getId() == R.id.list_item_btn_score_a) {
            int score = cursor.getInt(TargetFragment.COL_SCORE_A);
            if (score >= maxScore(targetType)) score = 0;
            else score++;
            targetValues.put(ScoreContract.TargetEntry.COLUMN_SCORE_A, score);
        }
        if (view.getId() == R.id.list_item_btn_score_b) {
            int score = cursor.getInt(TargetFragment.COL_SCORE_B);
            if (score >= maxScore(targetType)) score = 0;
            else score++;
            targetValues.put(ScoreContract.TargetEntry.COLUMN_SCORE_B, score);
        }
        if (view.getId() == R.id.list_item_btn_score_c) {
            int score = cursor.getInt(TargetFragment.COL_SCORE_C);
            if (score >= maxScore(targetType)) score = 0;
            else score++;
            targetValues.put(ScoreContract.TargetEntry.COLUMN_SCORE_C, score);
        }
        if (view.getId() == R.id.list_item_btn_score_d) {
            int score = cursor.getInt(TargetFragment.COL_SCORE_D);
            if (score >= maxScore(targetType)) score = 0;
            else score++;
            targetValues.put(ScoreContract.TargetEntry.COLUMN_SCORE_D, score);
        }
        if (view.getId() == R.id.list_item_btn_score_m) {
            int score = cursor.getInt(TargetFragment.COL_SCORE_M);
            if (score >= maxScore(targetType)) score = 0;
            else score++;
            targetValues.put(ScoreContract.TargetEntry.COLUMN_SCORE_M, score);
        }

        // select given target
        String selection = ScoreContract.TargetEntry.COLUMN_TARGET_NUMBER + " = ? ";
        String[] args = {Integer.toString(targetNum)};
        mContext.getContentResolver().update(targetUri, targetValues, selection, args);
    }

    private int maxScore(String targetType) {
        if (targetType.equals(ScoreContract.TargetEntry.TARGET_TYPE_STEEL))
            return ScoreContract.TargetEntry.MAX_SCORE_STEEL;
        else return ScoreContract.TargetEntry.MAX_SCORE_PAPER;
    }

}
