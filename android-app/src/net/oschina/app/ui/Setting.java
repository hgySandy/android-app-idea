package net.oschina.app.ui;


import net.oschina.app.fragment.SettingFragment;
import net.oschina.designapp.R;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;

public class Setting extends BaseActionBarActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.setting);
		
		ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayShowHomeEnabled(true);
		actionBar.setHomeButtonEnabled(true);
		actionBar.setTitle(R.string.setting_head_title);
		
		FragmentManager fManager = getSupportFragmentManager();
		FragmentTransaction fTransaction = fManager.beginTransaction();
		fTransaction.add(R.id.setting_layout,new SettingFragment()).commit();
	}
}
