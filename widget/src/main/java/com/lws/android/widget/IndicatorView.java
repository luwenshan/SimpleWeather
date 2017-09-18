package com.lws.android.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by lws on 2017/9/18.
 */

public class IndicatorView extends LinearLayout {

    private Context mContext;
    private Paint mPaint;
    private int mMarkerId;
    private Bitmap mMarker = null;

    private int mIndicatorValue = 0;// 默认AQI值
    private int mTextSize = 6;// 默认文字大小
    private int mIntervalValue = 1;// TextView之间的间隔大小，单位dp
    private int mTextColorId = R.color.indicator_text_color;// 默认文字颜色
    private int mTextColor;
    private int mIndicatorStringsResourceId = R.array.indicator_strings;
    private int mIndicatorColorsResourceId = R.array.indicator_colors;

    private int mIndicatorViewWidth;// IndicatorView宽度
    private int mPaddingTopInXML;

    private String[] mIndicatorStrings;
    private int[] mIndicatorColorIds;

    public IndicatorView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    private void init(Context context, @Nullable AttributeSet attrs) {
        this.mContext = context;
        this.setOrientation(HORIZONTAL);
        //开启绘图缓存，提高绘图效率
        this.setDrawingCacheEnabled(true);

        initPaint();
        initAttrs(attrs);
        fillViewToParent(context);

        this.mPaddingTopInXML = this.getPaddingTop();
        this.setPadding(getPaddingLeft() + mMarker.getWidth() / 2,
                getPaddingTop() + mMarker.getHeight(),
                getPaddingRight() + mMarker.getWidth() / 2,
                getPaddingBottom());
    }

    private void initPaint() {
        this.mPaint = new Paint();
        // 设置是否使用抗锯齿功能，会消耗较大资源，绘制图形速度会变慢。
        this.mPaint.setAntiAlias(true);
        // 设定是否使用图像抖动处理，会使绘制出来的图片颜色更加平滑和饱满，图像更加清晰
        this.mPaint.setDither(true);
    }

