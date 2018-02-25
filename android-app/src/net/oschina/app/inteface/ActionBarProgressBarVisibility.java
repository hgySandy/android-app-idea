package net.oschina.app.inteface;

public interface ActionBarProgressBarVisibility {
	public static final int VISIBLE = 0x00000000;
	public static final int INVISIBLE = 0x00000004;
	public static final int GONE = 0x00000008;
	public void setProgressBarVisibility(int v);
}
