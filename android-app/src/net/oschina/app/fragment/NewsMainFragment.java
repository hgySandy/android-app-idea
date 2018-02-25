package net.oschina.app.fragment;

import java.util.ArrayList;
import java.util.List;


import net.oschina.app.bean.BlogList;
import net.oschina.app.bean.NewsList;
import net.oschina.app.common.UIHelper;
import net.oschina.app.widget.TabButton;
import net.oschina.designapp.R;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;


public class NewsMainFragment extends MainFragment {
	
	PagerAdapter getPagerAdapter() {
		List<Fragment> fragments = new ArrayList<Fragment>();
		List<CharSequence> titles = new ArrayList<CharSequence>();
		
		String allTitle = getString(R.string.frame_title_news_lastest);
		String latestTitle = getString(R.string.frame_title_news_blog);
		String recommendTitle = getString(R.string.frame_title_news_recommend);
		
		Fragment news = addBundle(new NewsFragment(), NewsList.CATALOG_ALL);
		fragments.add(news);
		titles.add(allTitle);
		
		Fragment newBlog = addBundle(new BlogFragment(), BlogList.CATALOG_LATEST);
		fragments.add(newBlog);
		titles.add(latestTitle);
		
		Fragment reBlog = addBundle(new BlogFragment(), BlogList.CATALOG_RECOMMEND);
		fragments.add(reBlog);
		titles.add(recommendTitle);
		
		return new ActivePagerAdapter(getChildFragmentManager(), fragments, titles);
	}

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.news_fragment_menu, menu);
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);
		switch (item.getItemId()) {
		case R.id.news_fragment_search:
			UIHelper.showNewSearch(getActivity());
			return true;

		default:
			return false;
		}
	}
	
}
