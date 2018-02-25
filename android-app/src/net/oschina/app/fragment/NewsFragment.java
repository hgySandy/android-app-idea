package net.oschina.app.fragment;

import java.util.ArrayList;
import java.util.List;


import net.oschina.app.AppContext;
import net.oschina.app.AppException;
import net.oschina.app.adapter.ListViewNewsAdapter;
import net.oschina.app.bean.ListData;
import net.oschina.app.bean.News;
import net.oschina.app.bean.NewsList;
import net.oschina.app.common.HandlerManager;
import net.oschina.app.common.StringUtils;
import net.oschina.app.common.UIHelper;
import net.oschina.app.common.HandlerManager.LoadListDataCallbacks;
import net.oschina.app.widget.PullToRefreshListView;
import net.oschina.app.widget.PullToRefreshListView.OnRefreshListener;
import net.oschina.designapp.R;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.TextView;

public class NewsFragment extends ListFragment implements LoadListDataCallbacks{
	
	private List<News> lvNewsData = new ArrayList<News>();
	private int lvNewsSumData;
	private int curNewsCatalog;
	
	private AppContext appContext;// 全局Context
	private Handler mHandler;
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		curNewsCatalog = getArguments().getInt(NewsList.CATLOG, NewsList.CATALOG_ALL);
		appContext = (AppContext) getActivity().getApplication();
		
		View layout = inflater.inflate(R.layout.fragment_test, container, false);
		PullToRefreshListView lvNews = (PullToRefreshListView) layout.findViewById(R.id.fragment_test_listview);
		lvNews.setFootView(footerView);
		initListView(curNewsCatalog, lvNews, UIHelper.LISTVIEW_ACTION_INIT);
		return layout;
	}
	
	/**
	 * 初始化新闻列表
	 */
	protected void initListView(final int catalog, final PullToRefreshListView listView, int action) {
		final View lvNews_footer = getActivity().getLayoutInflater().inflate(R.layout.listview_footer, null);
		final TextView lvNews_foot_more = (TextView) lvNews_footer
				.findViewById(R.id.listview_foot_more);
		// 初始化Handler
		ListViewNewsAdapter lvNewsAdapter = new ListViewNewsAdapter(getActivity(), lvNewsData, R.layout.news_listitem);
		HandlerManager handlerUtil = HandlerManager.getInstance();
		mHandler = handlerUtil.
				getHandler(listView, lvNewsAdapter, lvNews_foot_more, abProgress, this, AppContext.PAGE_SIZE);
		loadLvData(catalog, 0, mHandler, action);
		listView.addFooterView(lvNews_footer);// 添加底部视图 必须在setAdapter前
		listView.setAdapter(lvNewsAdapter);
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// 点击头部、底部栏无效
				if (position == 0 || view == lvNews_footer)
					return;

				News news = null;
				// 判断是否是TextView
				if (view instanceof TextView) {
					news = (News) view.getTag();
				} else {
					TextView tv = (TextView) view
							.findViewById(R.id.news_listitem_title);
					news = (News) tv.getTag();
				}
				if (news == null)
					return;

				// 跳转到新闻详情
				UIHelper.showNewsRedirect(view.getContext(), news);
			}
		});
		listView.setOnScrollListener(new AbsListView.OnScrollListener() {
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				listView.onScrollStateChanged(view, scrollState);
				// 数据为空--不用继续下面代码了
				if (lvNewsData.isEmpty())
					return;
				// 判断是否滚动到底部
				boolean scrollEnd = false;
				try {
					if (view.getPositionForView(lvNews_footer) == view
							.getLastVisiblePosition())
						scrollEnd = true;
				} catch (Exception e) {
					scrollEnd = false;
				}

				int lvDataState = StringUtils.toInt(listView.getTag());
				if (scrollEnd && lvDataState == UIHelper.LISTVIEW_DATA_MORE) {
					listView.setTag(UIHelper.LISTVIEW_DATA_LOADING);
					lvNews_foot_more.setText(R.string.load_ing);
					// 当前pageIndex
					int pageIndex = lvNewsSumData / AppContext.PAGE_SIZE;
					loadLvData(catalog, pageIndex, mHandler,
							UIHelper.LISTVIEW_ACTION_SCROLL);
				}
			}

			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				listView.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
			}
		});
		listView.setOnRefreshListener(new OnRefreshListener() {
			@Override
			public void onRefresh() {
				loadLvData(catalog, 0, mHandler, UIHelper.LISTVIEW_ACTION_REFRESH);
			}
		});
	}
	
	@Override
	public void onDestroy() {
		if (mHandler != null)
			mHandler.removeCallbacksAndMessages(null);
		super.onDestroy();
	}

	/**
	 * 线程加载新闻数据
	 * 
	 * @param catalog
	 *            分类
	 * @param pageIndex
	 *            当前页数
	 * @param handler
	 *            处理器
	 * @param action
	 *            动作标识
	 */
	protected void loadLvData(final int catalog, final int pageIndex,
			final Handler handler, final int action) {
		super.abProgress.setProgressBarVisibility(View.VISIBLE);
		new Thread() {
			public void run() {
				Message msg = new Message();
				boolean isRefresh = false;
				if (action == UIHelper.LISTVIEW_ACTION_REFRESH
						|| action == UIHelper.LISTVIEW_ACTION_SCROLL)
					isRefresh = true;
				try {
					NewsList list = appContext.getNewsList(catalog, pageIndex,
							isRefresh);
					msg.what = list.getPageSize();
					msg.obj = list;
				} catch (AppException e) {
					e.printStackTrace();
					msg.what = -1;
					msg.obj = e;
				}
				msg.arg1 = action;
				msg.arg2 = UIHelper.LISTVIEW_DATATYPE_NEWS;
				handler.sendMessage(msg);
			}
		}.start();
	}

	public void onLoadDataFinished(ListData dataList) {
		lvNewsData.clear();
		lvNewsData.addAll(dataList.getNewsData());
		lvNewsSumData = dataList.getNewsSumData();
	}

	@Override
	public List<?> getListData() {
		return lvNewsData;
	}

}
