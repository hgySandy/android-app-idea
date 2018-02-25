package net.oschina.app.ui;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.oschina.app.AppContext;
import net.oschina.app.AppException;
import net.oschina.app.adapter.ListViewFriendAdapter;
import net.oschina.app.bean.FriendList;
import net.oschina.app.bean.FriendList.Friend;
import net.oschina.app.bean.Notice;
import net.oschina.app.common.ActionBarUtil;
import net.oschina.app.common.UIHelper;
import net.oschina.app.widget.PullToRefreshListView;
import net.oschina.designapp.R;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBar.Tab;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * 用户关注、粉丝
 * @author liux (http://my.oschina.net/liux)
 * @intent INTENT_TYEP 打开页面时显示的页面 FOOLOWERS : 显示自己的粉丝  FANS : 显示自己的关注者
 * @intent INTENT_FOOLOWERS 关注者的数目
 * @intent INTENT_FANS 粉丝的数目
 * @version 1.2
 * @created 2014-8-17
 */
public class UserFriend extends BaseActionBarActivity {
	public static final String INTENT_TYEP = "friend_type";
	public static final String INTENT_FOOLOWERS = "friend_followers";
	public static final String INTENT_FANS = "friend_fans";
	
	public static final int FOOLOWERS = 1;
	public static final int FANS = 0;

	private ActionBar mActionBar;
	private ProgressBar mProgressBar;
	
	private Tab followerTab;
	private Tab fansTab;
	
	private PullToRefreshListView mlvFriend;
	private ListViewFriendAdapter lvFriendAdapter;
	private List<Friend> lvFriendData = new ArrayList<Friend>();
	private View lvFriend_footer;
	private TextView lvFriend_foot_more;
    private Handler mFriendHandler;
    private int lvSumData;
	
	private int curLvCatalog;
	private int curLvDataState;
    
	private final static int DATA_LOAD_ING = 0x001;
	private final static int DATA_LOAD_COMPLETE = 0x002;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_friend);
        this.initActionBar();
        this.initData();
        this.initView();
        this.initTabs();
	}
	
	private void initActionBar(){
		mActionBar = getSupportActionBar();
		mActionBar.setTitle(R.string.user_friend_head_title);
		mActionBar.setDisplayShowHomeEnabled(true);
		mActionBar.setHomeButtonEnabled(true);
		mProgressBar = ActionBarUtil.getProgressBar(UserFriend.this);
		mProgressBar.setVisibility(View.GONE);
	}
	
	//初始化视图控件
    private void initView()
    {	
    	//设置当前分类
    	curLvCatalog = getIntent().getIntExtra(INTENT_TYEP, FriendList.TYPE_FOLLOWER);
    	
    	lvFriend_footer = getLayoutInflater().inflate(R.layout.listview_footer, null);
    	lvFriend_foot_more = (TextView)lvFriend_footer.findViewById(R.id.listview_foot_more);

    	lvFriendAdapter = new ListViewFriendAdapter(this, lvFriendData, R.layout.friend_listitem); 
    	mlvFriend = (PullToRefreshListView)findViewById(R.id.friend_listview);
    	
    	mlvFriend.addFooterView(lvFriend_footer);//添加底部视图  必须在setAdapter前
    	mlvFriend.setAdapter(lvFriendAdapter); 
    	mlvFriend.setOnItemClickListener(new AdapterView.OnItemClickListener() {
        	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        		//点击头部、底部栏无效
        		if(position == 0 || view == lvFriend_footer) return;
        		
    			TextView name = (TextView)view.findViewById(R.id.friend_listitem_name);
    			Friend friend = (Friend)name.getTag();

        		if(friend == null) return;
        		
        		//跳转
        		UIHelper.showUserCenter(view.getContext(), friend.getUserid(), friend.getName());
        	}
		});
    	mlvFriend.setOnScrollListener(new AbsListView.OnScrollListener() {
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				mlvFriend.onScrollStateChanged(view, scrollState);
				
				//数据为空--不用继续下面代码了
				if(lvFriendData.size() == 0) return;
				
				//判断是否滚动到底部
				boolean scrollEnd = false;
				try {
					if(view.getPositionForView(lvFriend_footer) == view.getLastVisiblePosition())
						scrollEnd = true;
				} catch (Exception e) {
					scrollEnd = false;
				}
				
				if(scrollEnd && curLvDataState==UIHelper.LISTVIEW_DATA_MORE)
				{
					mlvFriend.setTag(UIHelper.LISTVIEW_DATA_LOADING);
					lvFriend_foot_more.setText(R.string.load_ing);
					//当前pageIndex
					int pageIndex = lvSumData/20;
					loadLvFriendData(curLvCatalog, pageIndex, mFriendHandler, UIHelper.LISTVIEW_ACTION_SCROLL);
				}
			}
			public void onScroll(AbsListView view, int firstVisibleItem,int visibleItemCount, int totalItemCount) {
				mlvFriend.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
			}
		});
    	mlvFriend.setOnRefreshListener(new PullToRefreshListView.OnRefreshListener() {
			public void onRefresh() {
				loadLvFriendData(curLvCatalog, 0, mFriendHandler, UIHelper.LISTVIEW_ACTION_REFRESH);
            }
        });
    }
	/**
	 * 设置ActionBar的选项卡
	 */
	private void initTabs(){
		mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		followerTab = mActionBar.newTab()
					.setTag(FriendList.TYPE_FOLLOWER)
					.setText(R.string.software_lib_title_catalog)
					.setTabListener(tabListener);
		fansTab     = mActionBar.newTab()
					.setTag(FriendList.TYPE_FANS)
					.setText(R.string.software_lib_title_recommend)
					.setTabListener(tabListener);
		
		//设置粉丝与关注的数量
    	int followers = getIntent().getIntExtra(INTENT_FOOLOWERS, 0);
    	int fans = getIntent().getIntExtra(INTENT_FANS, 0);
    	followerTab.setText(getString(R.string.user_friend_follower, followers));
    	followerTab.setTag(FOOLOWERS);
    	fansTab.setText(getString(R.string.user_friend_fans, fans));
    	fansTab.setTag(FANS);
		
		mActionBar.addTab(followerTab);
		mActionBar.addTab(fansTab);
	}
	
	private ActionBar.TabListener tabListener = new ActionBar.TabListener() {
		
		@Override
		public void onTabUnselected(Tab arg0, FragmentTransaction arg1) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void onTabSelected(Tab arg0, FragmentTransaction arg1) {
			lvFriend_foot_more.setText(R.string.load_more);
			
			curLvCatalog = (Integer) arg0.getTag();
			Log.e("用户数据", ""+curLvCatalog);
			loadLvFriendData(curLvCatalog, 0, mFriendHandler, UIHelper.LISTVIEW_ACTION_CHANGE_CATALOG);		 
			
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
    
    //初始化控件数据
  	private void initData()
  	{	
		mFriendHandler = new Handler()
		{
			public void handleMessage(Message msg) {
				
				headButtonSwitch(DATA_LOAD_COMPLETE);

				if(msg.what >= 0){						
					FriendList list = (FriendList)msg.obj;
					Notice notice = list.getNotice();
					//处理listview数据
					switch (msg.arg1) {
					case UIHelper.LISTVIEW_ACTION_INIT:
					case UIHelper.LISTVIEW_ACTION_REFRESH:
					case UIHelper.LISTVIEW_ACTION_CHANGE_CATALOG:
						lvSumData = msg.what;
						lvFriendData.clear();//先清除原有数据
						lvFriendData.addAll(list.getFriendlist());
						break;
					case UIHelper.LISTVIEW_ACTION_SCROLL:
						lvSumData += msg.what;
						if(lvFriendData.size() > 0){
							for(Friend friend1 : list.getFriendlist()){
								boolean b = false;
								for(Friend friend2 : lvFriendData){
									if(friend1.getUserid() == friend2.getUserid()){
										b = true;
										break;
									}
								}
								if(!b) lvFriendData.add(friend1);
							}
						}else{
							lvFriendData.addAll(list.getFriendlist());
						}
						break;
					}	
					
					if(msg.what < 20){
						curLvDataState = UIHelper.LISTVIEW_DATA_FULL;
						lvFriendAdapter.notifyDataSetChanged();
						lvFriend_foot_more.setText(R.string.load_full);
					}else if(msg.what == 20){					
						curLvDataState = UIHelper.LISTVIEW_DATA_MORE;
						lvFriendAdapter.notifyDataSetChanged();
						lvFriend_foot_more.setText(R.string.load_more);
					}
					//发送通知广播
					if(notice != null){
						UIHelper.sendBroadCast(UserFriend.this, notice);
					}
				}
				else if(msg.what == -1){
					//有异常--显示加载出错 & 弹出错误消息
					curLvDataState = UIHelper.LISTVIEW_DATA_MORE;
					lvFriend_foot_more.setText(R.string.load_error);
					((AppException)msg.obj).makeToast(UserFriend.this);
				}
				if(lvFriendData.size()==0){
					curLvDataState = UIHelper.LISTVIEW_DATA_EMPTY;
					lvFriend_foot_more.setText(R.string.load_empty);
				}
				if(msg.arg1 == UIHelper.LISTVIEW_ACTION_REFRESH){
					mlvFriend.onRefreshComplete(getString(R.string.pull_to_refresh_update) + new Date().toLocaleString());
					mlvFriend.setSelection(0);
				}else if(msg.arg1 == UIHelper.LISTVIEW_ACTION_CHANGE_CATALOG){
					mlvFriend.onRefreshComplete();
					mlvFriend.setSelection(0);
				}
			}
		};
		this.loadLvFriendData(curLvCatalog,0,mFriendHandler,UIHelper.LISTVIEW_ACTION_INIT);
  	}
  	
    /**
     * 线程加载好友列表数据
     * @param type 0:显示自己的粉丝 1:显示自己的关注者
     * @param pageIndex 当前页数
     * @param handler 处理器
     * @param action 动作标识
     */
	private void loadLvFriendData(final int type,final int pageIndex,final Handler handler,final int action){  
		headButtonSwitch(DATA_LOAD_ING);
		new Thread(){
			public void run() {
				Message msg = new Message();
				boolean isRefresh = false;
				if(action == UIHelper.LISTVIEW_ACTION_REFRESH || action == UIHelper.LISTVIEW_ACTION_SCROLL)
					isRefresh = true;
				try {
					FriendList FriendList = ((AppContext)getApplication()).getFriendList(type, pageIndex, isRefresh);
					msg.what = FriendList.getFriendlist().size();
					msg.obj = FriendList;
	            } catch (AppException e) {
	            	e.printStackTrace();
	            	msg.what = -1;
	            	msg.obj = e;
	            }
				msg.arg1 = action;//告知handler当前action
				if(curLvCatalog == type)
					handler.sendMessage(msg);
			}
		}.start();
	} 
}
