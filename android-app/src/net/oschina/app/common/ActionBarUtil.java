package net.oschina.app.common;

import net.oschina.designapp.R;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.ProgressBar;

public class ActionBarUtil {
	
	public static final int PADDING_VERCATIL   = DipUtil.dpChangePix(8);
	public static final int PADDING_HORIZONTAL = DipUtil.dpChangePix(12);
	public static final int BUTTON_WIDTH  = DipUtil.dpChangePix(56);
	public static final int BUTTON_HEIGTH = DipUtil.dpChangePix(48);
	
	public ActionBarUtil(){
		
	}
	/**
	 * 
	 * @param activity ActionBarActivity 在ActionBar上设置一个圆形的不确定进度条。
	 * @return ProgressBar 进度条的引用
	 */
	public static ProgressBar getProgressBar(ActionBarActivity activity){
		ActionBar actionBar = activity.getSupportActionBar();
		actionBar.setDisplayShowCustomEnabled(true);
		ProgressBar progressBar = new ProgressBar(activity);
		progressBar.setPadding(
				PADDING_HORIZONTAL,
				PADDING_VERCATIL,
				PADDING_HORIZONTAL,
				PADDING_VERCATIL);
		progressBar.setIndeterminate(true);
		actionBar.setCustomView(progressBar, getActionViewLayoutParams());
		return progressBar;
	}
	/**
	 * 
	 * @param activity ActionBarActivity 在ActionBar上设置一个刷新图标的按钮。
	 * @return ImageView 按钮的引用
	 */
	public static ImageView getRefreshButton(ActionBarActivity activity){
		ActionBar actionBar = activity.getSupportActionBar();
		ImageView refreshButton = new ImageView(activity);
		refreshButton.setPadding(DipUtil.dpChangePix(4), 0, DipUtil.dpChangePix(4), 0);
		refreshButton.setBackgroundResource(R.drawable.but_actionbar_selector);
		refreshButton.setImageResource(R.drawable.ic_action_navigation_refresh);
		actionBar.setCustomView(refreshButton, getActionViewLayoutParams());
		return refreshButton;
	}
	
	public static ActionBar.LayoutParams getActionViewLayoutParams(){
		return new ActionBar.LayoutParams(
				BUTTON_WIDTH,
				BUTTON_HEIGTH,
				Gravity.CENTER | Gravity.RIGHT);
	}
}
