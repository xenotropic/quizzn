package net.xenotropic.quizznworldcap;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import net.xenotropic.quizznworldcap.MyDB;

public class Quizzer extends Activity {

	private Button tl_button;
	private Button tr_button;
	private Button bl_button;
	private Button br_button;
	private TextView word_view;
	private int screen_width = 300;
	private int screen_height = 200;
	private Integer[] shuffle_order = new Integer[4];
	private int correct_value = 1;
	private MyDB dba;
	private String category; // each Quizzer only does one category
	private boolean first_answer = true;
	private boolean forward_direction = true;
	private String quiz_word_db_key;
	private ProgressBar progress_bar;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		android.view.Display display = ((android.view.WindowManager)getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay(); 
		screen_width = display.getWidth();
		screen_height = display.getHeight();
		dba = new MyDB(this);
		dba.open();
		SharedPreferences prefs = getSharedPreferences("quiz_storage", MODE_PRIVATE);
		String quiz_direction = prefs.getString("direction", "");
		dba.setQuizDirection(quiz_direction);
		forward_direction = quiz_direction.equals(Constants.QUIZZED_FORWARD);
		for (int i = 0; i < shuffle_order.length; i++) {  // initializing to 0,1,2,3
			shuffle_order[i] = i + 1; 
		} 		
		category = getIntent().getStringExtra("category");
		startQuiz(category);
	}
	
	public void onPause () {
		dba.close();
		super.onPause();
	}
	
	public void onResume () {
		dba.open();
		super.onResume();
	}
	
	public void onDestroy () {
		dba.close();
		super.onDestroy();
	}
	
