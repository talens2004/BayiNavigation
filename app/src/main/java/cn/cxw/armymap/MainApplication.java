package cn.cxw.armymap;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiscCache;
import com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.download.BaseImageDownloader;
import com.nostra13.universalimageloader.utils.StorageUtils;
import com.squareup.otto.Bus;
import com.squareup.otto.ThreadEnforcer;

import java.net.CookieHandler;
import java.net.CookieManager;

import cn.cxw.core.DatabaseHelper;
import cn.cxw.util.PhoneHelper;

public class MainApplication extends Application {

    private static final String TAG = "MainApplication";

	public static Context applicationContext;
    private Bus mBus;
    
    private Handler mHandler = new Handler();
    public static MainApplication INSTANCE;
    public static DatabaseHelper dbHelper;

    // login user name
	public final String PREF_USERNAME = "username";

    /**
     * 应用程序被创建时的入口
     */
    @Override
    public void onCreate() {
        Log.d(TAG, "Application OnCreate....");
        super.onCreate();
        // StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectAll().penaltyDialog().build());
        // FIXME: lib "async-http 1.4.4" don't release the zip resource for handling the gzip stream from server
        // according to below vm check, app will be killed. So disable it now.
        // StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectAll().penaltyLog().penaltyDeath().build());

        applicationContext = this;
        //注册应用程序对象
        INSTANCE = this;
        dbHelper = new DatabaseHelper(this);
        
        //注册事件监听
        mBus = new Bus(ThreadEnforcer.MAIN);
        mBus.register(this);
        
        //初始化Android-Universal-Image-Loader三大组件
        initImageLoader();

        //XXX enable cookie, otherwise HttpUrlConnection doesn't support cookie
        //让网络连接使用Cookie
        CookieManager cookieManager = new CookieManager();
        CookieHandler.setDefault(cookieManager);

        //将设备信息写入配置文件
        registerDevice();
    }

    /**
     * 程序终止之前执行的操作
     * 将环信进程退出
     */
    @Override
    public void onTerminate() {
        Log.d(TAG, "onTerminate()");
        super.onTerminate();
    }

    /**
     * 框架要求
     * @return
     */
    public Bus getBus() {
        return mBus;
    }

    //注册事件，框架使用
    public void registerEvent(Object object) {
        mBus.register(object);
    }

    //撤销事件注册，框架使用
    public void unregisterEvent(Object object) {
        mBus.unregister(object);
    }
    

  
    /**
     * 发布事件
     * @param event
     */
    public void postEvent(final Object event) {
        //NOTE: make sure the event is post in main thread
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                getBus().post(event);
            }
        });
    }

    /**
     * 显示Toast通知
     * @param message
     */
    public void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
    
    /**
     * 显示Toast通知
     * @param messageResouceId
     */
    public void showMessage(int messageResouceId) {
        Toast.makeText(this, getResources().getString(messageResouceId), Toast.LENGTH_SHORT).show();
    }


    /**
     * 注册设备,将设备Id写入SharedPreferences,有什么用???
     */
    private void registerDevice() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        String userId = sp.getString("user_id", "");
        String deviceId = sp.getString("device_id", "");
        
        //如果用户已经登录而且设备Id不存在,则将设备Id写入配置文件
        if (TextUtils.isEmpty(deviceId) && !TextUtils.isEmpty(userId)) {
            deviceId = PhoneHelper.getDeviceId(this);
            Log.d(TAG, "register device "+ deviceId);

            SharedPreferences.Editor editor = sp.edit();
            editor.putString("device_id", deviceId);
            
            /*SharedPreference.Editor的apply和commit方法异同:
             * 1. apply没有返回值而commit返回boolean表明修改是否提交成功 
             * 2. apply是将修改数据原子提交到内存, 而后异步真正提交到硬件磁盘, 而commit是同步的提交到硬件磁盘，
             * 因此，在多个并发的提交commit的时候，他们会等待正在处理的commit保存到磁盘后在操作，从而降低了效率。
             * 而apply只是原子的提交到内容，后面有调用apply的函数的将会直接覆盖前面的内存数据，
             * 这样从一定程度上提高了很多效率。 
             * 3. apply方法不会提示任何失败的提示。 
             * 由于在一个进程中，sharedPreference是单实例，一般不会出现并发冲突，如果对提交的结果不关心的话，建议使用apply，
             * 当然需要确保提交成功且有后续操作的话，还是需要用commit的。
             */
            editor.apply();
        }
    }

    /**
     * 初始化图片下载对象
     * Android-Universal-Image-Loader三大组件DisplayImageOptions、ImageLoaderConfiguration、ImageLoader
     */
    private void initImageLoader() {
    	/*显示图像选项参数设置
    	 *用于指导每一个Imageloader根据网络图片的状态（空白、下载错误、正在下载）显示对应的图片，
    	 *是否将缓存加载到磁盘上，下载完后对图片进行怎么样的处理
    	 */
        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .imageScaleType(ImageScaleType.IN_SAMPLE_INT)
//                .showImageOnLoading(R.drawable.ic_stub)
//                .showImageForEmptyUri(R.drawable.default_avatar)
                .showImageOnFail(R.drawable.default_image)
                .build();
        
        int memoryCacheSize = (int) (Runtime.getRuntime().maxMemory() / 4);
        int threadPoolSize = 2;
        Log.i(TAG, " MAX HEAP SIZE = " + Runtime.getRuntime().maxMemory() / 1024 / 1024 + "m" + " threadpoolsize=" + threadPoolSize);
        
        /*ImageLoaderConfiguration是针对图片缓存的全局配置，
         * 主要有线程类、缓存大小、磁盘大小、图片下载与解析、日志方面的配置
         */
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this)
                .threadPriority(Thread.NORM_PRIORITY + 2)
                .tasksProcessingOrder(QueueProcessingType.FIFO)
                .defaultDisplayImageOptions(defaultOptions)
//                .writeDebugLogs() // Not necessary in common
                .diskCache(new UnlimitedDiscCache(StorageUtils.getIndividualCacheDirectory(this)))
                .denyCacheImageMultipleSizesInMemory()
                .threadPoolSize(threadPoolSize)
                .memoryCache(new LruMemoryCache(memoryCacheSize))
                .imageDownloader(new BaseImageDownloader(this, BaseImageDownloader.DEFAULT_HTTP_CONNECT_TIMEOUT * 4, BaseImageDownloader.DEFAULT_HTTP_READ_TIMEOUT * 2))
                .build();
        
        /*ImageLoader是具体下载图片，缓存图片，显示图片的具体执行类，
         * 它有两个具体的方法displayImage(...)、loadImage(...)，但是其实最终他们的实现都是displayImage(...)
         */
        ImageLoader.getInstance().init(config);
    }
}
