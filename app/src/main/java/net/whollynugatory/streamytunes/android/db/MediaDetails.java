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

import net.whollynugatory.streamytunes.android.db.entity.MediaEntity;
import net.whollynugatory.streamytunes.android.ui.BaseActivity;

import java.io.Serializable;

@DatabaseView(
  "SELECT Media.MediaId, " +
    "Media.Title, " +
    "Albums.AlbumId, " +
    "Albums.AlbumName, " +
    "Artists.ArtistId, " +
    "Artists.ArtistName, " +
    "Media.IsAudiobook, " +
    "Media.IsExternal, " +
    "Media.IsFavorite, " +
    "Media.IsMusic, " +
    "Media.IsPodcast, " +
    "Media.TrackNumber, " +
    "Media.Year " +
    "FROM media_table AS Media " +
    "JOIN albums_table AS Albums ON Albums.AlbumId = Media.AlbumId " +
    "JOIN artists_table AS Artists ON Artists.ArtistId = Media.ArtistId")
public class MediaDetails implements Serializable {

  public long MediaId;
  public String Title;
  public long AlbumId;
  public String AlbumName;
  public long ArtistId;
  public String ArtistName;
  public boolean IsAudiobook;
  public boolean IsExternal;
  public boolean IsFavorite;
  public boolean IsMusic;
  public boolean IsPodcast;
  public int TrackNumber;
  public int Year;

  public MediaDetails() {

    AlbumId = BaseActivity.UNKNOWN_ID;
    AlbumName = BaseActivity.UNKNOWN_STRING;
    ArtistId = BaseActivity.UNKNOWN_ID;
    ArtistName = BaseActivity.UNKNOWN_STRING;
    MediaId = BaseActivity.UNKNOWN_ID;
    Title = BaseActivity.UNKNOWN_STRING;
    IsAudiobook = false;
    IsExternal = false;
    IsFavorite = false;
    IsMusic = false;
    IsPodcast = false;
    TrackNumber = BaseActivity.UNKNOWN_ID;
    Year = BaseActivity.UNKNOWN_ID;
  }

  public MediaEntity toMediaEntity() {

    MediaEntity mediaEntity = new MediaEntity();
    mediaEntity.AlbumId = AlbumId;
    mediaEntity.ArtistId = ArtistId;
    mediaEntity.MediaId = MediaId;
    mediaEntity.Title = Title;
    mediaEntity.IsAudiobook = IsAudiobook;
    mediaEntity.IsExternal = IsExternal;
    mediaEntity.IsFavorite = IsFavorite;
    mediaEntity.IsMusic = IsMusic;
    mediaEntity.IsPodcast = IsPodcast;
    mediaEntity.TrackNumber = TrackNumber;
    mediaEntity.Year = Year;
    return mediaEntity;
  }
}
