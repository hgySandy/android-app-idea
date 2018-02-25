package net.oschina.app.common;

public class SystemInfo {

	public static final int BASE 					= android.os.Build.VERSION_CODES.BASE;
	public static final int BASE_1_1 				= android.os.Build.VERSION_CODES.BASE_1_1;
	public static final int CUPCAKE 				= android.os.Build.VERSION_CODES.CUPCAKE;
	public static final int CUR_DEVELOPMENT 		= android.os.Build.VERSION_CODES.CUR_DEVELOPMENT;
	public static final int DONUT 					= android.os.Build.VERSION_CODES.DONUT;
	public static final int ECLAIR 					= android.os.Build.VERSION_CODES.ECLAIR;
	public static final int ECLAIR_0_1 				= android.os.Build.VERSION_CODES.ECLAIR_0_1;
	public static final int ECLAIR_MR1 				= android.os.Build.VERSION_CODES.ECLAIR_MR1;
	public static final int FROYO 					= android.os.Build.VERSION_CODES.FROYO;
	public static final int GINGERBREAD 			= android.os.Build.VERSION_CODES.GINGERBREAD;
	public static final int GINGERBREAD_MR1 		= android.os.Build.VERSION_CODES.GINGERBREAD_MR1;
	public static final int HONEYCOMB 				= android.os.Build.VERSION_CODES.HONEYCOMB;
	public static final int HONEYCOMB_MR1 			= android.os.Build.VERSION_CODES.HONEYCOMB_MR1;
	public static final int HONEYCOMB_MR2			= android.os.Build.VERSION_CODES.HONEYCOMB_MR2;
	public static final int ICE_CREAM_SANDWICH 		= android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH;
	public static final int ICE_CREAM_SANDWICH_MR1 	= android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1;
	public static final int JELLY_BEAN 				= android.os.Build.VERSION_CODES.JELLY_BEAN;

	public static int androidVersion() {
		return android.os.Build.VERSION.SDK_INT;
	}
}
