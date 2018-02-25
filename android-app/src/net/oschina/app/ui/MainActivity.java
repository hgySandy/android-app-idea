package net.oschina.app.ui;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;
import net.oschina.app.AppContext;
import net.oschina.app.AppException;
import net.oschina.app.bean.ActiveList;
import net.oschina.app.bean.FriendList;
import net.oschina.app.bean.MessageList;
import net.oschina.app.bean.MyInformation;
import net.oschina.app.bean.Notice;
import net.oschina.app.bean.Result;
import net.oschina.app.bean.Tweet;
import net.oschina.app.common.AnimUtil;
import net.oschina.app.common.BadgeManager;
import net.oschina.app.common.DipUtil;
import net.oschina.app.common.HandlerManager;
import net.oschina.app.common.StringUtils;
import net.oschina.app.common.UIHelper;
import net.oschina.app.fragment.ActiveMainFragment;
import net.oschina.app.fragment.NewsMainFragment;
import net.oschina.app.fragment.QuestionMainFragment;
import net.oschina.app.fragment.TweetMainFragment;
import net.oschina.app.fragment.LoginDialogFragment.LoginDialogListener;
import net.oschina.app.inteface.ActionBarProgressBarVisibility;
import net.oschina.app.inteface.FooterViewVisibility;
import net.oschina.app.widget.LoadingDialog;
import net.oschina.designapp.R;

