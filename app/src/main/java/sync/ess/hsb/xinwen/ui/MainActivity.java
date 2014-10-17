package sync.ess.hsb.xinwen.ui;

/**
 * @author hemant
 */
import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.util.SparseArrayCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.text.Spannable;
import android.text.SpannableString;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.AbsListView;
import android.widget.ImageView;

import com.astuetz.PagerSlidingTabStrip;
import com.nineoldandroids.view.ViewHelper;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import sync.ess.hsb.xinwen.R;
import sync.ess.hsb.xinwen.fragmenthelper.ScrollTabHolder;
import sync.ess.hsb.xinwen.fragmenthelper.ScrollTabHolderFragment;
import sync.ess.hsb.xinwen.helper.AlphaForegroundColorSpan;
import sync.ess.hsb.xinwen.helper.KenBurnsView;
import sync.ess.hsb.xinwen.ui.AwesomeCardFragment.NewsImageGetter;
import sync.ess.hsb.xinwen.util.Utils;

public class MainActivity extends ActionBarActivity implements ScrollTabHolder,
		ViewPager.OnPageChangeListener, NewsImageGetter, ImageLoadingListener {

	private static AccelerateDecelerateInterpolator sSmoothInterpolator = new AccelerateDecelerateInterpolator();

	// private View mHeaderPicture;
	private View mHeader;

	private PagerSlidingTabStrip mPagerSlidingTabStrip;
	private ViewPager mViewPager;
	private PagerAdapter mPagerAdapter;

	private int mActionBarHeight;
	private int mMinHeaderHeight;
	private int mHeaderHeight;
	private int mMinHeaderTranslation;
	private ImageView mHeaderLogo;

	private RectF mRect1 = new RectF();
	private RectF mRect2 = new RectF();

	private TypedValue mTypedValue = new TypedValue();
	private SpannableString mSpannableString;
	private AlphaForegroundColorSpan mAlphaForegroundColorSpan;

	// kensburn
	private KenBurnsView mHeaderPicture;

	String[] mCategory = { Utils.CAT_TOP_NEWS, Utils.CAT_WORLD,
			Utils.CAT_INDIA, Utils.CAT_CHINA, Utils.CAT_BUSINESS,
			Utils.CAT_SPORTS, Utils.CAT_SCI_TECH, Utils.CAT_HEALTH };

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mMinHeaderHeight = getResources().getDimensionPixelSize(
				R.dimen.min_header_height);
		mHeaderHeight = getResources().getDimensionPixelSize(
				R.dimen.header_height);
		mMinHeaderTranslation = -mMinHeaderHeight + 2 * getActionBarHeight();

		setContentView(R.layout.activity_main);

		// mHeaderPicture = (View) findViewById(R.id.header_picture);
		mHeaderPicture = (KenBurnsView) findViewById(R.id.header_picture);
		//mHeaderPicture.setResourceIds(R.drawable.picture0, R.drawable.picture1);
		// mHeaderPicture.setResourceIds(R.drawable.pic0, R.drawable.pic1);
		mHeaderLogo = (ImageView) findViewById(R.id.header_logo);
		mHeader = findViewById(R.id.header);

		mPagerSlidingTabStrip = (PagerSlidingTabStrip) findViewById(R.id.tabs);
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setOffscreenPageLimit(4);

		mPagerAdapter = new PagerAdapter(getSupportFragmentManager());
		mPagerAdapter.setTabHolderScrollingContent(this);

		mViewPager.setAdapter(mPagerAdapter);
		// setting animation
		// mViewPager.setPageTransformer(true, new ZoomOutPageTransformer());

		mPagerSlidingTabStrip.setViewPager(mViewPager);
		mPagerSlidingTabStrip.setOnPageChangeListener(this);
		mSpannableString = new SpannableString("Xinwen");
		mAlphaForegroundColorSpan = new AlphaForegroundColorSpan(0xffffffff);

		ViewHelper.setAlpha(getActionBarIconView(), 0f);

		getSupportActionBar().setBackgroundDrawable(null);
		getSupportActionBar().setTitle("Xinwen");
		mPagerSlidingTabStrip.setTextColor(Color.parseColor("#1aff87"));
		// mPagerSlidingTabStrip.setTextSize();
		mPagerSlidingTabStrip.setIndicatorColor(Color.GREEN);
	}

	@Override
	public void onPageScrollStateChanged(int arg0) {
		// nothing
	}

	@Override
	public void onPageScrolled(int position, float positionOffset,
			int positionOffsetPixels) {
		// nothing
	}

	@Override
	public void onPageSelected(int position) {
		SparseArrayCompat<ScrollTabHolder> scrollTabHolders = mPagerAdapter
				.getScrollTabHolders();
		ScrollTabHolder currentHolder = scrollTabHolders.valueAt(position);

		currentHolder.adjustScroll((int) (mHeader.getHeight() + ViewHelper
				.getTranslationY(mHeader)));
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount, int pagePosition) {
		if (mViewPager.getCurrentItem() == pagePosition) {
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

	@Override
	public void adjustScroll(int scrollHeight) {
		// nothing
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
		rect.set(view.getLeft(), view.getTop(), view.getRight(),
				view.getBottom());
		return rect;
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public int getActionBarHeight() {
		if (mActionBarHeight != 0) {
			return mActionBarHeight;
		}

		if (Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB) {
			getTheme().resolveAttribute(android.R.attr.actionBarSize,
					mTypedValue, true);
		} else {
			getTheme()
					.resolveAttribute(R.attr.actionBarSize, mTypedValue, true);
		}

		mActionBarHeight = TypedValue.complexToDimensionPixelSize(
				mTypedValue.data, getResources().getDisplayMetrics());

		return mActionBarHeight;
	}

	private void setTitleAlpha(float alpha) {
		mAlphaForegroundColorSpan.setAlpha(alpha);
		mSpannableString.setSpan(mAlphaForegroundColorSpan, 0,
				mSpannableString.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		getSupportActionBar().setTitle(mSpannableString);
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private ImageView getActionBarIconView() {

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			return (ImageView) findViewById(android.R.id.home);
		}

		return (ImageView) findViewById(android.support.v7.appcompat.R.id.home);
	}

	public class PagerAdapter extends FragmentPagerAdapter {

		private SparseArrayCompat<ScrollTabHolder> mScrollTabHolders;
		// private final String[] TITLES = { "Top News", "India", "World",
		// "China", "Sports", "Sci-tech", "Business", "Entertainment" };
		private ScrollTabHolder mListener;

		public PagerAdapter(FragmentManager fm) {
			super(fm);
			mScrollTabHolders = new SparseArrayCompat<ScrollTabHolder>();
		}

		public void setTabHolderScrollingContent(ScrollTabHolder listener) {
			mListener = listener;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			return mCategory[position];
		}

		@Override
		public int getCount() {
			return mCategory.length;
		}

		@Override
		public Fragment getItem(int position) {
			ScrollTabHolderFragment fragment = (ScrollTabHolderFragment) AwesomeCardFragment
					.newInstance(position);
			 Bundle bundle = new Bundle();
			 bundle.putString("cat", mCategory[position]);
			 bundle.putInt("position", position);
			 fragment.setArguments(bundle);
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

	public class ZoomOutPageTransformer implements ViewPager.PageTransformer {
		private static final float MIN_SCALE = 0.85f;
		private static final float MIN_ALPHA = 0.5f;

		public void transformPage(View view, float position) {
			int pageWidth = view.getWidth();
			int pageHeight = view.getHeight();

			if (position < -1) { // [-Infinity,-1)
				// This page is way off-screen to the left.
				view.setAlpha(0);

			} else if (position <= 1) { // [-1,1]
				// Modify the default slide transition to shrink the page as
				// well
				float scaleFactor = Math.max(MIN_SCALE, 1 - Math.abs(position));
				float vertMargin = pageHeight * (1 - scaleFactor) / 2;
				float horzMargin = pageWidth * (1 - scaleFactor) / 2;
				if (position < 0) {
					view.setTranslationX(horzMargin - vertMargin / 2);
				} else {
					view.setTranslationX(-horzMargin + vertMargin / 2);
				}

				// Scale the page down (between MIN_SCALE and 1)
				view.setScaleX(scaleFactor);
				view.setScaleY(scaleFactor);

				// Fade the page relative to its size.
				view.setAlpha(MIN_ALPHA + (scaleFactor - MIN_SCALE)
						/ (1 - MIN_SCALE) * (1 - MIN_ALPHA));

			} else { // (1,+Infinity]
				// This page is way off-screen to the right.
				view.setAlpha(0);
			}
		}
	}

	public class DepthPageTransformer implements ViewPager.PageTransformer {
		private static final float MIN_SCALE = 0.75f;

		public void transformPage(View view, float position) {
			int pageWidth = view.getWidth();

			if (position < -1) { // [-Infinity,-1)
				// This page is way off-screen to the left.
				view.setAlpha(0);

			} else if (position <= 0) { // [-1,0]
				// Use the default slide transition when moving to the left page
				view.setAlpha(1);
				view.setTranslationX(0);
				view.setScaleX(1);
				view.setScaleY(1);

			} else if (position <= 1) { // (0,1]
				// Fade the page out.
				view.setAlpha(1 - position);

				// Counteract the default slide transition
				view.setTranslationX(pageWidth * -position);

				// Scale the page down (between MIN_SCALE and 1)
				float scaleFactor = MIN_SCALE + (1 - MIN_SCALE)
						* (1 - Math.abs(position));
				view.setScaleX(scaleFactor);
				view.setScaleY(scaleFactor);

			} else { // (1,+Infinity]
				// This page is way off-screen to the right.
				view.setAlpha(0);
			}
		}
	}

	@Override
	public void OnGettingImage(String url) {
		// TODO Auto-generated method stub
		// DisplayImageOptions options;
		// options = new DisplayImageOptions.Builder()
		// .showImageOnLoading(R.drawable.ic_launcher)
		// .showImageForEmptyUri(R.drawable.ic_launcher)
		// .showImageOnFail(R.drawable.ic_launcher).cacheInMemory(true)
		// .cacheOnDisk(true).considerExifParams(true)
		// .bitmapConfig(Bitmap.Config.RGB_565).build();
		Log.i("MainActivty", "Url" + url);
		//http://t3.gstatic.com/images?q=tbn:ANd9GcSdoIqV_mJVQM81zCmEZWd_DLTCElGqMsdkH2_db-1Wa6R3VBHeS4PAZsRIOfdfmVjOjw2nrTGF
		ImageLoader.getInstance().loadImage(
				"http:"+url,
				new ImageSize(500, 300), this);

		// ImageLoader.getInstance().displayImage(url, imageView, options);
	}

	@Override
	public void onLoadingCancelled(String arg0, View arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onLoadingComplete(String arg0, View arg1, Bitmap arg2) {
		// TODO Auto-generated method stub
		Drawable d = new BitmapDrawable(getResources(), arg2);
		// BitmapDrawable ob = new BitmapDrawable(bmp);
		mHeaderPicture.setBackgroundDrawable(d);
	}

	@Override
	public void onLoadingFailed(String arg0, View arg1, FailReason arg2) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onLoadingStarted(String arg0, View arg1) {
		// TODO Auto-generated method stub

	}
}
