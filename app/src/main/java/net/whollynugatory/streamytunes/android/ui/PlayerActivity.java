package net.whollynugatory.streamytunes.android.ui;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;

import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ui.PlayerView;

import net.whollynugatory.streamytunes.android.R;
import net.whollynugatory.streamytunes.android.db.MediaDetails;

import java.util.List;

public class PlayerActivity extends BaseActivity {

  private static final String TAG = BaseActivity.BASE_TAG + PlayerActivity.class.getSimpleName();

  private PlayerView mPlayerView;

  private int mCurrentWindow = 0;
  private List<MediaDetails> mMediaDetailsList;
  private SimpleExoPlayer mPlayer;
  private boolean mPlayWhenReady = true;
  private long mPlaybackPosition = 0;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    Log.d(TAG, "++onCreate(Bundle)");
    setContentView(R.layout.activity_player);
    mPlayerView = findViewById(R.id.player_video_view);
    mMediaDetailsList = (List<MediaDetails>) getIntent().getSerializableExtra(BaseActivity.ARG_MEDIA_DETAILS_LIST);
  }

  @Override
  public void onStart() {
    super.onStart();

    Log.d(TAG, "++onStart()");
    initializePlayer();
  }

  @Override
  public void onResume() {
    super.onResume();

    Log.d(TAG, "++onResume()");
//    hideSystemUi();
    if (mPlayer == null) {
      initializePlayer();
    }
  }

  @Override
  public void onPause() {
    super.onPause();

    Log.d(TAG, "++onPause()");
    releasePlayer();
  }

  @Override
  public void onStop() {
    super.onStop();

    Log.d(TAG, "++onStop()");
    releasePlayer();
    setResult(RESULT_OK);
    finish();
  }

  @SuppressLint("InlinedApi")
  private void hideSystemUi() {

    Log.d(TAG, "++hideSystemUi()");
    mPlayerView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
      | View.SYSTEM_UI_FLAG_FULLSCREEN
      | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
      | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
      | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
      | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
  }

  private void initializePlayer() {

    Log.d(TAG, "++initializePlayer()");
    mPlayer = new SimpleExoPlayer.Builder(this).build();
    mPlayerView.setPlayer(mPlayer);
    boolean startAdding = false;
    for (MediaDetails mediaDetails : mMediaDetailsList) {
      Uri localSource = Uri.withAppendedPath(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, "" + mediaDetails.MediaId);
      if (startAdding) {
        mPlayer.addMediaItem(MediaItem.fromUri(localSource));
      } else {
        startAdding = true;
        mPlayer.setMediaItem(MediaItem.fromUri(localSource));
      }
    }

    mPlayer.setPlayWhenReady(mPlayWhenReady);
    mPlayer.seekTo(mCurrentWindow, mPlaybackPosition);
    mPlayer.prepare();
  }

  private void releasePlayer() {

    Log.d(TAG, "++releasePlayer()");
    if (mPlayer != null) {
      mPlayWhenReady = mPlayer.getPlayWhenReady();
      mPlaybackPosition = mPlayer.getCurrentPosition();
      mCurrentWindow = mPlayer.getCurrentWindowIndex();
      mPlayer.release();
      mPlayer = null;
    }
  }
}
