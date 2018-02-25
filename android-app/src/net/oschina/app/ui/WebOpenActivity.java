package net.oschina.app.ui;

import net.oschina.app.common.UIHelper;
import android.app.Activity;
import android.os.Bundle;

public class WebOpenActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		String url = getIntent().getData().toString();
		UIHelper.showUrlRedirect(WebOpenActivity.this, url);
		finish();
	}
	
}
