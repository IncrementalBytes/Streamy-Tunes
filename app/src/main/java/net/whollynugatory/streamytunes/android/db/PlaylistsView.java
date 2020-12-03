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
  "SELECT DISTINCT Playlists.PlaylistId, " +
    "Playlists.PlaylistName, " +
    "COUNT(MediaId) AS SongCount " +
    "FROM playlists_table AS Playlists " +
    "GROUP BY PlaylistId")
public class PlaylistsView implements Serializable {

  public String PlaylistId;
  public String PlaylistName;
  public int SongCount;

  public PlaylistsView() {

    PlaylistId = BaseActivity.UNKNOWN_GUID;
    PlaylistName = BaseActivity.UNKNOWN_STRING;
    SongCount = 0;
  }
}
