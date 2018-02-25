package net.oschina.app.fragment;


import net.oschina.app.AppContext;
import net.oschina.app.common.UIHelper;
import net.oschina.app.widget.PullToRefreshListView;
import net.oschina.designapp.R;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

public class UserTweetFragment extends TweetFragment {
	private PullToRefreshListView lvTweet;
	private Button login;
	private AppContext appContext = AppContext.getAppContext();

	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View layout = inflater.inflate(R.layout.fragment_news, null);
		lvTweet = (PullToRefreshListView) layout.findViewById(R.id.frame_listview_news);
		lvTweet.setFootView(footerView);
		login = (Button) layout.findViewById(R.id.fragment_mian_login);
		// 判断登录
		int uid = appContext.getLoginUid();
		if (uid == 0) {
			login.setVisibility(View.VISIBLE);
			lvTweet.setVisibility(View.GONE);
			login.setOnClickListener(butOnClickListener);
		}
		else 
			initListView(uid, lvTweet, UIHelper.LISTVIEW_ACTION_INIT);

		return layout;
	}

	public void setUserVisibleHint(boolean isVisibleToUser) {
		super.setUserVisibleHint(isVisibleToUser);
		int uid = appContext.getLoginUid();
		if (isVisibleToUser && uid!= 0 && !isInitListView() && login!=null) {
			login.setVisibility(View.GONE);
			lvTweet.setVisibility(View.VISIBLE);
			initListView(uid, lvTweet, UIHelper.LISTVIEW_ACTION_INIT);
		}
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
				lvTweet.setVisibility(View.VISIBLE);
				initListView(appContext.getLoginUid(), lvTweet, UIHelper.LISTVIEW_ACTION_CHANGE_CATALOG);
			}
		}
	};
}
