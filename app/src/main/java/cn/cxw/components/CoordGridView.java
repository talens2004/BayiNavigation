package cn.cxw.components;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;

import com.baidu.mapapi.model.LatLng;



/**
 * 自定义的ImageView，用于绘制坐标网格
 *
 * @author guolin
 */
public class CoordGridView extends android.support.v7.widget.AppCompatImageView {

    public int width;
    public int height;

    public int level;
    public LatLng centerLatLng;

	/**
	 * ZoomImageView构造函数，将当前操作状态设为STATUS_INIT。
	 * 
	 * @param context
	 * @param attrs
	 */
	public CoordGridView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}


	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		if (changed) {
			// 分别获取到ZoomImageView的宽度和高度
			width = getWidth();
			height = getHeight();
		}
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);                    //设置画笔颜色
        canvas.drawColor(Color.WHITE);                  //设置背景颜色
        paint.setStrokeWidth((float) 1.0);              //设置线宽
        canvas.drawLine(0, height / 2, width, height / 2, paint);        //绘制直线

	}


}