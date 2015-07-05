package org.ShinRH.android.mocklocation.googlePlace;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

import org.ShinRH.android.mocklocation.googlePlace.Constants;

import android.R.integer;
import android.R.string;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteFullException;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.format.Time;
import android.util.Log;

public class SearchPlacesDB {
	private static final String TAG = SearchPlacesDB.class.getName();
	private SQLiteDatabase mDB;
	private final Context context;
	private final SQLiteOpenHelper dbhelper;

	public SearchPlacesDB(Context c) {
		context = c;
		dbhelper = new SearchPlacesDBHelper(context,
				Constants.SearchSuggestion.DATABASENAME, null,
				Constants.SearchSuggestion.VERSION);
	}

	public void setMaxSize(long numBytes) {
		open();
		mDB.setMaximumSize(numBytes);
	}

	public void close() {
		mDB.close();
	}

	public void open() throws SQLiteException {
		try {
			mDB = dbhelper.getWritableDatabase();
		} catch (SQLiteException ex) {
			Log.e(TAG, "Open database exception caught", ex);
			mDB = dbhelper.getReadableDatabase();
		}
	}

	/**
	 * Insert the {@link SuggestPlace} into database
	 * 
	 * @param suggestPlacesList
	 *            {@link SuggestPlace}
	 */
	public void insertGooglePlaces(ArrayList<SuggestPlace> suggestPlacesList) {

		if (suggestPlacesList != null && !suggestPlacesList.isEmpty()) {

			open();

			Iterator<SuggestPlace> iterator = suggestPlacesList.iterator();

			while (iterator.hasNext()) {

				SuggestPlace suggestPlace = iterator.next();

				if (Constants.DEBUG) {
					Log.d(TAG, suggestPlace.toString());
				}

				try {

					mDB.insertOrThrow(
							Constants.SearchSuggestion.GOOGLEPLACE_TABLENAME,
							null, suggestPlace.toContentValue());

				} catch (SQLiteFullException e) {
					e.printStackTrace();
					Log.d(TAG, "Database full delte all row ");
					deleteAllRow(Constants.SearchSuggestion.GOOGLEPLACE_TABLENAME);
				}

			}

		}
	}

	/**
	 * Insert the {@link SuggestPlace} into RecentPlaces database
	 * 
	 * @param suggestPlacesList
	 *            {@link SuggestPlace}
	 */
	public void insertRecentPlaces(SuggestPlace suggestPlace) {

		if (suggestPlace != null) {
			Cursor c1 = null;
			Cursor c2 = null;
			boolean hasDup = false;
			open();
			if (Constants.DEBUG) {
				Log.d(TAG, suggestPlace.toString());
			}

			try {

				c1 = mDB.query(
						Constants.SearchSuggestion.RECENTPLACE_TABLENAME, null,
						null, null, null, null, null);

				if (c1.getCount() >= 5) {
					int id = -1;

					// find the duplicate row if any
					//
					while (c1.moveToNext()) {
						String text = c1
								.getString(c1
										.getColumnIndex(Constants.SearchSuggestion.SEARCHPLACENAME));

						if (suggestPlace.getMainDescription().equals(text)) {
							id = c1.getInt(c1
									.getColumnIndex(Constants.SearchSuggestion._ID));
							hasDup = true;
							break;
						}

					}

					if (!hasDup) {

						// find the oldest row 

						c2 = mDB.rawQuery(
								" SELECT "
										+ Constants.SearchSuggestion._ID
										+ " FROM "
										+ Constants.SearchSuggestion.RECENTPLACE_TABLENAME
										+ " WHERE "
										+ Constants.SearchSuggestion.TIMESTAMP
										+ " = (SELECT min("
										+ Constants.SearchSuggestion.TIMESTAMP
										+ ") FROM "
										+ Constants.SearchSuggestion.RECENTPLACE_TABLENAME
										+ ")", new String[0]);

						if (c2.getCount() > 0) {
							c2.moveToNext();
							id = c2.getInt(c2
									.getColumnIndex(Constants.SearchSuggestion._ID));

						}
					}

					if (id != -1) {
						mDB.delete(
								Constants.SearchSuggestion.RECENTPLACE_TABLENAME,
								Constants.SearchSuggestion._ID + "=" + id, null);
					}
				}

				mDB.insertOrThrow(
						Constants.SearchSuggestion.RECENTPLACE_TABLENAME, null,
						suggestPlace.toContentValue());

			} catch (SQLiteFullException e) {
				e.printStackTrace();
				Log.d(TAG, "Database full delte all row ");
				deleteAllRow(Constants.SearchSuggestion.RECENTPLACE_TABLENAME);
			} finally {
				if (c1 != null) {
					c1.close();
				}
				if (c2 != null) {
					c2.close();
				}
			}
		}

	}

