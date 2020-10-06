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
package net.whollynugatory.streamytunes.android.db.models;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

public class SongDetails implements Parcelable {

  public long Id;
  public long ArtistId;
  public String ArtistName;
  public long AlbumId;
  public String AlbumName;
  public String DisplayName;
  public Uri LocalSource;
  public String Title;
  public long Track;
  public long Year;

  public SongDetails() {

    Id = 0;
    AlbumId = 0;
    AlbumName = "";
    ArtistId = 0;
    ArtistName = "";
    DisplayName = "";
    LocalSource = null;
    Title = "";
    Track = 0;
    Year = 0;
  }

  protected SongDetails(Parcel in) {
    Id = in.readLong();
    ArtistId = in.readLong();
    ArtistName = in.readString();
    AlbumId = in.readLong();
    AlbumName = in.readString();
    DisplayName = in.readString();
    LocalSource = in.readParcelable(Uri.class.getClassLoader());
    Title = in.readString();
    Track = in.readLong();
    Year = in.readLong();
  }

  /*
      Object Override(s)
     */
  public static final Creator<SongDetails> CREATOR = new Creator<SongDetails>() {
    @Override
    public SongDetails createFromParcel(Parcel in) {
      return new SongDetails(in);
    }

    @Override
    public SongDetails[] newArray(int size) {
      return new SongDetails[size];
    }
  };

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel parcel, int i) {
    parcel.writeLong(Id);
    parcel.writeLong(ArtistId);
    parcel.writeString(ArtistName);
    parcel.writeLong(AlbumId);
    parcel.writeString(AlbumName);
    parcel.writeString(DisplayName);
    parcel.writeParcelable(LocalSource, i);
    parcel.writeString(Title);
    parcel.writeLong(Track);
    parcel.writeLong(Year);
  }

  /*
    Public Method(s)
   */
  public AlbumDetails toAlbumDetails() {

    AlbumDetails albumDetails = new AlbumDetails();
    albumDetails.Id = AlbumId;
    albumDetails.ArtistName = ArtistName;
    albumDetails.Name = AlbumName;
    albumDetails.Songs.put(Id, this);
    return albumDetails;
  }

  public ArtistDetails toArtistDetails() {

    ArtistDetails artistDetails = new ArtistDetails();
    artistDetails.Id = ArtistId;
    artistDetails.Name = ArtistName;
    artistDetails.Albums.put(AlbumId, toAlbumDetails());
    return artistDetails;
  }
}
