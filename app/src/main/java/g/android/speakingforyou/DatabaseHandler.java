package g.android.speakingforyou;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHandler extends SQLiteOpenHelper {

    public static final String TABLE_NAME = "savedSentences";
    public static final String KEY = "id";
    public static final String SENTENCE = "sentence";
    public static final String LANGUAGE = "language";
    public static final String PITCH = "pitch";
    public static final String SPEECHRATE = "speechrate";
    public static final String POSITION = "position";

    public static final String TABLE_CREATE = "CREATE TABLE " + TABLE_NAME + " (" +
            KEY + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            SENTENCE + " TEXT, " +
            LANGUAGE + " TEXT, " +
            PITCH + " INTEGER, " +
            SPEECHRATE + " INTEGER, " +
            POSITION + " INTERGER)";

    public DatabaseHandler(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE);
    }

    public static final String TABLE_DROP = "DROP TABLE IF EXISTS " + TABLE_NAME + ";";

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(TABLE_DROP);
        onCreate(db);
    }

}
