package cn.cxw.armymap.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.baidu.mapapi.model.LatLng;

import cn.cxw.armymap.MainApplication;
import cn.cxw.armymap.R;
import cn.cxw.core.Constants;
import cn.cxw.core.Events;
import cn.cxw.dao.LiberaryDao;
import cn.cxw.model.GaussPoint;
import cn.cxw.model.Liberary;
import cn.cxw.model.Point;
import cn.cxw.util.NumberUtil;
import cn.cxw.util.UiHelper;


/**
 * Created by CXW-IBM on 2017/9/3.
 */

public class Dialogs {

    private static MainApplication mApp = MainApplication.INSTANCE;
    private static final String Tag = "Dialogs";

    /**
     * 显示箭头选择对话框
     */
    public static Dialog showArraySelectDialog(Activity activity) {

        final Dialog dialog = new Dialog(activity, R.style.anydo_dialog);
        final View dialogView = UiHelper.getLightThemeView(activity, R.layout.dialog_select_arrow);

        final RadioGroup radiogroup = (RadioGroup)dialogView.findViewById(R.id.radiogroup);
        dialog.setContentView(dialogView);

        //取消按钮
        (dialogView.findViewById(R.id.btn_cancel)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        //确定按钮
        (dialogView.findViewById(R.id.btn_ok)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int selectId = radiogroup.getCheckedRadioButtonId();
                switch (selectId){
                    case R.id.default_arrow:
                        mApp.postEvent(new Events.ChangeArrowIconEvent(-1));
                        break;
                    case R.id.my_arrow_like_default:
                        mApp.postEvent(new Events.ChangeArrowIconEvent(R.mipmap.icon_geo_default_small));
                        break;
                    case R.id.my_arrow_big:
                        mApp.postEvent(new Events.ChangeArrowIconEvent(R.mipmap.icon_geo));
                        break;
                    case R.id.my_arrow_small:
                        mApp.postEvent(new Events.ChangeArrowIconEvent(R.mipmap.icon_geo_small));
                        break;
                }
                dialog.dismiss();
            }
        });

