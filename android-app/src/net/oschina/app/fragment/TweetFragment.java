package net.oschina.app.fragment;

import java.util.ArrayList;
import java.util.List;


import net.oschina.app.AppContext;
import net.oschina.app.AppException;
import net.oschina.app.adapter.ListViewTweetAdapter;
import net.oschina.app.bean.ListData;
import net.oschina.app.bean.NewsList;
import net.oschina.app.bean.Result;
import net.oschina.app.bean.Tweet;
import net.oschina.app.bean.TweetList;
import net.oschina.app.common.HandlerManager;
import net.oschina.app.common.StringUtils;
import net.oschina.app.common.UIHelper;
import net.oschina.app.common.HandlerManager.LoadListDataCallbacks;
import net.oschina.app.widget.PullToRefreshListView;
import net.oschina.designapp.R;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class TweetFragment extends ListFragment implements LoadListDataCallbacks {
	
	private AppContext appContext = AppContext.getAppContext();
	private Handler mHandler;
	private List<Tweet> lvTweetData = new ArrayList<Tweet>();
	private int lvTweetSumData;
	private boolean isInitListView = false;

	/**
	 * 初始化动弹列表
	 */
	protected void initListView(final int curTweetCatalog , final PullToRefreshListView lvTweet, int action) {
		final ListViewTweetAdapter lvTweetAdapter = new ListViewTweetAdapter(getActivity(), lvTweetData, R.layout.tweet_listitem);
		final View lvTweet_footer = getActivity().getLayoutInflater().inflate(R.layout.listview_footer,null);
		final TextView lvTweet_foot_more = (TextView) lvTweet_footer.findViewById(R.id.listview_foot_more);
		HandlerManager handlerUtil = HandlerManager.getInstance();
		mHandler = handlerUtil.getHandler(lvTweet, lvTweetAdapter, lvTweet_foot_more, abProgress, this, AppContext.PAGE_SIZE);
		loadLvData(curTweetCatalog, 0, mHandler, action);
		lvTweet.addFooterView(lvTweet_footer);// 添加底部视图 必须在setAdapter前
		lvTweet.setFootView(footerView);
		lvTweet.setAdapter(lvTweetAdapter);
		lvTweet.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, final View view,
					int position, long id) {
				// 点击头部、底部栏无效
				if (position == 0 || view == lvTweet_footer)
					return;

				Tweet tweet = null;
				// 判断是否是TextView
				if (view instanceof TextView) {
					tweet = (Tweet) view.getTag();
				} else {
					TextView tv = (TextView) view
							.findViewById(R.id.tweet_listitem_username);
					tweet = (Tweet) tv.getTag();
				}
				if (tweet == null)
					return;   			
				// 跳转到动弹详情&评论页面
				UIHelper.showTweetDetail(view.getContext(), tweet.getId());
			}
		});
		lvTweet.setOnScrollListener(new AbsListView.OnScrollListener() {
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				lvTweet.onScrollStateChanged(view, scrollState);

				// 数据为空--不用继续下面代码了
				if (lvTweetData.isEmpty())
					return;

				// 判断是否滚动到底部
				boolean scrollEnd = false;
				try {
					if (view.getPositionForView(lvTweet_footer) == view.getLastVisiblePosition())
						scrollEnd = true;
				} catch (Exception e) {
					scrollEnd = false;
				}

				int lvDataState = StringUtils.toInt(lvTweet.getTag());
				if (scrollEnd && lvDataState == UIHelper.LISTVIEW_DATA_MORE) {
					lvTweet.setTag(UIHelper.LISTVIEW_DATA_LOADING);
					lvTweet_foot_more.setText(R.string.load_ing);
					// 当前pageIndex
					int pageIndex = lvTweetSumData / AppContext.PAGE_SIZE;
					loadLvData(curTweetCatalog, pageIndex, mHandler,UIHelper.LISTVIEW_ACTION_SCROLL);
				}
			}

			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				lvTweet.onScroll(view, firstVisibleItem, visibleItemCount,
						totalItemCount);
			}
		});
		lvTweet.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {
				// 点击头部、底部栏无效
				if (position == 0 || view == lvTweet_footer)
					return false;

				Tweet _tweet = null;
				// 判断是否是TextView
				if (view instanceof TextView) {
					_tweet = (Tweet) view.getTag();
				} else {
					TextView tv = (TextView) view.findViewById(R.id.tweet_listitem_username);
					_tweet = (Tweet) tv.getTag();
				}
				if (_tweet == null)
					return false;

				final Tweet tweet = _tweet;

				// 删除操作
				// if(appContext.getLoginUid() == tweet.getAuthorId()) {
				final Handler handler = new Handler() {
					public void handleMessage(Message msg) {
						if (msg.what == 1) {
							Result res = (Result) msg.obj;
							if (res.OK()) {
								lvTweetData.remove(tweet);
								lvTweetAdapter.notifyDataSetChanged();
							}
							UIHelper.ToastMessage(getActivity(),
									res.getErrorMessage());
						} else {
							((AppException) msg.obj).makeToast(getActivity());
						}
					}
				};
				Thread thread = new Thread() {
					public void run() {
						Message msg = new Message();
						try {
							Result res = appContext.delTweet(
									appContext.getLoginUid(), tweet.getId());
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
				UIHelper.showTweetOptionDialog(getActivity(), thread);
				// } else {
				// UIHelper.showTweetOptionDialog(Main.this, null);
				// }
				return true;
			}
		});
		lvTweet.setOnRefreshListener(new PullToRefreshListView.OnRefreshListener() {
			public void onRefresh() {
				loadLvData(curTweetCatalog, 0, mHandler,
						UIHelper.LISTVIEW_ACTION_REFRESH);
			}
		});
		isInitListView = true;
	}
	
	@Override
	public void onDestroy() {
		if (mHandler != null)
			mHandler.removeCallbacksAndMessages(null);
		super.onDestroy();
	}
	
	/**
	 * 线程加载动弹数据
	 * 
	 * @param catalog
	 *            -1 热门，0 最新，大于0 某用户的动弹(uid)
	 * @param pageIndex
	 *            当前页数
	 * @param handler
	 *            处理器
	 * @param action
	 *            动作标识
	 */
	protected void loadLvData(final int catalog, final int pageIndex,
			final Handler handler, final int action) {
		abProgress.setProgressBarVisibility(View.VISIBLE);
		new Thread() {
			public void run() {
				Message msg = new Message();
				boolean isRefresh = false;
				if (action == UIHelper.LISTVIEW_ACTION_REFRESH
						|| action == UIHelper.LISTVIEW_ACTION_SCROLL)
					isRefresh = true;
				try {
					TweetList list = appContext.getTweetList(catalog,
							pageIndex, isRefresh);
					msg.what = list.getPageSize();
					msg.obj = list;
				} catch (AppException e) {
					e.printStackTrace();
					msg.what = -1;
					msg.obj = e;
				}
				msg.arg1 = action;
				msg.arg2 = UIHelper.LISTVIEW_DATATYPE_TWEET;
				handler.sendMessage(msg);
			}
		}.start();
	}
	
	public boolean isInitListView(){
		return isInitListView;
	}

	public void onLoadDataFinished(ListData dataList) {
		lvTweetData.clear();
		lvTweetData.addAll(dataList.getTweetData());
		lvTweetSumData = dataList.getTweetSumData();
	}

	@Override
	public List<?> getListData() {
		return lvTweetData;
	}
}
