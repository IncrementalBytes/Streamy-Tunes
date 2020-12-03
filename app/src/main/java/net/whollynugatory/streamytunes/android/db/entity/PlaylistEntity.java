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

import java.util.Calendar;
import java.util.UUID;

@Entity(tableName = PlaylistEntity.TABLE_NAME)
public class PlaylistEntity {

  @Ignore
  public static final  String TABLE_NAME = "playlists_table";

  @PrimaryKey
  @NonNull
  public String Id;

  @NonNull
  public String PlaylistId;

  @NonNull
  public String PlaylistName;

  public long MediaId;

  public long AddedTimeStamp;

  public PlaylistEntity() {

    Id = UUID.randomUUID().toString();
    PlaylistId = BaseActivity.UNKNOWN_GUID;
    PlaylistName = BaseActivity.UNKNOWN_STRING;
    MediaId = BaseActivity.UNKNOWN_ID;
    AddedTimeStamp = Calendar.getInstance().getTimeInMillis();
  }
}
