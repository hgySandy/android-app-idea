package net.oschina.app.ui;


import net.oschina.app.AppContext;
import net.oschina.app.common.BadgeManager;
import net.oschina.app.widget.BadgeView;
import net.oschina.designapp.R;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

/**
 * 通知信息广播接收器
 * 
 * @author liux (http://my.oschina.net/liux)
 * @version 1.1
 * @created 2014-6-22
 */
public class BroadCast extends BroadcastReceiver {

	private final static int NOTIFICATION_ID = R.layout.main_activity_layout;

	private static int lastNoticeCount;

	@Override
	public void onReceive(Context context, Intent intent) {
		String ACTION_NAME = intent.getAction();
		if ("net.oschina.app.action.APPWIDGET_UPDATE".equals(ACTION_NAME)) {
			int atmeCount = intent.getIntExtra("atmeCount", 0);// @我
			int msgCount = intent.getIntExtra("msgCount", 0);// 留言
			int reviewCount = intent.getIntExtra("reviewCount", 0);// 评论
			int newFansCount = intent.getIntExtra("newFansCount", 0);// 新粉丝
			int activeCount = atmeCount + reviewCount + msgCount + newFansCount;// 信息总数

			BadgeManager bManager = BadgeManager.getInstance();
			// 动态-总数
			if (activeCount > 0) {
				bManager.setActiveShow(activeCount + "", true);
			} else {
				bManager.setActiveShow("", false);
			}
			// @我
			if (atmeCount > 0) {
				bManager.setAtmeShow(atmeCount + "", true);
			} else {
				bManager.setAtmeShow("", false);
			}
			// 评论
			if (reviewCount > 0) {
				bManager.setCommentShow(reviewCount + "", true);
			} else {
				bManager.setCommentShow("", false);
			}
			// 留言
			if (msgCount > 0) {
				bManager.setMessageShow(msgCount + "", true);
			} else {
				bManager.setMessageShow("", false);
			}

			// 通知栏显示
			this.notification(context, activeCount);
		}
	}

	private void notification(Context context, int noticeCount) {
		// 创建 NotificationManager
		NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

		String contentTitle = "开源中国";
		String contentText = "您有 " + noticeCount + " 条最新信息";
		int _lastNoticeCount;

		// 判断是否发出通知信息
		if (noticeCount == 0) {
			notificationManager.cancelAll();
			lastNoticeCount = 0;
			return;
		} else if (noticeCount == lastNoticeCount) {
			return;
		} else {
			_lastNoticeCount = lastNoticeCount;
			lastNoticeCount = noticeCount;
		}

		// 创建通知 Notification
		Notification notification = null;

		Notification.Builder builder = new Notification.Builder(context);

		if (noticeCount > _lastNoticeCount) {
			String noticeTitle = "您有 " + (noticeCount - _lastNoticeCount) + " 条最新信息";
			//低版本
			//notification = new Notification(R.drawable.icon, noticeTitle, System.currentTimeMillis());

			builder.setAutoCancel(true)
					.setSmallIcon(R.drawable.icon)
					.setContentTitle(noticeTitle)
					.setWhen(System.currentTimeMillis());

		} else {
			//notification = new Notification();
		}

		// 设置点击通知跳转
		Intent intent = new Intent(context, MainActivity.class);
		intent.putExtra("NOTICE", true);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

		PendingIntent contentIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);



		// 设置最新信息
		//notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);

		builder.setAutoCancel(true)
				.setContentTitle(contentTitle)
				.setContentText(contentText)
				.setContentIntent(contentIntent);

		notification = builder.getNotification();

		// 设置点击清除通知
		notification.flags = Notification.FLAG_AUTO_CANCEL;

		if (noticeCount > _lastNoticeCount) {
			// 设置通知方式
			notification.defaults |= Notification.DEFAULT_LIGHTS;

			// 设置通知音-根据app设置是否发出提示音
			if (((AppContext) context.getApplicationContext()).isAppSound())
				notification.sound = Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.notificationsound);

			// 设置振动 <需要加上用户权限android.permission.VIBRATE>
			// notification.vibrate = new long[]{100, 250, 100, 500};
		}

		// 发出通知
		notificationManager.notify(NOTIFICATION_ID, notification);
	}

}
