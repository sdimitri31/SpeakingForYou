package g.android.speakingforyou;

import android.annotation.TargetApi;
import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private TextToSpeech    textToSpeech;
    private VoiceSettings   mVoiceSettings;
    private TableLayout     mTableLayout_SettingsToggle;
    private ImageView       mImageView_SettingsArrowUp;
    private ImageView       mImageView_SettingsArrowDown;
    private Button          mButton_StartSpeaking;
    private Button          mButton_StopSpeaking;
    private EditText        mEditText_Sentence;
    private Spinner         mSpinner_LanguageAvailable;
    private SeekBar         mSeekBar_Pitch;
    private SeekBar         mSeekBar_SpeechRate;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTableLayout_SettingsToggle =   findViewById(R.id.tableLayout_Settings);
        mImageView_SettingsArrowUp =    findViewById(R.id.imageView_SettingsArrowUp);
        mImageView_SettingsArrowDown =  findViewById(R.id.imageView_SettingsArrowDown);
        mButton_StartSpeaking =         findViewById(R.id.button_StartSpeaking);
        mButton_StopSpeaking =          findViewById(R.id.button_StopSpeaking);
        mSpinner_LanguageAvailable =    findViewById(R.id.spinner_LanguageAvailable);
        mSeekBar_Pitch =                findViewById(R.id.seekBar_Pitch);
        mSeekBar_SpeechRate =           findViewById(R.id.seekBar_SpeechRate);
        mEditText_Sentence =            findViewById(R.id.editText_Sentence);

        mVoiceSettings = new VoiceSettings(getPreferences(MODE_PRIVATE));

        textToSpeech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
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

                    //Initialize the spinner and related events
                    initSpinnerLanguages();

                    Log.i("TTS", "Initialization success.");

                }
                else {
                    Toast.makeText(getApplicationContext(), "TTS Initialization failed!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //Initialize the speech rate
        setPitch(mVoiceSettings.getPitch());
        mSeekBar_Pitch.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                setPitch(i);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        //Initialize the last used speech rate
        setSpeechRate(mVoiceSettings.getSpeechRate());
        mSeekBar_SpeechRate.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                setSpeechRate(i);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        mButton_StartSpeaking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                String data = mEditText_Sentence.getText().toString();
                Log.i("TTS", "button clicked: " + data);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    ttsGreater21(data);
                } else {
                    ttsUnder20(data);
                }
            }
        });

        mButton_StopSpeaking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                textToSpeech.stop();
            }
        });
    }

    public void onClickSettingsToggle(final View v){
        //Hiding the settings
        if(mTableLayout_SettingsToggle.isShown()){
            Animation fadeout = new ScaleAnimation(1.0f,1.0f,1.0f,0.0f);
            fadeout.setDuration(200);
            mTableLayout_SettingsToggle.startAnimation(fadeout);
            mTableLayout_SettingsToggle.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mTableLayout_SettingsToggle.setVisibility(View.GONE);
                    mImageView_SettingsArrowDown.setVisibility(View.VISIBLE);
                    mImageView_SettingsArrowUp.setVisibility(View.GONE);
                }
            }, 200);

        }
        //Showing the settings
        else{
            Animation fadeout = new ScaleAnimation(1.0f,1.0f,0.0f,1.0f);
            fadeout.setDuration(200);
            mTableLayout_SettingsToggle.startAnimation(fadeout);
            mTableLayout_SettingsToggle.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mTableLayout_SettingsToggle.setVisibility(View.VISIBLE);
                    mImageView_SettingsArrowDown.setVisibility(View.GONE);
                    mImageView_SettingsArrowUp.setVisibility(View.VISIBLE);
                }
            }, 0);
        }
    }

    private void setPitch(int pitchValue){
        float pitch = (float) pitchValue / 100;
        textToSpeech.setPitch(pitch);
        mSeekBar_Pitch.setProgress(pitchValue);
        mVoiceSettings.setPitch(pitchValue);
    }

    private void setSpeechRate(int speechRateValue){
        float speechRate = (float) speechRateValue / 100;
        textToSpeech.setSpeechRate(speechRate);
        mSeekBar_SpeechRate.setProgress(speechRateValue);
        mVoiceSettings.setSpeechRate(speechRateValue);
    }

    private void initSpinnerLanguages(){
        final List<Locale> localeList = new ArrayList<>();
        final List<String> listLanguageName = new ArrayList<>();

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

        //Put all languages in the spinner
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(getApplicationContext(),
                android.R.layout.simple_spinner_item, listLanguageName);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinner_LanguageAvailable.setAdapter(dataAdapter);

        //Initialize the last used language
        String selectedLanguage = mVoiceSettings.getLanguage();
        if (selectedLanguage != null) {
            Log.i("TTS", "Last selected language : " + selectedLanguage);
            mSpinner_LanguageAvailable.setSelection(dataAdapter.getPosition(selectedLanguage));
        }

        //Spinner onSelectedItemEvent
        mSpinner_LanguageAvailable.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                textToSpeech.setLanguage(localeList.get(position));

                //Save the last language for next boot
                mVoiceSettings.setLanguage(parent.getItemAtPosition(position).toString());

                Log.i("TTS", "Country code name : " + localeList.get(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
    }
}