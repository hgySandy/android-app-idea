package net.oschina.app.ui;


import net.oschina.app.common.StringUtils;
import net.oschina.app.common.UIHelper;
import net.oschina.designapp.R;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.EditText;

/**
 * 用户反馈
 * 
 * @author liux (http://my.oschina.net/liux)
 * @version 1.0
 * @created 2012-3-21
 */
public class FeedBack extends DialogFragment {

	private EditText mEditer;

	@SuppressLint("InflateParams")
	@Override
	@NonNull
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		super.onCreateDialog(savedInstanceState);
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		View layout = getActivity().getLayoutInflater().inflate(R.layout.feedback, null);
		initView(layout);
		builder.setView(layout);
		builder.setTitle(R.string.feedback_title);
		builder.setPositiveButton(R.string.feedback_publish,
				publishClickListener);
		builder.setNegativeButton(R.string.fragment_dialog_negative,
				new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub

					}
				});
		return builder.create();
	}

	// 初始化视图控件
	private void initView(View layout) {
		mEditer = (EditText) layout.findViewById(R.id.feedback_content);
	}

	private DialogInterface.OnClickListener publishClickListener = new DialogInterface.OnClickListener() {

		@Override
		public void onClick(DialogInterface dialog, int which) {
			String content = mEditer.getText().toString().trim();

			if (StringUtils.isEmpty(content)) {
				UIHelper.ToastMessage(getActivity(), "反馈信息不能为空");
				return;
			}

			Intent i = new Intent(Intent.ACTION_SEND);
			// i.setType("text/plain"); //模拟器
			i.setType("message/rfc822"); // 真机
			i.putExtra(Intent.EXTRA_EMAIL, new String[] { "lan4627@gmail.net" });
			i.putExtra(Intent.EXTRA_SUBJECT, "用户反馈-Android客户端");
			i.putExtra(Intent.EXTRA_TEXT, content);
			startActivity(Intent.createChooser(i, "Sending mail..."));
		}
	};
}
