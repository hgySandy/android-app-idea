package net.oschina.app.fragment;



import net.oschina.app.AppContext;
import net.oschina.app.AppException;
import net.oschina.app.api.ApiClient;
import net.oschina.app.bean.Result;
import net.oschina.app.bean.User;
import net.oschina.app.common.StringUtils;
import net.oschina.app.common.UIHelper;
import net.oschina.designapp.R;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.EditText;

public class LoginDialogFragment extends DialogFragment {
	public static final String TAG = "LoginDialogFragment";
	private InputMethodManager imm;
	private ProgressDialog progressDialog;
	
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
		imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
		final View layout = getActivity().getLayoutInflater().inflate(R.layout.fragment_dialog_login, null);
		dialog.setView(layout);
		dialog.setTitle(R.string.fragment_dialog_user_longin);
		dialog.setPositiveButton(R.string.fragment_dialog_positive, new OnClickListener() {
			
			public void onClick(DialogInterface dialog, int which) {
				AutoCompleteTextView accountView = (AutoCompleteTextView) layout.findViewById(R.id.login_account);
				EditText passwordView = (EditText) layout.findViewById(R.id.login_password);
				CheckBox rememberMe = (CheckBox)layout.findViewById(R.id.login_checkbox_rememberMe);
				
				//隐藏软键盘
				imm.hideSoftInputFromWindow(passwordView.getWindowToken(), 0);  
				
				String account = accountView.getText().toString();
				String pwd = passwordView.getText().toString();
				boolean isRememberMe = rememberMe.isChecked();
				//判断输入
				if(StringUtils.isEmpty(account)){
					UIHelper.ToastMessage(getActivity(), getString(R.string.msg_login_email_null));
					return;
				}
				if(StringUtils.isEmpty(pwd)){
					UIHelper.ToastMessage(getActivity(), getString(R.string.msg_login_pwd_null));
					return;
				}
				progressDialog = new ProgressDialog(getActivity());
				progressDialog.show();
				login(account, pwd, isRememberMe);
			}
		});
		dialog.setNegativeButton(R.string.fragment_dialog_negative, new OnClickListener() {
			
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				
			}
		});
		return dialog.create();
	}
	
	private AppContext appContext = AppContext.getAppContext();
	//登录验证
    private void login(final String account, final String pwd, final boolean isRememberMe) {
		final Handler handler = new Handler() {
			public void handleMessage(Message msg) {
				progressDialog.hide();
				if(msg.what == 1){
					User user = (User)msg.obj;
					if(user != null){
						//清空原先cookie
						ApiClient.cleanCookie();
						//发送通知广播
						UIHelper.sendBroadCast(appContext, user.getNotice());
						//提示登陆成功
						UIHelper.ToastMessage(appContext, R.string.msg_login_success);
						if (listener!=null) {
	                		listener.isLogin(true);
						}
					}
				}else if(msg.what == 0){
					UIHelper.ToastMessage(appContext, appContext.getString(R.string.msg_login_fail)+msg.obj);
					if (listener!=null) {
                		listener.isLogin(false);
					}
				}else if(msg.what == -1){
					((AppException)msg.obj).makeToast(appContext);
				}
			}
		};
		new Thread(){
			public void run() {
				Message msg =new Message();
				try {
	                User user = appContext.loginVerify(account, pwd);
	                user.setAccount(account);
	                user.setPwd(pwd);
	                user.setRememberMe(isRememberMe);
	                Result res = user.getValidate();
	                if(res.OK()){
	                	appContext.saveLoginInfo(user);//保存登录信息
	                	msg.what = 1;//成功
	                	msg.obj = user;
	                }else{
	                	appContext.cleanLoginInfo();//清除登录信息
	                	msg.what = 0;//失败
	                	msg.obj = res.getErrorMessage();
	                }
	            } catch (AppException e) {
	            	e.printStackTrace();
			    	msg.what = -1;
			    	msg.obj = e;
	            }
				handler.sendMessage(msg);
			}
		}.start();
    }
	
    private LoginDialogListener listener;
    
    public void setLoginDialogListener(LoginDialogListener listener){
    	this.listener = listener;
    }
    
    public interface LoginDialogListener{
    	public void isLogin(boolean bool);
    }
}
