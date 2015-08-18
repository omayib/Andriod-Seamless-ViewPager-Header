package id.technomotion.stickyheaderviewpager.wrapper;

import android.content.Context;
import android.os.SystemClock;
import android.support.v4.util.SparseArrayCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;

import id.technomotion.stickyheaderviewpager.R;
import id.technomotion.stickyheaderviewpager.tools.ScrollableFragmentListener;
import id.technomotion.stickyheaderviewpager.tools.ScrollableListener;
import id.technomotion.stickyheaderviewpager.tools.ViewPagerHeaderHelper;
import id.technomotion.stickyheaderviewpager.widget.TouchCallbackLayout;

/**
 * Created by omayib on 8/18/15.
 */
public class StickyTabHeader implements ViewPagerHeaderHelper.OnViewPagerTouchListener, TouchCallbackLayout.TouchEventListener,ScrollableFragmentListener {
    private static final long  DEFAULT_DURATION = 300L;
    private static final float DEFAULT_DAMPING  = 1.5f;
    private static final String TAG = "StickyTabHeader";

    private SparseArrayCompat<ScrollableListener> mScrollableListenerArrays =
            new SparseArrayCompat<>();
    private ViewPager mViewPager;
    private View mHeaderLayoutView;
    private ViewPagerHeaderHelper mViewPagerHeaderHelper;

    private int mTouchSlop;
    private int mTabHeight;
    private int mHeaderHeight;

    private Interpolator mInterpolator = new DecelerateInterpolator();

    private Context context;

    public StickyTabHeader(Context context,View mHeaderLayoutView,ViewPager viewPager,TouchCallbackLayout touchCallbackLayout) {
        this.context = context;
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        mTabHeight = context.getResources().getDimensionPixelSize(R.dimen.tabs_height);
        mHeaderHeight = context.getResources().getDimensionPixelSize(R.dimen.viewpager_header_height);

        mViewPagerHeaderHelper = new ViewPagerHeaderHelper(context, this);
        touchCallbackLayout.setTouchEventListener(this);

        this.mViewPager=viewPager;
        this.mHeaderLayoutView=mHeaderLayoutView;
        ViewCompat.setTranslationY(mViewPager, mHeaderHeight);
    }

    public void setTabHeight(int tabHeight){
        mTabHeight=tabHeight;
    }
    public void setHeaderHeight(int headerHeight){
        this.mHeaderHeight=headerHeight;
    }

    @Override
    public boolean isViewBeingDragged(MotionEvent event) {
        return mScrollableListenerArrays.valueAt(mViewPager.getCurrentItem())
                .isViewBeingDragged(event);
    }

    @Override
    public void onMoveStarted(float eventY) {

    }