        dialog.show();
        return dialog;
    }


    /**
     * 显示点详细信息
     */
    public static Dialog showPointInfoDialog(Activity activity, final LatLng pt, final GaussPoint point) {

        final Dialog dialog = new Dialog(activity, R.style.anydo_dialog);
        final View dialogView = UiHelper.getLightThemeView(activity, R.layout.dialog_pint_info);
        dialog.setContentView(dialogView);

        TextView xLabel = (TextView) dialogView.findViewById(R.id.point_X);
        TextView yLabel = (TextView) dialogView.findViewById(R.id.point_Y);
        final EditText pointNameEdit = (EditText) dialogView.findViewById(R.id.edit_point_name);

        xLabel.setText(String.format("X：%s", NumberUtil.formatNumber(point.x, "##,#####")));
        yLabel.setText(String.format("Y：%s", NumberUtil.formatNumber(point.y, "##,#####")));
        pointNameEdit.setHint("保存名称：" + point.toString());

        //取消按钮
        (dialogView.findViewById(R.id.btn_cancel)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        //确定按钮
        (dialogView.findViewById(R.id.btn_ok)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String n = pointNameEdit.getText().toString();
                if(TextUtils.isEmpty(n)){
                    n = point.toString();
                }

                Liberary lib = new Liberary(n, pt, point);
                LiberaryDao.savePoint(lib);

                dialog.dismiss();
            }
        });

        dialog.show();
        return  dialog;
    }


    /**
     * 显示重命名编辑对话框
     */
    public static Dialog showEditDialog(Activity activity, String title, final String content, final Object obj) {

        final Dialog dialog = new Dialog(activity, R.style.anydo_dialog);
        final View dialogView = UiHelper.getLightThemeView(activity, R.layout.dialog_input);

        ((TextView)dialogView.findViewById(R.id.tv_title)).setText(title);
        dialog.setContentView(dialogView);
        final EditText editText = (EditText) dialogView.findViewById(R.id.edt_content);
        editText.setText(content);
        editText.setHint("请输入内容");
        UiHelper.setKeyboardFocus(editText);

        //取消按钮
        (dialogView.findViewById(R.id.btn_cancel)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        //确定按钮
        (dialogView.findViewById(R.id.btn_ok)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String s = editText.getText().toString();
                if(TextUtils.isEmpty(s)){
                    mApp.showMessage("名称不能为空");
                    return;
                }

                mApp.postEvent(new Events.ReceivedEditContentEvent(s, obj));
                dialog.dismiss();
            }
        });

        dialog.show();
        return  dialog;
    }


    /**
     * 显示保存轨迹对话框
     */
    public static Dialog showSaveTrailDialog(Activity activity, boolean b, final String content) {

        final Dialog dialog = new Dialog(activity, R.style.anydo_dialog);
        final View dialogView = UiHelper.getLightThemeView(activity, R.layout.dialog_save_trail);

        dialog.setContentView(dialogView);
        final EditText editText = (EditText) dialogView.findViewById(R.id.edit_name);
        editText.setText(content);
        final CheckBox checkBox = (CheckBox) dialogView.findViewById(R.id.chkbox);
        checkBox.setChecked(b);

        //取消按钮
        (dialogView.findViewById(R.id.btn_cancel)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        //确定按钮
        (dialogView.findViewById(R.id.btn_abandon)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mApp.postEvent(new Events.SavedTrailEvent(null, checkBox.isChecked()));
                dialog.dismiss();
            }
        });

        //确定按钮
        (dialogView.findViewById(R.id.btn_ok)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = editText.getText().toString();
                if(TextUtils.isEmpty(name)){
                    mApp.showMessage("名称不能为空");
                    return;
                }
                mApp.postEvent(new Events.SavedTrailEvent(name, checkBox.isChecked()));
                dialog.dismiss();
            }
        });

        dialog.show();
        return  dialog;
    }


    /**
     * 显示警告文本对话框
     */
    public static Dialog showWarningTextDialog(Activity activity, final Object obj, final String contentLong, final String contentShort) {

        final Dialog dialog = new Dialog(activity, R.style.anydo_dialog);
        final View dialogView = UiHelper.getLightThemeView(activity, R.layout.dialog_text_readonly);

        dialog.setContentView(dialogView);
        ((TextView) dialogView.findViewById(R.id.tv_title)).setText("提醒");
        final TextView textViewLongText = (TextView) dialogView.findViewById(R.id.tv_long);
        final TextView textViewShortText = (TextView) dialogView.findViewById(R.id.tv_short);
        if(contentLong != null){
            textViewLongText.setText(contentLong);
            textViewLongText.setVisibility(View.VISIBLE);
        } else {
            textViewShortText.setText(contentShort);
            textViewShortText.setVisibility(View.VISIBLE);
        }


        //取消按钮
        (dialogView.findViewById(R.id.btn_cancel)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        //确定按钮
        (dialogView.findViewById(R.id.btn_ok)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mApp.postEvent(new Events.ReceiveWarningInfoEvent(obj, true));
                dialog.dismiss();
            }
        });

        dialog.show();
        return  dialog;
    }


    /**
     * 显示查找点输入对话框
     */
    public static Dialog showSearchPointDialog(Activity activity) {

        final Dialog dialog = new Dialog(activity, R.style.anydo_dialog);
        final View dialogView = UiHelper.getLightThemeView(activity, R.layout.dialog_search_pint);

        dialog.setContentView(dialogView);
        final EditText editX = (EditText) dialogView.findViewById(R.id.edit_x);
        final EditText editY = (EditText) dialogView.findViewById(R.id.edit_y);
        UiHelper.setKeyboardFocus(editX);

        //取消按钮
        (dialogView.findViewById(R.id.btn_cancel)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        //确定按钮
        (dialogView.findViewById(R.id.btn_ok)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String X = editX.getText().toString();
                String Y = editY.getText().toString();
                if(TextUtils.isEmpty(X) || TextUtils.isEmpty(Y)){
                    mApp.showMessage("坐标值不能为空");
                    return;
                }
                int lenX = X.length();
                int lenY = Y.length();
                Log.d(Tag, "长度X：" + lenX + "长度Y：" + lenY);

                if((lenX != 4 && lenX != 7) || (lenY != 5 && lenY !=8)){
                    mApp.showMessage("坐标值格式错误，请输入全值精确坐标，X坐标为4位或7位，Y坐标为5位或8位");
                    return;
                }
                double x = Double.valueOf(X);
                double y = Double.valueOf(Y);
                if(lenX == 4){
                    x *= 1000;
                }
                if(lenY == 5){
                    y *= 1000;
                }

                //校正误差
                x += Constants.dp.x;
                y += Constants.dp.y;
                Point point = new Point(new GaussPoint(x, y));
                mApp.postEvent(new Events.SearchPointEvent(point));
                dialog.dismiss();
            }
        });

        dialog.show();
        return  dialog;
    }
}
