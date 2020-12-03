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
  "SELECT Media.MediaId, " +
    "Media.Title, " +
    "Albums.AlbumId, " +
    "Albums.AlbumName, " +
    "Artists.ArtistId, " +
    "Artists.ArtistName, " +
    "Media.IsFavorite, " +
    "Media.IsVisible," +
    "Playlists.PlaylistName, " +
    "Playlists.PlaylistId, " +
    "Playlists.AddedTimeStamp " +
    "FROM playlists_table AS Playlists " +
    "INNER JOIN media_table AS Media ON Media.MediaId = Playlists.MediaId " +
    "INNER JOIN albums_table AS Albums ON Albums.AlbumId = Media.AlbumId " +
    "INNER JOIN artists_table AS Artists ON Artists.ArtistId = Media.ArtistId " +
    "ORDER BY Playlists.AddedTimeStamp DESC")
public class PlaylistDetails implements Serializable {

  public long MediaId;
  public String Title;
  public long AlbumId;
  public String AlbumName;
  public long ArtistId;
  public String ArtistName;
  public boolean IsFavorite;
  public boolean IsVisible;
  public String PlaylistName;
  public String PlaylistId;
  public long AddedTimeStamp;

  public PlaylistDetails() {

    MediaId = BaseActivity.UNKNOWN_ID;
    Title = BaseActivity.UNKNOWN_STRING;
    AlbumId = BaseActivity.UNKNOWN_ID;
    AlbumName = BaseActivity.UNKNOWN_STRING;
    ArtistId = BaseActivity.UNKNOWN_ID;
    ArtistName = BaseActivity.UNKNOWN_STRING;
    IsFavorite = false;
    IsVisible = true;
    PlaylistName = BaseActivity.UNKNOWN_STRING;
    PlaylistId = BaseActivity.UNKNOWN_GUID;
    AddedTimeStamp = 0;
  }

  public MediaDetails toMediaDetails() {

    MediaDetails mediaDetails = new MediaDetails();
    mediaDetails.MediaId = MediaId;
    mediaDetails.Title = Title;
    mediaDetails.AlbumId = AlbumId;
    mediaDetails.AlbumName = AlbumName;
    mediaDetails.ArtistId = ArtistId;
    mediaDetails.ArtistName = ArtistName;
    mediaDetails.IsFavorite = IsFavorite;
    mediaDetails.IsVisible = IsVisible;
    return mediaDetails;
  }
}
