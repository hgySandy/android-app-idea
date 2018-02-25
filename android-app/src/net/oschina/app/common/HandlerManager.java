package net.oschina.app.common;

import java.util.Date;
import java.util.List;


import net.oschina.app.AppContext;
import net.oschina.app.AppException;
import net.oschina.app.adapter.ListViewSearchAdapter;
import net.oschina.app.bean.Active;
import net.oschina.app.bean.ActiveList;
import net.oschina.app.bean.Blog;
import net.oschina.app.bean.BlogList;
import net.oschina.app.bean.Entity;
import net.oschina.app.bean.ListData;
import net.oschina.app.bean.MessageList;
import net.oschina.app.bean.Messages;
import net.oschina.app.bean.News;
import net.oschina.app.bean.NewsList;
import net.oschina.app.bean.Notice;
import net.oschina.app.bean.Post;
import net.oschina.app.bean.PostList;
import net.oschina.app.bean.Result;
import net.oschina.app.bean.SearchList;
import net.oschina.app.bean.Tweet;
import net.oschina.app.bean.TweetList;
import net.oschina.app.inteface.ActionBarProgressBarVisibility;
import net.oschina.app.widget.NewDataToast;
import net.oschina.app.widget.PullToRefreshListView;
import net.oschina.designapp.R;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class HandlerManager {

	private AppContext appContext;// 全局Context
	
	private ListData data;
	//private boolean isClearNotice = false;
	private int curClearNoticeType = 0;
	
	private volatile static HandlerManager h;
	
	private HandlerManager() {
		data = new ListData();
	}
	
	public static HandlerManager getInstance(){
		if (h == null) {
			synchronized (HandlerManager.class) {
				if (h == null) {
					h = new HandlerManager();
					h.appContext = AppContext.getAppContext();
				}
			}
		}
		return h;
	}
	
	public interface LoadListDataCallbacks{
		public void onLoadDataFinished(ListData listData);
		public List<?> getListData();
	}
	
	/**
	 * 获取listview的初始化Handler
	 * 
	 * @param lv
	 * @param adapter
	 * @return
	 */
	public Handler getHandler(final PullToRefreshListView lv, final BaseAdapter adapter, final TextView more,
			final ActionBarProgressBarVisibility abProgress,final LoadListDataCallbacks callbacks, final int pageSize) {
		return new Handler() {
			public void handleMessage(Message msg) {
				if (msg.what >= 0) {
					// listview数据处理
					Notice notice = handleData(msg.what, msg.obj, msg.arg2, msg.arg1, callbacks);
					callbacks.onLoadDataFinished(data);
					if (msg.what < pageSize) {
						lv.setTag(UIHelper.LISTVIEW_DATA_FULL);
						adapter.notifyDataSetChanged();
						more.setText(R.string.load_full);
					} else if (msg.what == pageSize) {
						lv.setTag(UIHelper.LISTVIEW_DATA_MORE);
						adapter.notifyDataSetChanged();
						more.setText(R.string.load_more);

						/*// 特殊处理-热门动弹不能翻页
						if (lv == getRefreshListView()) {
							TweetList tlist = (TweetList) msg.obj;
							if (lvTweetData.size() == tlist.getTweetCount()) {
								lv.setTag(UIHelper.LISTVIEW_DATA_FULL);
								more.setText(R.string.load_full);
							}
						}*/
					}
					// 发送通知广播
					if (notice != null) {
						UIHelper.sendBroadCast(lv.getContext(), notice);
					}
				} else if (msg.what == -1) {
					// 有异常--显示加载出错 & 弹出错误消息
					lv.setTag(UIHelper.LISTVIEW_DATA_MORE);
					more.setText(R.string.load_error);
					((AppException) msg.obj).makeToast(appContext);
				}
				if (adapter.getCount() == 0) {
					lv.setTag(UIHelper.LISTVIEW_DATA_EMPTY);
					more.setText(R.string.load_empty);
				}
				abProgress.setProgressBarVisibility(ActionBarProgressBarVisibility.GONE);
				if (msg.arg1 == UIHelper.LISTVIEW_ACTION_REFRESH) {
					lv.onRefreshComplete(appContext.getString(R.string.pull_to_refresh_update) + new Date().toLocaleString());
					lv.setSelection(0);
				} else if (msg.arg1 == UIHelper.LISTVIEW_ACTION_CHANGE_CATALOG) {
					lv.onRefreshComplete();
					lv.setSelection(0);
				}
			}
		};
	}
	
	public Handler getSearcHandler(final ListView lv, final ListViewSearchAdapter adapter, final TextView more,
			final ActionBarProgressBarVisibility abProgress,final LoadListDataCallbacks callbacks){
		return new Handler(){
			
			public void handleMessage(Message msg) {
				
				if(msg.what >= 0){						
					SearchList list = (SearchList)msg.obj;
					Notice notice = list.getNotice();
					//处理listview数据
					switch (msg.arg1) {
					case UIHelper.LISTVIEW_ACTION_INIT:
					case UIHelper.LISTVIEW_ACTION_REFRESH:
					case UIHelper.LISTVIEW_ACTION_CHANGE_CATALOG:
						data.setSearchSumData(msg.what);
						data.setSearchData(list.getResultlist());
						break;
					case UIHelper.LISTVIEW_ACTION_SCROLL:
						data.addSearchSumData(msg.what);
						if(data.getSearchDataSize() > 0){
							for(SearchList.Result res1 : list.getResultlist()){
								boolean b = false;
								for(SearchList.Result res2 : data.getSearchData()){
									if(res1.getObjid() == res2.getObjid()){
										b = true;
										break;
									}
								}
								if(!b) data.addSearchData(res1);
							}
						}else{
							data.addAllSearchData(list.getResultlist());
						}
						break;
					}
					
					callbacks.onLoadDataFinished(data);
					
					if(msg.what < 20){
						lv.setTag(UIHelper.LISTVIEW_DATA_FULL);
						adapter.notifyDataSetChanged();
						more.setText(R.string.load_full);
					}else if(msg.what == 20){					
						lv.setTag(UIHelper.LISTVIEW_DATA_MORE);
						adapter.notifyDataSetChanged();
						more.setText(R.string.load_more);
					}
					//发送通知广播
					if(notice != null){
						UIHelper.sendBroadCast(lv.getContext(), notice);
					}
				}
				else if(msg.what == -1){
					//有异常--显示加载出错 & 弹出错误消息
					lv.setTag(UIHelper.LISTVIEW_DATA_MORE);
					more.setText(R.string.load_error);
					((AppException)msg.obj).makeToast(lv.getContext());
				}
				if(data.getSearchDataSize()==0){
					lv.setTag(UIHelper.LISTVIEW_DATA_EMPTY);
					more.setText(R.string.load_empty);
				}
				if(msg.arg1 != UIHelper.LISTVIEW_ACTION_SCROLL){
					lv.setSelection(0);//返回头部
				}
				
				abProgress.setProgressBarVisibility(View.GONE);
				lv.setVisibility(View.VISIBLE);
			}
		};
	}

	/**
	 * listview数据处理
	 * 
	 * @param what
	 *            数量
	 * @param obj
	 *            数据
	 * @param objtype
	 *            数据类型
	 * @param actiontype
	 *            操作类型
	 * @return notice 通知信息
	 */
	@SuppressWarnings("unchecked")
	private Notice handleData(int what, Object obj, int objtype, int actiontype, final LoadListDataCallbacks callbacks) {
		Notice notice = null;
		switch (actiontype) {
		case UIHelper.LISTVIEW_ACTION_INIT:
		case UIHelper.LISTVIEW_ACTION_REFRESH:
		case UIHelper.LISTVIEW_ACTION_CHANGE_CATALOG:
			int newdata = 0;// 新加载数据-只有刷新动作才会使用到
			switch (objtype) {
			case UIHelper.LISTVIEW_DATATYPE_NEWS:
				NewsList nlist = (NewsList) obj;
				notice = nlist.getNotice();
				List<News> oldNewsData = (List<News>) callbacks.getListData();
				data.setNewsSumData(what);
				if (actiontype == UIHelper.LISTVIEW_ACTION_REFRESH) {
					if (oldNewsData.size() > 0) {
						for (News news1 : nlist.getNewslist()) {
							boolean b = false;
							for (News news2 : oldNewsData) {
								if (news1.getId() == news2.getId()) {
									b = true;
									break;
								}
							}
							if (!b)
								newdata++;
						}
					} else {
						newdata = what;
					}
				}
				data.setNewsData(nlist.getNewslist());
				break;
			case UIHelper.LISTVIEW_DATATYPE_BLOG:
				BlogList blist = (BlogList) obj;
				notice = blist.getNotice();
				List<Blog> oldBlogData = (List<Blog>) callbacks.getListData();
				data.setBlogSumData(what);
				if (actiontype == UIHelper.LISTVIEW_ACTION_REFRESH) {
					if (oldBlogData.size() > 0) {
						for (Blog blog1 : blist.getBloglist()) {
							boolean b = false;
							for (Blog blog2 : oldBlogData) {
								if (blog1.getId() == blog2.getId()) {
									b = true;
									break;
								}
							}
							if (!b)
								newdata++;
						}
					} else {
						newdata = what;
					}
				}
				data.setBlogData(blist.getBloglist());
				break;
			case UIHelper.LISTVIEW_DATATYPE_POST:
				PostList plist = (PostList) obj;
				notice = plist.getNotice();
				List<Post> oldPostData = (List<Post>) callbacks.getListData();
				data.setQuestionSumData(what);
				if (actiontype == UIHelper.LISTVIEW_ACTION_REFRESH) {
					if (oldPostData.size() > 0) {
						for (Post post1 : plist.getPostlist()) {
							boolean b = false;
							for (Post post2 : oldPostData) {
								if (post1.getId() == post2.getId()) {
									b = true;
									break;
								}
							}
							if (!b)
								newdata++;
						}
					} else {
						newdata = what;
					}
				}
				data.setQuestionData(plist.getPostlist());
				break;
			case UIHelper.LISTVIEW_DATATYPE_TWEET:
				TweetList tlist = (TweetList) obj;
				notice = tlist.getNotice();
				List<Tweet> oldTweetData = (List<Tweet>) callbacks.getListData();
				data.setTweetSumData(what);
				if (actiontype == UIHelper.LISTVIEW_ACTION_REFRESH) {
					if (oldTweetData.size() > 0) {
						for (Tweet tweet1 : tlist.getTweetlist()) {
							boolean b = false;
							for (Tweet tweet2 : oldTweetData) {
								if (tweet1.getId() == tweet2.getId()) {
									b = true;
									break;
								}
							}
							if (!b)
								newdata++;
						}
					} else {
						newdata = what;
					}
				}
				data.setTweetData(tlist.getTweetlist());
				break;
			case UIHelper.LISTVIEW_DATATYPE_ACTIVE:
				ActiveList alist = (ActiveList) obj;
				notice = alist.getNotice();
				List<Active> oldActiveData = (List<Active>) callbacks.getListData();
				data.setActiveSumData(what);
				if (actiontype == UIHelper.LISTVIEW_ACTION_REFRESH) {
					if (oldActiveData.size() > 0) {
						for (Active active1 : alist.getActivelist()) {
							boolean b = false;
							for (Active active2 : oldActiveData) {
								if (active1.getId() == active2.getId()) {
									b = true;
									break;
								}
							}
							if (!b)
								newdata++;
						}
					} else {
						newdata = what;
					}
				}
				data.setActiveData(alist.getActivelist());
				break;
			case UIHelper.LISTVIEW_DATATYPE_MESSAGE:
				MessageList mlist = (MessageList) obj;
				notice = mlist.getNotice();
				List<Messages> oldMessagesData = (List<Messages>) callbacks.getListData();
				data.setMsgSumData(what);
				if (actiontype == UIHelper.LISTVIEW_ACTION_REFRESH) {
					if (oldMessagesData.size() > 0) {
						for (Messages msg1 : mlist.getMessagelist()) {
							boolean b = false;
							for (Messages msg2 : oldMessagesData) {
								if (msg1.getId() == msg2.getId()) {
									b = true;
									break;
								}
							}
							if (!b)
								newdata++;
						}
					} else {
						newdata = what;
					}
				}
				data.setMsgData(mlist.getMessagelist());
				break;
			}
			if (actiontype == UIHelper.LISTVIEW_ACTION_REFRESH) {
				// 提示新加载数据
				if (newdata > 0) {
					NewDataToast.makeText(appContext, appContext.getString(R.string.new_data_toast_message, newdata), appContext.isAppSound()).show();
				} else {
					NewDataToast.makeText(appContext, appContext.getString(R.string.new_data_toast_none), false).show();
				}
			}
			break;
		case UIHelper.LISTVIEW_ACTION_SCROLL:
			switch (objtype) {
			case UIHelper.LISTVIEW_DATATYPE_NEWS:
				NewsList list = (NewsList) obj;
				notice = list.getNotice();
				data.addNewsSumData(what);
				if (data.getNewsDataSize() > 0) {
					for (News news1 : list.getNewslist()) {
						boolean b = false;
						for (News news2 : data.getNewsData()) {
							if (news1.getId() == news2.getId()) {
								b = true;
								break;
							}
						}
						if (!b)
							data.addNewsData(news1);
					}
				} else {
					data.addAllNewsData(list.getNewslist());
				}
				break;
			case UIHelper.LISTVIEW_DATATYPE_BLOG:
				BlogList blist = (BlogList) obj;
				notice = blist.getNotice();
				data.addBlogSumData(what);
				if (data.getBlogDataSize() > 0) {
					for (Blog blog1 : blist.getBloglist()) {
						boolean b = false;
						for (Blog blog2 : data.getBlogData()) {
							if (blog1.getId() == blog2.getId()) {
								b = true;
								break;
							}
						}
						if (!b)
							data.addBlogData(blog1);
					}
				} else {
					data.addAllBlogData(blist.getBloglist());
				}
				break;
			case UIHelper.LISTVIEW_DATATYPE_POST:
				PostList plist = (PostList) obj;
				notice = plist.getNotice();
				data.addQuestionSumData(what);
				if (data.getQuestionDataSize() > 0) {
					for (Post post1 : plist.getPostlist()) {
						boolean b = false;
						for (Post post2 : data.getQuestionData()) {
							if (post1.getId() == post2.getId()) {
								b = true;
								break;
							}
						}
						if (!b)
							data.addQuestionData(post1);
					}
				} else {
					data.addAllQuestionData(plist.getPostlist());
				}
				break;
			case UIHelper.LISTVIEW_DATATYPE_TWEET:
				TweetList tlist = (TweetList) obj;
				notice = tlist.getNotice();
				data.addTweetSumData(what);
				if (data.getTweetDataSize() > 0) {
					for (Tweet tweet1 : tlist.getTweetlist()) {
						boolean b = false;
						for (Tweet tweet2 : data.getTweetData()) {
							if (tweet1.getId() == tweet2.getId()) {
								b = true;
								break;
							}
						}
						if (!b)
							data.addTweetData(tweet1);
					}
				} else {
					data.addAllTweetData(tlist.getTweetlist());
				}
				break;
			case UIHelper.LISTVIEW_DATATYPE_ACTIVE:
				ActiveList alist = (ActiveList) obj;
				notice = alist.getNotice();
				data.addActiveSumData(what);
				if (data.getActiveDataSize() > 0) {
					for (Active active1 : alist.getActivelist()) {
						boolean b = false;
						for (Active active2 : data.getActiveData()) {
							if (active1.getId() == active2.getId()) {
								b = true;
								break;
							}
						}
						if (!b)
							data.addActiveData(active1);
					}
				} else {
					data.addAllActiveData(alist.getActivelist());
				}
				break;
			case UIHelper.LISTVIEW_DATATYPE_MESSAGE:
				MessageList mlist = (MessageList) obj;
				notice = mlist.getNotice();
				data.addMsgSumData(what);
				if (data.getMsgDataSize() > 0) {
					for (Messages msg1 : mlist.getMessagelist()) {
						boolean b = false;
						for (Messages msg2 : data.getMsgData()) {
							if (msg1.getId() == msg2.getId()) {
								b = true;
								break;
							}
						}
						if (!b)
							data.addMsgData(msg1);
					}
				} else {
					data.addAllMsgData(mlist.getMessagelist());
				}
				break;
			}
			break;
		}
		return notice;
	}

	/**
	 * 通知信息处理
	 * 
	 * @param type
	 *            1:@我的信息 2:未读消息 3:评论个数 4:新粉丝个数
	 */
	public void ClearNotice(final int type) {
		final int uid = appContext.getLoginUid();
		final Handler handler = new Handler() {
			public void handleMessage(Message msg) {
				if (msg.what == 1 && msg.obj != null) {
					Result res = (Result) msg.obj;
					if (res.OK() && res.getNotice() != null) {
						UIHelper.sendBroadCast(appContext, res.getNotice());
					}
				} else {
					((AppException) msg.obj).makeToast(appContext);
				}
			}
		};
		new Thread() {
			public void run() {
				Message msg = new Message();
				try {
					Result res = appContext.noticeClear(uid, type);
					msg.what = 1;
					msg.obj = res;
				} catch (AppException e) {
					e.printStackTrace();
					msg.what = -1;
					msg.obj = e;
				}
				handler.sendMessage(msg);
			}
		}.start();
		//isClearNotice = false;// 重置
		curClearNoticeType = 0;
	}
	
	/*public boolean isClearNotice() {
		return isClearNotice;
	}

	public void setClearNotice(boolean isClearNotice) {
		this.isClearNotice = isClearNotice;
	}*/

	public int getCurClearNoticeType() {
		return curClearNoticeType;
	}

	public void setCurClearNoticeType(int curClearNoticeType) {
		this.curClearNoticeType = curClearNoticeType;
	}
	
}
