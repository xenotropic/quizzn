package net.xenotropic.quizznworldcap;

import net.xenotropic.quizznworldcap.Constants;

// import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.Cursor;
import android.util.Log;
import java.util.Random;


public class MyDB {
	private SQLiteDatabase db;
	private final Context context;
	private final MyDBhelper dbhelper;
	private final Random generator = new Random();
	private String quiz_direction = Constants.QUIZZED_FORWARD;  //determines whether the quiz is being done on the first or second set of words
	
	public MyDB (Context c) {
		context = c;
		dbhelper = new MyDBhelper (context, Constants.DATABASE_NAME, null, Constants.DATABASE_VERSION);
	}
	
	public void close() {
		db.close();	
	}

	public void open() throws SQLiteException {
		try {
			db = dbhelper.getWritableDatabase();
			Log.v ("MyDB", "getting writable database");
		} catch (SQLiteException ex) {
			Log.v ("Open database exception caught", ex.getMessage());
			db = dbhelper.getReadableDatabase();
		}
	}
	
	public String[] getCategories () {
		String[] categories = null;
		Cursor cur = db.query(true, Constants.TABLE_NAME, new String[]{Constants.WORD_CATEGORY}, null, null, null, null, null, null);  //  initial true makes distict
		cur.moveToFirst();
		categories = new String[cur.getCount()];
		// Log.w("My.DB.getCategories()", "categories count is " + cur.getCount());
		while (cur.isAfterLast() == false) {
			categories[cur.getPosition()] = cur.getString(0);   
			cur.moveToNext();
		}
		cur.close();
		return categories;
	}
	
	public String[][] getArrayForCategory (String category) {
		String[][] returnArray = null;	
		Cursor cur = db.query(Constants.TABLE_NAME, null, Constants.WORD_CATEGORY + "='" + category + "'", null, null, null, null);
		cur.moveToFirst();
		returnArray = new String[cur.getCount()][Constants.NUM_COLUMNS];
		int cur_pos = 0;
		int i; 
		while (cur.isAfterLast() == false) {
			cur_pos = cur.getPosition();
			for (i = 0; i < Constants.NUM_COLUMNS; i++) {
				returnArray[cur_pos][i]=cur.getString(i);
				// Log.w("MyDB loading", "position " + cur_pos + ", " + i);
			}
			cur.moveToNext();
		}
		cur.close();
		return returnArray;
	}
	/**
	 * Queries the database for a set of quiz words.    
	 * @return 
	 * A string array of six elements.
	 * String element [0] is the quiz word
	 * String element [1] is the quiz word answer
	 * String element [2] is the first bogus wrong answer
	 * String element [3] is the second bogus wrong answer
	 * String element [4] is the third bogus wrong answer
	 * String element [5] is the database key for the quiz word
	 */
	
	public String[] getQuizStrings (String category) {

		int cursor_pos_of_fwd_word = 0;
		int cursor_pos_of_rev_word = 1;
		
		if (quiz_direction == Constants.QUIZZED_FORWARD) {   // these determine whether the "quiz word" is from Constants.FIRST_WORD or SECOND_WORD
			cursor_pos_of_fwd_word = 1;
			cursor_pos_of_rev_word = 0;			
			// Log.w("database.getQuizStrings","getting quiz words forward with quiz_direction " + quiz_direction);
		} // else Log.w("database.getQuizStrings","getting quiz words reverse with quiz_direction " + quiz_direction);
		
		String right_words_query = "SELECT " + Constants.FIRST_WORD + ", " + Constants.SECOND_WORD + ", " + Constants.KEY_ID + 
		" FROM " + Constants.TABLE_NAME + " WHERE " + Constants.WORD_CATEGORY + "='" + category + "' AND " + quiz_direction +
		"!='1'";  // SQL to get unquizzed words

		Cursor right_words_cur = db.rawQuery (right_words_query, null);
		int num_words_to_quiz = right_words_cur.getCount();
		if (num_words_to_quiz == 0) return null;  // if no rows returned, this means everything in this category has been quizzed
		
		String[] quiz_vals = new String[6];  // initialize return array
		String right_word_db_key = null;
		right_words_cur.moveToPosition(generator.nextInt(num_words_to_quiz));

		quiz_vals[0] = right_words_cur.getString(cursor_pos_of_fwd_word);  // quiz word
		quiz_vals[1] = right_words_cur.getString(cursor_pos_of_rev_word);  // answer for quiz word 

		// sticking db key in here to update db if correct on first try; could make into a data struct with a String[] and int
		quiz_vals[5] = right_words_cur.getString(2);
		right_word_db_key = right_words_cur.getString(2);
		
		// Log.w("MyDB getQuizStrings - right word", "Quiz word is " + quiz_vals[0] + "; answer for quiz word is " + quiz_vals[1] + " and db key is " + quiz_vals[5]);
		
		// This SQL gets all rows for category, to use in generating false answers
		String wrong_words_query = "SELECT " + Constants.FIRST_WORD + ", " + Constants.SECOND_WORD + ", " + Constants.KEY_ID + 
		" FROM " + Constants.TABLE_NAME + " WHERE " + Constants.WORD_CATEGORY + "='" + category + "'";  

		Cursor wrong_words_cur = db.rawQuery(wrong_words_query, null);
		int max_keys = wrong_words_cur.getCount();
		String first_bogus_word_db_key = null;
		String second_bogus_word_db_key = null;
		String third_bogus_word_db_key = null;
		// get first bogus answer
		do {
			wrong_words_cur.moveToPosition(generator.nextInt(max_keys));
			quiz_vals[2] = wrong_words_cur.getString(cursor_pos_of_rev_word);
			// Log.w("MyDB getQuizStrings - wrong word 1", "Candidate word is " + quiz_vals[2] + "; key is " + wrong_words_cur.getString(2) + 
			//		" and db key is " + quiz_vals[5]);
			first_bogus_word_db_key = wrong_words_cur.getString(2);
		} while (right_word_db_key.equals(first_bogus_word_db_key));  	// keep trying until database keys are different
		
		// get second bogus answer 

		do {
			wrong_words_cur.moveToPosition(generator.nextInt(max_keys));
			quiz_vals[3] = wrong_words_cur.getString(cursor_pos_of_rev_word);
			second_bogus_word_db_key = wrong_words_cur.getString(2);
		} while (right_word_db_key.equals(second_bogus_word_db_key) || first_bogus_word_db_key.equals(second_bogus_word_db_key));  	// keep trying until database keys are different
		second_bogus_word_db_key = wrong_words_cur.getString(2);
		
		// get third bogus answer
		do {
			wrong_words_cur.moveToPosition(generator.nextInt(max_keys));
			quiz_vals[4] = wrong_words_cur.getString(cursor_pos_of_rev_word);
			third_bogus_word_db_key = wrong_words_cur.getString(2);
		} while (third_bogus_word_db_key.equals(right_word_db_key) || third_bogus_word_db_key.equals(first_bogus_word_db_key) 
				|| third_bogus_word_db_key.equals(second_bogus_word_db_key));  	// keep trying until database keys are different
		wrong_words_cur.close();
		right_words_cur.close();
		return quiz_vals;		
	}
	
