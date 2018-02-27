package net.oschina.app.fragment;

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
import net.oschina.app.adapter.ListViewSoftwareAdapter;
import net.oschina.app.bean.ListData;
import net.oschina.app.bean.SoftwareList;
import net.oschina.app.bean.SoftwareList.Software;
import net.oschina.app.bean.URLs;
import net.oschina.app.common.HandlerManager;
import net.oschina.app.common.HandlerManager.LoadListDataCallbacks;
import net.oschina.app.common.StringUtils;
import net.oschina.app.common.UIHelper;
import net.oschina.app.widget.PullToRefreshListView;
import net.oschina.app.widget.PullToRefreshListView.OnRefreshListener;
import net.oschina.designapp.R;

import java.util.ArrayList;
import java.util.List;

public class SoftwareFragment extends ListFragment implements LoadListDataCallbacks{
	
	private List<Software> lvSoftwareData = new ArrayList<Software>();
	private int lvSoftwareSumData;
	private int curSoftwareCatalog;
	
	private AppContext appContext;// 全局Context
	private Handler mHandler;
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		curSoftwareCatalog = getArguments().getInt(MainFragment.CATLOG, SoftwareList.HEAD_TAG_RECOMMEND);
		appContext = (AppContext) getActivity().getApplication();
		
		View layout = inflater.inflate(R.layout.fragment_test, container, false);
		PullToRefreshListView lvSoftware = (PullToRefreshListView) layout.findViewById(R.id.fragment_test_listview);
		lvSoftware.setFootView(footerView);
		initListView(curSoftwareCatalog, lvSoftware, UIHelper.LISTVIEW_ACTION_INIT);
		return layout;
	}
	
	/**
	 * 初始化软件列表
	 */
	protected void initListView(final int catalog, final PullToRefreshListView listView, int action) {
		final View lvSoftware_footer = getActivity().getLayoutInflater().inflate(R.layout.listview_footer, null);
		final TextView lvSoftware_foot_more = (TextView) lvSoftware_footer
				.findViewById(R.id.listview_foot_more);
		// 初始化Handler
		ListViewSoftwareAdapter lvSoftwareAdapter = new ListViewSoftwareAdapter(getActivity(), lvSoftwareData, R.layout.software_listitem);
		HandlerManager handlerUtil = HandlerManager.getInstance();
		mHandler = handlerUtil.
				getHandler(listView, lvSoftwareAdapter, lvSoftware_foot_more, abProgress, this, AppContext.PAGE_SIZE);
		loadLvData(catalog, 0, mHandler, action);
		listView.addFooterView(lvSoftware_footer);// 添加底部视图 必须在setAdapter前
		listView.setAdapter(lvSoftwareAdapter);
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// 点击头部、底部栏无效
				if (position == 0 || view == lvSoftware_footer)
					return;

				Software software = null;
				TextView tv = (TextView) view
						.findViewById(R.id.software_listitem_name);
				software = (Software) tv.getTag();
				if (software == null)
					return;
				URLs urls = URLs.parseURL(software.url);
				String indent = urls.getObjKey();

				// 跳转到详情
				UIHelper.showSoftwareDetail(view.getContext(), indent);
			}
		});
		listView.setOnScrollListener(new AbsListView.OnScrollListener() {
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				listView.onScrollStateChanged(view, scrollState);
				// 数据为空--不用继续下面代码了
				if (lvSoftwareData.isEmpty())
					return;
				// 判断是否滚动到底部
				boolean scrollEnd = false;
				try {
					if (view.getPositionForView(lvSoftware_footer) == view
							.getLastVisiblePosition())
						scrollEnd = true;
				} catch (Exception e) {
					scrollEnd = false;
				}

				int lvDataState = StringUtils.toInt(listView.getTag());
				if (scrollEnd && lvDataState == UIHelper.LISTVIEW_DATA_MORE) {
					listView.setTag(UIHelper.LISTVIEW_DATA_LOADING);
					lvSoftware_foot_more.setText(R.string.load_ing);
					// 当前pageIndex
					int pageIndex = lvSoftwareSumData / AppContext.PAGE_SIZE;
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

		final String searchTag = getSearchTag(catalog);

		new Thread() {
			public void run() {
				Message msg = new Message();
				boolean isRefresh = false;
				if (action == UIHelper.LISTVIEW_ACTION_REFRESH
						|| action == UIHelper.LISTVIEW_ACTION_SCROLL)
					isRefresh = true;
				try {
					SoftwareList list = appContext.getSoftwareList(searchTag, pageIndex,
							isRefresh);
					msg.what = list.getPageSize();
					msg.obj = list;
				} catch (AppException e) {
					e.printStackTrace();
					msg.what = -1;
					msg.obj = e;
				}
				msg.arg1 = action;
				msg.arg2 = UIHelper.LISTVIEW_DATATYPE_SOFTWARE;
				handler.sendMessage(msg);
			}
		}.start();
	}

	public String getSearchTag(int catalog){
		String searchTag = null;
		switch (catalog){
			case SoftwareList.HEAD_TAG_RECOMMEND:
				searchTag = SoftwareList.TAG_RECOMMEND;
				break;
			case SoftwareList.HEAD_TAG_LASTEST:
				searchTag = SoftwareList.TAG_LASTEST;
				break;
			case SoftwareList.HEAD_TAG_HOT:
				searchTag = SoftwareList.TAG_HOT;
				break;
			case SoftwareList.HEAD_TAG_CHINA:
				searchTag = SoftwareList.TAG_CHINA;
				break;
		}
		return searchTag;
	}

	public void onLoadDataFinished(ListData dataList) {
		lvSoftwareData.clear();
		lvSoftwareData.addAll(dataList.getSoftwareData());
		lvSoftwareSumData = dataList.getSoftwareSumData();
	}

	@Override
	public List<?> getListData() {
		return lvSoftwareData;
	}

}
