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
  "SELECT Artists.ArtistName," +
    "Artists.ArtistId," +
    "COUNT(*) AS SongCount  " +
    "FROM media_table AS Media " +
    "JOIN artists_table AS Artists ON Artists.ArtistId = Media.ArtistId " +
    "GROUP BY Artists.ArtistId")
public class ArtistsView implements Serializable {

  public long ArtistId;
  public String ArtistName;
  public int SongCount;

  public ArtistsView() {

    ArtistId = BaseActivity.UNKNOWN_ID;
    ArtistName = BaseActivity.UNKNOWN_STRING;
    SongCount = 0;
  }
}
