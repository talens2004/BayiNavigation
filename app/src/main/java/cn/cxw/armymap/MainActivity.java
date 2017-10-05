package cn.cxw.armymap;

import android.graphics.Bitmap;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ScrollView;
import android.widget.TextView;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.Poi;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.FileTileProvider;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationConfiguration.LocationMode;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.Tile;
import com.baidu.mapapi.map.TileOverlay;
import com.baidu.mapapi.map.TileOverlayOptions;
import com.baidu.mapapi.map.TileProvider;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.model.LatLngBounds;
import com.squareup.otto.Subscribe;

import java.util.List;

import cn.cxw.armymap.dialogs.Dialogs;
import cn.cxw.armymap.subpages.HistoryTrailsFragment;
import cn.cxw.armymap.subpages.LiberaryPointsFragment;
import cn.cxw.baidu.MapViewHelper;
import cn.cxw.baidu.Markers;
import cn.cxw.baidu.Paint;
import cn.cxw.core.Constants;
import cn.cxw.core.DatabaseHelper;
import cn.cxw.core.Events;
import cn.cxw.dao.TilesDao;
import cn.cxw.dao.TrailsDao;
import cn.cxw.model.GaussPoint;
import cn.cxw.model.Point;
import cn.cxw.model.TrailItems;
import cn.cxw.model.Trails;
import cn.cxw.util.CoordinateUtil;
import cn.cxw.util.DateHelper;
import cn.cxw.util.DbBackupUtil;
import cn.cxw.util.UiHelper;
import cn.cxw.views.BaseActivity;

import static cn.cxw.baidu.Markers.addMarker;


public class MainActivity extends BaseActivity implements BaiduMap.OnMapLoadedCallback, SensorEventListener {

    //坐标显示
    private TextView gaussText;
    private ImageView settingIconRunning;
    private ImageView settingIconStoping;
    private ImageView switchIcon;
    private TextView localStatusText;
    private View popManuView;
    private View trailSwitch;
    private View historyLook;
    private View requestLocButton;
    private View clearTrail;
    private View bottomBar;
    private View searchPoint;
    private View currentPointLove;

    private ScrollView scrollView;
    private View switchPaintPanel;
    private boolean isPainting = false;
    //标图面板
    private Paint paint;

    //弹出菜单
    PopupMenu popupMenu;
    Menu menu;

    //地图控件
    private MapView mMapView;
    //地图
    private BaiduMap mBaiduMap;

    //设置瓦片图的在线缓存大小，默认为20 M
    private static final int TILE_TMP = 20 * 1024 * 1024;
    //定位时间间隔，单位秒
    private static int scanSpan = 1;

    TileProvider tileProvider;
    TileOverlay tileOverlay;
    Tile offlineTile;
    MapStatusUpdate mMapStatusUpdate;

    private Trails currentTrail;
    private TrailItems currentTrailItem;

    //地图加载完成标志
    private boolean mapLoaded = false;


    //定位相关属性
    LocationClient mLocClient;
    public MyLocationListenner myListener = new MyLocationListenner();
    private LocationMode mCurrentMode;
    BitmapDescriptor mCurrentMarker;
    private static final int accuracyCircleFillColor = 0x00FFFF88;
    private static final int accuracyCircleStrokeColor = 0x0000FF00;
    private SensorManager mSensorManager;
    private Double lastX = 0.0;
    private float mCurrentDirection = 0;
    private double curLatitude = 0.0;
    private double curLongitude = 0.0;
    private float curAccracy;

    boolean isFirstLoc = true; // 是否首次定位
    private MyLocationData locData;
    private float direction;
    private Marker marker;
    private Marker markerStart;
    private Button startNewTrail;


