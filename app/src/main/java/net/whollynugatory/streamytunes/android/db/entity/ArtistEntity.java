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

@Entity(tableName = ArtistEntity.TABLE_NAME)
public class ArtistEntity {

  @Ignore
  public static final  String TABLE_NAME = "artists_table";

  @PrimaryKey
  public long ArtistId;

  @NonNull
  public String ArtistName;

  public ArtistEntity() {

    ArtistId = BaseActivity.UNKNOWN_ID;
    ArtistName = BaseActivity.UNKNOWN_STRING;
  }
}