	/**
	 * Get suggestion by query Keyword
	 * 
	 * @param queryString
	 *            The user query Keyword
	 * @return A Cursor object, which is positioned before the first entry
	 */
	public Cursor getSuggestGooglePlaces(String queryString) {
		open();
		Cursor c = null;
		/* find column KEYWORD="user query" */
		String l_selection = Constants.SearchSuggestion.INTENT_QUERY + "=?";
		try {
			c = mDB.query(Constants.SearchSuggestion.GOOGLEPLACE_TABLENAME,
					null, l_selection, new String[] { queryString }, null,
					null, null);
		} catch (SQLiteException ex) {
			Log.e(TAG, "getPlaces exception caught", ex);
		}

		return c;
	}
	
	/**
	 * Get RecentPlaces
	 * 
	 * @return A Cursor object, which is positioned before the first entry
	 */
	public Cursor getRecentPlaces() {
		open();
		Cursor c = null;
		Log.d(TAG, "getRecentPlaces");
		try {
			c = mDB.query(Constants.SearchSuggestion.RECENTPLACE_TABLENAME,
					null, null, null, Constants.SearchSuggestion.SEARCHPLACENAME,
					null, null);
		} catch (SQLiteException ex) {
			Log.e(TAG, "getRecentPlaces exception caught", ex);
		}

		return c;
	}

	/**
	 * Check if we need to query Geocoder for certain keyword , it will first
	 * check if the key word exits , if not exits return false , if exits ,
	 * check the record day if it is too old , it will return true , else return
	 * false
	 * 
	 * @param Keyword
	 *            The keyword need to check
	 * @return True for need update , false for not need
	 */
	public boolean ifNeedUpdate(String Keyword) {
		open();
		boolean ret = true;
		Cursor cursor = null;

		/**
		 * SELECT julianday(RecordDay) AS RecordDay,suggest_intent_query FROM
		 * GooglePlacesTable WHERE suggest_intent_query=?
		 */
		String queryString = " SELECT julianday("
				+ Constants.SearchSuggestion.TIMESTAMP + ") AS "
				+ Constants.SearchSuggestion.TIMESTAMP + ","
				+ Constants.SearchSuggestion.INTENT_QUERY + " FROM "
				+ Constants.SearchSuggestion.GOOGLEPLACE_TABLENAME + " WHERE "
				+ Constants.SearchSuggestion.INTENT_QUERY + "=?";

		int current_JD = Time.getJulianDay(System.currentTimeMillis(), 0);

		Log.d(TAG, queryString);

		Log.d(TAG, "current_JD " + current_JD);
		try {

			cursor = mDB.rawQuery(queryString, new String[] { Keyword });
			if (cursor.moveToNext()) {
				long record_JD = cursor
						.getLong(cursor
								.getColumnIndexOrThrow(Constants.SearchSuggestion.TIMESTAMP));
				Log.d(TAG, "julianday   of " + Keyword + " " + record_JD);

				if ((current_JD - 90) > record_JD) {
					Log.d(TAG,
							Keyword
									+ " already in database but too old , need update  ");
					delteSuggestPlacesByKeyword(Keyword,
							Constants.SearchSuggestion.GOOGLEPLACE_TABLENAME);
					ret = true;
				} else {
					Log.d(TAG, Keyword
							+ " already in database , no need update");
					ret = false;
				}
			} else {
				Log.d(TAG, Keyword + " not in database");
				ret = true;
			}

		} catch (SQLiteException ex) {
			Log.e(TAG, "getPlaces exception caught", ex);
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}

		return ret;

	}

	/**
	 * Check if specific keyword already in database
	 * 
	 * @param Keyword
	 *            The user query Keyword
	 * @return
	 */
	public boolean ifKeyWordExists(String Keyword, String tableName) {
		open();
		boolean exists = false;
		Cursor c = null;
		try {
			c = mDB.query(tableName, new String[] { "1" },
					Constants.SearchSuggestion.INTENT_QUERY + "=?",
					new String[] { Keyword }, null, null, null);
			exists = (c.getCount() > 0);
		} catch (SQLiteException ex) {
			Log.e(TAG, "getPlaces exception caught", ex);
		} finally {
			if (c != null) {
				c.close();
			}
		}

		return exists;
	}

	/**
	 * Delete all data of given table
	 * 
	 * @param tablename
	 *            The table name
	 * @return The number of row deleted
	 */
	public int deleteAllRow(String tablename) {
		open();
		return mDB.delete(tablename, "1", null);
	}

	/**
	 * Delete all the data related to the specific user query key words
	 * 
	 * @param Keyword
	 *            The user query Keyword you want to remove
	 */
	public void delteSuggestPlacesByKeyword(String Keyword, String tableName) {
		open();
		int ret = mDB.delete(tableName, Constants.SearchSuggestion.INTENT_QUERY
				+ "=?", new String[] { Keyword });
		Log.d(TAG, "deltePlaces keyword=" + Keyword + " " + ret + " rows ");
	}

}
