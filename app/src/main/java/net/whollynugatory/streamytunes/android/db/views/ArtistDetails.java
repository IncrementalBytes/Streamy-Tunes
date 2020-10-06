package net.whollynugatory.streamytunes.android.db.views;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.HashMap;
import java.util.Map;

public class ArtistDetails implements Parcelable {

  public long Id;
  public String Name;
  public HashMap<Long, AlbumDetails> Albums;

  public ArtistDetails() {

    Id = 0;
    Name = "";
    Albums = new HashMap<>();
  }

  protected ArtistDetails(Parcel in) {
    Id = in.readLong();
    Name = in.readString();
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {

    dest.writeLong(Id);
    dest.writeString(Name);
  }

  @Override
  public int describeContents() {

    return 0;
  }

  public static final Creator<ArtistDetails> CREATOR = new Creator<ArtistDetails>() {

    @Override
    public ArtistDetails createFromParcel(Parcel in) {
      return new ArtistDetails(in);
    }

    @Override
    public ArtistDetails[] newArray(int size) {
      return new ArtistDetails[size];
    }
  };

  /*
    Public Method(s)
   */
  public String getAlbumNames() {

    StringBuilder output = new StringBuilder();
    for (Map.Entry<Long, AlbumDetails> albumDetail : Albums.entrySet()) {
      output.append(albumDetail.getValue().Name).append(", ");
    }

    return output.substring(0, output.length() - 2);
  }

  public int getSongCount() {

    int totalCount = 0;
    for (Map.Entry<Long, AlbumDetails> albumDetail : Albums.entrySet()) {
      totalCount += albumDetail.getValue().Songs.size();
    }

    return totalCount;
  }
}
