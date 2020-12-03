/*
 * Copyright 2019 Ryan Ward
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
package net.whollynugatory.streamytunes.android.db.repository;

import android.util.Log;

import androidx.lifecycle.LiveData;

import net.whollynugatory.streamytunes.android.db.AlbumsView;
import net.whollynugatory.streamytunes.android.db.ArtistsView;
import net.whollynugatory.streamytunes.android.db.MediaDetails;
import net.whollynugatory.streamytunes.android.db.PlaylistDetails;
import net.whollynugatory.streamytunes.android.db.PlaylistsView;
import net.whollynugatory.streamytunes.android.db.dao.MediaDao;
import net.whollynugatory.streamytunes.android.db.entity.AlbumEntity;
import net.whollynugatory.streamytunes.android.db.entity.ArtistEntity;
import net.whollynugatory.streamytunes.android.db.entity.MediaEntity;
import net.whollynugatory.streamytunes.android.db.entity.PlaylistEntity;
import net.whollynugatory.streamytunes.android.ui.BaseActivity;

import java.util.List;

public class MediaRepository {

  private static final String TAG = BaseActivity.BASE_TAG + MediaRepository.class.getSimpleName();

  private static volatile MediaRepository INSTANCE;

  private final MediaDao mMediaDao;

  private MediaRepository(MediaDao mediaDao) {

    mMediaDao = mediaDao;
  }

  public static MediaRepository getInstance(final MediaDao mediaDao) {

    if (INSTANCE == null) {
      synchronized (MediaRepository.class) {
        if (INSTANCE == null) {
          Log.d(TAG, "++getInstance(MediaDao)");
          INSTANCE = new MediaRepository(mediaDao);
        }
      }
    }

    return INSTANCE;
  }

  public void deletePlaylist(PlaylistEntity playlistEntity) {

    mMediaDao.deletePlaylist(playlistEntity);
  }

  public LiveData<List<AlbumsView>> getAllAlbums() {

    return mMediaDao.getAllAlbums();
  }

  public LiveData<List<ArtistsView>> getAllArtists() {

    return mMediaDao.getAllArtists();
  }

  public LiveData<List<MediaDetails>> getAllAudiobooks() {

    return mMediaDao.getAllAudiobooks();
  }

  public LiveData<List<MediaDetails>> getAllMusic() {

    return mMediaDao.getAllMusic();
  }

  public LiveData<List<MediaDetails>> getAllMusicByAlbumId(long albumId) {

    return mMediaDao.getAllMusicByAlbumId(albumId);
  }

  public LiveData<List<MediaDetails>> getAllMusicByArtistId(long artistId) {

    return mMediaDao.getAllMusicByArtistId(artistId);
  }

  public LiveData<List<PlaylistDetails>> getPlaylistById(String playlistId) {

    return mMediaDao.getPlaylistById(playlistId);
  }

  public LiveData<List<PlaylistsView>> getAllPlaylists() {

    return mMediaDao.getAllPlaylists();
  }

  public LiveData<List<MediaDetails>> getAllPodcasts() {

    return mMediaDao.getAllPodcasts();
  }

  public void insertAlbum(AlbumEntity albumEntity) {

    mMediaDao.insertAlbum(albumEntity);
  }

  public void insertArtist(ArtistEntity artistEntity) {

    mMediaDao.insertArtist(artistEntity);
  }

  public void insertMedia(MediaEntity mediaEntity) {

    mMediaDao.insertMedia(mediaEntity);
  }

  public void insertPlaylist(PlaylistEntity playlistEntity) {

    mMediaDao.insertPlaylist(playlistEntity);
  }

  public void updateMedia(MediaEntity mediaEntity) {

    mMediaDao.updateMedia(mediaEntity);
  }
}
