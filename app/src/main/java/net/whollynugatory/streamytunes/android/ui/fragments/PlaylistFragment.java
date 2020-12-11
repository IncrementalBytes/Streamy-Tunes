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
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import net.whollynugatory.streamytunes.android.R;
import net.whollynugatory.streamytunes.android.db.PlaylistsView;
import net.whollynugatory.streamytunes.android.db.viewmodel.MediaViewModel;
import net.whollynugatory.streamytunes.android.ui.BaseActivity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

public class PlaylistFragment extends Fragment {

  private static final String TAG = BaseActivity.BASE_TAG + PlaylistFragment.class.getSimpleName();

  public interface OnPlaylistListener {

    void onPlaylistClicked(String playlistId);
  }

  private OnPlaylistListener mCallback;

  private MediaViewModel mMediaViewModel;

  private RecyclerView mRecyclerView;

  public static PlaylistFragment newInstance() {

    Log.d(TAG, "++newInstance()");
    return new PlaylistFragment();
  }

  /*
      Fragment Override(s)
   */
  @Override
  public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);

    Log.d(TAG, "++onActivityCreated(Bundle)");
    mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
    updateUI();
  }

  @Override
  public void onAttach(@NonNull Context context) {
    super.onAttach(context);

    Log.d(TAG, "++onAttach(Context)");
    try {
      mCallback = (OnPlaylistListener) context;
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
    View view = inflater.inflate(R.layout.fragment_playlist, container, false);
    mRecyclerView = view.findViewById(R.id.playlist_list_view);
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

  private void updateUI() {

    PlaylistAdapter playlistAdapter = new PlaylistAdapter(getContext());
    mRecyclerView.setAdapter(playlistAdapter);
    mMediaViewModel.getAllPlaylists().observe(getViewLifecycleOwner(), playlistsView -> {

      if (playlistsView == null || playlistsView.size() == 0) {
        // TODO: callback to activity
      } else {
        playlistAdapter.setPlaylistCollection(playlistsView);
      }
    });
  }

  /*
    Adapter class for Playlist objects
   */
  private class PlaylistAdapter extends RecyclerView.Adapter<PlaylistAdapter.PlaylistHolder> {

    /*
      Holder class for Playlist objects
     */
    class PlaylistHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

      private final TextView mPlaylistTextView;
      private final TextView mSongsTextView;

      private PlaylistsView mPlaylist;

      PlaylistHolder(View itemView) {
        super(itemView);

        mPlaylistTextView = itemView.findViewById(R.id.media_item_text_title);
        TextView artistTextView = itemView.findViewById(R.id.media_item_text_subtitle);
        artistTextView.setVisibility(View.INVISIBLE);
        mSongsTextView = itemView.findViewById(R.id.media_item_text_details);
        ImageView dragImage = itemView.findViewById(R.id.media_item_image_drag_bar);
        // TODO: handle drag image logic
        ImageView optionsImage = itemView.findViewById(R.id.media_item_image_options);
        optionsImage.setVisibility(View.INVISIBLE);

        itemView.setOnClickListener(this);
      }

      void bind(PlaylistsView playlist) {

        mPlaylist = playlist;

        if (mPlaylist != null) {
          mPlaylistTextView.setText(mPlaylist.PlaylistName);
          mSongsTextView.setText(getResources().getQuantityString(R.plurals.format_songs, mPlaylist.SongCount, mPlaylist.SongCount));
        }
      }

      @Override
      public void onClick(View view) {

        mCallback.onPlaylistClicked(mPlaylist.PlaylistId);
      }
    }

    private final LayoutInflater mInflater;
    private List<PlaylistsView> mPlaylists;

    PlaylistAdapter(Context context) {

      mInflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public PlaylistAdapter.PlaylistHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

      View itemView = mInflater.inflate(R.layout.item_media, parent, false);
      return new PlaylistAdapter.PlaylistHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull PlaylistAdapter.PlaylistHolder holder, int position) {

      if (mPlaylists != null) {
        PlaylistsView playlist = mPlaylists.get(position);
        holder.bind(playlist);
      } else {
        // TODO: No albums!
      }
    }

    @Override
    public int getItemCount() {

      if (mPlaylists != null) {
        return mPlaylists.size();
      } else {
        return 0;
      }
    }

    void setPlaylistCollection(Collection<PlaylistsView> playlists) {

      Log.d(TAG, "++setPlaylistCollection(Collection<PlaylistsView>)");
      mPlaylists = new ArrayList<>();
      mPlaylists.addAll(playlists);
      notifyDataSetChanged();
    }
  }
}
