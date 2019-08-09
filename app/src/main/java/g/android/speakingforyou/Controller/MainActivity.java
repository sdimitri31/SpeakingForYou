package g.android.speakingforyou.Controller;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.media.AudioManager;
import android.speech.tts.UtteranceProgressListener;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import g.android.speakingforyou.Model.HistoryDAO;
import g.android.speakingforyou.Model.SavedSentences;
import g.android.speakingforyou.Model.SavedSentencesDAO;
import g.android.speakingforyou.Model.Speaker;
import g.android.speakingforyou.Model.VoiceSettings;
import g.android.speakingforyou.R;
import g.android.speakingforyou.View.HistoryFragment;
import g.android.speakingforyou.View.QuickSettingsFragment;
import g.android.speakingforyou.View.SavedSentencesFragment;

public class MainActivity extends AppCompatActivity implements SavedSentencesFragment.OnButtonClickedListener,
        HistoryFragment.OnButtonClickedListener, QuickSettingsFragment.OnButtonClickedListener, QuickSettingsFragment.OnSeekBarChangeListener {

    public static final String PREF_NAME = "SharedPrefs";

    public static final String MENUTOP_SAVEDSENTENCES = "SavedSentences";
    public static final String MENUTOP_HISTORY = "History";
    public static final String MENUTOP_SETTINGS = "Settings";

    private String          mActiveFragment;

    private VoiceSettings   mVoiceSettings;
    private Speaker         mSpeaker;
    AudioManager            mAudioManager;

    //Menu Top
    private ImageView       mMenuTop_SavedSentences;
    private ImageView       mMenuTop_History;
    private ImageView       mMenuTop_Settings;

    //Manual writing field
    private EditText        mEditText_Sentence;
    private ImageButton     mButton_DeleteText;
    private ImageButton     mButton_SaveSentence;
    private ImageButton     mButton_ToggleSpeaking;

    private Boolean         mIsSpeaking;


    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        setContentView(R.layout.activity_main_2);



        //-----------------------------------------------------------------------------------
        //                          Initializing UI ID's
        //-----------------------------------------------------------------------------------

        // MENU TOP ELEMENTS
        mMenuTop_SavedSentences = findViewById(R.id.imageView_MenuTop_SavedSentences);
        mMenuTop_History            =   findViewById(R.id.imageView_MenuTop_History);
        mMenuTop_Settings           =   findViewById(R.id.imageView_MenuTop_QuickSettings);

        // EDIT TEXT ELEMENTS
        mEditText_Sentence          =   findViewById(R.id.editText_Sentence);
        mButton_DeleteText          =   findViewById(R.id.imageButton_DeleteText);
        mButton_SaveSentence        =   findViewById(R.id.imageButton_AddSentence);

        // PLAY/STOP BUTTON
        mButton_ToggleSpeaking      =   findViewById(R.id.imageButton_PlayStopToggle);


        //-----------------------------------------------------------------------------------
        //                 Initializing OBJECTS and DEFAULT SETTINGS
        //-----------------------------------------------------------------------------------

        mVoiceSettings  = new VoiceSettings(getSharedPreferences(PREF_NAME, MODE_PRIVATE));
        mSpeaker        = new Speaker(this, mVoiceSettings);
        mIsSpeaking     = false;
        mAudioManager   = (AudioManager) getSystemService(MainActivity.AUDIO_SERVICE);

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

        mMenuTop_Settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadFragment(MENUTOP_SETTINGS);
            }
        });


        //-----------------------------------------------------------------------------------

        // EDIT TEXT Listeners

        //If EnterKey pressed
        mEditText_Sentence.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    Log.v("TTS","Enter Key Pressed!");
                    speak(mEditText_Sentence.getText().toString(), true);
                    if(mVoiceSettings.getTalkMode()){
                        mEditText_Sentence.setText("");
                    }
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
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable editable) {
                //Log.v("TTS","afterTextChanged editable.length() : " + editable.length() );
                if(editable.length() > 0)
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
                    speak(mEditText_Sentence.getText().toString(), true);
                    if(mVoiceSettings.getTalkMode()){
                        mEditText_Sentence.setText("");
                    }
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

    private void speak(String sentence, boolean addToHistory){
        mSpeaker.ttsSpeak(sentence);
        setIsSpeaking(true);

        if(addToHistory){
            //If the last sentence in the history is the same as the current sentence don't add
            final HistoryDAO historyDAO = new HistoryDAO(this);
            if (!historyDAO.getLastHistory().equals(sentence)) {
                historyDAO.add(sentence);
                if(mActiveFragment.equals(MENUTOP_HISTORY))
                    loadFragment(MENUTOP_HISTORY);
            }
        }
    }

    private void ttsStop(){
        mSpeaker.ttsStop();
        setIsSpeaking(false);
    }

    private void setPitch(int pitchValue){
        mSpeaker.setPitch(pitchValue);
        mVoiceSettings.setPitch(pitchValue);
    }

    private void setSpeechRate(int speechRateValue){
        mSpeaker.setSpeechRate(speechRateValue);
        mVoiceSettings.setSpeechRate(speechRateValue);
    }

    private void setTalkMode(boolean isTalkMode){
        mVoiceSettings.setTalkMode(isTalkMode);
    }

    private void setLanguage(String languageTag){
        mSpeaker.setLanguage(languageTag);
        mVoiceSettings.setLanguage(languageTag);

    }

    public void languageSelector(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        //set the title for alert dialog
        builder.setTitle(getResources().getString(R.string.textView_QuickSettings_Language));

        //set items to alert dialog. i.e. our array , which will be shown as list view in alert dialog
        builder.setItems(mSpeaker.getListLanguageName().toArray(new String[mSpeaker.getListLanguageName().size()]), new DialogInterface.OnClickListener() {

            @Override public void onClick(DialogInterface dialog, int item) {
                setLanguage(mSpeaker.getListLanguageTag().get(item));
                loadFragment(MENUTOP_SETTINGS);
            }
        });

        //Creating CANCEL button in alert dialog, to dismiss the dialog box when nothing is selected
        builder .setCancelable(false)
                .setNegativeButton(getResources().getString(R.string.alertDialog_Cancel),new DialogInterface.OnClickListener() {

                    @Override  public void onClick(DialogInterface dialog, int id) {
                        //When clicked on CANCEL button the dalog will be dismissed
                        dialog.dismiss();
                    }
                });

        //Creating alert dialog
        AlertDialog alert = builder.create();

        //Showing alert dialog
        alert.show();
    }

    public void setVolume(int volume){
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0);
    }

    //-----------------------------------------------------------------------------------
    //                          FRAGMENTS METHODS
    //-----------------------------------------------------------------------------------

    /**
     *
     * @param fragment Load the fragment and set the colors of the MenuTop
     */
    private void loadFragment(String fragment){
        mActiveFragment = fragment;
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
                Bundle bundle = new Bundle();
                bundle.putInt("volume", mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC));
                bundle.putInt("pitch", mVoiceSettings.getPitch());
                bundle.putInt("speed", mVoiceSettings.getSpeechRate());
                bundle.putString("language", mVoiceSettings.getLanguage());
                bundle.putBoolean("talkMode", mVoiceSettings.getTalkMode());
                QuickSettingsFragment frag = new QuickSettingsFragment();
                frag.setArguments(bundle);
                this.getSupportFragmentManager().beginTransaction()
                        .replace(R.id.FrameLayout_Fragments, frag)
                        .commit();
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
        Log.i("TTS","CallBack received  : " + getResources().getResourceEntryName(view.getId()));
        switch (view.getId()){
            case R.id.sentence_saved_cell:
                SavedSentences clickedSentence = (SavedSentences) view.getTag();

                Log.i("TTS","clickedSentence !  : " + clickedSentence.getSentence());
                speak(clickedSentence.getSentence(), true);
                break;

            case R.id.sentence_history_cell:
                String history = (String) view.getTag();

                Log.i("TTS","history clicked !  : " + history);
                speak(history, false);
                break;

            case R.id.switch_QuickSettings_TalkMode:
                setTalkMode(((Switch) view).isChecked());
                break;

            case R.id.layout_QuickSettings_Language:
                languageSelector();
                break;

        }

    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b){
        //Log.i("TTS","MainActivity onProgressChanged ID : " + getResources().getResourceEntryName(seekBar.getId()));
    }
    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        //Log.i("TTS","MainActivity onStartTrackingTouch ID : " + getResources().getResourceEntryName(seekBar.getId()));
    }
    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        Log.i("TTS","MainActivity onStopTrackingTouch ID : " + getResources().getResourceEntryName(seekBar.getId()));
        switch (seekBar.getId())
        {
            case R.id.seekBar_QuickSettings_Pitch:
                setPitch(seekBar.getProgress());
                break;

            case R.id.seekBar_QuickSettings_Speed:
                setSpeechRate(seekBar.getProgress());
                break;

            case R.id.seekBar_QuickSettings_Volume:
                setVolume(seekBar.getProgress());
                break;
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