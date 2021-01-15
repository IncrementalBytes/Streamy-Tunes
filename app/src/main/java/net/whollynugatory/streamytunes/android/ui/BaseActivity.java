/*
 * Copyright 2021 Ryan Ward
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
package net.whollynugatory.streamytunes.android.ui;

import androidx.appcompat.app.AppCompatActivity;

public class BaseActivity extends AppCompatActivity {

  public static final String BASE_TAG = "StreamyTunes::";

  public static final int UNKNOWN_ID = -1;
  public static final String UNKNOWN_STRING = "UNKNOWN";
  public static final String UNKNOWN_GUID = "00000000-0000-0000-0000-0000000000000";

  public static final int REQUEST_SYNC = 1001;

  public static final String ARG_ALBUM_ID = "album_id";
  public static final String ARG_ARTIST_ID = "artist_id";
  public static final String ARG_PLAYLIST_ID = "playlist_id";

  public static final String DATABASE_NAME = "streamytunes-db.sqlite";
  public static final String DEFAULT_PLAYLIST_FAVORITES_ID = "5BD0B3B2-8362-4D3F-8EC4-BA4921ABF578";
  public static final String DEFAULT_PLAYLIST_FAVORITES = "Favorites";

  public static final int REQUEST_STORAGE_PERMISSIONS = 2001;
  public static final int REQUEST_PHONE_PERMISSIONS = 2002;

  public static final String ACTION_PLAY = "net.whollynugatory.streamytunes.android.PLAY";
  public static final String ACTION_PAUSE = "net.whollynugatory.streamytunes.android.PAUSE";
  public static final String ACTION_PREVIOUS = "net.whollynugatory.streamytunes.android.PREVIOUS";
  public static final String ACTION_NEXT = "net.whollynugatory.streamytunes.android.NEXT";

  public static final String BROADCAST_NEXT_AUDIO = "net.whollynugatory.streamytunes.android.NextAudio";
  public static final String BROADCAST_PAUSE_AUDIO = "net.whollynugatory.streamytunes.android.PauseAudio";
  public static final String BROADCAST_PLAY_AUDIO = "net.whollynugatory.streamytunes.android.PlayAudio";
  public static final String BROADCAST_PREV_AUDIO = "net.whollynugatory.streamytunes.android.PrevAudio";

  public enum ServiceState {

    Stopped,
    Preparing,
    Playing,
    Paused
  }
}
