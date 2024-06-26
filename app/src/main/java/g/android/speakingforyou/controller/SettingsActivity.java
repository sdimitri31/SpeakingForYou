package g.android.speakingforyou.controller;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.RemoteException;
import android.speech.tts.Voice;
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
import android.widget.Toast;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.AcknowledgePurchaseResponseListener;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchaseHistoryRecord;
import com.android.billingclient.api.PurchaseHistoryResponseListener;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;
import com.larswerkman.holocolorpicker.ColorPicker;
import com.larswerkman.holocolorpicker.SaturationBar;
import com.larswerkman.holocolorpicker.ValueBar;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
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

public class SettingsActivity extends AppCompatActivity implements SettingsFragment.OnButtonClickedListener, SettingsFragment.OnSeekBarChangeListener, PurchasesUpdatedListener {

    public static final String  LOG_TAG = "SFY : SettingsActivity";
    private BillingClient billingClient;
    private AcknowledgePurchaseResponseListener acknowledgePurchaseResponseListener;

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

        new ThemeColors(this);
        setContentView(R.layout.activity_settings);

        connectToPlayStore();

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
                onBackPressed();
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

            case R.id.layout_Settings_AccentColor:
                if(mVoiceSettings.getIsAccentColorBought())
                    openAccentColorPopup();
                else buyChangeColor();
                break;

            case R.id.button_Settings_Unlock_AccentColor:
                buyChangeColor();
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

