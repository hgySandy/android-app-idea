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
import net.oschina.app.adapter.ListViewMessageAdapter;
import net.oschina.app.bean.ListData;
import net.oschina.app.bean.MessageList;
import net.oschina.app.bean.Messages;
import net.oschina.app.bean.Notice;
import net.oschina.app.bean.Result;
import net.oschina.app.common.BadgeManager;
import net.oschina.app.common.HandlerManager;
import net.oschina.app.common.StringUtils;
import net.oschina.app.common.UIHelper;
import net.oschina.app.common.HandlerManager.LoadListDataCallbacks;
import net.oschina.app.widget.PullToRefreshListView;
import net.oschina.designapp.R;

public class MessageFragment extends ListFragment implements LoadListDataCallbacks{
	
	// 全局Context
	private AppContext appContext = AppContext.getAppContext();
	private List<Messages> lvMsgData = new ArrayList<Messages>();
	private Handler mHandler;
	private int lvMsgSumData;
	private boolean isInitListView = false;
	
	private int catalog = MessageList.CATALOG_MESSAGE;
	private View layout;
	private Button login;
	private PullToRefreshListView lvActive;
	private PullToRefreshListView.OnRefreshListener refreshListener;

	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		layout = inflater.inflate(R.layout.fragment_news, container, false);
		lvActive = (PullToRefreshListView) layout.findViewById(R.id.frame_listview_news);
		lvActive.setFootView(footerView);
		login = (Button) layout.findViewById(R.id.fragment_mian_login);
		
