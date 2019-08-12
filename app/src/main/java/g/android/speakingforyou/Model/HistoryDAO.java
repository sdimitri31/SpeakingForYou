package g.android.speakingforyou.Model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class HistoryDAO extends DAOBase {
    public static final String TABLE_NAME = "history";
    public static final String KEY = "id";
    public static final String SENTENCE = "sentence";

    public HistoryDAO(Context context) {
        super(context);
    }


    /**
     * @param sentence String to add to history
     */
    public void add(String sentence) {
        ContentValues value = new ContentValues();
        value.put(HistoryDAO.SENTENCE, sentence);
        open();
        mDb.insert(HistoryDAO.TABLE_NAME, null, value);
        close();
        Log.i("TTS", "New History : " + sentence);
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

    public List<String> getAll() {
        List<String> history =  new ArrayList<>();
        open();
        Cursor c = mDb.rawQuery("select * from " + TABLE_NAME + " ORDER BY " + KEY + " DESC", null);
        while (c.moveToNext()) {
            long key = c.getLong(0);
            String sentence = c.getString(1);

            history.add(sentence);
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

    public void refreshDatabase(List<String> historyList){
        open();
        mDb.delete(TABLE_NAME, null, null);
        close();
        for (String sentence : historyList) {
            add(sentence);
        }
    }
}
