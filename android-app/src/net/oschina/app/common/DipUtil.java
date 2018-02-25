package net.oschina.app.common;

import net.oschina.app.AppContext;

public class DipUtil {
	/**
	 * 本机的屏幕密度
	 */
	private static final float DENSITY = AppContext.getAppContext().getResources().getDisplayMetrics().density;
	
	/**
	 * 
	 * @Method: dpChangePix 
	 * @Description: 将输入的DP值转换成PIX值
	 * @throws 
	 * @param dp 输入要转换的DP值
	 * @return
	 */
	public static int dpChangePix(float dp) {
		// DP*本机的屏幕密度，再加上0.5来四舍五入到最接近的数。
		return (int) (dp * DENSITY + 0.5f);
	}
	/**
	 * 
	 * @Method: pixChangeDp 
	 * @Description: 将输入的PIX值转换成DP值
	 * @throws 
	 * @param pix 输入要转换的PIX值
	 * @return
	 */
	public static int pixChangeDp(float pix) {
		return (int) (pix / DENSITY + 0.5f);
	}
}
