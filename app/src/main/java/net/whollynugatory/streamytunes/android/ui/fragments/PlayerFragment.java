package net.whollynugatory.streamytunes.android.ui.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ui.PlayerView;

import net.whollynugatory.streamytunes.android.R;
import net.whollynugatory.streamytunes.android.Utils;
import net.whollynugatory.streamytunes.android.db.views.SongDetails;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PlayerFragment extends Fragment {

  private static final String TAG = Utils.BASE_TAG + "PlayerFragment";

  private PlaybackStateListener mPlaybackStateListener;

  public interface OnPlayerListener {

    void onPlayerSongComplete();
  }

  private OnPlayerListener mCallback;
  private int mCurrentWindow = 0;
  private long mPlaybackPosition = 0;
  private SimpleExoPlayer mPlayer;
  private PlayerView mPlayerView;
  private boolean mPlayWhenReady = true;
  private List<SongDetails> mSongDetailsList;

  @SuppressLint("InlinedApi")
  private void hideSystemUi() {
    mPlayerView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
      | View.SYSTEM_UI_FLAG_FULLSCREEN
      | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
      | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
      | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
      | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
  }

  public static PlayerFragment newInstance(ArrayList<SongDetails> songDetailsList) {

    Log.d(TAG, "++newInstance(Collection<SongDetails>)");
    Bundle arguments = new Bundle();
    arguments.putParcelableArrayList(Utils.ARG_SONG_DETAILS_LIST, songDetailsList);
    PlayerFragment fragment = new PlayerFragment();
    fragment.setArguments(arguments);
    return fragment;
  }

  @Override
  public void onAttach(@NonNull Context context) {
    super.onAttach(context);

    Log.d(TAG, "++onAttach(Context)");
    try {
      mCallback = (OnPlayerListener) context;
    } catch (ClassCastException e) {
      throw new ClassCastException(
        String.format(Locale.ENGLISH, "Missing interface implementations for %s", context.toString()));
    }

    Bundle arguments = getArguments();
    if (arguments != null) {
      mSongDetailsList = arguments.getParcelableArrayList(Utils.ARG_SONG_DETAILS_LIST);
    } else {
      String message = "Arguments were null.";
      Log.e(TAG, message);
    }
  }

  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

    Log.d(TAG, "++onCreateView(LayoutInflater, ViewGroup, Bundle)");
    mPlaybackStateListener = new PlaybackStateListener();
    View view = inflater.inflate(R.layout.fragment_player, container, false);
    mPlayerView = view.findViewById(R.id.player_view);
    return view;
  }

  @Override
  public void onDetach() {
    super.onDetach();

    Log.d(TAG, "++onDetach()");
    mCallback = null;
  }

  @Override
  public void onStart() {
    super.onStart();

    initializePlayer();
  }

  @Override
  public void onResume() {
    super.onResume();

    if (mPlayer == null) {
      initializePlayer();
    }
  }

  @Override
  public void onPause() {
    super.onPause();

    releasePlayer();
  }

  @Override
  public void onStop() {
    super.onStop();

    releasePlayer();
  }

  private void initializePlayer() {

    Log.d(TAG, "++initializePlayer()");
    mPlayer = new SimpleExoPlayer.Builder(getActivity()).build();
    mPlayerView.setPlayer(mPlayer);

    boolean startAdding = false;
    for (SongDetails songDetails : mSongDetailsList) {
      if (startAdding) {
        mPlayer.addMediaItem(MediaItem.fromUri(songDetails.LocalSource));
      } else {
        startAdding = true;
        mPlayer.setMediaItem(MediaItem.fromUri(songDetails.LocalSource));
      }
    }

    mPlayer.setPlayWhenReady(mPlayWhenReady);
    mPlayer.seekTo(mCurrentWindow, mPlaybackPosition);
    mPlayer.addListener(mPlaybackStateListener);
    mPlayer.prepare();
  }

  private void releasePlayer() {

    if (mPlayer != null) {
      mPlayWhenReady = mPlayer.getPlayWhenReady();
      mPlaybackPosition = mPlayer.getCurrentPosition();
      mCurrentWindow = mPlayer.getCurrentWindowIndex();
      mPlayer.removeListener(mPlaybackStateListener);
      mPlayer.release();
      mPlayer = null;
    }
  }

  private static class PlaybackStateListener implements Player.EventListener {

    @Override
    public void onPlaybackStateChanged(int playbackState) {

      String stateString;
      switch (playbackState) {
        case ExoPlayer.STATE_IDLE:
          stateString = "ExoPlayer.STATE_IDLE      -";
          break;
        case ExoPlayer.STATE_BUFFERING:
          stateString = "ExoPlayer.STATE_BUFFERING -";
          break;
        case ExoPlayer.STATE_READY:
          stateString = "ExoPlayer.STATE_READY     -";
          break;
        case ExoPlayer.STATE_ENDED:
          stateString = "ExoPlayer.STATE_ENDED     -";
          break;
        default:
          stateString = "UNKNOWN_STATE             -";
          break;
      }

      Log.d(TAG, "changed state to " + stateString);
    }
  }
}
