package cn.cxw.util;

import android.graphics.Bitmap;
import android.os.Environment;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * 文件处理工具类
 *
 * @author yangkang
 */
public class FileHelper {

    /**
     * 根据位图对象创建新的文件
     *
     * @param file
     * @param bmp
     * @return
     */
    public static File CreateNewFile(File file, Bitmap bmp) {
        FileOutputStream fos = null;
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
            fos = new FileOutputStream(file, false);
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            return file;
        } catch (Exception e) {
            e.printStackTrace();
            if (file != null && file.exists()) {
                file.delete();
            }
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return file;
    }

    /**
     * 根据二进制数据创建新文件
     *
     * @param file
     * @param data
     * @return
     */
    public static File CreateNewFile(File file, byte[] data) {
        FileOutputStream fos = null;
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
            fos = new FileOutputStream(file);
            fos.write(data, 0, data.length);
            fos.flush();
            return file;
        } catch (Exception e) {
            e.printStackTrace();
            if (file != null && file.exists()) {
                file.delete();
            }
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return file;
    }

    /**
     * 根据文件目录,文件路径,文件数据创建文件
     *
     * @param dir
     * @param filePath
     * @param data
     * @return
     */
    public static File CreateNewFile(File dir, String filePath, byte[] data) {
        return CreateNewFile(new File(dir, filePath), data);

    }

    /**
     * 根据文件路径和字节数据创建文件
     *
     * @param filePath
     * @param data
     * @return
     */
    public static File CreateNewFile(String filePath, byte[] data) {
        return CreateNewFile(new File(Environment.getExternalStorageDirectory(), filePath), data);
    }

    /**
     * 得到文件大小
     *
     * @param file
     * @return
     */
    public static int getFileSize(File file) {
        FileInputStream fis = null;
        try {
            if (!file.isDirectory()) {
                fis = new FileInputStream(file);
                return fis.available();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return 0;
    }

    /**
     * 根据文件和输入流创建文件
     *
     * @param file
     * @param inputStream
     * @return
     */
    public static File CreateNewFile(File file, InputStream inputStream) {
        FileOutputStream fos = null;
        DataInputStream dis = null;
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
            fos = new FileOutputStream(file);
            dis = new DataInputStream(inputStream);
            int i = 0;
            byte[] b = new byte[2048];
            while ((i = dis.read(b, 0, 2048)) != -1) {
                fos.write(b, 0, i);
            }
            fos.flush();
            return file;
        } catch (Exception e) {
            e.printStackTrace();
            if (file != null && file.exists()) {
                file.delete();
            }
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
                if (dis != null) {
                    dis.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return file;
    }
}
