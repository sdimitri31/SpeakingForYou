package g.android.speakingforyou.Model;

import android.util.Log;
import android.content.SharedPreferences;

import java.util.Locale;

public class VoiceSettings {

    private String mLastLanguageUsed;
    private int mLastPitchUsed;
    private int mLastSpeechRateUsed;
    private boolean mTalkMode;
    private SharedPreferences mSharedPreferences;

    public VoiceSettings(SharedPreferences sharedPreferences)
    {
        mSharedPreferences = sharedPreferences;

        //Initialize the last used language
        String selectedLanguage = mSharedPreferences.getString("selectedLanguage", null);
        if (selectedLanguage != null) {
            mLastLanguageUsed = selectedLanguage;
            Log.i("TTS", "Last language : " + mLastLanguageUsed);
        }
        else
        {
            setLanguage(Locale.getDefault().toLanguageTag());
        }

        //Initialize the last used pitch
        int pitch = mSharedPreferences.getInt("pitch", -1);
        if (pitch != -1) {
            mLastPitchUsed = pitch;
            Log.i("TTS", "Last pitch : " + pitch);
        }
        else {
            setPitch(100);
        }

        //Initialize the last used speech rate
        int speechRate = mSharedPreferences.getInt("speechRate", -1);
        if (speechRate != -1) {
            mLastSpeechRateUsed = speechRate;
            Log.i("TTS", "Last speech rate : " + speechRate);
        }
        else {
            setSpeechRate(100);
        }

        //Initialize the TalkMode
        boolean isTalkMode = mSharedPreferences.getBoolean("isTalkMode", false);
        setTalkMode(isTalkMode);

    }

    public void setPitch(int pitch){
        mLastPitchUsed = pitch;

        //Save the Pitch for next boot
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putInt("pitch", mLastPitchUsed);
        editor.apply();

        Log.i("TTS", "New pitch :" + mLastPitchUsed);
    }

    public int getPitch(){ return mLastPitchUsed; }

    public void setSpeechRate(int speechRate){
        mLastSpeechRateUsed = speechRate;

        //Save the Speech Rate for next boot
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putInt("speechRate", mLastSpeechRateUsed);
        editor.apply();

        Log.i("TTS", "New speech rate : " + mLastSpeechRateUsed);

    }

    public int getSpeechRate(){ return mLastSpeechRateUsed;}

    public void setLanguage(String language){
        mLastLanguageUsed = language;

        //Save the last language for next boot
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString("selectedLanguage", mLastLanguageUsed);
        editor.apply();

        Log.i("TTS", "New language : " + mLastLanguageUsed );

    }

    public String getLanguage() { return mLastLanguageUsed; }

    public void setTalkMode(boolean talkMode){
        mTalkMode = talkMode;

        //Save the last language for next boot
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putBoolean("isTalkMode", mTalkMode);
        editor.apply();

        Log.i("TTS", "New Talk Mode : " + mTalkMode );

    }

    public boolean getTalkMode(){ return mTalkMode; }
}
