package net.oschina.app.ui;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ProgressBar;
import net.oschina.app.common.ActionBarUtil;
import net.oschina.app.inteface.ActionBarProgressBarVisibility;

public abstract class CustomActivity extends BaseActionBarActivity implements ActionBarProgressBarVisibility {	
	private ActionBar mActionBar;
	private ProgressBar mProgressBar;
	private ImageView refreshButton;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setActionBar();
	}
	
	private void setActionBar(){
		mActionBar = getSupportActionBar();
		mActionBar.setDisplayHomeAsUpEnabled(true);
		mActionBar.setHomeButtonEnabled(true);
		mActionBar.setDisplayShowCustomEnabled(true);
		mProgressBar  = ActionBarUtil.getProgressBar(CustomActivity.this);
		refreshButton = ActionBarUtil.getRefreshButton(CustomActivity.this);
		refreshButton.setOnClickListener(refreshClickListener);
		mActionBar.setCustomView(refreshButton, ActionBarUtil.getActionViewLayoutParams());
	}
	
	public void setProgressBarVisibility(int isVisible){
		if(View.VISIBLE == isVisible)
			mActionBar.setCustomView(mProgressBar, ActionBarUtil.getActionViewLayoutParams());
		else
			mActionBar.setCustomView(refreshButton, ActionBarUtil.getActionViewLayoutParams());
	}
	
	private View.OnClickListener refreshClickListener = new OnClickListener() {
		
		public void onClick(View v) {
			onClickRefresh();
		}
	};
	
	protected abstract void onClickRefresh();
	
}
