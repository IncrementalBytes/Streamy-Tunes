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
import android.util.Size;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import net.whollynugatory.streamytunes.android.PreferenceUtils;
import net.whollynugatory.streamytunes.android.R;
import net.whollynugatory.streamytunes.android.db.models.AlbumDetails;
import net.whollynugatory.streamytunes.android.db.models.ArtistDetails;
import net.whollynugatory.streamytunes.android.db.models.AuthorDetails;
import net.whollynugatory.streamytunes.android.db.models.MediaDetails;
import net.whollynugatory.streamytunes.android.ui.BaseActivity;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;

public class SummaryFragment extends Fragment {

  private static final String TAG = BaseActivity.BASE_TAG + SummaryFragment.class.getSimpleName();

  private HashMap<Long, AlbumDetails> mAlbumMap = new HashMap<>();
  private HashMap<Long, ArtistDetails> mArtistMap = new HashMap<>();
  private HashMap<Long, AuthorDetails> mAuthorMap = new HashMap<>();
  private boolean mIsAudiobook;
  private boolean mIsPodcast;
  private HashMap<Long, MediaDetails> mMediaMap = new HashMap<>();
  private HashMap<Long, MediaDetails> mPlaylistMap = new HashMap<>();
  private String mSelection;

  private TextView mFirstValueTextView;
  private TextView mSecondValueTextView;
  private TextView mThirdValueTextView;

  public interface OnSummaryListener {

    void onSummaryAlbumsClicked(Collection<AlbumDetails> albumDetailsCollection);

    void onSummaryArtistsClicked(Collection<ArtistDetails> artistDetailsCollection);

    void onSummaryPlaylistsClicked();

    void onSummaryAuthorsClicked(Collection<AuthorDetails> authorDetailsCollection);

    void onSummaryAudiobooksClicked(Collection<MediaDetails> audiobookDetailsCollection);

    void onSummaryPodcastsClicked(Collection<MediaDetails> podcastDetailsCollection);
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
    CardView firstCardView = view.findViewById(R.id.summary_card_first);
    CardView secondCardView = view.findViewById(R.id.summary_card_second);
    CardView thirdCardView = view.findViewById(R.id.summary_card_third);
    TextView firstTextView = view.findViewById(R.id.summary_text_first);
    TextView secondTextView = view.findViewById(R.id.summary_text_second);
    TextView thirdTextView = view.findViewById(R.id.summary_text_third);

    mFirstValueTextView = view.findViewById(R.id.summary_text_first_value);
    mSecondValueTextView = view.findViewById(R.id.summary_text_second_value);
    mThirdValueTextView = view.findViewById(R.id.summary_text_third_value);

    mSelection = MediaStore.Audio.Media.IS_MUSIC + " == ?";
    mIsAudiobook = PreferenceUtils.getIsAudiobook(getContext());
    mIsPodcast = PreferenceUtils.getIsPodcast(getContext());
    firstTextView.setText(getString(R.string.albums));
    firstCardView.setOnClickListener(v -> mCallback.onSummaryAlbumsClicked(mAlbumMap.values()));
    secondTextView.setText(getString(R.string.artists));
    secondCardView.setOnClickListener(v -> mCallback.onSummaryArtistsClicked(mArtistMap.values()));
    thirdTextView.setText(getString(R.string.playlists));
    thirdCardView.setOnClickListener(v -> mCallback.onSummaryPlaylistsClicked());
    secondCardView.setVisibility(View.VISIBLE);
    thirdCardView.setVisibility(View.VISIBLE);
    if (mIsAudiobook) {
      mSelection = MediaStore.Audio.Media.IS_AUDIOBOOK + " == ?";
      firstTextView.setText(getString(R.string.authors));
      firstCardView.setOnClickListener(v -> mCallback.onSummaryAuthorsClicked(mAuthorMap.values()));
      secondTextView.setText(getString(R.string.audiobooks));
      secondCardView.setOnClickListener(v -> mCallback.onSummaryAudiobooksClicked(mMediaMap.values()));
      secondCardView.setVisibility(View.VISIBLE);
      thirdCardView.setVisibility(View.INVISIBLE);
    } else if (mIsPodcast) {
      mSelection = MediaStore.Audio.Media.IS_PODCAST + " == ?";
      firstTextView.setText(getString(R.string.podcasts));
      firstCardView.setOnClickListener(v -> mCallback.onSummaryPodcastsClicked(mMediaMap.values()));
      secondCardView.setVisibility(View.INVISIBLE);
      thirdCardView.setVisibility(View.INVISIBLE);
    }

