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

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.HashMap;

public class AlbumDetails implements Parcelable {

  public long Id;
  public Bitmap Art;
  public String ArtistName;
  public String Name;
  public HashMap<Long, MediaDetails> MediaMap;

  public AlbumDetails() {

    Id = 0;
    Art = null;
    ArtistName = "";
    Name = "";
    MediaMap = new HashMap<>();
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