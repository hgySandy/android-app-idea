package net.oschina.app.ui;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


import net.oschina.app.AppContext;
import net.oschina.app.AppException;
import net.oschina.app.adapter.ListViewFavoriteAdapter;
import net.oschina.app.bean.FavoriteList;
import net.oschina.app.bean.Notice;
import net.oschina.app.bean.Result;
import net.oschina.app.bean.FavoriteList.Favorite;
import net.oschina.app.common.ActionBarUtil;
import net.oschina.app.common.DipUtil;
import net.oschina.app.common.UIHelper;
import net.oschina.app.widget.PullToRefreshListView;
import net.oschina.designapp.R;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBar.Tab;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * 用户收藏
 * @author liux (http://my.oschina.net/liux)
 * @version 1.0
 * @created 2014-7-14
 */
public class UserFavorite extends BaseActionBarActivity {

	private ActionBar mActionBar;
	private ProgressBar mProgressBar;
	
	private PullToRefreshListView mlvFavorite;
	private ListViewFavoriteAdapter lvFavoriteAdapter;
	private List<Favorite> lvFavoriteData = new ArrayList<Favorite>();
	private View lvFavorite_footer;
	private TextView lvFavorite_foot_more;
    private Handler mFavoriteHandler;
    private int lvSumData;
	
	private int curFavoriteCatalog;
	private int curLvDataState;
    
	private final static int DATA_LOAD_ING = 0x001;
	private final static int DATA_LOAD_COMPLETE = 0x002;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_favorite);
        this.initActionBar();
        this.initView();
        this.initData();
        this.initTabs();
	}
	
	private void initActionBar(){
		mActionBar = getSupportActionBar();
		mActionBar.setTitle(R.string.user_favorite_title);
		mActionBar.setDisplayShowHomeEnabled(true);
		mActionBar.setHomeButtonEnabled(true);
		mProgressBar = ActionBarUtil.getProgressBar(UserFavorite.this);
		mProgressBar.setVisibility(View.GONE);
	}
	
	private void initTabs(){
		mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		Tab catalog   = mActionBar.newTab()
				.setTag(FavoriteList.TYPE_SOFTWARE)
				.setText(R.string.software_lib_title_catalog)
				.setTabListener(tabListener);
		Tab recommend = mActionBar.newTab()
				.setTag(FavoriteList.TYPE_POST)
				.setText(R.string.software_lib_title_recommend)
				.setTabListener(tabListener);
		Tab lastest   = mActionBar.newTab()
				.setTag(FavoriteList.TYPE_CODE)
				.setText(R.string.software_lib_title_lastest)
				.setTabListener(tabListener);
		Tab hot 	  = mActionBar.newTab()
				.setTag(FavoriteList.TYPE_BLOG)
				.setText(R.string.software_lib_title_hot)
				.setTabListener(tabListener);
		Tab china 	  = mActionBar.newTab()
				.setTag(FavoriteList.TYPE_NEWS)
				.setText(R.string.software_lib_title_china)
				.setTabListener(tabListener);
		
		mActionBar.addTab(catalog);
		mActionBar.addTab(recommend);
		mActionBar.addTab(lastest);
		mActionBar.addTab(hot);
		mActionBar.addTab(china);
	}
	
	private ActionBar.TabListener tabListener = new ActionBar.TabListener() {
		
		@Override
		public void onTabUnselected(Tab arg0, FragmentTransaction arg1) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void onTabSelected(Tab arg0, FragmentTransaction arg1) {
			lvFavorite_foot_more.setText(R.string.load_more);
			curFavoriteCatalog = (Integer) arg0.getTag();
			loadLvFavoriteData(curFavoriteCatalog, 0, mFavoriteHandler, UIHelper.LISTVIEW_ACTION_CHANGE_CATALOG);
			
		}
		
		@Override
		public void onTabReselected(Tab arg0, FragmentTransaction arg1) {
			// TODO Auto-generated method stub
			
		}
	};
	
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
	
	//初始化视图控件
    private void initView()
    {	
    	curFavoriteCatalog = FavoriteList.TYPE_SOFTWARE;
    	
    	lvFavorite_footer = getLayoutInflater().inflate(R.layout.listview_footer, null);
    	lvFavorite_foot_more = (TextView)lvFavorite_footer.findViewById(R.id.listview_foot_more);

    	lvFavoriteAdapter = new ListViewFavoriteAdapter(this, lvFavoriteData, R.layout.favorite_listitem); 
    	mlvFavorite = (PullToRefreshListView)findViewById(R.id.favorite_listview);
    	
    	mlvFavorite.addFooterView(lvFavorite_footer);//添加底部视图  必须在setAdapter前
    	mlvFavorite.setAdapter(lvFavoriteAdapter); 
    	mlvFavorite.setOnItemClickListener(new AdapterView.OnItemClickListener() {
        	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        		//点击头部、底部栏无效
        		if(position == 0 || view == lvFavorite_footer) return;
        		
        		Favorite fav = null;
        		//判断是否是TextView
        		if(view instanceof TextView){
        			fav = (Favorite)view.getTag();
        		}else{
        			TextView title = (TextView)view.findViewById(R.id.favorite_listitem_title);
        			fav = (Favorite)title.getTag();
        		} 
        		if(fav == null) return;
        		
        		//跳转
        		UIHelper.showUrlRedirect(view.getContext(), fav.url);
        	}
		});
    	mlvFavorite.setOnScrollListener(new AbsListView.OnScrollListener() {
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				mlvFavorite.onScrollStateChanged(view, scrollState);
				
				//数据为空--不用继续下面代码了
				if(lvFavoriteData.size() == 0) return;
				
				//判断是否滚动到底部
				boolean scrollEnd = false;
				try {
					if(view.getPositionForView(lvFavorite_footer) == view.getLastVisiblePosition())
						scrollEnd = true;
				} catch (Exception e) {
					scrollEnd = false;
				}
				
				if(scrollEnd && curLvDataState==UIHelper.LISTVIEW_DATA_MORE)
				{
					mlvFavorite.setTag(UIHelper.LISTVIEW_DATA_LOADING);
					lvFavorite_foot_more.setText(R.string.load_ing);
					//当前pageIndex
					int pageIndex = lvSumData/20;
					loadLvFavoriteData(curFavoriteCatalog, pageIndex, mFavoriteHandler, UIHelper.LISTVIEW_ACTION_SCROLL);
				}
			}
			public void onScroll(AbsListView view, int firstVisibleItem,int visibleItemCount, int totalItemCount) {
				mlvFavorite.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
			}
		});
    	mlvFavorite.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				//点击头部、底部栏无效
        		if(position == 0 || view == lvFavorite_footer) return false;				
				
        		Favorite _fav = null;
        		//判断是否是TextView
        		if(view instanceof TextView){
        			_fav = (Favorite)view.getTag();
        		}else{
        			TextView title = (TextView)view.findViewById(R.id.favorite_listitem_title);
            		_fav = (Favorite)title.getTag();
        		} 
        		if(_fav == null) return false;
        		
        		final Favorite fav = _fav;
        		
        		final AppContext ac = (AppContext)getApplication();
				//操作--删除
        		final int uid = ac.getLoginUid();

        		final Handler handler = new Handler(){
					public void handleMessage(Message msg) {
						if(msg.what == 1){
							Result res = (Result)msg.obj;
							if(res.OK()){
								lvFavoriteData.remove(fav);
								lvFavoriteAdapter.notifyDataSetChanged();
							}
							UIHelper.ToastMessage(UserFavorite.this, res.getErrorMessage());
						}else{
							((AppException)msg.obj).makeToast(UserFavorite.this);
						}
					}        			
        		};
        		final Thread thread = new Thread(){
					public void run() {
						Message msg = new Message();
						try {
							Result res = ac.delFavorite(uid, fav.objid, fav.type);
							msg.what = 1;
							msg.obj = res;
			            } catch (AppException e) {
			            	e.printStackTrace();
			            	msg.what = -1;
			            	msg.obj = e;
			            }
		                handler.sendMessage(msg);
					}        			
        		};
        		UIHelper.showFavoriteOptionDialog(UserFavorite.this, thread);
			
				return true;
			}        	
		});
    	mlvFavorite.setOnRefreshListener(new PullToRefreshListView.OnRefreshListener() {
			public void onRefresh() {
				loadLvFavoriteData(curFavoriteCatalog, 0, mFavoriteHandler, UIHelper.LISTVIEW_ACTION_REFRESH);
            }
        });
    }
    
    //初始化控件数据
  	private void initData()
  	{			
		mFavoriteHandler = new Handler()
		{
			public void handleMessage(Message msg) {
				
				headButtonSwitch(DATA_LOAD_COMPLETE);

				if(msg.what >= 0){						
					FavoriteList list = (FavoriteList)msg.obj;
					Notice notice = list.getNotice();
					//处理listview数据
					switch (msg.arg1) {
					case UIHelper.LISTVIEW_ACTION_INIT:
					case UIHelper.LISTVIEW_ACTION_REFRESH:
					case UIHelper.LISTVIEW_ACTION_CHANGE_CATALOG:
						lvSumData = msg.what;
						lvFavoriteData.clear();//先清除原有数据
						lvFavoriteData.addAll(list.getFavoritelist());
						break;
					case UIHelper.LISTVIEW_ACTION_SCROLL:
						lvSumData += msg.what;
						if(lvFavoriteData.size() > 0){
							for(Favorite fav1 : list.getFavoritelist()){
								boolean b = false;
								for(Favorite fav2 : lvFavoriteData){
									if(fav1.objid == fav2.objid){
										b = true;
										break;
									}
								}
								if(!b) lvFavoriteData.add(fav1);
							}
						}else{
							lvFavoriteData.addAll(list.getFavoritelist());
						}
						break;
					}	
					
					if(msg.what < 20){
						curLvDataState = UIHelper.LISTVIEW_DATA_FULL;
						lvFavoriteAdapter.notifyDataSetChanged();
						lvFavorite_foot_more.setText(R.string.load_full);
					}else if(msg.what == 20){					
						curLvDataState = UIHelper.LISTVIEW_DATA_MORE;
						lvFavoriteAdapter.notifyDataSetChanged();
						lvFavorite_foot_more.setText(R.string.load_more);
					}
					//发送通知广播
					if(notice != null){
						UIHelper.sendBroadCast(UserFavorite.this, notice);
					}
				}
				else if(msg.what == -1){
					//有异常--显示加载出错 & 弹出错误消息
					curLvDataState = UIHelper.LISTVIEW_DATA_MORE;
					lvFavorite_foot_more.setText(R.string.load_error);
					((AppException)msg.obj).makeToast(UserFavorite.this);
				}
				if(lvFavoriteData.size()==0){
					curLvDataState = UIHelper.LISTVIEW_DATA_EMPTY;
					lvFavorite_foot_more.setText(R.string.load_empty);
				}
				if(msg.arg1 == UIHelper.LISTVIEW_ACTION_REFRESH){
					mlvFavorite.onRefreshComplete(getString(R.string.pull_to_refresh_update) + new Date().toLocaleString());
					mlvFavorite.setSelection(0);
				}else if(msg.arg1 == UIHelper.LISTVIEW_ACTION_CHANGE_CATALOG){
					mlvFavorite.onRefreshComplete();
					mlvFavorite.setSelection(0);
				}
			}
		};
		this.loadLvFavoriteData(curFavoriteCatalog,0,mFavoriteHandler,UIHelper.LISTVIEW_ACTION_INIT);
  	}
  	
    /**
     * 线程加载收藏数据
     * @param type 0:全部收藏 1:软件 2:话题 3:博客 4:新闻 5:代码
     * @param pageIndex 当前页数
     * @param handler 处理器
     * @param action 动作标识
     */
	private void loadLvFavoriteData(final int type,final int pageIndex,final Handler handler,final int action){  
		headButtonSwitch(DATA_LOAD_ING);
		new Thread(){
			public void run() {
				Message msg = new Message();
				boolean isRefresh = false;
				if(action == UIHelper.LISTVIEW_ACTION_REFRESH || action == UIHelper.LISTVIEW_ACTION_SCROLL)
					isRefresh = true;
				try {
					FavoriteList favoriteList = ((AppContext)getApplication()).getFavoriteList(type, pageIndex, isRefresh);
					msg.what = favoriteList.getPageSize();
					msg.obj = favoriteList;
	            } catch (AppException e) {
	            	e.printStackTrace();
	            	msg.what = -1;
	            	msg.obj = e;
	            }
				msg.arg1 = action;//告知handler当前action
				if(curFavoriteCatalog == type)
					handler.sendMessage(msg);
			}
		}.start();
	} 
}
