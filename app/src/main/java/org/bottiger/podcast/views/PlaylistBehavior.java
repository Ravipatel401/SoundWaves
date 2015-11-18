package org.bottiger.podcast.views;

import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ScrollerCompat;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;

import org.bottiger.podcast.R;

/**
 * Created by aplb on 08-09-2015.
 */
public class PlaylistBehavior extends CoordinatorLayout.Behavior<View> {

    private final String TAG = "PlaylistBehavior";

    private TopPlayer mTopPlayer;
    private RecyclerView mRecyclerView;

    private int mRecyclerViewBottomPadding;

    private static final int MAX_VELOCITY_Y = 5000;

    private static final int DOWN = -1;
    private static final int UP = 1;

    // From AppBarLayout
    private Runnable mFlingRunnable;
    private ScrollerCompat mScroller;

    public PlaylistBehavior(Context context, AttributeSet attrs) {
        Log.v(TAG, "Created");
    }

    @Override
    public boolean onInterceptTouchEvent(CoordinatorLayout parent, View child, MotionEvent ev) {
        return false;
    }

    @Override
    public boolean onLayoutChild(CoordinatorLayout parent, View child, int layoutDirection) {

        if (mTopPlayer == null) {
            mTopPlayer = (TopPlayer) parent.findViewById(R.id.top_player);
        }

        if (mRecyclerView == null) {
            mRecyclerView = (RecyclerView) parent.findViewById(R.id.my_recycler_view);
            mRecyclerViewBottomPadding = mRecyclerView.getPaddingBottom();
        }

        int height = (int)mTopPlayer.getPlayerHeight(); //mTopPlayer.getLayoutParams().height;
        mRecyclerView.setTranslationY(height);

        Log.v(TAG, "onLayoutChild, child: " + child.getClass().getName() + " layoutDirection: " + layoutDirection + " height: " + height);
        return false;
    }

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, View child, View dependency) {
        Log.v(TAG, "layoutDependsOn, child: " + child.getClass().getName() + " dependency: " + dependency.getClass().getName());
        boolean corectChild = (child.getId() == R.id.my_recycler_view);
        boolean correctDependency = (dependency.getId() == R.id.top_player);
        return corectChild && correctDependency;
    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, View child, View dependency) {
        Log.v(TAG, "onDependentViewChanged, child: " + child.getClass().getName() + " dependency: " + dependency.getClass().getName());
        int height = mTopPlayer.getLayoutParams().height;
        mRecyclerView.setTranslationY(height);

        int left = mRecyclerView.getPaddingLeft();
        int right = mRecyclerView.getPaddingRight();
        int top = mRecyclerView.getPaddingTop();

        mRecyclerView.setPadding(left, top, right, mRecyclerViewBottomPadding+height);
        return true;
    }

    @Override
    public boolean onStartNestedScroll(CoordinatorLayout coordinatorLayout, View child, View directTargetChild, View target, int nestedScrollAxes) {
        Log.v(TAG, "onStartNestedScroll, child: " + child.getClass().getName() + " target: " + target.getClass().getName());

        if (mScroller != null && !mScroller.isFinished()) {
            mScroller.abortAnimation();
        }

        mRecyclerView.canScrollVertically(-1);
        return true;
    }

    @Override
    public void	onNestedPreScroll(CoordinatorLayout coordinatorLayout, View child, View target, int dx, int dy, int[] consumed) {
        Log.v(TAG, "onNestedPreScroll, child: " + child.getClass().getName() + " target: " + target.getClass().getName());

        /*
        if (target instanceof TopPlayer) {

        }
        */
        if (target instanceof FixedRecyclerView && dy > 0) {

                if (!mTopPlayer.isMinimumSize()) {
                    // scroll up (to smaller topplayer)
                    //float oldHeight = mTopPlayer.getPlayerHeight();
                    //float newHeight = mTopPlayer.setPlayerHeight(oldHeight - dy);
                    float newHeight = mTopPlayer.scrollBy(dy);
                    consumed[1] = (int) dy;//(oldHeight - newHeight);
                }
        }

    }

    @Override
    public void onNestedScroll(CoordinatorLayout coordinatorLayout, View child, View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
        Log.v(TAG, "onNestedScroll, child: " + child.getClass().getName() + " target: " + target.getClass().getName() + " dyC: " + dyConsumed + " dyUC: " + dyUnconsumed);

        // if we are pulling down, but do not consume all the pulls
        if (dyUnconsumed < 0) {
            int diff = 0;
            float oldHeight = 0;
            float newHeight = 0;

            //if (!mTopPlayer.isMinimumSize()) {
                //oldHeight = mTopPlayer.getPlayerHeight();
                //newHeight = mTopPlayer.setPlayerHeight(oldHeight - dyUnconsumed);
            //}

            newHeight = mTopPlayer.scrollBy(dyUnconsumed);

            diff = (int)(oldHeight-newHeight);
            if (diff == 0)
                return;

            //dyUnconsumed -= diff;

            //mRecyclerView.scrollBy(0, dyUnconsumed);
        }

        if (dyUnconsumed > 0) {
            if (!mTopPlayer.isMinimumSize()) {
                mTopPlayer.scrollBy(dyUnconsumed);
            }
        }

    }

    /**
     * Interpolator from android.support.v4.view.ViewPager. Snappier and more elastic feeling
     * than the default interpolator.
     */
    private static final Interpolator sInterpolator = new Interpolator() {

        /**
         * {@inheritDoc}
         */
        @Override
        public float getInterpolation(float t) {
            t -= 1.0f;
            return t * t * t * t * t + 1.0f;
        }
    };

    private VelocityTracker mVelocityTracker;
    @Override
    public boolean onNestedPreFling(CoordinatorLayout coordinatorLayout, View child, View target, float velocityX, float velocityY) {
        Log.v(TAG, "onNestedPreFling, child: " + child.getClass().getName() + " target: " + target.getClass().getName() + "vy: " + velocityY);

        //if (mVelocityTracker == null) {
        //    mVelocityTracker = VelocityTracker.obtain();
        //}
        int ymin = 0;
        int ymax = 100000;
        fling(coordinatorLayout, child, ymin, ymax, velocityY);
        return true;
    }

    @Override
    public boolean onNestedFling (CoordinatorLayout coordinatorLayout, View child, View target, float velocityX, float velocityY, boolean consumed) {
        Log.v(TAG, "onNestedFling, child: " + child.getClass().getName() + " target: " + target.getClass().getName() + "vy: " + velocityY + " consumed:" + consumed);
        int xmin = 0;
        int xmax = 0;
        int ymin = 0;
        int ymax = 100000;
        //mScroller.fling(0,0,(int)velocityX,(int)velocityY,xmin,xmax,ymin,ymax);
        fling(coordinatorLayout, child, ymin, ymax, velocityY);
        return true;
    }

    private boolean fling(CoordinatorLayout coordinatorLayout, View layout, int minOffset, int maxOffset, float velocityY) {
        if(this.mFlingRunnable != null) {
            layout.removeCallbacks(this.mFlingRunnable);
        }

        if(this.mScroller == null) {
            this.mScroller = ScrollerCompat.create(layout.getContext());
        }

        this.mScroller.fling(0, MAX_VELOCITY_Y, 0, Math.round(velocityY), 0, 0, minOffset, maxOffset);
        if(this.mScroller.computeScrollOffset()) {
            this.mFlingRunnable = new PlaylistBehavior.FlingRunnable(coordinatorLayout, layout);
            ViewCompat.postOnAnimation(layout, this.mFlingRunnable);
            return true;
        } else {
            this.mFlingRunnable = null;
            return false;
        }
    }

    class FlingRunnable implements Runnable {
        private final CoordinatorLayout mParent;
        private final View mLayout;

        private int lastY = 0;

        FlingRunnable(CoordinatorLayout parent, View layout) {
            this.mParent = parent;
            this.mLayout = layout;
        }

        public void run() {
            if(this.mLayout != null && PlaylistBehavior.this.mScroller != null && PlaylistBehavior.this.mScroller.computeScrollOffset()) {

                int currY = PlaylistBehavior.this.mScroller.getCurrY();

                if (lastY != 0) {
                    int diffY = currY-lastY;

                    Log.v(TAG, "scroll.run, diffY: " + diffY);

                    // diffY = Negative => larger topplayer
                    if (diffY < 0) {
                        //if (mTopPlayer.isMaximumSize()) {
                        if (mRecyclerView.canScrollVertically(DOWN)) {
                            // it cannot be larger, scroll recyclerview
                            Log.v(TAG, "scroll.run, mRecyclerView1");
                            PlaylistBehavior.this.mRecyclerView.scrollBy(0, diffY);
                        } else {
                            Log.v(TAG, "scroll.run, mTopPlayer2");
                            PlaylistBehavior.this.mTopPlayer.scrollBy(diffY);
                        }
                    } else {
                        if (mTopPlayer.isMinimumSize()) {
                            Log.v(TAG, "scroll.run, mRecyclerView2");
                            PlaylistBehavior.this.mRecyclerView.scrollBy(0, diffY);
                        } else {
                            Log.v(TAG, "scroll.run, mTopPlayer1");
                            PlaylistBehavior.this.mTopPlayer.scrollBy(diffY);
                        }
                    }
                }

                lastY = currY;
                ViewCompat.postOnAnimation(this.mLayout, this);
            }

        }
    }

}
