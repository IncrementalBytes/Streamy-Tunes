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

  public static final int REQUEST_SYNC = 1001;

  public static final String ARG_ALBUM_DETAILS_LIST = "album_details_list";
  public static final String ARG_ARTIST_DETAILS_LIST = "artist_details_list";
  public static final String ARG_MEDIA_ENTITY_LIST = "media_entity_list";

  public static final String DATABASE_NAME = "streamytunes-db.sqlite";

  public static final int REQUEST_STORAGE_PERMISSIONS = 2001;
}
