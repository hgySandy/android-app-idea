package net.oschina.app.fragment;

import java.util.ArrayList;
import java.util.List;


import net.oschina.app.bean.PostList;
import net.oschina.app.common.UIHelper;
import net.oschina.designapp.R;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class QuestionMainFragment extends MainFragment {
	public static final String TAG = "QuestionMainFragment";
	
	PagerAdapter getPagerAdapter() {
		List<Fragment> list = new ArrayList<Fragment>();
		List<CharSequence> titles = new ArrayList<CharSequence>();
		
		String askTitle   = getString(R.string.frame_title_question_ask);
		String shareTitle = getString(R.string.frame_title_question_share);
		String otherTitle = getString(R.string.frame_title_question_other);
		String jobTitle   = getString(R.string.frame_title_question_job);
		String siteTitle  = getString(R.string.frame_title_question_site);
		
		Fragment ask   = addBundle(
				new QuestionFragment(), PostList.CATALOG_ASK);
		list.add(ask);
		titles.add(askTitle);
		
		Fragment share = addBundle(
				new QuestionFragment(), PostList.CATALOG_SHARE);
		list.add(share);
		titles.add(shareTitle);
		
		Fragment other = addBundle(
				new QuestionFragment(), PostList.CATALOG_OTHER);
		list.add(other);
		titles.add(otherTitle);
		
		Fragment job   = addBundle(
				new QuestionFragment(), PostList.CATALOG_JOB);
		list.add(job);
		titles.add(jobTitle);
		
		Fragment site  = addBundle(
				new QuestionFragment(), PostList.CATALOG_SITE);
		list.add(site);
		titles.add(siteTitle);
		
		return new ActivePagerAdapter(getChildFragmentManager(), list, titles);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.question_fragment_menu, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);
		switch (item.getItemId()) {
		case R.id.question_fragment_put:
			UIHelper.showNewQuestionPub(getActivity());
			return true;

		default:
			return false;
		}
	}

}
