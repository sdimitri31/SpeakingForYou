package g.android.speakingforyou;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TableLayout;

public class MainActivity extends AppCompatActivity implements SavedSentencesFragment.OnButtonClickedListener{

    public static final String PREF_NAME = "SharedPrefs";

    private Speaker         mSpeaker;

    SavedSentencesFragment  mSavedSentencesFragment;
    private Boolean         mIsEditSavedSentences;

    //VoiceSettings
    private VoiceSettings   mVoiceSettings;
    private TableLayout     mTableLayout_SettingsToggle;
    private ImageView       mImageView_SettingsArrowUp;
    private ImageView       mImageView_SettingsArrowDown;
    private Spinner         mSpinner_LanguageAvailable;
    private SeekBar         mSeekBar_Pitch;
    private SeekBar         mSeekBar_SpeechRate;

    //Manual writing field
    private EditText        mEditText_Sentence;
    private Button          mButton_StartSpeaking;
    private Button          mButton_StopSpeaking;
    private Button          mButton_SaveSentence;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Start Activity without saved sentences editable
        SharedPreferences sharedPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("isEditSavedSentence", false);
        editor.apply();

        //Put the recycler with the saved sentences in the frameLayout
        FragmentManager fragmentManager = this.getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        mSavedSentencesFragment = new SavedSentencesFragment();
        fragmentTransaction.replace(R.id.frameLayoutFragment, mSavedSentencesFragment);
        fragmentTransaction.commit();

        //VoiceSettings
        mTableLayout_SettingsToggle =   findViewById(R.id.tableLayout_Settings);
        mImageView_SettingsArrowUp =    findViewById(R.id.imageView_SettingsArrowUp);
        mImageView_SettingsArrowDown =  findViewById(R.id.imageView_SettingsArrowDown);
        mSpinner_LanguageAvailable =    findViewById(R.id.spinner_LanguageAvailable);
        mSeekBar_Pitch =                findViewById(R.id.seekBar_Pitch);
        mSeekBar_SpeechRate =           findViewById(R.id.seekBar_SpeechRate);

        //Manual writing field
        mEditText_Sentence =            findViewById(R.id.editText_Sentence);
        mButton_StartSpeaking =         findViewById(R.id.button_StartSpeaking);
        mButton_StopSpeaking =          findViewById(R.id.button_StopSpeaking);
        mButton_SaveSentence =          findViewById(R.id.button_SaveSentence);

        mVoiceSettings = new VoiceSettings(getSharedPreferences(PREF_NAME, MODE_PRIVATE));

        //Initialize the TTS Engine and the Spinner
        mSpeaker = new Speaker(this, mSpinner_LanguageAvailable, mVoiceSettings);

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
                String sentence = mEditText_Sentence.getText().toString();
                mSpeaker.ttsSpeak(sentence);
                Log.i("TTS", "Play clicked: " + sentence);
            }
        });

        mButton_StopSpeaking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSpeaker.ttsStop();
            }
        });

        mButton_SaveSentence.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            String sentence = mEditText_Sentence.getText().toString();

            SavedSentences sentenceToSave = new SavedSentences(sentence, mVoiceSettings.getLanguage(), mVoiceSettings.getPitch(), mVoiceSettings.getSpeechRate());

            SavedSentencesDAO SSDAO = new SavedSentencesDAO(getApplicationContext());
            SSDAO.add(sentenceToSave);

            reloadSavedSentencesFragment();
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
        mSpeaker.setPitch(pitchValue);
        mSeekBar_Pitch.setProgress(pitchValue);
        mVoiceSettings.setPitch(pitchValue);
    }

    private void setSpeechRate(int speechRateValue){
        mSpeaker.setSpeechRate(speechRateValue);
        mSeekBar_SpeechRate.setProgress(speechRateValue);
        mVoiceSettings.setSpeechRate(speechRateValue);
    }

    private void reloadSavedSentencesFragment(){
        this.getSupportFragmentManager().beginTransaction()
                .replace(R.id.frameLayoutFragment, new SavedSentencesFragment())
                .commit();
        Log.i("TTS", "Reloading SavedSentences" );
    }

    // --------------
    // CallBack
    // --------------

    @Override
    public void onButtonClicked(View view) {
        Log.i("TTS","Button clicked ! ID : " + view.getId());
        if(view.getId() == R.id.sentence)
        {
            SavedSentences clickedSentence = (SavedSentences) view.getTag();

            Log.i("TTS","clickedSentence !  : " + clickedSentence.getSentence());
            mSpeaker.ttsSpeak(clickedSentence.getSentence());
        }
        else if(view.getId() == R.id.button_EditSavedSentences)
        {
            SharedPreferences sharedPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);

            //Reverse the value of the boolean to toggle the recycler
            mIsEditSavedSentences = sharedPreferences.getBoolean("isEditSavedSentence", false);
            mIsEditSavedSentences = !mIsEditSavedSentences;

            //Save the new value
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("isEditSavedSentence", mIsEditSavedSentences);
            editor.apply();

            //Restart the fragment properly
            reloadSavedSentencesFragment();
        }
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mSpeaker != null) {
            mSpeaker.destroy();
        }
    }
}