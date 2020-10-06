package net.whollynugatory.streamytunes.android.db.views;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.HashMap;

public class AlbumDetails implements Parcelable {

  public long Id;
  public String ArtistName;
  public String Name;
  public HashMap<Long, SongDetails> Songs;

  public AlbumDetails() {

    Id = 0;
    Name = "";
    ArtistName = "";
    Songs = new HashMap<>();
  }

  protected AlbumDetails(Parcel in) {
    Id = in.readLong();
    ArtistName = in.readString();
    Name = in.readString();
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {

    dest.writeLong(Id);
    dest.writeString(ArtistName);
    dest.writeString(Name);
  }

  @Override
  public int describeContents() {

    return 0;
  }

  public static final Creator<AlbumDetails> CREATOR = new Creator<AlbumDetails>() {

    @Override
    public AlbumDetails createFromParcel(Parcel in) {

      return new AlbumDetails(in);
    }

    @Override
    public AlbumDetails[] newArray(int size) {

      return new AlbumDetails[size];
    }
  };
}
