package org.kitdroid.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.ScrollView;

import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.animation.ValueAnimator;
import com.nineoldandroids.animation.ValueAnimator.AnimatorUpdateListener;

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
    private int resetDelay = ELASTIC_DELAY;

    /**
     * ScrollView的子View (ScrollView只能有一个子View)
     */
    private View mInnerView;
    private View elasticView;

    private float startY;
    private int originHeight;
    private Rect normalRect = new Rect();
    private int elasticId;


    public ElasticScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
        readStyleAttributes(context, attrs, 0);
    }

    public ElasticScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        readStyleAttributes(context, attrs, 0);
    }

    @Override
    protected void onFinishInflate() {
        if (getChildCount() == 0) {
            return;
        }
        mInnerView = getChildAt(0);
        if (elasticId != 0) {
            View viewById = findViewById(elasticId);
            setElasticView(viewById);
        }
    }

    protected void readStyleAttributes(Context context, AttributeSet attrs, int defStyle) {

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.elastic_scroll, 0, defStyle);

        elasticId = a.getResourceId(R.styleable.elastic_scroll_elasticId, 0);

        a.recycle();
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

    public int getResetDelay() {
        return resetDelay;
    }

    public void setResetDelay(int resetDelay) {
        this.resetDelay = resetDelay;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        int action = ev.getAction();

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
        switch (action) {
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
        layoutParams.height = Math.max(0,layoutParams.height - deltaY);
        elasticView.setLayoutParams(layoutParams);
    }

    // 是否需要还原
    private boolean isNeedReset() {
        if (elasticView == null) {
            return !normalRect.isEmpty();
        } else {
            return originHeight != elasticView.getLayoutParams().height;
        }
    }

    private void doReset() {
        boolean needReset = isNeedReset();

        if (!needReset) {
            return;
        }

        if (elasticView != null) {
            resetElasticView();
        } else {
            resetInnerView();
        }
    }

    private void resetElasticView() {

        ValueAnimator animator = ObjectAnimator.ofInt(elasticView.getLayoutParams().height, originHeight);
        animator.setDuration(resetDelay);
        animator.setInterpolator(new OvershootInterpolator());

        animator.addUpdateListener(new AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int value = (Integer) animation.getAnimatedValue();
                android.view.ViewGroup.LayoutParams layoutParams = elasticView.getLayoutParams();
                layoutParams.height = value;
                elasticView.setLayoutParams(layoutParams);

            }
        });
        animator.start();

    }

    private void resetInnerView() {
        int moveY = mInnerView.getTop() - normalRect.top;
        ValueAnimator animator = ObjectAnimator.ofInt(moveY, 0);
        animator.setDuration(resetDelay);
        animator.setInterpolator(new OvershootInterpolator());

        animator.addUpdateListener(new AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int value = (Integer) animation.getAnimatedValue();
                mInnerView.layout(normalRect.left, normalRect.top + value, normalRect.right, normalRect.bottom + value );
            }
        });
        animator.start();

    }

    // 是否需要移动布局
    private boolean isNeedMove(int deltaY) {
        return deltaY == 0 ? false : (deltaY < 0 ? isNeedMoveTop() : isNeedMoveBottom());
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
