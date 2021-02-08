/*
 * Copyright 2021Ryan Ward
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
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import net.whollynugatory.streamytunes.android.db.AlbumsView;
import net.whollynugatory.streamytunes.android.db.ArtistsView;
import net.whollynugatory.streamytunes.android.db.MediaDetails;
import net.whollynugatory.streamytunes.android.db.PlaylistDetails;
import net.whollynugatory.streamytunes.android.db.PlaylistsView;
import net.whollynugatory.streamytunes.android.db.entity.AlbumEntity;
import net.whollynugatory.streamytunes.android.db.entity.ArtistEntity;
import net.whollynugatory.streamytunes.android.db.entity.MediaEntity;
import net.whollynugatory.streamytunes.android.db.entity.PlaylistEntity;

import java.util.List;

@Dao
public interface MediaDao {

  @Delete
  void deletePlaylist(PlaylistEntity playlistEntity);

  @Query("SELECT * FROM AlbumsView")
  LiveData<List<AlbumsView>> getAlbums();

  @Query("SELECT * FROM ArtistsView")
  LiveData<List<ArtistsView>> getArtists();

  @Query("SELECT * FROM media_table WHERE IsAudiobook == 1")
  LiveData<List<MediaDetails>> getAudiobooks();

  @Query("SELECT * FROM media_table WHERE IsMusic == 1")
  LiveData<List<MediaDetails>> getMusic();

  @Query("SELECT * FROM MediaDetails WHERE IsMusic == 1 AND AlbumId == :albumId")
  LiveData<List<MediaDetails>> getMusicByAlbumId(long albumId);

  @Query("SELECT * FROM MediaDetails WHERE IsMusic == 1 AND ArtistId == :artistId")
  LiveData<List<MediaDetails>> getMusicByArtistId(long artistId);

  @Query("SELECT * FROM PlaylistDetails WHERE PlaylistId == :playlistId")
  LiveData<List<PlaylistDetails>> getMusicByPlaylistId(String playlistId);

  @Query("SELECT * FROM PlaylistsView")
  LiveData<List<PlaylistsView>> getPlaylists();

  @Query("SELECT * FROM media_table WHERE IsPodcast == 1")
  LiveData<List<MediaDetails>> getPodcasts();

  @Insert(onConflict = OnConflictStrategy.IGNORE)
  void insertAlbum(AlbumEntity albumEntity);

  @Insert(onConflict = OnConflictStrategy.IGNORE)
  void insertArtist(ArtistEntity artistEntity);

  @Insert
  void insertMedia(MediaEntity mediaEntity);

//  @Insert
//  void insertMediaMetadata(MediaMetadataCompat mediaMetadataCompat);

  @Insert
  void insertPlaylist(PlaylistEntity playlistEntity);

  @Update
  void updateMedia(MediaEntity mediaEntity);
}
