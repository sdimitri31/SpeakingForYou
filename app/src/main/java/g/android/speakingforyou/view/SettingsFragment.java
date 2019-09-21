package g.android.speakingforyou.view;


import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import java.util.Locale;

import g.android.speakingforyou.controller.MainActivity;
import g.android.speakingforyou.controller.SettingsContentObserver;
import g.android.speakingforyou.R;
import g.android.speakingforyou.model.VoiceSettings;

import static android.content.Context.MODE_PRIVATE;
import static g.android.speakingforyou.controller.MainActivity.PREF_NAME;

/**
 * A simple {@link Fragment} subclass.
 */
public class SettingsFragment extends Fragment implements View.OnClickListener, SeekBar.OnSeekBarChangeListener  {

    private static final String LOG_TAG = "SFY : SettingsFragment";

    //Declare callback
    private SettingsFragment.OnButtonClickedListener mCallback;
    private SettingsFragment.OnSeekBarChangeListener mCallbackSeekbar;

    private View            rootView;
    private VoiceSettings   mVoiceSettings;
    AudioManager            mAudioManager;
    SettingsContentObserver mSettingsContentObserver;

    private SeekBar seekBar_Volume;
    private SeekBar seekBar_Speed;
    private SeekBar seekBar_Pitch;
    private LinearLayout linearLayout_Language;
    private TextView textView_SelectedLanguage;
    private Button button_Test;
    private Button button_Reset;
    private Switch switch_TalkMode;
    LinearLayout linearLayout_Theme;
    TextView textView_SelectedTheme;
    private Button button_ClearHistory;
    private Button button_ClearSavedSentences;


    // 1 - Declare our interface that will be implemented by any container activity
    public interface OnButtonClickedListener {
        void onButtonClicked(View view);
    }

    public interface OnSeekBarChangeListener {
        void onProgressChanged(SeekBar seekBar, int i, boolean b);
        void onStartTrackingTouch(SeekBar seekBar);
        void onStopTrackingTouch(SeekBar seekBar);
    }

    // newInstance constructor for creating fragment with arguments
    public static SettingsFragment newInstance() {
        Log.i(LOG_TAG,"newInstance" );
        return new SettingsFragment();
    }

    public SettingsFragment(){
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        //Inflate the layout for this  MainFragment
        rootView = inflater.inflate(R.layout.fragment_settings, container, false);

        Log.i(LOG_TAG,"SettingsFragment onCreateView mVoiceSettings" );
        mVoiceSettings  = new VoiceSettings(getActivity().getSharedPreferences(PREF_NAME, MODE_PRIVATE));
        mAudioManager   = (AudioManager) getActivity().getSystemService(MainActivity.AUDIO_SERVICE);

        //Volume
        seekBar_Volume = rootView.findViewById(R.id.seekBar_Settings_Volume);
        seekBar_Volume.setOnSeekBarChangeListener(this);

        //Language
        linearLayout_Language = rootView.findViewById(R.id.layout_Settings_Language);
        textView_SelectedLanguage = rootView.findViewById((R.id.textView_Settings_SelectedLanguage));
        linearLayout_Language.setOnClickListener(this);

        //Speed
        seekBar_Speed = rootView.findViewById(R.id.seekBar_Settings_Speed);
        seekBar_Speed.setOnSeekBarChangeListener(this);

        //Pitch
        seekBar_Pitch = rootView.findViewById(R.id.seekBar_Settings_Pitch);
        seekBar_Pitch.setOnSeekBarChangeListener(this);

        //Test
        button_Test = rootView.findViewById(R.id.button_Settings_Test);
        button_Test.setOnClickListener(this);

        //Reset
        button_Reset = rootView.findViewById(R.id.button_Settings_Reset);
        button_Reset.setOnClickListener(this);

        //TalkMODE
        switch_TalkMode = rootView.findViewById(R.id.switch_Settings_TalkMode);
        switch_TalkMode.setOnClickListener(this);

        //Theme
        linearLayout_Theme = rootView.findViewById(R.id.layout_Settings_Theme);
        textView_SelectedTheme = rootView.findViewById((R.id.textView_Settings_SelectedTheme));
        linearLayout_Theme.setOnClickListener(this);

        //Clear History
        button_ClearHistory = rootView.findViewById(R.id.button_Settings_ClearHistory);
        button_ClearHistory.setOnClickListener(this);

        //Clear Saved Sentences
        button_ClearSavedSentences = rootView.findViewById(R.id.button_Settings_ClearSavedSentences);
        button_ClearSavedSentences.setOnClickListener(this);

        mSettingsContentObserver = new SettingsContentObserver(getActivity().getApplicationContext(), new Handler(), seekBar_Volume );
        getActivity().getApplicationContext().getContentResolver().registerContentObserver(
                android.provider.Settings.System.CONTENT_URI, true, mSettingsContentObserver );

        updateFields();

        return rootView;
    }

    public void updateFields(){
        Log.i(LOG_TAG,"updateFields");
        seekBar_Volume.setProgress(mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC));
        String languageDisplayName = (Locale.forLanguageTag(mVoiceSettings.getLanguage())).getDisplayName();
        textView_SelectedLanguage.setText(languageDisplayName);
        seekBar_Speed.setProgress(mVoiceSettings.getSpeechRate());
        seekBar_Pitch.setProgress(mVoiceSettings.getPitch());
        String[] selectedTheme = getResources().getStringArray(R.array.theme_array);
        textView_SelectedTheme.setText(selectedTheme[mVoiceSettings.getTheme()]);
        switch_TalkMode.setChecked(mVoiceSettings.getTalkMode());
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        // 4 - Call the method that creating callback after being attached to parent activity
        this.createCallbackToParentActivity();
    }

    // --------------
    // ACTIONS
    // --------------

    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b){}

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {}

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        Log.i(LOG_TAG,"onStopTrackingTouch : " + getResources().getResourceEntryName(seekBar.getId()));
        if((seekBar.getId() == R.id.seekBar_Settings_Pitch) || (seekBar.getId() == R.id.seekBar_Settings_Speed)){
            if(seekBar.getProgress() == 0)
                seekBar.setProgress(1);
        }
        mCallbackSeekbar.onStopTrackingTouch(seekBar);
    }

    @Override
    public void onClick(View v) {
        // 5 - Spread the click to the parent activity
        Log.i(LOG_TAG,"SettingsFragment onClick : " + getResources().getResourceEntryName(v.getId()));
        if(v.getId() == R.id.button_Settings_Reset){
            seekBar_Pitch.setProgress(100);
            seekBar_Speed.setProgress(100);
        }
        mCallback.onButtonClicked(v);
    }

    // --------------
    // FRAGMENT SUPPORT
    // --------------

    // 3 - Create callback to parent activity
    private void createCallbackToParentActivity(){
        try {
            //Parent activity will automatically subscribe to callback
            mCallback = (SettingsFragment.OnButtonClickedListener) getActivity();
            mCallbackSeekbar = (SettingsFragment.OnSeekBarChangeListener) getActivity();
        } catch (ClassCastException e) {
            throw new ClassCastException(e.toString()+ " must implement OnButtonClickedListener");
        }
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        getActivity().getApplicationContext().getContentResolver().unregisterContentObserver(mSettingsContentObserver);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i(LOG_TAG,"onResume");
        updateFields();
    }

}
