package g.android.speakingforyou.Model;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class Speaker {
    private TextToSpeech    textToSpeech;
    private VoiceSettings   mVoiceSettings;

    private List<String>    listLanguageName;   //Store the displayName to show to the user
    private List<String>    listLanguageTag;    //Store the LocaleTag to set up the TTS module

    public Speaker(final Context context, VoiceSettings voiceSettings)
    {
        mVoiceSettings = voiceSettings;
        listLanguageName = new ArrayList<>();
        listLanguageTag = new ArrayList<>();

        textToSpeech = new TextToSpeech(context, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    //Default TTS Initialization

                    //Language
                    int ttsLang = textToSpeech.setLanguage(Locale.forLanguageTag(mVoiceSettings.getLanguage()));
                    if (ttsLang == TextToSpeech.LANG_MISSING_DATA
                            || ttsLang == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.e("TTS", "The Language is not supported!");
                    } else {
                        Log.i("TTS", "Language Supported.");
                    }

                    //SpeechRate
                    setSpeechRate(mVoiceSettings.getSpeechRate());

                    //Pitch
                    setPitch(mVoiceSettings.getPitch());

                    //Get all available TTS languages
                    Locale[] locales = Locale.getAvailableLocales();

                    for (Locale locale : locales) {
                        int res = textToSpeech.isLanguageAvailable(locale);
                        if (res == TextToSpeech.LANG_COUNTRY_AVAILABLE) {
                            listLanguageTag.add(locale.toLanguageTag());
                            listLanguageName.add(locale.getDisplayName());
                            Log.i("TTS", "Language Tag : " + locale.toLanguageTag() + " DisplayName : " + locale.getDisplayName());
                        }
                    }

                    Log.i("TTS", "Initialization success.");

                }
                else {
                    Toast.makeText(context, "TTS Initialization failed!", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    public TextToSpeech getTextToSpeech(){ return textToSpeech; }

    public void setLanguage(String language){
        textToSpeech.setLanguage(Locale.forLanguageTag(language));
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
            Log.e("TTS", "Error in converting Text to Speech!");
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
