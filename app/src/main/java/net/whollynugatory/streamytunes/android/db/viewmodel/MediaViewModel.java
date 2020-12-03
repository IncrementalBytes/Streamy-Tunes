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
package net.whollynugatory.streamytunes.android.db.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import net.whollynugatory.streamytunes.android.db.AlbumsView;
import net.whollynugatory.streamytunes.android.db.ArtistsView;
import net.whollynugatory.streamytunes.android.db.MediaDetails;
import net.whollynugatory.streamytunes.android.db.PlaylistDetails;
import net.whollynugatory.streamytunes.android.db.PlaylistsView;
import net.whollynugatory.streamytunes.android.db.StreamyTunesDatabase;
import net.whollynugatory.streamytunes.android.db.repository.MediaRepository;

import java.util.List;

public class MediaViewModel extends AndroidViewModel {

  private final MediaRepository mMediaRepository;

  public MediaViewModel(@NonNull Application application) {
    super(application);

    mMediaRepository = MediaRepository.getInstance(StreamyTunesDatabase.getInstance(application).mediaDao());
  }

  public LiveData<List<AlbumsView>> getAllAlbums() {

    return mMediaRepository.getAllAlbums();
  }

  public LiveData<List<ArtistsView>> getAllArtists() {

    return mMediaRepository.getAllArtists();
  }

  public LiveData<List<MediaDetails>> getAllAudiobooks() {

    return mMediaRepository.getAllAudiobooks();
  }

  public LiveData<List<MediaDetails>> getAllMusic() {

    return mMediaRepository.getAllMusic();
  }

  public LiveData<List<MediaDetails>> getAllMusicByAlbumId(long albumId) {

    return mMediaRepository.getAllMusicByAlbumId(albumId);
  }

  public LiveData<List<MediaDetails>> getAllMusicByArtistId(long artistId) {

    return mMediaRepository.getAllMusicByArtistId(artistId);
  }

    public LiveData<List<PlaylistDetails>> getPlaylistById(String playlistId) {

    return mMediaRepository.getPlaylistById(playlistId);
  }

  public LiveData<List<PlaylistsView>> getAllPlaylists() {

    return mMediaRepository.getAllPlaylists();
  }

  public LiveData<List<MediaDetails>> getAllPodcasts() {

    return mMediaRepository.getAllPodcasts();
  }
}
