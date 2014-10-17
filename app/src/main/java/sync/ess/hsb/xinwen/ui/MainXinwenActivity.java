/*
 * Copyright (C) 2013 Andreas Stuetz <andreas.stuetz@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package sync.ess.hsb.xinwen.ui;

import sync.ess.hsb.xinwen.fragmenthelper.ScrollTabHolder;
import sync.ess.hsb.xinwen.fragmenthelper.ScrollTabHolderFragment;
import sync.ess.hsb.xinwen.R;
import sync.ess.hsb.xinwen.helper.AlphaForegroundColorSpan;
import sync.ess.hsb.xinwen.helper.KenBurnsView;
import sync.ess.hsb.xinwen.ui.AwesomeCardFragment.NewsImageGetter;
import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.util.SparseArrayCompat;
import android.support.v4.view.ViewPager;
import android.text.Spannable;
import android.text.SpannableString;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.astuetz.PagerSlidingTabStrip;
import com.nineoldandroids.view.ViewHelper;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageSize;

public class MainXinwenActivity extends FragmentActivity implements
		ScrollTabHolder, ViewPager.OnPageChangeListener {

	private final Handler handler = new Handler();
	private static AccelerateDecelerateInterpolator sSmoothInterpolator = new AccelerateDecelerateInterpolator();
	private PagerSlidingTabStrip tabs;
	ViewPager pager;
	private MyPagerAdapter adapter;

	private Drawable oldBackground = null;
	private int currentColor = 0xFF666666;

	// AmazingActionBar

	private int mActionBarTitleColor;
	private int mActionBarHeight;
	private int mHeaderHeight;
	private int mMinHeaderTranslation;
	// private ListView mListView;
	private KenBurnsView mHeaderPicture;
	private ImageView mHeaderLogo;
	private View mHeader;
	private View mPlaceHolderView;
	private RectF mRect1 = new RectF();
	private RectF mRect2 = new RectF();

	private AlphaForegroundColorSpan mAlphaForegroundColorSpan;
	private SpannableString mSpannableString;

	private TypedValue mTypedValue = new TypedValue();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		tabs = (PagerSlidingTabStrip) findViewById(R.id.tabs);
		pager = (ViewPager) findViewById(R.id.pager);
		adapter = new MyPagerAdapter(getSupportFragmentManager());

		pager.setAdapter(adapter);

		final int pageMargin = (int) TypedValue.applyDimension(
				TypedValue.COMPLEX_UNIT_DIP, 4, getResources()
						.getDisplayMetrics());
		pager.setPageMargin(pageMargin);

		tabs.setViewPager(pager);

		// changeColor(currentColor);

		new AccelerateDecelerateInterpolator();
		mHeaderHeight = getResources().getDimensionPixelSize(
				R.dimen.header_height);
		mMinHeaderTranslation = -mHeaderHeight + getActionBarHeight()
				+ getActionBarHeight();

		// setContentView(R.layout.activity_noboringactionbar);

		// mListView = (ListView) findViewById(R.id.listview);
		mHeader = findViewById(R.id.header);
		mHeaderPicture = (KenBurnsView) findViewById(R.id.header_picture);
		// mHeaderPicture.setResourceIds(R.drawable.picture0,
		// R.drawable.picture1);
		mHeaderLogo = (ImageView) findViewById(R.id.header_logo);

		mActionBarTitleColor = getResources().getColor(
				R.color.actionbar_title_color);

		mSpannableString = new SpannableString(
				getString(R.string.noboringactionbar_title));
		mAlphaForegroundColorSpan = new AlphaForegroundColorSpan(
				mActionBarTitleColor);
		adapter.setTabHolderScrollingContent(this);
		tabs.setViewPager(pager);
		tabs.setOnPageChangeListener(this);
		mSpannableString = new SpannableString("Google News");
		// mAlphaForegroundColorSpan = new AlphaForegroundColorSpan(0xffffffff);
		tabs.setTextColor(Color.parseColor("#1aff87"));
		tabs.setTextSize(25);
		tabs.setIndicatorColor(Color.GREEN);
		ViewHelper.setAlpha(getActionBarIconView(), 0f);

		// setupListView();

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {

		case R.id.action_contact:
			QuickContactFragment dialog = new QuickContactFragment();
			dialog.show(getSupportFragmentManager(), "QuickContactFragment");
			return true;

		}

		return super.onOptionsItemSelected(item);
	}

	@SuppressLint("NewApi")
	private void changeColor(int newColor) {

		tabs.setIndicatorColor(newColor);

		// change ActionBar color just if an ActionBar is available
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {

			Drawable colorDrawable = new ColorDrawable(newColor);
			Drawable bottomDrawable = getResources().getDrawable(
					R.drawable.actionbar_bottom);
			LayerDrawable ld = new LayerDrawable(new Drawable[] {
					colorDrawable, bottomDrawable });

			// Bitmap bmp = BitmapFactory.decodeResource(getResources(),
			// R.drawable.screen_shot_1234);
			// Drawable d = new BitmapDrawable(getResources(), bmp);

			if (oldBackground == null) {

				if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
					ld.setCallback(drawableCallback);
				} else {
					// getActionBar().setBackgroundDrawable(ld);
					// getActionBar().setBackgroundDrawable(d);
				}

			}

			else {

				TransitionDrawable td = new TransitionDrawable(new Drawable[] {
						oldBackground, ld });

				// workaround for broken ActionBarContainer drawable handling on
				// pre-API 17 builds
				// https://github.com/android/platform_frameworks_base/commit/a7cc06d82e45918c37429a59b14545c6a57db4e4
				if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
					td.setCallback(drawableCallback);
				} else {
					getActionBar().setBackgroundDrawable(td);
				}

				td.startTransition(200);

			}

			oldBackground = ld;

			// http://stackoverflow.com/questions/11002691/actionbar-setbackgrounddrawable-nulling-background-from-thread-handler
			getActionBar().setDisplayShowTitleEnabled(false);
			getActionBar().setDisplayShowTitleEnabled(true);
			// / getActionBar().setBackgroundDrawable(d);
		}

		currentColor = newColor;

	}

	public void onColorClicked(View v) {

		int color = Color.parseColor(v.getTag().toString());
		changeColor(color);

	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt("currentColor", currentColor);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		currentColor = savedInstanceState.getInt("currentColor");
		changeColor(currentColor);
	}

	private Drawable.Callback drawableCallback = new Drawable.Callback() {
		@SuppressLint("NewApi")
		@Override
		public void invalidateDrawable(Drawable who) {
			getActionBar().setBackgroundDrawable(who);
		}

		@Override
		public void scheduleDrawable(Drawable who, Runnable what, long when) {
			handler.postAtTime(what, when);
		}

		@Override
		public void unscheduleDrawable(Drawable who, Runnable what) {
			handler.removeCallbacks(what);
		}
	};

	public class MyPagerAdapter extends FragmentPagerAdapter {

		private SparseArrayCompat<ScrollTabHolder> mScrollTabHolders;
		private final String[] TITLES = { "Top News", "India", "World",
				"China", "Sports", "Sci-tech", "Business", "Entertainment" };
		private ScrollTabHolder mListener;

		public MyPagerAdapter(FragmentManager fm) {
			super(fm);
			mScrollTabHolders = new SparseArrayCompat<ScrollTabHolder>();
		}

		public void setTabHolderScrollingContent(ScrollTabHolder listener) {
			mListener = listener;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			return TITLES[position];
		}

		@Override
		public int getCount() {
			return TITLES.length;
		}

		@Override
		public Fragment getItem(int position) {
			ScrollTabHolderFragment fragment = (ScrollTabHolderFragment) AwesomeCardFragment
					.newInstance(position);

			mScrollTabHolders.put(position, fragment);
			if (mListener != null) {
				fragment.setScrollTabHolder(mListener);
			}
			return fragment;
		}

		public SparseArrayCompat<ScrollTabHolder> getScrollTabHolders() {
			return mScrollTabHolders;
		}

	}

	private void setTitleAlpha(float alpha) {
		mAlphaForegroundColorSpan.setAlpha(alpha);
		mSpannableString.setSpan(mAlphaForegroundColorSpan, 0,
				mSpannableString.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		getActionBar().setTitle(mSpannableString);
	}

	public static float clamp(float value, float max, float min) {
		return Math.max(Math.min(value, min), max);
	}

	private void interpolate(View view1, View view2, float interpolation) {
		getOnScreenRect(mRect1, view1);
		getOnScreenRect(mRect2, view2);

		float scaleX = 1.0F + interpolation
				* (mRect2.width() / mRect1.width() - 1.0F);
		float scaleY = 1.0F + interpolation
				* (mRect2.height() / mRect1.height() - 1.0F);
		float translationX = 0.5F * (interpolation * (mRect2.left
				+ mRect2.right - mRect1.left - mRect1.right));
		float translationY = 0.5F * (interpolation * (mRect2.top
				+ mRect2.bottom - mRect1.top - mRect1.bottom));

		ViewHelper.setTranslationX(view1, translationX);
		ViewHelper.setTranslationY(view1,
				translationY - ViewHelper.getTranslationY(mHeader));
		ViewHelper.setScaleX(view1, scaleX);
		ViewHelper.setScaleY(view1, scaleY);
	}

	private RectF getOnScreenRect(RectF rect, View view) {

		Log.i("wtf", "rect f" + rect + " view" + view);
		rect.set(view.getLeft(), view.getTop(), view.getRight(),
				view.getBottom());
		return rect;
	}

	private void setupActionBar() {
		ActionBar actionBar = getActionBar();
		actionBar.setIcon(R.drawable.ic_transparent);

	}

	private ImageView getActionBarIconView() {
		return (ImageView) findViewById(android.R.id.home);
	}

	public int getActionBarHeight() {
		if (mActionBarHeight != 0) {
			return mActionBarHeight;
		}
		getTheme().resolveAttribute(android.R.attr.actionBarSize, mTypedValue,
				true);
		mActionBarHeight = TypedValue.complexToDimensionPixelSize(
				mTypedValue.data, getResources().getDisplayMetrics());
		return mActionBarHeight;
	}

	public void changeViewPagerHeight(final int scrollY) {
		ViewTreeObserver viewTreeObserver = pager.getViewTreeObserver();
		viewTreeObserver
				.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {

					@Override
					public void onGlobalLayout() {

						LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
								LinearLayout.LayoutParams.WRAP_CONTENT,
								LinearLayout.LayoutParams.WRAP_CONTENT);

						int viewPagerWidth = pager.getWidth();
						float viewPagerHeight = (float) (viewPagerWidth * scrollY);

						layoutParams.width = viewPagerWidth;
						layoutParams.height = (int) viewPagerHeight;

						pager.setLayoutParams(layoutParams);
						pager.getViewTreeObserver()
								.removeGlobalOnLayoutListener(this);
					}
				});
	}

	public void interpolate1(View view2, float interpolation) {
		if (pager == null) {
			Log.i("hemant", "pager is null");
			pager = (ViewPager) this.findViewById(R.id.pager);
		}
		getOnScreenRect(mRect1, pager);
		getOnScreenRect(mRect2, view2);

		float scaleY = 1.0F + interpolation
				* (mRect2.height() / mRect1.height() + 1.0F);

		float translationY = 0.5F * (interpolation * (mRect2.top
				+ mRect2.bottom - mRect1.top - mRect1.bottom));

		// /view1.setTranslationX(translationX);
		pager.setTranslationY(translationY - mHeader.getTranslationY());
		// view1.setScaleX(scaleX);
		pager.setScaleY(scaleY);
	}

	@Override
	public void onPageScrollStateChanged(int arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onPageSelected(int position) {
		// TODO Auto-generated method stub
		SparseArrayCompat<ScrollTabHolder> scrollTabHolders = adapter
				.getScrollTabHolders();
		ScrollTabHolder currentHolder = scrollTabHolders.valueAt(position);

		currentHolder.adjustScroll((int) (mHeader.getHeight() + ViewHelper
				.getTranslationY(mHeader)));
	}

	@Override
	public void adjustScroll(int scrollHeight) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount, int pagePosition) {
		// TODO Auto-generated method stub
		if (pager.getCurrentItem() == pagePosition) {
			int scrollY = getScrollY(view);
			ViewHelper.setTranslationY(mHeader,
					Math.max(-scrollY, mMinHeaderTranslation));
			float ratio = clamp(ViewHelper.getTranslationY(mHeader)
					/ mMinHeaderTranslation, 0.0f, 1.0f);
			interpolate(mHeaderLogo, getActionBarIconView(),
					sSmoothInterpolator.getInterpolation(ratio));
			setTitleAlpha(clamp(5.0F * ratio - 4.0F, 0.0F, 1.0F));
		}

	}

	public int getScrollY(AbsListView view) {
		View c = view.getChildAt(0);
		if (c == null) {
			return 0;
		}

		int firstVisiblePosition = view.getFirstVisiblePosition();
		int top = c.getTop();

		int headerHeight = 0;
		if (firstVisiblePosition >= 1) {
			headerHeight = mHeaderHeight;
		}

		return -top + firstVisiblePosition * c.getHeight() + headerHeight;
	}

	
}