package g.android.speakingforyou.controller;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.speech.tts.Voice;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import java.util.List;
import java.util.Locale;

import g.android.speakingforyou.R;
import g.android.speakingforyou.model.History;
import g.android.speakingforyou.model.HistoryDAO;
import g.android.speakingforyou.model.SavedSentencesDAO;
import g.android.speakingforyou.model.Speaker;
import g.android.speakingforyou.model.VoiceSettings;
import g.android.speakingforyou.view.SettingsFragment;

import static g.android.speakingforyou.controller.MainActivity.PREF_NAME;
import static g.android.speakingforyou.controller.MainActivity.THEME_DARK;
import static g.android.speakingforyou.controller.MainActivity.THEME_LIGHT;

public class SettingsActivity extends AppCompatActivity implements SettingsFragment.OnButtonClickedListener, SettingsFragment.OnSeekBarChangeListener {

    public static final String  LOG_TAG = "SFY : SettingsActivity";
    private VoiceSettings       mVoiceSettings;
    private Speaker mSpeaker;
    AudioManager                mAudioManager;

    private int                 mCurrentTheme;
    private ImageButton         mButton_Settings;
    private ImageButton         mButton_ThemeMode;

    //Databases
    final private SavedSentencesDAO mSavedSentencesDAO = new SavedSentencesDAO(this);
    final private HistoryDAO mHistoryDAO = new HistoryDAO(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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

        setContentView(R.layout.activity_settings);


        mSpeaker        = new Speaker(this, mVoiceSettings);
        mAudioManager   = (AudioManager) getSystemService(MainActivity.AUDIO_SERVICE);

        //HEADER
        mButton_Settings            =   findViewById(R.id.imageButton_Settings);
        mButton_ThemeMode           =   findViewById(R.id.imageButton_ThemeMode);

        //--------------------
        //HEADER Listeners
        mButton_Settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        mButton_ThemeMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleThemeMode();
            }
        });
    }

    @Override
    public void onButtonClicked(View view) {
        Log.i(LOG_TAG,"CallBack received  : onButtonClicked " + getResources().getResourceEntryName(view.getId()));
        switch (view.getId()){
            case R.id.switch_Settings_TalkMode:
                setTalkMode(((Switch) view).isChecked());
                break;

            case R.id.layout_Settings_Language:
                languageSelector();
                break;

            case R.id.layout_Settings_Voice:
                voiceSelector();
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

            case R.id.button_Settings_About:
                aboutPopup();
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
        mSpeaker.setVoice(0);
    }

    private void setVoice(int voiceID){
        mSpeaker.setVoice(voiceID);
    }

    private void themeSelector(){
        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(this, getAlertDialogStyle()));

        //set the title for alert dialog
        builder.setTitle(getResources().getString(R.string.textView_Settings_Theme));

        //set items to alert dialog. i.e. our array , which will be shown as list view in alert dialog
        builder.setItems(getResources().getStringArray(R.array.theme_array), new DialogInterface.OnClickListener() {

            @Override public void onClick(DialogInterface dialog, int item) {
                if (mVoiceSettings.getTheme() != item) {
                    Utils.changeToTheme(SettingsActivity.this, item);
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

        String[] array = new String[mSpeaker.getListLocales().size()];
        int index = 0;
        int selectedLocale = 0;
        String tagToFind = mVoiceSettings.getLanguage();
        for (Locale value : mSpeaker.getListLocales()) {
            array[index] = value.getDisplayName();
            if(tagToFind.equals(value.toLanguageTag())){
                selectedLocale = index;
            }
            index++;
        }

        // add a radio button list
        builder.setSingleChoiceItems(array, selectedLocale, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                Log.i(LOG_TAG, "setSingleChoiceItems " + item);
                setLanguage(mSpeaker.getListLocales().get(item).toLanguageTag());
                updateFragmentSettings();
                dialog.dismiss();
            }
        });

        builder.setNegativeButton(getResources().getString(R.string.alertDialog_Cancel), null);

        //Creating alert dialog
        AlertDialog alert = builder.create();

        //Showing alert dialog
        alert.show();
    }

    private void voiceSelector(){
        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(this, getAlertDialogStyle()));

        //set the title for alert dialog
        builder.setTitle(getResources().getString(R.string.textView_QuickSettings_Voice));

        String[] array = new String[mSpeaker.getListVoices().size()];
        int index = 0;
        for (Voice value : mSpeaker.getListVoices()) {
            array[index] = value.getName();
            index++;
        }

        // add a radio button list
        builder.setSingleChoiceItems(array, mVoiceSettings.getLastVoiceUsed(), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                Log.i(LOG_TAG, "setSingleChoiceItems " + item);
                setVoice(item);
                updateFragmentSettings();
                dialog.dismiss();
            }
        });

        builder.setNegativeButton(getResources().getString(R.string.alertDialog_Cancel), null);

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
        builder.setMessage(getResources().getString(R.string.alertDialog_DeleteSavedSentencesQuestion))
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

    public void sortHistoryPopup(){
        LayoutInflater inflater = getLayoutInflater();
        View alertLayout = inflater.inflate(R.layout.history_sorting_popup, null);

        final RadioGroup radioGroupSort = alertLayout.findViewById(R.id.radioGroup_History_SortBy);
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
                updateFragmentSettings();
            }
        });
        AlertDialog dialog = alert.create();
        dialog.show();
    }

    public History findUsingEnhancedForLoop(
            String sentence, List<History> historyList) {

        for (History history : historyList) {
            Log.v(LOG_TAG,"findUsingEnhancedForLoop : " + history.getSentence() +" ? " + sentence);
            if (history.getSentence().toLowerCase().equals(sentence.toLowerCase())) {
                Log.v(LOG_TAG,"      Match  : " + history.getSentence() +" = " + sentence);
                return history;
            }
        }
        return null;
    }


    public void aboutPopup(){
        LayoutInflater inflater = getLayoutInflater();
        View alertLayout = inflater.inflate(R.layout.about_popup, null);

        TextView tv = alertLayout.findViewById(R.id.textView_about_version);
        tv.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                final String appPackageName = getPackageName(); // getPackageName() from Context or Activity object
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                } catch (Exception e) {
                    Log.d(LOG_TAG,"Message ="+e);

                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + appPackageName)));
                }
            }
        });

        AlertDialog.Builder alert = new AlertDialog.Builder(new ContextThemeWrapper(this, getAlertDialogStyle()));
        // this set the view from XML inside AlertDialog
        alert.setView(alertLayout);

        alert.setPositiveButton(getResources().getString(R.string.alertDialog_OK), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog = alert.create();
        dialog.show();
    }

    private void updateFragmentSettings(){
        Log.v(LOG_TAG,"updateFragmentSettings");
        FragmentManager fm = getSupportFragmentManager();
        SettingsFragment fragment = (SettingsFragment)fm.findFragmentById(R.id.fragmentView_settings);
        fragment.updateFields();
    }


    private void toggleThemeMode() {
        if(mCurrentTheme == THEME_LIGHT){
            Utils.changeToTheme(SettingsActivity.this, THEME_DARK);
            mVoiceSettings.setTheme(THEME_DARK);
        }
        else if(mCurrentTheme == THEME_DARK){
            Utils.changeToTheme(SettingsActivity.this, THEME_LIGHT);
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
}
