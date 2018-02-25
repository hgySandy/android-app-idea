package net.oschina.app.ui;


import net.oschina.app.AppContext;
import net.oschina.app.AppException;
import net.oschina.app.bean.Result;
import net.oschina.app.common.StringUtils;
import net.oschina.app.common.UIHelper;
import net.oschina.designapp.R;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

/**
 * 转发留言
 * @author liux (http://my.oschina.net/liux)
 * @version 1.1
 * @created 2014-7-2
 */
public class MessageForward extends BaseActionBarActivity{
	
	private EditText mReceiver;
	private EditText mContent;
    private ProgressDialog mProgress;
    private InputMethodManager imm;
	
	private int _uid;
	private String _content;
	private String _receiver;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.message_forward);
		
		this.initView();
	}
	
    //初始化视图控件
    private void initView()
    {
    	getSupportActionBar().setTitle(R.string.message_forword_title);
    	imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
    	
    	String friend_name = "@" + getIntent().getStringExtra("friend_name") + " ";
		_uid = getIntent().getIntExtra("user_id", 0);
		_content = friend_name + getIntent().getStringExtra("message_content");
    	
    	mContent = (EditText)findViewById(R.id.message_forward_content);
    	mReceiver = (EditText)findViewById(R.id.message_forward_receiver);
    	
    	mContent.setText(_content);
    } 
	
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.activity_actionbar_pub, menu);
		return true;
	}

	public boolean onContextItemSelected(MenuItem item) {
		super.onContextItemSelected(item);
		switch (item.getItemId()) {
		case R.id.actionbar_pub:
			publish();
			return true;

		default:
			return false;
		}
	}
	
	private void publish(){
		//隐藏软键盘
		imm.hideSoftInputFromWindow(mContent.getWindowToken(), 0);  
		
		_content = mContent.getText().toString();
		_receiver = mReceiver.getText().toString();
		if(StringUtils.isEmpty(_content)){
			UIHelper.ToastMessage(MessageForward.this, "请输入留言内容");
			return;
		}
		if(StringUtils.isEmpty(_receiver)){
			UIHelper.ToastMessage(MessageForward.this, "请输入对方的昵称");
			return;
		}
		
		final AppContext ac = (AppContext)getApplication();
		if(!ac.isLogin()){
			UIHelper.showLoginDialog(getSupportFragmentManager());
			return;
		}
		
		mProgress = ProgressDialog.show(MessageForward.this, null, "发送中···",true,true); 
		
		final Handler handler = new Handler(){
			public void handleMessage(Message msg) {
				
				if(mProgress!=null)mProgress.dismiss();
				
				if(msg.what == 1){
					Result res = (Result)msg.obj;
					UIHelper.ToastMessage(MessageForward.this, res.getErrorMessage());
					if(res.OK()){
						//发送通知广播
						if(res.getNotice() != null){
							UIHelper.sendBroadCast(MessageForward.this, res.getNotice());
						}
						finish();
					}
				}
				else {
					((AppException)msg.obj).makeToast(MessageForward.this);
				}
			}
		};
		new Thread(){
			public void run() {
				Message msg =new Message();
				try {
					Result res = ac.forwardMessage(_uid, _receiver, _content);
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
}
