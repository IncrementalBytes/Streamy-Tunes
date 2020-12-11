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
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import net.whollynugatory.streamytunes.android.R;
import net.whollynugatory.streamytunes.android.db.ArtistsView;
import net.whollynugatory.streamytunes.android.db.viewmodel.MediaViewModel;
import net.whollynugatory.streamytunes.android.ui.BaseActivity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

public class ArtistsFragment extends Fragment {

  private static final String TAG = BaseActivity.BASE_TAG + ArtistsFragment.class.getSimpleName();

  public interface OnArtistListener {

    void onArtistClicked(long artistId);
  }

  private OnArtistListener mCallback;
  private MediaViewModel mMediaViewModel;
  private RecyclerView mRecyclerView;

  public static ArtistsFragment newInstance() {

    Log.d(TAG, "++newInstance()");
    return new ArtistsFragment();
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
      mCallback = (OnArtistListener) context;
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
    View view =  inflater.inflate(R.layout.fragment_artists, container, false);
    mRecyclerView = view.findViewById(R.id.artists_list_view);
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

    ArtistsAdapter artistsAdapter = new ArtistsAdapter(getContext());
    mRecyclerView.setAdapter(artistsAdapter);
    mMediaViewModel.getAllArtists().observe(getViewLifecycleOwner(), artists -> {

      if (artists == null || artists.size() == 0) {
        // TODO: callback to activity
      } else {
        artistsAdapter.setArtistsList(artists);
      }
    });
  }

  /*
    Adapter class for Artists objects
   */
  private class ArtistsAdapter extends RecyclerView.Adapter<ArtistsAdapter.ArtistsHolder> {

    /*
      Holder class for Artists objects
     */
    class ArtistsHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

      private final TextView mArtistTextView;
      private final TextView mSongsTextView;

      private ArtistsView mArtist;

      ArtistsHolder(View itemView) {
        super(itemView);

        ImageView imageView = itemView.findViewById(R.id.media_item_image);
        imageView.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_artist_dark, null));
        mArtistTextView = itemView.findViewById(R.id.media_item_text_title);
        mSongsTextView = itemView.findViewById(R.id.media_item_text_details);

        TextView albumTextView = itemView.findViewById(R.id.media_item_text_subtitle);
        albumTextView.setVisibility(View.INVISIBLE);
        ImageView dragImage = itemView.findViewById(R.id.media_item_image_drag_bar);
        dragImage.setVisibility(View.INVISIBLE);
        ImageView optionsImage = itemView.findViewById(R.id.media_item_image_options);
        optionsImage.setVisibility(View.INVISIBLE);

        itemView.setOnClickListener(this);
      }

      void bind(ArtistsView artist) {

        mArtist = artist;

        if (mArtist != null) {
          mArtistTextView.setText(mArtist.ArtistName);
          mSongsTextView.setText(getResources().getQuantityString(R.plurals.format_songs, mArtist.SongCount, mArtist.SongCount));
        }
      }

      @Override
      public void onClick(View view) {

        mCallback.onArtistClicked(mArtist.ArtistId);
      }
    }

    private final LayoutInflater mInflater;
    private List<ArtistsView> mArtists;

    ArtistsAdapter(Context context) {

      mInflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public ArtistsHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

      View itemView = mInflater.inflate(R.layout.item_media, parent, false);
      return new ArtistsHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ArtistsHolder holder, int position) {

      if (mArtists != null) {
        ArtistsView artist = mArtists.get(position);
        holder.bind(artist);
      } else {
        // TODO: No artists!
      }
    }

    @Override
    public int getItemCount() {

      if (mArtists != null) {
        return mArtists.size();
      } else {
        return 0;
      }
    }

    void setArtistsList(Collection<ArtistsView> artists) {

      Log.d(TAG, "++setArtistsList(Collection<ArtistsView>)");
      mArtists = new ArrayList<>();
      mArtists.addAll(artists);
      notifyDataSetChanged();
    }
  }
}
