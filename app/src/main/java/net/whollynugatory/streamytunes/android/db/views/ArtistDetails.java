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
package net.whollynugatory.streamytunes.android.db.views;

import androidx.room.DatabaseView;
import androidx.room.Ignore;

import net.whollynugatory.streamytunes.android.db.entity.MediaEntity;

import java.util.HashMap;

@DatabaseView(
  "SELECT MediaData.artist_id AS Id, " +
    "MediaData.artist_name AS ArtistName " +
    "FROM media_table AS MediaData")
public class ArtistDetails {

  public long Id;
  public String Name;

  @Ignore
  public HashMap<Long, AlbumDetails> Albums;

  public ArtistDetails() {

    Id = -1;
    Name = "UNKNOWN";
    Albums = new HashMap<>();
  }

  @Ignore
  public String getAlbumNames() {

    StringBuilder albumNames = new StringBuilder();
    for (AlbumDetails albumDetails : Albums.values()) {
      albumNames.append(albumDetails.Name);
      albumNames.append(", ");
    }

    return albumNames.substring(0, albumNames.length() - 2);
  }

  @Ignore
  public int getSongCount() {

    int songCount = 0;
    for (AlbumDetails albumDetails : Albums.values()) {
      songCount += albumDetails.MediaMap.values().size();
    }

    return songCount;
  }

  @Ignore
  public static ArtistDetails createArtistDetails(MediaEntity mediaEntity) {

    ArtistDetails artistDetails = new ArtistDetails();
    artistDetails.Id = mediaEntity.ArtistId;
    artistDetails.Name = mediaEntity.ArtistName;
    artistDetails.Albums.put(mediaEntity.AlbumId, AlbumDetails.createAlbumDetails(mediaEntity));
    return artistDetails;
  }
}
