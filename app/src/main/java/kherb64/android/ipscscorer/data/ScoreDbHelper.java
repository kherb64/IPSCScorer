package kherb64.android.ipscscorer.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by herb on 20.03.15.
 */
public class ScoreDbHelper extends SQLiteOpenHelper {

    // If you change the database schema, you must increment the database version.
    private static final int DATABASE_VERSION = 3;

    static final String DATABASE_NAME = "score.db";


    public ScoreDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        // Create a table to hold targets. A target consists of the target number,
        // target type and 5 scores
        final String SQL_CREATE_TARGET_TABLE = "CREATE TABLE "
                + ScoreContract.TargetEntry.TABLE_NAME + " (" +
                ScoreContract.TargetEntry._ID + " INTEGER PRIMARY KEY," +

                ScoreContract.TargetEntry.COLUMN_TARGET_NUMBER + " INTEGER UNIQUE NOT NULL, " +
                ScoreContract.TargetEntry.COLUMN_TARGET_TYPE + " TEXT NOT NULL," +
                ScoreContract.TargetEntry.COLUMN_SCORE_A + " INTEGER, " +
                ScoreContract.TargetEntry.COLUMN_SCORE_B + " INTEGER, " +
                ScoreContract.TargetEntry.COLUMN_SCORE_C + " INTEGER, " +
                ScoreContract.TargetEntry.COLUMN_SCORE_D + " INTEGER, " +
                ScoreContract.TargetEntry.COLUMN_SCORE_M + " INTEGER" +

                " );";

        // Create a table holding the score. A score has only entry and consists
        // of the sum of the target hits, the ohter hist as well as other data.
        final String SQL_CREATE_SCORE_TABLE = "CREATE TABLE "
                + ScoreContract.ScoreEntry.TABLE_NAME + " (" +
                ScoreContract.ScoreEntry._ID + " INTEGER PRIMARY KEY," +

                ScoreContract.ScoreEntry.COLUMN_TOTAL_A + " INTEGER, " +
                ScoreContract.ScoreEntry.COLUMN_TOTAL_B + " INTEGER, " +
                ScoreContract.ScoreEntry.COLUMN_TOTAL_C + " INTEGER, " +
                ScoreContract.ScoreEntry.COLUMN_TOTAL_D + " INTEGER, " +
                ScoreContract.ScoreEntry.COLUMN_TOTAL_M + " INTEGER, " +
                ScoreContract.ScoreEntry.COLUMN_TOTAL_TARGET  + " INTEGER, " +
                ScoreContract.ScoreEntry.COLUMN_TOTAL_PT + " INTEGER, " +
                ScoreContract.ScoreEntry.COLUMN_TOTAL_PRC + " INTEGER, " +
                ScoreContract.ScoreEntry.COLUMN_TOTAL_DQ + " INTEGER, " +
                ScoreContract.ScoreEntry.COLUMN_SHOOTER + " TEXT, " +
                ScoreContract.ScoreEntry.COLUMN_FACTOR + " INTEGER, " +
                ScoreContract.ScoreEntry.COLUMN_NUM_SHOTS  + " INTEGER, " +
                ScoreContract.ScoreEntry.COLUMN_TIME  + " INTEGER, " +
                ScoreContract.ScoreEntry.COLUMN_COMMENT + " TEXT" +

                " );";

        sqLiteDatabase.execSQL(SQL_CREATE_TARGET_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_SCORE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        // Dont even  try to update, just begin from scratch
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + ScoreContract.TargetEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + ScoreContract.ScoreEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }


}
