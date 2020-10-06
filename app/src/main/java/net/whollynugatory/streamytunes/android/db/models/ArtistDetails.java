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