    //轨迹记录
    public boolean isTrailingOpen = false;
    //上一次定位点坐标
    public LatLng lastPoint = new LatLng(0.0, 0.0);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //在使用SDK各组件之前初始化context信息，传入ApplicationContext
        //注意该方法要再setContentView方法之前实现
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_main);
        //保持屏幕常亮
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
//        Constants.getRect();

        Log.d(TAG, "start---------------------------------");


        //找到所有控件
        findViews();
        //设置弹出菜单
        setupMenu();
        //设置地图控件
        setupMapView();
        //设置定位按钮
        setupLocalButton();
        //加载标绘面板
        paint = new Paint(mBaiduMap, this);
    }

    //Marker点击时弹出菜单
    private void popMarkerMenu(final Marker marker) {
        PopupMenu markerMenu = new PopupMenu(this, findViewById(R.id.popup_menu));
        Menu menu = markerMenu.getMenu();

        // 通过XML文件添加菜单项
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.marker_click_menu, menu);

        // 监听事件
        markerMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.marker_love:
                        Markers.clickMarker(MainActivity.this, marker);
                        break;
                    case R.id.marker_remove:
                        marker.remove();
                        break;
                    default:
                        break;
                }
                return false;
            }
        });
        markerMenu.show();
    }

    //主菜单
    private void setupMenu() {
        popupMenu = new PopupMenu(this, findViewById(R.id.popup_menu));
        menu = popupMenu.getMenu();

        // 通过XML文件添加菜单项
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main_menu, menu);


        // 监听事件
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    //新建标图
                    case R.id.new_plan:
                        paint.createNewPlan();
                        ;
                        break;
                    //新建道路
                    case R.id.new_road:
                        newTrail(true);
                        ;
                        break;
                    //收藏的点
                    case R.id.liberary:
                        UiHelper.showFragment(MainActivity.this.getSupportFragmentManager(), null, new LiberaryPointsFragment());
                        break;
                    //历史轨迹
                    case R.id.history:
                        UiHelper.showFragment(MainActivity.this.getSupportFragmentManager(), null, new HistoryTrailsFragment());
                        break;
                    //箭头选项
                    case R.id.arrow:
                        Dialogs.showArraySelectDialog(MainActivity.this);
                        break;
                    //数据备份
                    case R.id.db_backup:
                        DbBackupUtil.doDataBackup(MainActivity.this, DatabaseHelper.DB_NAME, Constants.sdCardDbBackupDirName);
                        break;
                    //数据恢复
                    case R.id.db_restore:
                        DbBackupUtil.doDataRestore(MainActivity.this, DatabaseHelper.DB_NAME, Constants.sdCardDbBackupDirName);
                        break;
                    default:
                        break;
                }
                return false;
            }
        });
    }

    private void findViews() {
        //获取地图控件引用
        mMapView = (MapView) findViewById(R.id.bmapView);
        //坐标显示标签
        gaussText = (TextView) findViewById(R.id.gauss);
        //定位模式按钮
        requestLocButton = findViewById(R.id.local_model);
        //定位与停止指示图标
        settingIconRunning = (ImageView) findViewById(R.id.setting_icon);
        //停止定位按钮
        settingIconStoping = (ImageView) findViewById(R.id.setting_stop);
        //定位模式文本
        localStatusText = (TextView) findViewById(R.id.local_status);
        //回家按钮
        popManuView = findViewById(R.id.popup_menu);
        //轨迹开关
        trailSwitch = findViewById(R.id.switch_trail);
        //开关图标
        switchIcon = (ImageView) findViewById(R.id.switch_icon);
        //查看轨迹历史
        searchPoint = findViewById(R.id.local_search);
        //清除轨迹按钮
        clearTrail = findViewById(R.id.clear_trail);
        //控制栏
        bottomBar = findViewById(R.id.bottom_bar);
        //开始记录按钮
        startNewTrail = (Button) findViewById(R.id.btn_start);
        //收藏当前点按钮
        currentPointLove = findViewById(R.id.local_love);

        //标绘面板
        scrollView = (ScrollView) findViewById(R.id.paint_panel);
        //标绘开关
        switchPaintPanel = findViewById(R.id.switch_panel);

    }


    private void setupMapView() {

        //隐藏缩放控件
        mMapView.showZoomControls(false);
        //移除百度图标
        mMapView.removeViewAt(1);

        //得到地图
        mBaiduMap = mMapView.getMap();
        //地图加载完毕执行
        mBaiduMap.setOnMapLoadedCallback(this);
        //设置缩放级别
        mBaiduMap.setMaxAndMinZoomLevel(Constants.MAX_LEVEL, Constants.MIN_LEVEL);

        //显示地名
        mBaiduMap.showMapPoi(true);

        //得到地图状态，初始位置为天安门
        MapStatus.Builder builder = new MapStatus.Builder().zoom(Constants.DEFAULT_LEVEL).target(Constants.chengde);

        //设置更新显示位置
        mMapStatusUpdate = MapStatusUpdateFactory.newMapStatus(builder.build());

        //更新显示位置
        mBaiduMap.setMapStatus(mMapStatusUpdate);

        mBaiduMap.setCompassEnable(true);

        // 修改为自定义marker
        mCurrentMarker = BitmapDescriptorFactory.fromResource(R.mipmap.icon_geo_default_small);
        mBaiduMap.setMyLocationConfiguration(new MyLocationConfiguration(
                mCurrentMode, true, mCurrentMarker,
                accuracyCircleFillColor, accuracyCircleStrokeColor));

        //默认离线地图显示
        offlineTile();

        mBaiduMap.setOnMapClickListener(new BaiduMap.OnMapClickListener() {
            /**
             * 地图单击事件回调函数
             * @param point 点击的地理坐标
             */
            public void onMapClick(LatLng point) {
                LatLng gps = CoordinateUtil.BDToGPS(point.latitude, point.longitude);
                GaussPoint gauss = CoordinateUtil.GPSToGauss(gps.latitude, gps.longitude);
                gaussText.setText("点击位置 " + gauss.toString());

                //如果是标绘状态
                if (isPainting) {
                    paint.addSymbol(point);
                } else {
                    //添加定位图标
                    marker = addMarker(mBaiduMap, point);
                }
            }

            /**
             * 地图内 Poi 单击事件回调函数
             * @param poi 点击的 poi 信息
             */
            public boolean onMapPoiClick(MapPoi poi) {
                return false;
            }
        });


        //收藏当前位置按钮
        currentPointLove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LatLng point = new LatLng(curLatitude, curLongitude);
                Marker marker = addMarker(mBaiduMap, point);
                Markers.clickMarker(MainActivity.this, marker);
            }
        });

        //调用BaiduMap对象的setOnMarkerDragListener方法设置marker拖拽的监听
        mBaiduMap.setOnMarkerDragListener(new BaiduMap.OnMarkerDragListener() {
            public void onMarkerDrag(Marker marker) {
                //拖拽中
            }

            public void onMarkerDragEnd(Marker marker) {
                //拖拽结束
            }

            public void onMarkerDragStart(Marker marker) {
                //开始拖拽
            }
        });

        //点击Marker事件
        mBaiduMap.setOnMarkerClickListener(new BaiduMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
//                Markers.clickMarker(MainActivity.this, marker);
                popMarkerMenu(marker);
                return false;
            }
        });

        //地图状态改变相关接口
