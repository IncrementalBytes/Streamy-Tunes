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
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Size;

import net.whollynugatory.streamytunes.android.ui.BaseActivity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

public class Utils {

  private static final String TAG = BaseActivity.BASE_TAG + Utils.class.getSimpleName();

  public static Bitmap loadImageFromStorage(Context context, long artistId, long albumId) {

    ContextWrapper cw = new ContextWrapper(context);
    File directory = cw.getDir(context.getString(R.string.album), Context.MODE_PRIVATE);
    File sourcePath = new File(directory, artistId + "-" + albumId + ".jpg");
    return loadImageFromStorage(sourcePath);
  }

  public static Bitmap loadImageFromStorage(File sourcePath) {

    try {
      return BitmapFactory.decodeStream(new FileInputStream(sourcePath));
    } catch (FileNotFoundException e) {
      Log.e(TAG, "Failed to retrieve image.", e);
    }

    return null;
  }

  public static File saveImageToStorage(Context context, long artistId, long albumId, String mediaId) {

    File directory = context.getDir(context.getString(R.string.album), Context.MODE_PRIVATE);
    File destinationPath = new File(directory, artistId + "-" + albumId + ".jpg");
    try (FileOutputStream fos = new FileOutputStream(destinationPath)) {
      Uri localSource = Uri.withAppendedPath(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, "" + mediaId);
      context.getContentResolver().loadThumbnail(
        localSource,
        new Size(640, 480), null)
        .compress(Bitmap.CompressFormat.PNG, 100, fos);
      return destinationPath;
    } catch (Exception e) {
      Log.w(TAG, "Failed to save image: " + mediaId);
    }

    return null;
  }
}