public class MainActivity extends BaseActionBarActivity implements
		ActionBarProgressBarVisibility, FooterViewVisibility, OnClickListener {
	public static final String TAG = MainActivity.class.getSimpleName();
	
	private TweetReceiver tweetReceiver;
	private AppContext appContext;
	private ProgressBar mProgressBar;
	private LinearLayout footerLayout;

	private RadioButton fbNews;
	private RadioButton fbQuestion;
	private RadioButton fbTweet;
	private RadioButton fbactive;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_activity_layout);

		// 注册广播接收器
		tweetReceiver = new TweetReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction("net.oschina.app.action.APP_TWEETPUB");
		registerReceiver(tweetReceiver, filter);

		appContext = (AppContext) getApplication();

		initDrawerLayout();
		initFirstFragment();
		setFooterView();

		// 网络连接判断
		if (!appContext.isNetworkConnected())
			UIHelper.ToastMessage(this, R.string.network_not_connected);
		// 初始化登录
		appContext.initLoginInfo();
		// 检查新版本
		if (appContext.isCheckUp()) {
			// UpdateManager.getUpdateManager().checkAppUpdate(this, false);
		}

		initDrawer();
		// 启动轮询通知信息
		this.foreachUserNotice();
	}

	private ActionBar mActionBar;
	private DrawerLayout mDrawerLayout;
	private MainToggle mMaintoggle;

	private void initDrawerLayout() {
		mActionBar = getSupportActionBar();
		mActionBar.setHomeButtonEnabled(true);
		mActionBar.setDisplayHomeAsUpEnabled(true);
		mActionBar.setDisplayShowCustomEnabled(true);

		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawerlayout);
		mMaintoggle = new MainToggle();
		mDrawerLayout.setDrawerListener(mMaintoggle);

		mProgressBar = new ProgressBar(this);
		mActionBar.setCustomView(mProgressBar, new ActionBar.LayoutParams(
				DipUtil.dpChangePix(32), DipUtil.dpChangePix(32),
				Gravity.CENTER | Gravity.RIGHT));
	}

	private void initFirstFragment() {
		FragmentTransaction fTransaction = getSupportFragmentManager()
				.beginTransaction();
		fTransaction.add(R.id.main_activity_linearlayout,
				new NewsMainFragment()).commit();
	}

	private void setFooterView() {
		footerLayout = (LinearLayout) findViewById(R.id.main_layout_footer);

		fbNews = (RadioButton) findViewById(R.id.main_footbar_news);
		fbQuestion = (RadioButton) findViewById(R.id.main_footbar_question);
		fbTweet = (RadioButton) findViewById(R.id.main_footbar_tweet);
		fbactive = (RadioButton) findViewById(R.id.main_footbar_user);

		fbNews.setChecked(true);

		fbNews.setOnClickListener(onClickListener);
		fbQuestion.setOnClickListener(onClickListener);
		fbTweet.setOnClickListener(onClickListener);
		fbactive.setOnClickListener(onClickListener);

		BadgeManager bManager = BadgeManager.getInstance();
		bManager.setActive(MainActivity.this, fbactive);
	}

	private void initDrawer() {
		final ImageView userFace = (ImageView) findViewById(R.id.user_info_userface);
		final TextView userName  = (TextView) findViewById(R.id.user_info_username);
		
		final LinearLayout watch 	= (LinearLayout) findViewById(R.id.watch);
		final LinearLayout favorite = (LinearLayout) findViewById(R.id.Favorites);
		final LinearLayout fans 	= (LinearLayout) findViewById(R.id.fans);
		watch.setOnClickListener(this);
		favorite.setOnClickListener(this);
		fans.setOnClickListener(this);
		
		final TextView watchNum 	 = (TextView) findViewById(R.id.watch_num);
		final TextView favoriteNum 	 = (TextView) findViewById(R.id.Favorites_num);
		final TextView fansNum 		 = (TextView) findViewById(R.id.fans_num);
		final TextView drawerOpen 	 = (TextView) findViewById(R.id.drawer_soft);
		final TextView drawerCapture = (TextView) findViewById(R.id.drawer_capture);
		final TextView drawerSetting = (TextView) findViewById(R.id.drawer_setting);
		final TextView drawerLogout  = (TextView) findViewById(R.id.drawer_longout);
		if (appContext.getLoginUid() > 0)
			drawerLogout.setText(R.string.main_menu_logout);
		else 
			drawerLogout.setText(R.string.main_menu_login);
		
		drawerOpen	 .setOnClickListener(this);
		drawerCapture.setOnClickListener(this);
		drawerSetting.setOnClickListener(this);
		drawerLogout .setOnClickListener(this);

		mHandler = new Handler() {
			public void handleMessage(Message msg) {
				if (msg.what == 1 && msg.obj != null) {
					user = (MyInformation) msg.obj;
					
					if (user.getId() > 0){
						// 加载用户头像
						UIHelper.showUserFace(userFace, user.getFace());
						// 其他资料
						userName.setText(user.getName());
						watchNum.setText(user.getFollowerscount() + "");
						fansNum.setText(user.getFanscount() + "");
						favoriteNum.setText(user.getFavoritecount() + "");
					}else{
						// 加载默认头像
						userFace.setImageResource(R.drawable.widget_dface);
						// 信息设置为0
						userName.setText("未知");
						watchNum.setText("0");
						fansNum.setText("0");
						favoriteNum.setText("0");
					}
				} else if (msg.obj != null) {
					Log.i(TAG, "用户信息读取失败");
					((AppException) msg.obj).makeToast(MainActivity.this);
				}
			}
		};
		this.loadUserInfoThread(false);

	}

	private Handler mHandler;
	private MyInformation user;

	private void loadUserInfoThread(final boolean isRefresh) {
		new Thread() {
			public void run() {
				Message msg = new Message();
				try {
					MyInformation user = ((AppContext) getApplication())
							.getMyInformation(isRefresh);
					msg.what = 1;
					msg.obj = user;
				} catch (AppException e) {
					e.printStackTrace();
					msg.what = -1;
					msg.obj = e;
				}
				mHandler.sendMessage(msg);
			}
		}.start();
	}

	private View.OnClickListener onClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			resetRadioButton();
			FragmentManager fManager = getSupportFragmentManager();
			FragmentTransaction fTransaction = fManager.beginTransaction();
			switch (v.getId()) {
			case R.id.main_footbar_news:
				fbNews.setChecked(true);
				fTransaction.replace(R.id.main_activity_linearlayout,
						new NewsMainFragment()).commit();
				break;
			case R.id.main_footbar_question:
				fbQuestion.setChecked(true);
				fTransaction.replace(R.id.main_activity_linearlayout,
						new QuestionMainFragment()).commit();
				break;
			case R.id.main_footbar_tweet:
				fbTweet.setChecked(true);
				fTransaction.replace(R.id.main_activity_linearlayout,
						new TweetMainFragment()).commit();
				break;
			case R.id.main_footbar_user:
				fbactive.setChecked(true);
				fTransaction.replace(R.id.main_activity_linearlayout,
						new ActiveMainFragment()).commit();
				break;

			default:
				break;
			}
		}
	};

	class MainToggle extends ActionBarDrawerToggle {

		public MainToggle() {
			super(MainActivity.this, mDrawerLayout,
					R.drawable.ic_navigation_drawer, R.string.drawer_open,
					R.string.drawer_close);
		}

		public MainToggle(Activity activity, DrawerLayout drawerLayout,
				int drawerImageRes, int openDrawerContentDescRes,
				int closeDrawerContentDescRes) {
			super(activity, drawerLayout, drawerImageRes,
					openDrawerContentDescRes, closeDrawerContentDescRes);
			// TODO Auto-generated constructor stub
		}

		@Override
		public void onDrawerOpened(View drawerView) {
			super.onDrawerOpened(drawerView);
			if (user != null)
				Log.i("MAIN", "user" + user.getId() + " login"
						+ AppContext.getAppContext().getLoginUid());
			// if(user != null && user.getId() !=
			// AppContext.getAppContext().getLoginUid()){
			loadUserInfoThread(true);
		}

		@Override
		public void onDrawerClosed(View drawerView) {
			// TODO Auto-generated method stub
			super.onDrawerClosed(drawerView);
		}

	}

	private void resetRadioButton() {
		fbNews.setChecked(false);
		fbQuestion.setChecked(false);
		fbTweet.setChecked(false);
		fbactive.setChecked(false);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// super.onSaveInstanceState(outState);
		// outState.putInt("FooterButtonState", value);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(tweetReceiver);
	}

	/**
	 * 轮询通知信息
	 */
	private void foreachUserNotice() {
		final int uid = appContext.getLoginUid();
		final Handler handler = new Handler() {
			public void handleMessage(Message msg) {
				if (msg.what == 1) {
					UIHelper.sendBroadCast(MainActivity.this, (Notice) msg.obj);
				}
				foreachUserNotice();// 回调
			}
		};
		new Thread() {
			public void run() {
				Message msg = new Message();
				try {
					sleep(60 * 1000);
					if (uid > 0) {
						Notice notice = appContext.getUserNotice(uid);
						msg.what = 1;
						msg.obj = notice;
					} else {
						msg.what = 0;
					}
				} catch (AppException e) {
					e.printStackTrace();
					msg.what = -1;
				} catch (Exception e) {
					e.printStackTrace();
					msg.what = -1;
				}
				handler.sendMessage(msg);
			}
		}.start();
	}

	public class TweetReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(final Context context, Intent intent) {
			int what = intent.getIntExtra("MSG_WHAT", 0);
			if (what == 1) {
				Result res = (Result) intent.getSerializableExtra("RESULT");
				UIHelper.ToastMessage(context, res.getErrorMessage(), 1000);
				if (res.OK()) {
					// 发送通知广播
					if (res.getNotice() != null) {
						UIHelper.sendBroadCast(context, res.getNotice());
					}
					// 发完动弹后-刷新最新动弹、我的动弹&最新动态(当前界面必须是动弹|动态)
				}
			} else {
				final Tweet tweet = (Tweet) intent
						.getSerializableExtra("TWEET");
				final Handler handler = new Handler() {
					public void handleMessage(Message msg) {
						if (msg.what == 1) {
							Result res = (Result) msg.obj;
							UIHelper.ToastMessage(context,
									res.getErrorMessage(), 1000);
							if (res.OK()) {
								// 发送通知广播
								if (res.getNotice() != null) {
									UIHelper.sendBroadCast(context,
											res.getNotice());
								}
							}
						} else {
							((AppException) msg.obj).makeToast(context);
							if (TweetPub.mContext != null
									&& TweetPub.mMessage != null)
								TweetPub.mMessage.setVisibility(View.GONE);
						}
					}
				};
				Thread thread = new Thread() {
					public void run() {
						Message msg = new Message();
						try {
							Result res = appContext.pubTweet(tweet);
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
				if (TweetPub.mContext != null)
					UIHelper.showResendTweetDialog(TweetPub.mContext, thread);
				else
					UIHelper.showResendTweetDialog(context, thread);
			}
		}
	}

	public void setFooterViewVisibility(int v) {
		AnimUtil.FooterViewAnim(MainActivity.this, footerLayout, v);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater mInflater = getMenuInflater();
		mInflater.inflate(R.menu.mian_activity_menu, menu);
		MenuItem item = menu.findItem(R.id.main_menu_login);
		if (appContext.getLoginUid()>0) {
			item.setTitle(R.string.main_menu_logout);
		}
		return true;
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		mMaintoggle.onConfigurationChanged(newConfig);
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		mMaintoggle.syncState();
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		super.onOptionsItemSelected(item);
		if (mMaintoggle.onOptionsItemSelected(item)) {
			return true;
		}
		switch (item.getItemId()) {
		case R.id.main_menu_login:
			UIHelper.loginOrLogout(MainActivity.this,
					new LoginDialogListener() {

						@Override
						public void isLogin(boolean bool) {
							if (bool)
								item.setTitle(R.string.main_menu_logout);
							else
								item.setTitle(R.string.main_menu_login);
						}
					});
			return true;

		case R.id.main_menu_user:
			UIHelper.showUserInfo(MainActivity.this);
			return true;

		case R.id.main_menu_open:
			UIHelper.showSoftware(MainActivity.this);
			return true;

		case R.id.main_menu_capture:
			UIHelper.showCapture(MainActivity.this);
			return true;

		case R.id.main_menu_setting:
			UIHelper.showSetting(MainActivity.this);
			return true;

		case R.id.main_menu_about:
			UIHelper.showAbout(MainActivity.this);
			return true;

		default:
			return false;
		}
	}

	public void setProgressBarVisibility(int v) {
		mProgressBar.setVisibility(v);
	}

	@Override
	public void onClick(final View v) {
		int fans;
		int followers;
		switch (v.getId()) {
		case R.id.Favorites_num:
			UIHelper.showUserFavorite(v.getContext());
			break;
		case R.id.watch_num:
			followers = user != null ? user.getFollowerscount() : 0;
			fans = user != null ? user.getFanscount() : 0;
			UIHelper.showUserFriend(v.getContext(), FriendList.TYPE_FOLLOWER,
					followers, fans);
			break;
		case R.id.fans_num:
			followers = user != null ? user.getFollowerscount() : 0;
			fans = user != null ? user.getFanscount() : 0;
			UIHelper.showUserFriend(v.getContext(), FriendList.TYPE_FANS,
					followers, fans);
			break;
		case R.id.user_info_userface:
			UIHelper.showUserInfo(MainActivity.this);
			break;
		case R.id.drawer_soft:
			UIHelper.showSoftware(MainActivity.this);
			break;
		case R.id.drawer_capture:
			UIHelper.showCapture(MainActivity.this);
			break;
		case R.id.drawer_setting:
			UIHelper.showSetting(MainActivity.this);
			break;
		case R.id.drawer_longout:
			UIHelper.loginOrLogout(MainActivity.this,
					new LoginDialogListener() {

						@Override
						public void isLogin(boolean bool) {
							if (bool)
								((TextView)v).setText(R.string.main_menu_logout);
							else
								((TextView)v).setText(R.string.main_menu_login);
						}
					});
			break;

		default:
			break;
		}

	}

}
