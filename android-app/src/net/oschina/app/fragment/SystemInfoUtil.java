package net.oschina.app.fragment;

import android.app.Activity;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.Surface;

public class SystemInfoUtil {
	
	public static int getWidthPixels(Context context){
		DisplayMetrics dm = new DisplayMetrics();
		((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(dm);
		// 获取屏幕的宽度
		return dm.widthPixels;
	}
	
	public static int getHeightPixels(Context context){
		DisplayMetrics dm = new DisplayMetrics();
		((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(dm);
		// 获取屏幕的高度
		return dm.heightPixels;
	}
	
	@SuppressWarnings("deprecation")
	public static int getOrientation(Context context){
		return ((Activity) context).getWindowManager().getDefaultDisplay().getOrientation();
	}
}
