package cn.cxw.util;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Vibrator;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import java.util.List;
import java.util.Locale;
import java.util.Random;

import android.os.Handler;
/**
 * 设备相关类
 * @author cxw
 *
 */
public class PhoneHelper {
	
	public static final String IMEI = "imei";
	public static final String IMSI = "imsi";
	//fix the history bug
	public static final String SERIALNUM = "serinum";
    public static final String PLUS86 = "+86";

    /**
     * 获取手机号码
     * @param context
     * @return
     */
	public static String getPhoneNumber(Context context){   
		if(NetWorkHelper.getSimState(context) == NetWorkHelper.SIM_OK){
			TelephonyManager mTelephonyMgr;   
		    mTelephonyMgr = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);    
		    String myPhoneNumber=mTelephonyMgr.getLine1Number();
		    if(myPhoneNumber != null && myPhoneNumber.startsWith(PLUS86)){
		    	myPhoneNumber = myPhoneNumber.substring(PLUS86.length());
		    }
		    return myPhoneNumber;
		}else{
			return null;
		}
	}   
	
	/**
	 * 生成IMSI???
	 * @return
	 */
	public static String generateImei(){
    	StringBuffer imei = new StringBuffer();
    	
    	long time = System.currentTimeMillis();
    	String currentTime = Long.toString(time);
    	imei.append(currentTime.substring(currentTime.length()-5));
    	
    	StringBuffer model = new StringBuffer();
    	model.append(Build.MODEL.replaceAll(" ", ""));
    	while(model.length()<6){
    		model.append('0');
    	}
    	imei.append(model.substring(0, 6));
    	
    	Random random = new Random(time);
    	long tmp = 0;
    	while(tmp<0x1000){
    		tmp = random.nextLong();
    	}
    	imei.append(Long.toHexString(tmp).substring(0,4));
    	return imei.toString();
    }
	
	/**
	 * 取得IMEI
	 * @param context
	 * @return
	 */
	public static String getImei(Context context){
    	String imei = null;
    	SharedPreferences sp = context.getSharedPreferences(IMEI,Context.MODE_PRIVATE);
    	imei = sp.getString(IMEI, null);
    	if(imei == null || imei.length() == 0){
    		TelephonyManager tm = (TelephonyManager) context.getSystemService(Activity.TELEPHONY_SERVICE);
    		imei = tm.getDeviceId();
        	if(imei == null || imei.length() == 0){
        		imei="";
        	}
        	Editor editor = sp.edit();
    		editor.putString(IMEI, imei);
    		editor.commit();
    	}
	    return imei.trim();
    }
	
	/**
	 * 获得设备序列号
	 * @param context
	 * @return
	 */
	public static String getSeriNum(Context context){
    	String seriNum = null;
    	SharedPreferences sp = context.getSharedPreferences(SERIALNUM,Context.MODE_PRIVATE);
    	seriNum = sp.getString(SERIALNUM, null);
    	if(seriNum == null || seriNum.length() == 0){
    		TelephonyManager tm = (TelephonyManager) context.getSystemService(Activity.TELEPHONY_SERVICE);
    		seriNum = tm.getSimSerialNumber();
        	if(seriNum == null || seriNum.length() == 0){
        		seriNum="";
        	}
          	Editor editor = sp.edit();
    		editor.putString(SERIALNUM, seriNum);
    		editor.commit();
    	}
	    return seriNum.trim();
    }
    
	/**
	 * 获得IMSI
	 * @param context
	 * @return
	 */
    public static String getImsi(Context context){
    	String imsi = null;
    	SharedPreferences sp = context.getSharedPreferences(IMEI,Context.MODE_PRIVATE);
    	imsi = sp.getString(IMSI, null);
    	if(imsi == null || imsi.length() == 0){
    		TelephonyManager tm = (TelephonyManager) context.getSystemService(Activity.TELEPHONY_SERVICE);
        	imsi = tm.getSubscriberId();
        	if(imsi == null || imsi.length() == 0){
        		imsi = generateImei();
        	}
        	Editor editor = sp.edit();
			editor.putString(IMSI, imsi);
			editor.commit();
    	}
	    return imsi;
    }
    
    /**
     * 获得设备语言,目前仅支持中文与英文
     * @return
     */
    public static String getDeviceLanguage() {
        String Language = "zh_CN";
        if (Locale.getDefault().getLanguage().equals("en")) {
            Language = "en_US";
        }
        return Language;
    }
    
    /**
     * 判断某个服务是否处于运行状态
     * @param mContext
     * @param className
     * @return
     */
    public static boolean isServiceRunning(Context mContext,String className) {
        boolean isRunning = false;
        ActivityManager activityManager = (ActivityManager)
        mContext.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> serviceList = activityManager.getRunningServices(30);
        if (!(serviceList.size()>0)) {
            return false;
        }
        for (int i = 0; i < serviceList.size(); i++) {
            if (serviceList.get(i).service.getClassName().equals(className) == true) {
                isRunning = true;
                break;
            }
        }
        return isRunning;
    }

    /**
     * 获得App版本号
     * @param context
     * @return
     */
    public static int getLocalVersionCode(Context context) {
        int versionCode;
        try {
            // 获取软件版本号，对应AndroidManifest.xml下android:versionCode
            versionCode = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return versionCode;
    }

    /**
     * 获得App版本名称
     * @param context
     * @return
     */
    public static String getLocalVersionName(Context context) {
        String versionName;
        try {
            // 获取软件版本名称，对应AndroidManifest.xml下android:versionName
            versionName = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return versionName;
    }
    
    /**
     * 获得App版本名称
     * @param context
     * @return
     */
    public static int getAndroidOSVersion() {
//    	android.os.Build.VERSION.RELEASE获取版本号
//      android.os.Build.MODEL 获取手机型号
        return android.os.Build.VERSION.SDK_INT;
    }

    /**
     * 得到设备ID
     * @param context
     * @return
     */
    public static String getDeviceId(Context context) {
        final TelephonyManager tm = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
        String deviceId = tm.getDeviceId();

        if (TextUtils.isEmpty(deviceId) || deviceId.startsWith("00000000")) {
            deviceId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        }
        return deviceId;
    }

    /**
     * 自定义振动模式
     * @return
     */
    public static long [] getVibratorPattern(){
    	// 振动模式，震动1秒停止1秒
        long [] pattern = new long[]{1000, 1000, 1000, 1000, 1000};
        return pattern;
    }

    /**
     * 按设置的振动模式振动
     * @param context
     */
    public static void vibrate(Context context){
        Vibrator vibrator = (Vibrator)context.getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(getVibratorPattern(), -1);
    }

    /**
     * 指定seconds秒后播放手机铃声
     * @param context
     * @param seconds
     */
    public static void playRingtone(Context context, int seconds) {
    	// 获取默认手机铃声URI
        Uri notificationUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        // 播放默认手机铃声
        final Ringtone r = RingtoneManager.getRingtone(context, notificationUri);
        r.play();

        /**
         * 播放seconds指定的秒数后停止
         */
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                r.stop();
            }
        }, seconds * 1000);
    }

}
