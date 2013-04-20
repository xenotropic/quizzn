package net.xenotropic.quizznworldcap;

import android.content.Context;
import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.util.Log;
import au.com.bytecode.opencsv.*;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.List;
import net.xenotropic.quizznworldcap.R;

public class MyDBhelper extends SQLiteOpenHelper {

	private static final String CREATE_TABLE="create table "+
	Constants.TABLE_NAME+" ("+
	Constants.KEY_ID+" integer primary key autoincrement, "+
	Constants.FIRST_WORD+ " text not null, "+
	Constants.SECOND_WORD+ " text not null, "+
	Constants.WORD_CATEGORY+ " text not null, "+
	Constants.QUIZZED_FORWARD+ " text not null, "+
	Constants.QUIZZED_REVERSE+ " text not null)";
	
	private Context constructor_context;
	
	public MyDBhelper(Context context, String name, CursorFactory factory, int version) {
		super(context, name, factory, version);
		constructor_context = context;
//		Log.w("MyDBHelper", "Constructor called.");
	}
	
	@Override
	public void onCreate(SQLiteDatabase db) { 
		// Log.w("MyDBHelper", "onCreate called.");
		try {
			db.execSQL(CREATE_TABLE);
			loadDb(constructor_context, db);
		} catch (SQLiteException ex) {
			Log.w("Create table exception", ex.getMessage());
		}
	}

	private void loadDb (Context dbload_context, SQLiteDatabase db) {
		InputStream input_stream = dbload_context.getApplicationContext().getResources().openRawResource(R.raw.wordpairs);

		InputStreamReader isr = null;
		try {
			isr = new InputStreamReader(input_stream, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		CSVReader csv_reader = new CSVReader (isr);

		String[] current_text_row = null;
		String native_word = "initial_native_word";
		String foreign_word = "initial_foreign_word";
		String word_type = "initial_word_type";
		ContentValues insert_word_pairs;
		
		try {
			List wordlist = csv_reader.readAll();
//			Log.w ("MyDBhelper", "inserting " + native_word + " & " + foreign_word);		
			Log.w ("Quizzn Spanish", "inserting words into database");		
			for (int i=0; i < wordlist.size(); i++) {
				current_text_row = (String[])wordlist.get(i);
				native_word=current_text_row[0];
				foreign_word=current_text_row[1];
				word_type=current_text_row[2];
				insert_word_pairs = new ContentValues();
				insert_word_pairs.put(Constants.FIRST_WORD, native_word);
				insert_word_pairs.put(Constants.SECOND_WORD, foreign_word);
				insert_word_pairs.put(Constants.WORD_CATEGORY, word_type);
				insert_word_pairs.put(Constants.QUIZZED_FORWARD, "0");
				insert_word_pairs.put(Constants.QUIZZED_REVERSE, "0");
				db.insert(Constants.TABLE_NAME, null, insert_word_pairs);
			}
		} catch (java.io.IOException ioe) {
			Log.e("MyDBHelper", "FAILED Load on " + native_word + " & " + foreign_word);
		}
	}
	
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.w("Quizzn Spanish", "Upgrading from verson "+oldVersion+" to "+newVersion+" which will destroy old data.");
		db.execSQL("drop table if exists "+Constants.TABLE_NAME);
		onCreate(db);
	}
}
