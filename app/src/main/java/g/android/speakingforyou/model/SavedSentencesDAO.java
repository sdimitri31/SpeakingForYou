package g.android.speakingforyou.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class SavedSentencesDAO extends DAOBase {

    private static final String LOG_TAG = "SFY : SavedSentencesDAO";
    private static final String TABLE_NAME = "savedSentences";
    private static final String KEY = "id";
    private static final String SENTENCE = "sentence";
    private static final String POSITION = "position";
    private static final String DATEFORMAT = "dateFormat";
    private static final String USAGE = "usage";

    private static final int ROWID_KEY = 0;
    private static final int ROWID_SENTENCE = 1;
    private static final int ROWID_POSITION = 2;
    private static final int ROWID_DATEFORMAT = 3;
    private static final int ROWID_USAGE = 4;

    public SavedSentencesDAO(Context context) {
        super(context);
    }


    /**
     * @param savedSentences the savedSentence to add
     */
    public void add(SavedSentences savedSentences) {
        ContentValues value = new ContentValues();
        value.put(SavedSentencesDAO.SENTENCE, savedSentences.getSentence());
        value.put(SavedSentencesDAO.POSITION, savedSentences.getPosition());
        value.put(SavedSentencesDAO.DATEFORMAT, savedSentences.getStringDate());
        value.put(SavedSentencesDAO.USAGE, savedSentences.getUsage());
        open();
        mDb.insert(SavedSentencesDAO.TABLE_NAME, null, value);
        close();
        Log.i(LOG_TAG, "New savedSentence : " + savedSentences.getSentence() + "  Position : " + savedSentences.getPosition() );
    }

    /**
     * @param id the id to delete
     */
    public void delete(long id) {
        open();
        mDb.delete(TABLE_NAME, KEY + " = ?", new String[] {String.valueOf(id)});
        close();

        Log.i(LOG_TAG, "DELETED ID : " + id );
    }

    public void deleteAll() {
        open();
        mDb.execSQL("delete from "+ TABLE_NAME);
        close();
    }

    /**
     * @param savedSentences the savedSentence to edit
     */
    public void update(SavedSentences savedSentences) {
        ContentValues value = new ContentValues();
        value.put(SavedSentencesDAO.SENTENCE, savedSentences.getSentence());
        value.put(SavedSentencesDAO.POSITION, savedSentences.getPosition());
        value.put(SavedSentencesDAO.DATEFORMAT, savedSentences.getStringDate());
        value.put(SavedSentencesDAO.USAGE, savedSentences.getUsage());
        open();
        mDb.update(TABLE_NAME, value, KEY  + " = ?", new String[] {String.valueOf(savedSentences.getId())});
        close();
    }

    /**
     * @param id the id of the savedSentence to get
     */
    public SavedSentences get(long id) {
        open();
        Cursor c = mDb.rawQuery("select * from " + TABLE_NAME + " where " + KEY + " = ?", new String[]{String.valueOf(id)});
        c.moveToFirst();
        long key = c.getLong(ROWID_KEY);
        String sentence = c.getString(ROWID_SENTENCE);
        int position = c.getInt(ROWID_POSITION);
        String dateFormat = c.getString(ROWID_DATEFORMAT);
        int usage = c.getInt(ROWID_USAGE);
        c.close();
        close();

        SavedSentences savedSentence = new SavedSentences (key, sentence, position, dateFormat, usage);

        return savedSentence;
    }

    public List<SavedSentences> getAll() {
        List<SavedSentences> savedSentences =  new ArrayList<>();
        open();
        Cursor c = mDb.rawQuery("select * from " + TABLE_NAME , null);
        while (c.moveToNext()) {
            long key = c.getLong(ROWID_KEY);
            String sentence = c.getString(ROWID_SENTENCE);
            int position = c.getInt(ROWID_POSITION);
            String dateFormat = c.getString(ROWID_DATEFORMAT);
            int usage = c.getInt(ROWID_USAGE);
            savedSentences.add(new SavedSentences(key, sentence, position, dateFormat, usage));
        }
        c.close();
        close();

        return savedSentences;
    }

    public int getNextPosition(){
        List<SavedSentences> savedSentences =  new ArrayList<>();
        open();
        Cursor c = mDb.rawQuery("select * from " + TABLE_NAME , null);
        while (c.moveToNext()) {
            long key = c.getLong(ROWID_KEY);
            String sentence = c.getString(ROWID_SENTENCE);
            int position = c.getInt(ROWID_POSITION);
            String dateFormat = c.getString(ROWID_DATEFORMAT);
            int usage = c.getInt(ROWID_USAGE);
            savedSentences.add(new SavedSentences(key, sentence, position, dateFormat, usage));
        }
        c.close();
        close();

        return savedSentences.size();

    }

    public void refreshDatabase(List<SavedSentences> savedSentencesList){
        open();
        mDb.delete(TABLE_NAME, null, null);
        close();
        for (SavedSentences savedSentence: savedSentencesList) {
            add(savedSentence);
        }


        Log.i(LOG_TAG, "refreshDatabase ");
    }
}
