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
package net.whollynugatory.streamytunes.android.ui.fragments;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import net.whollynugatory.streamytunes.android.R;
import net.whollynugatory.streamytunes.android.Utils;
import net.whollynugatory.streamytunes.android.db.models.AlbumDetails;
import net.whollynugatory.streamytunes.android.db.models.ArtistDetails;
import net.whollynugatory.streamytunes.android.db.models.SongDetails;

import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;

public class SummaryFragment extends Fragment {

  private static final String TAG = Utils.BASE_TAG + "SummaryFragment";

  private HashMap<Long, AlbumDetails> mAlbumMap = new HashMap<>();
  private HashMap<Long, ArtistDetails> mArtistMap = new HashMap<>();

  private static String sSelection = MediaStore.Audio.Media.IS_MUSIC + " == ?";
  private static String[] sSelectionArgs = new String[]{"1"};
  private static String sSortOrder = MediaStore.Audio.Media.YEAR + " DESC";

  private TextView mAlbumValueTextView;
  private TextView mArtistValueTextView;

  public interface OnSummaryListener {

    void onSummaryAlbumsClicked(Collection<AlbumDetails> songDetailsCollection);

    void onSummaryArtistsClicked(Collection<ArtistDetails> songDetailsCollection);
  }

  private OnSummaryListener mCallback;

  public static SummaryFragment newInstance() {

    Log.d(TAG, "++newInstance()");
    return new SummaryFragment();
  }

  /*
      Fragment Override(s)
   */
  @Override
  public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);

    Log.d(TAG, "++onActivityCreated(Bundle)");
  }

  @Override
  public void onAttach(@NonNull Context context) {
    super.onAttach(context);

    Log.d(TAG, "++onAttach(Context)");
    try {
      mCallback = (OnSummaryListener) context;
    } catch (ClassCastException e) {
      throw new ClassCastException(
        String.format(Locale.ENGLISH, "Missing interface implementations for %s", context.toString()));
    }
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    Log.d(TAG, "++onCreate(Bundle)");
  }

  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

    Log.d(TAG, "++onCreateView(LayoutInflater, ViewGroup, Bundle)");
    View view = inflater.inflate(R.layout.fragment_summary, container, false);

    CardView albumCardView = view.findViewById(R.id.summary_card_albums);
    albumCardView.setOnClickListener(v -> mCallback.onSummaryAlbumsClicked(mAlbumMap.values()));
    CardView artistCardView = view.findViewById(R.id.summary_card_artists);
    artistCardView.setOnClickListener(v -> mCallback.onSummaryArtistsClicked(mArtistMap.values()));

    mAlbumValueTextView = view.findViewById(R.id.summary_text_albums_value);
    mArtistValueTextView = view.findViewById(R.id.summary_text_artists_value);
    FloatingActionButton fab = view.findViewById(R.id.summary_fab_sync);
    fab.setOnClickListener(view1 -> getSongList());

    getSongList();

    return view;
  }

  @Override
  public void onDetach() {
    super.onDetach();

    Log.d(TAG, "++onDetach()");
    mCallback = null;
  }

  @Override
  public void onDestroy() {
    super.onDestroy();

    Log.d(TAG, "++onDestroy()");
  }

  @Override
  public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    Log.d(TAG, "++onViewCreated(View, Bundle)");
  }

  private void getSongList() {

    Log.d(TAG, "++getSongList()");
    String[] projection = new String[]{
      MediaStore.Audio.Media._ID,
      MediaStore.Audio.Media.ALBUM,
      MediaStore.Audio.Media.ALBUM_ID,
      MediaStore.Audio.Media.ARTIST,
      MediaStore.Audio.Media.ARTIST_ID,
      MediaStore.Audio.Media.DISPLAY_NAME,
      MediaStore.Audio.Media.TITLE,
      MediaStore.Audio.Media.TRACK,
      MediaStore.Audio.Media.YEAR
    };

    try (Cursor cursor = getContext().getContentResolver().query(
      MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
      projection,
      sSelection,
      sSelectionArgs,
      sSortOrder
    )) {

      int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID);
      int albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM);
      int albumIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID);
      int artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST);
      int artistIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST_ID);
      int displayNameColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME);
      int titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE);
      int trackColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TRACK);
      int yearColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.YEAR);

      while (cursor.moveToNext()) {
        SongDetails songDetails = new SongDetails();
        songDetails.Id = cursor.getLong(idColumn);
        songDetails.AlbumName = cursor.getString(albumColumn);
        songDetails.AlbumId = cursor.getLong(albumIdColumn);
        songDetails.ArtistName = cursor.getString(artistColumn);
        songDetails.ArtistId = cursor.getLong(artistIdColumn);
        songDetails.DisplayName = cursor.getString(displayNameColumn);
        songDetails.LocalSource = Uri.withAppendedPath(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, "" + songDetails.Id);
        songDetails.Title = cursor.getString(titleColumn);
        songDetails.Track = cursor.getInt(trackColumn);
        songDetails.Year = cursor.getInt(yearColumn);

        AlbumDetails albumDetails = mAlbumMap.get(songDetails.AlbumId);
        if (albumDetails != null) { // album found, check for song
          if (!albumDetails.Songs.containsKey(songDetails.Id)) { // song not found
            mAlbumMap.get(songDetails.AlbumId).Songs.put(songDetails.Id, songDetails);
          }
        } else { // album not found
          mAlbumMap.put(songDetails.AlbumId, songDetails.toAlbumDetails());
        }

        ArtistDetails artistDetails = mArtistMap.get(songDetails.ArtistId);
        if (artistDetails != null) { // artist found, check for album
          AlbumDetails album = artistDetails.Albums.get(songDetails.AlbumId);
          if (album != null) { // album found, check for song
            if (!album.Songs.containsKey(songDetails.Id)) {
              mArtistMap.get(songDetails.ArtistId).Albums.get(songDetails.AlbumId).Songs.put(songDetails.Id, songDetails);
            }
          } else { // album not found
            mArtistMap.get(songDetails.ArtistId).Albums.put(songDetails.AlbumId, songDetails.toAlbumDetails());
          }
        } else { // artist not found
          mArtistMap.put(songDetails.ArtistId, songDetails.toArtistDetails());
        }
      }

      mAlbumValueTextView.setText(String.format(getString(R.string.format_albums), mAlbumMap.size()));
      mArtistValueTextView.setText(String.format(getString(R.string.format_albums), mArtistMap.size()));
    }
  }
}
