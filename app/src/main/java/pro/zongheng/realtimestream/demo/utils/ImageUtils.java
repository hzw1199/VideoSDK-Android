package pro.zongheng.realtimestream.demo.utils;

import static com.blankj.utilcode.util.FileUtils.createOrExistsDir;

import android.Manifest;
import android.content.ContentValues;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.Nullable;

import com.blankj.utilcode.util.FileIOUtils;
import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.PermissionUtils;
import com.blankj.utilcode.util.UriUtils;
import com.blankj.utilcode.util.Utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


public final class ImageUtils {

    private ImageUtils() {
        throw new UnsupportedOperationException("u can't instantiate me...");
    }



    @Nullable
    public static File save2Album(final String srcPath,
                                  final String dirName,
                                  final String fileName,
                                  final boolean isVideo) {
        String safeDirName = TextUtils.isEmpty(dirName) ? Utils.getApp().getPackageName() : dirName;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            if (!PermissionUtils.isGranted(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                Log.e("ImageUtils", "save to album need storage permission");
                return null;
            }
            File picDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
            File destFile = new File(picDir, safeDirName + "/" + fileName);
            FileUtils.move(new File(srcPath), destFile);
            FileUtils.notifySystemToScan(destFile);
            return destFile;
        } else {
            ContentValues contentValues = new ContentValues();
            Uri contentUri;
            if (isVideo) {
                if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else {
                    contentUri = MediaStore.Video.Media.INTERNAL_CONTENT_URI;
                }
            } else {
                if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else {
                    contentUri = MediaStore.Images.Media.INTERNAL_CONTENT_URI;
                }
            }
            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, isVideo? "video/*": "image/*");
            contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DCIM + "/" + safeDirName);
            contentValues.put(MediaStore.MediaColumns.IS_PENDING, 1);
            Uri uri = Utils.getApp().getContentResolver().insert(contentUri, contentValues);
            if (uri == null) {
                return null;
            }
            OutputStream os = null;
            try {
                os = Utils.getApp().getContentResolver().openOutputStream(uri);
                int sBufferSize = 524288;
                File srcFile = new File(srcPath);
                InputStream is = new FileInputStream(srcFile);
                byte[] data = new byte[sBufferSize];
                for (int len; (len = is.read(data)) != -1; ) {
                    os.write(data, 0, len);
                }

                contentValues.clear();
                contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0);
                Utils.getApp().getContentResolver().update(uri, contentValues, null, null);

                FileUtils.delete(srcFile);
                return UriUtils.uri2File(uri);
            } catch (Exception e) {
                Utils.getApp().getContentResolver().delete(uri, null, null);
                e.printStackTrace();
                return null;
            } finally {
                try {
                    if (os != null) {
                        os.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Nullable
    public static File save2Album(final InputStream is,
                                  final String dirName,
                                  final String fileName,
                                  final boolean isVideo) {
        String safeDirName = TextUtils.isEmpty(dirName) ? Utils.getApp().getPackageName() : dirName;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            if (!PermissionUtils.isGranted(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                Log.e("ImageUtils", "save to album need storage permission");
                return null;
            }
            File picDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
            File destFile = new File(picDir, safeDirName + "/" + fileName);

            if (!createOrExistsDir(destFile.getParentFile())) return null;
            if (!FileIOUtils.writeFileFromIS(destFile, is)) {
                return null;
            }
            FileUtils.notifySystemToScan(destFile);
            return destFile;
        } else {
            ContentValues contentValues = new ContentValues();
            Uri contentUri;
            if (isVideo) {
                if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else {
                    contentUri = MediaStore.Video.Media.INTERNAL_CONTENT_URI;
                }
            } else {
                if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else {
                    contentUri = MediaStore.Images.Media.INTERNAL_CONTENT_URI;
                }
            }
            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, isVideo? "video/*": "image/*");
            contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DCIM + "/" + safeDirName);
            contentValues.put(MediaStore.MediaColumns.IS_PENDING, 1);
            Uri uri = Utils.getApp().getContentResolver().insert(contentUri, contentValues);
            if (uri == null) {
                return null;
            }
            OutputStream os = null;
            try {
                os = Utils.getApp().getContentResolver().openOutputStream(uri);
                int sBufferSize = 524288;
                byte[] data = new byte[sBufferSize];
                for (int len; (len = is.read(data)) != -1; ) {
                    os.write(data, 0, len);
                }

                contentValues.clear();
                contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0);
                Utils.getApp().getContentResolver().update(uri, contentValues, null, null);
                return UriUtils.uri2File(uri);
            } catch (Exception e) {
                Utils.getApp().getContentResolver().delete(uri, null, null);
                e.printStackTrace();
                return null;
            } finally {
                try {
                    if (os != null) {
                        os.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void saveBitmap(Bitmap bitmap) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                InputStream inputStream = new ByteArrayInputStream(baos.toByteArray());
                ImageUtils.save2Album(
                        inputStream,
                        "rts-library",
                        System.currentTimeMillis() + ".jpg",
                        false
                );
            }
        }).start();
    }


}
