package g.android.speakingforyou;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class SavedSentencesDAO extends DAOBase {
    public static final String TABLE_NAME = "savedSentences";
    public static final String KEY = "id";
    public static final String SENTENCE = "sentence";
    public static final String LANGUAGE = "language";
    public static final String PITCH = "pitch";
    public static final String SPEECHRATE = "speechrate";

    public SavedSentencesDAO(Context context) {
        super(context);
    }


    /**
     * @param savedSentences the savedSentence to add
     */
    public void add(SavedSentences savedSentences) {
        ContentValues value = new ContentValues();
        value.put(SavedSentencesDAO.SENTENCE, savedSentences.getSentence());
        value.put(SavedSentencesDAO.LANGUAGE, savedSentences.getLanguage());
        value.put(SavedSentencesDAO.PITCH, savedSentences.getPitch());
        value.put(SavedSentencesDAO.SPEECHRATE, savedSentences.getSpeechRate());
        open();
        mDb.insert(SavedSentencesDAO.TABLE_NAME, null, value);
        close();
        Log.i("TTS", "New savedSentence : " + savedSentences.getSentence() + " Language : " + savedSentences.getLanguage() + " Pitch : " +
                savedSentences.getPitch() + " SpeechRate : " + savedSentences.getSpeechRate() + " ");
    }

    /**
     * @param id the id to delete
     */
    public void delete(long id) {
        open();
        mDb.delete(TABLE_NAME, KEY + " = ?", new String[] {String.valueOf(id)});
        close();
    }

    /**
     * @param savedSentences the savedSentence to edit
     */
    public void update(SavedSentences savedSentences) {
        ContentValues value = new ContentValues();
        value.put(SavedSentencesDAO.SENTENCE, savedSentences.getSentence());
        value.put(SavedSentencesDAO.LANGUAGE, savedSentences.getLanguage());
        value.put(SavedSentencesDAO.PITCH, savedSentences.getPitch());
        value.put(SavedSentencesDAO.SPEECHRATE, savedSentences.getSpeechRate());
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
        String language = c.getString(2);
        int pitch = c.getInt(3);
        int speechRate = c.getInt(4);
        c.close();
        close();

        SavedSentences savedSentence = new SavedSentences (key, sentence, language, pitch, speechRate);

        return savedSentence;
    }

    public List<SavedSentences> getAll() {
        List<SavedSentences> savedSentences =  new ArrayList<>();
        open();
        Cursor c = mDb.rawQuery("select * from " + TABLE_NAME , null);
        while (c.moveToNext()) {
            long key = c.getLong(0);
            String sentence = c.getString(1);
            String language = c.getString(2);
            int pitch = c.getInt(3);
            int speechRate = c.getInt(4);
            savedSentences.add(new SavedSentences(key, sentence, language, pitch, speechRate));
        }
        c.close();
        close();

        return savedSentences;
    }
}
