package com.ego.shadow.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

/**
 * 自定义控件
 */
public class ArcView extends View {

    private Paint mArcPaint;
    private Path mArcPath;

    public ArcView(Context context) {
        super(context);
        paint();
    }

    public ArcView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        paint();
    }

    public ArcView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        paint();
    }

    private void paint(){
        mArcPaint = new Paint();
        mArcPaint.setDither(true);//设定是否使用图像抖动处理，会使绘制出来的图片颜色更加平滑和饱满，图像更加清晰
        mArcPaint.setAntiAlias(true);//设置抗锯齿
        mArcPaint.setStrokeWidth(5);
        mArcPaint.setStyle(Paint.Style.FILL);
        mArcPaint.setStrokeCap(Paint.Cap.ROUND);
        mArcPaint.setColor(Color.parseColor("#0F4C81"));

        mArcPath = new Path();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        //1、先画上一部分长方形
        int height = getHeight() - 150;
        mArcPath.lineTo(0, height);
        mArcPath.lineTo(getWidth(), height);
        mArcPath.lineTo(getWidth(), 0);
        mArcPath.lineTo(0, 0);

        //2、画贝塞尔曲线
        mArcPath.moveTo(0, height);
        mArcPath.quadTo(getWidth() / 2, getHeight(), getWidth(), height);
        canvas.drawPath(mArcPath, mArcPaint);

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = measureDimension(100, widthMeasureSpec);
        int height = measureDimension(100, heightMeasureSpec);
        setMeasuredDimension(width, height);
    }

    private int measureDimension(int defSize, int measureSpec) {
        int result;

        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        if (specMode == MeasureSpec.EXACTLY)
            result = specSize;
        else {
            result = defSize;
            if (specMode == MeasureSpec.AT_MOST)
                result = Math.min(defSize, specSize);
        }
        return result;
    }



}
