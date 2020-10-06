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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

public class AlbumsFragment extends Fragment {

  private static final String TAG = Utils.BASE_TAG + "AlbumsFragment";

  public interface OnAlbumListener {

    void onAlbumClicked(AlbumDetails albumDetails);
  }

  private OnAlbumListener mCallback;
  private List<AlbumDetails> mAlbumDetailsList;

  private RecyclerView mRecyclerView;

  public static AlbumsFragment newInstance(ArrayList<AlbumDetails> albumDetailsList) {

    Log.d(TAG, "++newInstance(ArrayList<AlbumDetails>)");
    Bundle arguments = new Bundle();
    arguments.putParcelableArrayList(Utils.ARG_ALBUM_DETAILS_COLLECTION, albumDetailsList);
    AlbumsFragment fragment = new AlbumsFragment();
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
      mCallback = (OnAlbumListener) context;
    } catch (ClassCastException e) {
      throw new ClassCastException(
        String.format(Locale.ENGLISH, "Missing interface implementations for %s", context.toString()));
    }

    Bundle arguments = getArguments();
    if (arguments != null) {
      mAlbumDetailsList = arguments.getParcelableArrayList(Utils.ARG_ALBUM_DETAILS_COLLECTION);
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
    albumAdapter.setAlbumInfoList(mAlbumDetailsList);
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

      private AlbumDetails mAlbumDetails;

      AlbumHolder(View itemView) {
        super(itemView);

        mAlbumImage = itemView.findViewById(R.id.album_item_image_album);
        mAlbumTextView = itemView.findViewById(R.id.album_item_text_album);
        mArtistTextView = itemView.findViewById(R.id.album_item_text_artist);
        mSongsTextView = itemView.findViewById(R.id.album_item_text_songs);

        itemView.setOnClickListener(this);
      }

      void bind(AlbumDetails albumDetails) {

        mAlbumDetails = albumDetails;

        if (mAlbumDetails != null) {
          mAlbumTextView.setText(mAlbumDetails.Name);
          mSongsTextView.setText(String.format(getString(R.string.format_songs), mAlbumDetails.Songs.values().size()));
          mArtistTextView.setText(mAlbumDetails.ArtistName);
        }
      }

      @Override
      public void onClick(View view) {

        mCallback.onAlbumClicked(mAlbumDetails);
      }
    }

    private final LayoutInflater mInflater;
    private List<AlbumDetails> mAlbumDetailsList;

    AlbumAdapter(Context context) {

      mInflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public AlbumHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

      View itemView = mInflater.inflate(R.layout.item_album, parent, false);
      return new AlbumHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull AlbumHolder holder, int position) {

      if (mAlbumDetailsList != null) {
        AlbumDetails albumDetails = mAlbumDetailsList.get(position);
        holder.bind(albumDetails);
      } else {
        // TODO: No albums!
      }
    }

    @Override
    public int getItemCount() {

      if (mAlbumDetailsList != null) {
        return mAlbumDetailsList.size();
      } else {
        return 0;
      }
    }

    void setAlbumInfoList(Collection<AlbumDetails> albumDetailsCollection) {

      Log.d(TAG, "++setAlbumInfoList(Collection<AlbumDetails>)");
      mAlbumDetailsList = new ArrayList<>();
      mAlbumDetailsList.addAll(albumDetailsCollection);
      notifyDataSetChanged();
    }
  }
}
