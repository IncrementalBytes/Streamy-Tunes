package net.whollynugatory.streamytunes.android.db.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.HashMap;

public class AuthorDetails implements Parcelable {

  public long Id;
  public String Name;
  public HashMap<Long, MediaDetails> Audiobooks;

  public AuthorDetails() {

    Id = 0;
    Name = "";
    Audiobooks = new HashMap<>();
  }

  protected AuthorDetails(Parcel in) {

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
}
