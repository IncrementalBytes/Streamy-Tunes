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
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import androidx.lifecycle.ViewModelProvider;

import net.whollynugatory.streamytunes.android.PreferenceUtils;
import net.whollynugatory.streamytunes.android.R;
import net.whollynugatory.streamytunes.android.db.entity.MediaEntity;
import net.whollynugatory.streamytunes.android.db.models.AlbumDetails;
import net.whollynugatory.streamytunes.android.db.models.ArtistDetails;
import net.whollynugatory.streamytunes.android.db.models.AuthorDetails;
import net.whollynugatory.streamytunes.android.db.models.MediaDetails;
import net.whollynugatory.streamytunes.android.db.viewmodel.MediaViewModel;
import net.whollynugatory.streamytunes.android.ui.BaseActivity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;

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

  private MediaViewModel mMediaViewModel;

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
    mMediaViewModel = new ViewModelProvider(this).get(MediaViewModel.class);
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
  public void onResume() {
    super.onResume();

    Log.d(TAG, "++onResume()");
    updateUI();
  }

  @Override
  public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    Log.d(TAG, "++onViewCreated(View, Bundle)");
  }

  private void updateUI() {

    if (mIsAudiobook) {
      mMediaViewModel.getAllAudiobooks().observe(getViewLifecycleOwner(), audiobookEntities -> {

        if (audiobookEntities != null) {
          mFirstValueTextView.setText(String.format(getString(R.string.format_authors), audiobookEntities.size()));
          mSecondValueTextView.setText(String.format(getString(R.string.format_audiobooks), audiobookEntities.size()));
        }
      });
    } else if (mIsPodcast) {
      mMediaViewModel.getAllPodcasts().observe(getViewLifecycleOwner(), podcastEntities -> {

        if (podcastEntities != null) {
          mFirstValueTextView.setText(String.format(getString(R.string.format_podcasts), podcastEntities.size()));
        }
      });
    } else {
      mMediaViewModel.getAllMusic().observe(getViewLifecycleOwner(), musicEntities -> {

        for (MediaEntity musicEntity : musicEntities) {
          MediaDetails mediaDetails = new MediaDetails(musicEntity);
          mediaDetails.AlbumArt = loadImageFromStorage(musicEntity);
          if (mAlbumMap.containsKey(musicEntity.AlbumId)) {
            Objects.requireNonNull(mAlbumMap.get(musicEntity.AlbumId)).MediaMap.put(musicEntity.AlbumId, mediaDetails);
          } else {
            mAlbumMap.put(musicEntity.AlbumId, mediaDetails.toAlbumDetails());
          }

          if (mArtistMap.containsKey(musicEntity.ArtistId)) {
            Objects.requireNonNull(mArtistMap.get(musicEntity.ArtistId)).Albums.put(musicEntity.AlbumId, mediaDetails.toAlbumDetails());
          } else {
            mArtistMap.put(musicEntity.ArtistId, mediaDetails.toArtistDetails());
          }
        }

        mFirstValueTextView.setText(String.format(getString(R.string.format_albums), mAlbumMap.size()));
        mSecondValueTextView.setText(String.format(getString(R.string.format_artists), mArtistMap.size()));
        mThirdValueTextView.setText(String.format(getString(R.string.format_playists), 0));
      });
    }
  }

  private Bitmap loadImageFromStorage(MediaEntity mediaEntity) {

    try {
      ContextWrapper cw = new ContextWrapper(getActivity());
      File directory = cw.getDir(getActivity().getString(R.string.album), Context.MODE_PRIVATE);
      File sourcePath = new File(directory, mediaEntity.ArtistId + "-" + mediaEntity.AlbumId + ".jpg");
      return BitmapFactory.decodeStream(new FileInputStream(sourcePath));
    } catch (FileNotFoundException e) {
      Log.e(TAG, "Failed to retrieve image.", e);
    }

    return null;
  }
}
