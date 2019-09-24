package g.android.speakingforyou.model;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

public abstract class DAOBase {

    protected final static int VERSION = 6;
    protected final static String NOM = "database.db";

    protected SQLiteDatabase mDb = null;
    protected DatabaseHandler mHandler = null;

    public DAOBase(Context pContext) {
        this.mHandler = new DatabaseHandler(pContext, NOM, null, VERSION);
    }

    public SQLiteDatabase open() {

        mDb = mHandler.getWritableDatabase();
        return mDb;
    }

    public void close() {
        mDb.close();
    }

    public SQLiteDatabase getDb() {
        return mDb;
    }
}
