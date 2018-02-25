package net.oschina.app.ui;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


import net.oschina.app.AppConfig;
import net.oschina.app.AppContext;
import net.oschina.app.AppException;
import net.oschina.app.adapter.ListViewCommentAdapter;
import net.oschina.app.bean.Blog;
import net.oschina.app.bean.BlogCommentList;
import net.oschina.app.bean.Comment;
import net.oschina.app.bean.FavoriteList;
import net.oschina.app.bean.Notice;
import net.oschina.app.bean.Result;
import net.oschina.app.common.AnimUtil;
import net.oschina.app.common.StringUtils;
import net.oschina.app.common.UIHelper;
import net.oschina.app.inteface.FooterViewVisibility;
import net.oschina.app.widget.BadgeView;
import net.oschina.app.widget.ExtentScrollView;
import net.oschina.app.widget.PullToRefreshListView;
import net.oschina.designapp.R;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ViewSwitcher;

/**
 * 博客详情
 * 
 * @author liux (http://my.oschina.net/liux)
 * @version 1.0
 * @created 2012-3-21
 */
public class BlogDetail extends CustomActivity implements FooterViewVisibility {

	public final static String TAG = "BlogDetail";

	private ExtentScrollView mScrollView;
	private ViewSwitcher mViewSwitcher;

	private BadgeView bv_comment;
	private ImageButton mDetail;
	private ImageButton mCommentList;
	private MenuItem favoriteItem;

	private ImageView mDocTYpe;
	private TextView mTitle;
	private TextView mAuthor;
	private TextView mPubDate;
	private TextView mCommentCount;

	private WebView mWebView;
	private Handler mHandler;
	private Blog blogDetail;
	private int blogId;

	private final static int VIEWSWITCH_TYPE_DETAIL = 0x001;
	private final static int VIEWSWITCH_TYPE_COMMENTS = 0x002;

	private final static int DATA_LOAD_ING = 0x001;
	private final static int DATA_LOAD_COMPLETE = 0x002;
	private final static int DATA_LOAD_FAIL = 0x003;

	private PullToRefreshListView mLvComment;
	private ListViewCommentAdapter lvCommentAdapter;
	private List<Comment> lvCommentData = new ArrayList<Comment>();
	private View lvComment_footer;
	private TextView lvComment_foot_more;
	private Handler mCommentHandler;
	private int lvSumData;

	private int curId;
	private int curCatalog; // 博客评论分类
	private int curLvDataState;
	private int curLvPosition;// 当前listview选中的item位置

	private ViewSwitcher mFootViewSwitcher;
	private TextView mFootEditebox;
	private EditText mFootEditer;
	private ImageButton mFootPubcomment;
	private ProgressDialog mProgress;
	private InputMethodManager imm;
	private String tempCommentKey = AppConfig.TEMP_COMMENT;

	private int _id;
	private int _uid;
	private String _content;

