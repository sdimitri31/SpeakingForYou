package g.android.speakingforyou;

import android.util.Log;
import android.content.SharedPreferences;

public class VoiceSettings {

    private String mlastLanguageUsed;
    private int mLastPitchUsed;
    private int mLastSpeechRateUsed;
    private SharedPreferences mSharedPreferences;

    VoiceSettings(SharedPreferences sharedPreferences)
    {
        mSharedPreferences = sharedPreferences;

        //Initialize the last used language
        String selectedLanguage = mSharedPreferences.getString("selectedLanguage", null);
        if (selectedLanguage != null) {
            setLanguage(selectedLanguage);
            Log.i("TTS", "Last language : " + mlastLanguageUsed);
        }

        //Initialize the last used pitch
        int pitch = mSharedPreferences.getInt("pitch", -1);
        Log.i("TTS", "Last pitch : " + pitch);
        if (pitch != -1) {
            setPitch(pitch);
        }
        else {
            setPitch(100);
        }

        //Initialize the last used speech rate
        int speechRate = mSharedPreferences.getInt("speechRate", -1);
        Log.i("TTS", "Last speech rate : " + speechRate);
        if (speechRate != -1) {
            setSpeechRate(speechRate);
        }
        else {
            setSpeechRate(100);
        }
    }

    public void setPitch(int pitch)
    {
        mLastPitchUsed = pitch;

        //Save the Pitch for next boot
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putInt("pitch", mLastPitchUsed);
        editor.apply();

        Log.i("TTS", "New pitch :" + mLastPitchUsed);
    }

    public int getPitch()
    {
        return mLastPitchUsed;
    }

    public void setSpeechRate(int speechRate)
    {
        mLastSpeechRateUsed = speechRate;

        //Save the Speech Rate for next boot
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putInt("speechRate", mLastSpeechRateUsed);
        editor.apply();

        Log.i("TTS", "New speech rate : " + mLastSpeechRateUsed);

    }

    public int getSpeechRate()
    {
        return mLastSpeechRateUsed;
    }

    public void setLanguage(String language)
    {
        mlastLanguageUsed = language;

        //Save the last language for next boot
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString("selectedLanguage", mlastLanguageUsed);
        editor.apply();

        Log.i("TTS", "New language : " + mlastLanguageUsed );

    }

    public String getLanguage()
    {
        return mlastLanguageUsed;
    }

}
