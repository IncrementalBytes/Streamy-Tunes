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

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import net.whollynugatory.streamytunes.android.ui.BaseActivity;

@Entity(tableName = MediaEntity.TABLE_NAME)
public class MediaEntity {

  @Ignore
  public static final  String TABLE_NAME = "media_table";

  @PrimaryKey
  public long MediaId;

  @NonNull
  public String Title;

  public long AlbumId;

  public long ArtistId;

  public boolean IsAudiobook;

  public boolean IsExternal;

  public boolean IsFavorite;

  public boolean IsMusic;

  public boolean IsPodcast;

  public int TrackNumber;

  public int Year;

  public MediaEntity() {

    MediaId = BaseActivity.UNKNOWN_ID;
    Title = BaseActivity.UNKNOWN_STRING;
    AlbumId = BaseActivity.UNKNOWN_ID;
    IsAudiobook = false;
    IsExternal = false;
    IsFavorite = false;
    IsMusic = false;
    IsPodcast = false;
    TrackNumber = 0;
    Year = 0;
  }
}
