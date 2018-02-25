package net.oschina.app.fragment;

import java.util.ArrayList;
import java.util.List;

import net.oschina.app.bean.ActiveList;
import net.oschina.app.bean.NewsList;
import net.oschina.app.common.ActionBarUtil;
import net.oschina.app.common.BadgeManager;
import net.oschina.app.ui.CustomActivity;
import net.oschina.app.widget.TabButton;
import net.oschina.app.widget.TabButton.TabsButtonOnClickListener;
import net.oschina.designapp.R;
import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ProgressBar;

public class ActiveMainFragment extends Fragment {

	private ViewPager viewPager;
	private TabButton tabsButton;
	
	private String lastestTitle;
	private String atmeTitle;
	private String commenttTitle;
	private String myselfTitle;
	private String messageTitle;
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_main, container, false);
		viewPager = (ViewPager) view.findViewById(R.id.fragment_main_viewpager);
		tabsButton = (TabButton) view.findViewById(R.id.fragment_main_tabsbutton);
		tabsButton.setTabsButtonOnClickListener(onClickListener);
		FragmentPagerAdapter adapter = getPagerAdapter();
		viewPager.setAdapter(adapter);
		viewPager.setOnPageChangeListener(tabsButton.getOnPageChangeListener());
		
		initTab();
		return view;
	}
	
	private FragmentPagerAdapter getPagerAdapter() {
		List<Fragment> fragments = new ArrayList<Fragment>();
		List<CharSequence> titles = new ArrayList<CharSequence>();
		
		lastestTitle  = getString(R.string.frame_title_active_lastest);
		atmeTitle     = getString(R.string.frame_title_active_atme);
		commenttTitle = getString(R.string.frame_title_active_comment);
		myselfTitle   = getString(R.string.frame_title_active_myself);
		messageTitle  = getString(R.string.frame_title_active_message);

		Fragment newActiveFragment = addBundle(
				new ActiveFragment(), ActiveList.CATALOG_LASTEST);
		fragments.add(newActiveFragment);
		titles.add(lastestTitle);

		Fragment atmeFragment = addBundle(
				new ActiveFragment(), ActiveList.CATALOG_ATME);
		fragments.add(atmeFragment);
		titles.add(atmeTitle);

		Fragment commentFragment = addBundle(
				new ActiveFragment(), ActiveList.CATALOG_COMMENT);
		fragments.add(commentFragment);
		titles.add(commenttTitle);

		Fragment myFragment = addBundle(
				new ActiveFragment(), ActiveList.CATALOG_MYSELF);
		fragments.add(myFragment);
		titles.add(myselfTitle);

		MessageFragment messageFragment = new MessageFragment();
		fragments.add(messageFragment);
		titles.add(messageTitle);
		
		return new ActivePagerAdapter(getChildFragmentManager(), fragments, titles);
	}
	
	private void initTab() {
		View lastest = tabsButton.newTextTab(lastestTitle);
		View atme    = tabsButton.newTextTab(atmeTitle);
		View comment = tabsButton.newTextTab(commenttTitle);
		View myself  = tabsButton.newTextTab(myselfTitle);
		View message = tabsButton.newTextTab(messageTitle);
		
		tabsButton.addTab(lastest);
		tabsButton.addTab(atme);
		tabsButton.addTab(comment);
		tabsButton.addTab(myself);
		tabsButton.addTab(message);

		BadgeManager bManager = BadgeManager.getInstance();
		bManager.setAtme(getActivity(), atme);
		bManager.setComment(getActivity(), comment);
		bManager.setMessage(getActivity(), message);
	}
	
	private Fragment addBundle(Fragment fragment, int catlog){
		Bundle bundle = new Bundle();
		bundle.putInt(NewsList.CATLOG, catlog);
		fragment.setArguments(bundle);
		return fragment;
	}
	
	private TabsButtonOnClickListener onClickListener = new TabsButtonOnClickListener() {
		
		public void tabsButtonOnClick(int id, View v) {
			Log.e("setCurrentItem", ""+id);
			viewPager.setCurrentItem(id);
		}
	};
	
}
