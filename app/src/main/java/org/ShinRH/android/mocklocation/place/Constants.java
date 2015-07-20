package org.ShinRH.android.mocklocation.place;


import android.provider.BaseColumns;
import android.app.SearchManager;


public class Constants {
	
	public static final boolean DEBUG = true;
	public static final boolean VERBODE = false;
	
	public class SearchSuggestion implements BaseColumns {
		
		
		public static final String PlacesApiKey = "AIzaSyDRs_BzeppLde5YbHMwDPFZ6D0pBqWx3wg";
		
		public static final String AUTHORITY_SEARCHPLACE = "org.ShinRH.android.mocklocation.googlePlace.SearchPlaceContentProvider";
		
		//the code returned for URI match to components
		public static final int    SEARCHPLACETABLE_URI_CODE_KEYWORD_QUERY = 1;
		
		//the code returned for delete all record
		public static final int    SEARCHPLACETABLE_URI_CODE_DELTE_ALL = 2;
		
		//the code returned for update recent place table
		public static final int    SEARCHPLACETABLE_URI_CODE_UPDATE_RECENTPLACE = 3;
		
		//the code returned for update recent place table
		public static final int    SEARCHPLACETABLE_URI_CODE_RECENTPLACE_QUERY = 3;
				
		//DataBase Version
		public static final int    VERSION = 1 ;
		
		//DataBase name
		public static final String DATABASENAME = "SuggestPlacesDataBase.db";
		
		//Tables name
		public static final String GOOGLEPLACE_TABLENAME = "GooglePlacesTable";
		public static final String RECENTPLACE_TABLENAME = "RecentPlacesTable";
		
		//Uri path for queried suggestions data. 
		//This is the path that the search manager will use when querying your content provider for suggestions data based on user input 
		public static final String SUGGEST_URI_PATH_QUERY = SearchManager.SUGGEST_URI_PATH_QUERY;	
		
		public static final String SUGGEST_URI_PATH_DELTEALL = "DelteAll";
		
		public static final String SUGGEST_URI_PATH_UPDATE_RECENTPLACE = "UpdateRecentPlace";
		
		// Columns
		public static final String INTENT_QUERY =  SearchManager.SUGGEST_COLUMN_QUERY;
		public static final String INTENT_EXTRA_LOCATION =  SearchManager.SUGGEST_COLUMN_INTENT_EXTRA_DATA;
		public static final String SEARCHPLACENAME = SearchManager.SUGGEST_COLUMN_TEXT_1;
		public static final String SEARCHPLACENAME2 = SearchManager.SUGGEST_COLUMN_TEXT_2;
		public static final String TIMESTAMP = "RecordDay";
		
		public static final String CREATE_GOOGLEPLACES_TABLE  = "create table " + 
				   GOOGLEPLACE_TABLENAME + "( " +
				   _ID + " integer primary key autoincrement ," + 
				   INTENT_QUERY + " text not null ," + 
				   SEARCHPLACENAME + " text not null ," +
				   SEARCHPLACENAME2 +  " text  ," +
				   INTENT_EXTRA_LOCATION + " text ," + 
				   TIMESTAMP + " TIMESTAMP NOT NULL DEFAULT current_date " +
				   " ) " ;
		public static final String CREATE_RECENTPLACES_TABLE  = "create table " + 
					RECENTPLACE_TABLENAME + "( " +
					_ID + " integer primary key autoincrement ," + 
					INTENT_QUERY + " text not null ," + 
					SEARCHPLACENAME + " text not null ," +
					SEARCHPLACENAME2 +  " text  ," +
					INTENT_EXTRA_LOCATION + " text ," +
					TIMESTAMP + " TIMESTAMP NOT NULL DEFAULT current_timestamp " +
					" ) " ;

	}

}
