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
package net.whollynugatory.streamytunes.android.ui;

import androidx.appcompat.app.AppCompatActivity;

public class BaseActivity extends AppCompatActivity {

  public static final String BASE_TAG = "StreamyTunes::";

  public static final int UNKNOWN_ID = -1;
  public static final String UNKNOWN_STRING = "UNKNOWN";
  public static final String UNKNOWN_GUID = "00000000-0000-0000-0000-0000000000000";

  public static final int REQUEST_SYNC = 1001;
  public static final int REQUEST_PLAYER = 1002;

  public static final String ARG_ALBUM_ID = "album_id";
  public static final String ARG_ARTIST_ID = "artist_id";
  public static final String ARG_MEDIA_DETAILS_LIST = "media_details_list";
  public static final String ARG_MEDIA_ENTITY_LIST = "media_entity_list";
  public static final String ARG_PLAYLIST_ID = "playlist_id";

  public static final String DATABASE_NAME = "streamytunes-db.sqlite";
  public static final String DEFAULT_PLAYLIST_FAVORITES_ID = "5BD0B3B2-8362-4D3F-8EC4-BA4921ABF578";
  public static final String DEFAULT_PLAYLIST_FAVORITES = "Favorites";

  public static final int REQUEST_STORAGE_PERMISSIONS = 2001;
}
