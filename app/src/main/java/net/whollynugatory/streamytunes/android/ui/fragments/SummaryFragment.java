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
import android.os.Bundle;
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
import net.whollynugatory.streamytunes.android.db.viewmodel.MediaViewModel;
import net.whollynugatory.streamytunes.android.ui.BaseActivity;

import java.util.Locale;

public class SummaryFragment extends Fragment {

  private static final String TAG = BaseActivity.BASE_TAG + SummaryFragment.class.getSimpleName();

  private boolean mIsAudiobook;
  private boolean mIsPodcast;

  private TextView mFirstValueTextView;
  private TextView mSecondValueTextView;
  private TextView mThirdValueTextView;

  public interface OnSummaryListener {

    void onQueryComplete(int mediaFound);

    void onSummaryAlbumsClicked();

    void onSummaryArtistsClicked();

    void onSummaryPlaylistsClicked();

    void onSummaryAudiobooksClicked();

    void onSummaryPodcastsClicked();
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

    mIsAudiobook = PreferenceUtils.getIsAudiobook(getContext());
    mIsPodcast = PreferenceUtils.getIsPodcast(getContext());
    firstTextView.setText(getString(R.string.albums));
    firstCardView.setOnClickListener(v -> mCallback.onSummaryAlbumsClicked());
    secondTextView.setText(getString(R.string.artists));
    secondCardView.setOnClickListener(v -> mCallback.onSummaryArtistsClicked());
    thirdTextView.setText(getString(R.string.playlists));
    thirdCardView.setOnClickListener(v -> mCallback.onSummaryPlaylistsClicked());
    secondCardView.setVisibility(View.VISIBLE);
    thirdCardView.setVisibility(View.VISIBLE);
    if (mIsAudiobook) {
      firstTextView.setText(getString(R.string.authors));
      firstCardView.setOnClickListener(v -> mCallback.onSummaryArtistsClicked());
      secondTextView.setText(getString(R.string.audiobooks));
      secondCardView.setOnClickListener(v -> mCallback.onSummaryAudiobooksClicked());
      secondCardView.setVisibility(View.VISIBLE);
      thirdCardView.setVisibility(View.INVISIBLE);
    } else if (mIsPodcast) {
      firstTextView.setText(getString(R.string.podcasts));
      firstCardView.setOnClickListener(v -> mCallback.onSummaryPodcastsClicked());
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
          mFirstValueTextView.setText(getResources().getQuantityString(R.plurals.format_authors, audiobookEntities.size(), audiobookEntities.size()));
          mSecondValueTextView.setText(
            getResources().getQuantityString(
              R.plurals.format_audiobooks,
              audiobookEntities.size(),
              audiobookEntities.size()));
          mCallback.onQueryComplete(audiobookEntities.size());
        }
      });
    } else if (mIsPodcast) {
      mMediaViewModel.getAllPodcasts().observe(getViewLifecycleOwner(), podcastEntities -> {

        if (podcastEntities != null) {
          mFirstValueTextView.setText(getResources().getQuantityString(R.plurals.format_podcasts, podcastEntities.size(), podcastEntities.size()));
          mCallback.onQueryComplete(podcastEntities.size());
        }
      });
    } else {
      mMediaViewModel.getAllAlbums().observe(getViewLifecycleOwner(), albumViews -> {

        mFirstValueTextView.setText(getResources().getQuantityString(R.plurals.format_albums, albumViews.size(), albumViews.size()));
        mMediaViewModel.getAllArtists().observe(getViewLifecycleOwner(), artistViews -> {

          mSecondValueTextView.setText(getResources().getQuantityString(R.plurals.format_artists, artistViews.size(), artistViews.size()));
          mMediaViewModel.getAllPlaylists().observe(getViewLifecycleOwner(), playlistViews -> {

            mThirdValueTextView.setText(getResources().getQuantityString(R.plurals.format_playlists, playlistViews.size(), playlistViews.size()));
            mCallback.onQueryComplete(albumViews.size() + artistViews.size() + playlistViews.size());
          });
        });
      });
    }
  }
}
