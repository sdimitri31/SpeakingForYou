package g.android.speakingforyou.model;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHandler extends SQLiteOpenHelper {

    public static final String TABLE_NAME_SAVEDSENTENCES = "savedSentences";
    public static final String TABLE_NAME_HISTORY = "history";

    public static final String KEY = "id";
    public static final String SENTENCE = "sentence";
    public static final String POSITION = "position";
    public static final String DATEFORMAT = "dateFormat";
    public static final String USAGE = "usage";

    public static final String TABLE_CREATE_SAVEDSENTENCES = "CREATE TABLE " + TABLE_NAME_SAVEDSENTENCES + " (" +
            KEY + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            SENTENCE + " TEXT, " +
            POSITION + " INTERGER)";

    public static final String TABLE_CREATE_HISTORY = "CREATE TABLE " + TABLE_NAME_HISTORY + " (" +
            KEY + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            SENTENCE + " TEXT, " +
            DATEFORMAT + " TEXT, " +
            USAGE + " INTEGER)";

    public DatabaseHandler(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE_SAVEDSENTENCES);
        db.execSQL(TABLE_CREATE_HISTORY);
    }

    public static final String TABLE_DROP_SAVEDSENTENCES = "DROP TABLE IF EXISTS " + TABLE_NAME_SAVEDSENTENCES + ";";
    public static final String TABLE_DROP_HISTORY = "DROP TABLE IF EXISTS " + TABLE_NAME_HISTORY + ";";

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(TABLE_DROP_SAVEDSENTENCES);
        db.execSQL(TABLE_DROP_HISTORY);
        onCreate(db);
    }

}
