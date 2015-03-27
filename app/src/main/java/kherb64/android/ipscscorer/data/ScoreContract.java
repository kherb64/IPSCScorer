package kherb64.android.ipscscorer.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Defines table and columns names for the score database.
 */
public class ScoreContract {
    public static final String CONTENT_AUTHORITY = "kherb64.android.ipscscorer.app";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_TARGET = "target";
    public static final String PATH_SCORE = "score";

    /**
     * Defines table contents of the target table. Contains target based scores.
     */
    public static final class TargetEntry implements BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_TARGET).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_TARGET;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_TARGET;

        // Table name
        public static final String TABLE_NAME = "target";

        // Target number
        public static final String COLUMN_TARGET_NUMBER = "target_number";
        // Target type: S (steel) or P (paper)
        public static final String COLUMN_TARGET_TYPE = "target_type";

        // Number of hits per score A,B,C,D,M
        public static final String COLUMN_SCORE_A = "score_a";
        public static final String COLUMN_SCORE_B = "score_b";
        public static final String COLUMN_SCORE_C = "score_c";
        public static final String COLUMN_SCORE_D = "score_d";
        public static final String COLUMN_SCORE_M = "score_m";

        public static final String TARGET_TYPE_STEEL = "S";
        public static final String TARGET_TYPE_PAPER = "P";
        public static final int MAX_SCORE_PAPER = 2;
        public static final int MAX_SCORE_STEEL = 1;

        public static Uri buildTargetUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }

    /**
     * Defines table contents of the score table. Contains non table based scores
     */
    public static final class ScoreEntry implements BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_SCORE).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_SCORE;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_SCORE;

        // Table name
        public static final String TABLE_NAME = "score";

        // total number of target hits
        public static final String COLUMN_TOTAL_A = "total_a";
        public static final String COLUMN_TOTAL_B = "total_b";
        public static final String COLUMN_TOTAL_C = "total_c";
        public static final String COLUMN_TOTAL_D = "total_d";
        public static final String COLUMN_TOTAL_M = "total_m";

        // total number of target hits
        public static final String COLUMN_TOTAL_TARGET = "total_target";

        // total number of other hits
        public static final String COLUMN_TOTAL_PT = "total_pt";
        public static final String COLUMN_TOTAL_PRC = "total_prc";
        public static final String COLUMN_TOTAL_DQ = "total_dq";

        // Other
        public static final String COLUMN_SHOOTER = "shooter";
        public static final String COLUMN_FACTOR = "factor";
        public static final String COLUMN_NUM_SHOTS = "num_shots";
        public static final String COLUMN_TIME = "time";
        public static final String COLUMN_COMMENT = "comment";

        public static final int FACTOR_MINOR = 0;
        public static final int FACTOR_MAJOR = 1;


        public static Uri buildScoreUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

    }
}