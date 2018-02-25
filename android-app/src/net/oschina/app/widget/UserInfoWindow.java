package net.oschina.app.widget;


import net.oschina.app.bean.User;
import net.oschina.app.common.DipUtil;
import net.oschina.app.common.StringUtils;
import net.oschina.app.common.UIHelper;
import net.oschina.designapp.R;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager.LayoutParams;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

/**
 * 用户信息对话框控件
 * @author liux (http://my.oschina.net/liux)
 * @version 1.0
 * @created 2014-7-14
 */
public class UserInfoWindow extends DialogFragment {
	
	private User mUser;
	
	private ImageView mUserface;
	private TextView mUsername;
	private TextView mFrom;
	private TextView mGender;
	private TextView mJointime;
	private TextView mDevplatform;
	private TextView mExpertise;
	private TextView mLatestonline;
	
	@Override
	@NonNull
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		UserInfoDialog userInfo = new UserInfoDialog(getActivity());
		init(userInfo);
		return userInfo;
	}

	private void init(Dialog layout){
		mUserface = (ImageView)layout.findViewById(R.id.user_center_userface);
    	mUsername = (TextView)layout.findViewById(R.id.user_center_username);
    	mFrom = (TextView)layout.findViewById(R.id.user_center_from);
    	mGender = (TextView)layout.findViewById(R.id.user_center_gender);
    	mJointime = (TextView)layout.findViewById(R.id.user_center_jointime);
    	mDevplatform = (TextView)layout.findViewById(R.id.user_center_devplatform);
    	mExpertise = (TextView)layout.findViewById(R.id.user_center_expertise);
    	mLatestonline = (TextView)layout.findViewById(R.id.user_center_latestonline);
    	
    	if (mUser!=null) {
    		mUsername.setText(mUser.getName());
    		mFrom.setText(mUser.getLocation());
    		mGender.setText(mUser.getGender());
    		mJointime.setText(StringUtils.friendly_time(mUser.getJointime()));
    		mDevplatform.setText(mUser.getDevplatform());
    		mExpertise.setText(mUser.getExpertise());
    		mLatestonline.setText(StringUtils.friendly_time(mUser.getLatestonline()));
    		//加载用户头像
    		UIHelper.showUserFace(mUserface, mUser.getFace());
		}
	}
	
	public void setUserInfo(User mUser){
		this.mUser = mUser;
	}
	
	class UserInfoDialog extends Dialog{
		private LayoutParams lp;

		public UserInfoDialog(Context context) {
			super(context, R.style.Dialog);		
			setContentView(R.layout.user_center_content);
			
			// 设置点击对话框之外能消失
			setCanceledOnTouchOutside(true);
			// 设置window属性
			lp = getWindow().getAttributes();
			lp.gravity = Gravity.TOP;
			lp.dimAmount = 0; // 去背景遮盖
			lp.alpha = 1.0f;
			lp.y = DipUtil.dpChangePix(48);
			getWindow().setAttributes(lp);

		}
	}
}
