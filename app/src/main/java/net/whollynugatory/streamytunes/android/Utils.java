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
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import net.whollynugatory.streamytunes.android.ui.BaseActivity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class Utils {

  private static final String TAG = BaseActivity.BASE_TAG + Utils.class.getSimpleName();

  public static Bitmap loadImageFromStorage(Context context, long artistId, long albumId) {

    try {
      ContextWrapper cw = new ContextWrapper(context);
      File directory = cw.getDir(context.getString(R.string.album), Context.MODE_PRIVATE);
      File sourcePath = new File(directory, artistId + "-" + albumId + ".jpg");
      return BitmapFactory.decodeStream(new FileInputStream(sourcePath));
    } catch (FileNotFoundException e) {
      Log.e(TAG, "Failed to retrieve image.", e);
    }

    return null;
  }
}
