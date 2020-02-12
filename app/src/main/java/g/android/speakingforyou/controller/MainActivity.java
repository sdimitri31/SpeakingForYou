package g.android.speakingforyou.controller;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioManager;
import android.os.Build;
import android.speech.tts.UtteranceProgressListener;
import android.support.constraint.ConstraintLayout;
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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Date;
import java.util.List;

import g.android.speakingforyou.model.History;
import g.android.speakingforyou.model.HistoryDAO;
import g.android.speakingforyou.model.SavedSentences;
import g.android.speakingforyou.model.SavedSentencesDAO;
import g.android.speakingforyou.model.Speaker;
import g.android.speakingforyou.model.ViewPagerAdapter;
import g.android.speakingforyou.model.VoiceSettings;
import g.android.speakingforyou.R;
import g.android.speakingforyou.view.HistoryFragment;
import g.android.speakingforyou.view.SavedSentencesFragment;


public class MainActivity extends AppCompatActivity implements SavedSentencesFragment.OnButtonClickedListener, SavedSentencesFragment.OnLongClickListener ,
        HistoryFragment.OnButtonClickedListener, HistoryFragment.OnLongClickListener {


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
    boolean mIsMenuSortVisible          = false;

    //Core functionality
    private VoiceSettings       mVoiceSettings;
    private Speaker             mSpeaker;
    AudioManager                mAudioManager;
    private Boolean             mIsSpeaking;

    //Databases
    final private SavedSentencesDAO     mSavedSentencesDAO = new SavedSentencesDAO(this);
    final private HistoryDAO            mHistoryDAO = new HistoryDAO(this);

    //Header
    private ImageButton         mButton_Settings;
    private ImageButton         mButton_ThemeMode;
    private ImageButton         mButton_Sort;

    //CheckBox Menu
    private ConstraintLayout    mConstraintLayout_ActionRadioButton;
    private ImageButton         mSelection_Clear;
    private ImageButton         mSelection_All;
    private ImageButton         mSelection_Delete;
    private ImageButton         mSelection_Add;
    private ImageButton         mSelection_Remove;
    private boolean mIsActionRadioButtonVisible = false;

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

    //-----------------------------------------------------------------------------------

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Avoid white status bar with white text on SDK lower than 23
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.BLACK);
        }

        //Setting up Voice and Audio
        mVoiceSettings  = new VoiceSettings(getSharedPreferences(PREF_NAME, MODE_PRIVATE));
        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        //Setting up Theme
        mCurrentTheme = mVoiceSettings.getTheme();
        Utils.setTheme(mCurrentTheme);
        Utils.onActivityCreateSetTheme(this);

        //Starting Activity

        new ThemeColors(this);
        setContentView(R.layout.activity_main);

        //-----------------------------------------------------------------------------------
        //                          Initializing UI ID's
        //-----------------------------------------------------------------------------------

        //HEADER
        mButton_Settings            =   findViewById(R.id.imageButton_Settings);
        mButton_ThemeMode           =   findViewById(R.id.imageButton_ThemeMode);
        mButton_Sort                =   findViewById(R.id.imageButton_Sort);

        //CHECKBOX MENU
        mConstraintLayout_ActionRadioButton = findViewById(R.id.constrainLayout_ActionRadioButton);
        mSelection_Clear                    = findViewById(R.id.imageButton_CancelSelectRadioButton);
        mSelection_All                      = findViewById(R.id.imageButton_SelectAllRadioButton);
        mSelection_Delete                   = findViewById(R.id.imageButton_DeleteAllSelectedRadioButton);
        mSelection_Add                      = findViewById(R.id.imageButton_AddAllRadioButton);
        mSelection_Remove                   = findViewById(R.id.imageButton_DeleteFromSavedRadioButton);

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

        mButton_DeleteText.setVisibility(View.INVISIBLE);
        mButton_SaveSentence.setVisibility(View.INVISIBLE);

        mPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mPagerAdapter);

        mTabLayout.setupWithViewPager(mViewPager);
        setTabIcon();


        //-----------------------------------------------------------------------------------
        //                          Initializing Listeners
        //-----------------------------------------------------------------------------------

        //--------------------
        //HEADER Listeners
        mButton_Settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivityForResult(intent, 1);
                overridePendingTransition(R.anim.slide_in_up,android.R.anim.fade_out);
            }
        });

        mButton_ThemeMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleThemeMode();
            }
        });

        mButton_Sort.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mActiveFragment == MENUTOP_HISTORY){
                    sortHistoryPopup();
                }
                else if(mActiveFragment == MENUTOP_SAVEDSENTENCES){
                    sortSavedSentencesPopup();
                }
            }
        });

        //--------------------
        //VIEWPAGER Listeners
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {
                //Refresh the fragment
                if(v == 0.0){
                    Log.v(LOG_TAG,"onPageScrolled i : " + i + " v : " + v + " i1 : " + i1);
                    //Update the fragment only if the fragment has changed
                    if(mActiveFragment != i){
                        mActiveFragment = i;
                        setTabIcon();
                        updateFragment(i);
                    }
                }
            }

            @Override
            public void onPageSelected(int i) {
                Log.v(LOG_TAG,"onPageSelected : " + i);
                /*
                DEPRECATED
                USED BEFORE THE NEW DESIGN


                //Show/Hide Keyboard depending on the fragment we are
                if(i == MENUTOP_SETTINGS)
                    setTextFieldVisible(false);
                else {
                    if(!mEditTextWasVisible)
                        setTextFieldVisible(true);
                }

                if(i == MENUTOP_HISTORY){
                    mIsMenuSortVisible = true;
                    invalidateOptionsMenu();
                }
                else {
                    mIsMenuSortVisible = false;
                    invalidateOptionsMenu();
                }

                mActiveFragment = i;
                setTabIcon();
                updateFragment(i);
                */

            }

            @Override
            public void onPageScrollStateChanged(int i) {}
        });

        //--------------------
        // CHECKBOX MENU Listeners
        mSelection_Clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (mActiveFragment){
                    case MENUTOP_SAVEDSENTENCES:
                        SavedSentencesFragment savedSentencesFragment =  (SavedSentencesFragment)mPagerAdapter.getFragment(MENUTOP_SAVEDSENTENCES);
                        savedSentencesFragment.unSelectAll();
                        break;
                    case MENUTOP_HISTORY:
                        HistoryFragment historyFragment =  (HistoryFragment)mPagerAdapter.getFragment(MENUTOP_HISTORY);
                        historyFragment.unSelectAll();
                        break;
                }
                exitSelectMenu();
            }
        });

        mSelection_All.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (mActiveFragment){
                    case MENUTOP_SAVEDSENTENCES:
                        SavedSentencesFragment savedSentencesFragment =  (SavedSentencesFragment)mPagerAdapter.getFragment(MENUTOP_SAVEDSENTENCES);
                        savedSentencesFragment.selectALL();
                        break;
                    case MENUTOP_HISTORY:
                        HistoryFragment historyFragment =  (HistoryFragment)mPagerAdapter.getFragment(MENUTOP_HISTORY);
                        historyFragment.selectALL();
                        break;
                }
            }
        });

        mSelection_Delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (mActiveFragment){
                    case MENUTOP_SAVEDSENTENCES:
                        SavedSentencesFragment savedSentencesFragment =  (SavedSentencesFragment)mPagerAdapter.getFragment(MENUTOP_SAVEDSENTENCES);
                        for (SavedSentences savedSentence: savedSentencesFragment.getCurrentSelectedItems()) {
                            mSavedSentencesDAO.delete(savedSentence.getId());
                        }
                        savedSentencesFragment.unSelectAll();
                        break;
                    case MENUTOP_HISTORY:
                        HistoryFragment historyFragment =  (HistoryFragment)mPagerAdapter.getFragment(MENUTOP_HISTORY);
                        for (History history: historyFragment.getCurrentSelectedItems()) {
                            mHistoryDAO.delete(history.getId());
                        }
                        historyFragment.unSelectAll();
                        break;
                }
                exitSelectMenu();
            }
        });

        mSelection_Add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.v(LOG_TAG,"Add selected items to Saved pressed");
                if(mActiveFragment == MENUTOP_HISTORY) {
                        HistoryFragment historyFragment =  (HistoryFragment)mPagerAdapter.getFragment(MENUTOP_HISTORY);

                        //Get a list of the savedSentences
                        List<SavedSentences> savedSentencesList = mSavedSentencesDAO.getAll();
                        Log.v(LOG_TAG,"     Number of Item selected : " + historyFragment.getCurrentSelectedItems().size());

                        //Loop for every selected items
                        for (History history : historyFragment.getCurrentSelectedItems()) {
                            //If the sentence is not already saved, we add it to savedSentences
                            Log.v(LOG_TAG,"     Item : " + history.getSentence());


                            //Check if the history is already in the savedSentences
                            SavedSentences tmpSavedSentence = null;
                            tmpSavedSentence = MainActivity.findSavedSentenceUsingEnhancedForLoop(history.getSentence(), savedSentencesList);

                            if( tmpSavedSentence == null){
                                //It's not saved, we add it
                                Log.v(LOG_TAG,"     Adding : " + history.getSentence());
                                mSavedSentencesDAO.add(new SavedSentences(history.getSentence(), mSavedSentencesDAO.getNextPosition(), new Date(), 0));
                            }
                            else {
                                //It's already saved, we remove the found element from the list to avoid double checking
                                savedSentencesList.remove(tmpSavedSentence);
                            }
                        }
                        //historyFragment.unSelectAll();
                    Toast.makeText(getBaseContext(), getResources().getString(R.string.toast_addedToSavedSentences), Toast.LENGTH_SHORT).show();
                }
                exitSelectMenu();
            }
        });

        mSelection_Remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mActiveFragment == MENUTOP_HISTORY) {
                    HistoryFragment historyFragment =  (HistoryFragment)mPagerAdapter.getFragment(MENUTOP_HISTORY);
                    //Loop for every selected items
                    for (History history : historyFragment.getCurrentSelectedItems()) {
                        //If the sentence is already saved, we remove it to savedSentences
                        SavedSentences foundSaved = findSavedSentenceUsingEnhancedForLoop(history.getSentence(), mSavedSentencesDAO.getAll());
                        if(foundSaved != null)
                            mSavedSentencesDAO.delete(foundSaved.getId());
                    }
                    historyFragment.unSelectAll();
                }
                exitSelectMenu();
            }
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

                //Get a list of the savedSentences
                List<SavedSentences> savedSentencesList = mSavedSentencesDAO.getAll();

                //Check if the sentence is already in the savedSentences
                SavedSentences tmpSavedSentence = null;
                tmpSavedSentence = MainActivity.findSavedSentenceUsingEnhancedForLoop(sentence, savedSentencesList);

                if( tmpSavedSentence == null){
                    //It's not saved, we add it
                    mSavedSentencesDAO.add(new SavedSentences(sentence, mSavedSentencesDAO.getNextPosition(), new Date(), 0));
                }

                Toast.makeText(getBaseContext(), getResources().getString(R.string.toast_addedToSavedSentences), Toast.LENGTH_SHORT).show();
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
                    mButton_ToggleSpeaking.setImageResource(R.drawable.ic_stop);
                }
                else {
                    mButton_ToggleSpeaking.setImageResource(R.drawable.ic_play);
                }

                mIsSpeaking = isSpeaking;
            }
        });
    }

    private void speak(String sentence, boolean addToHistory){
        if (sentence.trim().length() > 0) {
            //Delete spaces after the last word
            sentence = sentence.replaceAll("\\s+$", "");

            mSpeaker.ttsSpeak(sentence);
            setIsSpeaking(true);

            if (addToHistory) {
                History dbHistory = findUsingEnhancedForLoop(sentence, mHistoryDAO.getAll());

                //If the sentence is already in the database
                if (dbHistory != null) {
                    Log.v(LOG_TAG,"dbHistory != null");
                    dbHistory.setDate(new Date());
                    dbHistory.setUsage(dbHistory.getUsage() + 1);
                    mHistoryDAO.update(dbHistory);
                }
                else{
                    Log.v(LOG_TAG,"dbHistory == null");
                    mHistoryDAO.add(new History(sentence, new Date(), 1));
                }

                if (mActiveFragment == MENUTOP_HISTORY)
                    updateFragment(MENUTOP_HISTORY);
            }

            SavedSentences savedSentences = findSavedSentenceUsingEnhancedForLoop(sentence, mSavedSentencesDAO.getAll());
            if(savedSentences != null){
                savedSentences.setDate(new Date());
                savedSentences.setUsage(savedSentences.getUsage() + 1);
                mSavedSentencesDAO.update(savedSentences);
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



    public void sortHistoryPopup(){
        LayoutInflater inflater = getLayoutInflater();
        View alertLayout = inflater.inflate(R.layout.history_sorting_popup, null);

        final RadioGroup  radioGroupSort = alertLayout.findViewById(R.id.radioGroup_History_SortBy);
        RadioButton radioButtonSortChrono = alertLayout.findViewById(R.id.radioButton_History_SortBy_Chronological);
        RadioButton radioButtonSortUsage = alertLayout.findViewById(R.id.radioButton_History_SortBy_Usage);
        RadioButton radioButtonSortAlpha = alertLayout.findViewById(R.id.radioButton_History_SortBy_Alphabetical);

        final RadioGroup  radioGroupOrder = alertLayout.findViewById(R.id.radioGroup_History_OrderBy);
        RadioButton radioButtonOrderDesc = alertLayout.findViewById(R.id.radioButton_History_OrderBy_Desc);
        RadioButton radioButtonOrderAsc = alertLayout.findViewById(R.id.radioButton_History_OrderBy_Asc);

        switch (mVoiceSettings.getHistorySort()){
            case VoiceSettings.SORTBY_CHRONO:
                radioButtonSortChrono.setChecked(true);
                break;
            case VoiceSettings.SORTBY_USAGE:
                radioButtonSortUsage.setChecked(true);
                break;
            case VoiceSettings.SORTBY_ALPHA:
                radioButtonSortAlpha.setChecked(true);
                break;
        }

        switch (mVoiceSettings.getHistoryOrder()){
            case VoiceSettings.ORDERBY_DESC:
                radioButtonOrderDesc.setChecked(true);
                break;
            case VoiceSettings.ORDERBY_ASC:
                radioButtonOrderAsc.setChecked(true);
                break;
        }

        AlertDialog.Builder alert = new AlertDialog.Builder(new ContextThemeWrapper(this, getAlertDialogStyle()));
        // this set the view from XML inside AlertDialog
        alert.setView(alertLayout);
        alert.setNegativeButton(getResources().getString(R.string.alertDialog_Cancel), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        alert.setPositiveButton(getResources().getString(R.string.alertDialog_OK), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                int sorting = 0;
                int ordering = 0;

                switch (radioGroupSort.getCheckedRadioButtonId()){
                    case R.id.radioButton_History_SortBy_Chronological:
                        sorting = VoiceSettings.SORTBY_CHRONO;
                        break;
                    case R.id.radioButton_History_SortBy_Usage:
                        sorting = VoiceSettings.SORTBY_USAGE;
                        break;
                    case R.id.radioButton_History_SortBy_Alphabetical:
                        sorting = VoiceSettings.SORTBY_ALPHA;
                        break;
                }

                switch (radioGroupOrder.getCheckedRadioButtonId()){
                    case R.id.radioButton_History_OrderBy_Desc:
                        ordering = VoiceSettings.ORDERBY_DESC;
                        break;
                    case R.id.radioButton_History_OrderBy_Asc:
                        ordering = VoiceSettings.ORDERBY_ASC;
                        break;
                }

                mVoiceSettings.setHistorySort(sorting);
                mVoiceSettings.setHistoryOrder(ordering);
                updateFragment(MENUTOP_HISTORY);
            }
        });
        AlertDialog dialog = alert.create();
        dialog.show();
    }

    public void sortSavedSentencesPopup(){
        LayoutInflater inflater = getLayoutInflater();
        View alertLayout = inflater.inflate(R.layout.saved_sentence_sorting_popup, null);

        final RadioGroup  radioGroupSort = alertLayout.findViewById(R.id.radioGroup_SavedSentence_SortBy);
        RadioButton radioButtonSortChrono = alertLayout.findViewById(R.id.radioButton_SavedSentence_SortBy_Chronological);
        RadioButton radioButtonSortUsage = alertLayout.findViewById(R.id.radioButton_SavedSentence_SortBy_Usage);
        RadioButton radioButtonSortAlpha = alertLayout.findViewById(R.id.radioButton_SavedSentence_SortBy_Alphabetical);

        final RadioGroup  radioGroupOrder = alertLayout.findViewById(R.id.radioGroup_SavedSentence_OrderBy);
        RadioButton radioButtonOrderDesc = alertLayout.findViewById(R.id.radioButton_SavedSentence_OrderBy_Desc);
        RadioButton radioButtonOrderAsc = alertLayout.findViewById(R.id.radioButton_SavedSentence_OrderBy_Asc);

        switch (mVoiceSettings.getSavedSentencesSort()){
            case VoiceSettings.SORTBY_CHRONO:
                radioButtonSortChrono.setChecked(true);
                break;
            case VoiceSettings.SORTBY_USAGE:
                radioButtonSortUsage.setChecked(true);
                break;
            case VoiceSettings.SORTBY_ALPHA:
                radioButtonSortAlpha.setChecked(true);
                break;
        }

        switch (mVoiceSettings.getSavedSentencesOrder()){
            case VoiceSettings.ORDERBY_DESC:
                radioButtonOrderDesc.setChecked(true);
                break;
            case VoiceSettings.ORDERBY_ASC:
                radioButtonOrderAsc.setChecked(true);
                break;
        }

        AlertDialog.Builder alert = new AlertDialog.Builder(new ContextThemeWrapper(this, getAlertDialogStyle()));
        // this set the view from XML inside AlertDialog
        alert.setView(alertLayout);
        alert.setNegativeButton(getResources().getString(R.string.alertDialog_Cancel), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        alert.setPositiveButton(getResources().getString(R.string.alertDialog_OK), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                int sorting = 0;
                int ordering = 0;

                switch (radioGroupSort.getCheckedRadioButtonId()){
                    case R.id.radioButton_SavedSentence_SortBy_Chronological:
                        sorting = VoiceSettings.SORTBY_CHRONO;
                        break;
                    case R.id.radioButton_SavedSentence_SortBy_Usage:
                        sorting = VoiceSettings.SORTBY_USAGE;
                        break;
                    case R.id.radioButton_SavedSentence_SortBy_Alphabetical:
                        sorting = VoiceSettings.SORTBY_ALPHA;
                        break;
                }

                switch (radioGroupOrder.getCheckedRadioButtonId()){
                    case R.id.radioButton_SavedSentence_OrderBy_Desc:
                        ordering = VoiceSettings.ORDERBY_DESC;
                        break;
                    case R.id.radioButton_SavedSentence_OrderBy_Asc:
                        ordering = VoiceSettings.ORDERBY_ASC;
                        break;
                }

                mVoiceSettings.setSavedSentencesSort(sorting);
                mVoiceSettings.setSavedSentencesOrder(ordering);
                updateFragment(MENUTOP_SAVEDSENTENCES);
            }
        });
        AlertDialog dialog = alert.create();
        dialog.show();
    }


    public static History findUsingEnhancedForLoop(
            String sentence, List<History> historyList) {
        //Delete spaces after the last word
        sentence = sentence.replaceAll("\\s+$", "");

        for (History history : historyList) {
            //Delete spaces after the last word
            String sentenceToLook = history.getSentence().replaceAll("\\s+$", "");
            //Log.v(LOG_TAG,"findUsingEnhancedForLoop : " + history.getSentence() +" ? " + sentence);
            if (sentenceToLook.toLowerCase().equals(sentence.toLowerCase())) {
                Log.v(LOG_TAG,"      Match  : " + history.getSentence() +" = " + sentence);
                return history;
            }
        }
        return null;
    }

    public static SavedSentences findSavedSentenceUsingEnhancedForLoop(
            String sentence, List<SavedSentences> savedSentencesList) {

        //Delete spaces after the last word
        sentence = sentence.replaceAll("\\s+$", "");

        for (SavedSentences savedSentences : savedSentencesList) {

            //Delete spaces after the last word
            String sentenceToLook = savedSentences.getSentence().replaceAll("\\s+$", "");
            //Log.v(LOG_TAG,"findUsingEnhancedForLoop : " + savedSentences.getSentence() +" ? " + sentence);
            if (sentenceToLook.toLowerCase().equals(sentence.toLowerCase())) {
                Log.v(LOG_TAG,"      Match  : " + savedSentences.getSentence() +" = " + sentence);
                return savedSentences;
            }
        }
        return null;
    }

    //-----------------------------------------------------------------------------------
    //                          FRAGMENTS METHODS
    //-----------------------------------------------------------------------------------

    private void updateFragment(int fragmentIndex){
        Log.v(LOG_TAG,"updateFragment : " + fragmentIndex);
        Fragment fragment = mPagerAdapter.getFragment(fragmentIndex);
        if (fragment != null) {
            if(fragmentIndex == MENUTOP_SAVEDSENTENCES){
                SavedSentencesFragment fragmentSaved =  (SavedSentencesFragment)mPagerAdapter.getFragment(MENUTOP_SAVEDSENTENCES);
                fragmentSaved.unSelectAll();
            }
            else if(fragmentIndex == MENUTOP_HISTORY){
                HistoryFragment fragmentHistory =  (HistoryFragment)mPagerAdapter.getFragment(MENUTOP_HISTORY);
                fragmentHistory.unSelectAll();
            }
            fragment.onResume();
            setActionRadioButtonVisible(false);
        }
    }

    private void setTabIcon(){
        try {
            mTabLayout.getTabAt(MENUTOP_SAVEDSENTENCES).setText(getResources().getString(R.string.tabLayout_SavedSentences));
            mTabLayout.getTabAt(MENUTOP_HISTORY).setText(getResources().getString(R.string.tabLayout_History));
            if(mActiveFragment == MENUTOP_HISTORY){
                mButton_Sort.setImageDrawable(getDrawable(R.drawable.ic_sort_hist));
            }
            else if(mActiveFragment == MENUTOP_SAVEDSENTENCES){
                mButton_Sort.setImageDrawable(getDrawable(R.drawable.ic_sort_fav));
            }
           // mTabLayout.getTabAt(MENUTOP_SETTINGS).setText(getResources().getString(R.string.tabLayout_Settings));
           // mTabLayout.getTabAt(MENUTOP_SAVEDSENTENCES).setIcon(android.R.drawable.btn_star);
           // mTabLayout.getTabAt(MENUTOP_SAVEDSENTENCES).getIcon().setColorFilter(getTabIconColor(MENUTOP_SAVEDSENTENCES), PorterDuff.Mode.MULTIPLY);
           // mTabLayout.getTabAt(MENUTOP_HISTORY).setIcon(android.R.drawable.ic_menu_recent_history);
           // mTabLayout.getTabAt(MENUTOP_HISTORY).getIcon().setColorFilter(getTabIconColor(MENUTOP_HISTORY), PorterDuff.Mode.MULTIPLY);
           // mTabLayout.getTabAt(MENUTOP_SETTINGS).setIcon(android.R.drawable.ic_menu_manage);
           // mTabLayout.getTabAt(MENUTOP_SETTINGS).getIcon().setColorFilter(getTabIconColor(MENUTOP_SETTINGS), PorterDuff.Mode.MULTIPLY);
        }
        catch (NullPointerException e){
            Log.e(LOG_TAG,"setTabIcon NullPointerException");
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
                speak(history.getSentence(), true);
                break;
        }

    }

    @Override
    public void onLongClick(View view) {
        Log.i(LOG_TAG,"CallBack received  : onLongClick " + getResources().getResourceEntryName(view.getId()));
        setActionRadioButtonVisible(true);
    }

    public void setActionRadioButtonVisible(boolean isVisible){
        mIsActionRadioButtonVisible = isVisible;
        if(isVisible) {
            mConstraintLayout_ActionRadioButton.setVisibility(View.VISIBLE);
            mTabLayout.setVisibility(View.INVISIBLE);
            switch (mActiveFragment){
                case MENUTOP_SAVEDSENTENCES:
                    mSelection_Add.setVisibility(View.INVISIBLE);
                    mSelection_Remove.setVisibility(View.INVISIBLE);
                    break;

                case MENUTOP_HISTORY:
                    mSelection_Add.setVisibility(View.VISIBLE);
                    mSelection_Remove.setVisibility(View.VISIBLE);
                    break;
            }
        }
        else {
            mConstraintLayout_ActionRadioButton.setVisibility(View.INVISIBLE);
            mTabLayout.setVisibility(View.VISIBLE);
        }
    }


    //-----------------------------------------------------------------------------------
    //                          OTHER
    //-----------------------------------------------------------------------------------

/*
    private int getTabIconColor(int tab){
        if(mCurrentTheme == THEME_LIGHT){
            if(mActiveFragment == tab)
                return getResources().getColor(R.color.light_colorAccent);

            return getResources().getColor(R.color.light_colorTabIcon);
        }
        else if(mCurrentTheme == THEME_DARK){
            if(mActiveFragment == tab)
                return getResources().getColor(R.color.dark_colorAccent);

            return getResources().getColor(R.color.dark_colorTabIcon);
        }
        else{
            return getResources().getColor(R.color.dark_colorTabIcon);
        }
    }
*/

    private void exitSelectMenu(){
        setActionRadioButtonVisible(false);
        updateFragment(mActiveFragment);
    }

    private void toggleThemeMode() {
        if(mCurrentTheme == THEME_LIGHT){
            Utils.changeToTheme(MainActivity.this, THEME_DARK);
            mVoiceSettings.setTheme(THEME_DARK);
        }
        else if(mCurrentTheme == THEME_DARK){
            Utils.changeToTheme(MainActivity.this, THEME_LIGHT);
            mVoiceSettings.setTheme(THEME_LIGHT);
        }
    }


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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //checkSettings();

        this.startActivity(new Intent(this, this.getClass()));
        this.finish();
        //this.overridePendingTransition(android.R.anim.fade_in,
        //        android.R.anim.fade_out);
    }

    private void checkSettings() {
        Log.v(LOG_TAG,"checkSettings" );
        mSpeaker.setALL();

        if (mVoiceSettings.getTheme() != mCurrentTheme) {
            Utils.changeToTheme(MainActivity.this, mVoiceSettings.getTheme());
        }

        updateFragment(mActiveFragment);
    }

    @Override
    public void onBackPressed() {
        if(mIsActionRadioButtonVisible){
            exitSelectMenu();
            Log.v(LOG_TAG,"onBackPressed mIsActionRadioButtonVisible" );
        }
        else{
            MainActivity.super.onBackPressed();
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        setActionRadioButtonVisible(false);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mSpeaker != null) {
            mSpeaker.destroy();
        }
    }


}