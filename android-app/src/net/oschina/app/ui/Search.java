package net.oschina.app.ui;

import java.util.ArrayList;
import java.util.List;


import net.oschina.app.bean.NewsList;
import net.oschina.app.bean.SearchList;
import net.oschina.app.common.ActionBarUtil;
import net.oschina.app.common.DipUtil;
import net.oschina.app.common.UIHelper;
import net.oschina.app.fragment.SearchListFragment;
import net.oschina.app.fragment.SearchListFragment.SearchContextListener;
import net.oschina.app.inteface.ActionBarProgressBarVisibility;
import net.oschina.app.widget.TabButton;
import net.oschina.designapp.R;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

/**
 * 搜索
 * @author liux (http://my.oschina.net/liux)
 * @version 1.0
 * @created 2012-3-21
 */
public class Search extends BaseActionBarActivity implements ActionBarProgressBarVisibility, SearchContextListener{
	private ViewPager mViewPager;
	private List<SearchListFragment> fragments;
	private Button mSearchBtn;
	private EditText mSearchEditer;
	private ProgressBar mProgressBar;
    
	private InputMethodManager imm;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search);
        this.initActionBar();
        this.initView();
    }
	
	private void initActionBar(){
		ActionBar actionBar = getSupportActionBar();
		actionBar.setTitle(R.string.search_title);
		actionBar.setDisplayShowHomeEnabled(true);
		actionBar.setHomeButtonEnabled(true);
		mProgressBar = ActionBarUtil.getProgressBar(Search.this);
		mProgressBar.setVisibility(View.GONE);
	}
	
	//初始化视图控件
    private void initView()
    {
    	TabButton tabsButton = (TabButton) findViewById(R.id.search_tabs);
        mViewPager = (ViewPager) findViewById(R.id.search_viewpager);
        mViewPager.setAdapter(getAdapter());
 		tabsButton.setViewPager(mViewPager);
    	
    	imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
    	
    	mSearchBtn = (Button)findViewById(R.id.search_btn);
    	mSearchEditer = (EditText)findViewById(R.id.search_editer);
    	
    	mSearchBtn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				//开始搜索
				mSearchEditer.clearFocus();
				closeInputMethod();
				int itemId = mViewPager.getCurrentItem();
				SearchListFragment fragment = fragments.get(itemId);
				fragment.search(UIHelper.LISTVIEW_ACTION_INIT);
			}
		});
    	mSearchEditer.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			public void onFocusChange(View v, boolean hasFocus) {
				if(hasFocus){  
					imm.showSoftInput(v, 0);  
		        }  
		        else{  
		            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);  
		        }  
			}
		}); 
    	mSearchEditer.setOnKeyListener(new View.OnKeyListener() {
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_SEARCH) {
					if(v.getTag() == null) {
						v.setTag(1);
						mSearchEditer.clearFocus();
						int itemId = mViewPager.getCurrentItem();
						SearchListFragment fragment = fragments.get(itemId);
						fragment.search(UIHelper.LISTVIEW_ACTION_INIT);				
					}else{
						v.setTag(null);
					}
					return true;
				}
				return false;
			}
		});
    	
    }
    
    private FragmentPagerAdapter getAdapter(){
		fragments = new ArrayList<SearchListFragment>();
		List<CharSequence> titles = new ArrayList<CharSequence>();
		
		String softwareTitle = getString(R.string.search_title_software);
		String questionTitle = getString(R.string.search_title_question);
		String blogTitle = getString(R.string.search_title_blog);
		String newTitle = getString(R.string.search_title_new);
		String codeTitle = getString(R.string.search_title_code);
		
		SearchListFragment software = addBundle(new SearchListFragment(), SearchList.CATALOG_SOFTWARE);
		fragments.add(software);
		titles.add(softwareTitle);
		
		SearchListFragment question = addBundle(new SearchListFragment(), SearchList.CATALOG_POST);
		fragments.add(question);
		titles.add(questionTitle);
		
		SearchListFragment blog = addBundle(new SearchListFragment(), SearchList.CATALOG_BLOG);
		fragments.add(blog);
		titles.add(blogTitle);
		
		SearchListFragment news = addBundle(new SearchListFragment(), SearchList.CATALOG_NEWS);
		fragments.add(news);
		titles.add(newTitle);
		
		SearchListFragment code = addBundle(new SearchListFragment(), SearchList.CATALOG_CODE);
		fragments.add(code);
		titles.add(codeTitle);
		return new SearchPagerAdapter(getSupportFragmentManager(), fragments, titles);
	}
	
    private SearchListFragment addBundle(SearchListFragment fragment, String catlog){
		Bundle bundle = new Bundle();
		bundle.putString(NewsList.CATLOG, catlog);
		fragment.setArguments(bundle);
		return fragment;
	}
    
	private void closeInputMethod(){
		imm.hideSoftInputFromWindow(mSearchEditer.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
	}
	
	/**
     * ActionBar上的圆形进度条显示与隐藏
     * @param type
     */
	@Override
	public void setProgressBarVisibility(int v) {
		switch (v) {
    	case View.VISIBLE:
    		mSearchBtn.setClickable(false);
			mProgressBar.setVisibility(View.VISIBLE);
			break;
			
    	case View.INVISIBLE:
		case View.GONE:
			mSearchBtn.setClickable(true);
			mProgressBar.setVisibility(View.GONE);
			break;
		}
	}
	
	class SearchPagerAdapter extends FragmentPagerAdapter {
		private List<SearchListFragment> fragments;
		private List<CharSequence> titles;
		
		public SearchPagerAdapter(FragmentManager fm, List<SearchListFragment> fragments, List<CharSequence> titles) {
			super(fm);
			this.fragments = fragments;
			this.titles = titles;
		}

		public Fragment getItem(int arg0) {
			return fragments.get(arg0);
		}

		public int getCount() {
			return fragments.size();
		}

		public CharSequence getPageTitle(int position) {
			return position<titles.size() ? titles.get(position) : "";
		}
		
		
	}

	@Override
	public String getSearchContext() {
		return mSearchEditer.getText().toString();
	}
	
}
