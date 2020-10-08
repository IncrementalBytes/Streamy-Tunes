package net.whollynugatory.streamytunes.android.ui;

import androidx.appcompat.app.AppCompatActivity;

public class BaseActivity extends AppCompatActivity {

  public static final String BASE_TAG = "StreamyTunes::";

  public static final String ARG_ARTIST_DETAILS_COLLECTION = "artist_details_collection";
  public static final String ARG_ALBUM_DETAILS_COLLECTION = "album_details_collection";
  public static final String ARG_SONG_DETAILS_LIST = "song_details_list";
  public static final String ARG_SONG_DETAILS = "song_details";

  public static final String DEFAULT_ID = "00000000-0000-0000-0000-000000000000";
  public static final String DATABASE_NAME = "streamytunes-db.sqlite";
  public static final String DEFAULT_SOURCE = "Music";

  public static final String MP3_PATTERN = ".mp3";

  public static final int REQUEST_SETTINGS = 5001;
  public static final int REQUEST_STORAGE_PERMISSIONS = 1001;
}