    getMediaList();
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

  private void getMediaList() {

    Log.d(TAG, "++getSongList()");
    String[] selectionArgs = new String[]{"1"};
    String sortOrder = MediaStore.Audio.Media.YEAR + " DESC";
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

    // TODO: add support for internal/external querying
    try (Cursor cursor = getContext().getContentResolver().query(
      MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
      projection,
      mSelection,
      selectionArgs,
      sortOrder
    )) {

      while (cursor.moveToNext()) {
        try {
          MediaDetails mediaDetails = new MediaDetails();
          mediaDetails.Id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID));
          mediaDetails.AlbumName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM));
          mediaDetails.AlbumId = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID));
          mediaDetails.ArtistName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
          mediaDetails.ArtistId = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST_ID));
          mediaDetails.DisplayName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME));
          mediaDetails.LocalSource = Uri.withAppendedPath(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, "" + mediaDetails.Id);
          mediaDetails.Title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
          mediaDetails.Track = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TRACK));
          mediaDetails.Year = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.YEAR));

          mediaDetails.AlbumArt = getContext().getContentResolver().loadThumbnail(
            mediaDetails.LocalSource,
            new Size(640, 480),
            null);
          if (mIsAudiobook) {
            AuthorDetails authorDetails = mAuthorMap.get(mediaDetails.ArtistId);
            if (authorDetails != null) { // author found
              MediaDetails media = authorDetails.Audiobooks.get(mediaDetails.AlbumId);
              if (media == null) { // album found, check for song
                mAuthorMap.get(mediaDetails.ArtistId).Audiobooks.put(mediaDetails.AlbumId, mediaDetails);
              }
            } else { // author not found
              mAuthorMap.put(mediaDetails.ArtistId, mediaDetails.toAuthorDetails());
            }
          } else if (mIsPodcast) {
            if (!mMediaMap.containsKey(mediaDetails.Id)) {
              mMediaMap.put(mediaDetails.Id, mediaDetails);
            }
          } else {
            AlbumDetails albumDetails = mAlbumMap.get(mediaDetails.AlbumId);
            if (albumDetails != null) { // album found, check for song
              if (!albumDetails.MediaMap.containsKey(mediaDetails.Id)) { // song not found
                mAlbumMap.get(mediaDetails.AlbumId).MediaMap.put(mediaDetails.Id, mediaDetails);
              }
            } else { // album not found
              mAlbumMap.put(mediaDetails.AlbumId, mediaDetails.toAlbumDetails());
            }

            ArtistDetails artistDetails = mArtistMap.get(mediaDetails.ArtistId);
            if (artistDetails != null) { // artist found, check for album
              AlbumDetails album = artistDetails.Albums.get(mediaDetails.AlbumId);
              if (album != null) { // album found, check for song
                if (!album.MediaMap.containsKey(mediaDetails.Id)) {
                  mArtistMap.get(mediaDetails.ArtistId).Albums.get(mediaDetails.AlbumId).MediaMap.put(mediaDetails.Id, mediaDetails);
                }
              } else { // album not found
                mArtistMap.get(mediaDetails.ArtistId).Albums.put(mediaDetails.AlbumId, mediaDetails.toAlbumDetails());
              }
            } else { // artist not found
              mArtistMap.put(mediaDetails.ArtistId, mediaDetails.toArtistDetails());
            }
          }
        } catch (NullPointerException | IOException e) {
          Log.e(TAG, "Failed to create song details object.", e);
        }
      }

      if (mIsAudiobook) {
        mFirstValueTextView.setText(String.format(getString(R.string.format_authors), mArtistMap.size()));
        mSecondValueTextView.setText(String.format(getString(R.string.format_audiobooks), mAlbumMap.size()));
      } else if (mIsPodcast) {
        mFirstValueTextView.setText(String.format(getString(R.string.format_podcasts), mAlbumMap.size()));
      } else {
        mFirstValueTextView.setText(String.format(getString(R.string.format_albums), mAlbumMap.size()));
        mSecondValueTextView.setText(String.format(getString(R.string.format_artists), mArtistMap.size()));
        mThirdValueTextView.setText(String.format(getString(R.string.format_playists), mPlaylistMap.size()));
      }
    }
  }
}
