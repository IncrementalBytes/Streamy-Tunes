package net.whollynugatory.streamytunes.android;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

import net.whollynugatory.streamytunes.android.ui.BaseActivity;

public class MusicIntentReceiver extends BroadcastReceiver {

  private final static String TAG = BaseActivity.BASE_TAG + MusicIntentReceiver.class.getSimpleName();

  @Override
  public void onReceive(Context context, Intent intent) {

    Log.d(TAG, "++onReceive(Context, Intent)");
    if (intent.getAction().equals(android.media.AudioManager.ACTION_AUDIO_BECOMING_NOISY)) {
      Toast.makeText(context, "Headphones disconnected.", Toast.LENGTH_SHORT).show();

      // send an intent to our MusicService to telling it to pause the audio
      context.startService(new Intent(BaseActivity.ACTION_PAUSE));

    } else if (intent.getAction().equals(Intent.ACTION_MEDIA_BUTTON)) {
      KeyEvent keyEvent = (KeyEvent) intent.getExtras().get(Intent.EXTRA_KEY_EVENT);
      if (keyEvent.getAction() != KeyEvent.ACTION_DOWN) {
        return;
      }

      switch (keyEvent.getKeyCode()) {
//        case KeyEvent.KEYCODE_HEADSETHOOK:
//        case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
//          context.startService(new Intent(BaseActivity.ACTION_TOGGLE_PLAYBACK));
//          break;
        case KeyEvent.KEYCODE_MEDIA_PLAY:
          context.startService(new Intent(BaseActivity.ACTION_PLAY));
          break;
        case KeyEvent.KEYCODE_MEDIA_PAUSE:
          context.startService(new Intent(BaseActivity.ACTION_PAUSE));
          break;
        case KeyEvent.KEYCODE_MEDIA_NEXT:
          context.startService(new Intent(BaseActivity.ACTION_NEXT));
          break;
        case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
          context.startService(new Intent(BaseActivity.ACTION_PREVIOUS));
          break;
      }
    }
  }
}