    private void initAttrs(AttributeSet attrs) {
        DisplayMetrics dm = getResources().getDisplayMetrics();
        mTextSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, mTextSize, dm);
        mIntervalValue = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, mIntervalValue, dm);


        TypedArray typedArray = mContext.obtainStyledAttributes(attrs, R.styleable.IndicatorView);
        mMarkerId = typedArray.getResourceId(R.styleable.IndicatorView_marker, R.drawable.ic_vector_indicator_down);
        mMarker = drawableToBitmap(createVectorDrawable(mMarkerId, R.color.indicator_color_1));
        mIndicatorValue = typedArray.getInt(R.styleable.IndicatorView_indicatorValue, mIndicatorValue);
        mTextSize = typedArray.getDimensionPixelSize(R.styleable.IndicatorView_textSize, mTextSize);
        mIntervalValue = typedArray.getDimensionPixelSize(R.styleable.IndicatorView_intervalSize, mIntervalValue);
        mTextColor = typedArray.getColor(R.styleable.IndicatorView_textColor, getResources().getColor(mTextColor));
        mIndicatorStringsResourceId = typedArray.getInt(R.styleable.IndicatorView_indicatorStrings, mIndicatorStringsResourceId);
        mIndicatorColorsResourceId = typedArray.getInt(R.styleable.IndicatorView_indicatorColors, mIndicatorColorsResourceId);
        typedArray.recycle();
    }

    /**
     * 向父容器中填充View
     */
    private void fillViewToParent(Context context) {
        mIndicatorStrings = context.getResources().getStringArray(mIndicatorStringsResourceId);
        mIndicatorColorIds = context.getResources().getIntArray(mIndicatorColorsResourceId);
        if (mIndicatorStrings.length != mIndicatorColorIds.length) {
            throw new IllegalArgumentException("qualities和aqiColors的数组长度不一致！");
        }
        for (int i = 0; i < mIndicatorStrings.length; i++) {
            addTextView(context, mIndicatorStrings[i], mIndicatorColorIds[i]);
            if (i != (mIndicatorStrings.length - 1)) {
                addBlankView(context);
            }
        }
    }

    /**
     * 向父容器中添加TextView
     *
     * @param text  TextView显示文字
     * @param color TextView的背景颜色，如："#FADBCC"
     */
    private void addTextView(Context context, String text, int color) {
        TextView textView = new TextView(context);
        textView.setBackgroundColor(color);
        textView.setText(text);
        textView.setTextColor(mTextColor);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTextSize);
        textView.setSingleLine();
        textView.setGravity(Gravity.CENTER);
        textView.setLayoutParams(new LayoutParams(0, LayoutParams.WRAP_CONTENT, 1.0F));
        this.addView(textView);
    }

    /**
     * 向父容器中添加空白View
     */
    private void addBlankView(Context context) {
        View transparentView = new View(context);
        transparentView.setBackgroundColor(Color.TRANSPARENT);
        transparentView.setLayoutParams(new LayoutParams(mIntervalValue, LayoutParams.WRAP_CONTENT));
        this.addView(transparentView);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        mIndicatorViewWidth = MeasureSpec.getSize(widthMeasureSpec);

        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int indicatorViewHeight = MeasureSpec.getSize(heightMeasureSpec);

        int desiredWidth = mIndicatorViewWidth + getPaddingLeft() + getPaddingRight();
        int desiredHeight = this.getChildAt(0).getMeasuredHeight() + getPaddingTop() + getPaddingBottom();

        //测量宽度
        switch (widthMode) {
            case MeasureSpec.EXACTLY:
                break;
            case MeasureSpec.AT_MOST:
                mIndicatorViewWidth = Math.min(desiredWidth, mIndicatorViewWidth);
                break;
            case MeasureSpec.UNSPECIFIED:
                mIndicatorViewWidth = desiredWidth;
                break;
        }

        //测量高度
        switch (heightMode) {
            case MeasureSpec.EXACTLY:
                break;
            case MeasureSpec.AT_MOST:
                indicatorViewHeight = Math.min(desiredHeight, indicatorViewHeight);
                break;
            case MeasureSpec.UNSPECIFIED:
                indicatorViewHeight = desiredHeight;
                break;
        }
        setMeasuredDimension(mIndicatorViewWidth, indicatorViewHeight);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        drawMarkView(canvas);
    }

    /**
     * 用于绘制指示器图标
     */
    private void drawMarkView(Canvas canvas) {

        int width = this.mIndicatorViewWidth - this.getPaddingLeft() - this.getPaddingRight() - mIndicatorValue * 5;

        int left = this.getPaddingLeft();
        if (mIndicatorValue <= 50) {
            left += mIndicatorValue * (width * 4 / 6 / 200);
        } else if (mIndicatorValue > 50 && mIndicatorValue <= 100) {
            left += mIndicatorValue * (width * 4 / 6 / 200) + mIndicatorValue;
        } else if (mIndicatorValue > 100 && mIndicatorValue <= 150) {
            left += mIndicatorValue * (width * 4 / 6 / 200) + mIndicatorValue * 2;
        } else if (mIndicatorValue > 150 && mIndicatorValue <= 200) {
            left += mIndicatorValue * (width * 4 / 6 / 200) + mIndicatorValue * 3;
        } else if (mIndicatorValue > 200 && mIndicatorValue <= 300) {
            left += (width * 4 / 6) + (mIndicatorValue - 200) * width / 6 / 100 + mIndicatorValue * 4;
        } else {
            left += (width * 5 / 6) + (mIndicatorValue - 300) * width / 6 / 200 + mIntervalValue * 5;
        }
        canvas.drawBitmap(mMarker, left - mMarker.getWidth() / 2 - 2, this.mPaddingTopInXML, mPaint);
    }

    private IndicatorValueChangeListener indicatorValueChangeListener;

    public void setIndicatorValueChangeListener(IndicatorValueChangeListener indicatorValueChangeListener) {
        this.indicatorValueChangeListener = indicatorValueChangeListener;
    }

    public void setIndicatorValue(int indicatorValue) {

        if (indicatorValue < 0)
            throw new IllegalStateException("参数indicatorValue必须大于0");

        this.mIndicatorValue = indicatorValue;
        if (indicatorValueChangeListener != null) {
            String stateDescription;
            int indicatorTextColor;
            if (indicatorValue <= 50) {
                stateDescription = mIndicatorStrings[0];
                indicatorTextColor = mIndicatorColorIds[0];
            } else if (indicatorValue > 50 && indicatorValue <= 100) {
                stateDescription = mIndicatorStrings[1];
                indicatorTextColor = mIndicatorColorIds[1];
            } else if (indicatorValue > 100 && indicatorValue <= 150) {
                stateDescription = mIndicatorStrings[2];
                indicatorTextColor = mIndicatorColorIds[2];
            } else if (indicatorValue > 150 && indicatorValue <= 200) {
                stateDescription = mIndicatorStrings[3];
                indicatorTextColor = mIndicatorColorIds[3];
            } else if (indicatorValue > 200 && indicatorValue <= 300) {
                stateDescription = mIndicatorStrings[4];
                indicatorTextColor = mIndicatorColorIds[4];
            } else {
                stateDescription = mIndicatorStrings[5];
                indicatorTextColor = mIndicatorColorIds[5];
            }
            mMarker.recycle();
            mMarker = drawableToBitmap(createVectorDrawable(mMarkerId, indicatorTextColor));
            indicatorValueChangeListener.onChange(mIndicatorValue, stateDescription, indicatorTextColor);
        }
        invalidate();
    }

    private Drawable createVectorDrawable(int drawableId, int color) {
        VectorDrawableCompat vectorDrawableCompat = VectorDrawableCompat.create(getResources(), drawableId, mContext.getTheme());
        assert vectorDrawableCompat != null;
        DrawableCompat.setTint(vectorDrawableCompat, color);
        DrawableCompat.setTintMode(vectorDrawableCompat, PorterDuff.Mode.SRC);
        return vectorDrawableCompat;
    }

    private Bitmap drawableToBitmap(Drawable drawable) {
        Bitmap bitmap;
        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if (bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }

        if (drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            // Single color bitmap will be created of 1x1 pixel
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }
}
