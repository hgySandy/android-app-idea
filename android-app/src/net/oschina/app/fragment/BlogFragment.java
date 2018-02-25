package net.oschina.app.fragment;

import java.util.ArrayList;
import java.util.List;


import net.oschina.app.AppContext;
import net.oschina.app.AppException;
import net.oschina.app.adapter.ListViewBlogAdapter;
import net.oschina.app.bean.Blog;
import net.oschina.app.bean.BlogList;
import net.oschina.app.bean.ListData;
import net.oschina.app.bean.NewsList;
import net.oschina.app.common.HandlerManager;
import net.oschina.app.common.StringUtils;
import net.oschina.app.common.UIHelper;
import net.oschina.app.common.HandlerManager.LoadListDataCallbacks;
import net.oschina.app.widget.PullToRefreshListView;
import net.oschina.designapp.R;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
/**
 * 
 * @author zhen lan4627@gmail.com
 * 2014-8-17 上午1:29:14
 */
public class BlogFragment extends ListFragment implements LoadListDataCallbacks{
	
	private AppContext appContext;// 全局Context
	private Handler mHandler;
	private List<Blog> lvBlogData;
	private int lvBlogSumData;

	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_blog, container, false);
		int curNewsCatalog = getArguments().getInt(NewsList.CATLOG, NewsList.CATALOG_ALL);
		appContext = (AppContext) getActivity().getApplication();
		lvBlogData = new ArrayList<Blog>();
		
		PullToRefreshListView lvBlog = (PullToRefreshListView) view.findViewById(R.id.frame_listview_blog);
		lvBlog.setFootView(footerView);
		initListView(curNewsCatalog, lvBlog, UIHelper.LISTVIEW_ACTION_INIT);

		return view;
	}

	/**
	 * 初始化博客列表
	 */
	protected void initListView(final int catalog , final PullToRefreshListView listView, int action) {
		ListViewBlogAdapter lvBlogAdapter = new ListViewBlogAdapter(getActivity(), BlogList.CATALOG_LATEST, lvBlogData, R.layout.blog_listitem);
		final View lvBlog_footer = getActivity().getLayoutInflater().inflate(R.layout.listview_footer, null);
		final TextView lvBlog_foot_more = (TextView) lvBlog_footer.findViewById(R.id.listview_foot_more);
		HandlerManager handlerUtil = HandlerManager.getInstance();
		mHandler = handlerUtil.getHandler(listView, lvBlogAdapter, lvBlog_foot_more, abProgress, this, AppContext.PAGE_SIZE);
		loadLvData(catalog, 0, mHandler, action);
		listView.addFooterView(lvBlog_footer);// 添加底部视图 必须在setAdapter前
		listView.setAdapter(lvBlogAdapter);
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				// 点击头部、底部栏无效
				if (position == 0 || view == lvBlog_footer)
					return;

				Blog blog = null;
				// 判断是否是TextView
				if (view instanceof TextView) {
					blog = (Blog) view.getTag();
				} else {
					TextView tv = (TextView) view.findViewById(R.id.blog_listitem_title);
					blog = (Blog) tv.getTag();
				}
				if (blog == null)
					return;

				// 跳转到博客详情
				UIHelper.showUrlRedirect(view.getContext(), blog.getUrl());
			}
		});
		listView.setOnScrollListener(new AbsListView.OnScrollListener() {
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				listView.onScrollStateChanged(view, scrollState);

				// 数据为空--不用继续下面代码了
				if (lvBlogData.isEmpty())
					return;

				// 判断是否滚动到底部
				boolean scrollEnd = false;
				try {
					if (view.getPositionForView(lvBlog_footer) == view.getLastVisiblePosition())
						scrollEnd = true;
				} catch (Exception e) {
					scrollEnd = false;
				}

				int lvDataState = StringUtils.toInt(listView.getTag());
				if (scrollEnd && lvDataState == UIHelper.LISTVIEW_DATA_MORE) {
					listView.setTag(UIHelper.LISTVIEW_DATA_LOADING);
					lvBlog_foot_more.setText(R.string.load_ing);
					// 当前pageIndex
					int pageIndex = lvBlogSumData / AppContext.PAGE_SIZE;
					loadLvData(catalog, pageIndex, mHandler, UIHelper.LISTVIEW_ACTION_SCROLL);
				}
			}

			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
				listView.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
			}
		});
		listView.setOnRefreshListener(new PullToRefreshListView.OnRefreshListener() {
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
	 * 线程加载博客数据
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
				String type = "";
				switch (catalog) {
				case BlogList.CATALOG_LATEST:
					type = BlogList.TYPE_LATEST;
					break;
				case BlogList.CATALOG_RECOMMEND:
					type = BlogList.TYPE_RECOMMEND;
					break;
				}
				try {
					BlogList list = appContext.getBlogList(type, pageIndex,
							isRefresh);
					msg.what = list.getPageSize();
					msg.obj = list;
				} catch (AppException e) {
					e.printStackTrace();
					msg.what = -1;
					msg.obj = e;
				}
				msg.arg1 = action;
				msg.arg2 = UIHelper.LISTVIEW_DATATYPE_BLOG;
				handler.sendMessage(msg);
			}
		}.start();
	}

	public void onLoadDataFinished(ListData dataList) {
		lvBlogData.clear();
		lvBlogSumData = dataList.getBlogSumData();
		lvBlogData.addAll(dataList.getBlogData());
	}

	@Override
	public List<?> getListData() {
		return lvBlogData;
	}
}