    private void connectToPlayStore(){
        //Attempting to connect to play store
        billingClient = BillingClient.newBuilder(this).enablePendingPurchases().setListener(this).build();
        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(BillingResult billingResult) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    // The BillingClient is ready. You can query purchases here.
                    Log.i(LOG_TAG,"onBillingSetupFinished OK ");
                    //Check if the ColorAccent has been purchased and update Prefs
                    isColorAccentBought();
                }
            }
            @Override
            public void onBillingServiceDisconnected() {
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
                Log.i(LOG_TAG,"onBillingServiceDisconnected ");
            }
        });
    }

    private void buyChangeColor(){
        //If we are connected to the play store
        if(billingClient.isReady()){

            //Check if we didn't already bought it, Bought = open accent color popup, Or open buy menu
            if (mVoiceSettings.getIsAccentColorBought()) {
                Log.i(LOG_TAG,"accent_color_unlock purchased. Opening Accent popup ");
                openAccentColorPopup();
            }
            else{
                //Setup the available items to buy
                List<String> skuList = new ArrayList<> ();
                skuList.add("accent_color_unlock");

                SkuDetailsParams.Builder params = SkuDetailsParams.newBuilder();
                params.setSkusList(skuList).setType(BillingClient.SkuType.INAPP);

                billingClient.querySkuDetailsAsync(params.build(),
                    new SkuDetailsResponseListener() {
                        @Override
                        public void onSkuDetailsResponse(BillingResult billingResult,
                                                         List<SkuDetails> skuDetailsList) {
                            // Process the result.
                            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                                try {
                                    //Open buy menu for the item accent_color_unlock
                                    Log.i(LOG_TAG,"accent_color_unlock not purchased. Opening buying popup ");
                                    BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder().setSkuDetails(skuDetailsList.get(0)).build();
                                    billingClient.launchBillingFlow(SettingsActivity.this, billingFlowParams);
                                }
                                catch (Exception e){
                                    Log.i(LOG_TAG,"Exception " + e.getLocalizedMessage());
                                }
                            }
                            if(billingResult.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED){
                                updateFragmentSettings();
                            }
                        }
                    });
            }
        }

    }

    private boolean isColorAccentBought(){
        if(billingClient.isReady()) {
            //Setup the available items to buy
            List<String> skuList = new ArrayList<>();
            skuList.add("accent_color_unlock");

            SkuDetailsParams.Builder params = SkuDetailsParams.newBuilder();
            params.setSkusList(skuList).setType(BillingClient.SkuType.INAPP);

            //Check if we didn't already bought it, Bought = open accent color popup, Or open buy menu
            Purchase.PurchasesResult purchasesResult = billingClient.queryPurchases(BillingClient.SkuType.INAPP);
            if (purchasesResult.getPurchasesList() != null) {
                for (Purchase purchase : purchasesResult.getPurchasesList()) {
                    Log.i(LOG_TAG, "getPurchaseState " + purchase.getPurchaseState());
                    if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
                        Log.i(LOG_TAG, "getOrderId " + purchase.getOrderId());
                        Log.i(LOG_TAG, "getSku " + purchase.getSku());
                        mVoiceSettings.setIsAccentColorBought(true);
                        updateFragmentSettings();
                        return true;
                    }
                }
            }
            mVoiceSettings.setIsAccentColorBought(false);
        }
        return false;
    }

    private void openAccentColorPopup(){
        LayoutInflater inflater = getLayoutInflater();
        View alertLayout = inflater.inflate(R.layout.colorpicker_popup, null);

        final ColorPicker picker = (ColorPicker) alertLayout.findViewById(R.id.picker);
        SaturationBar saturationBar = (SaturationBar) alertLayout.findViewById(R.id.saturationbar);
        ValueBar valueBar = (ValueBar) alertLayout.findViewById(R.id.valuebar);

        picker.addSaturationBar(saturationBar);
        picker.addValueBar(valueBar);
        ThemeColors themeColors = new ThemeColors(this);
        picker.setOldCenterColor(themeColors.color);
        picker.setColor(themeColors.color);

        AlertDialog.Builder alert = new AlertDialog.Builder(new ContextThemeWrapper(this, getAlertDialogStyle()));
        // this set the view from XML inside AlertDialog
        alert.setView(alertLayout);

        alert.setPositiveButton(getResources().getString(R.string.alertDialog_OK), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                int red = Color.red(picker.getColor());
                int green = Color.green(picker.getColor());
                int blue = Color.blue(picker.getColor());

                ThemeColors.setNewThemeColor(SettingsActivity.this, red, green, blue);
                Log.i(LOG_TAG, "picker.getColor " + picker.getColor());
                dialog.dismiss();
            }
        });

        alert.setNegativeButton(getResources().getString(R.string.alertDialog_Cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        AlertDialog dialog = alert.create();
        dialog.show();
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

    @Override
    public void onPurchasesUpdated(BillingResult billingResult, List<Purchase> list) {
        Log.i(LOG_TAG,"onPurchasesUpdated " + billingResult.getResponseCode());
        if(list == null)
            return;

        Purchase purchase = list.get(0);

        if(purchase.getPurchaseState() == Purchase.PurchaseState.PENDING){
            Log.i(LOG_TAG,"     PENDING ");
            Toast.makeText(SettingsActivity.this,"Purchase pending", Toast.LENGTH_SHORT).show();
        }
        else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
            Log.i(LOG_TAG, "     OK " + list.size());
            if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
                //Acknowledge purchase
                if (!purchase.isAcknowledged()) {
                    AcknowledgePurchaseParams acknowledgePurchaseParams =
                            AcknowledgePurchaseParams.newBuilder()
                                    .setPurchaseToken(purchase.getPurchaseToken())
                                    .build();
                    billingClient.acknowledgePurchase(acknowledgePurchaseParams, new AcknowledgePurchaseResponseListener() {
                        @Override
                        public void onAcknowledgePurchaseResponse(BillingResult billingResult) {
                            Log.i(LOG_TAG, "     Purchase acknowledged ");

                            mVoiceSettings.setIsAccentColorBought(true);
                            Intent intent = getIntent();
                            finish();
                            startActivity(intent);
                            overridePendingTransition(android.R.anim.fade_in,
                                    android.R.anim.fade_out);
                        }
                    });
                }
            }
        }
        else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED) {
            // Handle an error caused by a user cancelling the purchase flow.
            Log.d(LOG_TAG, "onPurchasesUpdated() response: User cancelled" + billingResult.getResponseCode());
        } else {
            // Handle any other error codes.
            Log.d(LOG_TAG, "onPurchasesUpdated() response: Other" + billingResult.getResponseCode());
        }


    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        billingClient.endConnection();
        mSpeaker.destroy();
    }

    @Override
    public void onResume(){
        super.onResume();
        updateFragmentSettings();
    }

    @Override
    public void onBackPressed(){
        super.onBackPressed();
        finishActivity(1);
        overridePendingTransition(0,R.anim.slide_out_down);

    }
}
