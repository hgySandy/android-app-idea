package net.oschina.app.widget;

import net.oschina.app.inteface.FooterViewVisibility;
import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ScrollView;

public class ExtentScrollView extends ScrollView {

	private static final String TAG = "ExtentScrollView";
	
	private FooterViewVisibility footView;
	private boolean overScrollInfo = true;
	
	public ExtentScrollView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	
	public ExtentScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}
	
	public ExtentScrollView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}
	
	public FooterViewVisibility getFootView() {
		return footView;
	}

	public void setFootView(FooterViewVisibility footView) {
		this.footView = footView;
	}
	/**
	 * 是否关闭边界反弹效果。
	 * @param Enabled
	 */
	public void setOverScrollInfoEnabled(boolean Enabled){
		overScrollInfo = Enabled;
	}
	public boolean getOverScrollInfo(){
		return overScrollInfo;
	}
	
	@SuppressLint("NewApi")
	protected boolean overScrollBy(int deltaX, int deltaY, int scrollX, int scrollY, int scrollRangeX, int scrollRangeY, int maxOverScrollX, int maxOverScrollY, boolean isTouchEvent) {
		
		return super.overScrollBy(deltaX,
				deltaY,
				scrollX,
				scrollY,
				scrollRangeX,
				scrollRangeY,
				overScrollInfo ? maxOverScrollX : 0,
				overScrollInfo ? maxOverScrollY : 0,
				isTouchEvent);
	}
	
	private int tempY=0;
	
	protected void onScrollChanged(int l, int t, int oldl, int oldt) {
		int intY = (int) getScrollY();
		if (footView != null && getChildAt(0).getMeasuredHeight() <= getHeight()+intY)
			footView.setFooterViewVisibility(View.VISIBLE);
		// 向上滑动的时候显示
		else if (footView!=null && tempY-intY > 10 ) {
			Log.d("onScrollChanged", "VISIBLE");
			Log.d(TAG, "tempY: "+tempY);
			tempY = intY;
			footView.setFooterViewVisibility(View.VISIBLE);
		}
		// 向下滑动的时候隐藏
		else if (footView!=null && tempY-intY < -10 ) {
			Log.d("onScrollChanged", "GONE");
			Log.d(TAG, "tempY: "+tempY);
        	tempY = intY;
			footView.setFooterViewVisibility(View.GONE);
		}
		super.onScrollChanged(l, t, oldl, oldt);
	}
}