	protected void startQuiz (String category) {
		
		RelativeLayout quiz_view;
		quiz_view = new RelativeLayout(this);
		quiz_view.setLayoutParams ( new LinearLayout.LayoutParams (LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT, (float)0.9) );
		tl_button = new Button (getApplicationContext());
		tr_button = new Button (getApplicationContext());
		bl_button = new Button (getApplicationContext());
		br_button = new Button (getApplicationContext());
		word_view = new TextView (this);
		word_view.setText ("Quiz Word");
		word_view.setGravity(android.view.Gravity.CENTER);
		progress_bar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
		progress_bar.setIndeterminate(false);
		progress_bar.setProgress(Integer.parseInt(dba.getScoreForCategory(category)));
		
		tl_button.setText ("Top Left This is some really long run on text that someone made way too long");
		tr_button.setText ("Top Right");
		bl_button.setText ("Bot Left");
		br_button.setText ("Bot Right");
		
		// adding button listener to each button so we can do stuff when the button is pressed
		
		tl_button.setOnClickListener (new QuizButtonListener (this, tl_button));
		tr_button.setOnClickListener (new QuizButtonListener (this, tr_button));
		bl_button.setOnClickListener (new QuizButtonListener (this, bl_button));
		br_button.setOnClickListener (new QuizButtonListener (this, br_button));

		// need to make sure that each of these buttons has IDs so the parent view can know what they are
		
		tl_button.setId(0);
		tr_button.setId(1);
		bl_button.setId(2);
		br_button.setId(3);

		//  create layout parameter objects for each of the buttons
		
		RelativeLayout.LayoutParams tl_lp = new RelativeLayout.LayoutParams(
	            LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		RelativeLayout.LayoutParams tr_lp = new RelativeLayout.LayoutParams(
	            LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		RelativeLayout.LayoutParams bl_lp = new RelativeLayout.LayoutParams(
	            LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		RelativeLayout.LayoutParams br_lp = new RelativeLayout.LayoutParams(
	            LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		RelativeLayout.LayoutParams word_lp = new RelativeLayout.LayoutParams(
	            LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		RelativeLayout.LayoutParams pb_lp = new RelativeLayout.LayoutParams(
	            LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		
		tr_lp.addRule (RelativeLayout.ALIGN_PARENT_RIGHT);
		tl_lp.addRule (RelativeLayout.ALIGN_PARENT_LEFT);

		bl_lp.addRule (RelativeLayout.ALIGN_PARENT_LEFT);
		br_lp.addRule (RelativeLayout.ALIGN_PARENT_RIGHT);
		bl_lp.addRule (RelativeLayout.ALIGN_PARENT_BOTTOM);
		br_lp.addRule (RelativeLayout.ALIGN_PARENT_BOTTOM);

		word_lp.addRule(RelativeLayout.CENTER_IN_PARENT);
		
		pb_lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		
		int button_width = (int)(screen_width / 2.2);
		int button_height = (int)(screen_height / 3.2);

		tl_button.setWidth(button_width);
		tr_button.setWidth(button_width);
		bl_button.setWidth(button_width);
		br_button.setWidth(button_width);
		
		tl_button.setHeight(button_height);
		tr_button.setHeight(button_height);
		bl_button.setHeight(button_height);
		br_button.setHeight(button_height);
		
//		Log.w ("Quiz setup", "screen width is " + screen_width);
		int text_size = screen_width/28;
		
		tl_button.setTextSize(text_size);
		tr_button.setTextSize(text_size);
		bl_button.setTextSize(text_size);
		br_button.setTextSize(text_size);
		word_view.setTextSize((int)(text_size*1.35));
		word_view.setTextColor(Color.RED);
		
		quiz_view.setGravity(Gravity.FILL);
		
		quiz_view.addView (tl_button, tl_lp);
		quiz_view.addView (tr_button, tr_lp);
		quiz_view.addView (bl_button, bl_lp);
		quiz_view.addView (br_button, br_lp);
		quiz_view.addView (word_view, word_lp);
//		quiz_view.addView (progress_bar, pb_lp);

		progress_bar.setLayoutParams(new LinearLayout.LayoutParams (LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT, (float)0.1));
		LinearLayout l_layout = new LinearLayout (this);
		l_layout.setLayoutParams ( new LinearLayout.LayoutParams (LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT) );
		l_layout.setOrientation (LinearLayout.VERTICAL);
		l_layout.addView (quiz_view);
		l_layout.addView (progress_bar);
		
		progress_bar.getLayoutParams().width = screen_width;
		progress_bar.invalidate();
		
		
		//TODO: add in a Menu for option to reset score (in top menu)
		//TODO: add in a Menu for haptic feedback
		this.setContentView (l_layout);
		newQuestion();
	}
	
	protected void newQuestion () {
		String[] quizVals = dba.getQuizStrings(category);  // position zero is the quiz word, position one is correct answer, rest are decoys
		
		if (quizVals == null) {
			Toast.makeText(getApplicationContext(), "Quiz Complete!", Toast.LENGTH_SHORT).show();
			finish();
			return;
		}
		
		word_view.setText(quizVals[0]);

		java.util.Collections.shuffle(java.util.Arrays.asList(shuffle_order));   // this shuffles the shuffle_order Integer array
		
		tl_button.setBackgroundColor (android.graphics.Color.LTGRAY);
		tr_button.setBackgroundColor (android.graphics.Color.LTGRAY);
		bl_button.setBackgroundColor (android.graphics.Color.LTGRAY);
		br_button.setBackgroundColor (android.graphics.Color.LTGRAY);
				
		tl_button.setText(quizVals[shuffle_order[0].intValue()]);
		tr_button.setText(quizVals[shuffle_order[1].intValue()]);
		bl_button.setText(quizVals[shuffle_order[2].intValue()]);
		br_button.setText(quizVals[shuffle_order[3].intValue()]);
		
		// sticking db key in here to update db if correct on first try
		quiz_word_db_key = quizVals[5]; 
		
		for (int i = 0; i < 4; i++) {
			if (shuffle_order[i] == 1) {  // because position one is the correct answer
				correct_value = i;
			}
		}
	}
	
	protected void quizButtonClicked (Button button) {
		if (button.getId() != correct_value) {
			button.setBackgroundColor (android.graphics.Color.RED);
			first_answer = false;
		} else {
			button.setBackgroundColor (android.graphics.Color.GREEN);
			button.invalidate();
				if (forward_direction) {
					Toast.makeText(getApplicationContext(), "Correct! " + capitalizeFirstLetter(button.getText().toString()) + " is the capital of " + word_view.getText() + ".", Toast.LENGTH_SHORT).show();	
				} else {
					Toast.makeText(getApplicationContext(), "Correct! " + capitalizeFirstLetter(word_view.getText().toString()) + " is the capital of " + button.getText() + ".", Toast.LENGTH_SHORT).show();		
				}
			if (first_answer) {
				dba.updateDatabaseToMarkCorrect(quiz_word_db_key); 
				progress_bar.setProgress(Integer.parseInt(dba.getScoreForCategory(category)));
				//Log.w ("Quiz Button First Correct Answer", button.getText() + " marking key " + quiz_word_db_key + " correct in database");
			} else {
				dba.updateDataBaseToMarkMissed (quiz_word_db_key);
				progress_bar.setProgress(Integer.parseInt(dba.getScoreForCategory(category)));
			}
			newQuestion();
			first_answer = true;
		}
	}
	
	private String capitalizeFirstLetter (String s) {
		return Character.toUpperCase(s.charAt(0)) + s.substring(1);
	}
	
	private class QuizButtonListener implements OnClickListener {
		private Button button;
		private Quizzer app;
		
		public QuizButtonListener (Quizzer f, Button b) {
			button = b;
			app = f;
		}
		
		public void onClick (View v) {
			// Log.w ("QuizButtonListener", "You clicked "+position+"!");
			app.quizButtonClicked(button);
		}
	}
}