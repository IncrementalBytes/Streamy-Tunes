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
import net.whollynugatory.streamytunes.android.db.views.AlbumDetails;
import net.whollynugatory.streamytunes.android.db.views.SongDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class SongsFragment extends Fragment {

  private static final String TAG = Utils.BASE_TAG + "SongsFragment";

  public interface OnSongListener {

    void onSongClicked(Collection<SongDetails> songDetailsList);
  }

  private OnSongListener mCallback;
  private List<SongDetails> mSongDetailsList;

  private RecyclerView mRecyclerView;

  public static SongsFragment newInstance(AlbumDetails albumDetails) {

    Log.d(TAG, "++newInstance(AlbumDetails)");
    List<AlbumDetails> albums = new ArrayList<>();
    albums.add(albumDetails);
    return newInstance(new ArrayList<>(albums));
  }

  public static SongsFragment newInstance(ArrayList<AlbumDetails> albumDetailsList) {

    Log.d(TAG, "++newInstance(ArrayList<AlbumDetails>)");
    Bundle arguments = new Bundle();
    arguments.putParcelableArrayList(Utils.ARG_ALBUM_DETAILS_COLLECTION, albumDetailsList);
    SongsFragment fragment = new SongsFragment();
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
      mCallback = (OnSongListener) context;
    } catch (ClassCastException e) {
      throw new ClassCastException(
        String.format(Locale.ENGLISH, "Missing interface implementations for %s", context.toString()));
    }

    Bundle arguments = getArguments();
    if (arguments != null) {
      List<AlbumDetails> albumDetails = arguments.getParcelableArrayList(Utils.ARG_ALBUM_DETAILS_COLLECTION);
      mSongDetailsList = new ArrayList<>();
      for (AlbumDetails album : albumDetails) {
        for (Map.Entry<Long, SongDetails> song : album.Songs.entrySet()) {
          mSongDetailsList.add(song.getValue());
        }
      }
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

    SongAdapter songAdapter = new SongAdapter(getContext());
    mRecyclerView.setAdapter(songAdapter);
    songAdapter.setSongInfoList(mSongDetailsList);
  }

  /*
    Adapter class for Song objects
   */
  private class SongAdapter extends RecyclerView.Adapter<SongAdapter.SongHolder> {

    /*
      Holder class for Song objects
     */
    class SongHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

      private final ImageView mAlbumImage;
      private final TextView mAlbumTextView;
      private final TextView mArtistTextView;
      private final TextView mTitleTextView;

      private SongDetails mSongDetails;

      SongHolder(View itemView) {
        super(itemView);

        mAlbumImage = itemView.findViewById(R.id.song_item_image_album);
        mAlbumTextView = itemView.findViewById(R.id.song_item_text_album);
        mArtistTextView = itemView.findViewById(R.id.song_item_text_artist);
        mTitleTextView = itemView.findViewById(R.id.song_item_text_title);

        itemView.setOnClickListener(this);
      }

      void bind(SongDetails songDetails) {

        mSongDetails = songDetails;

        if (mSongDetails != null) {
          mAlbumTextView.setText(mSongDetails.AlbumName);
          mTitleTextView.setText(mSongDetails.Title);
          mArtistTextView.setText(mSongDetails.ArtistName);
        }
      }

      @Override
      public void onClick(View view) {

        List<SongDetails> songDetailsList = new ArrayList<>();
        boolean addedToList = false;
        for (SongDetails songDetails : mSongDetailsList) {
          if (addedToList) {
            songDetailsList.add(songDetails);
          } else if (songDetails.Id == mSongDetails.Id) {
            addedToList = true;
            songDetailsList.add(songDetails);
          }
        }

        mCallback.onSongClicked(songDetailsList);
      }
    }

    private final LayoutInflater mInflater;
    private List<SongDetails> mSongDetailsList;

    SongAdapter(Context context) {

      mInflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public SongAdapter.SongHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

      View itemView = mInflater.inflate(R.layout.item_song, parent, false);
      return new SongAdapter.SongHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull SongAdapter.SongHolder holder, int position) {

      if (mSongDetailsList != null) {
        SongDetails songDetails = mSongDetailsList.get(position);
        holder.bind(songDetails);
      } else {
        // TODO: No songs!
      }
    }

    @Override
    public int getItemCount() {

      if (mSongDetailsList != null) {
        return mSongDetailsList.size();
      } else {
        return 0;
      }
    }

    void setSongInfoList(Collection<SongDetails> songDetailsCollection) {

      Log.d(TAG, "++setSongInfoList(Collection<SongDetails>)");
      mSongDetailsList = new ArrayList<>();
      mSongDetailsList.addAll(songDetailsCollection);
      notifyDataSetChanged();
    }
  }
}

