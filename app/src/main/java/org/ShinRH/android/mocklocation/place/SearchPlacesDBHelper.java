package org.ShinRH.android.mocklocation.place;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class SearchPlacesDBHelper extends SQLiteOpenHelper{
	/*
	 * create table MyTable (key_id integer primary key autoincrement, 
	 * title text not null, 
	 * content text not null,
	   recordDate long);*/	
	private static final String TAG =  SearchPlacesDBHelper.class.getName();
	public SearchPlacesDBHelper(Context context, String name, CursorFactory factory,
			int version) {
		super(context, name, factory, version);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		try {
			Log.d(TAG , "onCreate ++" + Constants.SearchSuggestion.CREATE_GOOGLEPLACES_TABLE);
			db.execSQL(Constants.SearchSuggestion.CREATE_GOOGLEPLACES_TABLE); 
			Log.d(TAG , "onCreate --" + Constants.SearchSuggestion.CREATE_GOOGLEPLACES_TABLE);
			Log.d(TAG , "onCreate ++" + Constants.SearchSuggestion.CREATE_RECENTPLACES_TABLE);
			db.execSQL(Constants.SearchSuggestion.CREATE_RECENTPLACES_TABLE); 
			Log.d(TAG , "onCreate --" + Constants.SearchSuggestion.CREATE_RECENTPLACES_TABLE);
			
		} catch(SQLiteException ex) {
			Log.v("Create table exception", ex.getMessage()); 
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		Log.w(TAG , " Upgrading from version "+ oldVersion +" to "+ newVersion
				+", which will destroy all old data");
		db.execSQL("drop table if exists "+ Constants.SearchSuggestion.GOOGLEPLACE_TABLENAME);
		db.execSQL("drop table if exists "+ Constants.SearchSuggestion.RECENTPLACE_TABLENAME);
		onCreate(db);
	}

}
