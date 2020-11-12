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
package net.whollynugatory.streamytunes.android.db.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(tableName = "media_table")
public class MediaEntity implements Serializable {

  @PrimaryKey
  @ColumnInfo(name = "id")
  public long Id;

  @ColumnInfo(name = "album_id")
  public long AlbumId;

  @ColumnInfo(name = "album_name")
  public String AlbumName;

  @ColumnInfo(name = "artist_id")
  public long ArtistId;

  @ColumnInfo(name = "artist_name")
  public String ArtistName;

  @ColumnInfo(name = "is_audiobook")
  public boolean IsAudiobook;

  @ColumnInfo(name = "is_music")
  public boolean IsMusic;

  @ColumnInfo(name = "is_podcast")
  public boolean IsPodcast;

  @ColumnInfo(name = "is_favorite")
  public boolean IsFavorite;

  @ColumnInfo(name = "is_hidden")
  public boolean IsHidden;

  @ColumnInfo(name = "is_external")
  public boolean IsExternal;

  @ColumnInfo(name = "title")
  public String Title;

  @ColumnInfo(name = "track_number")
  public int TrackNumber;

  @ColumnInfo(name = "year")
  public int Year;

  public MediaEntity() {

    Id = -1;
    AlbumId = -1;
    AlbumName = "UNKNOWN";
    ArtistId = -1;
    ArtistName = "UNKNOWN";
    IsAudiobook = false;
    IsMusic = false;
    IsPodcast = false;
    IsFavorite = false;
    IsHidden = false;
    IsExternal = false;
    Title = "UNKNOWN";
    TrackNumber = -1;
    Year = -1;
  }
}
