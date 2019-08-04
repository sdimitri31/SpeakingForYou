package g.android.speakingforyou.Controller;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.speech.tts.UtteranceProgressListener;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.view.inputmethod.EditorInfo;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TextView;

import g.android.speakingforyou.Model.HistoryDAO;
import g.android.speakingforyou.Model.SavedSentences;
import g.android.speakingforyou.Model.SavedSentencesDAO;
import g.android.speakingforyou.Model.Speaker;
import g.android.speakingforyou.Model.VoiceSettings;
import g.android.speakingforyou.R;
import g.android.speakingforyou.View.HistoryFragment;
import g.android.speakingforyou.View.SavedSentencesFragment;

public class MainActivity extends AppCompatActivity implements SavedSentencesFragment.OnButtonClickedListener, HistoryFragment.OnButtonClickedListener {

    public static final String PREF_NAME = "SharedPrefs";

    public static final String MENUTOP_SAVEDSENTENCES = "SavedSentences";
    public static final String MENUTOP_HISTORY = "History";
    public static final String MENUTOP_SETTINGS = "Settings";

    private Speaker mSpeaker;

    //Menu Top
    private ImageView mMenuTop_SavedSentences;
    private ImageView mMenuTop_History;
    private ImageView mMenuTop_Settings;

    //VoiceSettings
    private VoiceSettings mVoiceSettings;
    private TableLayout     mTableLayout_SettingsToggle;
    private ImageView       mImageView_SettingsArrowUp;
    private ImageView       mImageView_SettingsArrowDown;
    private Spinner         mSpinner_LanguageAvailable;
    private SeekBar         mSeekBar_Pitch;
    private SeekBar         mSeekBar_SpeechRate;
    private CheckBox        mCheckBox_TalkMode;

    //Manual writing field
    private EditText    mEditText_Sentence;
    private ImageButton mButton_DeleteText;
    private ImageButton mButton_SaveSentence;
    private ImageButton     mButton_ToggleSpeaking;

