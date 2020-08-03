package com.ego.shadow.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.animation.LinearInterpolator;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;

import com.ego.shadow.R;

/**
 * @author lxy
 * @time 2019/7/1 10:59
 */
public class TickView extends AppCompatTextView {

    private Paint mPaintArc;//圆弧画笔
    private int mRetreatType = 1;//圆弧绘制方式（增加和减少）
    private float mPaintArcWidth;//最外层圆弧的宽度
    private int mCircleRadius;//圆圈的半径
    private int mPaintArcColor = Color.parseColor("#D81B60");//初始值
    private int mLoadingTime = 3;//时间，单位秒
    private int location = 2;//从哪个位置开始
    private float startAngle;//开始角度
    private float mmSweepAngleStart;//起点
    private float mmSweepAngleEnd;//终点
    private float mSweepAngle;//扫过的角度
    private int mWidth;
    private int mHeight;

    public TickView(Context context) {
        this(context, null);
    }

    public TickView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TickView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        this.setText("跳过");
        this.setGravity(Gravity.CENTER);
        this.setTextColor(Color.WHITE);
        this.setBackgroundResource(R.drawable.shape_round);
        this.mCircleRadius = dip2px(getContext(), 25);
        this.mPaintArcWidth = dip2px(getContext(), 3);

        mPaintArc = new Paint();
        mPaintArc.setStyle(Paint.Style.STROKE);
        mPaintArc.setAntiAlias(true);
        mPaintArc.setColor(mPaintArcColor);
        mPaintArc.setStrokeWidth(mPaintArcWidth);

        if (mLoadingTime < 0) {
            mLoadingTime = 3;
        }
        if (location == 1) {//默认从左侧开始
            startAngle = -180;
        } else if (location == 2) {
            startAngle = -90;
        } else if (location == 3) {
            startAngle = 0;
        } else if (location == 4) {
            startAngle = 90;
        }

        if (mRetreatType == 1) {
            mmSweepAngleStart = 0f;
            mmSweepAngleEnd = 360f;
        } else {
            mmSweepAngleStart = 360f;
            mmSweepAngleEnd = 0f;
        }
    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        //获取view宽高
        mWidth = w;
        mHeight = h;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        //因为必须是圆形的view，所以在这里重新赋值
        setMeasuredDimension(mCircleRadius * 2, mCircleRadius * 2);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        RectF rectF = new RectF(0 + mPaintArcWidth / 2, 0 + mPaintArcWidth / 2
                , mWidth - mPaintArcWidth / 2, mHeight - mPaintArcWidth / 2);
        canvas.drawArc(rectF, startAngle, mSweepAngle, false, mPaintArc);
    }

    public void start() {
        ValueAnimator animator = ValueAnimator.ofFloat(mmSweepAngleStart, mmSweepAngleEnd);
        animator.setInterpolator(new LinearInterpolator());
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                mSweepAngle = (float) valueAnimator.getAnimatedValue();
                //获取到需要绘制的角度，重新绘制
                invalidate();
            }
        });
        //这里是时间获取和赋值
        ValueAnimator animator1 = ValueAnimator.ofInt(mLoadingTime, 0);
        animator1.setInterpolator(new LinearInterpolator());
        animator1.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
            }
        });
        AnimatorSet set = new AnimatorSet();
        set.playTogether(animator, animator1);
        set.setDuration(mLoadingTime * 1000);
        set.setInterpolator(new LinearInterpolator());
        set.start();
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                clearAnimation();
                if (loadingFinishListener != null) {
                    loadingFinishListener.finish();
                }
            }
        });

    }

    private OnLoadingFinishListener loadingFinishListener;

    public void setOnLoadingFinishListener(OnLoadingFinishListener listener) {
        this.loadingFinishListener = listener;
    }

    public interface OnLoadingFinishListener {
        void finish();
    }

    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }
}
