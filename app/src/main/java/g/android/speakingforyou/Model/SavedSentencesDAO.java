package g.android.speakingforyou.Model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import g.android.speakingforyou.Model.SavedSentences;

public class SavedSentencesDAO extends DAOBase {
    public static final String TABLE_NAME = "savedSentences";
    public static final String KEY = "id";
    public static final String SENTENCE = "sentence";
    public static final String POSITION = "position";

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
        open();
        mDb.insert(SavedSentencesDAO.TABLE_NAME, null, value);
        close();
        Log.i("TTS", "New savedSentence : " + savedSentences.getSentence() + "  Position : " + savedSentences.getPosition() );
    }

    /**
     * @param id the id to delete
     */
    public void delete(long id) {
        open();
        mDb.delete(TABLE_NAME, KEY + " = ?", new String[] {String.valueOf(id)});
        close();

        Log.i("TTS", "DELETED ID : " + id );
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
        long key = c.getLong(0);
        String sentence = c.getString(1);
        int position = c.getInt(2);
        c.close();
        close();

        SavedSentences savedSentence = new SavedSentences (key, sentence, position);

        return savedSentence;
    }

    public List<SavedSentences> getAll() {
        List<SavedSentences> savedSentences =  new ArrayList<>();
        open();
        Cursor c = mDb.rawQuery("select * from " + TABLE_NAME , null);
        while (c.moveToNext()) {
            long key = c.getLong(0);
            String sentence = c.getString(1);
            int position = c.getInt(2);
            savedSentences.add(new SavedSentences(key, sentence, position));
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
            long key = c.getLong(0);
            String sentence = c.getString(1);
            int position = c.getInt(2);
            savedSentences.add(new SavedSentences(key, sentence, position));
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


        Log.i("TTS", "refreshDatabase ");
    }
}
