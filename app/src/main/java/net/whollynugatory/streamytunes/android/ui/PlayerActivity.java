/*
 * Copyright 2020 Ryan Ward
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package net.whollynugatory.streamytunes.android.ui;

import android.Manifest;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;

import net.whollynugatory.streamytunes.android.MediaPlayerService;
import net.whollynugatory.streamytunes.android.PreferenceUtils;
import net.whollynugatory.streamytunes.android.R;
import net.whollynugatory.streamytunes.android.db.entity.AudioEntity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PlayerActivity extends BaseActivity {

  private static final String TAG = BaseActivity.BASE_TAG + PlayerActivity.class.getSimpleName();

  private ArrayList<AudioEntity> mAudioList;
  private MediaPlayerService mPlayer;
  private boolean mServiceBound = false;

  private final ServiceConnection mServiceConnection = new ServiceConnection() {

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {

      Log.d(TAG, "++onServiceConnected(ComponentName, IBinder)");
      MediaPlayerService.LocalBinder binder = (MediaPlayerService.LocalBinder) service;
      mPlayer = binder.getService();
      mServiceBound = true;
      Log.d(TAG, "Service Bound");
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {

      Log.d(TAG, "++onServiceDisconnected(ComponentName)");
      mServiceBound = false;
    }
  };

  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    Log.d(TAG, "++onCreate(Bundle)");
    setContentView(R.layout.activity_player);
    checkForPermissions();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();

    Log.d(TAG, "++onDestroy()");
    if (mServiceBound) {
      unbindService(mServiceConnection);
      mPlayer.stopSelf();
    }
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

    Log.d(TAG, "++onRequestPermissionsResult(int, String[], int[])");
    if (requestCode == BaseActivity.REQUEST_STORAGE_PERMISSIONS) {
      checkForPermissions();
    }
  }

  @Override
  public void onRestoreInstanceState(@NonNull  Bundle savedInstanceState) {
    super.onRestoreInstanceState(savedInstanceState);

    mServiceBound = savedInstanceState.getBoolean("ServiceState");
  }

  @Override
  public void onSaveInstanceState(Bundle savedInstanceState) {

    savedInstanceState.putBoolean("ServiceState", mServiceBound);
    super.onSaveInstanceState(savedInstanceState);
  }

  /*
    Public Method(s)
   */
