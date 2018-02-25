package net.oschina.app.fragment;


import net.oschina.app.AppContext;
import net.oschina.app.AppException;
import net.oschina.app.bean.Report;
import net.oschina.app.common.StringUtils;
import net.oschina.app.common.UIHelper;
import net.oschina.designapp.R;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
/**
 * 举报操作窗口
 * @author 火蚁（http://my.oschina.net/LittleDY）
 * @version 1.0
 * @created 2014-02-13
 */
public class ReportUi extends DialogFragment {
	private AppContext ac;
	private TextView mLink;
	private Spinner mReason;
	private EditText mOtherReason;
	
	private ProgressDialog mProgress;
	
	@Override
	@NonNull
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		super.onCreateDialog(savedInstanceState);
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		View layout = getActivity().getLayoutInflater().inflate(R.layout.report, null);
		initView(layout);
		builder.setTitle(R.string.report_title).setView(layout);
		builder.setPositiveButton(R.string.fragment_dialog_positive, publishListener);
		builder.setNegativeButton(R.string.fragment_dialog_negative, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				
			}
		});
		return builder.create();
	}

	
	private void initView(View layout) {
		ac = (AppContext)getActivity().getApplication();
		mLink = (TextView) layout.findViewById(R.id.report_link);
		mReason = (Spinner) layout.findViewById(R.id.report_reason);
		mOtherReason = (EditText) layout.findViewById(R.id.report_other_reson);
		initData();
	}
	
	private void initData() {
		mLink.setText(getArguments().getString(Report.REPORT_LINK));
	}
	
	private DialogInterface.OnClickListener publishListener = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int which) {
			final Report report = new Report();
			report.setLinkAddress(mLink.getText() + "");
			report.setReportId(ac.getLoginUid());
			String otherReason = mOtherReason.getText().toString().trim();
			if (StringUtils.isEmpty(otherReason)) {
				report.setOtherReason("其他原因");
			} else {
				report.setOtherReason(otherReason);
			}
			report.setReason(mReason.getSelectedItem().toString());
			mProgress = ProgressDialog.show(getActivity(), null, "举报信息发送中...", true, true);
			final Handler handler = new Handler(){
				public void handleMessage(Message msg) {
					if(mProgress!=null)mProgress.dismiss();
					if(msg.what == 0){
						UIHelper.ToastMessage(getActivity(), "发送成功");
					} else if (msg.what == 1) {
						UIHelper.ToastMessage(getActivity(), "发送失败");
					}
					else {
						((AppException)msg.obj).makeToast(getActivity());
					}
				}
			};
			new Thread(){
				public void run() {
					Message msg = new Message();
					String res = "";
						try {
							res = ac.report(report);
							msg.obj = res;
							if (res == null || res.equals("")) {
								msg.what = 0;
							} else {
								msg.what = 1;
							}
						} catch (AppException e) {
							e.printStackTrace();
						}
					handler.sendMessage(msg);
				}
			}.start();
		}
	};
}
