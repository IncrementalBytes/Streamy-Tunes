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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import net.whollynugatory.streamytunes.android.R;
import net.whollynugatory.streamytunes.android.db.models.AlbumDetails;
import net.whollynugatory.streamytunes.android.db.models.MediaDetails;
import net.whollynugatory.streamytunes.android.ui.BaseActivity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MediaFragment extends Fragment {

  private static final String TAG = BaseActivity.BASE_TAG + MediaFragment.class.getSimpleName();

  public interface OnMediaListener {

    void onMediaClicked(Collection<MediaDetails> mediaDetailsList);
  }

  private OnMediaListener mCallback;
  private List<MediaDetails> mMediaDetailsList;

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
    arguments.putParcelableArrayList(BaseActivity.ARG_ALBUM_DETAILS_COLLECTION, albumDetailsList);
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
      List<AlbumDetails> albumDetails = arguments.getParcelableArrayList(BaseActivity.ARG_ALBUM_DETAILS_COLLECTION);
      mMediaDetailsList = new ArrayList<>();
      if (albumDetails != null) {
        for (AlbumDetails album : albumDetails) {
          for (Map.Entry<Long, MediaDetails> media : album.MediaMap.entrySet()) {
            mMediaDetailsList.add(media.getValue());
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
    mediaAdapter.setMediaDetailsList(mMediaDetailsList);
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

      private MediaDetails mMediaDetails;

      MediaHolder(View itemView) {
        super(itemView);

        mAlbumImage = itemView.findViewById(R.id.song_item_image_album);
        mAlbumTextView = itemView.findViewById(R.id.song_item_text_album);
        mArtistTextView = itemView.findViewById(R.id.song_item_text_artist);
        mTitleTextView = itemView.findViewById(R.id.song_item_text_title);

        itemView.setOnClickListener(this);
      }

      void bind(MediaDetails mediaDetails) {

        mMediaDetails = mediaDetails;
        if (mMediaDetails != null) {
          if (mMediaDetails.AlbumArt != null) {
            mAlbumImage.setImageBitmap(mMediaDetails.AlbumArt);
          }

          mAlbumTextView.setText(mMediaDetails.AlbumName);
          mTitleTextView.setText(mMediaDetails.Title);
          mArtistTextView.setText(mMediaDetails.ArtistName);
        }
      }

      @Override
      public void onClick(View view) {

        List<MediaDetails> mediaDetailsList = new ArrayList<>();
        boolean addedToList = false;
        for (MediaDetails mediaDetails : mMediaDetailsList) {
          if (addedToList) {
            mediaDetailsList.add(mediaDetails);
          } else if (mediaDetails.Id == mMediaDetails.Id) {
            addedToList = true;
            mediaDetailsList.add(mediaDetails);
          }
        }

        mCallback.onMediaClicked(mediaDetailsList);
      }
    }

    private final LayoutInflater mInflater;
    private List<MediaDetails> mMediaDetailsList;

    MediaAdapter(Context context) {

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

      if (mMediaDetailsList != null) {
        MediaDetails mediaDetails = mMediaDetailsList.get(position);
        holder.bind(mediaDetails);
      } else {
        // TODO: No songs!
      }
    }

    @Override
    public int getItemCount() {

      if (mMediaDetailsList != null) {
        return mMediaDetailsList.size();
      } else {
        return 0;
      }
    }

    void setMediaDetailsList(Collection<MediaDetails> mediaDetailsCollection) {

      Log.d(TAG, "++setMediaDetailsList(Collection<MediaDetails>)");
      mMediaDetailsList = new ArrayList<>();
      mMediaDetailsList.addAll(mediaDetailsCollection);
      notifyDataSetChanged();
    }
  }
}

