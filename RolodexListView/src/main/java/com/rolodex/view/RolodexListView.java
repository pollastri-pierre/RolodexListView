package com.rolodex.view;

import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.VelocityTrackerCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ScrollerCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.Toast;

/**
 * Created by Pollastri Pierre on 08/12/2013.
 */
public class RolodexListView extends AdapterView<ListAdapter> {

    private ListAdapter mAdapter;

    // Constants
    private static final int TOUCH_MODE_IDLE = 0;
    private static final int TOUCH_MODE_DRAGGING = 1;
    private static final int TOUCH_MODE_FLINGING = 2;
    private static final int TOUCH_MODE_DOWN = 3;
    private static final int TOUCH_MODE_TAP = 4;

    private static final int OFFSET_SCROLLING = 0;
    private static final int OFFSET_HIT_BOTTOM = 1;
    private static final int OFFSET_HIT_TOP = 2;

    // Drawing object
    private final Camera mCamera = new Camera();
    private final Matrix mMatrix = new Matrix();

    // Properties
    private int mItemPerPage = 4;

    // View position and offsets
    private int mFirstPosition;
    private int mLastPosition;
    private int mTopOffset;
    private int mMaxTopOffset;

    private int mDistanceBetweenPage = 300;

    // Computation Caches
    private int mItemHeight;

    // Touch
    private float mLastTouchY;
    private float mLastTouchX;
    private float mTouchRemainderY;
    private int mActivePointerId;

    private int mTouchMode = TOUCH_MODE_IDLE;

    private int mTouchSlop;
    private int mMaximumFlingVelocity;
    private int mMinimumFlingVelocity;
    private ScrollerCompat mScroller;
    private VelocityTracker mVelocityTracker;


    public RolodexListView(Context context) {
        this(context, null);
    }