	public String getScoreForCategory (String score_category) {
		String db_query_string = "SELECT " + quiz_direction + " FROM " + Constants.TABLE_NAME + " WHERE " + Constants.WORD_CATEGORY +
			"='" + score_category +"'";
		Cursor cur = db.rawQuery(db_query_string, null);
		cur.moveToFirst();
		int score_count = 0;
		int temp_score;
		while (cur.isAfterLast() == false) {
			temp_score = Integer.parseInt(cur.getString(0));
			score_count += temp_score;
			cur.moveToNext();
		}
		if (score_count < 0) score_count = 0;
		score_count *= 100;
		score_count = score_count / cur.getCount();
		cur.close();
		return Integer.toString(score_count);
	}
	
	public void setQuizDirection (String quizdir) {
		quiz_direction = quizdir;
	}
	
	public void updateDatabaseToMarkCorrect(String key) {
		int current_score;
		String fetch_score_sql = "SELECT " + quiz_direction + " FROM " + Constants.TABLE_NAME + " WHERE " + Constants.KEY_ID + "=" + key;
		Cursor cur = db.rawQuery(fetch_score_sql, null);
		cur.moveToFirst();
		String string_score = cur.getString(0);
		current_score = Integer.parseInt(string_score);
		if (current_score < 1) current_score++;
		String update_sql = "UPDATE " + Constants.TABLE_NAME + " SET " + quiz_direction + "=" + Integer.toString(current_score)+" WHERE " 
		+ Constants.KEY_ID + "=" + key;
		db.execSQL (update_sql);
		cur.close();
	}
	
	public void updateDataBaseToMarkMissed (String key) {
		int current_score;
		String fetch_score_sql = "SELECT " + quiz_direction + " FROM " + Constants.TABLE_NAME + " WHERE " + Constants.KEY_ID + "=" + key;
		Cursor cur = db.rawQuery(fetch_score_sql, null);
		cur.moveToFirst();
		String string_score = cur.getString(0);
		current_score = Integer.parseInt(string_score);
		if (current_score > -1) current_score--;
		String update_sql = "UPDATE " + Constants.TABLE_NAME + " SET " + quiz_direction + "=" + Integer.toString(current_score)+" WHERE " 
		+ Constants.KEY_ID + "=" + key;
		db.execSQL (update_sql);
		cur.close();
	}
	
	public void resetScore (String category) {
		String reset_score_sql = "UPDATE " + Constants.TABLE_NAME + " SET " + quiz_direction + "='0' WHERE " 
		+ Constants.WORD_CATEGORY + "='" + category + "'";
		db.execSQL (reset_score_sql);
	}
	
	/**
	 * @return the value of the autoincrement key
	 */
	
	public int maxKeyValue() {
		Cursor cur = db.rawQuery("SELECT MAX("+Constants.KEY_ID+") FROM " + Constants.TABLE_NAME, null);
		cur.moveToFirst();
		int max_val = cur.getInt (0);
		cur.close();
		return max_val;
	}
}
