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
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import net.whollynugatory.streamytunes.android.R;
import net.whollynugatory.streamytunes.android.Utils;
import net.whollynugatory.streamytunes.android.db.AlbumsView;
import net.whollynugatory.streamytunes.android.db.viewmodel.MediaViewModel;
import net.whollynugatory.streamytunes.android.ui.BaseActivity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

public class AlbumsFragment extends Fragment {

  private static final String TAG = BaseActivity.BASE_TAG + AlbumsFragment.class.getSimpleName();

  public interface OnAlbumListener {

    void onAlbumClicked(long albumId);
  }

  private OnAlbumListener mCallback;

  private MediaViewModel mMediaViewModel;

  private RecyclerView mRecyclerView;

  public static AlbumsFragment newInstance() {

    Log.d(TAG, "++newInstance()");
    return new AlbumsFragment();
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
      mCallback = (OnAlbumListener) context;
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
    View view =  inflater.inflate(R.layout.fragment_albums, container, false);
    mRecyclerView = view.findViewById(R.id.albums_list_view);
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

    AlbumAdapter albumAdapter = new AlbumAdapter(getContext());
    mRecyclerView.setAdapter(albumAdapter);
    mMediaViewModel.getAllAlbums().observe(getViewLifecycleOwner(), albums -> {

      if (albums == null || albums.size() == 0) {
        // TODO: callback to activity
      } else {
        albumAdapter.setAlbumsList(albums);
      }
    });
  }

  /*
    Adapter class for Album objects
   */
  private class AlbumAdapter extends RecyclerView.Adapter<AlbumAdapter.AlbumHolder> {

    /*
      Holder class for Album objects
     */
    class AlbumHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

      private final ImageView mAlbumImage;
      private final TextView mAlbumTextView;
      private final TextView mArtistTextView;
      private final TextView mSongsTextView;

      private AlbumsView mAlbum;

      AlbumHolder(View itemView) {
        super(itemView);

        mAlbumImage = itemView.findViewById(R.id.media_item_image);
        mAlbumTextView = itemView.findViewById(R.id.media_item_text_title);
        mArtistTextView = itemView.findViewById(R.id.media_item_text_subtitle);
        mSongsTextView = itemView.findViewById(R.id.media_item_text_details);

        ImageView favoriteImage = itemView.findViewById(R.id.media_item_image_favorite);
        favoriteImage.setVisibility(View.INVISIBLE);
        ImageView playlistImage = itemView.findViewById(R.id.media_item_image_playlist);
        playlistImage.setVisibility(View.INVISIBLE);
        ImageView visibleImage = itemView.findViewById(R.id.media_item_image_visible);
        visibleImage.setVisibility(View.INVISIBLE);

        itemView.setOnClickListener(this);
      }

      void bind(AlbumsView album) {

        mAlbum = album;

        if (mAlbum != null) {
          Bitmap albumArt = Utils.loadImageFromStorage(getActivity(), mAlbum.ArtistId, mAlbum.AlbumId);
          if (albumArt != null) {
            mAlbumImage.setImageBitmap(albumArt);
          }

          mAlbumTextView.setText(mAlbum.AlbumName);
          mSongsTextView.setText(String.format(getString(R.string.format_songs), mAlbum.SongCount));
          mArtistTextView.setText(mAlbum.ArtistName);
        }
      }

      @Override
      public void onClick(View view) {

        mCallback.onAlbumClicked(mAlbum.AlbumId);
      }
    }

    private final LayoutInflater mInflater;
    private List<AlbumsView> mAlbums;

    AlbumAdapter(Context context) {

      mInflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public AlbumHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

      View itemView = mInflater.inflate(R.layout.item_media, parent, false);
      return new AlbumHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull AlbumHolder holder, int position) {

      if (mAlbums != null) {
        AlbumsView album = mAlbums.get(position);
        holder.bind(album);
      } else {
        // TODO: No albums!
      }
    }

    @Override
    public int getItemCount() {

      if (mAlbums != null) {
        return mAlbums.size();
      } else {
        return 0;
      }
    }

    void setAlbumsList(Collection<AlbumsView> albums) {

      Log.d(TAG, "++setAlbumsList(Collection<AlbumsView>)");
      mAlbums = new ArrayList<>();
      mAlbums.addAll(albums);
      notifyDataSetChanged();
    }
  }
}
