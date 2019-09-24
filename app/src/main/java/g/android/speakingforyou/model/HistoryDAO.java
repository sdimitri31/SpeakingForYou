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
    private static final String DATEFORMAT = "dateFormat";
    private static final String USAGE = "usage";


    private static final int ROWID_KEY = 0;
    private static final int ROWID_SENTENCE = 1;
    private static final int ROWID_DATEFORMAT = 2;
    private static final int ROWID_USAGE = 3;

    public HistoryDAO(Context context) {
        super(context);
    }


    /**
     * @param history History to add
     */
    public void add(History history) {
        ContentValues value = new ContentValues();
        value.put(HistoryDAO.SENTENCE, history.getSentence());
        value.put(HistoryDAO.DATEFORMAT, history.getStringDate());
        value.put(HistoryDAO.USAGE, history.getUsage());
        open();
        mDb.insert(HistoryDAO.TABLE_NAME, null, value);
        /*
        //Delete first element if there is more than MAX_ENTRIES in the database to prevent too many useless entries
        long count = DatabaseUtils.queryNumEntries(mDb, TABLE_NAME);
        if(count > MAX_ENTRIES){
            Cursor cursor = mDb.rawQuery("select * from " + TABLE_NAME + " ORDER BY " + KEY + " ASC", null);
            if(cursor.moveToFirst()) {
                String rowId = cursor.getString(cursor.getColumnIndex(KEY));
                mDb.delete(TABLE_NAME, KEY + "=?",  new String[]{rowId});
            }
        }*/
        close();
        Log.i(LOG_TAG, "New History : " + history.getSentence() + " getDateFormat " + history.getStringDate());
    }

    /**
     * @param history the savedSentence to edit
     */
    public void update(History history) {
        ContentValues value = new ContentValues();
        value.put(HistoryDAO.SENTENCE, history.getSentence());
        value.put(HistoryDAO.DATEFORMAT, history.getStringDate());
        value.put(HistoryDAO.USAGE, history.getUsage());
        open();
        Log.i(LOG_TAG, "update History : " + history.getSentence() + " getDateFormat " + history.getStringDate()+ " getUsage " + history.getUsage());
        mDb.update(TABLE_NAME, value, KEY  + " = ?", new String[] {String.valueOf(history.getId())});
        close();
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
    public History get(long id) {
        open();
        Cursor c = mDb.rawQuery("select * from " + TABLE_NAME + " where " + KEY + " = ?", new String[]{String.valueOf(id)});
        c.moveToFirst();
        String sentence = c.getString(ROWID_SENTENCE);
        String dateFormat = c.getString(ROWID_DATEFORMAT);
        int usage = c.getInt(ROWID_USAGE);
        c.close();
        close();
        History history = new History(id, sentence, dateFormat, usage);
        return history;
    }

    public List<History> getAll() {
        List<History> history =  new ArrayList<>();
        open();
        Cursor c = mDb.rawQuery("select * from " + TABLE_NAME + " ORDER BY " + KEY + " DESC", null);
        while (c.moveToNext()) {
            long key = c.getLong(ROWID_KEY);
            String sentence = c.getString(ROWID_SENTENCE);
            String dateFormat = c.getString(ROWID_DATEFORMAT);
            int usage = c.getInt(ROWID_USAGE);
            history.add(new History(key, sentence, dateFormat, usage));
            //Log.i(LOG_TAG, "getAll : " + key + " sentence " + sentence + " dateFormat " + dateFormat + " usage " + usage);
        }
        c.close();
        close();

        return history;
    }

    public History getLastHistory(){
        long key = 0;
        String sentence = "";
        String dateFormat = "";
        int usage = 0;
        open();
        Cursor c = mDb.rawQuery("select * from " + TABLE_NAME+ " ORDER BY " + KEY + " DESC" , null);
        if(c.getCount() > 0){
            c.moveToFirst();
            key = c.getLong(ROWID_KEY);
            sentence = c.getString(ROWID_SENTENCE);
            dateFormat = c.getString(ROWID_DATEFORMAT);
            usage = c.getInt(ROWID_USAGE);
        }
        c.close();
        close();

        History history = new History(key, sentence, dateFormat, usage);

        return history;

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
