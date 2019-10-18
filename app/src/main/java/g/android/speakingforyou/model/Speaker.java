package g.android.speakingforyou.model;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.speech.tts.Voice;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class Speaker {
    private static final String LOG_TAG = "SFY : Speaker";

    private TextToSpeech    textToSpeech;
    private VoiceSettings   mVoiceSettings;

    private List<Voice>     mListVoices;
    private List<Locale>    mListLocales;
    private List<String>    listLanguageName;   //Store the displayName to show to the user
    private List<String>    listLanguageTag;    //Store the LocaleTag to set up the TTS module

    public Speaker(final Context context, VoiceSettings voiceSettings)
    {
        mVoiceSettings = voiceSettings;
        mListVoices = new ArrayList<>();
        mListLocales = new ArrayList<>();
        listLanguageName = new ArrayList<>();
        listLanguageTag = new ArrayList<>();

        textToSpeech = new TextToSpeech(context, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    //Default TTS Initialization

                    //Language
                    Locale lastLocale = Locale.forLanguageTag(mVoiceSettings.getLanguage());
                    int ttsLang = textToSpeech.setLanguage(lastLocale);
                    if (ttsLang == TextToSpeech.LANG_MISSING_DATA
                            || ttsLang == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.e(LOG_TAG, "The Language is not supported!");
                    } else {
                        Log.i(LOG_TAG, "Language Supported.");
                    }

                    setListVoices(lastLocale);
                    setVoice(mVoiceSettings.getLastVoiceUsed());

                    //SpeechRate
                    setSpeechRate(mVoiceSettings.getSpeechRate());

                    //Pitch
                    setPitch(mVoiceSettings.getPitch());

                    //Get all available TTS languages
                    Locale[] locales = Locale.getAvailableLocales();

                    for (Locale locale : locales) {
                        int res = textToSpeech.isLanguageAvailable(locale);
                        if (res == TextToSpeech.LANG_COUNTRY_AVAILABLE) {
                            mListLocales.add(locale);
                            listLanguageTag.add(locale.toLanguageTag());
                            listLanguageName.add(locale.getDisplayName());
                            Log.i(LOG_TAG, "Language Tag : " + locale.toLanguageTag() + " DisplayName : " + locale.getDisplayName());
                        }
                    }

                    Collections.sort(mListLocales, new Comparator<Locale>() {
                        @Override
                        public int compare(final Locale object1, final Locale object2) {
                            return object1.getDisplayName().compareToIgnoreCase(object2.getDisplayName());
                        }
                    });

                    Log.i(LOG_TAG, "Initialization success.");

                }
                else {
                    Toast.makeText(context, "TTS Initialization failed!", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    public void setVoice(int voiceID){
        if((mListVoices.size() > 0) && (mListVoices.size() > voiceID )) {
            textToSpeech.setVoice(mListVoices.get(voiceID));
            mVoiceSettings.setLastVoiceUsed(voiceID);
            mVoiceSettings.setLastVoiceNameUsed(mListVoices.get(voiceID).getName());
        }
        else{
            mVoiceSettings.setLastVoiceUsed(-1);
        }
    }

    public List<Voice> getListVoices(){
        return mListVoices;
    }


    private void setListVoices(Locale locale){
        mListVoices.clear();
        Log.i(LOG_TAG, "localeToLookFor :" + locale);

        try {
            //Fetching all voice for the selected locale
            for (Voice voice : textToSpeech.getVoices() ) {
                if(voice.getLocale().getDisplayName().equals(locale.getDisplayName())){
                    mListVoices.add(voice);
                    Log.i(LOG_TAG, "      MATCH     :" + voice.getLocale().getDisplayName());
                }
            }

            //If some voices are found => Sorting by Name
            Log.i(LOG_TAG, "mListVoices.size() :" + mListVoices.size());
            if(mListVoices.size() > 0) {
                Collections.sort(mListVoices, new Comparator<Voice>() {
                    @Override
                    public int compare(final Voice object1, final Voice object2) {
                        return object1.getName().compareToIgnoreCase(object2.getName());
                    }
                });
            }
        }
        catch (Exception e){

            Log.i(LOG_TAG, "Exception :" + e.getLocalizedMessage());
        }


        //Setup the setting to know if different voices are available
        mVoiceSettings.setVoicesFound(mListVoices.size());
    }

    public TextToSpeech getTextToSpeech(){ return textToSpeech; }


    public List<Locale> getListLocales(){
        return mListLocales;
    }

    public void setLanguage(String language){
        textToSpeech.setLanguage(Locale.forLanguageTag(language));
        setListVoices(Locale.forLanguageTag(language));
        //textToSpeech.setVoice(textToSpeech.getDefaultVoice());
    }

    public void setPitch(int pitchValue){
        float pitch = (float) pitchValue / 100;
        textToSpeech.setPitch(pitch);
    }

    public void setSpeechRate(int speechRateValue){
        float speechRate = (float) speechRateValue / 100;
        textToSpeech.setSpeechRate(speechRate);
    }

    public List<String> getListLanguageName(){return listLanguageName;}

    public List<String> getListLanguageTag(){return listLanguageTag;}

    public void ttsSpeak(String sentence){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ttsGreater21(sentence);
        } else {
            ttsUnder20(sentence);
        }
    }

    @SuppressWarnings("deprecation")
    private void ttsUnder20(String text) {
        HashMap<String, String> map = new HashMap<>();
        map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "MessageId");
        int speechStatus = textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, map);
        if (speechStatus == TextToSpeech.ERROR) {
            Log.e("TTS", "Error in converting Text to Speech!");
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void ttsGreater21(String text) {
        String utteranceId=this.hashCode() + "";
        int speechStatus2 = textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId);
        if (speechStatus2 == TextToSpeech.ERROR) {
            Log.e(LOG_TAG, "Error in converting Text to Speech!");
        }
    }

    public void ttsStop(){
        textToSpeech.stop();
    }

    public void destroy(){
        textToSpeech.stop();
        textToSpeech.shutdown();
    }
}
