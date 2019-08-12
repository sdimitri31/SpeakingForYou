package g.android.speakingforyou.Controller;

import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import java.util.Locale;

import g.android.speakingforyou.Model.HistoryDAO;
import g.android.speakingforyou.Model.SavedSentencesDAO;
import g.android.speakingforyou.Model.Speaker;
import g.android.speakingforyou.Model.VoiceSettings;
import g.android.speakingforyou.R;

import static g.android.speakingforyou.Controller.MainActivity.PREF_NAME;

public class SettingsActivity extends AppCompatActivity {


    SettingsContentObserver mSettingsContentObserver;
    AudioManager            mAudioManager;
    VoiceSettings           mVoiceSettings;
    private Speaker         mSpeaker;

    SeekBar                 seekBar_Volume;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        assert getSupportActionBar() != null;   //null check
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);   //show back button
        setContentView(R.layout.activity_settings);

        mVoiceSettings  = new VoiceSettings(getSharedPreferences(PREF_NAME, MODE_PRIVATE));
        mSpeaker        = new Speaker(this, mVoiceSettings);
        mAudioManager   = (AudioManager) getSystemService(SettingsActivity.AUDIO_SERVICE);

        //Volume
        seekBar_Volume = findViewById(R.id.seekBar_Settings_Volume);
        seekBar_Volume.setProgress(mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC));
        seekBar_Volume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, seekBar.getProgress(), 0);
            }
        });

        //Language
        LinearLayout linearLayout_Language = findViewById(R.id.layout_Settings_Language);
        final TextView textView_SelectedLanguage = findViewById((R.id.textView_Settings_SelectedLanguage));
        final String languageDisplayName = (Locale.forLanguageTag(mVoiceSettings.getLanguage()).getDisplayName());
        textView_SelectedLanguage.setText(languageDisplayName);
        linearLayout_Language.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                languageSelector(textView_SelectedLanguage);
            }
        });

        //Speed
        final SeekBar seekBar_Speed = findViewById(R.id.seekBar_Settings_Speed);
        seekBar_Speed.setProgress(mVoiceSettings.getSpeechRate());
        seekBar_Speed.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                 @Override
                 public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

                 }

                 @Override
                 public void onStartTrackingTouch(SeekBar seekBar) {

                 }

                 @Override
                 public void onStopTrackingTouch(SeekBar seekBar) {
                     if(seekBar.getProgress() == 0)
                         seekBar.setProgress(1);
                     setSpeechRate(seekBar.getProgress());
                 }
             }
        );

        //Pitch
        final SeekBar seekBar_Pitch = findViewById(R.id.seekBar_Settings_Pitch);
        seekBar_Pitch.setProgress(mVoiceSettings.getPitch());
        seekBar_Pitch.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if(seekBar.getProgress() == 0)
                    seekBar.setProgress(1);
                setPitch(seekBar.getProgress());

            }
        });

        //Test
        Button button_Test = findViewById(R.id.button_Settings_Test);
        button_Test.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String testSentence = getResources().getString(R.string.settings_TestSentence);
                mSpeaker.ttsSpeak(testSentence);
            }
        });

        //Reset
        Button button_Reset = findViewById(R.id.button_Settings_Reset);
        button_Reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                seekBar_Pitch.setProgress(100);
                setPitch(100);

                seekBar_Speed.setProgress(100);
                setSpeechRate(100);
            }
        });

        //TalkMODE
        final Switch switch_TalkMode = findViewById(R.id.switch_Settings_TalkMode);
        switch_TalkMode.setChecked(mVoiceSettings.getTalkMode());
        switch_TalkMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setTalkMode(switch_TalkMode.isChecked());
            }
        });

        //Theme
        /*
        LinearLayout linearLayout_Theme = findViewById(R.id.layout_Settings_Theme);
        TextView textView_SelectedTheme = findViewById((R.id.textView_Settings_SelectedTheme));
        String selectedTheme = (Locale.forLanguageTag(mVoiceSettings.getLanguage()).getDisplayName());
        textView_SelectedTheme.setText(selectedTheme);
        linearLayout_Theme.setOnClickListener(this);
        */

        //Clear History
        Button button_ClearHistory = findViewById(R.id.button_Settings_ClearHistory);
        button_ClearHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearHistory();
            }
        });

        //Clear Saved Sentences
        Button button_ClearSavedSentences = findViewById(R.id.button_Settings_ClearSavedSentences);
        button_ClearSavedSentences.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearSavedSentences();
            }
        });



        mSettingsContentObserver = new SettingsContentObserver(getApplicationContext(), new Handler(), seekBar_Volume );
        getApplicationContext().getContentResolver().registerContentObserver(
                android.provider.Settings.System.CONTENT_URI, true, mSettingsContentObserver );

    }

    private void languageSelector(final TextView selectedLanguage){
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);

        //set the title for alert dialog
        builder.setTitle(getResources().getString(R.string.textView_QuickSettings_Language));

        //set items to alert dialog. i.e. our array , which will be shown as list view in alert dialog
        builder.setItems(mSpeaker.getListLanguageName().toArray(new String[mSpeaker.getListLanguageName().size()]),
                new DialogInterface.OnClickListener() {

                    @Override public void onClick(DialogInterface dialog, int item) {
                        setLanguage(mSpeaker.getListLanguageTag().get(item));
                        selectedLanguage.setText(mSpeaker.getListLanguageName().get(item));
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

    private void clearHistory(){
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        //Yes button clicked
                        final HistoryDAO historyDAO = new HistoryDAO(getApplicationContext());
                        historyDAO.deleteAll();
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        //No button clicked
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getResources().getString(R.string.alertDialog_DeleteHistoryQuestion)).setPositiveButton(getResources().getString(R.string.alertDialog_OK), dialogClickListener)
                .setNegativeButton(getResources().getString(R.string.alertDialog_Cancel), dialogClickListener).show();
    }

    private void clearSavedSentences(){
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        //Yes button clicked
                        final SavedSentencesDAO savedSentencesDAO = new SavedSentencesDAO(getApplicationContext());
                        savedSentencesDAO.deleteAll();
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        //No button clicked
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getResources().getString(R.string.alertDialog_DeleteSavecSentencesQuestion)).setPositiveButton(getResources().getString(R.string.alertDialog_OK), dialogClickListener)
                .setNegativeButton(getResources().getString(R.string.alertDialog_Cancel), dialogClickListener).show();
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

    @Override
    public boolean onSupportNavigateUp(){
        Intent returnIntent = new Intent();
        //returnIntent.putExtra("volume", seekBar_Volume.getProgress());
        setResult(RESULT_OK, returnIntent);
        finish();
        return true;
    }


    @Override
    public void onDestroy(){
        super.onDestroy();
        getApplicationContext().getContentResolver().unregisterContentObserver(mSettingsContentObserver);
    }
}
