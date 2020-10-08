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
package net.whollynugatory.streamytunes.android.ui.fragments;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.preference.CheckBoxPreference;
import androidx.preference.EditTextPreference;
import androidx.preference.PreferenceFragmentCompat;

import net.whollynugatory.streamytunes.android.BuildConfig;
import net.whollynugatory.streamytunes.android.PreferenceUtils;
import net.whollynugatory.streamytunes.android.R;
import net.whollynugatory.streamytunes.android.ui.BaseActivity;

public class UserSettingsFragment extends PreferenceFragmentCompat {

  private static final String TAG = BaseActivity.BASE_TAG + "UserSettingsFragment";

  public static UserSettingsFragment newInstance() {

    Log.d(TAG, "++newInstance()");
    return new UserSettingsFragment();
  }

  /*
    Fragment Override(s)
   */
  @Override
  public void onAttach(@NonNull Context context) {
    super.onAttach(context);

    Log.d(TAG, "++onAttach(Context)");
  }

  @Override
  public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {

    Log.d(TAG, "++onCreatePreferences(Bundle, String)");
    addPreferencesFromResource(R.xml.app_preferences);
    setupSourcePreferences();
    setupAppVersionPreference();
  }

  /*
    Private Method(s)
   */
  private void setupAppVersionPreference() {

    Log.d(TAG, "++setupAppVersionPreference()");
    EditTextPreference editTextPreference = findPreference(getString(R.string.pref_key_app_version));
    if (editTextPreference != null) {
      editTextPreference.setSummary(BuildConfig.VERSION_NAME);
    }
  }

  private void setupSourcePreferences() {

    Log.d(TAG, "++setupSourcePreferences()");
    CheckBoxPreference externalCheckBoxPreference = findPreference(getString(R.string.pref_key_is_external));
    if (externalCheckBoxPreference != null) {
      externalCheckBoxPreference.setOnPreferenceChangeListener((preference, newValue) -> {

        PreferenceUtils.saveBooleanPreference(
          getActivity(),
          R.string.pref_key_is_external,
          (boolean) newValue);
        return true;
      });
    }

    CheckBoxPreference internalCheckBoxPreference = findPreference(getString(R.string.pref_key_is_internal));
    if (internalCheckBoxPreference != null) {
      internalCheckBoxPreference.setOnPreferenceChangeListener((preference, newValue) -> {

        PreferenceUtils.saveBooleanPreference(
          getActivity(),
          R.string.pref_key_is_internal,
          (boolean) newValue);
        return true;
      });
    }
  }
}
