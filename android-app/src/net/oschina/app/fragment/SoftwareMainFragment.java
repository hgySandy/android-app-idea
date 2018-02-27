package net.oschina.app.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import net.oschina.app.bean.BlogList;
import net.oschina.app.bean.NewsList;
import net.oschina.app.bean.SoftwareList;
import net.oschina.app.common.UIHelper;
import net.oschina.app.ui.SoftwareLib;
import net.oschina.designapp.R;

import java.util.ArrayList;
import java.util.List;


public class SoftwareMainFragment extends MainFragment {
	
	PagerAdapter getPagerAdapter() {
		List<Fragment> fragments = new ArrayList<Fragment>();
		List<CharSequence> titles = new ArrayList<CharSequence>();

		String recommendTitle = getString(R.string.software_lib_title_recommend); //推荐
		String latestTitle = getString(R.string.software_lib_lastestsoft); //最新
		String hotTitle = getString(R.string.software_lib_hotsoft); //热门
		String chinaTitle = getString(R.string.software_lib_title_china); //国产



		Fragment recommendSoftware = addBundle(new SoftwareFragment(), SoftwareList.HEAD_TAG_RECOMMEND);
		fragments.add(recommendSoftware);
		titles.add(recommendTitle);

		Fragment latestSoftware = addBundle(new SoftwareFragment(), SoftwareList.HEAD_TAG_LASTEST);
		fragments.add(latestSoftware);
		titles.add(latestTitle);

		Fragment hotSoftware = addBundle(new SoftwareFragment(), SoftwareList.HEAD_TAG_HOT);
		fragments.add(hotSoftware);
		titles.add(hotTitle);

		Fragment chinaSoftware = addBundle(new SoftwareFragment(), SoftwareList.HEAD_TAG_RECOMMEND);
		fragments.add(chinaSoftware);
		titles.add(chinaTitle);



		return new ActivePagerAdapter(getChildFragmentManager(), fragments, titles);
	}

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(false);
	}

}