		int uid = appContext.getLoginUid();
		// 判断用户是否登陆了。未登录的话显示登陆按钮
		if (uid == 0) {
			login.setVisibility(View.VISIBLE);
			lvActive.setVisibility(View.GONE);
			login.setOnClickListener(butOnClickListener);
		} else {
			initListView(catalog, lvActive, UIHelper.LISTVIEW_ACTION_INIT);
		}
		return layout;
	}
	
	private View.OnClickListener butOnClickListener = new View.OnClickListener() {
		
		public void onClick(View v) {
			//UIHelper.showLoginDialog(getActivity());
			LoginDialogFragment dialog = new LoginDialogFragment();
			dialog.setLoginDialogListener(dialogListener);
			dialog.show(getFragmentManager(), LoginDialogFragment.TAG);
		}
	};
	
	public void setUserVisibleHint(boolean isVisibleToUser) {
		super.setUserVisibleHint(isVisibleToUser);
		// 当页面重新显示时，判断是否已经登陆了。是的话隐藏登陆按钮，加载列表数据。
		int uid = appContext.getLoginUid();
		if (isVisibleToUser && uid!=0 && !isInitListView && login!=null) {
			login.setVisibility(View.GONE);
			lvActive.setVisibility(View.VISIBLE);
			initListView(catalog, lvActive, UIHelper.LISTVIEW_ACTION_INIT);
		}
	}

	public void onDestroyView() {
		super.onDestroyView();
		isInitListView = false;
	}

	private LoginDialogFragment.LoginDialogListener dialogListener = new LoginDialogFragment.LoginDialogListener() {
		
		public void isLogin(boolean bool) {
			System.out.print("DialogListener: "+bool);
			if (bool) {
				login.setVisibility(View.GONE);
				lvActive.setVisibility(View.VISIBLE);
				// 加载列表
				initListView(catalog, lvActive ,UIHelper.LISTVIEW_ACTION_INIT);
			}
		}
	};
	
	/**
	 * 初始化留言列表
	 */
	protected void initListView(final int catalog , final PullToRefreshListView listView, int action) {
		final ListViewMessageAdapter lvMsgAdapter = new ListViewMessageAdapter(getActivity(), lvMsgData,
				R.layout.message_listitem);
		final View lvMsg_footer = getActivity().getLayoutInflater().inflate(R.layout.listview_footer,
				null);
		final TextView lvMsg_foot_more = (TextView) lvMsg_footer
				.findViewById(R.id.listview_foot_more);
		final HandlerManager handlerUtil = HandlerManager.getInstance();
		mHandler = handlerUtil.getHandler(listView, lvMsgAdapter,
				lvMsg_foot_more, abProgress, this, AppContext.PAGE_SIZE);
		loadLvData(catalog, 0, mHandler, action);
		listView.addFooterView(lvMsg_footer);// 添加底部视图 必须在setAdapter前
		listView.setAdapter(lvMsgAdapter);
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// 点击头部、底部栏无效
				if (position == 0 || view == lvMsg_footer)
					return;

				Messages msg = null;
				// 判断是否是TextView
				if (view instanceof TextView) {
					msg = (Messages) view.getTag();
				} else {
					TextView tv = (TextView) view
							.findViewById(R.id.message_listitem_username);
					msg = (Messages) tv.getTag();
				}
				if (msg == null)
					return;

				// 跳转到留言详情
				UIHelper.showMessageDetail(view.getContext(),
						msg.getFriendId(), msg.getFriendName());
			}
		});
		listView.setOnScrollListener(new AbsListView.OnScrollListener() {
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				listView.onScrollStateChanged(view, scrollState);

				// 数据为空--不用继续下面代码了
				if (lvMsgData.isEmpty())
					return;

				// 判断是否滚动到底部
				boolean scrollEnd = false;
				try {
					if (view.getPositionForView(lvMsg_footer) == view
							.getLastVisiblePosition())
						scrollEnd = true;
				} catch (Exception e) {
					scrollEnd = false;
				}

				int lvDataState = StringUtils.toInt(listView.getTag());
				if (scrollEnd && lvDataState == UIHelper.LISTVIEW_DATA_MORE) {
					listView.setTag(UIHelper.LISTVIEW_DATA_LOADING);
					lvMsg_foot_more.setText(R.string.load_ing);
					// 当前pageIndex
					int pageIndex = lvMsgSumData / AppContext.PAGE_SIZE;
					loadLvData(catalog, pageIndex, mHandler,
							UIHelper.LISTVIEW_ACTION_SCROLL);
				}
			}

			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				listView.onScroll(view, firstVisibleItem, visibleItemCount,
						totalItemCount);
			}
		});
		listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {
				// 点击头部、底部栏无效
				if (position == 0 || view == lvMsg_footer)
					return false;

				Messages _msg = null;
				// 判断是否是TextView
				if (view instanceof TextView) {
					_msg = (Messages) view.getTag();
				} else {
					TextView tv = (TextView) view
							.findViewById(R.id.message_listitem_username);
					_msg = (Messages) tv.getTag();
				}
				if (_msg == null)
					return false;

				final Messages message = _msg;

				// 选择操作
				final Handler handler = new Handler() {
					public void handleMessage(Message msg) {
						if (msg.what == 1) {
							Result res = (Result) msg.obj;
							if (res.OK()) {
								lvMsgData.remove(message);
								lvMsgAdapter.notifyDataSetChanged();
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
							Result res = appContext.delMessage(
									appContext.getLoginUid(),
									message.getFriendId());
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
				UIHelper.showMessageListOptionDialog(getActivity(), message, thread);
				return true;
			}
		});
		refreshListener = new PullToRefreshListView.OnRefreshListener() {
			public void onRefresh() {
				// 处理通知信息
				if (BadgeManager.getInstance().isShowMessage())
					handlerUtil.ClearNotice(Notice.TYPE_MESSAGE);
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
	 * 线程加载留言数据
	 * 
	 * @param pageIndex
	 *            当前页数
	 * @param handler
	 * @param action
	 */
	protected void loadLvData(final int catalog, final int pageIndex, final Handler handler,
			final int action) {
		abProgress.setProgressBarVisibility(View.VISIBLE);
		new Thread() {
			public void run() {
				Message msg = new Message();
				boolean isRefresh = false;
				if (action == UIHelper.LISTVIEW_ACTION_REFRESH
						|| action == UIHelper.LISTVIEW_ACTION_SCROLL)
					isRefresh = true;
				try {
					MessageList list = appContext.getMessageList(pageIndex,
							isRefresh);
					msg.what = list.getPageSize();
					msg.obj = list;
				} catch (AppException e) {
					e.printStackTrace();
					msg.what = -1;
					msg.obj = e;
				}
				msg.arg1 = action;
				msg.arg2 = UIHelper.LISTVIEW_DATATYPE_MESSAGE;
				handler.sendMessage(msg);
			}
		}.start();
	}
	
	public boolean isInitListView(){
		return isInitListView;
	}

	public void onLoadDataFinished(ListData dataList) {
		lvMsgData.clear();
		lvMsgData.addAll(dataList.getMsgData());
		lvMsgSumData = dataList.getMsgSumData();
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
		return lvMsgData;
	}
}
