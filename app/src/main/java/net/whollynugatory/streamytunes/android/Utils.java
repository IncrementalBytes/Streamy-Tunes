package net.whollynugatory.streamytunes.android;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.preference.PreferenceManager;

public class Utils {

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

  public static String getSource(Context context) {

    return getStringPref(context, R.string.pref_key_source, Utils.DEFAULT_SOURCE);
  }

  public static void saveStringPreference(

    Context context, @StringRes int prefKeyId, @Nullable String value) {
    PreferenceManager.getDefaultSharedPreferences(context)
      .edit()
      .putString(context.getString(prefKeyId), value)
      .apply();
  }

  /*
    Private Method(s)
   */
  private static String getStringPref(Context context, @StringRes int prefKeyId, String defaultValue) {

    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    String prefKey = context.getString(prefKeyId);
    return sharedPreferences.getString(prefKey, defaultValue);
  }
}
