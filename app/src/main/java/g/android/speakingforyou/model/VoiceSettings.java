package g.android.speakingforyou.model;

import android.util.Log;
import android.content.SharedPreferences;

import java.util.Locale;

public class VoiceSettings {

    private static final String LOG_TAG = "SFY : VoiceSettings";

    public static final int SORTBY_CHRONO = 0;
    public static final int SORTBY_USAGE = 1;
    public static final int SORTBY_ALPHA = 2;
    public static final int ORDERBY_DESC = 0;
    public static final int ORDERBY_ASC = 1;

    private SharedPreferences mSharedPreferences;

    private String mLastLanguageUsed;
    private int mLastPitchUsed;
    private int mLastSpeechRateUsed;
    private int mLastVoiceUsed;
    private String mLastVoiceNameUsed;
    private int mVoicesFound;
    private boolean mIsFirstBoot;

    private boolean mTalkMode;

    private int mThemeIndex;
    private boolean mIsAccentColorBought;

    private int mHistorySort;
    private int mHistoryOrder;
    private int mSavedSentencesSort;
    private int mSavedSentencesOrder;

    public VoiceSettings(SharedPreferences sharedPreferences)
    {
        mSharedPreferences = sharedPreferences;
    }

    public void setVoicesFound(int nbVoicesFound){
        mVoicesFound = nbVoicesFound;
        //Save the voice for next boot
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putInt("nbVoicesFound", mVoicesFound);
        editor.apply();
    }

    public int getVoicesFound(){
        //Initialize the last used voice
        int nbVoicesFound = mSharedPreferences.getInt("nbVoicesFound", -1);
        if (nbVoicesFound != -1) {
            mVoicesFound = nbVoicesFound;
            Log.i(LOG_TAG, "nbVoicesFound : " + nbVoicesFound);
        }
        else {
            setVoicesFound(0);
        }
        return mVoicesFound;
    }

    public void setLastVoiceUsed(int voiceIndex){
        mLastVoiceUsed = voiceIndex;
        //Save the voice for next boot
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putInt("voice", mLastVoiceUsed);
        editor.apply();
    }

    public int getLastVoiceUsed(){
        //Initialize the last used voice
        int voice = mSharedPreferences.getInt("voice", -1);
        if (voice != -1) {
            mLastVoiceUsed = voice;
            Log.i(LOG_TAG, "voice : " + voice);
        }
        else {
            setLastVoiceUsed(0);
        }
        return mLastVoiceUsed;
    }

    public void setLastVoiceNameUsed(String voiceName){
        mLastVoiceNameUsed = voiceName;
        //Save the voice for next boot
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString("voiceName", mLastVoiceNameUsed);
        editor.apply();
    }

    public String getLastVoiceNameUsed(){
        //Initialize the last used voice
        String voiceName = mSharedPreferences.getString("voiceName", null);
        if (voiceName != null) {
            mLastVoiceNameUsed = voiceName;
            Log.i(LOG_TAG, "voiceName : " + voiceName);
        }
        else {
            setLastVoiceNameUsed(null);
        }
        return mLastVoiceNameUsed;
    }

    public void setTheme(int themeIndex){
        mThemeIndex = themeIndex;
        //Save the Theme for next boot
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putInt("theme", mThemeIndex);
        editor.apply();
    }

    public int getTheme(){
        //Initialize the last used theme
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

    public void setHistorySort(int sorting){
        mHistorySort = sorting;
        //Save the Sorting for next boot
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putInt("historySort", mHistorySort);
        editor.apply();
    }

    public int getHistorySort(){
        //Initialize the last used historySort
        int historySort = mSharedPreferences.getInt("historySort", -1);
        if (historySort != -1) {
            mHistorySort = historySort;
            Log.i(LOG_TAG, "historySort : " + historySort);
        }
        else {
            setHistorySort(0);
        }
        return mHistorySort;
    }

    public void setHistoryOrder(int order){
        mHistoryOrder = order;
        //Save the Sorting for next boot
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putInt("historyOrder", mHistoryOrder);
        editor.apply();
    }

    public int getHistoryOrder(){
        //Initialize the last used historyOrder
        int historyOrder = mSharedPreferences.getInt("historyOrder", -1);
        if (historyOrder != -1) {
            mHistoryOrder = historyOrder;
            Log.i(LOG_TAG, "historyOrder : " + historyOrder);
        }
        else {
            setHistoryOrder(0);
        }
        return mHistoryOrder;
    }

    public void setSavedSentencesSort(int sorting){
        mSavedSentencesSort = sorting;
        //Save the Sorting for next boot
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putInt("savedSentencesSort", mSavedSentencesSort);
        editor.apply();
    }

    public int getSavedSentencesSort(){
        //Initialize the last used mSavedSentencesSort
        int savedSentencesSort = mSharedPreferences.getInt("savedSentencesSort", -1);
        if (savedSentencesSort != -1) {
            mSavedSentencesSort = savedSentencesSort;
            Log.i(LOG_TAG, "savedSentencesOrder : " + savedSentencesSort);
        }
        else {
            setSavedSentencesSort(0);
        }
        return mSavedSentencesSort;
    }

    public void setSavedSentencesOrder(int order){
        mSavedSentencesOrder = order;
        //Save the Sorting for next boot
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putInt("savedSentencesOrder", mSavedSentencesOrder);
        editor.apply();
    }

    public int getSavedSentencesOrder(){
        //Initialize the last used savedSentencesOrder
        int savedSentencesOrder = mSharedPreferences.getInt("savedSentencesOrder", -1);
        if (savedSentencesOrder != -1) {
            mSavedSentencesOrder = savedSentencesOrder;
            Log.i(LOG_TAG, "savedSentencesOrder : " + savedSentencesOrder);
        }
        else {
            setSavedSentencesOrder(0);
        }
        return mSavedSentencesOrder;
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
        mTalkMode = mSharedPreferences.getBoolean("isTalkMode", true);
        setTalkMode(mTalkMode);
        return mTalkMode;
    }


    public void setIsAccentColorBought(boolean isAccentColorBought){
        mIsAccentColorBought = isAccentColorBought;

        //Save the last language for next boot
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putBoolean("isAccentColorBought", mIsAccentColorBought);
        editor.apply();

        Log.i(LOG_TAG, "isAccentColorBought : " + mIsAccentColorBought );
    }

    public boolean getIsAccentColorBought(){
        mIsAccentColorBought = mSharedPreferences.getBoolean("isAccentColorBought", false);
        setIsAccentColorBought(mIsAccentColorBought);
        return mIsAccentColorBought;
    }

    public void setIsFirstBoot(boolean isFirstBoot){
        mIsFirstBoot = isFirstBoot;

        //Save the last language for next boot
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putBoolean("isFirstBoot", mIsFirstBoot);
        editor.apply();

        Log.i(LOG_TAG, "isFirstBoot : " + mIsFirstBoot );
    }

    public boolean getIsFirstBoot(){
        mIsFirstBoot = mSharedPreferences.getBoolean("isFirstBoot", true);
        setIsFirstBoot(mIsFirstBoot);
        return mIsFirstBoot;
    }
}
