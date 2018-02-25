package net.oschina.app.common;

import net.oschina.designapp.R;
import android.content.Context;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

public class AnimUtil {
	public AnimUtil(){
		
	}
	public static void FooterViewAnim(Context context, View footerView, int visibility){
		Animation anim = null;
		switch (visibility) {
		case View.VISIBLE:
			if (footerView.getVisibility()!=View.VISIBLE) {
				anim = AnimationUtils.loadAnimation(context, R.anim.footer_in_bottom);
				footerView.setAnimation(anim);
				footerView.setVisibility(View.VISIBLE);
			}
			break;
		case View.GONE:
			if (footerView.getVisibility()!=View.INVISIBLE) {
				anim = AnimationUtils.loadAnimation(context, R.anim.footer_out_bottom);
				footerView.setAnimation(anim);
				footerView.setVisibility(View.INVISIBLE);
			}
			break;
		case View.INVISIBLE:
			if (footerView.getVisibility()!=View.INVISIBLE) {
				anim = AnimationUtils.loadAnimation(context, R.anim.footer_out_bottom);
				footerView.setAnimation(anim);
				footerView.setVisibility(View.INVISIBLE);
			}
			break;
			
		default:
			break;
		}
	}
}
