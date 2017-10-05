package cn.cxw.util;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

import cn.cxw.armymap.MainApplication;

public class DbBackupUtil extends AsyncTask<String, Void, Integer> {

    public static final String COMMAND_BACKUP = "backup_database";
    public static final String COMMAND_RESTORE = "restroe_database";
    private static final int BACKUP_SUCCESS = 1;
    public static final int RESTORE_SUCCESS = 2;
    private static final int BACKUP_ERROR = 3;
    public static final int RESTORE_NOFLEERROR = 4;
    private Context context;


    //在mainActivity里实现备份、还原
    //数据恢复
    public static void doDataRestore(Context context, String dbName, String backupDir) {
        new DbBackupUtil(context).execute(COMMAND_RESTORE, dbName, backupDir);
    }

    //数据备份
    public static void doDataBackup(Context context, String dbName, String backupDir) {
        new DbBackupUtil(context).execute(COMMAND_BACKUP, dbName, backupDir);
    }

    public DbBackupUtil(Context context) {
        this.context = context;
    }

    @Override
    protected Integer doInBackground(String... params) {

        // 默认路径是 data/data/honglin.doors/databases/HonglinDoors.db
        File dbFile = context.getDatabasePath(params[1]);
        Log.e("backup", dbFile.getPath());

        // 数据库备份目录
        File backupDir = new File(params[2]);
        Log.e("backup", backupDir.getPath());


        //如果目录不存在则进行创建
        if (!backupDir.exists()) {
            Log.e("backup", "备份路径不存在，创建路径成功");
            backupDir.mkdirs();
        }

//        Process p;
//        int status;
//        try {
//            Log.e("backup", "正在修改备份文件夹目录权限...");
//            p = Runtime.getRuntime().exec("chmod 777 " + backupDir);
//            status = p.waitFor();
//            if(status == 0){
//                Log.e("backup", "文件夹权限设置成功");
//            }else {
//                Log.e("backup", "文件夹权限设置失败");
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

        //创建备份文件
        File backupFile = new File(backupDir, dbFile.getName());
        Log.e("backup", backupFile.getPath());
        String command = params[0];
        if (command.equals(COMMAND_BACKUP)) {
            try {
                backupFile.createNewFile();
                fileCopy(dbFile, backupFile);
                Log.e("backup", "备份成功");
                return BACKUP_SUCCESS;
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("backup", "备份出错");
                return BACKUP_ERROR;
            }
        } else if (command.equals(COMMAND_RESTORE)) {
            try {
                fileCopy(backupFile, dbFile);
                Log.e("backup", "恢复成功");
                return RESTORE_SUCCESS;
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("backup", "恢复出错");
                return RESTORE_NOFLEERROR;
            }
        } else {
            return null;
        }
    }

    private void fileCopy(File dbFile, File backup) throws IOException {
        FileChannel inChannel = new FileInputStream(dbFile).getChannel();
        FileChannel outChannel = new FileOutputStream(backup).getChannel();
        try {
            inChannel.transferTo(0, inChannel.size(), outChannel);
        } catch (IOException e) {
            Log.e("backup", "文件复制出错");
            e.printStackTrace();
        } finally {
            if (inChannel != null) {
                inChannel.close();
            }
            if (outChannel != null) {
                outChannel.close();
            }
        }
    }

    @Override
    protected void onPostExecute(Integer result) {
        super.onPostExecute(result);
        switch (result) {
            case BACKUP_SUCCESS:
                MainApplication.INSTANCE.showMessage("数据备份成功");
                Log.e("backup", "ok");
                break;
            case BACKUP_ERROR:
                MainApplication.INSTANCE.showMessage("数据备份失败");
                Log.e("backup", "fail");
                break;
            case RESTORE_SUCCESS:
                MainApplication.INSTANCE.showMessage("数据恢复成功");
                Log.e("restore", "success");
                break;
            case RESTORE_NOFLEERROR:
                MainApplication.INSTANCE.showMessage("数据恢复失败");
                Log.e("restore", "fail");
                break;
            default:
                break;
        }
    }

}