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
import cn.cxw.dao.LiberaryDao;
import cn.cxw.model.Liberary;
import cn.cxw.util.UiHelper;
import cn.cxw.views.BasicFragment;


@SuppressLint("ValidFragment")
public class LiberaryPointsFragment extends BasicFragment {

    //弹出菜单
    PopupMenu popupMenu;
    Menu menu;

    public List<Liberary> list;
    public ListView listView;
    LiberaryAdapter adapter;

    private boolean isLoadFinish;
    public int curPage = 1;
    private View listViewFootProgressBar;


    //视图创建时执行
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return UiHelper.getLightThemeView(getActivity(), R.layout.fragment_liberary_points);
    }

    //视图创建完成后执行
    @Override
    public void onViewCreated(View v, Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        // 设置页面标题
        ((TextView) v.findViewById(R.id.tv_title)).setText("收藏的点");
        listView = (ListView) v.findViewById(R.id.listview);
        listView.setEmptyView(v.findViewById(R.id.empty_text));
        list = new ArrayList<Liberary>();
        adapter = new LiberaryAdapter(list);

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
                    List<Liberary> libs = LiberaryDao.getLibPoints(curPage);

                    if(libs != null && libs.size() > 0){
                        list.addAll(libs);
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
        popupMenu = new PopupMenu(getActivity(), v);
        menu = popupMenu.getMenu();

        // 通过XML文件添加菜单项
        MenuInflater menuInflater = getActivity().getMenuInflater();
        menuInflater.inflate(R.menu.liberary_memu, menu);

        // 监听事件
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Liberary lib = list.get(position);
                switch (item.getItemId()) {
                    case R.id.rename:
                        Dialogs.showEditDialog(getActivity(), "收藏点重命名", lib.name, lib);
                        break;
                    case R.id.delete:
                        LiberaryDao.deletePoint(lib);
                        list.remove(position);
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

    //接收到重命名消息
    @Subscribe
    public void requestRename(Events.ReceivedEditContentEvent e) {
        Liberary lib = (Liberary) e.info;
        lib.name = e.content;
        LiberaryDao.updatePoint(lib);
        adapter.notifyDataSetChanged();
    }

    private class LiberaryAdapter extends BaseAdapter implements AdapterView.OnItemClickListener, OnItemLongClickListener {

        //数据源,从构造方法传入
        public List<Liberary> list;
        public ViewsHolder viewsHolder;
        public LayoutInflater inflater;

        public LiberaryAdapter(List<Liberary> list) {
            this.list = list;
            inflater = LayoutInflater.from(getActivity());
        }

        public void setDataAndRefresh(List<Liberary> list){
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
        public Liberary getItem(int position) {
            return list.get(position);
        }

        //当前条目的Id即任务Id
        @Override
        public long getItemId(int position) {
            return position;
        }


        //用于缓存条目的包裹类
        private final class ViewsHolder {
            public TextView name;
            public TextView time;
            public TextView gauss;
        }

        /**
         * 得到List条目视图
         */
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.row_liberary_points, null);
                viewsHolder = new ViewsHolder();
                viewsHolder.name = (TextView)convertView.findViewById(R.id.tv_name);
                viewsHolder.time = (TextView)convertView.findViewById(R.id.tv_time);
                viewsHolder.gauss = (TextView)convertView.findViewById(R.id.tv_gauss);
                convertView.setTag(viewsHolder);
            }else{
                viewsHolder = (ViewsHolder)convertView.getTag();
            }

            Liberary lib = getItem(position);
            viewsHolder.name.setText(lib.name);
            viewsHolder.time.setText(lib.getCreateTime());
            viewsHolder.gauss.setText(lib.getGaussRemark());
            return convertView;
        }

        /**
         * 点击List条目时执行
         */
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            mApp.postEvent(new Events.ShowPointEvent(getItem(position)));
            getActivity().getSupportFragmentManager().popBackStack();
        }

        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            popupMenu(view, position);
            return true;
        }
    }

}