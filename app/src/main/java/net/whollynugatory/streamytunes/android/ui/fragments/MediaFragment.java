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
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import net.whollynugatory.streamytunes.android.R;
import net.whollynugatory.streamytunes.android.Utils;
import net.whollynugatory.streamytunes.android.db.MediaDetails;
import net.whollynugatory.streamytunes.android.db.PlaylistDetails;
import net.whollynugatory.streamytunes.android.db.entity.MediaEntity;
import net.whollynugatory.streamytunes.android.db.viewmodel.MediaViewModel;
import net.whollynugatory.streamytunes.android.ui.BaseActivity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

public class MediaFragment extends Fragment {

  private static final String TAG = BaseActivity.BASE_TAG + MediaFragment.class.getSimpleName();

  public interface OnMediaListener {

    void onMediaAddToPlaylist(MediaEntity mediaEntity);
    void onMediaClicked(Collection<MediaDetails> mediaDetailsCollection);
    void onMediaUpdateFavorites(MediaEntity mediaEntity);
  }

  private OnMediaListener mCallback;

  private long mAlbumId;
  private long mArtistId;
  private String mPlaylistId;
  private MediaViewModel mMediaViewModel;

  private RecyclerView mRecyclerView;

  public static MediaFragment newInstanceByAlbum(long albumId) {

    Log.d(TAG, "++newInstanceByAlbum(long)");
    MediaFragment fragment = new MediaFragment();
    Bundle arguments = new Bundle();
    arguments.putLong(BaseActivity.ARG_ALBUM_ID, albumId);
    fragment.setArguments(arguments);
    return fragment;
  }

  public static MediaFragment newInstanceByArtist(long artistId) {

    Log.d(TAG, "++newInstanceByArtist(long)");
    MediaFragment fragment = new MediaFragment();
    Bundle arguments = new Bundle();
    arguments.putLong(BaseActivity.ARG_ARTIST_ID, artistId);
    fragment.setArguments(arguments);
    return fragment;
  }

  public static MediaFragment newInstanceByPlaylist(String playlistId) {

    Log.d(TAG, "++newInstanceByPlaylist(long)");
    MediaFragment fragment = new MediaFragment();
    Bundle arguments = new Bundle();
    arguments.putString(BaseActivity.ARG_PLAYLIST_ID, playlistId);
    fragment.setArguments(arguments);
    return fragment;
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
      mCallback = (OnMediaListener) context;
    } catch (ClassCastException e) {
      throw new ClassCastException(
        String.format(Locale.ENGLISH, "Missing interface implementations for %s", context.toString()));
    }

    Bundle arguments = getArguments();
    if (arguments != null) {
      if (arguments.containsKey(BaseActivity.ARG_ALBUM_ID)) {
        mAlbumId = arguments.getLong(BaseActivity.ARG_ALBUM_ID);
      } else {
        mAlbumId = -1;
      }

      if (arguments.containsKey(BaseActivity.ARG_ARTIST_ID)) {
        mArtistId = arguments.getLong(BaseActivity.ARG_ARTIST_ID);
      } else {
        mArtistId = -1;
      }

      if (arguments.containsKey(BaseActivity.ARG_PLAYLIST_ID)) {
        mPlaylistId = arguments.getString(BaseActivity.ARG_PLAYLIST_ID);
      } else {
        mPlaylistId = BaseActivity.UNKNOWN_GUID;
      }
    } else {
      Log.e(TAG, "Arguments were null.");
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
    View view =  inflater.inflate(R.layout.fragment_songs, container, false);
    mRecyclerView = view.findViewById(R.id.songs_list_view);
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

    MediaAdapter mediaAdapter = new MediaAdapter(getContext());
    mRecyclerView.setAdapter(mediaAdapter);
    if (mAlbumId > BaseActivity.UNKNOWN_ID) {
      mMediaViewModel.getAllMusicByAlbumId(mAlbumId).observe(getViewLifecycleOwner(), albumWithSongs -> {

        if (albumWithSongs != null && albumWithSongs.size() > 0) {
          mediaAdapter.setMediaDetailsList(albumWithSongs);
        } else {
          // TODO: callback with no media
        }
      });
    } else if (mArtistId > BaseActivity.UNKNOWN_ID) {
      mMediaViewModel.getAllMusicByArtistId(mArtistId).observe(getViewLifecycleOwner(), artistWithSongs -> {

        if (artistWithSongs != null && artistWithSongs.size() > 0) {
          mediaAdapter.setMediaDetailsList(artistWithSongs);
        } else {
          // TODO: callback with no media
        }
      });
    } else if (!mPlaylistId.isEmpty() && !mPlaylistId.equals(BaseActivity.UNKNOWN_GUID)) {
      mMediaViewModel.getPlaylistById(mPlaylistId).observe(getViewLifecycleOwner(), playlistSongs -> {

        if (playlistSongs != null && playlistSongs.size() > 0) {
          mediaAdapter.setPlaylistDetailsList(playlistSongs);
        } else {
          // TODO: callback with no media
        }
      });
    }
  }

