package g.android.speakingforyou.model;

import android.util.Log;
import android.content.SharedPreferences;

import java.util.Locale;

public class VoiceSettings {

    private static final String LOG_TAG = "SFY : VoiceSettings";
    private String mLastLanguageUsed;
    private int mLastPitchUsed;
    private int mLastSpeechRateUsed;
    private boolean mTalkMode;
    private SharedPreferences mSharedPreferences;
    public int mThemeIndex;

    public VoiceSettings(SharedPreferences sharedPreferences)
    {
        mSharedPreferences = sharedPreferences;

        //Initialize the last used language
        //getLanguage();

        //Initialize the last used pitch
        //getPitch();

        //Initialize the last used speech rate
        //getSpeechRate();

        //Initialize the TalkMode
        //getTalkMode();
    }

    public void setTheme(int themeIndex){
        mThemeIndex = themeIndex;
        //Save the Pitch for next boot
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putInt("theme", mThemeIndex);
        editor.apply();
    }

    public int getTheme(){
        //Initialize the last used pitch
        int theme = mSharedPreferences.getInt("theme", -1);
        if (theme != -1) {
            mThemeIndex = theme;
            Log.i(LOG_TAG, "theme : " + theme);
        }
        else {
            setTheme(0);
        }
        return mThemeIndex;
    }

    public void setPitch(int pitch){
        mLastPitchUsed = pitch;

        //Save the Pitch for next boot
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putInt("pitch", mLastPitchUsed);
        editor.apply();

        Log.i("TTS", "New pitch :" + mLastPitchUsed);
    }

    public int getPitch(){
        //Initialize the last used pitch
        int pitch = mSharedPreferences.getInt("pitch", -1);
        if (pitch != -1) {
            mLastPitchUsed = pitch;
            Log.i(LOG_TAG, "Last pitch : " + pitch);
        }
        else {
            setPitch(100);
        }
        return mLastPitchUsed;
    }

    public void setSpeechRate(int speechRate){
        mLastSpeechRateUsed = speechRate;

        //Save the Speech Rate for next boot
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putInt("speechRate", mLastSpeechRateUsed);
        editor.apply();

        Log.i(LOG_TAG, "New speech rate : " + mLastSpeechRateUsed);

    }

    public int getSpeechRate(){
        int speechRate = mSharedPreferences.getInt("speechRate", -1);
        if (speechRate != -1) {
            mLastSpeechRateUsed = speechRate;
            Log.i(LOG_TAG, "Last speech rate : " + speechRate);
        }
        else {
            setSpeechRate(100);
        }
        return mLastSpeechRateUsed;
    }

    public void setLanguage(String language){
        mLastLanguageUsed = language;

        //Save the last language for next boot
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString("selectedLanguage", mLastLanguageUsed);
        editor.apply();

        Log.i(LOG_TAG, "New language : " + mLastLanguageUsed );

    }

    public String getLanguage() {
        String selectedLanguage = mSharedPreferences.getString("selectedLanguage", null);
        if (selectedLanguage != null) {
            mLastLanguageUsed = selectedLanguage;
            Log.i(LOG_TAG, "Last language : " + mLastLanguageUsed);
        }
        else
        {
            setLanguage(Locale.getDefault().toLanguageTag());
        }
        return mLastLanguageUsed;
    }

    public void setTalkMode(boolean talkMode){
        mTalkMode = talkMode;

        //Save the last language for next boot
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putBoolean("isTalkMode", mTalkMode);
        editor.apply();

        Log.i(LOG_TAG, "New Talk Mode : " + mTalkMode );
    }

    public boolean getTalkMode(){
        mTalkMode = mSharedPreferences.getBoolean("isTalkMode", false);
        setTalkMode(mTalkMode);
        return mTalkMode;
    }
}
