package net.oschina.app.fragment;


import net.oschina.app.bean.NewsList;
import net.oschina.app.widget.TabButton;
import net.oschina.designapp.R;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public abstract class MainFragment extends Fragment {
	private ViewPager viewPager;
	public final static String CATLOG = "CATLOG";
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_main, container, false);
		viewPager = (ViewPager) view.findViewById(R.id.fragment_main_viewpager);
		viewPager.setAdapter(getPagerAdapter());
		TabButton tabsButton = (TabButton) view.findViewById(R.id.fragment_main_tabsbutton);
		tabsButton.setViewPager(viewPager);
		return view;
	}
	
	abstract PagerAdapter getPagerAdapter();
	
	protected Fragment addBundle(Fragment fragment, int catlog){
		Bundle bundle = new Bundle();
		bundle.putInt(MainFragment.CATLOG, catlog);
		fragment.setArguments(bundle);
		return fragment;
	}
}