  /*
    Adapter class for Media objects
   */
  private class MediaAdapter extends RecyclerView.Adapter<MediaAdapter.MediaHolder> {

    /*
      Holder class for Media objects
     */
    class MediaHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

      private final ImageView mAlbumImage;
      private final TextView mAlbumTextView;
      private final TextView mArtistTextView;
      private final TextView mTitleTextView;

      private MediaDetails mMedia;

      MediaHolder(View itemView) {
        super(itemView);

        mAlbumImage = itemView.findViewById(R.id.media_item_image);
        mAlbumTextView = itemView.findViewById(R.id.media_item_text_details);
        mArtistTextView = itemView.findViewById(R.id.media_item_text_subtitle);
        mTitleTextView = itemView.findViewById(R.id.media_item_text_title);
        ImageView dragImage = itemView.findViewById(R.id.media_item_image_drag_bar);
        dragImage.setVisibility(View.INVISIBLE);

        ImageView optionsImage = itemView.findViewById(R.id.media_item_image_options);
        optionsImage.setOnClickListener(v -> {

          PopupMenu optionsPopup = new PopupMenu(mContext, optionsImage);
          optionsPopup.inflate(R.menu.menu_options);
          if (mMedia.IsFavorite) {
            optionsPopup.getMenu().findItem(R.id.action_option_favorite).setTitle(getString(R.string.remove_from_favorites));
          }

          optionsPopup.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
              case R.id.action_option_playlist:
                Log.d(TAG, "Add to playlist " + mMedia.Title);
                mCallback.onMediaAddToPlaylist(mMedia.toMediaEntity());
                return true;
              case R.id.action_option_favorite:
                if (mMedia.IsFavorite) {
                  Log.d(TAG, "Remove from favorites " + mMedia.Title);
                  mMedia.IsFavorite = false;
                } else {
                  Log.d(TAG, "Add to favorites " + mMedia.Title);
                  mMedia.IsFavorite = true;
                }

                mCallback.onMediaUpdateFavorites(mMedia.toMediaEntity());

                return true;
              default:
                return false;
            }
          });

          optionsPopup.show();
        });

        itemView.setOnClickListener(this);
      }

      void bind(MediaDetails media) {

        mMedia = media;
        if (mMedia != null) {
          Bitmap albumArt = Utils.loadImageFromStorage(getActivity(), mMedia.ArtistId, mMedia.AlbumId);
          if (albumArt != null) {
            mAlbumImage.setImageBitmap(albumArt);
          }

          mTitleTextView.setText(mMedia.Title);
          mAlbumTextView.setText(mMedia.AlbumName);
          mArtistTextView.setText(mMedia.ArtistName);
        }
      }

      @Override
      public void onClick(View view) {

        List<MediaDetails> mediaList = new ArrayList<>();
        boolean addedToList = false;
        for (MediaDetails media : mMediaList) {
          if (addedToList) { // add media following the media item we are looking for
            mediaList.add(media);
          } else if (media.MediaId == mMedia.MediaId) {
            addedToList = true;
            mediaList.add(media);
          }
        }

        mCallback.onMediaClicked(mediaList);
      }
    }

    private final Context mContext;
    private final LayoutInflater mInflater;

    private List<MediaDetails> mMediaList;

    MediaAdapter(Context context) {

      mContext = context;
      mInflater = LayoutInflater.from(context);
      mMediaList = new ArrayList<>();
    }

    @NonNull
    @Override
    public MediaAdapter.MediaHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

      View itemView = mInflater.inflate(R.layout.item_media, parent, false);
      return new MediaAdapter.MediaHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MediaAdapter.MediaHolder holder, int position) {

      if (mMediaList != null) {
        MediaDetails media = mMediaList.get(position);
        holder.bind(media);
      } else {
        // TODO: No songs!
      }
    }

    @Override
    public int getItemCount() {

      if (mMediaList != null) {
        return mMediaList.size();
      } else {
        return 0;
      }
    }

    void setMediaDetailsList(List<MediaDetails> mediaList) {

      Log.d(TAG, "++setMediaDetailsList(List<MediaDetails>)");
      mMediaList = new ArrayList<>();
      mMediaList.addAll(mediaList);
      notifyDataSetChanged();
    }

    void setPlaylistDetailsList(List<PlaylistDetails> playlist) {

      Log.d(TAG, "++setPlaylistDetailsList(List<PlaylistDetails>)");
      mMediaList = new ArrayList<>();
      for (PlaylistDetails playlistDetails : playlist) {
        mMediaList.add(playlistDetails.toMediaDetails());
      }

      notifyDataSetChanged();
    }
  }
}
