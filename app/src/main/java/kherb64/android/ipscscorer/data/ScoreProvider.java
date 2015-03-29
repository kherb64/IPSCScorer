package kherb64.android.ipscscorer.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

/**
 * Content provider for score.db
 */
public class ScoreProvider extends ContentProvider {

    private static final String LOG_TAG = ScoreProvider.class.getSimpleName();

    // The URI Matcher used by this content provider.
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private ScoreDbHelper mOpenHelper;

    static final int TARGET = 100; // target based scores
    static final int SCORE  = 101;  // totals, shooter, ...

    private static final SQLiteQueryBuilder sTargetQueryBuilder;
    static {
        sTargetQueryBuilder = new SQLiteQueryBuilder();
        sTargetQueryBuilder.setTables(ScoreContract.TargetEntry.TABLE_NAME);
    }

    static UriMatcher buildUriMatcher() {
        // 1) The code passed into the constructor represents the code to return for the root
        // URI.  It's common to use NO_MATCH as the code for this case. Add the constructor below.
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = ScoreContract.CONTENT_AUTHORITY;

        // 2) Use the addURI function to match each of the types.  Use the constants from
        // WeatherContract to help define the types to the UriMatcher.
        matcher.addURI(authority, ScoreContract.PATH_TARGET, TARGET);
        matcher.addURI(authority, ScoreContract.PATH_SCORE, SCORE);

        // 3) Return the new matcher!
        return matcher;
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new ScoreDbHelper(getContext());
        return true;
    }

    @Override
    public String getType(Uri uri) {
        // Use the Uri Matcher to determine what kind of URI this is.
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case TARGET:
                return ScoreContract.TargetEntry.CONTENT_TYPE;
            case SCORE:
                return ScoreContract.ScoreEntry.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    /**
     * Opens a query against the weather or location table using the given selection.
     * @param uri uri
     * @param projection columns for result
     * @param selection columns for where clause
     * @param selectionArgs values for where clause
     * @param sortOrder order of the result
     * @return A cursor to the selected weather or location data.
     */
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        // Here's the switch statement that, given a URI, will determine what kind of request it is,
        // and query the database accordingly.
        Cursor retCursor;
        switch (sUriMatcher.match(uri)) {
            // "target"
            case TARGET: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        ScoreContract.TargetEntry.TABLE_NAME,
                        projection, selection, selectionArgs, null, null, sortOrder
                );
                break;
            }
            case SCORE: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        ScoreContract.ScoreEntry.TABLE_NAME,
                        projection, selection, selectionArgs, null, null, sortOrder
                );
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        // Register content observer
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    /**
     * Inserts one entry into the score database.
     * @param uri Uri built by ScoreContract rules.
     * @param values Contentvalues to be inserted.
     * @return Returns Uri appended with newly created record id.
     */
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match) {
            case TARGET: {
                long _id = db.insert(ScoreContract.TargetEntry.TABLE_NAME, null, values);
                if (_id > 0)
                    returnUri = ScoreContract.TargetEntry.buildTargetUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case SCORE: {
                long _id = db.insert(ScoreContract.ScoreEntry.TABLE_NAME, null, values);
                if (_id > 0)
                    returnUri = ScoreContract.ScoreEntry.buildScoreUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    /**
     * Deletes entries in the score database.
     * @param uri Uri built by rules in ScoreContract
     * @param selection Common selection string for Cursor.queries
     * @param selectionArgs Common selection valuees for Cursor.queries
     * @return returns number of deleted entries
     */
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Student: Start by getting a writable database
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsDeleted;

        // This makes delete all rows return the number of rows deleted
        if (selection == null) selection = "1";

        switch (match) {
            case TARGET: {
                rowsDeleted = db.delete(ScoreContract.TargetEntry.TABLE_NAME, selection, selectionArgs);
                break;
            }
            case SCORE: {
                rowsDeleted = db.delete(ScoreContract.ScoreEntry.TABLE_NAME, selection, selectionArgs);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        // notify the listeners here.
        if (rowsDeleted != 0)
            getContext().getContentResolver().notifyChange(uri, null);

        // Student: return the actual rows deleted
        return rowsDeleted;
    }

    /**
     * Updates entries in the score database.
     * @param uri Uri built by rules in ScoreContract
     * @param selection Common selection string for Cursor.queries
     * @param selectionArgs Common selection valuees for Cursor.queries
     * @return returns number of updated entries
     */
    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsUpdated;

        switch (match) {
            case TARGET: {
                rowsUpdated = db.update(ScoreContract.TargetEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            }
            case SCORE: {
                rowsUpdated = db.update(ScoreContract.ScoreEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        if (rowsUpdated != 0)
            getContext().getContentResolver().notifyChange(uri, null);

        return rowsUpdated;
    }

}
