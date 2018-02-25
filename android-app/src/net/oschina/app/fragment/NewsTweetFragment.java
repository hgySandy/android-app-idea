package net.oschina.app.fragment;


import net.oschina.app.bean.NewsList;
import net.oschina.app.bean.TweetList;
import net.oschina.app.common.UIHelper;
import net.oschina.app.widget.PullToRefreshListView;
import net.oschina.designapp.R;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class NewsTweetFragment extends TweetFragment {
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_news, null);
		PullToRefreshListView lvTweet = (PullToRefreshListView) view.findViewById(R.id.frame_listview_news);
		int curTweetCatalog = getArguments().getInt(NewsList.CATLOG, TweetList.CATALOG_LASTEST);
		initListView(curTweetCatalog, lvTweet, UIHelper.LISTVIEW_ACTION_INIT);
		return view;
	}
}
