package net.xenotropic.quizznworldcap;

public class Constants {
	public static final String DATABASE_NAME="spanish_flashcards";
	public static final int DATABASE_VERSION=1;
	public static final String TABLE_NAME="words";
	public static final String KEY_ID="_id";  // column 1
	public static final String FIRST_WORD="first_word";  // column 2
	public static final String SECOND_WORD="second_word"; // column 3
	public static final String WORD_CATEGORY="word_category";  // column 4
	public static final String QUIZZED_FORWARD="score_forward";  // column 5 - subtract one when wrong, add one when right
	public static final String QUIZZED_REVERSE="score_reverse";  // column 6 - subtract one when wrong, add one when right
	public static final int NUM_COLUMNS=6;
}