//  public class MediaServiceReceiver extends BroadcastReceiver
//  {
//    @Override
//    //this method receives broadcast messages. Be sure to modify AndroidManifest.xml file in order to enable message receiving
//    public void onReceive(Context context, Intent intent) {
//
//      accelerationX = intent.getDoubleExtra(MediaPlayerService.ACTION_PLAY, 0);
//      accelerationY = intent.getDoubleExtra(AccelerationService.ACCELERATION_Y, 0);
//      accelerationZ = intent.getDoubleExtra(AccelerationService.ACCELERATION_Z, 0);
//
//      announceSession();
//
//      updateUI();
//    }
//  }

  /*
    Private Method(s)
   */
  private void checkForPermissions() {

    Log.d(TAG, "++checkForPermissions()");
    if ((ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) ||
      (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_MEDIA_LOCATION) != PackageManager.PERMISSION_GRANTED) ||
      (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED)) {

      if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE) ||
        ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_MEDIA_LOCATION) ||
        ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_PHONE_STATE)) {

        String requestMessage = getString(R.string.permission_storage);
        Snackbar.make(
          findViewById(R.id.player_list_view),
          requestMessage,
          Snackbar.LENGTH_INDEFINITE)
          .setAction(
            getString(R.string.ok),
            view -> ActivityCompat.requestPermissions(
              PlayerActivity.this,
              new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.ACCESS_MEDIA_LOCATION, Manifest.permission.READ_PHONE_STATE},
              BaseActivity.REQUEST_STORAGE_PERMISSIONS))
          .show();
      } else {
        ActivityCompat.requestPermissions(
          this,
          new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.ACCESS_MEDIA_LOCATION, Manifest.permission.READ_PHONE_STATE},
          BaseActivity.REQUEST_STORAGE_PERMISSIONS);
      }
    } else {
      Log.d(
        TAG,
        "Permission granted: " +
          Manifest.permission.READ_EXTERNAL_STORAGE +
          ", " +
          Manifest.permission.ACCESS_MEDIA_LOCATION +
          ", " +
          Manifest.permission.READ_PHONE_STATE);
      loadAudio();
    }
  }

  private void loadAudio() {

    Log.d(TAG, "++loadAudio()");
    ContentResolver contentResolver = getContentResolver();
    Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
    String selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0";
    String sortOrder = MediaStore.Audio.Media.TITLE + " ASC";
    Cursor cursor = contentResolver.query(uri, null, selection, null, sortOrder);

    if (cursor != null && cursor.getCount() > 0) {
      mAudioList = new ArrayList<>();
      while (cursor.moveToNext()) {
        String data = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
        String title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
        String album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
        String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));

        mAudioList.add(new AudioEntity(data, title, album, artist));
      }
    }

    if (cursor != null) {
      cursor.close();
    }

    updateUI();
  }

  private void playAudio(int audioIndex) {

    Log.d(TAG, "++playAudio(int)");
    if (!mServiceBound) { // store serializable mAudioList to SharedPreferences
      PreferenceUtils.setAudioList(getApplicationContext(), mAudioList);
      PreferenceUtils.setAudioIndex(getApplicationContext(), audioIndex);
      Intent playerIntent = new Intent(this, MediaPlayerService.class);
      startService(playerIntent);
      bindService(playerIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
    } else { // store the new mAudioIndex to SharedPreferences
      PreferenceUtils.setAudioIndex(getApplicationContext(), audioIndex);
      Intent broadcastIntent = new Intent(BaseActivity.BROADCAST_PLAY_NEW_AUDIO);
      sendBroadcast(broadcastIntent);
    }
  }

  private void updateUI() {

    RecyclerView recyclerView = findViewById(R.id.player_list_view);
    LayoutAdapter adapter = new LayoutAdapter(mAudioList, getApplication());
    recyclerView.setAdapter(adapter);
    recyclerView.setLayoutManager(new LinearLayoutManager(this));
    recyclerView.addOnItemTouchListener(new CustomTouchListener(this, (view, index) -> playAudio(index)));
  }

  /*
    Adapter class for Layout objects
   */
  private static class LayoutAdapter extends RecyclerView.Adapter<LayoutAdapter.ViewHolder> {

    /*
      Holder class for Layout objects
     */
    class ViewHolder extends RecyclerView.ViewHolder {

      public TextView Title;
      public ImageView PlayPause;

      ViewHolder(View itemView) {
        super(itemView);

        Title = itemView.findViewById(R.id.layout_text_title);
        PlayPause = itemView.findViewById(R.id.layout_image_play_pause);
      }
    }

    private List<AudioEntity> mAudioEntityList = Collections.emptyList();
    private Context mContext;

    public LayoutAdapter(List<AudioEntity> audioEntityList, Context context) {

      mAudioEntityList = audioEntityList;
      mContext = context;
    }

    @Override
    @NonNull
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

      View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_layout, parent, false);
      ViewHolder holder = new ViewHolder(v);
      return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

      holder.Title.setText(mAudioEntityList.get(position).getTitle());
    }

    @Override
    public int getItemCount() {

      return mAudioEntityList.size();
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
      super.onAttachedToRecyclerView(recyclerView);
    }
  }

  private static class CustomTouchListener implements RecyclerView.OnItemTouchListener {

    private GestureDetector mGestureDetector;
    private onItemClickListener mClickListener;

    public CustomTouchListener(Context context, final onItemClickListener clickListener) {

      mClickListener = clickListener;
      mGestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
          return true;
        }
      });
    }

    @Override
    public boolean onInterceptTouchEvent(RecyclerView recyclerView, MotionEvent e) {

      View child = recyclerView.findChildViewUnder(e.getX(), e.getY());
      if (child != null && mClickListener != null && mGestureDetector.onTouchEvent(e)) {
        mClickListener.onClick(child, recyclerView.getChildLayoutPosition(child));
      }

      return false;
    }

    @Override
    public void onTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
    }

    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
    }
  }

  private interface onItemClickListener {

    void onClick(View view, int index);
  }
}
