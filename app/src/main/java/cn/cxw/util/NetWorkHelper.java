package cn.cxw.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;

/**
 * 网络连接工具类
 * @author cxw
 *
 */
public class NetWorkHelper {
    private static final String TAG = "ichange.NetWorkHelper";

	public static final int CHINA_MOBILE = 1;
	public static final int CHINA_UNICOM = 2;
	public static final int CHINA_TELECOM = 3;

	public static final int SIM_OK = 0;
	public static final int SIM_NO = -1;
	public static final int SIM_UNKNOW = -2;
	
	/**
	 * 判断网络是否可用
	 * @param context
	 * @return
	 */
	public static boolean isNetworkAvailable(Context context)
    {   
    	boolean result = false;
    	if(GetNetworkType(context) != null)
    		result = true;
       
		return result;
    }

	/**
	 * 判断网络连接是否正常
	 * @param context
	 * @return
	 */
    public static boolean isNetworkConnected(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context
                        .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = mConnectivityManager.getActiveNetworkInfo();
            if (networkInfo != null) {
                return networkInfo.isAvailable();
            }
         }
        return false;
    }

    /**
     * 得到网络连接类型
     * @param activity
     * @return
     */
    public static String GetNetworkType(Context activity){
		String result = null;
		ConnectivityManager connectivity = (ConnectivityManager) (activity.getSystemService(Context.CONNECTIVITY_SERVICE));
		if (connectivity == null) {
			result = null;
		}else{
			//获得手机所有的网络连接信息，然后进行遍历
			NetworkInfo[] info = connectivity.getAllNetworkInfo();
			if (info != null) {
				for (int i = 0; i < info.length; i++) {
					//如果某一网络信息不为空，则取得其状态
					if(info[i] != null){
						NetworkInfo.State tem  = info[i].getState();
						//如果网络状态为已连接或正在连接
						if ((tem == NetworkInfo.State.CONNECTED || tem == NetworkInfo.State.CONNECTING)) {
							//得到此连接额外的信息
							String temp = info[i].getExtraInfo();
							result = info[i].getTypeName() + " "
									+ info[i].getSubtypeName() + temp;
							break;
						}
					}
				}
			}
		}
		return result;
	}

    /**
     * 判断WIFI是否可用
     * @param context
     * @return
     */
    public static boolean isWifiAvailable(Context context) {
        ConnectivityManager connectivity = (ConnectivityManager) (context.getSystemService(Context.CONNECTIVITY_SERVICE));
        Boolean isAvailable = false;
        if (connectivity != null) {

            NetworkInfo info = connectivity.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            if (info != null) {
                NetworkInfo.State state = info.getState();
                isAvailable = (state == NetworkInfo.State.CONNECTED);
            }
        }
        return isAvailable;
    }
    
    /**
     * 获取Sim卡状态
     * @param context
     * @return
     */
    public static int getSimState(Context context){
    	TelephonyManager telMgr = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
    	int simState = telMgr.getSimState();
        if(simState ==TelephonyManager.SIM_STATE_READY){
           return SIM_OK;
        }
        else if(simState==TelephonyManager.SIM_STATE_ABSENT){
           return SIM_NO;
        }
        else{
           return SIM_UNKNOW;
        }
    }
    
    /**
     * 监听网络状态的广播接收者，在应用程序启动后注册，应用程序销毁时取消注册
     */
    public static BroadcastReceiver connectionReceiver = new BroadcastReceiver() {
    	@Override
    	public void onReceive(Context context, Intent intent) {
	    	ConnectivityManager connectMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
	    	NetworkInfo mobNetInfo = connectMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
	    	NetworkInfo wifiNetInfo = connectMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
//	    	if (!mobNetInfo.isConnected() && !wifiNetInfo.isConnected()) {
//		    	Log.d(TAG, "unconnect");
//		    	// unconnect network
//	    	}else {
//	    		// connect network
//	    	}
    	}
    };
    
    /**
     * 注册网络状态广播接收者
     * @param context
     */
    public static void registerNetworkReceiver(Context context){
    	IntentFilter intentFilter = new IntentFilter();
    	intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
    	context.registerReceiver(connectionReceiver, intentFilter); 
    }
    
    /**
     * 反注册网络状态广播接收者
     * @param context
     */
    public static void unregisterNetworkReceiver(Context context){
    	if (connectionReceiver != null) 
    		context.unregisterReceiver(connectionReceiver); 
    }
}
