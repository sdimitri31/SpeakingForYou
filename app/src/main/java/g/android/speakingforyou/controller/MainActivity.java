package g.android.speakingforyou.controller;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.media.AudioManager;
import android.speech.tts.UtteranceProgressListener;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import g.android.speakingforyou.model.History;
import g.android.speakingforyou.model.HistoryDAO;
import g.android.speakingforyou.model.SavedSentences;
import g.android.speakingforyou.model.SavedSentencesDAO;
import g.android.speakingforyou.model.Speaker;
import g.android.speakingforyou.model.ViewPagerAdapter;
import g.android.speakingforyou.model.VoiceSettings;
import g.android.speakingforyou.R;
import g.android.speakingforyou.view.HistoryFragment;
import g.android.speakingforyou.view.SettingsFragment;
import g.android.speakingforyou.view.SavedSentencesFragment;

public class MainActivity extends AppCompatActivity implements SavedSentencesFragment.OnButtonClickedListener,
        HistoryFragment.OnButtonClickedListener, SettingsFragment.OnButtonClickedListener, SettingsFragment.OnSeekBarChangeListener {


    //-----------------------------------------------------------------------------------
    //                          Static Variables
    //-----------------------------------------------------------------------------------

    public static final String  LOG_TAG = "SFY : MainActivity";

    public static final String  PREF_NAME = "SharedPrefs";

    public static final int     MENUTOP_SAVEDSENTENCES = 0;
    public static final int     MENUTOP_HISTORY = 1;
    public static final int     MENUTOP_SETTINGS = 2;

    public static final int     THEME_LIGHT = 0;
    public static final int     THEME_DARK = 1;


    //-----------------------------------------------------------------------------------
    //                          Class Variables
    //-----------------------------------------------------------------------------------

    //Core functionality
    private VoiceSettings       mVoiceSettings;
    private Speaker             mSpeaker;
    AudioManager                mAudioManager;
    private Boolean             mIsSpeaking;

    //Databases
    final private SavedSentencesDAO     mSavedSentencesDAO = new SavedSentencesDAO(this);
    final private HistoryDAO            mHistoryDAO = new HistoryDAO(this);

    //Fragments
    private ViewPager           mViewPager;
    private ViewPagerAdapter    mPagerAdapter;
    TabLayout                   mTabLayout;
    private int                 mActiveFragment;
    private int                 mCurrentTheme;

    //Manual writing field
    private EditText            mEditText_Sentence;
    private ImageButton         mButton_DeleteText;
    private ImageButton         mButton_SaveSentence;
    private ImageButton         mButton_ToggleSpeaking;
    private Boolean             mEditTextWasVisible;

    //-----------------------------------------------------------------------------------


    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Setting up Voice and Audio
        mVoiceSettings  = new VoiceSettings(getSharedPreferences(PREF_NAME, MODE_PRIVATE));
        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        //Setting up Theme
        mCurrentTheme = mVoiceSettings.getTheme();
        Utils.setTheme(mCurrentTheme);
        Utils.onActivityCreateSetTheme(this);

        //Starting Activity
        setContentView(R.layout.activity_main);

        //-----------------------------------------------------------------------------------
        //                          Initializing UI ID's
        //-----------------------------------------------------------------------------------

        //VIEWPAGER ELEMENTS
        mViewPager                  =   findViewById(R.id.viewPager_Fragments);
        mTabLayout                  =   findViewById(R.id.tab_layout);

        // EDIT TEXT ELEMENTS
        mEditText_Sentence          =   findViewById(R.id.editText_Sentence);
        mButton_DeleteText          =   findViewById(R.id.imageButton_DeleteText);
        mButton_SaveSentence        =   findViewById(R.id.imageButton_AddSentence);

        // PLAY/STOP BUTTON
        mButton_ToggleSpeaking      =   findViewById(R.id.imageButton_PlayStopToggle);


        //-----------------------------------------------------------------------------------
        //                 Initializing OBJECTS and DEFAULT SETTINGS
        //-----------------------------------------------------------------------------------

        mSpeaker        = new Speaker(this, mVoiceSettings);
        mIsSpeaking     = false;
        mAudioManager   = (AudioManager) getSystemService(MainActivity.AUDIO_SERVICE);

        //mSavedSentencesDAO = new SavedSentencesDAO(this);

        mButton_DeleteText.setVisibility(View.INVISIBLE);
        mButton_SaveSentence.setVisibility(View.INVISIBLE);

        mPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mPagerAdapter);

        mEditTextWasVisible = true;

        mTabLayout.setupWithViewPager(mViewPager);
        setTabIcon();

        //-----------------------------------------------------------------------------------
        //                          Initializing Listeners
        //-----------------------------------------------------------------------------------

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {
                //Refresh the fragment
                if(v == 0.0){
                    Log.v(LOG_TAG,"onPageScrolled i : " + i + " v : " + v + " i1 : " + i1);
                    mActiveFragment = i;
                    updateFragment(i);
                }
            }

            @Override
            public void onPageSelected(int i) {
                //Show/Hide Keyboard depending on the fragment we are
                Log.v(LOG_TAG,"onPageSelected : " + i);
                if(i == MENUTOP_SETTINGS)
                    setTextFieldVisible(false);
                else {
                    if(!mEditTextWasVisible)
                        setTextFieldVisible(true);
                }
            }

            @Override
            public void onPageScrollStateChanged(int i) {}
        });

        //--------------------
        // EDIT TEXT Listeners

        //If EnterKey pressed
        mEditText_Sentence.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    Log.v(LOG_TAG,"Enter Key Pressed!");
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
                //Log.v(LOG_TAG,"afterTextChanged editable.length() : " + editable.length() );
                if(editable.length() > 0) {
                    mButton_DeleteText.setVisibility(View.VISIBLE);
                    mButton_SaveSentence.setVisibility(View.VISIBLE);
                }
                else {
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
                        mSavedSentencesDAO.getNextPosition()
                );
                mSavedSentencesDAO.add(sentenceToSave);

                updateFragment(MENUTOP_SAVEDSENTENCES);
            }
        });

        //----------------------------
        // PLAY/STOP BUTTON Listeners

        mButton_ToggleSpeaking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mIsSpeaking) {
                    ttsStop();
                }
                else {
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
                Log.v(LOG_TAG,"Finish talking!");
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
        if (sentence.trim().length() > 0) {
            mSpeaker.ttsSpeak(sentence);
            setIsSpeaking(true);

            if (addToHistory) {
                //If the last sentence in the history is the same as the current sentence don't add
                if (!mHistoryDAO.getLastHistory().equals(sentence)) {
                    mHistoryDAO.add(new History(sentence));
                    if (mActiveFragment == MENUTOP_HISTORY)
                        updateFragment(MENUTOP_HISTORY);
                }
            }
        }
        else {
            Log.v(LOG_TAG,"Empty sentence");
        }
    }

    private void ttsStop(){
        mSpeaker.ttsStop();
        setIsSpeaking(false);
    }

    //-----------------------------------------------------------------------------------
    //                          SETTINGS METHODS
    //-----------------------------------------------------------------------------------

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

    private void themeSelector(){
        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(this, getAlertDialogStyle()));

        //set the title for alert dialog
        builder.setTitle(getResources().getString(R.string.textView_Settings_Theme));

        //set items to alert dialog. i.e. our array , which will be shown as list view in alert dialog
        builder.setItems(getResources().getStringArray(R.array.theme_array), new DialogInterface.OnClickListener() {

            @Override public void onClick(DialogInterface dialog, int item) {
                if (mVoiceSettings.getTheme() != item) {
                    Utils.changeToTheme(MainActivity.this, item);
                }
                mVoiceSettings.setTheme(item);
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

    private void languageSelector(){
        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(this, getAlertDialogStyle()));

        //set the title for alert dialog
        builder.setTitle(getResources().getString(R.string.textView_QuickSettings_Language));

        //set items to alert dialog. i.e. our array , which will be shown as list view in alert dialog
        builder.setItems(mSpeaker.getListLanguageName().toArray(new String[mSpeaker.getListLanguageName().size()]), new DialogInterface.OnClickListener() {

            @Override public void onClick(DialogInterface dialog, int item) {
                setLanguage(mSpeaker.getListLanguageTag().get(item));
                updateFragment(MENUTOP_SETTINGS);
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

    private void setVolume(int volume){
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0);
    }

    private void clearSavedSentences(){
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        //Yes button clicked
                        mSavedSentencesDAO.deleteAll();
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        //No button clicked
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(this, getAlertDialogStyle()));
        builder.setMessage(getResources().getString(R.string.alertDialog_DeleteSavecSentencesQuestion))
                .setPositiveButton(getResources().getString(R.string.alertDialog_OK), dialogClickListener)
                .setNegativeButton(getResources().getString(R.string.alertDialog_Cancel), dialogClickListener)
                .show();
    }

    private void clearHistory(){
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        //Yes button clicked
                        mHistoryDAO.deleteAll();
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        //No button clicked
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(this, getAlertDialogStyle()));
        builder.setMessage(getResources().getString(R.string.alertDialog_DeleteHistoryQuestion)).setPositiveButton(getResources().getString(R.string.alertDialog_OK), dialogClickListener)
                .setNegativeButton(getResources().getString(R.string.alertDialog_Cancel), dialogClickListener).show();
    }


    //-----------------------------------------------------------------------------------
    //                          FRAGMENTS METHODS
    //-----------------------------------------------------------------------------------

    private void updateFragment(int fragmentIndex){
        Log.v(LOG_TAG,"updateFragment : " + fragmentIndex);
        Fragment fragment = mPagerAdapter.getFragment(fragmentIndex);
        if (fragment != null) {
            fragment.onResume();
        }
    }

    private void setTabIcon(){
        mTabLayout.getTabAt(MENUTOP_SAVEDSENTENCES).setIcon(android.R.drawable.btn_star);
        mTabLayout.getTabAt(MENUTOP_HISTORY).setIcon(android.R.drawable.ic_menu_recent_history);
        mTabLayout.getTabAt(MENUTOP_SETTINGS).setIcon(android.R.drawable.ic_menu_manage);
    }

    // slide the view from below itself to the current position
    public static void expand(final View v) {
        int matchParentMeasureSpec = View.MeasureSpec.makeMeasureSpec(((View) v.getParent()).getWidth(), View.MeasureSpec.EXACTLY);
        int wrapContentMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        v.measure(matchParentMeasureSpec, wrapContentMeasureSpec);
        final int targetHeight = v.getMeasuredHeight();

        // Older versions of android (pre API 21) cancel animations for views with a height of 0.
        v.getLayoutParams().height = 1;
        v.setVisibility(View.VISIBLE);

        Animation a = new Animation()
        {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                v.getLayoutParams().height = interpolatedTime == 1
                        ? LinearLayout.LayoutParams.WRAP_CONTENT
                        : Math.max(1, (int)(targetHeight * interpolatedTime));
                v.requestLayout();
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        // Expansion speed of 1dp/ms
        a.setDuration(100);
        v.startAnimation(a);
    }

    public static void collapse(final View v) {
        final int initialHeight = v.getMeasuredHeight();

        Animation a = new Animation()
        {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                if(interpolatedTime == 1){
                    v.setVisibility(View.GONE);
                }else{
                    v.getLayoutParams().height = initialHeight - (int)(initialHeight * interpolatedTime);
                    v.requestLayout();
                }
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        // Collapse speed of 1dp/ms
        a.setDuration(100);
        v.startAnimation(a);
    }

    private void setTextFieldVisible(boolean isVisible){
        final LinearLayout linearLayout = findViewById(R.id.linearLayout_EditText_Play);

        if(isVisible) {
            expand(linearLayout);
            //slideUp(linearLayout);
            mEditTextWasVisible = true;
        }
        else {
            collapse(linearLayout);
            mEditTextWasVisible = false;

            //Hide the keyboard
            InputMethodManager imm = (InputMethodManager) this.getSystemService(Activity.INPUT_METHOD_SERVICE);
            //Find the currently focused view, so we can grab the correct window token from it.
            View view = this.getCurrentFocus();
            //If no view currently has focus, create a new one, just so we can grab a window token from it
            if (view == null) {
                view = new View(this);
            }
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            view.clearFocus();
        }
    }

    //-----------------------------------------------------------------------------------
    //                          CALLBACKS
    //-----------------------------------------------------------------------------------

    @Override
    public void onButtonClicked(View view) {
        Log.i(LOG_TAG,"CallBack received  : onButtonClicked " + getResources().getResourceEntryName(view.getId()));
        switch (view.getId()){
            case R.id.constraintLayout_SavedSentenceCell:
                SavedSentences clickedSentence = (SavedSentences) view.getTag();

                Log.i(LOG_TAG,"     From SavedSentence : " + clickedSentence.getSentence());
                speak(clickedSentence.getSentence(), true);
                break;

            case R.id.constraintLayout_HistoryCell:
                History history = (History) view.getTag();

                Log.i(LOG_TAG,"     From History : " + history);
                speak(history.getSentence(), false);
                break;

            case R.id.switch_Settings_TalkMode:
                setTalkMode(((Switch) view).isChecked());
                break;

            case R.id.layout_Settings_Language:
                languageSelector();
                break;

            case R.id.button_Settings_Test:
                String testSentence = getResources().getString(R.string.settings_TestSentence);
                mSpeaker.ttsSpeak(testSentence);
                break;

            case R.id.button_Settings_Reset:
                setPitch(100);
                setSpeechRate(100);
                break;

            case R.id.button_Settings_ClearHistory:
                clearHistory();
                break;

            case R.id.button_Settings_ClearSavedSentences:
                clearSavedSentences();
                break;

            case R.id.layout_Settings_Theme:
                themeSelector();
                break;
        }

    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b){}
    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {}
    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        Log.i(LOG_TAG,"onStopTrackingTouch ID : " + getResources().getResourceEntryName(seekBar.getId()));
        switch (seekBar.getId())
        {
            case R.id.seekBar_Settings_Pitch:
                setPitch(seekBar.getProgress());
                break;

            case R.id.seekBar_Settings_Speed:
                setSpeechRate(seekBar.getProgress());
                break;

            case R.id.seekBar_Settings_Volume:
                setVolume(seekBar.getProgress());
                break;
        }
    }

    //-----------------------------------------------------------------------------------
    //                          OTHER
    //-----------------------------------------------------------------------------------

    private int getAlertDialogStyle(){
        if(mCurrentTheme == THEME_LIGHT){
            return R.style.alert_dialog_wh;
        }
        else if(mCurrentTheme == THEME_DARK){
            return R.style.alert_dialog_dark;
        }
        else{
            return R.style.alert_dialog_dark;
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