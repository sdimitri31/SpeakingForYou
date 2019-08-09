package g.android.speakingforyou.Controller;

import android.content.Context;
import android.database.ContentObserver;
import android.media.AudioManager;
import android.os.Handler;
import android.util.Log;
import android.widget.SeekBar;


public class SettingsContentObserver extends ContentObserver {

    int previousVolume;
    Context context;
    SeekBar volume;

    public SettingsContentObserver(Context c, Handler handler, SeekBar volume) {
        super(handler);
        context=c;
        this.volume = volume;

        AudioManager audio = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        previousVolume = audio.getStreamVolume(AudioManager.STREAM_MUSIC);
    }

    @Override
    public boolean deliverSelfNotifications() {
        return super.deliverSelfNotifications();
    }

    @Override
    public void onChange(boolean selfChange) {
        super.onChange(selfChange);

        AudioManager audio = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        int currentVolume = audio.getStreamVolume(AudioManager.STREAM_MUSIC);

        int delta=previousVolume-currentVolume;

        if(delta>0)
        {
            Log.d("TTS", "Volume Decreased from : " + previousVolume + " to : " + currentVolume);
            previousVolume=currentVolume;
            volume.setProgress(currentVolume);
        }
        else if(delta<0)
        {
            Log.d("TTS", "Volume Increased from : " + previousVolume + " to : " + currentVolume);
            previousVolume=currentVolume;
            volume.setProgress(currentVolume);
        }
    }
}