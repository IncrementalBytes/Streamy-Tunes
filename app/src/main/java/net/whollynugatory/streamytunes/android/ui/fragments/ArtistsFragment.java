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
import net.whollynugatory.streamytunes.android.Utils;
import net.whollynugatory.streamytunes.android.db.models.ArtistDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

public class ArtistsFragment extends Fragment {

  private static final String TAG = Utils.BASE_TAG + "ArtistsFragment";

  public interface OnArtistListener {

    void onArtistClicked(ArtistDetails artistDetails);
  }

  private OnArtistListener mCallback;
  private List<ArtistDetails> mArtistDetailsList;

  private RecyclerView mRecyclerView;

  public static ArtistsFragment newInstance(ArrayList<ArtistDetails> artistDetailsList) {

    Log.d(TAG, "++newInstance(ArrayList<ArtistDetails>)");
    Bundle arguments = new Bundle();
    arguments.putParcelableArrayList(Utils.ARG_ARTIST_DETAILS_COLLECTION, artistDetailsList);
    ArtistsFragment fragment = new ArtistsFragment();
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
      mCallback = (OnArtistListener) context;
    } catch (ClassCastException e) {
      throw new ClassCastException(
        String.format(Locale.ENGLISH, "Missing interface implementations for %s", context.toString()));
    }

    Bundle arguments = getArguments();
    if (arguments != null) {
      mArtistDetailsList = arguments.getParcelableArrayList(Utils.ARG_ARTIST_DETAILS_COLLECTION);
    } else {
      String message = "Arguments were null.";
      Log.e(TAG, message);
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
    artistsAdapter.setArtistsList(mArtistDetailsList);
  }

  /*
    Adapter class for Artists objects
   */
  private class ArtistsAdapter extends RecyclerView.Adapter<ArtistsAdapter.ArtistsHolder> {

    /*
      Holder class for Artists objects
     */
    class ArtistsHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

      private final ImageView mArtistsImage;
      private final TextView mAlbumsTextView;
      private final TextView mArtistTextView;
      private final TextView mSongsTextView;

      private ArtistDetails mArtistDetails;

      ArtistsHolder(View itemView) {
        super(itemView);

        mArtistsImage = itemView.findViewById(R.id.artist_item_image_artist);
        mAlbumsTextView = itemView.findViewById(R.id.artist_item_text_albums);
        mArtistTextView = itemView.findViewById(R.id.artist_item_text_artist);
        mSongsTextView = itemView.findViewById(R.id.artist_item_text_songs);

        itemView.setOnClickListener(this);
      }

      void bind(ArtistDetails artistDetails) {

        mArtistDetails = artistDetails;

        if (mArtistDetails != null) {
          mAlbumsTextView.setText(mArtistDetails.getAlbumNames());
          mArtistTextView.setText(mArtistDetails.Name);
          mSongsTextView.setText(String.format(getString(R.string.format_songs), mArtistDetails.getSongCount()));
        }
      }

      @Override
      public void onClick(View view) {

        mCallback.onArtistClicked(mArtistDetails);
      }
    }

    private final LayoutInflater mInflater;
    private List<ArtistDetails> mArtistDetailsList;

    ArtistsAdapter(Context context) {

      mInflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public ArtistsHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

      View itemView = mInflater.inflate(R.layout.item_artist, parent, false);
      return new ArtistsHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ArtistsHolder holder, int position) {

      if (mArtistDetailsList != null) {
        ArtistDetails artistDetails = mArtistDetailsList.get(position);
        holder.bind(artistDetails);
      } else {
        // TODO: No artists!
      }
    }

    @Override
    public int getItemCount() {

      if (mArtistDetailsList != null) {
        return mArtistDetailsList.size();
      } else {
        return 0;
      }
    }

    void setArtistsList(Collection<ArtistDetails> artistDetailsCollection) {

      Log.d(TAG, "++setArtistsList(Collection<ArtistDetails>)");
      mArtistDetailsList = new ArrayList<>();
      mArtistDetailsList.addAll(artistDetailsCollection);
      notifyDataSetChanged();
    }
  }
}