//        mBaiduMap.setOnMapStatusChangeListener(new BaiduMap.OnMapStatusChangeListener() {
//
//            @Override
//            public void onMapStatusChangeStart(MapStatus mapStatus) {
//                // 手势操作地图，设置地图状态等操作导致地图状态开始改变。
//            }
//
//            @Override
//            public void onMapStatusChangeStart(MapStatus mapStatus, int i) {
//
//            }
//
//            @Override
//            public void onMapStatusChange(MapStatus mapStatus) {
//
//            }
//
//            // 地图状态改变结束
//            @Override
//            public void onMapStatusChangeFinish(MapStatus mapStatus) {
//                //target地图操作的中心点。
//                //LatLng centerBL = mapStatus.target;
//            }
//
//        });


        //开始定位
        settingIconRunning.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reStartLocClient(scanSpan);
            }
        });

        //停止定位
        settingIconStoping.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //如果正在录制轨迹，不允许停止定位
                if (isTrailingOpen) {
                    mApp.showMessage("正在记录轨迹，如需停止定位，请先保存并停止轨迹记录");
                    return;
                }

                stopLocClient();
                settingIconRunning.setSelected(false);
                settingIconStoping.setSelected(true);
            }
        });

        //菜单按钮点击事件
        popManuView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupMenu.show();
            }
        });

        //轨迹记录开关
        trailSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isTrailingOpen) {
                    Dialogs.showSaveTrailDialog(MainActivity.this, currentTrail.isRoad, currentTrail.title);
                } else {
                    //新建临时轨迹
                    newTrail(false);
                }
            }
        });

        //正式启动记录轨迹
        startNewTrail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startNewTrail.setVisibility(View.INVISIBLE);

                //将上一点设置在起点标签位置
                lastPoint = markerStart.getPosition();
                //开始轨迹记录
                switchTrail(true);
                currentTrail.id = TrailsDao.saveTrail(currentTrail);

                //构建轨迹起始点
                currentTrailItem = addTrailItem(currentTrail.id);
                currentTrail.startItemId = currentTrailItem.id;
                currentTrail.title = (currentTrail.isRoad ? "新道路" : "新轨迹_") + DateHelper.convertTime2ShortDate(currentTrailItem.time);
                TrailsDao.updateTrail(currentTrail);

                mApp.showMessage("从当前位置开始记录轨迹");
            }
        });

        //清除所有图层
        clearTrail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //清除所有图层
                mBaiduMap.clear();
                //显示记录的永久道路
                Markers.showAllRoads(mBaiduMap);
                //如果正在记录轨迹，则将当前轨迹进行重绘
                if (isTrailingOpen && !currentTrail.isRoad) {
                    Markers.showTrail(mBaiduMap, currentTrail);
                }
            }
        });

        searchPoint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dialogs.showSearchPointDialog(MainActivity.this);
            }
        });

        //打开关闭标图
        switchPaintPanel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPainting) {
                    scrollView.setVisibility(View.GONE);
                    switchPaintPanel.setSelected(false);
                    isPainting = false;
                } else {
                    scrollView.setVisibility(View.VISIBLE);
                    switchPaintPanel.setSelected(true);
                    isPainting = true;
                }
            }
        });
    }

    // 新建轨迹
    private void newTrail(boolean isRoad) {
        if (isTrailingOpen) {
            mApp.showMessage("正在进行轨迹录制，请先保存并停止当前录制");
            return;
        }

        if (scanSpan == 0) {
            mApp.showMessage("定位未开启，请先开启定位功能");
            return;
        }

        //如果首次定位未成功，说明GPS没有信号，禁止开启轨迹
        if (isFirstLoc) {
            mApp.showMessage("GPS信号弱，请确认GPS信号后再试");
            return;
        }

        //得到上一点坐标
        lastPoint = new LatLng(curLatitude, curLongitude);
        if (!Constants.isPointEffect(lastPoint)) {
            mApp.showMessage("GPS信号弱，请确认GPS信号后再试");
            return;
        }

        //如果起点已经存在则重新设置起点
        if (markerStart != null) {
            markerStart.remove();
        }
        markerStart = Markers.addMarker(mBaiduMap, lastPoint, R.mipmap.icon_st);

        //-----------------------------------------------------------
//        mApp.showMessage("长按起点位置标签可拖动对起点手工进行更改");
//        startNewTrail.setVisibility(View.VISIBLE);
        //-----------------------------------------------------------

        //构建轨迹对象
        currentTrail = new Trails();
        currentTrail.isRoad = isRoad;


        //将上一点设置在起点标签位置
        lastPoint = markerStart.getPosition();
        //开始轨迹记录
        switchTrail(true);
        currentTrail.id = TrailsDao.saveTrail(currentTrail);

        //构建轨迹起始点
        currentTrailItem = addTrailItem(currentTrail.id);
        currentTrail.startItemId = currentTrailItem.id;
        currentTrail.title = (currentTrail.isRoad ? "新道路" : "新轨迹_") + DateHelper.convertTime2ShortDate(currentTrailItem.time);
        TrailsDao.updateTrail(currentTrail);

        mApp.showMessage("从当前位置开始记录轨迹");
    }

    /**
     * 瓦片图的离线添加
     */
    private void offlineTile() {
        //如果已存在离线图层则先移除
        if (tileOverlay != null && mBaiduMap != null) {
            tileOverlay.removeTileOverlay();
        }

        /**
         * 定义瓦片图的离线Provider，并实现相关接口
         * MAX_LEVEL、MIN_LEVEL 表示地图显示瓦片图的最大、最小级别
         * Tile 对象表示地图每个x、y、z状态下的瓦片对象
         */
        tileProvider = new FileTileProvider() {
            @Override
            public Tile getTile(int x, int y, int z) {
                // 根据地图某一状态下x、y、z加载指定的瓦片图
                String filedir = String.format("/%3$d/%1$d/%3$d_%1$d_%2$d.png", x, y, z); // "/" + z + "/" + x + "/" + z + "_" + x + "_" + y + ".png";
                Bitmap bm = TilesDao.getDiskBitmap(filedir);
                if (bm == null) {
                    return null;
                }
                // 瓦片图尺寸必须满足256 * 256
                offlineTile = new Tile(bm.getWidth(), bm.getHeight(), TilesDao.toRawData(bm));
                bm.recycle();
                return offlineTile;
            }

            @Override
            public int getMaxDisLevel() {
                return (int) Constants.MAX_LEVEL;
            }

            @Override
            public int getMinDisLevel() {
                return (int) Constants.MIN_LEVEL;
            }

        };
        TileOverlayOptions options = new TileOverlayOptions();
        // 设置离线瓦片图属性option
        options.tileProvider(tileProvider).setPositionFromBounds(new LatLngBounds.Builder().include(Constants.northeast).include(Constants.southwest).build());
        // 通过option指定相关属性，向地图添加离线瓦片图对象
        tileOverlay = mBaiduMap.addTileLayer(options);
    }


    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理
        mMapView.onPause();
    }


    @Override
    public void onMapLoaded() {
        mapLoaded = true;

        //设置比例尺控件位置
        android.graphics.Point point = new android.graphics.Point();
        point.x = 40;
        point.y = mMapView.getHeight() - bottomBar.getHeight() - 80;
        mMapView.setScaleControlPosition(point);

        //更改指北针位置
        mBaiduMap.setCompassPosition(new android.graphics.Point(100, 200));

        //绘制所有道路
        Markers.showAllRoads(mBaiduMap);
    }


    //设置定位控件----------------------------------------------------------------------------
    private void setupLocalButton() {
        mCurrentMode = LocationMode.NORMAL;
        localStatusText.setText("浏览");
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);//获取传感器管理服务

        // 开启定位图层
        mBaiduMap.setMyLocationEnabled(true);


        requestLocButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (mCurrentMode) {
                    case NORMAL:
                        localStatusText.setText("跟随");
                        mCurrentMode = LocationMode.FOLLOWING;
                        mBaiduMap.setMyLocationConfiguration(new MyLocationConfiguration(
                                mCurrentMode, true, mCurrentMarker,
                                accuracyCircleFillColor, accuracyCircleStrokeColor));
                        MapStatus.Builder builder = new MapStatus.Builder();
                        builder.overlook(0);
                        mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
                        break;
                    case FOLLOWING:
                        localStatusText.setText("浏览");
                        mCurrentMode = LocationMode.NORMAL;
                        mBaiduMap.setMyLocationConfiguration(new MyLocationConfiguration(
                                mCurrentMode, true, mCurrentMarker,
                                accuracyCircleFillColor, accuracyCircleStrokeColor));
                        MapStatus.Builder builder1 = new MapStatus.Builder();
                        builder1.overlook(0);
                        mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder1.build()));
                        break;
                    default:
                        break;
                }
            }
        });


        // 定位初始化
        mLocClient = new LocationClient(this);
        mLocClient.registerLocationListener(myListener);
        reStartLocClient(scanSpan);

    }


    public void reStartLocClient(int span) {

        settingIconRunning.setSelected(true);
        settingIconStoping.setSelected(false);
        scanSpan = span;
        stopLocClient();
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true); // 打开gps
        option.setCoorType("bd09ll"); // 设置返回结果坐标类型
        option.setIsNeedAddress(true);// 位置，一定要设置，否则后面得不到地址
        option.setScanSpan(scanSpan * 1000);

        mLocClient.setLocOption(option);
        mLocClient.start();
    }

    public void stopLocClient() {
        if (mLocClient.isStarted()) {
            mLocClient.stop();
        }
    }

    //禁止方向传感器
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
//        double x = sensorEvent.values[SensorManager.DATA_X];
//        if (Math.abs(x - lastX) > 1.0) {
//            mCurrentDirection = (int) x;
//
//            locData = new MyLocationData.Builder()
//                    .accuracy(curAccracy)
//                    // 此处设置开发者获取到的方向信息，顺时针0-360
//                    .direction(mCurrentDirection)
//                    .latitude(curLatitude)
//                    .longitude(curLongitude)
//                    .build();
//            mBaiduMap.setMyLocationData(locData);
//        }
//        lastX = x;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public void setGaussText(BDLocation location) {
        Point p = new Point(location);
        gaussText.setText(p.gauss.toString());
    }

    /**
     * 定位SDK监听函数
     */
    public class MyLocationListenner implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation location) {
            Log.d(TAG, "onReceiveLocation: 获取到位置信息-------");
            // map view 销毁后不在处理新接收的位置
            if (location == null || mMapView == null) {
                return;
            }
            //记录定位结果
//            logLocationInfo(location);
            curLatitude = location.getLatitude();
            curLongitude = location.getLongitude();
            curAccracy = location.getRadius();
            mCurrentDirection = location.getDirection();
            LatLng pt = new LatLng(curLatitude, curLongitude);
            Log.e(TAG, String.format("当前点纬度：%.5f, 经度：%.5f", curLatitude, curLongitude));
            Log.e(TAG, String.format("当前定位方向：%.5f", mCurrentDirection));

            if (Constants.isPointEffect(pt)) {
                setGaussText(location);
            } else {
                gaussText.setText("GPS信号弱，本次定位失败");
                return;
            }

            locData = new MyLocationData.Builder()
                    .accuracy(location.getRadius())
                    // 此处设置开发者获取到的方向信息，顺时针0-360
                    .direction(mCurrentDirection)
                    .latitude(curLatitude)
                    .longitude(curLongitude)
                    .build();
            mBaiduMap.setMyLocationData(locData);
            if (isFirstLoc) {
                isFirstLoc = false;
                MapStatus.Builder builder = new MapStatus.Builder();
                builder.target(pt).zoom(Constants.DEFAULT_LEVEL);
                mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
            }


            //如果轨迹开关打开，则开始记录轨迹
            if (isTrailingOpen) {
                LatLng newPoint = new LatLng(curLatitude, curLongitude);
                Markers.updateTrail(mBaiduMap, lastPoint, newPoint);
                lastPoint = newPoint;
                //将当前点保存到数据库
                currentTrailItem = addTrailItem(currentTrail.id);
            }
        }

        public void onReceivePoi(BDLocation poiLocation) {
            Log.d(TAG, "onReceiveLocation: 获取到点信息-------");
        }
    }

    private void logLocationInfo(BDLocation location) {
        StringBuffer sb = new StringBuffer(256);
        sb.append("time : ");
        sb.append(location.getTime());
        sb.append("\nerror code : ");
        sb.append(location.getLocType());
        sb.append("\nlatitude : ");
        sb.append(location.getLatitude());
        sb.append("\nlontitude : ");
        sb.append(location.getLongitude());
        sb.append("\nradius : ");
        sb.append(location.getRadius());
        if (location.getLocType() == BDLocation.TypeGpsLocation) {// GPS定位结果
            sb.append("\nspeed : ");
            sb.append(location.getSpeed());// 单位：公里每小时
            sb.append("\nsatellite : ");
            sb.append(location.getSatelliteNumber());
            sb.append("\nheight : ");
            sb.append(location.getAltitude());// 单位：米
            sb.append("\ndirection : ");
            sb.append(location.getDirection());// 单位度
            sb.append("\naddr : ");
            sb.append(location.getAddrStr());
            sb.append("\ndescribe : ");
            sb.append("gps定位成功");

        } else if (location.getLocType() == BDLocation.TypeNetWorkLocation) {// 网络定位结果
            sb.append("\naddr : ");
            sb.append(location.getAddrStr());
            //运营商信息
            sb.append("\noperationers : ");
            sb.append(location.getOperators());
            sb.append("\ndescribe : ");
            sb.append("网络定位成功");
        } else if (location.getLocType() == BDLocation.TypeOffLineLocation) {// 离线定位结果
            sb.append("\ndescribe : ");
            sb.append("离线定位成功，离线定位结果也是有效的");
        } else if (location.getLocType() == BDLocation.TypeServerError) {
            sb.append("\ndescribe : ");
            sb.append("服务端网络定位失败，可以反馈IMEI号和大体定位时间到loc-bugs@baidu.com，会有人追查原因");
        } else if (location.getLocType() == BDLocation.TypeNetWorkException) {
            sb.append("\ndescribe : ");
            sb.append("网络不同导致定位失败，请检查网络是否通畅");
        } else if (location.getLocType() == BDLocation.TypeCriteriaException) {
            sb.append("\ndescribe : ");
            sb.append("无法获取有效定位依据导致定位失败，一般是由于手机的原因，处于飞行模式下一般会造成这种结果，可以试着重启手机");
        }
        sb.append("\nlocationdescribe : ");
        sb.append(location.getLocationDescribe());// 位置语义化信息
        List<Poi> list = location.getPoiList();// POI数据
        if (list != null) {
            sb.append("\npoilist size = : ");
            sb.append(list.size());
            for (Poi p : list) {
                sb.append("\npoi= : ");
                sb.append(p.getId() + " " + p.getName() + " " + p.getRank());
            }
        }
        Log.e("描述：", sb.toString());
    }

    private TrailItems addTrailItem(long trailId) {
        TrailItems trailItem = new TrailItems(trailId, curLatitude, curLongitude, System.currentTimeMillis());
        TrailsDao.saveTrailItem(trailItem);
        return trailItem;
    }

    private void switchTrail(boolean on) {
        if (on) {
            isTrailingOpen = true;
            switchIcon.setBackgroundResource(R.mipmap.switch_open);
        } else {
            isTrailingOpen = false;
            switchIcon.setBackgroundResource(R.mipmap.switch_close);
        }
    }

    //生命周期方法------------------------------------------------------------------------------
    @Override
    protected void onDestroy() {
        // 退出时销毁定位
        mLocClient.stop();
        // 关闭定位图层
        mBaiduMap.setMyLocationEnabled(false);
        //在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
        mMapView.onDestroy();
        mMapView = null;

        super.onDestroy();
    }

    @Override
    protected void onResume() {
        //在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
        mMapView.onResume();

        super.onResume();
        //为系统的方向传感器注册监听器
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
                SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    protected void onStop() {
        //取消注册传感器监听
        mSensorManager.unregisterListener(this);
        super.onStop();
    }


    //接收到推送消息
    @Subscribe
    public void onShowNewPoint(Events.ShowPointEvent e) {
        LatLng pt = new LatLng(e.liberary.latitude, e.liberary.longitude);
        addMarker(mBaiduMap, pt);
        MapViewHelper.setUserMapCenter(mBaiduMap, pt);
    }

    //接收到推送消息
    @Subscribe
    public void onSearchPoint(Events.SearchPointEvent e) {
        Point p = e.point;
        addMarker(mBaiduMap, p.bd);
        MapViewHelper.setUserMapCenter(mBaiduMap, p.bd);
    }

    //接收到推送消息
    @Subscribe
    public void onChangeArrowIcon(Events.ChangeArrowIconEvent e) {
        if (e.iconId > 0) {
            mCurrentMarker = BitmapDescriptorFactory.fromResource(e.iconId);
            mBaiduMap.setMyLocationConfiguration(new MyLocationConfiguration(
                    mCurrentMode, true, mCurrentMarker, accuracyCircleFillColor, accuracyCircleStrokeColor));
        } else {
            mCurrentMarker = null;
            mBaiduMap.setMyLocationConfiguration(new MyLocationConfiguration(mCurrentMode, true, mCurrentMarker));
        }
    }

    //接收到保存当前轨迹消息
    @Subscribe
    public void onSavedCurrentTrail(Events.SavedTrailEvent e) {
        //关闭轨迹记录功能
        switchTrail(false);
        //名称为空表示放弃
        if (e.name == null) {
            //删除所有点，并删除当前轨迹
            TrailsDao.deleteTrail(currentTrail);
            return;
        }

        currentTrail.endItemId = currentTrailItem.id;
        currentTrail.title = e.name;
        currentTrail.isRoad = e.isRoad;
        TrailsDao.updateTrail(currentTrail);
    }

    //接收到推送消息
    @Subscribe
    public void onRequestShowTrail(Events.ShowTrailEvent e) {
        //清除所有图层
        Markers.clearTrail(mBaiduMap);
        //重绘永久道路
        Markers.showAllRoads(mBaiduMap);
        //高亮显示点击道路
        Markers.showTrail(mBaiduMap, e.trail, Markers.HIGHELIGHT_ROAD_COLOR, 10);
    }
}