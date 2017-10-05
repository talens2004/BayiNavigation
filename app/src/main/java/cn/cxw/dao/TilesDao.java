package cn.cxw.dao;

import android.app.Activity;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.File;
import java.io.InputStream;
import java.nio.ByteBuffer;

import cn.cxw.core.Constants;

/**
 * Created by CXW-IBM on 2017/9/3.
 */

public class TilesDao {

    /**
     * 瓦片文件解析为Bitmap
     *
     * @param fileName
     * @return 瓦片文件的Bitmap
     */
    public static Bitmap getFromAssets(Activity activity, String fileName) {
        AssetManager am = activity.getAssets();
        InputStream is = null;
        Bitmap bm;

        try {
            is = am.open(fileName);
            bm = BitmapFactory.decodeStream(is);
            return bm;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    public static Bitmap getDiskBitmap(String pathString) {
        Bitmap bitmap = null;
        try {
            pathString = Constants.sdCardTileDirName + "/" + pathString;
//            Log.d(TAG, pathString);
            File file = new File(pathString);
            if (file.exists()) {
                bitmap = BitmapFactory.decodeFile(pathString);
            }
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
        return bitmap;
    }


    /**
     * 解析Bitmap
     *
     * @param bitmap
     * @return
     */
    public static byte[] toRawData(Bitmap bitmap) {
        ByteBuffer buffer = ByteBuffer.allocate(bitmap.getWidth() * bitmap.getHeight() * 4);
        bitmap.copyPixelsToBuffer(buffer);
        byte[] data = buffer.array();
        buffer.clear();
        return data;
    }

}
