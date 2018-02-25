package net.oschina.app.common;


import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import net.oschina.app.widget.BadgeView;
import net.oschina.designapp.R;

public class BadgeManager {

	private boolean activeShow;
	private boolean messageShow;
	private boolean atmeShow;
	private boolean commentShow;
	
	private String activeText;
	private String messageText;
	private String atmeText;
	private String commentText;

	private BadgeView active;
	private BadgeView message;
	private BadgeView atme;
	private BadgeView comment;
	
	private static volatile BadgeManager bManager;

	private BadgeManager() {
		// TODO Auto-generated constructor stub
	}

	public static BadgeManager getInstance() {
		if (bManager == null) {
			synchronized (BadgeManager.class) {
				if (bManager == null) {
					bManager = new BadgeManager();
				}
			}
		}
		return bManager;
	}
	
	public boolean isActiveShow() {
		return activeShow;
	}

	public void setActiveShow(String text, boolean activeShow) {
		refreshBadgeView(active, text, activeShow);
		this.activeText = text;
		this.activeShow = activeShow;
	}

	public boolean isMessageShow() {
		return messageShow;
	}

	public void setMessageShow(String text, boolean messageShow) {
		refreshBadgeView(message, text, messageShow);
		this.messageText = text;
		this.messageShow = messageShow;
	}

	public boolean isAtmeShow() {
		return atmeShow;
	}

	public void setAtmeShow(String text, boolean atmeShow) {
		refreshBadgeView(atme, text, atmeShow);
		this.atmeText = text;
		this.atmeShow = atmeShow;
	}

	public boolean isCommentShow() {
		return commentShow;
	}

	public void setCommentShow(String text, boolean commentShow) {
		refreshBadgeView(comment, text, commentShow);
		this.commentText = text;
		this.commentShow = commentShow;
	}
	private void refreshBadgeView(BadgeView bView, String text, boolean isShow){
		if (bView==null)
			return;
		else
			bView.setText(text);
		
		if (isShow)
			bView.show();
		else
			bView.hide();
	}
	public BadgeView setActive(Context context, View target) {
		active = getBadgeView(context, target);
		refreshBadgeView(active, activeText, activeShow);
		return active;
	}

	public BadgeView setMessage(Context context, View target) {
		message = getBadgeView(context, target);
		refreshBadgeView(message, messageText, messageShow);
		return message;
	}

	public BadgeView setAtme(Context context, View target) {
		atme = getBadgeView(context, target);
		refreshBadgeView(atme, atmeText, atmeShow);
		return atme;
	}

	public BadgeView setComment(Context context, View target) {
		comment = getBadgeView(context, target);
		refreshBadgeView(comment, commentText, commentShow);
		return comment;
	}

	public BadgeView getActive() {
		return active;
	}

	public BadgeView getMessage() {
		return message;
	}

	public BadgeView getAtme() {
		return atme;
	}

	public BadgeView getComment() {
		return comment;
	}

	public BadgeManager getbManager() {
		return bManager;
	}
	
	public boolean isShowActive(){
		if (active != null)
			return active.isShown();
		else
			return false;
	}
	
	public boolean isShowMessage(){
		if (message != null)
			return message.isShown();
		else
			return false;
	}
	
	public boolean isShowAtme(){
		if (atme != null)
			return atme.isShown();
		else
			return false;
	}
	
	public boolean isShowComment(){
		if (comment != null)
			return comment.isShown();
		else
			return false;
	}
	
	public void showActive(){
		if (active != null)
			active.show();
	}
	
	public void showMessage(){
		if (message != null)
			message.show();
	}
	
	public void showAtme(){
		if (atme != null)
			atme.show();
	}
	
	public void showComment(){
		if (comment != null)
			comment.show();
	}
	
	public void showAll(){
		
	}
	
	private BadgeView getBadgeView(Context context, View target){
		BadgeView bView = new BadgeView(context, target);
		bView.setBackgroundResource(R.drawable.widget_count_bg);
		bView.setIncludeFontPadding(false);
		bView.setGravity(Gravity.CENTER);
		bView.setTextSize(8f);
		bView.setTextColor(Color.WHITE);
		return bView;
	}
}
