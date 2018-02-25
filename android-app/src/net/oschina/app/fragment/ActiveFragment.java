package net.oschina.app.fragment;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.TextView;
import net.oschina.app.AppContext;
import net.oschina.app.AppException;
import net.oschina.app.adapter.ListViewActiveAdapter;
import net.oschina.app.bean.Active;
import net.oschina.app.bean.ActiveList;
import net.oschina.app.bean.ListData;
import net.oschina.app.bean.NewsList;
import net.oschina.app.bean.Notice;
import net.oschina.app.common.BadgeManager;
import net.oschina.app.common.HandlerManager;
import net.oschina.app.common.StringUtils;
import net.oschina.app.common.UIHelper;
import net.oschina.app.common.HandlerManager.LoadListDataCallbacks;
import net.oschina.app.widget.PullToRefreshListView;
import net.oschina.designapp.R;

public class ActiveFragment extends ListFragment implements LoadListDataCallbacks {

	private List<Active> lvActiveData = new ArrayList<Active>();
	private AppContext appContext = AppContext.getAppContext();// 全局Context
	private Handler mHandler;
	private PullToRefreshListView lvActive;
	private PullToRefreshListView.OnRefreshListener refreshListener;
	private Button login;
	
	private int lvActiveSumData;
	private int curActiveCatalog;
	private boolean isInitListView = false;

	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		curActiveCatalog = getArguments().getInt(NewsList.CATLOG, ActiveList.CATALOG_LASTEST);
		View layout = inflater.inflate(R.layout.fragment_news, container, false);
		lvActive = (PullToRefreshListView) layout.findViewById(R.id.frame_listview_news);
		lvActive.setFootView(footerView);
		login = (Button) layout.findViewById(R.id.fragment_mian_login);
		int uid = appContext.getLoginUid();
		System.out.println("ActiveFragment-onStart"+uid);
		if (uid == 0) {
			login.setVisibility(View.VISIBLE);
			lvActive.setVisibility(View.GONE);
			login.setOnClickListener(butOnClickListener);
		} else {
			initListView(curActiveCatalog, lvActive, UIHelper.LISTVIEW_ACTION_INIT);
		}
		return layout;
	}
	
	@Override
	public void setUserVisibleHint(boolean isVisibleToUser) {
		super.setUserVisibleHint(isVisibleToUser);
		int uid = appContext.getLoginUid();
		if (isVisibleToUser && uid!=0 && !isInitListView && login!=null) {
			login.setVisibility(View.GONE);
			lvActive.setVisibility(View.VISIBLE);
			initListView(curActiveCatalog, lvActive, UIHelper.LISTVIEW_ACTION_INIT);
		}
	}
	
	@Override
	public void onDestroyView() {
		super.onDestroyView();
		isInitListView = false;
	}

	private View.OnClickListener butOnClickListener = new View.OnClickListener() {

		public void onClick(View v) {
			// UIHelper.showLoginDialog(getActivity());
			LoginDialogFragment dialog = new LoginDialogFragment();
			dialog.setLoginDialogListener(dialogListener);
			dialog.show(getFragmentManager(), LoginDialogFragment.TAG);
		}
	};

	private LoginDialogFragment.LoginDialogListener dialogListener = new LoginDialogFragment.LoginDialogListener() {

		public void isLogin(boolean bool) {
			if (bool) {
				login.setVisibility(View.GONE);
				lvActive.setVisibility(View.VISIBLE);
				// 加载列表
				initListView(curActiveCatalog, lvActive, UIHelper.LISTVIEW_ACTION_INIT);
			}
		}
	};

	/**
	 * 初始化动态列表
	 */
	protected void initListView(final int catalog, final PullToRefreshListView listView, int action) {
		final ListViewActiveAdapter lvActiveAdapter = new ListViewActiveAdapter(getActivity(), lvActiveData, R.layout.active_listitem);
		final View lvActive_footer = getActivity().getLayoutInflater().inflate(R.layout.listview_footer, null);
		final TextView lvActive_foot_more = (TextView) lvActive_footer.findViewById(R.id.listview_foot_more);
		final HandlerManager handlerUtil = HandlerManager.getInstance();
		mHandler = handlerUtil.getHandler(
				listView,
				lvActiveAdapter,
				lvActive_foot_more,
				abProgress,
				ActiveFragment.this,
				AppContext.PAGE_SIZE);
		loadLvData(catalog, 0, mHandler, action);
		listView.addFooterView(lvActive_footer);// 添加底部视图 必须在setAdapter前
		listView.setFootView(footerView);
		listView.setAdapter(lvActiveAdapter);
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				// 点击头部、底部栏无效
				if (position == 0 || view == lvActive_footer)
					return;

				Active active = null;
				// 判断是否是TextView
				if (view instanceof TextView) {
					active = (Active) view.getTag();
				} else {
					TextView tv = (TextView) view.findViewById(R.id.active_listitem_username);
					active = (Active) tv.getTag();
				}
				if (active == null)
					return;

				// 跳转
				UIHelper.showActiveRedirect(view.getContext(), active);
			}
		});
		listView.setOnScrollListener(new AbsListView.OnScrollListener() {
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				listView.onScrollStateChanged(view, scrollState);

				// 数据为空--不用继续下面代码了
				if (lvActiveData.isEmpty())
					return;

				// 判断是否滚动到底部
				boolean scrollEnd = false;
				try {
					if (view.getPositionForView(lvActive_footer) == view.getLastVisiblePosition())
						scrollEnd = true;
				} catch (Exception e) {
					scrollEnd = false;
				}

				int lvDataState = StringUtils.toInt(listView.getTag());
				if (scrollEnd && lvDataState == UIHelper.LISTVIEW_DATA_MORE) {
					listView.setTag(UIHelper.LISTVIEW_DATA_LOADING);
					lvActive_foot_more.setText(R.string.load_ing);
					// 当前pageIndex
					int pageIndex = lvActiveSumData / AppContext.PAGE_SIZE;
					loadLvData(catalog, pageIndex, mHandler, UIHelper.LISTVIEW_ACTION_SCROLL);
				}
			}

			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
				listView.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
			}
		});
		refreshListener = new PullToRefreshListView.OnRefreshListener() {
			public void onRefresh() {
				// 处理通知信息
				BadgeManager bManager = BadgeManager.getInstance();
				if (catalog == ActiveList.CATALOG_ATME && bManager.isShowAtme()) {
					handlerUtil.ClearNotice(Notice.TYPE_ATME);
				} else if (catalog == ActiveList.CATALOG_COMMENT
						&& bManager.isShowComment()) {
					handlerUtil.ClearNotice(Notice.TYPE_COMMENT);
				}
				// 刷新数据
				loadLvData(catalog, 0, mHandler, UIHelper.LISTVIEW_ACTION_REFRESH);
			}
		};
		listView.setOnRefreshListener(refreshListener);
		isInitListView = true;
	}
	
	@Override
	public void onDestroy() {
		if (mHandler != null)
			mHandler.removeCallbacksAndMessages(null);
		super.onDestroy();
	}

	/**
	 * 线程加载动态数据
	 * 
	 * @param catalog
	 * @param pageIndex
	 *            当前页数
	 * @param handler
	 * @param action
	 */
	protected void loadLvData(final int catalog, final int pageIndex, final Handler handler, final int action) {
		abProgress.setProgressBarVisibility(View.VISIBLE);
		new Thread() {
			public void run() {
				Message msg = new Message();
				boolean isRefresh = false;
				if (action == UIHelper.LISTVIEW_ACTION_REFRESH || action == UIHelper.LISTVIEW_ACTION_SCROLL)
					isRefresh = true;
				try {
					ActiveList list = appContext.getActiveList(catalog, pageIndex, isRefresh);
					msg.what = list.getPageSize();
					msg.obj = list;
				} catch (AppException e) {
					e.printStackTrace();
					msg.what = -1;
					msg.obj = e;
				}
				msg.arg1 = action;
				msg.arg2 = UIHelper.LISTVIEW_DATATYPE_ACTIVE;
				if (curActiveCatalog == catalog)
					handler.sendMessage(msg);
			}
		}.start();
	}
	
	public boolean isInitListView(){
		return isInitListView;
	}

	public void onLoadDataFinished(ListData dataList) {
		lvActiveData.clear();
		lvActiveData.addAll(dataList.getActiveData());
		lvActiveSumData = dataList.getActiveSumData();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.active_refresh_menu, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);
		switch (item.getItemId()) {
		case R.id.active_fragment_refresh:
			refreshListener.onRefresh();
			return true;

		default:
			return false;
		}
	}

	@Override
	public List<?> getListData() {
		// TODO Auto-generated method stub
		return lvActiveData;
	}
	
}
