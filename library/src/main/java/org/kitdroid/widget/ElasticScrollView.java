package org.kitdroid.widget;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.TranslateAnimation;
import android.widget.ScrollView;
import android.widget.Scroller;

/**
 * ScrollView反弹效果的实现
 */
public class ElasticScrollView extends ScrollView {
    /**
     * 区分点击or滑动
     */
    private static final int SHAKE_THRESHOLD_VALUE = 3;

    private static final int DAMP_COEFFICIENT = 2;

    private static final int ELASTIC_DELAY = 200;

    private static final int TOP_Y = 0;
    /**
     * 阻力
     */
    private float damk = DAMP_COEFFICIENT;
    /**
     * 回弹延迟
     */
    private int restoreDelay = ELASTIC_DELAY;

    /**
     * ScrollView的子View (ScrollView只能有一个子View)
     */
    private View mInnerView;
    private View elasticView;

    private float startY;
    private int originHeight;
    private Rect normalRect = new Rect();
    private Scroller mScroller;



    public ElasticScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        if (getChildCount() > 0) {
            mInnerView = getChildAt(0);
        }
        mScroller = new Scroller(getContext());
    }

    public void setElasticView(View view) {
        refreshOriginHeight(view);
        elasticView = view;
    }

    public View getElasticView() {
        return elasticView;
    }

    public float getDamk() {
        return damk;
    }

    public void setDamk(float damk) {
        this.damk = damk;
    }

    public int getRestoreDelay() {
        return restoreDelay;
    }

    public void setRestoreDelay(int restoreDelay) {
        this.restoreDelay = restoreDelay;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        int action = ev.getAction();
        System.out.println("onInterceptTouchEvent: "+action);
        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                startY = ev.getY();
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                float currentY = ev.getY();
                float scrollY = currentY - startY;

                return Math.abs(scrollY) > SHAKE_THRESHOLD_VALUE;
            }
        }
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (mInnerView != null) {
            computeMove(ev);
        }
        return super.onTouchEvent(ev);
    }

    private void computeMove(MotionEvent event) {
        int action = event.getAction();
        System.out.println("onTouchEvent: "+action);
        switch (action) {
//            case MotionEvent.ACTION_DOWN: {
//                startY = event.getY();
//                break;
//            }
            case MotionEvent.ACTION_UP: {
                doReset();
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                doMove(event);
                break;
            }
            default:
                break;
        }
    }

    private int computeDeltaY(MotionEvent event) {
        float currentY = event.getY();

        int deltaY = (int) ((startY - currentY) / damk);
        startY = currentY;

        return deltaY;
    }

    private void doMove(MotionEvent event) {
        int deltaY = computeDeltaY(event);

        if (!isNeedMove(deltaY)) {
            return;
        }

        refreshNormalRect();

        if (elasticView != null) {
            moveElasticView(deltaY);
        } else {
            moveInnerView(deltaY);
        }
    }

    private void refreshOriginHeight(View view) {
        if (elasticView != null) {
            android.view.ViewGroup.LayoutParams layoutParams = elasticView.getLayoutParams();
            layoutParams.height = originHeight;
            elasticView.setLayoutParams(layoutParams);
        }
        if (null != view) {
            originHeight = view.getLayoutParams().height;
        }
    }

    private void refreshNormalRect() {
        if (!normalRect.isEmpty()) {// 保存正常的布局位置
            return;
        }
        normalRect.set(mInnerView.getLeft(), mInnerView.getTop(), mInnerView.getRight(), mInnerView.getBottom());
    }

    private void moveInnerView(int deltaY) {
        mInnerView.layout(mInnerView.getLeft(), mInnerView.getTop() - deltaY, mInnerView.getRight(), mInnerView.getBottom() - deltaY);
    }

    private void moveElasticView(int deltaY) {
        android.view.ViewGroup.LayoutParams layoutParams = elasticView.getLayoutParams();
        layoutParams.height -= deltaY;
        elasticView.setLayoutParams(layoutParams);
    }

    // 是否需要还原
    private boolean isNeedRestore() {
        return !normalRect.isEmpty();
    }

    private void doReset() {
        if (!isNeedRestore()) {
            return;
        }

        if (elasticView != null) {
            restoreElasticView();
        } else {
            restoreInnerView();
        }
    }

    private void restoreElasticView() {
        android.view.ViewGroup.LayoutParams layoutParams = elasticView.getLayoutParams();
        mScroller.startScroll(0, layoutParams.height, 0, originHeight - layoutParams.height, restoreDelay);
        invalidate();
    }

    private void restoreInnerView() {
        TranslateAnimation ta = new TranslateAnimation(0, 0, mInnerView.getTop(), normalRect.top);
        ta.setDuration(restoreDelay);
        mInnerView.startAnimation(ta);// 开启移动动画

        mInnerView.layout(normalRect.left, normalRect.top, normalRect.right, normalRect.bottom);// 设置回到正常的布局位置

        normalRect.setEmpty();
    }

    // 是否需要移动布局
    private boolean isNeedMove(int deltaY) {
        return deltaY == 0 ? false : (deltaY < 0 ? isNeedMoveTop() : isNeedMoveBottom());
//        if (deltaY < 0) {
//            return isNeedMoveTop();
//        }
//        if (deltaY > 0) {
//            return isNeedMoveBottom();
//        }
//        return false;
    }

    private boolean isNeedMoveTop() {
        int scrollY = getScrollY();
        return (scrollY == TOP_Y);
    }

    private boolean isNeedMoveBottom() {
        int offset = mInnerView.getMeasuredHeight() - getHeight();
        offset = (offset < 0) ? 0 : offset;

        int scrollY = getScrollY();
        return (scrollY == offset);
    }
}