    @Override
    public void onMove(float y, float yDx) {
        float headerTranslationY = ViewCompat.getTranslationY(mHeaderLayoutView) + yDx;
        if (headerTranslationY >= 0) { // pull end
            headerExpand(0L);

            //Log.d("kaede", "pull end");
            if(countPullEnd>=1){
                if (countPullEnd==1){
                    downtime= SystemClock.uptimeMillis();
                    simulateTouchEvent(mViewPager,downtime, SystemClock.uptimeMillis(), MotionEvent.ACTION_DOWN, 250, y+mHeaderHeight);
                }
                simulateTouchEvent(mViewPager,downtime, SystemClock.uptimeMillis(), MotionEvent.ACTION_MOVE, 250, y+mHeaderHeight);
            }
            countPullEnd++;

        } else if (headerTranslationY <= -mHeaderHeight) { // push end
            headerFold(0L);

            //Log.d("kaede", "push end");
            //Log.d("kaede", "kaede onMove y="+y+",yDx="+yDx+",headerTranslationY="+headerTranslationY);
            if(countPushEnd>=1){
                if (countPushEnd==1){
                    downtime=SystemClock.uptimeMillis();
                    simulateTouchEvent(mViewPager,downtime, SystemClock.uptimeMillis(), MotionEvent.ACTION_DOWN, 250, y+mHeaderHeight);
                }
                simulateTouchEvent(mViewPager,downtime, SystemClock.uptimeMillis(), MotionEvent.ACTION_MOVE, 250, y+mHeaderHeight);
            }
            countPushEnd++;

        } else {

            //Log.d("kaede", "ing");
        	/*if(!isHasDispatchDown3){
        	simulateTouchEvent(mViewPager,SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_CANCEL, 250, y+mHeaderHeight);
        	isHasDispatchDown3=true;
        	}*/

            ViewCompat.animate(mHeaderLayoutView)
                    .translationY(headerTranslationY)
                    .setDuration(0)
                    .start();
            ViewCompat.animate(mViewPager)
                    .translationY(headerTranslationY + mHeaderHeight)
                    .setDuration(0)
                    .start();
        }
    }
    long downtime=-1;
    int countPushEnd=0,countPullEnd=0;
    private void simulateTouchEvent(View dispatcher, long downTime, long eventTime, int action, float x, float y) {
        MotionEvent motionEvent = MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), action, x, y, 0);
        try {
            dispatcher.dispatchTouchEvent(motionEvent);
        } catch (Throwable e) {
            Log.e(TAG, "simulateTouchEvent error: " + e.toString());
        } finally {
            motionEvent.recycle();
        }
    }
    @Override
    public void onMoveEnded(boolean isFling, float flingVelocityY) {
//Log.d("kaede", "move end");
        countPushEnd = countPullEnd=0;

        float headerY = ViewCompat.getTranslationY(mHeaderLayoutView); // 0到负数
        if (headerY == 0 || headerY == -mHeaderHeight) {
            return;
        }

        if (mViewPagerHeaderHelper.getInitialMotionY() - mViewPagerHeaderHelper.getLastMotionY()
                < -mTouchSlop) {  // pull > mTouchSlop = expand
            headerExpand(headerMoveDuration(true, headerY, isFling, flingVelocityY));
        } else if (mViewPagerHeaderHelper.getInitialMotionY()
                - mViewPagerHeaderHelper.getLastMotionY()
                > mTouchSlop) { // push > mTouchSlop = fold
            headerFold(headerMoveDuration(false, headerY, isFling, flingVelocityY));
        } else {
            if (headerY > -mHeaderHeight / 2f) {  // headerY > header/2 = expand
                headerExpand(headerMoveDuration(true, headerY, isFling, flingVelocityY));
            } else { // headerY < header/2= fold
                headerFold(headerMoveDuration(false, headerY, isFling, flingVelocityY));
            }
        }
    }
    private long headerMoveDuration(boolean isExpand, float currentHeaderY, boolean isFling,
                                    float velocityY) {

        long defaultDuration = DEFAULT_DURATION;

        if (isFling) {

            float distance = isExpand ? Math.abs(mHeaderHeight) - Math.abs(currentHeaderY)
                    : Math.abs(currentHeaderY);
            velocityY = Math.abs(velocityY) / 1000;

            defaultDuration = (long) (distance / velocityY * DEFAULT_DAMPING);

            defaultDuration =
                    defaultDuration > DEFAULT_DURATION ? DEFAULT_DURATION : defaultDuration;
        }

        return defaultDuration;
    }
    @Override
    public boolean onLayoutInterceptTouchEvent(MotionEvent event) {

        return mViewPagerHeaderHelper.onLayoutInterceptTouchEvent(event,
                mTabHeight + mHeaderHeight);
    }

    @Override
    public boolean onLayoutTouchEvent(MotionEvent event) {
        return mViewPagerHeaderHelper.onLayoutTouchEvent(event);
    }

    private void headerFold(long duration) {
        ViewCompat.animate(mHeaderLayoutView)
                .translationY(-mHeaderHeight)
                .setDuration(duration)
                .setInterpolator(mInterpolator)
                .start();

        ViewCompat.animate(mViewPager).translationY(0).
                setDuration(duration).setInterpolator(mInterpolator).start();

        mViewPagerHeaderHelper.setHeaderExpand(false);
    }

    private void headerExpand(long duration) {
        ViewCompat.animate(mHeaderLayoutView)
                .translationY(0)
                .setDuration(duration)
                .setInterpolator(mInterpolator)
                .start();

        ViewCompat.animate(mViewPager)
                .translationY(mHeaderHeight)
                .setDuration(duration)
                .setInterpolator(mInterpolator)
                .start();
        mViewPagerHeaderHelper.setHeaderExpand(true);
    }

    @Override
    public void onFragmentAttached(ScrollableListener listener, int position) {
        System.out.println("onFragmentAttached");
        mScrollableListenerArrays.put(position, listener);
    }

    @Override
    public void onFragmentDetached(ScrollableListener fragment, int position) {
        System.out.println("onFragmentDetached");
        mScrollableListenerArrays.remove(position);
    }
}
