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
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import net.whollynugatory.streamytunes.android.R;
import net.whollynugatory.streamytunes.android.Utils;
import net.whollynugatory.streamytunes.android.db.entity.MediaEntity;
import net.whollynugatory.streamytunes.android.db.views.AlbumDetails;
import net.whollynugatory.streamytunes.android.ui.BaseActivity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MediaFragment extends Fragment {

  private static final String TAG = BaseActivity.BASE_TAG + MediaFragment.class.getSimpleName();

  public interface OnMediaListener {

    void onMediaAddToFavorites(MediaEntity mediaEntity);
    void onMediaAddToPlaylist(MediaEntity mediaEntity);
    void onMediaClicked(Collection<MediaEntity> mediaEntityCollection);
    void onMediaHideInLibrary(MediaEntity mediaEntity);
    void onMediaRemoveFromFavorites(MediaEntity mediaEntity);
    void onMediaShowInLibrary(MediaEntity mediaEntity);
  }

  private OnMediaListener mCallback;
  private List<MediaEntity> mMediaEntityList;

  private RecyclerView mRecyclerView;

  public static MediaFragment newInstance(AlbumDetails albumDetails) {

    Log.d(TAG, "++newInstance(AlbumDetails)");
    List<AlbumDetails> albums = new ArrayList<>();
    albums.add(albumDetails);
    return newInstance(new ArrayList<>(albums));
  }

  public static MediaFragment newInstance(ArrayList<AlbumDetails> albumDetailsList) {

    Log.d(TAG, "++newInstance(ArrayList<AlbumDetails>)");
    Bundle arguments = new Bundle();
    arguments.putSerializable(BaseActivity.ARG_ALBUM_DETAILS_LIST, albumDetailsList);
    MediaFragment fragment = new MediaFragment();
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
      List<AlbumDetails> albumDetails = (List<AlbumDetails>)arguments.getSerializable(BaseActivity.ARG_ALBUM_DETAILS_LIST);
      mMediaEntityList = new ArrayList<>();
      if (albumDetails != null) {
        for (AlbumDetails album : albumDetails) {
          for (Map.Entry<Long, MediaEntity> media : album.MediaMap.entrySet()) {
            mMediaEntityList.add(media.getValue());
          }
        }
      } else {
        Log.w(TAG, "No album details in collection.");
      }
    } else {
      Log.e(TAG, "Arguments were null.");
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
    mediaAdapter.setMediaEntityList(mMediaEntityList);
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

      private MediaEntity mMediaEntity;

      MediaHolder(View itemView) {
        super(itemView);

        mAlbumImage = itemView.findViewById(R.id.media_item_image);
        mAlbumTextView = itemView.findViewById(R.id.media_item_text_details);
        mArtistTextView = itemView.findViewById(R.id.media_item_text_subtitle);
        mTitleTextView = itemView.findViewById(R.id.media_item_text_title);

        itemView.setOnClickListener(this);
        ImageView optionsImage = itemView.findViewById(R.id.media_item_image_options);
        optionsImage.setOnClickListener(v -> {

          PopupMenu popup = new PopupMenu(mContext, optionsImage);
          popup.inflate(R.menu.menu_options);
          popup.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
              case R.id.action_option_playlist:
                Log.d(TAG, "Add to playlist" + mMediaEntity.Title);
                mCallback.onMediaAddToPlaylist(mMediaEntity);
                return true;
              case R.id.action_option_favorite:
                if (mMediaEntity.IsFavorite) {
                  Log.d(TAG, "Remove from favorites" + mMediaEntity.Title);
                  mMediaEntity.IsFavorite = false;
                  mCallback.onMediaRemoveFromFavorites(mMediaEntity);
                } else {
                  Log.d(TAG, "Add to favorites" + mMediaEntity.Title);
                  mMediaEntity.IsFavorite = true;
                  mCallback.onMediaAddToFavorites(mMediaEntity);
                }
                return true;
              case R.id.action_option_visible:
                if (mMediaEntity.IsHidden) {
                  Log.d(TAG, "Show in library" + mMediaEntity.Title);
                  mMediaEntity.IsHidden = false;
                  mCallback.onMediaShowInLibrary(mMediaEntity);
                } else {
                  Log.d(TAG, "Hide in library" + mMediaEntity.Title);
                  mMediaEntity.IsHidden = true;
                  mCallback.onMediaHideInLibrary(mMediaEntity);
                }
                return true;
              default:
                return false;
            }
          });

          popup.show();
        });
      }

      void bind(MediaEntity mediaEntity) {

        mMediaEntity = mediaEntity;
        if (mMediaEntity != null) {
          Bitmap albumArt = Utils.loadImageFromStorage(getActivity(), mMediaEntity.ArtistId, mMediaEntity.AlbumId);
          if (albumArt != null) {
            mAlbumImage.setImageBitmap(albumArt);
          }

          mAlbumTextView.setText(mMediaEntity.AlbumName);
          mTitleTextView.setText(mMediaEntity.Title);
          mArtistTextView.setText(mMediaEntity.ArtistName);
        }
      }

      @Override
      public void onClick(View view) {

        List<MediaEntity> mediaEntityList = new ArrayList<>();
        boolean addedToList = false;
        for (MediaEntity mediaEntity : mMediaEntityList) {
          if (addedToList) {
            mediaEntityList.add(mediaEntity);
          } else if (mediaEntity.Id == mMediaEntity.Id) {
            addedToList = true;
            mediaEntityList.add(mediaEntity);
          }
        }

        mCallback.onMediaClicked(mediaEntityList);
      }
    }

    private final LayoutInflater mInflater;
    private List<MediaEntity> mMediaEntityList;
    private Context mContext;

    MediaAdapter(Context context) {

      mContext = context;
      mInflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public MediaAdapter.MediaHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

      View itemView = mInflater.inflate(R.layout.item_media, parent, false);
      return new MediaAdapter.MediaHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MediaAdapter.MediaHolder holder, int position) {

      if (mMediaEntityList != null) {
        MediaEntity mediaEntity = mMediaEntityList.get(position);
        holder.bind(mediaEntity);
      } else {
        // TODO: No songs!
      }
    }

    @Override
    public int getItemCount() {

      if (mMediaEntityList != null) {
        return mMediaEntityList.size();
      } else {
        return 0;
      }
    }

    void setMediaEntityList(Collection<MediaEntity> mediaEntityCollection) {

      Log.d(TAG, "++setMediaEntityList(Collection<MediaEntity>)");
      mMediaEntityList = new ArrayList<>();
      mMediaEntityList.addAll(mediaEntityCollection);
      notifyDataSetChanged();
    }
  }
}
