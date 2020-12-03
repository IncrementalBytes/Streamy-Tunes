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

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import net.whollynugatory.streamytunes.android.db.dao.MediaDao;
import net.whollynugatory.streamytunes.android.db.entity.AlbumEntity;
import net.whollynugatory.streamytunes.android.db.entity.ArtistEntity;
import net.whollynugatory.streamytunes.android.db.entity.PlaylistEntity;
import net.whollynugatory.streamytunes.android.db.entity.MediaEntity;
import net.whollynugatory.streamytunes.android.ui.BaseActivity;

@Database(
  entities = {AlbumEntity.class, ArtistEntity.class, MediaEntity.class, PlaylistEntity.class},
  views = {AlbumsView.class, ArtistsView.class, MediaDetails.class,PlaylistDetails.class,PlaylistsView.class},
  version = 1,
  exportSchema = false)
public abstract class StreamyTunesDatabase extends RoomDatabase {

  private static final String TAG = BaseActivity.BASE_TAG + StreamyTunesDatabase.class.getSimpleName();

  public abstract MediaDao mediaDao();

  private static StreamyTunesDatabase sInstance;

  public static StreamyTunesDatabase getInstance(final Context context) {

    if (sInstance == null) {
      synchronized (StreamyTunesDatabase.class) {
        if (sInstance == null) {
          sInstance = Room.databaseBuilder(context.getApplicationContext(), StreamyTunesDatabase.class, BaseActivity.DATABASE_NAME)
            .addCallback(sRoomDatabaseCallback)
            .build();
        }
      }
    }

    return sInstance;
  }

  private static final RoomDatabase.Callback sRoomDatabaseCallback = new RoomDatabase.Callback() {

    @Override
    public void onCreate(@NonNull SupportSQLiteDatabase db) {
      super.onCreate(db);

      Log.d(TAG, "++onCreate(SupportSQLiteDatabase)");
//      ContentValues contentValues = new ContentValues();
//      contentValues.put("Id", UUID.randomUUID().toString());
//      contentValues.put("PlaylistId", BaseActivity.DEFAULT_PLAYLIST_FAVORITES_ID);
//      contentValues.put("PlaylistName", BaseActivity.DEFAULT_PLAYLIST_FAVORITES);
//      contentValues.put("AddedTimeStamp", Calendar.getInstance().getTimeInMillis());
//      db.insert(PlaylistEntity.TABLE_NAME, CONFLICT_REPLACE, contentValues);
    }

    @Override
    public void onOpen(@NonNull SupportSQLiteDatabase db) {
      super.onOpen(db);

      Log.d(TAG, "++onOpen(SupportSQLiteDatabase)");
    }
  };
}
