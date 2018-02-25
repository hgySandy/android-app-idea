package net.oschina.app.inteface;

import android.support.v7.app.ActionBar.Tab;

public interface ActionBarTabListener {
	void addTab(Tab tab);
	void removeTab(Tab tab);
	void removeAllTabs();
}