    private Boolean         mIsSpeaking;


    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_2);

        //-----------------------------------------------------------------------------------
        //                          Initializing UI ID's
        //-----------------------------------------------------------------------------------

        // MENU TOP ELEMENTS
        mMenuTop_SavedSentences     =   findViewById(R.id.imageView_MenuTop_SavedSentences);
        mMenuTop_History            =   findViewById(R.id.imageView_MenuTop_History);
        mMenuTop_Settings           =   findViewById(R.id.imageView_MenuTop_QuickSettings);

        // EDIT TEXT ELEMENTS
        mEditText_Sentence          =   findViewById(R.id.editText_Sentence);
        mButton_DeleteText          =   findViewById(R.id.imageButton_DeleteText);
        mButton_SaveSentence        =   findViewById(R.id.imageButton_AddSentence);

        // PLAY/STOP BUTTON
        mButton_ToggleSpeaking      =   findViewById(R.id.imageButton_PlayStopToggle);

        //VoiceSettings
        /*
        mTableLayout_SettingsToggle =   findViewById(R.id.tableLayout_Settings);
        mImageView_SettingsArrowUp =    findViewById(R.id.imageView_SettingsArrowUp);
        mImageView_SettingsArrowDown =  findViewById(R.id.imageView_SettingsArrowDown);
        mSpinner_LanguageAvailable =    findViewById(R.id.spinner_LanguageAvailable);
        mSeekBar_Pitch =                findViewById(R.id.seekBar_Pitch);
        mSeekBar_SpeechRate =           findViewById(R.id.seekBar_SpeechRate);
        mCheckBox_TalkMode =            findViewById(R.id.checkBox_TalkMode);
        */


        //-----------------------------------------------------------------------------------
        //                 Initializing OBJECTS and DEFAULT SETTINGS
        //-----------------------------------------------------------------------------------

        mVoiceSettings  = new VoiceSettings(getSharedPreferences(PREF_NAME, MODE_PRIVATE));
        mSpeaker        = new Speaker(this, mVoiceSettings);
        mIsSpeaking     = false;

        final SavedSentencesDAO savedSentencesDAO = new SavedSentencesDAO(this);

        mButton_DeleteText.setVisibility(View.INVISIBLE);
        mButton_SaveSentence.setVisibility(View.INVISIBLE);

        loadFragment(MENUTOP_SAVEDSENTENCES);


        //-----------------------------------------------------------------------------------
        //                          Initializing Listeners
        //-----------------------------------------------------------------------------------


        // MENU TOP Listeners

        mMenuTop_SavedSentences.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadFragment(MENUTOP_SAVEDSENTENCES);
            }
        });

        mMenuTop_History.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadFragment(MENUTOP_HISTORY);
            }
        });


        //-----------------------------------------------------------------------------------

        // EDIT TEXT Listeners

        //If EnterKey pressed
        mEditText_Sentence.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    Log.v("TTS","Enter Key Pressed!");
                    speak();
                    return true;
                } else {
                    return false;
                }
            }
        });

        //Show/Hide Delete and Save Button depending on text in editText
        mEditText_Sentence.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                if(count > 0)
                {
                    mButton_DeleteText.setVisibility(View.VISIBLE);
                    mButton_SaveSentence.setVisibility(View.VISIBLE);
                }
                else
                {
                    mButton_DeleteText.setVisibility(View.INVISIBLE);
                    mButton_SaveSentence.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });

        mButton_DeleteText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mEditText_Sentence.setText("");
            }
        });

        mButton_SaveSentence.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String sentence = mEditText_Sentence.getText().toString();

                SavedSentences sentenceToSave = new SavedSentences(sentence,
                        savedSentencesDAO.getNextPosition()
                );

                savedSentencesDAO.add(sentenceToSave);

                loadFragment(MENUTOP_SAVEDSENTENCES);
            }
        });

        //-----------------------------------------------------------------------------------

        // PLAY/STOP BUTTON Listeners

        mButton_ToggleSpeaking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mIsSpeaking){
                    ttsStop();
                }
                else{
                    speak();
                }
            }
        });

        mSpeaker.getTextToSpeech().setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override
            public void onStart(String s) {
                setIsSpeaking(true);
            }

            @Override
            public void onDone(String s) {
                Log.v("TTS","Finish talking!");
                setIsSpeaking(false);
            }

            @Override
            public void onError(String s) {}
        });

        /*
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

        //Initialize TalkMode
        setTalkMode(mVoiceSettings.getTalkMode());
        mCheckBox_TalkMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setTalkMode(mCheckBox_TalkMode.isChecked());
            }
        });

        */
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

    private void setTalkMode(boolean isTalkMode){
        mCheckBox_TalkMode.setChecked(isTalkMode);
        mVoiceSettings.setTalkMode(isTalkMode);
    }


    //-----------------------------------------------------------------------------------
    //                          SPEAKING METHODS
    //-----------------------------------------------------------------------------------

    private void setIsSpeaking(final Boolean isSpeaking){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                if (isSpeaking) {
                    mButton_ToggleSpeaking.setImageResource(R.drawable.stop);
                }
                else {
                    mButton_ToggleSpeaking.setImageResource(R.drawable.play);
                }

                mIsSpeaking = isSpeaking;
            }
        });
    }

    private void speak(){
        String sentence = mEditText_Sentence.getText().toString();
        mSpeaker.ttsSpeak(sentence);
        mButton_ToggleSpeaking.setImageResource(R.drawable.stop);

        final HistoryDAO historyDAO = new HistoryDAO(this);
        historyDAO.add(sentence);

        setIsSpeaking(true);

        if(mVoiceSettings.getTalkMode()){
            mEditText_Sentence.setText("");
        }
    }

    private void ttsStop(){
        mSpeaker.ttsStop();
        setIsSpeaking(false);
    }


    //-----------------------------------------------------------------------------------
    //                          FRAGMENTS METHODS
    //-----------------------------------------------------------------------------------

    /**
     *
     * @param fragment Load the fragment and set the colors of the MenuTop
     */
    private void loadFragment(String fragment){
        switch (fragment)
        {
            case MENUTOP_SAVEDSENTENCES:
                this.getSupportFragmentManager().beginTransaction()
                        .replace(R.id.FrameLayout_Fragments, new SavedSentencesFragment())
                        .commit();
                break;

            case MENUTOP_HISTORY:
                this.getSupportFragmentManager().beginTransaction()
                        .replace(R.id.FrameLayout_Fragments, new HistoryFragment())
                        .commit();
                break;

            case MENUTOP_SETTINGS:
                //TODO: Create SettingFragment
                break;
        }
        setActiveMenuTop(fragment);
        Log.i("TTS", "Reloading fragment : " + fragment );
    }

    /**
     *
     * @param activeMenuTop Set the colors of the MenuTop
     */
    private void setActiveMenuTop(final String activeMenuTop){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mMenuTop_Settings.setBackgroundColor(getResources().getColor(R.color.colorMenuTopInactive));
                mMenuTop_History.setBackgroundColor(getResources().getColor(R.color.colorMenuTopInactive));
                mMenuTop_SavedSentences.setBackgroundColor(getResources().getColor(R.color.colorMenuTopInactive));

                switch (activeMenuTop)
                {
                    case MENUTOP_SAVEDSENTENCES:
                        mMenuTop_SavedSentences.setBackgroundColor(getResources().getColor(R.color.colorMenuTopActive));
                        break;

                    case MENUTOP_HISTORY:
                        mMenuTop_History.setBackgroundColor(getResources().getColor(R.color.colorMenuTopActive));
                        break;

                    case MENUTOP_SETTINGS:
                        mMenuTop_Settings.setBackgroundColor(getResources().getColor(R.color.colorMenuTopActive));
                        break;
                }
            }
        });
    }

    // --------------
    // CallBack
    // --------------

    @Override
    public void onButtonClicked(View view) {
        Log.i("TTS","Button clicked ! ID : " + view.getId());
        if(view.getId() == R.id.sentence_saved_cell)
        {
            SavedSentences clickedSentence = (SavedSentences) view.getTag();

            Log.i("TTS","clickedSentence !  : " + clickedSentence.getSentence());
            mSpeaker.ttsSpeak(clickedSentence.getSentence());

            final HistoryDAO historyDAO = new HistoryDAO(this);
            historyDAO.add(clickedSentence.getSentence());
        }

        if(view.getId() == R.id.sentence_history_cell)
        {
            String history = (String) view.getTag();

            Log.i("TTS","history clicked !  : " + history);
            mSpeaker.ttsSpeak(history);
        }

    }

    //-----------------------------------------------------------------------------------

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mSpeaker != null) {
            mSpeaker.destroy();
        }
    }
}