package net.oschina.app.fragment;

import java.util.ArrayList;
import java.util.List;


import net.oschina.app.bean.NewsList;
import net.oschina.app.bean.TweetList;
import net.oschina.app.common.UIHelper;
import net.oschina.designapp.R;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class TweetMainFragment extends MainFragment {

	PagerAdapter getPagerAdapter() {
		List<Fragment> fragments = new ArrayList<Fragment>();
		List<CharSequence> titles = new ArrayList<CharSequence>();
		
		String lastestTitle = getString(R.string.frame_title_tweet_lastest);
		String hotTitle     = getString(R.string.frame_title_tweet_hot);
		String myTitle      = getString(R.string.frame_title_tweet_my);
		
		Fragment newTweet = addBundle(new NewsTweetFragment(), TweetList.CATALOG_LASTEST);
		fragments.add(newTweet);
		titles.add(lastestTitle);
		
		Fragment hotTweet = addBundle(new NewsTweetFragment(), TweetList.CATALOG_HOT);
		fragments.add(hotTweet);
		titles.add(hotTitle);
		
		fragments.add(new UserTweetFragment());
		titles.add(myTitle);
		
		return new ActivePagerAdapter(getChildFragmentManager(), fragments, titles);
	}

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.tweet_fragment_menu, menu);
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);
		switch (item.getItemId()) {
		case R.id.tweet_fragment_put:
			UIHelper.showNewTweetPub(getActivity());
			return true;

		default:
			return false;
		}
	}
}
