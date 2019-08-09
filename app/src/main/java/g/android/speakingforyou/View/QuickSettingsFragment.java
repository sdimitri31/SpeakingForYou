package g.android.speakingforyou.View;


import android.content.Context;
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

import g.android.speakingforyou.Controller.SettingsContentObserver;
import g.android.speakingforyou.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class QuickSettingsFragment extends Fragment implements View.OnClickListener, SeekBar.OnSeekBarChangeListener  {

    //2 - Declare callback
    private QuickSettingsFragment.OnButtonClickedListener mCallback;
    private QuickSettingsFragment.OnSeekBarChangeListener mCallbackSeekbar;

    SettingsContentObserver mSettingsContentObserver;

    // 1 - Declare our interface that will be implemented by any container activity
    public interface OnButtonClickedListener {
        void onButtonClicked(View view);
    }

    public interface OnSeekBarChangeListener {
        void onProgressChanged(SeekBar seekBar, int i, boolean b);
        void onStartTrackingTouch(SeekBar seekBar);
        void onStopTrackingTouch(SeekBar seekBar);
    }

    public QuickSettingsFragment(){
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        //Inflate the layout for this  MainFragment
        View rootView = inflater.inflate(R.layout.fragment_quick_settings, container, false);

        //Volume
        SeekBar seekBar_Volume = rootView.findViewById(R.id.seekBar_QuickSettings_Volume);
        seekBar_Volume.setProgress(getArguments().getInt("volume", 7));
        seekBar_Volume.setOnSeekBarChangeListener(this);

        //Language
        LinearLayout linearLayout_Language = rootView.findViewById(R.id.layout_QuickSettings_Language);
        TextView textView_SelectedLanguage = rootView.findViewById((R.id.textView_QuickSettings_SelectedLanguage));
        String languageDisplayName = (Locale.forLanguageTag(getArguments().getString("language", ""))).getDisplayName();
        textView_SelectedLanguage.setText(languageDisplayName);
        linearLayout_Language.setOnClickListener(this);

        //Speed
        SeekBar seekBar_Speed = rootView.findViewById(R.id.seekBar_QuickSettings_Speed);
        seekBar_Speed.setProgress(getArguments().getInt("speed", 100));
        seekBar_Speed.setOnSeekBarChangeListener(this);

        //Pitch
        SeekBar seekBar_Pitch = rootView.findViewById(R.id.seekBar_QuickSettings_Pitch);
        seekBar_Pitch.setProgress(getArguments().getInt("pitch", 100));
        seekBar_Pitch.setOnSeekBarChangeListener(this);

        //TalkMODE
        Switch switch_TalkMode = rootView.findViewById(R.id.switch_QuickSettings_TalkMode);
        switch_TalkMode.setChecked(getArguments().getBoolean("talkMode", false));
        switch_TalkMode.setOnClickListener(this);

        //More Settings
        Button button_MoreSettings = rootView.findViewById(R.id.button_QuickSettings_MoreSettings);
        button_MoreSettings.setOnClickListener(this);

        mSettingsContentObserver = new SettingsContentObserver(getActivity().getApplicationContext(), new Handler(), seekBar_Volume );
        getActivity().getApplicationContext().getContentResolver().registerContentObserver(
                android.provider.Settings.System.CONTENT_URI, true, mSettingsContentObserver );


        return rootView;
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
    public void onProgressChanged(SeekBar seekBar, int i, boolean b){
        //Log.i("TTS","QuickSettingsFragment onProgressChanged : " + getResources().getResourceEntryName(seekBar.getId()));
        switch (seekBar.getId())
        {
            case R.id.seekBar_QuickSettings_Pitch:
                if(seekBar.getProgress() == 0)
                    seekBar.setProgress(1);
                break;

            case R.id.seekBar_QuickSettings_Speed:
                if(seekBar.getProgress() == 0)
                    seekBar.setProgress(1);
                break;
        }

        mCallbackSeekbar.onProgressChanged(seekBar, i, b);
    }
    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        //Log.i("TTS","QuickSettingsFragment onStartTrackingTouch : " + getResources().getResourceEntryName(seekBar.getId()));
        switch (seekBar.getId())
        {
            case R.id.seekBar_QuickSettings_Pitch:
                if(seekBar.getProgress() == 0)
                    seekBar.setProgress(1);
                break;

            case R.id.seekBar_QuickSettings_Speed:
                if(seekBar.getProgress() == 0)
                    seekBar.setProgress(1);
                break;
        }
        mCallbackSeekbar.onStartTrackingTouch(seekBar);
    }
    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        Log.i("TTS","QuickSettingsFragment onStopTrackingTouch : " + getResources().getResourceEntryName(seekBar.getId()));
        switch (seekBar.getId())
        {
            case R.id.seekBar_QuickSettings_Pitch:
                if(seekBar.getProgress() == 0)
                    seekBar.setProgress(1);
                break;

            case R.id.seekBar_QuickSettings_Speed:
                if(seekBar.getProgress() == 0)
                    seekBar.setProgress(1);
                break;
        }
        mCallbackSeekbar.onStopTrackingTouch(seekBar);
    }

    @Override
    public void onClick(View v) {
        // 5 - Spread the click to the parent activity
        Log.i("TTS","QuickSettingsFragment onClick : " + getResources().getResourceEntryName(v.getId()));
        mCallback.onButtonClicked(v);
    }

    // --------------
    // FRAGMENT SUPPORT
    // --------------

    // 3 - Create callback to parent activity
    private void createCallbackToParentActivity(){
        try {
            //Parent activity will automatically subscribe to callback
            mCallback = (QuickSettingsFragment.OnButtonClickedListener) getActivity();
            mCallbackSeekbar = (QuickSettingsFragment.OnSeekBarChangeListener) getActivity();
        } catch (ClassCastException e) {
            throw new ClassCastException(e.toString()+ " must implement OnButtonClickedListener");
        }
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        getActivity().getApplicationContext().getContentResolver().unregisterContentObserver(mSettingsContentObserver);
    }
}
