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
  "SELECT MediaData.album_id AS Id, " +
    "MediaData.album_name AS Name, " +
    "MediaData.artist_id AS ArtistId, " +
    "MediaData.artist_name AS ArtistName " +
    "FROM media_table AS MediaData")
public class AlbumDetails {

  public long Id;
  public String Name;
  public long ArtistId;
  public String ArtistName;

  @Ignore
  public HashMap<Long, MediaEntity> MediaMap;

  public AlbumDetails() {

    Id = -1;
    Name= "UNKNOWN";
    ArtistId = -1;
    ArtistName = "UNKNOWN";
    MediaMap = new HashMap<>();
  }

  @Ignore
  public static AlbumDetails createAlbumDetails(MediaEntity mediaEntity) {

    AlbumDetails albumDetails = new AlbumDetails();
    albumDetails.Id = mediaEntity.AlbumId;
    albumDetails.ArtistName = mediaEntity.ArtistName;
    albumDetails.Name = mediaEntity.AlbumName;
    albumDetails.MediaMap.put(mediaEntity.Id, mediaEntity);
    return albumDetails;
  }
}
