package net.oschina.app.widget;

import net.oschina.designapp.R;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class TabButton extends HorizontalScrollView {
	
	private static final int DEF_LINE_COLOR   = 0x66000000; 
	private static final int DEF_SLIDER_COLOR = 0xFF6595F9;
	private static final int DEF_TEXT_COLOR   = 0xFF000000;
	private static final int DEF_TAB_PADDING  = 20;
	
	private static final float DEF_BOTTON_LINE_SIZE = 0.5f;
	private static final float DEF_DIVIDER_SIZE = 20f;
	private static final float DEF_SLIDER_SIZE = 4f;
	private static final float DEF_TEXT_SIZE = 14f;
	
	private int screenWidth;
	
	private LinearLayout mLinearLayout;
	private ViewPager mViewPager;
	
	private int mButtonBackground;	// 按钮背景
	private float mTextSize;		// 字体大小
	private int mTextColor;			// 字体颜色
	
	private Paint sliderPaint;		// 滑块的画笔
	private Paint bottomPaint;		// 底部线条的画笔
	private Paint dividerPaint;		// 分割线的画笔
	
	private float dividerSize; 		// 分割线高度
	private float bottomLineSize;	// 底部线高度
	private float sliderSize;		// 滑块高度
	
	private int sliderWidth;		// 滑块宽度
	private int tabSize;			// 选项卡的个数
	private int tabPadding;			// 选项卡的边距
	private int pageSelect;			// 当前选中的页面

	private float scrollOffset;		// 滑块已经滑动过的宽度

	private TabsButtonOnClickListener onClickListener;

	public TabButton(Context context) {
		super(context);
		init(context, null);
	}

	public TabButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		init(context, attrs);
	}

	@SuppressLint("NewApi")
	public TabButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context, attrs);
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		final int layoutHeight = getHeight();
		final int layoutWidth  = getWidth();
		// 绘制分割线
		for (int i = 0; i < mLinearLayout.getChildCount(); i++) {
			View view = mLinearLayout.getChildAt(i);
			float pianyi = (layoutHeight - dividerSize) / 2;
			canvas.drawRect(view.getLeft()-1f, pianyi, view.getLeft(), layoutHeight - pianyi, dividerPaint);
			//width = view.getWidth();
		}
		// 绘制滑块
		canvas.drawRect(scrollOffset, layoutHeight - sliderSize, scrollOffset + sliderWidth, layoutHeight, sliderPaint);
		// 绘制底线
		canvas.drawRect(0, layoutHeight - bottomLineSize, layoutWidth, layoutHeight, bottomPaint);	
	}

	private void init(Context context, AttributeSet attrs) {
		TypedArray tArray = context.obtainStyledAttributes(attrs, R.styleable.TabButton);
		mButtonBackground = tArray.getResourceId(R.styleable.TabButton_buttonBackground, R.drawable.background_tabs);
		
		int bottomLineColor = tArray.getColor(R.styleable.TabButton_bottomLineColor, DEF_LINE_COLOR);
		int dividerColor 	= tArray.getColor(R.styleable.TabButton_dividerColor,    DEF_LINE_COLOR);
		int sliderColor 	= tArray.getColor(R.styleable.TabButton_sliderColor,     DEF_SLIDER_COLOR);
		
		float defBottonLineSize = TypedValue.applyDimension(
				TypedValue.COMPLEX_UNIT_DIP,
				DEF_BOTTON_LINE_SIZE,
				getResources().getDisplayMetrics());
		float defDividerSize	= TypedValue.applyDimension(
				TypedValue.COMPLEX_UNIT_DIP,
				DEF_DIVIDER_SIZE,
				getResources().getDisplayMetrics());
		float defSliderSize		= TypedValue.applyDimension(
				TypedValue.COMPLEX_UNIT_DIP,
				DEF_SLIDER_SIZE,
				getResources().getDisplayMetrics());
		float defTextSize		= TypedValue.applyDimension(
				TypedValue.COMPLEX_UNIT_SP,
				DEF_TEXT_SIZE,
				getResources().getDisplayMetrics());
		tabPadding		  = (int) TypedValue.applyDimension(
				TypedValue.COMPLEX_UNIT_DIP,
				DEF_TAB_PADDING,
				getResources().getDisplayMetrics());
		
		bottomLineSize 	= tArray.getDimension(R.styleable.TabButton_bottomLineSize, defBottonLineSize);
		dividerSize		= tArray.getDimension(R.styleable.TabButton_dividerSize, 	defDividerSize);
		sliderSize		= tArray.getDimension(R.styleable.TabButton_sliderSize, 	defSliderSize);
		
		mTextSize  = tArray.getDimension(R.styleable.TabButton_textSize, defTextSize);
		mTextColor = tArray.getColor(R.styleable.TabButton_textColor, DEF_TEXT_COLOR);
		tArray.recycle();
		
		if (isInEditMode()) {
			return;
		}
		
		mLinearLayout = new LinearLayout(getContext());
		//mLinearLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.FILL_PARENT));
		mLinearLayout.setOrientation(LinearLayout.HORIZONTAL);
		mLinearLayout.setGravity(Gravity.CENTER);
		addView(mLinearLayout);

		setWillNotDraw(false);
		setHorizontalScrollBarEnabled(false);

		sliderPaint = new Paint();
		sliderPaint.setAntiAlias(true);
		sliderPaint.setStyle(Style.FILL);
		sliderPaint.setColor(sliderColor);
		
		dividerPaint = new Paint();
		dividerPaint.setAntiAlias(true);
		dividerPaint.setStyle(Style.FILL);
		dividerPaint.setColor(dividerColor);

		bottomPaint = new Paint();
		bottomPaint.setAntiAlias(true);
		bottomPaint.setStyle(Style.FILL);
		bottomPaint.setColor(bottomLineColor);
		
	}
	
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);
		// 当布局确定大小的时候，初始化滑块的位置。
		drawUnderLine(pageSelect, 0);
	}

	public void setViewPager(ViewPager vp) {
		this.mViewPager = vp;
		vp.setOnPageChangeListener(getOnPageChangeListener());
		PagerAdapter pagerAdapter = vp.getAdapter();
		int count = pagerAdapter.getCount();
		for (int i = 0; i < count; i++) {
			addTab(newTextTab(pagerAdapter.getPageTitle(i)));
		}
	}

	@SuppressWarnings("deprecation")
	public View newTextTab(CharSequence text) {
		TextView tv = new TextView(getContext());
		LinearLayout.LayoutParams lParams = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.FILL_PARENT, 
				LinearLayout.LayoutParams.FILL_PARENT,
				1f);
		tv.setLayoutParams(lParams);
		tv.setGravity(Gravity.CENTER);
		tv.setPadding(tabPadding, 0, tabPadding, 0);
		tv.setBackgroundResource(mButtonBackground);
		tv.setOnClickListener(buttonOnClick);
		tv.setTextColor(mTextColor);
		tv.setTextSize(TypedValue.COMPLEX_UNIT_PX,mTextSize);
		tv.setText(text);
		return tv;
	}

	public void addTab(View view) {
		view.setId(tabSize++);
		mLinearLayout.addView(view);
	}

	public void setTab(String... strings) {
		for (String text : strings) {
			addTab(newTextTab(text));
		}
	}

	public OnPageChangeListener getOnPageChangeListener() {
		return new OnPageChangeListener() {

			public void onPageSelected(int arg0) {
				pageSelect = arg0;
			}

			public void onPageScrolled(int index, float arg1, int scroll) {
				if (scroll != 0)
					drawUnderLine(index, arg1);
			}

			public void onPageScrollStateChanged(int state) {
				if(state == ViewPager.SCROLL_STATE_IDLE)
					drawUnderLine(pageSelect, 0);
			}
		};
	}
	
	private void drawUnderLine(int index, float scroll){
		int itemWidth = mLinearLayout.getChildAt(index).getWidth();
		// 滑块的长度。ratio
		if (index < tabSize-1) {
			int add = mLinearLayout.getChildAt(index+1).getWidth()-itemWidth;
			sliderWidth = (int) (scroll * add + itemWidth  + 0.5f);
		}
		// 滑块已经滑动的距离。
		scrollOffset = mLinearLayout.getChildAt(index).getLeft() + scroll * itemWidth;
		// 控件宽度。
		screenWidth = getWidth();
		// 滑块中间点距离控件左边的距离。
		int half = (int) (scrollOffset+sliderWidth/2);
		// 当滑块中间点大于控件宽度一半或水平滚动量大于零时，让滑块保持在控件中间。
		if( half > (screenWidth/2)||computeHorizontalScrollOffset()!=0)
			scrollTo(half - screenWidth/2, 0);
		invalidate();
	}

	private OnClickListener buttonOnClick = new OnClickListener() {

		public void onClick(View v) {
			if (onClickListener != null) {
				onClickListener.tabsButtonOnClick(v.getId(), v);
			}else if (mViewPager!= null){
				mViewPager.setCurrentItem(v.getId());
			}
		}
	};
	
	/**
	 * 监听按钮条被点击的接口。
	 * 
	 */
	public interface TabsButtonOnClickListener {
		public void tabsButtonOnClick(int id, View v);
	}
	
	public void setTabsButtonOnClickListener(TabsButtonOnClickListener onClickListener) {
		this.onClickListener = onClickListener;
	}
	
	/**
	 * 保存滑块的当前位置。当横竖屏切换或Activity被系统杀死时调用。
	 */
	protected Parcelable onSaveInstanceState() {
		Parcelable parcelable = super.onSaveInstanceState();
		TabsSaveState tabsSaveState = new TabsSaveState(parcelable);
		tabsSaveState.select = pageSelect;
		return tabsSaveState;
	}
	
	/**
	 * 还原滑块的位置。
	 */
	protected void onRestoreInstanceState(Parcelable state) {
		TabsSaveState tabsSaveState = (TabsSaveState) state;
		super.onRestoreInstanceState(tabsSaveState.getSuperState());
		pageSelect = tabsSaveState.select;
		drawUnderLine(pageSelect, 0);
	}
	
	static class TabsSaveState extends BaseSavedState {
		private int select;

		public TabsSaveState(Parcelable arg0) {
			super(arg0);
		}

		public TabsSaveState(Parcel arg0) {
			super(arg0);
			select = arg0.readInt();
		}

		public int describeContents() {
			return super.describeContents();
		}

		public void writeToParcel(Parcel dest, int flags) {
			super.writeToParcel(dest, flags);
			dest.writeInt(select);
		}

		public static final Parcelable.Creator<TabButton.TabsSaveState> CREATOR = new Parcelable.Creator<TabButton.TabsSaveState>() {

			public TabsSaveState createFromParcel(Parcel source) {
				return new TabsSaveState(source);
			};

			public TabsSaveState[] newArray(int size) {
				return new TabsSaveState[size];
			}
		};
	}
}