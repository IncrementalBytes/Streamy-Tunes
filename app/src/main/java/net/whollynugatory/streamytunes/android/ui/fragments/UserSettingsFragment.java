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
import androidx.preference.EditTextPreference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreference;

import net.whollynugatory.streamytunes.android.BuildConfig;
import net.whollynugatory.streamytunes.android.PreferenceUtils;
import net.whollynugatory.streamytunes.android.R;
import net.whollynugatory.streamytunes.android.ui.BaseActivity;

public class UserSettingsFragment extends PreferenceFragmentCompat {

  private static final String TAG = BaseActivity.BASE_TAG + UserSettingsFragment.class.getSimpleName();

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
    setupShowHiddenPreference();
    EditTextPreference editTextPreference = findPreference(getString(R.string.pref_key_app_version));
    if (editTextPreference != null) {
      editTextPreference.setSummary(BuildConfig.VERSION_NAME);
    }
  }

  /*
    Private Method(s)
   */
   private void setupShowHiddenPreference() {

     Log.d(TAG, "++setupShowHiddenPreference()");
     SwitchPreference switchPreference = findPreference(getString(R.string.pref_key_show_hidden));
     if (switchPreference != null) {
       switchPreference.setChecked(PreferenceUtils.getShowHidden(getActivity()));
       switchPreference.setOnPreferenceChangeListener(
         (preference, newValue) -> {

           Log.d(TAG, "++setupShowHiddenPreference::onPreferenceChange()");
           PreferenceUtils.saveBooleanPreference(
             getActivity(),
             R.string.pref_key_show_hidden,
             (boolean) newValue);
           return true;
         });
     }
   }
}
