package cn.cxw.armymap.subpages;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;

import cn.cxw.armymap.R;
import cn.cxw.armymap.dialogs.Dialogs;
import cn.cxw.core.Constants;
import cn.cxw.core.Events;
import cn.cxw.dao.TrailsDao;
import cn.cxw.model.Trails;
import cn.cxw.util.DateHelper;
import cn.cxw.util.UiHelper;
import cn.cxw.views.BasicFragment;


@SuppressLint("ValidFragment")
public class HistoryTrailsFragment extends BasicFragment {

    //弹出菜单
    PopupMenu popupMenu;
    Menu menu;

    public List<Trails> list;
    public ListView listView;
    TrailsAdapter adapter;
    private boolean isLoadFinish;
    public int curPage = 1;
    private View listViewFootProgressBar;

    //视图创建时执行
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return UiHelper.getLightThemeView(getActivity(), R.layout.fragment_history_trails);
    }

    //视图创建完成后执行
    @Override
    public void onViewCreated(View v, Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        // 设置页面标题
        ((TextView) v.findViewById(R.id.tv_title)).setText("轨迹记录历史");
        listView = (ListView) v.findViewById(R.id.listview);
        listView.setEmptyView(v.findViewById(R.id.empty_text));
        list =  new ArrayList<Trails>();
        adapter = new TrailsAdapter(list);

        listViewFootProgressBar = UiHelper.getLightThemeView(getActivity(), R.layout.listview_foot_progressbar);
        
        listView.addFooterView(listViewFootProgressBar);
        listView.setAdapter(adapter);
        listView.removeFooterView(listViewFootProgressBar);

        listView.setOnItemClickListener(adapter);
        listView.setOnItemLongClickListener(adapter);

        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) { }

            @Override
            public void onScroll(AbsListView listView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                int lastItemIndex = listView.getLastVisiblePosition();//当前屏幕最后一条记录ID
                if(lastItemIndex + 1 == totalItemCount && isLoadFinish){//判断往下是否达到数据最后一条记录
                    //锁定加载完成标志
                    isLoadFinish = false;
                    //根据页码获取最新数据
                    try {
                        asyncAddData();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        //首次加载数据
        asyncAddData();
    }

    private void asyncAddData() {
        listView.addFooterView(listViewFootProgressBar);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    List<Trails> trails = TrailsDao.getTrails(curPage);

                    if(trails != null && trails.size() > 0){
                        list.addAll(trails);
                        mApp.postEvent(new Events.OnRefreshListEvent(Constants.hasData));
                    }else{
                        mApp.postEvent(new Events.OnRefreshListEvent(Constants.noLeftData));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }


    private void popupMenu(View v, final int position){

        final Trails trail = list.get(position);

        popupMenu = new PopupMenu(getActivity(), v);
        menu = popupMenu.getMenu();
        menu.add(0, Menu.FIRST + 2, 0, trail.isRoad ? "设为临时轨迹" : "设为永久道路");

        // 通过XML文件添加菜单项
        MenuInflater menuInflater = getActivity().getMenuInflater();
        menuInflater.inflate(R.menu.liberary_memu, menu);

        // 监听事件
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

            @Override
            public boolean onMenuItemClick(MenuItem item) {

                switch (item.getItemId()) {
                    case R.id.rename:
                        Dialogs.showEditDialog(getActivity(), "历史路径重命名", trail.title, trail);
                        break;
                    case R.id.delete:
                        //删除轨迹将无法恢复，请慎重
                        Dialogs.showWarningTextDialog(getActivity(), position, "删除轨迹将无法恢复，请慎重操作，是否确定？", null);
                        break;
                    case Menu.FIRST + 2:
                        trail.isRoad = !trail.isRoad;
                        TrailsDao.updateTrail(trail);
                        adapter.notifyDataSetChanged();
                        break;
                    default:
                        break;
                }
                return false;
            }
        });
        popupMenu.show();
    }


    @Subscribe
    public void onRefreshList(Events.OnRefreshListEvent e){
        listView.removeFooterView(listViewFootProgressBar);
        if(e.msgType == Constants.hasData){
            curPage++;
            adapter.notifyDataSetChanged();
            isLoadFinish = true;
        }else if(e.msgType == Constants.noLeftData){
            //第1页不做提醒
            if(curPage > 2) {
                mApp.showMessage("后面没有数据了");
            }
        }
    }

    //接收到推送消息
    @Subscribe
    public void onRenameTrail(Events.ReceivedEditContentEvent e) {
        Trails trail = (Trails) e.info;
        trail.title = e.content;
        TrailsDao.updateTrail(trail);
        adapter.notifyDataSetChanged();
    }

    //接收到删除对象消息
    @Subscribe
    public void onDeleteTrail(Events.ReceiveWarningInfoEvent e) {
        int position = (Integer) e.obj;
        Trails trail = list.get(position);
        TrailsDao.deleteTrail(trail);
        list.remove(position);
        adapter.notifyDataSetChanged();
    }


    private class TrailsAdapter extends BaseAdapter implements AdapterView.OnItemClickListener, OnItemLongClickListener {

        //数据源,从构造方法传入
        public List<Trails> list;
        public ViewsHolder viewsHolder;
        public LayoutInflater inflater;

        public TrailsAdapter(List<Trails> list) {
            this.list = list;
            inflater = LayoutInflater.from(getActivity());
        }

        public void setDataAndRefresh(List<Trails> list){
            this.list = list;
            notifyDataSetChanged();
        }

        //任务条数
        @Override
        public int getCount() {
            return list.size();
        }

        //得到所在位置的数据
        @Override
        public Trails getItem(int position) {
            return list.get(position);
        }

        //当前条目的Id即任务Id
        @Override
        public long getItemId(int position) {
            return position;
        }


        //用于缓存条目的包裹类
        private final class ViewsHolder {
            public ImageView icon;
            public TextView title;
            public TextView isRoadText;
            public TextView createTime;
        }

        /**
         * 得到List条目视图
         */
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.row_history_trails, null);
                viewsHolder = new ViewsHolder();
                viewsHolder.icon = (ImageView)convertView.findViewById(R.id.img_icon);
                viewsHolder.title = (TextView)convertView.findViewById(R.id.tv_name);
                viewsHolder.createTime = (TextView)convertView.findViewById(R.id.tv_time);
                viewsHolder.isRoadText = (TextView)convertView.findViewById(R.id.tv_is_road);
                convertView.setTag(viewsHolder);
            }else{
                viewsHolder = (ViewsHolder)convertView.getTag();
            }

            Trails trail = getItem(position);
            viewsHolder.title.setText(trail.title);
            viewsHolder.createTime.setText(DateHelper.convertTime2ShortDate(TrailsDao.getTrailItem(trail.startItemId).time));
            viewsHolder.isRoadText.setText(trail.isRoad ? "永久道路" : "临时轨迹");
            viewsHolder.icon.setImageResource(trail.isRoad ? R.mipmap.road_icon : R.mipmap.road_temp_icon);
            return convertView;
        }

        /**
         * 点击List条目时执行
         */
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            // 高亮显示某条道路
            mApp.postEvent(new Events.ShowTrailEvent(getItem(position)));
            getActivity().getSupportFragmentManager().popBackStack();
        }

        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            popupMenu(view, position);
            return true;
        }
    }

}