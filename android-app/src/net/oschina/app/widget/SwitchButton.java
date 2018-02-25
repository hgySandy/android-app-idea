package net.oschina.app.widget;

import net.oschina.designapp.R;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ImageView;
import android.widget.ViewSwitcher;
/**
 * 此组件暂未写好，未启用。
 * @author zhen
 *
 */
public class SwitchButton implements TextWatcher {
	private ImageView button;
	private ViewSwitcher viewSwitcher;
	private Drawable bakcgroundDrawable;
	private View.OnClickListener onClickListener;
	public SwitchButton(ImageView button, View.OnClickListener onClickListener, ViewSwitcher viewSwitcher){
		this.button = button;
		this.viewSwitcher = viewSwitcher;
		this.onClickListener = onClickListener;
		this.bakcgroundDrawable = button.getDrawable();
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void afterTextChanged(Editable s) {
		if (s.toString().equals("")) {
			button.setImageResource(R.drawable.ic_footer_switch);
			button.setOnClickListener(closeEditTextClickListener);
		}else {
			button.setImageDrawable(bakcgroundDrawable);
			button.setOnClickListener(onClickListener);
		}
	}
	
	private View.OnClickListener closeEditTextClickListener = new View.OnClickListener() {
		public void onClick(View v) {
			viewSwitcher.showNext();
		}
	};
}
