package g.android.speakingforyou;

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
    private Context         mContext;
    private Spinner         mSpinner_LanguageAvailable;
    private VoiceSettings   mVoiceSettings;

    private List<Locale>    localeList;
    private List<String>    listLanguageName;

    public Speaker(final Context context, Spinner language, VoiceSettings voiceSettings)
    {
        mContext = context;
        mSpinner_LanguageAvailable = language;
        mVoiceSettings = voiceSettings;
        localeList = new ArrayList<>();
        listLanguageName = new ArrayList<>();

        textToSpeech = new TextToSpeech(context, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    //Default TTS Initialization
                    int ttsLang = textToSpeech.setLanguage(Locale.getDefault());
                    if (ttsLang == TextToSpeech.LANG_MISSING_DATA
                            || ttsLang == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.e("TTS", "The Language is not supported!");
                    } else {
                        Log.i("TTS", "Language Supported.");
                    }

                    //Get all available TTS languages
                    Locale[] locales = Locale.getAvailableLocales();

                    for (Locale locale : locales) {
                        int res = textToSpeech.isLanguageAvailable(locale);
                        if (res == TextToSpeech.LANG_COUNTRY_AVAILABLE) {
                            localeList.add(locale);
                            listLanguageName.add(locale.getDisplayName());
                            Log.i("TTS", "Locale : " + locale + " DisplayName : " + locale.getDisplayName());
                        }
                    }

                    //Initialize the spinner and related events
                    initSpinnerLanguages();

                    Log.i("TTS", "Initialization success.");

                }
                else {
                    Toast.makeText(context, "TTS Initialization failed!", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    public Speaker(final Context context, final SavedSentences savedSentences)
    {
        textToSpeech = new TextToSpeech(context, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    //Default TTS Initialization
                    int ttsLang = textToSpeech.setLanguage(Locale.forLanguageTag(savedSentences.getLanguage()));
                    if (ttsLang == TextToSpeech.LANG_MISSING_DATA
                            || ttsLang == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.e("TTS", "The Language is not supported!");
                    } else {
                        Log.i("TTS", "Language Supported.");
                    }

                    setPitch(savedSentences.getPitch());
                    setSpeechRate(savedSentences.getSpeechRate());
                    ttsSpeak(savedSentences.getSentence());

                    Log.i("TTS", "Initialization success.");
                }
                else {
                    Toast.makeText(context, "TTS Initialization failed!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void initSpinnerLanguages(){

        //Put all languages in the spinner
        ArrayAdapter<String> arrayAdapterLanguages = new ArrayAdapter<>(mContext,
                android.R.layout.simple_spinner_item, listLanguageName);
        arrayAdapterLanguages.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinner_LanguageAvailable.setAdapter(arrayAdapterLanguages);

        //Initialize the last used language
        String selectedLanguage = mVoiceSettings.getLanguage();
        if (selectedLanguage != null) {
            Log.i("TTS", "Last selected language : " + selectedLanguage);
            Locale loc = Locale.forLanguageTag(selectedLanguage);

            Log.i("TTS", "Last selected language displayed name: " + loc.getDisplayName());
            mSpinner_LanguageAvailable.setSelection(arrayAdapterLanguages.getPosition(loc.getDisplayName()));
        }

        //Spinner onSelectedItemEvent
        mSpinner_LanguageAvailable.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                setLanguage(getLocaleList().get(position));

                //Save the last language for next boot
                mVoiceSettings.setLanguage(getLocaleList().get(position).toLanguageTag());

                Log.i("TTS", "Display name : " + getLocaleList().get(position).getDisplayName());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

    }

    public void setLanguage(Locale language){
        textToSpeech.setLanguage(language);
    }

    public void setPitch(int pitchValue){
        float pitch = (float) pitchValue / 100;
        textToSpeech.setPitch(pitch);
    }

    public void setSpeechRate(int speechRateValue){
        float speechRate = (float) speechRateValue / 100;
        textToSpeech.setSpeechRate(speechRate);
    }

    public List<Locale> getLocaleList() {
        return localeList;
    }

    public List<String> getListLanguageName(){return listLanguageName;}

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