    public RolodexListView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RolodexListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        mScroller = ScrollerCompat.create(context);
        final ViewConfiguration configuration = ViewConfiguration
                .get(getContext());
        mTouchSlop = configuration.getScaledTouchSlop();
        mMaximumFlingVelocity = configuration.getScaledMaximumFlingVelocity();
        mMinimumFlingVelocity = configuration.getScaledMinimumFlingVelocity();
        mTopOffset = getTopPaddingOffset();
    }

    @Override
    public ListAdapter getAdapter() {
        return mAdapter;
    }

    @Override
    public void setAdapter(ListAdapter listAdapter) {
        if (mAdapter != null) {
            mAdapter.unregisterDataSetObserver(mAdapterDataSetObserver);
        }
        mAdapter = listAdapter;

        mAdapter.registerDataSetObserver(mAdapterDataSetObserver);
        reloadData();
    }

    @Override
    public View getSelectedView() {
        return null;
    }

    @Override
    public void setSelection(int i) {

    }

    // Layouting

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        if (mAdapter == null) {
            return ;
        }

        mItemHeight = (bottom - top) / mItemPerPage;

        layout();
    }

    protected void layout() {
        if (getWidth() == 0 || getHeight() == 0 || mAdapter == null
                || mAdapter.getCount() == 0) {
            removeAllViewsInLayout();
            return;
        }
        fillList();
        layoutChildren();
    }

    protected void fillList() {
        fillListDown();

    }

    protected void fillListDown() {
        // This is only for tests
        if (getChildCount() == 0 && getAdapter() != null) {
            for (int index = 0; index < getAdapter().getCount(); index++) {
                addAndMeasureChild(obtainView(index));
            }
        }
    }

    protected void layoutChildren() {
        final int childCount = getChildCount();

        for (int childIndex = 0; childIndex < childCount; childIndex++) {
            final View child = getChildAt(childIndex);
            child.layout(0, -mTopOffset + childIndex * mDistanceBetweenPage, getWidth(), -mTopOffset + getHeight() + childIndex * mDistanceBetweenPage);
        }
    }

    private View getView(int position) {
        View v = null;
        final int index = position - mFirstPosition;
        if (index < getChildCount()) {
            v = getChildAt(index);
        } else {
            v = obtainView(position);
            addAndMeasureChild(v);
        }
        return v;
    }

    private View obtainView(int position) {
        View out = null;
        out = getAdapter().getView(position, out, this);
        return out;
    }

    private void addAndMeasureChild(View child, int index) {
        LayoutParams params = (LayoutParams) (child.getLayoutParams() instanceof LayoutParams ? child
                .getLayoutParams() : null);
        addViewInLayout(child, index, params, true);
        child.forceLayout();
        child.destroyDrawingCache();
        child.measure(MeasureSpec.AT_MOST | getWidth(), MeasureSpec.AT_MOST | getHeight());
        child.invalidate();
    }

    private void addAndMeasureChild(View child) {
        addAndMeasureChild(child, -1);
    }

    // Drawing methods

    @Override
    protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
        final int top = child.getTop();
        final int left = child.getLeft();

        final int centerY = child.getHeight() / 2;
        final int centerX = child.getWidth() / 2;


        mCamera.save();
        //mCamera.translate(0, 0, 1020);
        mCamera.rotateX((float) -20); // remove this line..
        mCamera.getMatrix(mMatrix);
        mCamera.restore();

        mMatrix.preTranslate(-centerX, 0);
        mMatrix.postTranslate(centerX, 0);
        mMatrix.postTranslate(left, top);
        canvas.save();
        canvas.concat(mMatrix);
        child.draw(canvas);
        canvas.restore();

        return false;
    }

    // Touch

    @Override
    public void computeScroll() {
        super.computeScroll();
        boolean needInvalidate = false;
        if (mScroller.computeScrollOffset()) {
            final int y = mScroller.getCurrY();
            final int dy = (int) (y - mLastTouchY);
            mLastTouchY = y;
            final int newOffset = mTopOffset - dy;
            int result = computeOffset(mScroller, newOffset);
        }

        if (mScroller.isFinished() && mTouchMode == TOUCH_MODE_FLINGING) {
            mTouchMode = TOUCH_MODE_IDLE;
        }
        if (needInvalidate) {
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    protected void performOnClickItem(float x, float y) {
        if (getOnItemClickListener() != null) {
            int position = 0;
            getOnItemClickListener().onItemClick(this, getView(position), position, getAdapter().getItemId(position));
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(ev);
        final int action = ev.getAction() & MotionEventCompat.ACTION_MASK;
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mVelocityTracker.clear();
                mScroller.abortAnimation();
                mLastTouchY = ev.getY();
                mActivePointerId = MotionEventCompat.getPointerId(ev, 0);
                mTouchRemainderY = 0;
                if (mTouchMode == TOUCH_MODE_FLINGING) {
                    // Catch!
                    mTouchMode = TOUCH_MODE_DRAGGING;
                    return true;
                }
                break;

            case MotionEvent.ACTION_MOVE: {
                int index = MotionEventCompat
                        .findPointerIndex(ev, mActivePointerId);
                if (index < 0) {
                    mActivePointerId = MotionEventCompat.getPointerId(ev, 0);
                    index = MotionEventCompat
                            .findPointerIndex(ev, mActivePointerId);
                }
                final float y = MotionEventCompat.getY(ev, index);
                final float dy = y - mLastTouchY + mTouchRemainderY;
                final int deltaY = (int) dy;
                mTouchRemainderY = dy - deltaY;

                if (Math.abs(dy) > mTouchSlop) {
                    mTouchMode = TOUCH_MODE_DRAGGING;

                    return true;
                }
            }
        }

        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (getChildCount() == 0) {
            return false;
        }
        mVelocityTracker.addMovement(ev);
        final int action = ev.getAction() & MotionEventCompat.ACTION_MASK;
        mLastTouchX = ev.getX();
        switch (action) {
            case MotionEvent.ACTION_DOWN:

                mVelocityTracker.clear();
                mScroller.abortAnimation();
                mLastTouchY = ev.getY();
                mActivePointerId = MotionEventCompat.getPointerId(ev, 0);
                mTouchRemainderY = 0;

                if (mTouchMode != TOUCH_MODE_FLINGING
                        && mTouchMode != TOUCH_MODE_DRAGGING) {
                    mTouchMode = TOUCH_MODE_DOWN;
                }
                postInvalidate();
                break;

            case MotionEvent.ACTION_MOVE: {

                int index = MotionEventCompat
                        .findPointerIndex(ev, mActivePointerId);
                if (index < 0) {
                    mActivePointerId = MotionEventCompat.getPointerId(ev, 0);
                    index = MotionEventCompat
                            .findPointerIndex(ev, mActivePointerId);
                    mLastTouchY = MotionEventCompat.getY(ev, index);
                }
                final float y = MotionEventCompat.getY(ev, index);
                final float dy = y - mLastTouchY + mTouchRemainderY;
                final int deltaY = (int) dy;
                mTouchRemainderY = dy - deltaY;

                if (Math.abs(dy) > mTouchSlop) {
                    mTouchMode = TOUCH_MODE_DRAGGING;

                }

                if (mTouchMode == TOUCH_MODE_DRAGGING) {
                    boolean needsInvalidate = false;
                    int newOffset = mTopOffset - deltaY;
                    int result = computeOffset(null, newOffset);

                    mLastTouchY = y;

                    if (needsInvalidate) {
                        ViewCompat.postInvalidateOnAnimation(this);
                    }

                }
            }
            break;

            case MotionEvent.ACTION_CANCEL:
                mTouchMode = TOUCH_MODE_IDLE;
                setPressed(false);
                mVelocityTracker.recycle();
                mVelocityTracker = null;
                mTouchMode = TOUCH_MODE_IDLE;
                break;

            case MotionEvent.ACTION_UP: {
                boolean unpressChildren = true;
                mVelocityTracker
                        .computeCurrentVelocity(1000, mMaximumFlingVelocity);
                final float velocity = VelocityTrackerCompat.getYVelocity(
                        mVelocityTracker, mActivePointerId);

                if (Math.abs(velocity) > mMinimumFlingVelocity) {
                    mTouchMode = TOUCH_MODE_FLINGING;
                    mScroller.fling(0, 0, 0, (int) velocity, 0, 0,
                            Integer.MIN_VALUE, Integer.MAX_VALUE);
                    mLastTouchY = 0;
                    invalidate();
                } else {
                    if (mTouchMode == TOUCH_MODE_DOWN
                            || mTouchMode == TOUCH_MODE_TAP) {
                        // Perform Click
                        performOnClickItem(ev.getX(), ev.getY());
                    }
                    mTouchMode = TOUCH_MODE_IDLE;
                }
                mVelocityTracker.recycle();
                mVelocityTracker = null;

            }
            break;
        }
        postInvalidate();
        return true;
    }

    protected int computeOffset(ScrollerCompat scroller, int newOffset) {
        final int maxBottom = 300000; // For tests
        int result = OFFSET_SCROLLING;

        if (newOffset <= getPaddingTop()) {
            moveTopOffset(getPaddingTop());
            result = OFFSET_HIT_TOP;
        } else if (newOffset >= mMaxTopOffset) {
            moveTopOffset(mMaxTopOffset);
            result = OFFSET_HIT_BOTTOM;
        } else {
            moveTopOffset(newOffset);
        }
        requestLayout();
        return result;
    }

    protected void moveTopOffset(int newOffset) {
        mTopOffset = newOffset;
        postInvalidate();
    }

    private void reloadData() {
        mMaxTopOffset = 600;
        mMaxTopOffset = (getAdapter().getCount() - 1) * (mDistanceBetweenPage);
    }

    private DataSetObserver mAdapterDataSetObserver = new DataSetObserver() {
        @Override
        public void onChanged() {
            super.onChanged();
            reloadData();
        }

        @Override
        public void onInvalidated() {
            super.onInvalidated();
        }
    };

}
