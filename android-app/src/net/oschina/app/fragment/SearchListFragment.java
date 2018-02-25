package net.oschina.app.fragment;

import java.util.ArrayList;
import java.util.List;


import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import net.oschina.app.AppContext;
import net.oschina.app.AppException;
import net.oschina.app.adapter.ListViewSearchAdapter;
import net.oschina.app.bean.ListData;
import net.oschina.app.bean.NewsList;
import net.oschina.app.bean.SearchList;
import net.oschina.app.bean.SearchList.Result;
import net.oschina.app.common.HandlerManager;
import net.oschina.app.common.StringUtils;
import net.oschina.app.common.UIHelper;
import net.oschina.app.common.HandlerManager.LoadListDataCallbacks;
import net.oschina.app.inteface.ActionBarProgressBarVisibility;
import net.oschina.designapp.R;

public class SearchListFragment extends Fragment implements LoadListDataCallbacks{
	
	private ActionBarProgressBarVisibility abProgress;
	private SearchContextListener searchContext;
	
	private ListView mlvSearch;
	private String curSearchCatalog;
	private Handler mHandler;
	private AppContext appContext = AppContext.getAppContext();
	private List<Result> lvSearchData = new ArrayList<Result>();
	private int lvSumData;
	
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			abProgress = (ActionBarProgressBarVisibility)activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()+"must implenemt ActionBarProgressVisibility");
		}
		try {
			searchContext = (SearchContextListener)activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()+"must implenemt SearchContextListener");
		}
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater,
			@Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		curSearchCatalog = getArguments().getString(NewsList.CATLOG);
		View layout = inflater.inflate(R.layout.search_listview, container, false);
		initListView(layout, UIHelper.LISTVIEW_ACTION_INIT);
		return layout;
	}
	
	public void setUserVisibleHint(boolean isVisibleToUser) {
		super.setUserVisibleHint(isVisibleToUser);
		if (mHandler!=null)
			loadLvData(0, mHandler, UIHelper.LISTVIEW_ACTION_INIT);
	}
	
	private void initListView(View layout, int action) {
		mlvSearch = (ListView)layout.findViewById(R.id.search_listview);
		final View fooer = getActivity().getLayoutInflater().inflate(R.layout.listview_footer, mlvSearch, false);
		final TextView footMore = (TextView)fooer.findViewById(R.id.listview_foot_more);
		ListViewSearchAdapter adapter = new ListViewSearchAdapter(getActivity(), lvSearchData, R.layout.search_listitem); 
		HandlerManager handlerUtil = HandlerManager.getInstance();
		mHandler = handlerUtil.getSearcHandler(mlvSearch, adapter, footMore, abProgress, this);
		loadLvData(0, mHandler, action);
		mlvSearch.setVisibility(View.GONE);
    	mlvSearch.addFooterView(fooer);//添加底部视图  必须在setAdapter前
    	mlvSearch.setAdapter(adapter); 
    	mlvSearch.setOnItemClickListener(new AdapterView.OnItemClickListener() {
        	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        		//点击底部栏无效
        		if(view == fooer) return;
        		
        		Result res = null;
        		//判断是否是TextView
        		if(view instanceof TextView){
        			res = (Result)view.getTag();
        		}else{
        			TextView title = (TextView)view.findViewById(R.id.search_listitem_title);
        			res = (Result)title.getTag();
        		} 
        		if(res == null) return;
        		
        		//跳转
        		UIHelper.showUrlRedirect(view.getContext(), res.getUrl());
        	}
		});
    	mlvSearch.setOnScrollListener(new AbsListView.OnScrollListener() {
			public void onScrollStateChanged(AbsListView view, int scrollState) {				
				//数据为空--不用继续下面代码了
				if(lvSearchData.size() == 0) return;
				
				//判断是否滚动到底部
				boolean scrollEnd = false;
				try {
					if(view.getPositionForView(fooer) == view.getLastVisiblePosition())
						scrollEnd = true;
				} catch (Exception e) {
					scrollEnd = false;
				}
				
				int lvDataState = StringUtils.toInt(mlvSearch.getTag());
				if(scrollEnd && lvDataState==UIHelper.LISTVIEW_DATA_MORE)
				{
					mlvSearch.setTag(UIHelper.LISTVIEW_DATA_LOADING);
					footMore.setText(R.string.load_ing);
					//当前pageIndex
					int pageIndex = lvSumData/20;
					loadLvData(pageIndex, mHandler, UIHelper.LISTVIEW_ACTION_SCROLL);
				}
			}
			public void onScroll(AbsListView view, int firstVisibleItem,int visibleItemCount, int totalItemCount) {
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
     * 线程加载收藏数据
     * @param type 0:全部收藏 1:软件 2:话题 3:博客 4:新闻 5:代码
     * @param pageIndex 当前页数
     * @param handler 处理器
     * @param action 动作标识
     */
	private void loadLvData(final int pageIndex, final Handler handler,
			final int action) {
		final String searchString = searchContext.getSearchContext();
		if(StringUtils.isEmpty(searchString)){
			UIHelper.ToastMessage(getActivity(), "请输入搜索内容");
			return;
		}
		abProgress.setProgressBarVisibility(View.VISIBLE);
		new Thread(){
			public void run() {
				Message msg = new Message();
				try {
					SearchList searchList = appContext.getSearchList(curSearchCatalog, searchString, pageIndex, 20);
					msg.what = searchList.getPageSize();
					msg.obj = searchList;
	            } catch (AppException e) {
	            	e.printStackTrace();
	            	msg.what = -1;
	            	msg.obj = e;
	            }
				msg.arg1 = action;//告知handler当前action
				handler.sendMessage(msg);
			}
		}.start();
	}
	
	@Override
	public void onLoadDataFinished(ListData dataList) {
		lvSearchData.clear();
		lvSearchData.addAll(dataList.getSearchData());
		lvSumData = dataList.getSearchSumData();
	}
	
	@Override
	public List<?> getListData() {
		return lvSearchData;
	}
	
	public void search(int action){
		loadLvData(0, mHandler, action);
	}
	
	public interface SearchContextListener{
		public String getSearchContext();
	}

}
