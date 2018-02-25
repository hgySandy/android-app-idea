package net.oschina.app.ui;


import net.oschina.app.AppConfig;
import net.oschina.app.AppContext;
import net.oschina.app.AppException;
import net.oschina.app.bean.Result;
import net.oschina.app.common.StringUtils;
import net.oschina.app.common.UIHelper;
import net.oschina.designapp.R;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

/**
 * 发表留言
 * @author liux (http://my.oschina.net/liux)
 * @version 1.0
 * @created 2012-3-21
 */
public class MessagePub extends BaseActionBarActivity{
	
	private TextView mReceiver;
	private EditText mContent;
    private ProgressDialog mProgress;
    private InputMethodManager imm;
    private String tempMessageKey = AppConfig.TEMP_MESSAGE;
	
	private int _uid;
	private int _friendid;
	private String _friendname;
	private String _content;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.message_pub);
		
		this.initView();
	}
	
    //初始化视图控件
    private void initView()
    {
    	// 设置ActionBar标题
    	getSupportActionBar().setTitle(R.string.message_pub_title);
    	imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
    	
		_uid = getIntent().getIntExtra("user_id", 0);
		_friendid = getIntent().getIntExtra("friend_id", 0);
		_friendname = getIntent().getStringExtra("friend_name");
    	
		if(_friendid > 0) tempMessageKey = AppConfig.TEMP_MESSAGE + "_" + _friendid;
		
    	mContent = (EditText)findViewById(R.id.message_pub_content);
    	mReceiver = (TextView)findViewById(R.id.message_pub_receiver);
    	
    	//编辑器添加文本监听
    	mContent.addTextChangedListener(UIHelper.getTextWatcher(this, tempMessageKey));
    	
    	//显示临时编辑内容
    	UIHelper.showTempEditContent(this, mContent, tempMessageKey);
    	
    	mReceiver.setText("发送留言给  "+_friendname);
    }    

	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater mInflater = getMenuInflater();
		mInflater.inflate(R.menu.activity_actionbar_pub, menu);
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
		if(StringUtils.isEmpty(_content)){
			UIHelper.ToastMessage(MessagePub.this, "请输入留言内容");
			return;
		}
		
		final AppContext ac = (AppContext)getApplication();
		if(!ac.isLogin()){
			UIHelper.showLoginDialog(getSupportFragmentManager());
			return;
		}
		
		mProgress = ProgressDialog.show(MessagePub.this, null, "发送中···",true,true); 
		
		final Handler handler = new Handler(){
			public void handleMessage(Message msg) {
				
				if(mProgress!=null)mProgress.dismiss();
				
				if(msg.what == 1){
					Result res = (Result)msg.obj;
					UIHelper.ToastMessage(MessagePub.this, res.getErrorMessage());
					if(res.OK()){
						//发送通知广播
						if(res.getNotice() != null){
							UIHelper.sendBroadCast(MessagePub.this, res.getNotice());
						}
						//清除之前保存的编辑内容
						ac.removeProperty(tempMessageKey);
						//返回刚刚发表的评论
						Intent intent = new Intent();
						intent.putExtra("COMMENT_SERIALIZABLE", res.getComment());
						setResult(RESULT_OK, intent);
						finish();
					}
				}
				else {
					((AppException)msg.obj).makeToast(MessagePub.this);
				}
			}
		};
		new Thread(){
			public void run() {
				Message msg =new Message();
				try {
					Result res = ac.pubMessage(_uid, _friendid, _content);
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
