package cn.cxw.baidu;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.model.LatLng;

import java.util.HashMap;
import java.util.Map;

import cn.cxw.armymap.MainApplication;
import cn.cxw.armymap.R;
import cn.cxw.dao.PlanDao;
import cn.cxw.model.Plan;
import cn.cxw.model.Symbol;

import static cn.cxw.baidu.Markers.addMarker;

/**
 * Created by CXW-IBM on 2017/9/17.
 */

public class Paint {

    public class Preset {
        int penId;
        int iconId;
        String defualtName;
        Markers.Anchor anchor;

        public Preset(int penId, int iconId, String defualtName, Markers.Anchor anchor) {
            this.penId = penId;
            this.iconId = iconId;
            this.defualtName = defualtName;
            this.anchor = anchor;
        }
    }

    public final Preset[] presets = {
            new Preset(R.id.icon_001, R.mipmap.panel_icon_001, "基本指挥所", new Markers.Anchor(16 / 58.0, 43 / 54.0)),
            new Preset(R.id.icon_002, R.mipmap.panel_icon_002, "后方指挥所", new Markers.Anchor(16 / 58.0, 43 / 54.0)),
            new Preset(R.id.icon_003, R.mipmap.panel_icon_003, "预备指挥所", new Markers.Anchor(16 / 58.0, 43 / 54.0)),
            new Preset(R.id.icon_004, R.mipmap.panel_icon_004, "工兵保障队", new Markers.Anchor(0.5, 9 / 29)),
            new Preset(R.id.icon_005, R.mipmap.panel_icon_005, "防化保障队", new Markers.Anchor(0.5, 9 / 29)),
            new Preset(R.id.icon_006, R.mipmap.panel_icon_006, "通信保障队", new Markers.Anchor(0.5, 9 / 29)),
            new Preset(R.id.icon_007, R.mipmap.panel_icon_007, "障碍设置队", new Markers.Anchor(0.5, 9 / 29)),
            new Preset(R.id.icon_008, R.mipmap.panel_icon_008, "障碍排除队", new Markers.Anchor(0.5, 9 / 29)),
            new Preset(R.id.icon_009, R.mipmap.panel_icon_009, "运动保障队", new Markers.Anchor(0.5, 9 / 29)),
            new Preset(R.id.icon_010, R.mipmap.panel_icon_010, "穿插迂回队", new Markers.Anchor(0.5, 9 / 29)),
            new Preset(R.id.icon_011, R.mipmap.panel_icon_011, "袭击分队", new Markers.Anchor(0.5, 9 / 29)),
            new Preset(R.id.icon_012, R.mipmap.panel_icon_012, "反坦克保障队", new Markers.Anchor(0.5, 9 / 29)),
            new Preset(R.id.icon_013, R.mipmap.panel_icon_013, "卫勤保障队", new Markers.Anchor(0.5, 9 / 29))
    };

    public static final Map<Integer, Preset> panMap = new HashMap<Integer, Preset>();

    public BaiduMap mBaiduMap;
    public Activity activity;
    public View selectedPen;
    public Preset currentPreset;
    public Plan currentPlan;
    public MainApplication mApp;

    public Paint(BaiduMap mBaiduMap, Activity activity) {
        this.mApp = MainApplication.INSTANCE;
        this.mBaiduMap = mBaiduMap;
        this.activity = activity;

        for (int i = 0; i < presets.length; i++) {
            panMap.put(presets[i].penId, presets[i]);
        }
        initPens();
    }

    public void initPens() {
        ViewGroup viewGroup = (ViewGroup) activity.findViewById(R.id.icons_containt);
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            View v = viewGroup.getChildAt(i);
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (selectedPen != null) {
                        selectedPen.setSelected(false);
                    }
                    selectedPen = v;
                    selectedPen.setSelected(true);
                    currentPreset = panMap.get(v.getId());
                }
            });
        }

        //模拟点击第1个按钮
        viewGroup.getChildAt(0).performClick();

        //得到当前方案，如果没有则创建一个方案
        currentPlan = PlanDao.getLatestPlan();
        if (currentPlan == null) {
            createNewPlan();
        }
    }

    //添加新标号
    public void addSymbol(LatLng point) {
        Symbol symbol = new Symbol();
        symbol.planId = currentPlan.id;
        symbol.bdLatitude = point.latitude;
        symbol.bdLongitude = point.longitude;
        //添加队标
        symbol.iconResId = currentPreset.iconId;
        symbol.iconMarker = Markers.addMarker(mBaiduMap, point, getCurrentSymbolIcon(), currentPreset.anchor);
        //添加文本标签
        symbol.name = currentPreset.defualtName;
//        symbol.textLabel = Markers.addTextLabel(mBaiduMap, point, symbol.name);
        //保存点
        //symbol.id = PlanDao.saveSymbol(symbol);
        //将当前标号添加到Map
        currentPlan.symbolMap.put(symbol.iconMarker, symbol);
    }

    //修改图标
    public void changeIcon(Marker marker, int iconId) {
        //得到集合中的点
        Symbol symbol = currentPlan.symbolMap.get(marker);
        if (symbol == null) {
            return;
        }
        symbol.iconMarker.remove();
        symbol.iconResId = currentPreset.iconId;
        symbol.iconMarker = addMarker(mBaiduMap, marker.getPosition(), symbol.iconResId);
        PlanDao.updateSymbol(symbol);
    }

    //修改文本内容
    public void changeLabel(Marker marker, String text) {
        //得到集合中的点
        Symbol symbol = currentPlan.symbolMap.get(marker);
        if (symbol == null) {
            return;
        }

        //移除文本标签
        symbol.textLabel.remove();
        symbol.name = text;
        symbol.textLabel = Markers.addTextLabel(mBaiduMap, marker.getPosition(), symbol.name);
        PlanDao.updateSymbol(symbol);
    }

    //删除某队标
    public void removeSymbol(Marker marker) {
        //得到集合中的点
        Symbol symbol = currentPlan.symbolMap.get(marker);
        if (symbol == null) {
            return;
        }
        //移除图标
        symbol.iconMarker.remove();
        //移除文本标签
        symbol.textLabel.remove();
        //数据库中删除该图标
        PlanDao.deleteSymbol(symbol);
        //集合中移除图标
        currentPlan.symbolMap.remove(marker);
    }

    //新建标图方案
    public void createNewPlan() {
        currentPlan = new Plan();
        currentPlan.title = "新标图";
        currentPlan.updateTimestamp = System.currentTimeMillis();
        currentPlan.id = PlanDao.savePlan(currentPlan);
    }

    public BitmapDescriptor getCurrentSymbolIcon() {
        return BitmapDescriptorFactory.fromView(getView(currentPreset.iconId, currentPreset.defualtName));
    }


    private View view;

    public View getView(int resId, String text) {
        ViewHolder viewHolder;
        if (view == null) {
            view = View.inflate(activity, R.layout.symbol_layout, null);
            viewHolder = new ViewHolder();
            viewHolder.iv = (ImageView) view.findViewById(R.id.symbol_icon);
            viewHolder.tv = (TextView) view.findViewById(R.id.symbol_text);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }
        viewHolder.iv.setImageResource(resId);
        viewHolder.tv.setText(text);
        return view;
    }

    private static class ViewHolder {
        public ImageView iv;
        public TextView tv;
    }
}
