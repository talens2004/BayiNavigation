package cn.cxw.util;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import cn.cxw.armymap.MainApplication;
import cn.cxw.core.Events;

/**
 * App升级类
 * @author cxw
 *
 */
public class UpgradeHelper {
    private static final String TAG = "migu.upgrade";

    /**
     * 下载App
     * @param context
     * @param appUrl
     * @param appVersion
     */
    public static void downloadApp(final Context context, final String appUrl, final int appVersion) {
        Log.d(TAG, " download app from " + appUrl);
        new Thread() {
            public void run() {
                URL url = null;
                try {
                    url = new URL(appUrl);
                } catch (MalformedURLException e) {
                    throw new RuntimeException(e);
                }
                HttpURLConnection conn = null;
                String errorMessage = "";
                Uri installUri = null;
                try {
                    conn = (HttpURLConnection)url.openConnection();
                    File apkFile = null;
                    if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                        File sdCardDir = Environment.getExternalStorageDirectory();
                        apkFile = new File(sdCardDir, context.getPackageName() + ".apk");
                    } else {
                        throw new RuntimeException("sdcard不可用!!下载失败. 请插入sdcard");
                    }
                    InputStream is = conn.getInputStream();
                    FileOutputStream fileOutputStream = null;
                    if (is != null) {
                        fileOutputStream = new FileOutputStream(apkFile);

                        byte[] b = new byte[1024 * 8];
                        int readBytes = -1;
                        int count = 0;
                        while ((readBytes = is.read(b)) != -1) {
                            fileOutputStream.write(b, 0, readBytes);
                            count += readBytes;
                        }
                        installUri = Uri.fromFile(apkFile);
                    }
                    fileOutputStream.flush();
                    if (fileOutputStream != null) {
                        fileOutputStream.close();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e(TAG, "下载失败", e);
                    errorMessage = "下载失败!!\n原因:" + e.getMessage();
                } finally {
                    MainApplication.INSTANCE.postEvent(new Events.AppDownloadCompleteEvent(installUri, appVersion, errorMessage));
                }
            }
        }.start();
    }
}
