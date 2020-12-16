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

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import net.whollynugatory.streamytunes.android.db.entity.AudioEntity;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class PreferenceUtils {

  public static void clearCachedAudioList(Context context) {

    SharedPreferences sharedPreferences = context.getSharedPreferences(context.getString(R.string.pref_key_audio_list), Context.MODE_PRIVATE);
    sharedPreferences.edit().clear().apply();
  }

  /**
   *
   * @param context Current application context
   * @return Returns -1 if no data found
   */
  public static int getAudioIndex(Context context) {

    return getIntPref(context, R.string.pref_key_audio_index);
  }

  /**
   *
   * @param context Current application context
   * @return Returns empty list if no data found
   */
  public static ArrayList<AudioEntity> getAudioList(Context context) {

    Gson gson = new Gson();
    String json = getStringPref(context, R.string.pref_key_audio_list);
    Type type = new TypeToken<ArrayList<AudioEntity>>() { }.getType();
    return gson.fromJson(json, type);
  }

  public static boolean getIsAudiobook(Context context) {

    return getBooleanPref(context, R.string.pref_key_is_audiobook);
  }

  public static boolean getIsMusic(Context context) {

    return getBooleanPref(context, R.string.pref_key_is_music);
  }

  public static boolean getIsPodcast(Context context) {

    return getBooleanPref(context, R.string.pref_key_is_podcast);
  }

  public static boolean getShowHidden(Context context) {

    return getBooleanPref(context, R.string.pref_key_show_hidden);
  }

  public static void saveBooleanPreference(Context context, @StringRes int prefKeyId, boolean value) {

    PreferenceManager.getDefaultSharedPreferences(context)
      .edit()
      .putBoolean(context.getString(prefKeyId), value)
      .apply();
  }

  public static void setAudioList(Context context, ArrayList<AudioEntity> arrayList) {

    Gson gson = new Gson();
    String json = gson.toJson(arrayList);
    PreferenceManager.getDefaultSharedPreferences(context)
      .edit()
      .putString(context.getString(R.string.pref_key_audio_list), json)
      .apply();
  }

  public static void setAudioIndex(Context context, int index) {

    PreferenceManager.getDefaultSharedPreferences(context)
      .edit()
      .putInt(context.getString(R.string.pref_key_audio_index), index)
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

  public static void setShowHidden(Context context, boolean hiddenValue) {

    PreferenceManager.getDefaultSharedPreferences(context)
      .edit()
      .putBoolean(context.getString(R.string.pref_key_show_hidden), hiddenValue)
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

  private static int getIntPref(Context context, @StringRes int prefKeyId) {

    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    String prefKey = context.getString(prefKeyId);
    return sharedPreferences.getInt(prefKey, -1);
  }

  private static String getStringPref(Context context, @StringRes int prefKeyId) {

    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    String prefKey = context.getString(prefKeyId);
    return sharedPreferences.getString(prefKey, "");
  }
}
