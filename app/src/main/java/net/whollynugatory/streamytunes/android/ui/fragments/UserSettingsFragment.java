package net.whollynugatory.streamytunes.android.ui.fragments;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.preference.EditTextPreference;
import androidx.preference.PreferenceFragmentCompat;

import net.whollynugatory.streamytunes.android.BuildConfig;
import net.whollynugatory.streamytunes.android.R;
import net.whollynugatory.streamytunes.android.Utils;

public class UserSettingsFragment extends PreferenceFragmentCompat {

  private static final String TAG = Utils.BASE_TAG + "UserSettingsFragment";

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
}
