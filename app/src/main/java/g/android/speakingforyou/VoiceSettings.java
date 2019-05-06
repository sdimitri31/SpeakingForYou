package g.android.speakingforyou;

import android.util.Log;
import android.content.SharedPreferences;

class VoiceSettings {

    private String mLastLanguageUsed;
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
            Log.i("TTS", "Last language : " + mLastLanguageUsed);
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

    void setPitch(int pitch)
    {
        mLastPitchUsed = pitch;

        //Save the Pitch for next boot
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putInt("pitch", mLastPitchUsed);
        editor.apply();

        Log.i("TTS", "New pitch :" + mLastPitchUsed);
    }

    int getPitch()
    {
        return mLastPitchUsed;
    }

    void setSpeechRate(int speechRate)
    {
        mLastSpeechRateUsed = speechRate;

        //Save the Speech Rate for next boot
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putInt("speechRate", mLastSpeechRateUsed);
        editor.apply();

        Log.i("TTS", "New speech rate : " + mLastSpeechRateUsed);

    }

    int getSpeechRate()
    {
        return mLastSpeechRateUsed;
    }

    void setLanguage(String language)
    {
        mLastLanguageUsed = language;

        //Save the last language for next boot
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString("selectedLanguage", mLastLanguageUsed);
        editor.apply();

        Log.i("TTS", "New language : " + mLastLanguageUsed );

    }

    String getLanguage()
    {
        return mLastLanguageUsed;
    }

}
