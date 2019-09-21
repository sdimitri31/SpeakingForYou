package g.android.speakingforyou.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class HistoryDAO extends DAOBase {

    private static final String  LOG_TAG = "SFY : HistoryDAO";
    private static final int MAX_ENTRIES = 50;
    private static final String TABLE_NAME = "history";
    private static final String KEY = "id";
    private static final String SENTENCE = "sentence";

    public HistoryDAO(Context context) {
        super(context);
    }


    /**
     * @param history History to add
     */
    public void add(History history) {
        ContentValues value = new ContentValues();
        value.put(HistoryDAO.SENTENCE, history.getSentence());
        open();
        mDb.insert(HistoryDAO.TABLE_NAME, null, value);

        //Delete first element if there is more than MAX_ENTRIES in the database to prevent too many useless entries
        long count = DatabaseUtils.queryNumEntries(mDb, TABLE_NAME);
        if(count > MAX_ENTRIES){
            Cursor cursor = mDb.rawQuery("select * from " + TABLE_NAME + " ORDER BY " + KEY + " ASC", null);
            if(cursor.moveToFirst()) {
                String rowId = cursor.getString(cursor.getColumnIndex(KEY));
                mDb.delete(TABLE_NAME, KEY + "=?",  new String[]{rowId});
            }
        }
        close();
        Log.i(LOG_TAG, "New History : " + history.getSentence());
    }

    /**
     * @param id the id to delete
     */
    public void delete(long id) {
        open();
        mDb.delete(TABLE_NAME, KEY + " = ?", new String[] {String.valueOf(id)});
        close();
    }


    public void deleteAll() {
        open();
        mDb.execSQL("delete from "+ TABLE_NAME);
        close();
    }

    /**
     * @param id the id of the history to get
     */
    public String get(long id) {
        open();
        Cursor c = mDb.rawQuery("select * from " + TABLE_NAME + " where " + KEY + " = ?", new String[]{String.valueOf(id)});
        c.moveToFirst();
        String sentence = c.getString(1);
        c.close();
        close();

        return sentence;
    }

    public List<History> getAll() {
        List<History> history =  new ArrayList<>();
        open();
        Cursor c = mDb.rawQuery("select * from " + TABLE_NAME + " ORDER BY " + KEY + " DESC", null);
        while (c.moveToNext()) {
            long key = c.getLong(0);
            String sentence = c.getString(1);
            history.add(new History(key, sentence));
        }
        c.close();
        close();

        return history;
    }

    public String getLastHistory(){
        String sentence = "";
        open();
        Cursor c = mDb.rawQuery("select * from " + TABLE_NAME+ " ORDER BY " + KEY + " DESC" , null);
        if(c.getCount() > 0){
            c.moveToFirst();
            sentence = c.getString(1);
        }
        c.close();
        close();

        return sentence;

    }

    public void refreshDatabase(List<History> historyList){
        open();
        mDb.delete(TABLE_NAME, null, null);
        close();
        for (History history : historyList) {
            add(history);
        }
    }
}
