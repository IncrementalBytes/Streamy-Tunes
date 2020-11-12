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
package net.whollynugatory.streamytunes.android.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import net.whollynugatory.streamytunes.android.db.entity.MediaEntity;

import java.util.List;

@Dao
public interface MediaDao {

  @Query("DELETE FROM media_table WHERE id = :id")
  void delete(String id);

  @Query("SELECT * FROM media_table")
  LiveData<List<MediaEntity>> getAll();

  @Query("SELECT * FROM media_table WHERE is_audiobook == 1")
  LiveData<List<MediaEntity>> getAllAudiobooks();

  @Query("SELECT * FROM media_table WHERE is_music == 1")
  LiveData<List<MediaEntity>> getAllMusic();

  @Query("SELECT * FROM media_table WHERE is_podcast == 1")
  LiveData<List<MediaEntity>> getAllPodcasts();

  @Insert(onConflict = OnConflictStrategy.IGNORE)
  void insert(MediaEntity mediaEntity);

  @Insert(onConflict = OnConflictStrategy.IGNORE)
  void insertAll(List<MediaEntity> mediaEntityList);

  @Update
  void update(MediaEntity mediaEntity);
}
