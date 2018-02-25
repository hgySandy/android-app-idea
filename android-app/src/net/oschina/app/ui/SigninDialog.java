package net.oschina.app.ui;


import net.oschina.app.AppContext;
import net.oschina.app.AppException;
import net.oschina.app.AppManager;
import net.oschina.app.bean.Barcode;
import net.oschina.app.bean.JsonResult;
import net.oschina.app.bean.Report;
import net.oschina.app.common.StringUtils;
import net.oschina.app.common.UIHelper;
import net.oschina.designapp.R;
import android.R.layout;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
/**
 * 线下活动签到
 * @author 火蚁（http://my.oschina.net/LittleDY）
 * @version 1.0
 * @created 2014-03-18
 */
public class SigninDialog extends DialogFragment {
	
	public static final String TAG = SigninDialog.class.getSimpleName();
	
	private AppContext ac;
	private TextView mTitle;// 活动标题
	
	private ProgressDialog mProgress;
	private Barcode barcode;
	
	
	
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		super.onCreateDialog(savedInstanceState);
		AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
		dialog.setTitle(R.string.signin_title);
		dialog.setView(initView());
		dialog.setPositiveButton(R.string.signin_positive, positive);
		dialog.setNegativeButton(R.string.fragment_dialog_negative, negative);
		return dialog.create();
	}
	
	private View initView() {
		ac = (AppContext)getActivity().getApplication();
		View layout = getActivity().getLayoutInflater().inflate(R.layout.signin, null);
		mTitle = (TextView) layout.findViewById(R.id.signin_title);
		mTitle.setText(getDetail());
		return layout;
	}
	
	private String getDetail() {
		barcode = (Barcode) getArguments().getSerializable("barcode");
		return barcode.getTitle();
	}
	
	private  DialogInterface.OnClickListener positive = new DialogInterface.OnClickListener() {
		
		public void onClick(DialogInterface dialog, int which) {
			signIn(barcode);
		}
	};
	
	private  DialogInterface.OnClickListener negative = new DialogInterface.OnClickListener() {
		
		public void onClick(DialogInterface dialog, int which) {
			//
		}
	};
	
	/**
	 * 签到
	 * @param barcode
	 */
	private void signIn(final Barcode barcode) {
		// 如果网络没有连接则返回
		if (!ac.isNetworkConnected()) {
			UIHelper.ToastMessage(getActivity(), "当前网络不可用，请检查网络设置", Toast.LENGTH_LONG);
			return;
		}
		mProgress = ProgressDialog.show(getActivity(), null, "正在签到，请稍候...", true, true);
		final Handler handler = new Handler(){
			public void handleMessage(Message msg) {
				if(mProgress!=null)mProgress.dismiss();
				if(msg.what == 0){
					try {
						JsonResult res = JsonResult.parse(msg.obj.toString());
						if (res.isOk()) {
							UIHelper.ToastMessage(getActivity(), res.getMessage(), Toast.LENGTH_LONG);
						} else {
							UIHelper.ToastMessage(getActivity(), res.getErrorMes(), Toast.LENGTH_LONG);
						}
					} catch (AppException e) {
						e.printStackTrace();
					}
				} else {
					((AppException)msg.obj).makeToast(getActivity());
				}
			}
		};
		new Thread(){
			public void run() {
				Message msg = new Message();
				String res = "";
				try {
					res = ac.signIn(barcode);
					if (mProgress != null && mProgress.isShowing()) {
						mProgress.dismiss();
					}
					msg.what = 0;
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
}
