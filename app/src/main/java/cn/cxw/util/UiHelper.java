package cn.cxw.util;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import cn.cxw.armymap.MainApplication;
import cn.cxw.armymap.R;


/**
 * 界面处理工具
 *
 * @author cxw
 */
public class UiHelper {

    public static final String EXTRA_TITLE = "title";
    public static final String EXTRA_ADDRESS = "address";
    public static final String EXTRA_ISNEEDTOKEN = "isNeedToken";

    /**
     * 将构造方法私有化，不允许外部实例化工具类
     */
    private UiHelper() {
    }

    /**
     * 关闭软键盘，首先要判断当前是否有焦点
     *
     * @param activity
     */
    public static void shutKeyboardIfNeed(Activity activity) {
        View v = activity.getCurrentFocus();
        if (v != null) {
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        }
    }

    /**
     * 弹出输入法键盘
     *
     * @param activity
     */
    public static void showKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(0, InputMethodManager.SHOW_FORCED);
    }

    /**
     * 切换键盘显示与隐藏
     */
    public static void switchingKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm.isActive())
            shutKeyboardIfNeed(activity);
        else
            showKeyboard(activity);
    }

    /**
     * 当编辑框在AlertDialog或PoupWIndow中时，仅仅调用requestFocus()不会获得弹出键盘，所以必须显示地设置监听响应
     * 将光标置于编辑框末尾，并取得焦点
     *
     * @param editText
     */
    public static void selectEndOfEdittext(final AlertDialog dialog, EditText editText) {
        editText.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                }
            }
        });

        editText.setSelection(editText.getText().length());
        // 下面这行不需要，因为上面设置光标位置已经获取焦点
        // editText.requestFocus();
    }

    /**
     * 在Fragment中弹出键盘
     *
     * @param editText
     */
    public static void setKeyboardFocus(final EditText editText) {
        new Handler().postDelayed(new Runnable() {
            public void run() {
                editText.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_DOWN, 0, 0, 0));
                editText.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_UP, 0, 0, 0));
                editText.setSelection(editText.getText().length());
            }
        }, 100);
    }


    /**
     * 打开新的Fragment
     *
     * @param args
     * @param targetFr
     */
    public static void showFragment(FragmentManager fm, Bundle args, Fragment targetFr) {
        int contentId = R.id.full_screen_stub;
        showFragment(contentId, fm, args, targetFr);
    }


    /**
     * 打开新的Fragment
     *
     * @param fr
     * @param args
     * @param targetFr
     */
    public static void showFragment(int contentId, Fragment fr, Bundle args, Fragment targetFr) {
        FragmentManager fm = fr.getActivity().getSupportFragmentManager();
        showFragment(contentId, fm, args, targetFr);
    }

    /**
     * 得到控制器
     *
     * @param fm
     * @param args
     * @param targetFr
     */
    public static void showFragment(int contentId, FragmentManager fm, Bundle args, Fragment targetFr) {
        FragmentTransaction ft = fm.beginTransaction();
        targetFr.setArguments(args);
        ft.setCustomAnimations(R.anim.fragment_slide_left_enter, R.anim.fragment_slide_left_exit, R.anim.fragment_slide_right_enter, R.anim.fragment_slide_right_exit);
        ft.add(contentId, targetFr, targetFr.getClass().getName());
        ft.addToBackStack(null);
        ft.commit();
    }

    /**
     * 显示默认等待对话框
     *
     * @param activity
     * @return
     */
    public static Dialog showWaitingDialog(Context activity) {
        Dialog dialog = new Dialog(activity, android.R.style.Theme_DeviceDefault_Light_Dialog);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.setContentView(R.layout.dialog_waiting);
        dialog.show();
        return dialog;
    }

    /**
     * 显示自定义等待对话框
     *
     * @param activity
     * @param text
     * @return
     */
    public static Dialog showWaitingDialog(Context activity, String text) {
        Dialog dialog = new Dialog(activity, android.R.style.Theme_DeviceDefault_Light_Dialog);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.setContentView(R.layout.dialog_waiting);
        ((TextView) dialog.findViewById(R.id.waiting_text)).setText(text);
        dialog.show();
        return dialog;
    }

    /**
     * 得到浅色背景，黑色文字主题
     *
     * @param context
     * @return
     */
    public static LayoutInflater getLightThemeInflater(Context context) {
        final Context contextThemeWrapper = new ContextThemeWrapper(context, android.R.style.Theme_DeviceDefault_Light_Dialog);
        return LayoutInflater.from(context).cloneInContext(contextThemeWrapper);
    }

    /**
     * 得到浅色主题的View
     *
     * @param context
     * @param resourceId
     * @return
     */
    public static View getLightThemeView(Context context, int resourceId) {
        LayoutInflater inflater = getLightThemeInflater(context);
        return inflater.inflate(resourceId, null);
    }

    /**
     * ScrollView嵌套ExpandableListView
     *
     * @param listView
     */
    public static void setListViewHeightBasedOnChildren(ExpandableListView listView) {
        ExpandableListAdapter listAdapter = listView.getExpandableListAdapter();
        if (listAdapter == null) {
            // pre-condition
            return;
        }

        int totalHeight = 0;
        for (int i = 0; i < listAdapter.getGroupCount(); i++) {
            View listItem = listAdapter.getGroupView(i, false, null, listView);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getGroupCount() - 1));
        listView.setLayoutParams(params);
    }

    public static void setListViewHeightBasedOnChildren(ExpandableListView listView, int addHeight) {
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height += addHeight;
        System.out.println("-------------原始" + params.height);
        listView.setLayoutParams(params);
    }

    public static void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            // pre-condition
            return;
        }

        int totalHeight = 0;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            View listItem = listAdapter.getView(i, null, listView);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
    }

    /**
     * 修改给定控件子元素字体颜色
     *
     * @param childType
     * @param source
     */
    public static void changeChildColor(String childType, Object source, int color) {
        Field[] fieldsOfDatePicker = DatePicker.class.getDeclaredFields();
        // 获取DatePicker中的属性
        for (Field fieldOfDatePicker : fieldsOfDatePicker) {
            fieldOfDatePicker.setAccessible(true);
            if (fieldOfDatePicker.getType().getSimpleName().equals(childType)) {
                try {
                    NumberPicker numberPicker = (NumberPicker) fieldOfDatePicker.get(source);

                    // 获取NumberPicker中的属性
                    if (numberPicker != null) {
                        Field[] fieldsOfNumberPicker = numberPicker.getClass().getDeclaredFields();
                        for (Field fieldOfNumberPicker : fieldsOfNumberPicker) {
                            fieldOfNumberPicker.setAccessible(true);
                            if (fieldOfNumberPicker.getType().getName().equals(EditText.class.getName())) {
                                EditText editText = (EditText) fieldOfDatePicker.get(numberPicker);
                                if (editText != null)
                                    editText.setTextColor(color);
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 设置NumberPicker编辑框字体颜色
     *
     * @param viewGroup
     * @param color
     */
    public static void setNumberPickerColor(ViewGroup viewGroup, int color) {
        List<NumberPicker> npList = findNumberPicker(viewGroup);
        if (null != npList) {
            for (NumberPicker np : npList) {
                EditText et = findEditText(np);
                // et.setFocusable(false);
                et.setGravity(Gravity.CENTER);
                // et.setTextSize(10);
                et.setTextColor(color);
            }
        }
    }

    /**
     * 获得NumberPicker
     *
     * @param viewGroup
     * @return
     */
    public static List<NumberPicker> findNumberPicker(ViewGroup viewGroup) {
        List<NumberPicker> npList = new ArrayList<NumberPicker>();
        View child = null;

        if (null != viewGroup) {
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                child = viewGroup.getChildAt(i);
                if (child instanceof NumberPicker) {
                    npList.add((NumberPicker) child);
                } else if (child instanceof LinearLayout) {
                    List<NumberPicker> result = findNumberPicker((ViewGroup) child);
                    if (result.size() > 0)
                        return result;
                }
            }
        }
        return npList;
    }

    /**
     * 找到NumberPicker中的编辑框
     *
     * @param np
     * @return
     */
    public static EditText findEditText(NumberPicker np) {
        if (null != np) {
            for (int i = 0; i < np.getChildCount(); i++) {
                View child = np.getChildAt(i);
                if (child instanceof EditText)
                    return (EditText) child;
            }
        }
        return null;
    }

    /**
     * 位图到字节数组
     *
     * @param bm
     * @return
     */
    public static byte[] Bitmap2Bytes(Bitmap bm) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
        return baos.toByteArray();
    }

    public static final String[] LOGOS = {"com.uhicare.patient.ui.guidAndLoginPages.SplashActivity_0", "com.uhicare.patient.ui.guidAndLoginPages.SplashActivity_1",
            "com.uhicare.patient.ui.guidAndLoginPages.SplashActivity_2", "com.uhicare.patient.ui.guidAndLoginPages.SplashActivity_3",
            "com.uhicare.patient.ui.guidAndLoginPages.SplashActivity_4", "com.uhicare.patient.ui.guidAndLoginPages.SplashActivity_5"};

    public static void setAppLogo(String activityAlias) {
        Context ctx = MainApplication.INSTANCE;
        PackageManager pm = ctx.getPackageManager();
        ActivityManager am = (ActivityManager) ctx.getSystemService(Activity.ACTIVITY_SERVICE);

        // Enable/disable activity-aliases
        for (int i = 0; i < LOGOS.length; i++) {
            pm.setComponentEnabledSetting(new ComponentName(ctx, LOGOS[i]), LOGOS[i].equals(activityAlias) ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                    : PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
        }

        // Find launcher and kill it
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        List<ResolveInfo> resolves = pm.queryIntentActivities(intent, 0);
        for (ResolveInfo res : resolves) {
            if (res.activityInfo != null) {
                am.killBackgroundProcesses(res.activityInfo.packageName);
            }
        }
    }

    public static Dialog showReadOnlyDialog(final FragmentActivity activity, final String title, final String content, int style) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        final AlertDialog dialog = builder.create();
        View dialogView = UiHelper.getLightThemeInflater(activity).inflate(R.layout.dialog_text_readonly, null);
        dialog.setView(dialogView, 0, 0, 0, 0);
        TextView textView;

        TextView textVeiwTitle = (TextView) dialogView.findViewById(R.id.tv_title);
        textVeiwTitle.setText(title);

        if (style == 0) {
            textView = (TextView) dialogView.findViewById(R.id.tv_short);
        } else {
            textView = (TextView) dialogView.findViewById(R.id.tv_long);
        }
        textView.setVisibility(View.VISIBLE);
        textView.setText(content);

        dialogView.findViewById(R.id.btn_ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View button) {
                dialog.dismiss();
            }
        });
        dialog.show();
        return dialog;
    }
}