package net.oschina.app.fragment;

import java.util.ArrayList;
import java.util.List;


import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.TextView;
import net.oschina.app.AppContext;
import net.oschina.app.AppException;
import net.oschina.app.adapter.ListViewQuestionAdapter;
import net.oschina.app.bean.ListData;
import net.oschina.app.bean.NewsList;
import net.oschina.app.bean.Post;
import net.oschina.app.bean.PostList;
import net.oschina.app.common.HandlerManager;
import net.oschina.app.common.StringUtils;
import net.oschina.app.common.UIHelper;
import net.oschina.app.common.HandlerManager.LoadListDataCallbacks;
import net.oschina.app.widget.PullToRefreshListView;
import net.oschina.designapp.R;

public class QuestionFragment extends ListFragment implements LoadListDataCallbacks{
	
	private AppContext appContext;// 全局Context
	private Handler mHandler;
	private List<Post> lvQuestionData;
	private int lvQuestionSumData;

	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_news, container, false);
		int curQuestionCatalog = getArguments().getInt(NewsList.CATLOG, PostList.CATALOG_ASK);
		appContext = (AppContext) getActivity().getApplication();
		lvQuestionData = new ArrayList<Post>();
		
		PullToRefreshListView lvQuestion = (PullToRefreshListView) view.findViewById(R.id.frame_listview_news);
		lvQuestion.setFootView(footerView);
		initListView(curQuestionCatalog, lvQuestion, UIHelper.LISTVIEW_ACTION_INIT);
		return view;
	}

	/**
	 * 初始化帖子列表
	 */
	protected void initListView(final int catalog , final PullToRefreshListView listView, int action) {
		ListViewQuestionAdapter lvQuestionAdapter = new ListViewQuestionAdapter(getActivity(), lvQuestionData, R.layout.question_listitem);
		final View lvQuestion_footer = getActivity().getLayoutInflater().inflate(R.layout.listview_footer, null);
		final TextView lvQuestion_foot_more = (TextView) lvQuestion_footer.findViewById(R.id.listview_foot_more);
		HandlerManager handlerUtil = HandlerManager.getInstance();
		mHandler = handlerUtil.
				getHandler(listView, lvQuestionAdapter, lvQuestion_foot_more, abProgress, this, AppContext.PAGE_SIZE);
		loadLvData(catalog, 0, mHandler, action);
		listView.addFooterView(lvQuestion_footer);// 添加底部视图 必须在setAdapter前
		listView.setAdapter(lvQuestionAdapter);
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				// 点击头部、底部栏无效
				if (position == 0 || view == lvQuestion_footer)
					return;

				Post post = null;
				// 判断是否是TextView
				if (view instanceof TextView) {
					post = (Post) view.getTag();
				} else {
					TextView tv = (TextView) view.findViewById(R.id.question_listitem_title);
					post = (Post) tv.getTag();
				}
				if (post == null)
					return;

				// 跳转到问答详情
				UIHelper.showQuestionDetail(view.getContext(), post.getId());
			}
		});
		listView.setOnScrollListener(new AbsListView.OnScrollListener() {
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				listView.onScrollStateChanged(view, scrollState);

				// 数据为空--不用继续下面代码了
				if (lvQuestionData.isEmpty())
					return;

				// 判断是否滚动到底部
				boolean scrollEnd = false;
				try {
					if (view.getPositionForView(lvQuestion_footer) == view.getLastVisiblePosition())
						scrollEnd = true;
				} catch (Exception e) {
					scrollEnd = false;
				}

				int lvDataState = StringUtils.toInt(listView.getTag());
				if (scrollEnd && lvDataState == UIHelper.LISTVIEW_DATA_MORE) {
					listView.setTag(UIHelper.LISTVIEW_DATA_LOADING);
					lvQuestion_foot_more.setText(R.string.load_ing);
					// 当前pageIndex
					int pageIndex = lvQuestionSumData / AppContext.PAGE_SIZE;
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
	 * 线程加载帖子数据
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
		abProgress.setProgressBarVisibility(View.VISIBLE);
		new Thread() {
			public void run() {
				Message msg = new Message();
				boolean isRefresh = false;
				if (action == UIHelper.LISTVIEW_ACTION_REFRESH
						|| action == UIHelper.LISTVIEW_ACTION_SCROLL)
					isRefresh = true;
				try {
					PostList list = appContext.getPostList(catalog, pageIndex,
							isRefresh);
					msg.what = list.getPageSize();
					msg.obj = list;
				} catch (AppException e) {
					e.printStackTrace();
					msg.what = -1;
					msg.obj = e;
				}
				msg.arg1 = action;
				msg.arg2 = UIHelper.LISTVIEW_DATATYPE_POST;
				handler.sendMessage(msg);
			}
		}.start();
	}

	public void onLoadDataFinished(ListData dataList) {
		lvQuestionData.clear();
		lvQuestionData.addAll(dataList.getQuestionData());
		lvQuestionSumData = dataList.getQuestionSumData();	
	}

	@Override
	public List<?> getListData() {
		// TODO Auto-generated method stub
		return lvQuestionData;
	}
}
