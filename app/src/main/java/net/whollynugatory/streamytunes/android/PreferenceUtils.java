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
package net.whollynugatory.streamytunes.android;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.StringRes;
import androidx.preference.PreferenceManager;

public class PreferenceUtils {

  public static boolean getIsAudiobook(Context context) {

    return getBooleanPref(context, R.string.pref_key_is_audiobook);
  }

  public static boolean getIsMusic(Context context) {

    return getBooleanPref(context, R.string.pref_key_is_music);
  }

  public static boolean getIsPodcast(Context context) {

    return getBooleanPref(context, R.string.pref_key_is_podcast);
  }

  public static void saveBooleanPreference(Context context, @StringRes int prefKeyId, boolean value) {

    PreferenceManager.getDefaultSharedPreferences(context)
      .edit()
      .putBoolean(context.getString(prefKeyId), value)
      .apply();
  }

  public static void setIsAudiobook(Context context) {

    PreferenceManager.getDefaultSharedPreferences(context)
      .edit()
      .putBoolean(context.getString(R.string.pref_key_is_audiobook), true)
      .putBoolean(context.getString(R.string.pref_key_is_music), false)
      .putBoolean(context.getString(R.string.pref_key_is_podcast), false)
      .apply();
  }

  public static void setIsMusic(Context context) {

    PreferenceManager.getDefaultSharedPreferences(context)
      .edit()
      .putBoolean(context.getString(R.string.pref_key_is_audiobook), false)
      .putBoolean(context.getString(R.string.pref_key_is_music), true)
      .putBoolean(context.getString(R.string.pref_key_is_podcast), false)
      .apply();
  }

  public static void setIsPodcast(Context context) {

    PreferenceManager.getDefaultSharedPreferences(context)
      .edit()
      .putBoolean(context.getString(R.string.pref_key_is_audiobook), false)
      .putBoolean(context.getString(R.string.pref_key_is_music), false)
      .putBoolean(context.getString(R.string.pref_key_is_podcast), true)
      .apply();
  }

  /*
    Private Method(s)
   */
  private static Boolean getBooleanPref(Context context, @StringRes int prefKeyId) {

    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    String prefKey = context.getString(prefKeyId);
    return sharedPreferences.getBoolean(prefKey, false);
  }
}
