package net.oschina.app.ui;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;


import net.oschina.app.AppContext;
import net.oschina.app.AppException;
import net.oschina.app.adapter.ListViewSoftwareAdapter;
import net.oschina.app.adapter.ListViewSoftwareCatalogAdapter;
import net.oschina.app.bean.Notice;
import net.oschina.app.bean.SoftwareCatalogList;
import net.oschina.app.bean.SoftwareList;
import net.oschina.app.bean.SoftwareCatalogList.SoftwareType;
import net.oschina.app.bean.SoftwareList.Software;
import net.oschina.app.common.ActionBarUtil;
import net.oschina.app.common.DipUtil;
import net.oschina.app.common.StringUtils;
import net.oschina.app.common.UIHelper;
import net.oschina.app.widget.PullToRefreshListView;
import net.oschina.app.widget.ScrollLayout;
import net.oschina.designapp.R;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBar.Tab;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * 软件库
 * @author liux (http://my.oschina.net/liux)
 * @version 1.0
 * @created 2012-3-21
 */
public class SoftwareLib extends BaseActionBarActivity{
	
	private ActionBar mActionBar;
	private ProgressBar mProgressBar;
	private ScrollLayout mScrollLayout;	
	
	private PullToRefreshListView mlvSoftware;
	private ListViewSoftwareAdapter lvSoftwareAdapter;
	private List<Software> lvSoftwareData = new ArrayList<Software>();
	private View lvSoftware_footer;
	private TextView lvSoftware_foot_more;
    private Handler mSoftwareHandler;
    private int lvSumData;
	
	private ListView mlvSoftwareCatalog;
	private ListViewSoftwareCatalogAdapter lvSoftwareCatalogAdapter;
	private List<SoftwareType> lvSoftwareCatalogData = new ArrayList<SoftwareType>();
    private Handler mSoftwareCatalogHandler;
    
	private ListView mlvSoftwareTag;
	private ListViewSoftwareCatalogAdapter lvSoftwareTagAdapter;
	private List<SoftwareType> lvSoftwareTagData = new ArrayList<SoftwareType>();
    private Handler mSoftwareTagHandler;
    
	private int curHeadTag = HEAD_TAG_CATALOG;//默认初始头部标签
	private int curScreen = SCREEN_CATALOG;//默认当前屏幕
	private int curSearchTag;//当前二级分类的Tag
	private int curLvSoftwareDataState;
	private String curTitleLV1;//当前一级分类标题
    
	private final static int HEAD_TAG_CATALOG = 0x001;
	private final static int HEAD_TAG_RECOMMEND = 0x002;
	private final static int HEAD_TAG_LASTEST = 0x003;
	private final static int HEAD_TAG_HOT = 0x004;
	private final static int HEAD_TAG_CHINA = 0x005;
	
	private final static int DATA_LOAD_ING = 0x001;
	private final static int DATA_LOAD_COMPLETE = 0x002;
	
	private final static int SCREEN_CATALOG = 0;
	private final static int SCREEN_TAG = 1;
	private final static int SCREEN_SOFTWARE = 2;
	
	private SparseArray<String> longTitle;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.frame_software);
        
        this.initActionBar();
        this.initLongTitle();
        this.initView();
        this.initData();
        this.initTabs();
	}
	
	//初始化动作栏
	private void initActionBar(){
		mActionBar = getSupportActionBar();
		mActionBar.setTitle(R.string.software_lib_title);
		mActionBar.setDisplayShowHomeEnabled(true);
		mActionBar.setHomeButtonEnabled(true);
		mProgressBar = ActionBarUtil.getProgressBar(SoftwareLib.this);
		mProgressBar.setVisibility(View.GONE);
	}
	
	private void initTabs(){
		mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		Tab catalog   = mActionBar.newTab()
				.setTag(HEAD_TAG_CATALOG)
				.setText(R.string.software_lib_title_catalog)
				.setTabListener(tabListener);
		Tab recommend = mActionBar.newTab()
				.setTag(HEAD_TAG_RECOMMEND)
				.setText(R.string.software_lib_title_recommend)
				.setTabListener(tabListener);
		Tab lastest   = mActionBar.newTab()
				.setTag(HEAD_TAG_LASTEST)
				.setText(R.string.software_lib_title_lastest)
				.setTabListener(tabListener);
		Tab hot 	  = mActionBar.newTab()
				.setTag(HEAD_TAG_HOT)
				.setText(R.string.software_lib_title_hot)
				.setTabListener(tabListener);
		Tab china 	  = mActionBar.newTab()
				.setTag(HEAD_TAG_CHINA)
				.setText(R.string.software_lib_title_china)
				.setTabListener(tabListener);
		
		mActionBar.addTab(catalog);
		mActionBar.addTab(recommend);
		mActionBar.addTab(lastest);
		mActionBar.addTab(hot);
		mActionBar.addTab(china);
	}
	
	private void initLongTitle(){
		longTitle = new SparseArray<String>();
		longTitle.put(HEAD_TAG_CATALOG,   getString(R.string.software_lib_title));
		longTitle.put(HEAD_TAG_RECOMMEND, getString(R.string.software_lib_weekly));
		longTitle.put(HEAD_TAG_LASTEST,	  getString(R.string.software_lib_lastestsoft));
		longTitle.put(HEAD_TAG_HOT, 	  getString(R.string.software_lib_hotsoft));
		longTitle.put(HEAD_TAG_CHINA, 	  getString(R.string.software_lib_title));
	}
	
	private ActionBar.TabListener tabListener = new ActionBar.TabListener() {
		
		@Override
		public void onTabUnselected(Tab arg0, FragmentTransaction arg1) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void onTabSelected(Tab arg0, FragmentTransaction arg1) {
			curHeadTag = (Integer) arg0.getTag();	
	    	
	    	if(HEAD_TAG_CATALOG == curHeadTag)
	    	{			    		
	    		curScreen = SCREEN_CATALOG;
	    		if(lvSoftwareCatalogData.size() == 0)
	    			loadLvSoftwareCatalogData(0, mSoftwareCatalogHandler, UIHelper.LISTVIEW_ACTION_CHANGE_CATALOG);		
	    	}
	    	else
	    	{		    		
	    		curScreen = SCREEN_SOFTWARE;
	    		loadLvSoftwareData(curHeadTag, 0, mSoftwareHandler, UIHelper.LISTVIEW_ACTION_CHANGE_CATALOG);	
	    	}			
    	
	    	mActionBar.setTitle(longTitle.get(curHeadTag));
	    	mScrollLayout.setToScreen(curScreen);
			
		}
		
		@Override
		public void onTabReselected(Tab arg0, FragmentTransaction arg1) {
			// TODO Auto-generated method stub
			
		}
	};
	
	//初始化视图控件
    private void initView()
    {
    	mScrollLayout = (ScrollLayout) findViewById(R.id.frame_software_scrolllayout);
    	
    	//禁用滑动
        mScrollLayout.setIsScroll(false);
    	
    	this.initSoftwareCatalogListView();
    	this.initSoftwareTagListView();
    	this.initSoftwareListView();
    }
    
    //初始化分类listview
    private void initSoftwareCatalogListView()
    {
    	lvSoftwareCatalogAdapter = new ListViewSoftwareCatalogAdapter(this, lvSoftwareCatalogData, R.layout.softwarecatalog_listitem); 
    	mlvSoftwareCatalog = (ListView)findViewById(R.id.frame_software_listview_catalog);
    	mlvSoftwareCatalog.setAdapter(lvSoftwareCatalogAdapter); 
    	mlvSoftwareCatalog.setOnItemClickListener(new AdapterView.OnItemClickListener() {
        	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {       		
    			TextView name = (TextView)view.findViewById(R.id.softwarecatalog_listitem_name);
        		SoftwareType type = (SoftwareType)name.getTag();
        		
        		if(type == null) return;
        		
        		if(type.tag > 0){
        			curTitleLV1 = type.name;
        			mActionBar.setTitle(curTitleLV1);
        			//加载二级分类
        			curScreen = SCREEN_TAG;
        			mScrollLayout.scrollToScreen(curScreen);
        			loadLvSoftwareCatalogData(type.tag, mSoftwareTagHandler, UIHelper.LISTVIEW_ACTION_CHANGE_CATALOG);
        		}
        	}
		});
    	
    	mSoftwareCatalogHandler = new Handler()
		{
			public void handleMessage(Message msg) {
				
				headButtonSwitch(DATA_LOAD_COMPLETE);

				if(msg.what >= 0){						
					SoftwareCatalogList list = (SoftwareCatalogList)msg.obj;
					Notice notice = list.getNotice();
					//处理listview数据
					switch (msg.arg1) {
					case UIHelper.LISTVIEW_ACTION_INIT:
					case UIHelper.LISTVIEW_ACTION_REFRESH:
					case UIHelper.LISTVIEW_ACTION_CHANGE_CATALOG:
						lvSoftwareCatalogData.clear();//先清除原有数据
						lvSoftwareCatalogData.addAll(list.getSoftwareTypelist());
						break;
					case UIHelper.LISTVIEW_ACTION_SCROLL:
						break;
					}	
					
					lvSoftwareCatalogAdapter.notifyDataSetChanged();

					//发送通知广播
					if(notice != null){
						UIHelper.sendBroadCast(SoftwareLib.this, notice);
					}
				}
				else if(msg.what == -1){
					//有异常--显示加载出错 & 弹出错误消息
					((AppException)msg.obj).makeToast(SoftwareLib.this);
				}
			}
		};
    }
    
    //初始化二级分类listview
    private void initSoftwareTagListView()
    {
    	lvSoftwareTagAdapter = new ListViewSoftwareCatalogAdapter(this, lvSoftwareTagData, R.layout.softwarecatalog_listitem); 
    	mlvSoftwareTag = (ListView)findViewById(R.id.frame_software_listview_tag);
    	mlvSoftwareTag.setAdapter(lvSoftwareTagAdapter); 
    	mlvSoftwareTag.setOnItemClickListener(new AdapterView.OnItemClickListener() {
        	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {       		
        		TextView name = (TextView)view.findViewById(R.id.softwarecatalog_listitem_name);
        		SoftwareType type = (SoftwareType)name.getTag();
        		
        		if(type == null) return;
        		
        		if(type.tag > 0){
        			mActionBar.setTitle(type.name);
        			//加载软件列表
        			curScreen = SCREEN_SOFTWARE;
        			mScrollLayout.scrollToScreen(curScreen);
        			curSearchTag = type.tag;
        			loadLvSoftwareTagData(curSearchTag, 0, mSoftwareHandler, UIHelper.LISTVIEW_ACTION_CHANGE_CATALOG);
        		}
        	}
		});
    	
    	mSoftwareTagHandler = new Handler()
		{
			public void handleMessage(Message msg) {
				
				headButtonSwitch(DATA_LOAD_COMPLETE);

				if(msg.what >= 0){						
					SoftwareCatalogList list = (SoftwareCatalogList)msg.obj;
					Notice notice = list.getNotice();
					//处理listview数据
					switch (msg.arg1) {
					case UIHelper.LISTVIEW_ACTION_INIT:
					case UIHelper.LISTVIEW_ACTION_REFRESH:
					case UIHelper.LISTVIEW_ACTION_CHANGE_CATALOG:
						lvSoftwareTagData.clear();//先清除原有数据
						lvSoftwareTagData.addAll(list.getSoftwareTypelist());
						break;
					case UIHelper.LISTVIEW_ACTION_SCROLL:
						break;
					}	
					
					lvSoftwareTagAdapter.notifyDataSetChanged();

					//发送通知广播
					if(notice != null){
						UIHelper.sendBroadCast(SoftwareLib.this, notice);
					}
				}
				else if(msg.what == -1){
					//有异常--显示加载出错 & 弹出错误消息
					((AppException)msg.obj).makeToast(SoftwareLib.this);
				}
			}
		};
    }
    
    //初始化软件listview
    private void initSoftwareListView()
    {
    	lvSoftware_footer = getLayoutInflater().inflate(R.layout.listview_footer, null);
    	lvSoftware_foot_more = (TextView)lvSoftware_footer.findViewById(R.id.listview_foot_more);

    	lvSoftwareAdapter = new ListViewSoftwareAdapter(this, lvSoftwareData, R.layout.software_listitem); 
    	mlvSoftware = (PullToRefreshListView)findViewById(R.id.frame_software_listview);
    	
    	mlvSoftware.addFooterView(lvSoftware_footer);//添加底部视图  必须在setAdapter前
    	mlvSoftware.setAdapter(lvSoftwareAdapter); 
    	mlvSoftware.setOnItemClickListener(new AdapterView.OnItemClickListener() {
        	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        		//点击头部、底部栏无效
        		if(position == 0 || view == lvSoftware_footer) return;
        		        		
    			TextView name = (TextView)view.findViewById(R.id.software_listitem_name);
    			Software sw = (Software)name.getTag();

        		if(sw == null) return;
        		
        		//跳转
        		UIHelper.showUrlRedirect(view.getContext(), sw.url);
        	}
		});
    	mlvSoftware.setOnScrollListener(new AbsListView.OnScrollListener() {
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				mlvSoftware.onScrollStateChanged(view, scrollState);
				
				//数据为空--不用继续下面代码了
				if(lvSoftwareData.size() == 0) return;
				
				//判断是否滚动到底部
				boolean scrollEnd = false;
				try {
					if(view.getPositionForView(lvSoftware_footer) == view.getLastVisiblePosition())
						scrollEnd = true;
				} catch (Exception e) {
					scrollEnd = false;
				}
				
				if(scrollEnd && curLvSoftwareDataState==UIHelper.LISTVIEW_DATA_MORE)
				{
					mlvSoftware.setTag(UIHelper.LISTVIEW_DATA_LOADING);
					lvSoftware_foot_more.setText(R.string.load_ing);
					//当前pageIndex
					int pageIndex = lvSumData/20;
					if(curHeadTag == HEAD_TAG_CATALOG)
						loadLvSoftwareTagData(curSearchTag, pageIndex, mSoftwareHandler, UIHelper.LISTVIEW_ACTION_SCROLL);
					else
						loadLvSoftwareData(curHeadTag, pageIndex, mSoftwareHandler, UIHelper.LISTVIEW_ACTION_SCROLL);
				}
			}
			public void onScroll(AbsListView view, int firstVisibleItem,int visibleItemCount, int totalItemCount) {
				mlvSoftware.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
			}
		});
    	mlvSoftware.setOnRefreshListener(new PullToRefreshListView.OnRefreshListener() {
			public void onRefresh() {
				if(curHeadTag == HEAD_TAG_CATALOG)
					loadLvSoftwareTagData(curSearchTag, 0, mSoftwareHandler, UIHelper.LISTVIEW_ACTION_REFRESH);
				else
					loadLvSoftwareData(curHeadTag, 0, mSoftwareHandler, UIHelper.LISTVIEW_ACTION_REFRESH);
            }
        });
    	
      	mSoftwareHandler = new Handler()
		{
			public void handleMessage(Message msg) {
				
				headButtonSwitch(DATA_LOAD_COMPLETE);

				if(msg.what >= 0){						
					SoftwareList list = (SoftwareList)msg.obj;
					Notice notice = list.getNotice();
					//处理listview数据
					switch (msg.arg1) {
					case UIHelper.LISTVIEW_ACTION_INIT:
					case UIHelper.LISTVIEW_ACTION_REFRESH:
					case UIHelper.LISTVIEW_ACTION_CHANGE_CATALOG:
						lvSumData = msg.what;
						lvSoftwareData.clear();//先清除原有数据
						lvSoftwareData.addAll(list.getSoftwarelist());
						break;
					case UIHelper.LISTVIEW_ACTION_SCROLL:
						lvSumData += msg.what;
						if(lvSoftwareData.size() > 0){
							for(Software sw1 : list.getSoftwarelist()){
								boolean b = false;
								for(Software sw2 : lvSoftwareData){
									if(sw1.name.equals(sw2.name)){
										b = true;
										break;
									}
								}
								if(!b) lvSoftwareData.add(sw1);
							}
						}else{
							lvSoftwareData.addAll(list.getSoftwarelist());
						}
						break;
					}	
					
					if(msg.what < 20){
						curLvSoftwareDataState = UIHelper.LISTVIEW_DATA_FULL;
						lvSoftwareAdapter.notifyDataSetChanged();
						lvSoftware_foot_more.setText(R.string.load_full);
					}else if(msg.what == 20){					
						curLvSoftwareDataState = UIHelper.LISTVIEW_DATA_MORE;
						lvSoftwareAdapter.notifyDataSetChanged();
						lvSoftware_foot_more.setText(R.string.load_more);
					}
					//发送通知广播
					if(notice != null){
						UIHelper.sendBroadCast(SoftwareLib.this, notice);
					}
				}
				else if(msg.what == -1){
					//有异常--显示加载出错 & 弹出错误消息
					curLvSoftwareDataState = UIHelper.LISTVIEW_DATA_MORE;
					lvSoftware_foot_more.setText(R.string.load_error);
					((AppException)msg.obj).makeToast(SoftwareLib.this);
				}
				if(lvSoftwareData.size()==0){
					curLvSoftwareDataState = UIHelper.LISTVIEW_DATA_EMPTY;
					lvSoftware_foot_more.setText(R.string.load_empty);
				}
				if(msg.arg1 == UIHelper.LISTVIEW_ACTION_REFRESH){
					mlvSoftware.onRefreshComplete(getString(R.string.pull_to_refresh_update) + new Date().toLocaleString());
					mlvSoftware.setSelection(0);
				}else if(msg.arg1 == UIHelper.LISTVIEW_ACTION_CHANGE_CATALOG){
					mlvSoftware.onRefreshComplete();
					mlvSoftware.setSelection(0);
				}
			}
		};
    }
    
    //初始化控件数据
  	private void initData()
  	{
  		this.loadLvSoftwareCatalogData(0, mSoftwareCatalogHandler, UIHelper.LISTVIEW_ACTION_CHANGE_CATALOG);
  	}
  	
  	/**
     * 头部按钮展示
     * @param type
     */
    private void headButtonSwitch(int type) {
    	switch (type) {
    	case DATA_LOAD_ING:
    		mProgressBar.setVisibility(View.VISIBLE);
			break;
		case DATA_LOAD_COMPLETE:
			mProgressBar.setVisibility(View.GONE);
			break;
		}
    }
	
  	/**
     * 线程加载软件分类列表数据
     * @param tag 第一级:0 第二级:tag
     * @param handler 处理器
     * @param action 动作标识
     */
	private void loadLvSoftwareCatalogData(final int tag,final Handler handler,final int action){  
		headButtonSwitch(DATA_LOAD_ING);
		new Thread(){
			public void run() {
				Message msg = new Message();
				try {
					SoftwareCatalogList softwareCatalogList = ((AppContext)getApplication()).getSoftwareCatalogList(tag);
					msg.what = softwareCatalogList.getSoftwareTypelist().size();
					msg.obj = softwareCatalogList;
	            } catch (AppException e) {
	            	e.printStackTrace();
	            	msg.what = -1;
	            	msg.obj = e;
	            }
				msg.arg1 = action;//告知handler当前action
                handler.sendMessage(msg);
			}
		}.start();
	}
	
  	/**
     * 线程加载软件分类二级列表数据
     * @param tag 第一级:0 第二级:tag
     * @param handler 处理器
     * @param action 动作标识
     */
	private void loadLvSoftwareTagData(final int searchTag,final int pageIndex,final Handler handler,final int action){  
		headButtonSwitch(DATA_LOAD_ING);
		new Thread(){
			public void run() {
				Message msg = new Message();
				boolean isRefresh = false;
				if(action == UIHelper.LISTVIEW_ACTION_REFRESH || action == UIHelper.LISTVIEW_ACTION_SCROLL)
					isRefresh = true;
				try {
					SoftwareList softwareList = ((AppContext)getApplication()).getSoftwareTagList(searchTag, pageIndex, isRefresh);
					msg.what = softwareList.getSoftwarelist().size();
					msg.obj = softwareList;
	            } catch (AppException e) {
	            	e.printStackTrace();
	            	msg.what = -1;
	            	msg.obj = e;
	            }
				msg.arg1 = action;//告知handler当前action
                handler.sendMessage(msg);
			}
		}.start();
	}
	
  	/**
     * 线程加载软件列表数据
     * @param searchTag 软件分类 推荐:recommend 最新:time 热门:view 国产:list_cn
     * @param pageIndex 当前页数
     * @param handler 处理器
     * @param action 动作标识
     */
	private void loadLvSoftwareData(final int tag,final int pageIndex,final Handler handler,final int action){  
		
		String _searchTag = "";
		
		switch (tag) {
		case HEAD_TAG_RECOMMEND: 
			_searchTag = SoftwareList.TAG_RECOMMEND;
			break;
		case HEAD_TAG_LASTEST: 
			_searchTag = SoftwareList.TAG_LASTEST;
			break;
		case HEAD_TAG_HOT: 
			_searchTag = SoftwareList.TAG_HOT;
			break;
		case HEAD_TAG_CHINA: 
			_searchTag = SoftwareList.TAG_CHINA;
			break;
		}
		
		if(StringUtils.isEmpty(_searchTag)) return;		
		
		final String searchTag = _searchTag;
		
		headButtonSwitch(DATA_LOAD_ING);
		
		new Thread(){
			public void run() {
				Message msg = new Message();
				boolean isRefresh = false;
				if(action == UIHelper.LISTVIEW_ACTION_REFRESH || action == UIHelper.LISTVIEW_ACTION_SCROLL)
					isRefresh = true;
				try {
					SoftwareList softwareList = ((AppContext)getApplication()).getSoftwareList(searchTag, pageIndex, isRefresh);
					msg.what = softwareList.getPageSize();
					msg.obj = softwareList;
	            } catch (AppException e) {
	            	e.printStackTrace();
	            	msg.what = -1;
	            	msg.obj = e;
	            }
				msg.arg1 = action;//告知handler当前action
				if(curHeadTag == tag)
					handler.sendMessage(msg);
			}
		}.start();
	} 
	
	/**
	 * 返回事件
	 */
	private void back() {
		if(curHeadTag == HEAD_TAG_CATALOG) {
			switch (curScreen) {
			case SCREEN_SOFTWARE:
    			mActionBar.setTitle(curTitleLV1);
				curScreen = SCREEN_TAG;
				mScrollLayout.scrollToScreen(SCREEN_TAG);
				break;
			case SCREEN_TAG:
				mActionBar.setTitle(R.string.software_lib_title);
				curScreen = SCREEN_CATALOG;
				mScrollLayout.scrollToScreen(SCREEN_CATALOG);
				break;
			case SCREEN_CATALOG:
				finish();
				break;
			}
			
		}else{
			finish();
		}
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK) {
			back();
			return true;
		}
		return false;
	}
	
	
}
