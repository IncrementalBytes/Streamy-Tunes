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
package net.whollynugatory.streamytunes.android.db;

import androidx.room.DatabaseView;

import net.whollynugatory.streamytunes.android.ui.BaseActivity;

import java.io.Serializable;

@DatabaseView(
  "SELECT DISTINCT Albums.AlbumId, " +
    "Albums.AlbumName, " +
    "Artists.ArtistId, " +
    "Artists.ArtistName, " +
    "COUNT(Media.MediaId) AS SongCount " +
    "FROM albums_table AS Albums " +
    "INNER JOIN artists_table AS Artists ON Artists.ArtistId = Albums.ArtistId " +
    "INNER JOIN media_table AS Media ON Media.AlbumId = Albums.AlbumId " +
    "GROUP BY Albums.AlbumId")
public class AlbumsView implements Serializable {

  public long AlbumId;
  public String AlbumName;
  public long ArtistId;
  public String ArtistName;
  public int SongCount;

  public AlbumsView() {

    AlbumId = BaseActivity.UNKNOWN_ID;
    AlbumName = BaseActivity.UNKNOWN_STRING;
    ArtistId = BaseActivity.UNKNOWN_ID;
    ArtistName = BaseActivity.UNKNOWN_STRING;
    SongCount = 0;
  }
}