	private GestureDetector gd;
	private boolean isFullScreen;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.blog_detail);

		this.initView();
		this.initData();

		// 加载评论视图&数据
		this.initCommentView();
		this.initCommentData();

		// 注册双击全屏事件
		this.regOnDoubleEvent();
	}

	// 隐藏输入发表回帖状态
	private void hideEditor() {
		// imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
		imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
		if (mFootViewSwitcher.getDisplayedChild() == 1) {
			mFootViewSwitcher.setDisplayedChild(0);
			mFootEditer.clearFocus();
			mFootEditer.setVisibility(View.GONE);
		}
	}

	// 初始化视图控件
	private void initView() {
		blogId = getIntent().getIntExtra("blog_id", 0);

		if (blogId > 0)
			tempCommentKey = AppConfig.TEMP_COMMENT + "_"
					+ CommentPub.CATALOG_BLOG + "_" + blogId;

		mViewSwitcher = (ViewSwitcher) findViewById(R.id.blog_detail_viewswitcher);
		mScrollView = (ExtentScrollView) findViewById(R.id.blog_detail_scrollview);
		mScrollView.setFootView(this);

		mDetail = (ImageButton) findViewById(R.id.detail_footbar_detail);
		mCommentList = (ImageButton) findViewById(R.id.detail_footbar_commentlist);

		mDocTYpe = (ImageView) findViewById(R.id.blog_detail_documentType);
		mTitle = (TextView) findViewById(R.id.blog_detail_title);
		mAuthor = (TextView) findViewById(R.id.blog_detail_author);
		mPubDate = (TextView) findViewById(R.id.blog_detail_date);
		mCommentCount = (TextView) findViewById(R.id.blog_detail_commentcount);

		mDetail.setEnabled(false);

		mWebView = (WebView) findViewById(R.id.blog_detail_webview);
		mWebView.getSettings().setSupportZoom(true);
		mWebView.getSettings().setBuiltInZoomControls(true);
		mWebView.getSettings().setDefaultFontSize(15);
		UIHelper.setZoomControlGone(mWebView);
		UIHelper.addWebImageShow(this, mWebView);
		
		mAuthor.setOnClickListener(authorClickListener);
		mDetail.setOnClickListener(detailClickListener);
		mCommentList.setOnClickListener(commentlistClickListener);

		bv_comment = new BadgeView(this, mCommentList);
		bv_comment.setBackgroundResource(R.drawable.widget_count_bg2);
		bv_comment.setIncludeFontPadding(false);
		bv_comment.setGravity(Gravity.CENTER);
		bv_comment.setTextSize(8f);
		bv_comment.setTextColor(Color.WHITE);

		imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);

		mFootViewSwitcher = (ViewSwitcher) findViewById(R.id.detail_footbar_viewswitcher);
		mFootPubcomment = (ImageButton) findViewById(R.id.detail_footbar_pubcomment);
		mFootPubcomment.setOnClickListener(closeEditTextClickListener);
		mFootEditebox = (TextView) findViewById(R.id.detail_footbar_editebox);
		mFootEditebox.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				mFootViewSwitcher.showNext();
				mFootEditer.setVisibility(View.VISIBLE);
				mFootEditer.requestFocus();
				mFootEditer.requestFocusFromTouch();
			}
		});
		mFootEditer = (EditText) findViewById(R.id.detail_footbar_editer);
		mFootEditer.addTextChangedListener(mTextWatcher);
		mFootEditer.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus) {
					imm.showSoftInput(v, 0);
				} else {
					imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
				}
			}
		});
		mFootEditer.setOnKeyListener(new View.OnKeyListener() {
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_BACK) {
					hideEditor();
					return true;
				}
				return false;
			}
		});
		// 编辑器添加文本监听
		mFootEditer.addTextChangedListener(UIHelper.getTextWatcher(this,
				tempCommentKey));

		// 显示临时编辑内容
		UIHelper.showTempEditContent(this, mFootEditer, tempCommentKey);

	}

	// 初始化控件数据
	private void initData() {
		mHandler = new Handler() {
			public void handleMessage(Message msg) {
				if (msg.what == 1) {
					headButtonSwitch(DATA_LOAD_COMPLETE);

					int docType = blogDetail.getDocumentType();
					if (docType == Blog.DOC_TYPE_ORIGINAL) {
						mDocTYpe.setImageResource(R.drawable.widget_original_icon);
					} else if (docType == Blog.DOC_TYPE_REPASTE) {
						mDocTYpe.setImageResource(R.drawable.widget_repaste_icon);
					}

					mTitle.setText(blogDetail.getTitle());
					mAuthor.setText(blogDetail.getAuthor());
					mPubDate.setText(StringUtils.friendly_time(blogDetail
							.getPubDate()));
					mCommentCount.setText(String.valueOf(blogDetail
							.getCommentCount()));

					// 是否收藏
					setFavoriteState();

					// 显示评论数
					if (blogDetail.getCommentCount() > 0) {
						bv_comment.setText(blogDetail.getCommentCount() + "");
						bv_comment.show();
					} else {
						bv_comment.setText("");
						bv_comment.hide();
					}

					String body = UIHelper.WEB_STYLE + blogDetail.getBody();

					Log.i(TAG, blogDetail.getBody());
					// 读取用户设置：是否加载文章图片--默认有wifi下始终加载图片
					boolean isLoadImage;
					AppContext ac = (AppContext) getApplication();
					if (AppContext.NETTYPE_WIFI == ac.getNetworkType()) {
						isLoadImage = true;
					} else {
						isLoadImage = ac.isLoadImage();
					}
					if (isLoadImage) {
						body = body.replaceAll(
								"(<img[^>]*?)\\s+width\\s*=\\s*\\S+", "$1");
						body = body.replaceAll(
								"(<img[^>]*?)\\s+height\\s*=\\s*\\S+", "$1");

						// 添加点击图片放大支持
						body = body
								.replaceAll("(<img[^>]+src=\")(\\S+)\"",
										"$1$2\" onClick=\"javascript:mWebViewImageListener.onImageClick('$2')\"");
					} else {
						body = body.replaceAll("<\\s*img\\s+([^>]*)\\s*>", "");
					}

					mWebView.loadDataWithBaseURL(null, body, "text/html",
							"utf-8", null);
					mWebView.setWebViewClient(UIHelper.getWebViewClient());

					// 发送通知广播
					if (msg.obj != null) {
						UIHelper.sendBroadCast(BlogDetail.this,
								(Notice) msg.obj);
					}
				} else if (msg.what == 0) {
					headButtonSwitch(DATA_LOAD_FAIL);

					UIHelper.ToastMessage(BlogDetail.this,
							R.string.msg_load_is_null);
				} else if (msg.what == -1 && msg.obj != null) {
					headButtonSwitch(DATA_LOAD_FAIL);

					((AppException) msg.obj).makeToast(BlogDetail.this);
				}
			}
		};

		initData(blogId, false);
	}

	private void initData(final int blog_id, final boolean isRefresh) {
		headButtonSwitch(DATA_LOAD_ING);

		new Thread() {
			public void run() {
				Message msg = new Message();
				try {
					blogDetail = ((AppContext) getApplication()).getBlog(
							blog_id, isRefresh);
					msg.what = (blogDetail != null && blogDetail.getId() > 0) ? 1
							: 0;
					msg.obj = (blogDetail != null) ? blogDetail.getNotice()
							: null;
				} catch (AppException e) {
					e.printStackTrace();
					msg.what = -1;
					msg.obj = e;
				}
				mHandler.sendMessage(msg);
			}
		}.start();
	}

	/**
	 * 底部栏切换
	 * 
	 * @param type
	 */
	private void viewSwitch(int type) {
		switch (type) {
		case VIEWSWITCH_TYPE_DETAIL:
			mDetail.setEnabled(false);
			mCommentList.setEnabled(true);
			getSupportActionBar().setTitle(R.string.blog_detail_head_title);
			mViewSwitcher.setDisplayedChild(0);
			break;
		case VIEWSWITCH_TYPE_COMMENTS:
			mDetail.setEnabled(true);
			mCommentList.setEnabled(false);
			getSupportActionBar().setTitle(R.string.comment_list_head_title);
			mViewSwitcher.setDisplayedChild(1);
			break;
		}
	}

	/**
	 * 头部按钮展示
	 * 
	 * @param type
	 */
	private void headButtonSwitch(int type) {
		switch (type) {
		case DATA_LOAD_ING:
			mScrollView.setVisibility(View.GONE);
			setProgressBarVisibility(View.VISIBLE);
			break;
		case DATA_LOAD_COMPLETE:
			mScrollView.setVisibility(View.VISIBLE);
			setProgressBarVisibility(View.GONE);
			break;
		case DATA_LOAD_FAIL:
			mScrollView.setVisibility(View.GONE);
			setProgressBarVisibility(View.GONE);
			break;
		}
	}

	private View.OnClickListener authorClickListener = new View.OnClickListener() {
		public void onClick(View v) {
			UIHelper.showUserCenter(v.getContext(), blogDetail.getAuthorId(),
					blogDetail.getAuthor());
		}
	};

	private View.OnClickListener detailClickListener = new View.OnClickListener() {
		public void onClick(View v) {
			if (blogId == 0) {
				return;
			}
			// 切换到详情
			viewSwitch(VIEWSWITCH_TYPE_DETAIL);
		}
	};

	private View.OnClickListener commentlistClickListener = new View.OnClickListener() {
		public void onClick(View v) {
			if (blogId == 0) {
				return;
			}
			// 切换到评论
			viewSwitch(VIEWSWITCH_TYPE_COMMENTS);
		}
	};

	// 初始化视图控件
	private void initCommentView() {
		lvComment_footer = getLayoutInflater().inflate(
				R.layout.listview_footer, null);
		lvComment_foot_more = (TextView) lvComment_footer
				.findViewById(R.id.listview_foot_more);

		lvCommentAdapter = new ListViewCommentAdapter(this, lvCommentData,
				R.layout.comment_listitem);
		mLvComment = (PullToRefreshListView) findViewById(R.id.comment_list_listview);

		mLvComment.addFooterView(lvComment_footer);// 添加底部视图 必须在setAdapter前
		mLvComment.setAdapter(lvCommentAdapter);
		mLvComment
				.setOnItemClickListener(new AdapterView.OnItemClickListener() {
					public void onItemClick(AdapterView<?> parent, View view,
							int position, long id) {
						// 点击头部、底部栏无效
						if (position == 0 || view == lvComment_footer)
							return;

						Comment com = null;
						// 判断是否是TextView
						if (view instanceof TextView) {
							com = (Comment) view.getTag();
						} else {
							ImageView img = (ImageView) view
									.findViewById(R.id.comment_listitem_userface);
							com = (Comment) img.getTag();
						}
						if (com == null)
							return;

						// 跳转--回复评论界面
						UIHelper.showCommentReply(BlogDetail.this, curId,
								curCatalog, com.getId(), com.getAuthorId(),
								com.getAuthor(), com.getContent());
					}
				});
		mLvComment.setOnScrollListener(new AbsListView.OnScrollListener() {
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				mLvComment.onScrollStateChanged(view, scrollState);

				// 数据为空--不用继续下面代码了
				if (lvCommentData.size() == 0)
					return;

				// 判断是否滚动到底部
				boolean scrollEnd = false;
				try {
					if (view.getPositionForView(lvComment_footer) == view
							.getLastVisiblePosition())
						scrollEnd = true;
				} catch (Exception e) {
					scrollEnd = false;
				}

				if (scrollEnd && curLvDataState == UIHelper.LISTVIEW_DATA_MORE) {
					mLvComment.setTag(UIHelper.LISTVIEW_DATA_LOADING);
					lvComment_foot_more.setText(R.string.load_ing);
					// 当前pageIndex
					int pageIndex = lvSumData / 20;
					loadLvCommentData(curId, pageIndex, mCommentHandler,
							UIHelper.LISTVIEW_ACTION_SCROLL);
				}
			}

			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				mLvComment.onScroll(view, firstVisibleItem, visibleItemCount,
						totalItemCount);
			}
		});
		mLvComment
				.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
					public boolean onItemLongClick(AdapterView<?> parent,
							View view, int position, long id) {
						// 点击头部、底部栏无效
						if (position == 0 || view == lvComment_footer)
							return false;

						Comment _com = null;
						// 判断是否是TextView
						if (view instanceof TextView) {
							_com = (Comment) view.getTag();
						} else {
							ImageView img = (ImageView) view
									.findViewById(R.id.comment_listitem_userface);
							_com = (Comment) img.getTag();
						}
						if (_com == null)
							return false;

						final Comment com = _com;

						curLvPosition = lvCommentData.indexOf(com);

						final AppContext ac = (AppContext) getApplication();
						// 操作--回复 & 删除
						final int uid = ac.getLoginUid();
						// 判断当前登录用户是否是博主 或者 该评论是否是当前登录用户发表的：true--有删除操作
						// false--没有删除操作
						if (uid == com.getAuthorId()
								|| (blogDetail != null && uid == blogDetail
										.getAuthorId())) {
							final Handler handler = new Handler() {
								public void handleMessage(Message msg) {
									if (msg.what == 1) {
										Result res = (Result) msg.obj;
										if (res.OK()) {
											lvSumData--;
											bv_comment.setText(lvSumData + "");
											bv_comment.show();
											lvCommentData.remove(com);
											lvCommentAdapter
													.notifyDataSetChanged();
										}
										UIHelper.ToastMessage(BlogDetail.this,
												res.getErrorMessage());
									} else {
										((AppException) msg.obj)
												.makeToast(BlogDetail.this);
									}
								}
							};
							final Thread thread = new Thread() {
								public void run() {
									Message msg = new Message();
									try {
										Result res = ac.delBlogComment(uid,
												blogId, com.getId(),
												com.getAuthorId(),
												blogDetail.getAuthorId());
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
							UIHelper.showCommentOptionDialog(BlogDetail.this,
									curId, curCatalog, com, thread);
						} else {
							UIHelper.showCommentOptionDialog(BlogDetail.this,
									curId, curCatalog, com, null);
						}
						return true;
					}
				});
		mLvComment
				.setOnRefreshListener(new PullToRefreshListView.OnRefreshListener() {
					public void onRefresh() {
						loadLvCommentData(curId, 0, mCommentHandler,
								UIHelper.LISTVIEW_ACTION_REFRESH);
					}
				});
	}

	// 初始化评论数据
	private void initCommentData() {
		curId = blogId;
		curCatalog = CommentPub.CATALOG_BLOG;

		mCommentHandler = new Handler() {
			public void handleMessage(Message msg) {
				if (msg.what >= 0) {
					BlogCommentList list = (BlogCommentList) msg.obj;
					Notice notice = list.getNotice();
					// 处理listview数据
					switch (msg.arg1) {
					case UIHelper.LISTVIEW_ACTION_INIT:
					case UIHelper.LISTVIEW_ACTION_REFRESH:
						lvSumData = msg.what;
						lvCommentData.clear();// 先清除原有数据
						lvCommentData.addAll(list.getCommentlist());
						break;
					case UIHelper.LISTVIEW_ACTION_SCROLL:
						lvSumData += msg.what;
						if (lvCommentData.size() > 0) {
							for (Comment com1 : list.getCommentlist()) {
								boolean b = false;
								for (Comment com2 : lvCommentData) {
									if (com1.getId() == com2.getId()
											&& com1.getAuthorId() == com2
													.getAuthorId()) {
										b = true;
										break;
									}
								}
								if (!b)
									lvCommentData.add(com1);
							}
						} else {
							lvCommentData.addAll(list.getCommentlist());
						}
						break;
					}

					// 评论数更新
					if (blogDetail != null
							&& lvCommentData.size() > blogDetail
									.getCommentCount()) {
						blogDetail.setCommentCount(lvCommentData.size());
						bv_comment.setText(lvCommentData.size() + "");
						bv_comment.show();
					}

					if (msg.what < 20) {
						curLvDataState = UIHelper.LISTVIEW_DATA_FULL;
						lvCommentAdapter.notifyDataSetChanged();
						lvComment_foot_more.setText(R.string.load_full);
					} else if (msg.what == 20) {
						curLvDataState = UIHelper.LISTVIEW_DATA_MORE;
						lvCommentAdapter.notifyDataSetChanged();
						lvComment_foot_more.setText(R.string.load_more);
					}
					// 发送通知广播
					if (notice != null) {
						UIHelper.sendBroadCast(BlogDetail.this, notice);
					}
				} else if (msg.what == -1) {
					// 有异常--显示加载出错 & 弹出错误消息
					curLvDataState = UIHelper.LISTVIEW_DATA_MORE;
					lvComment_foot_more.setText(R.string.load_error);
					((AppException) msg.obj).makeToast(BlogDetail.this);
				}
				if (lvCommentData.size() == 0) {
					curLvDataState = UIHelper.LISTVIEW_DATA_EMPTY;
					lvComment_foot_more.setText(R.string.load_empty);
				}
				if (msg.arg1 == UIHelper.LISTVIEW_ACTION_REFRESH) {
					mLvComment
							.onRefreshComplete(getString(R.string.pull_to_refresh_update)
									+ new Date().toLocaleString());
					mLvComment.setSelection(0);
				}
			}
		};
		this.loadLvCommentData(curId, 0, mCommentHandler,
				UIHelper.LISTVIEW_ACTION_INIT);
	}

	/**
	 * 线程加载评论数据
	 * 
	 * @param id
	 *            当前文章id
	 * @param pageIndex
	 *            当前页数
	 * @param handler
	 *            处理器
	 * @param action
	 *            动作标识
	 */
	private void loadLvCommentData(final int id, final int pageIndex,
			final Handler handler, final int action) {
		new Thread() {
			public void run() {
				Message msg = new Message();
				boolean isRefresh = false;
				if (action == UIHelper.LISTVIEW_ACTION_REFRESH
						|| action == UIHelper.LISTVIEW_ACTION_SCROLL)
					isRefresh = true;
				try {
					BlogCommentList commentlist = ((AppContext) getApplication())
							.getBlogCommentList(id, pageIndex, isRefresh);
					msg.what = commentlist.getPageSize();
					msg.obj = commentlist;
				} catch (AppException e) {
					e.printStackTrace();
					msg.what = -1;
					msg.obj = e;
				}
				msg.arg1 = action;// 告知handler当前action
				handler.sendMessage(msg);
			}
		}.start();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode != RESULT_OK)
			return;
		if (data == null)
			return;

		viewSwitch(VIEWSWITCH_TYPE_COMMENTS);// 跳到评论列表

		if (requestCode == UIHelper.REQUEST_CODE_FOR_RESULT) {
			Comment comm = (Comment) data
					.getSerializableExtra("COMMENT_SERIALIZABLE");
			lvCommentData.add(0, comm);
			lvCommentAdapter.notifyDataSetChanged();
			mLvComment.setSelection(0);
			// 显示评论数
			int count = blogDetail.getCommentCount() + 1;
			blogDetail.setCommentCount(count);
			bv_comment.setText(count + "");
			bv_comment.show();
		} else if (requestCode == UIHelper.REQUEST_CODE_FOR_REPLY) {
			Comment comm = (Comment) data
					.getSerializableExtra("COMMENT_SERIALIZABLE");
			lvCommentData.set(curLvPosition, comm);
			lvCommentAdapter.notifyDataSetChanged();
		}
	}
	/**
	 * 当用户在EditText中无输入内容时，按钮变成切换按钮。当有内容输入时则切换成发送按钮。
	 */
	private TextWatcher mTextWatcher = new TextWatcher(){

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void afterTextChanged(Editable s) {
			if (s.toString().equals("")) {
				mFootPubcomment.setImageResource(R.drawable.ic_footer_switch);
				mFootPubcomment.setOnClickListener(closeEditTextClickListener);
			}else {
				mFootPubcomment.setImageResource(R.drawable.ic_menu_send);
				mFootPubcomment.setOnClickListener(commentpubClickListener);
			}
		}
		
	};
	
	private View.OnClickListener closeEditTextClickListener = new View.OnClickListener() {
		public void onClick(View v) {
			mFootViewSwitcher.setDisplayedChild(0);
		}
	};

	private View.OnClickListener commentpubClickListener = new View.OnClickListener() {
		public void onClick(View v) {
			_id = curId;

			if (curId == 0) {
				return;
			}

			_content = mFootEditer.getText().toString();
			if (StringUtils.isEmpty(_content)) {
				UIHelper.ToastMessage(v.getContext(), "请输入评论内容");
				return;
			}

			final AppContext ac = (AppContext) getApplication();
			if (!ac.isLogin()) {
				UIHelper.showLoginDialog(getSupportFragmentManager());
				return;
			}

			_uid = ac.getLoginUid();

			mProgress = ProgressDialog.show(v.getContext(), null, "发表中···",
					true, true);

			final Handler handler = new Handler() {
				public void handleMessage(Message msg) {

					if (mProgress != null)
						mProgress.dismiss();

					if (msg.what == 1) {
						Result res = (Result) msg.obj;
						UIHelper.ToastMessage(BlogDetail.this,
								res.getErrorMessage());
						if (res.OK()) {
							// 发送通知广播
							if (res.getNotice() != null) {
								UIHelper.sendBroadCast(BlogDetail.this,
										res.getNotice());
							}
							// 恢复初始底部栏
							mFootViewSwitcher.setDisplayedChild(0);
							mFootEditer.clearFocus();
							mFootEditer.setText("");
							mFootEditer.setVisibility(View.GONE);
							// 跳到评论列表
							viewSwitch(VIEWSWITCH_TYPE_COMMENTS);
							// 更新评论列表
							lvCommentData.add(0, res.getComment());
							lvCommentAdapter.notifyDataSetChanged();
							mLvComment.setSelection(0);
							// 显示评论数
							int count = blogDetail.getCommentCount() + 1;
							blogDetail.setCommentCount(count);
							bv_comment.setText(count + "");
							bv_comment.show();
							// 清除之前保存的编辑内容
							ac.removeProperty(tempCommentKey);
						}
					} else {
						((AppException) msg.obj).makeToast(BlogDetail.this);
					}
				}
			};
			new Thread() {
				public void run() {
					Message msg = new Message();
					Result res = new Result();
					try {
						// 发表评论
						res = ac.pubBlogComment(_id, _uid, _content);
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
		}
	};

	/**
	 * 注册双击全屏事件
	 */
	private void regOnDoubleEvent() {
		gd = new GestureDetector(this,
				new GestureDetector.SimpleOnGestureListener() {
					@Override
					public boolean onDoubleTap(MotionEvent e) {
						isFullScreen = !isFullScreen;
						if (!isFullScreen) {
							WindowManager.LayoutParams params = getWindow()
									.getAttributes();
							params.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
							getWindow().setAttributes(params);
							getWindow()
									.clearFlags(
											WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
							getSupportActionBar().show();
							mFootViewSwitcher.setVisibility(View.VISIBLE);
						} else {
							WindowManager.LayoutParams params = getWindow()
									.getAttributes();
							params.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
							getWindow().setAttributes(params);
							getWindow()
									.addFlags(
											WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
							getSupportActionBar().hide();
							mFootViewSwitcher.setVisibility(View.GONE);
						}
						return true;
					}
				});
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent event) {
		if (isAllowFullScreen()) {
			gd.onTouchEvent(event);
		}
		return super.dispatchTouchEvent(event);
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater mInflater = getMenuInflater();
		mInflater.inflate(R.menu.news_detail_activity_menu, menu);

		favoriteItem = menu.findItem(R.id.news_detail_favorite);
		setFavoriteState();
		return true;
	}

	private void setFavoriteState() {
		if (blogDetail == null || favoriteItem == null)
			return;
		// 是否收藏
		if (blogDetail.getFavorite() == 1)
			favoriteItem.setIcon(R.drawable.ic_action_hasfavorite);
		else
			favoriteItem.setIcon(R.drawable.ic_action_nofavorite);
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);
		switch (item.getItemId()) {
		case R.id.news_detail_favorite:
			favorite();
			return true;
		case R.id.news_detail_share:
			share();
			return true;

		default:
			return false;
		}
	}
	
	private void favorite() {
		if (blogId == 0 || blogDetail == null) {
			return;
		}

		final AppContext ac = (AppContext) getApplication();
		if (!ac.isLogin()) {
			UIHelper.showLoginDialog(getSupportFragmentManager());
			return;
		}
		final int uid = ac.getLoginUid();
		
		UIHelper.ToastMessage(BlogDetail.this, R.string.news_detail_iscollection);

		final Handler handler = new Handler() {
			public void handleMessage(Message msg) {
				if (msg.what == 1) {
					Result res = (Result) msg.obj;
					if (res.OK()) {
						if (blogDetail.getFavorite() == 1) {
							blogDetail.setFavorite(0);
							favoriteItem.setIcon(R.drawable.ic_action_nofavorite);
						} else {
							blogDetail.setFavorite(1);
							favoriteItem.setIcon(R.drawable.ic_action_hasfavorite);
						}
						// 重新保存缓存
						ac.saveObject(blogDetail, blogDetail.getCacheKey());
					}
					UIHelper.ToastMessage(BlogDetail.this,
							res.getErrorMessage());
				} else {
					((AppException) msg.obj).makeToast(BlogDetail.this);
				}
			}
		};
		new Thread() {
			public void run() {
				Message msg = new Message();
				Result res = null;
				try {
					if (blogDetail.getFavorite() == 1) {
						res = ac.delFavorite(uid, blogId,
								FavoriteList.TYPE_BLOG);
					} else {
						res = ac.addFavorite(uid, blogId,
								FavoriteList.TYPE_BLOG);
					}
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
	}

	private void share() {
		if (blogDetail == null) {
			UIHelper.ToastMessage(BlogDetail.this,
					R.string.msg_read_detail_fail);
			return;
		}
		// 分享到
		UIHelper.showShareDialog(BlogDetail.this, blogDetail.getTitle(),
				blogDetail.getUrl());
	}

	protected void onClickRefresh() {
		hideEditor();
		initData(blogId, true);
		loadLvCommentData(curId, 0, mCommentHandler,
				UIHelper.LISTVIEW_ACTION_REFRESH);
	}

	public void setFooterViewVisibility(int v) {
		AnimUtil.FooterViewAnim(this, mFootViewSwitcher, v);
	}

}
