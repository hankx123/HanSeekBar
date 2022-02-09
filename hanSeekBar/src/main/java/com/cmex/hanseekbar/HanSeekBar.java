package com.cmex.hanseekbar;


import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

public class HanSeekBar extends View {

    public interface ProgressCallback{
        void onProgressChanged(float currentValue);
    }

    private final float mMin;
    private final float mMax;
    private float mProgress;
    private final int mUntrackColor;
    private final int mTrackColor;
    private final int mSelectionColor;
    private final float mSelectionSize;
    private final float mBubbleTextSize;
    private final int mBubbleTextColor;
    private final int mDuration;
    private final float mThumbRadius;
    private final float mBarHeight;

    private int mSelectionCount = 2;
    
    private final Paint mPaint;
    private final Path mBubblePath;
    private float[] mSelectionPositions;
    private String[] mSelectionTitles;
    private float mThumbX;
    private float mThumbY;
    float xLeft = 0;
    float xRight = 0;

    private String mBubbleText = "";
    private final int mBgColor;

    public HanSeekBar(Context context) {
        this(context, null);
    }

    public HanSeekBar(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HanSeekBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.HanSeekBar, defStyleAttr, 0);
        mMin = a.getFloat(R.styleable.HanSeekBar_hsb_min, 0.0f);
        mMax = a.getFloat(R.styleable.HanSeekBar_hsb_max, 100.0f);
        mProgress = a.getFloat(R.styleable.HanSeekBar_hsb_progress, 0.0f);
        mUntrackColor = a.getColor(R.styleable.HanSeekBar_hsb_untrack_color, ContextCompat.getColor(getContext(), R.color.gray));
        mTrackColor = a.getColor(R.styleable.HanSeekBar_hsb_track_color, ContextCompat.getColor(getContext(), R.color.gray));
        mSelectionColor = a.getColor(R.styleable.HanSeekBar_hsb_selection_color, ContextCompat.getColor(getContext(), R.color.blue));
        mSelectionCount = a.getInt(R.styleable.HanSeekBar_hsb_selection_count, 2);
        mSelectionSize = a.getDimension(R.styleable.HanSeekBar_hsb_selection_size, 0);

        mBubbleTextSize = a.getDimension(R.styleable.HanSeekBar_hsb_bubble_size, 0.0f);
        mBubbleTextColor = a.getColor(R.styleable.HanSeekBar_hsb_bubble_color, ContextCompat.getColor(getContext(), R.color.gray));
        mDuration = a.getInt(R.styleable.HanSeekBar_hsb_bubble_duration, 1000);
        mThumbRadius = a.getDimension(R.styleable.HanSeekBar_hsb_thumb_radius, 0.0f);
        mBarHeight = a.getDimension(R.styleable.HanSeekBar_hsb_bar_height, 0.0f);

        Drawable background = getBackground();
        //background包括color和Drawable,这里分开取值
        if (background instanceof ColorDrawable) {
            ColorDrawable colordDrawable = (ColorDrawable) background;
            mBgColor = colordDrawable.getColor();
        } else {
            mBgColor = Color.TRANSPARENT;
        }

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setTextAlign(Paint.Align.CENTER);
        mBubblePath = new Path();

        setClickable(true);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int height; // 默认高度为拖动时thumb圆的直径
        if(mSelectionTitles != null && mSelectionTitles.length > 0){
            height = (int) (mSelectionSize * 1.5f + mThumbRadius * 2.2f + mBubbleTextSize * 2.1f + dp2px(getContext(), 6));
        } else {
            height = (int) (mThumbRadius * 2.2f + mBubbleTextSize * 2.1f + dp2px(getContext(), 6));
        }
        setMeasuredDimension(resolveSize(20, widthMeasureSpec), height + getPaddingBottom() + getPaddingTop());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float bottom = getMeasuredHeight() - getPaddingBottom();
        xLeft = getPaddingLeft() + mThumbRadius;
        xRight = getMeasuredWidth() - getPaddingRight() - mThumbRadius;
        float width = xRight - xLeft;
        mThumbX = xLeft + mProgress * width;

        if(mSelectionTitles != null && mSelectionTitles.length > 0) {
            Typeface font = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD);
            mPaint.setTextSize(mSelectionSize);
            mPaint.setColor(mSelectionColor);
            mPaint.setTextAlign(Paint.Align.CENTER);
            mPaint.setTypeface(font);
            mPaint.setStyle(Paint.Style.FILL);
            float txtY = bottom - mSelectionSize/2;
            for (int i = 0; i < mSelectionCount; i++) {
                canvas.drawText(mSelectionTitles[i], xLeft + width * mSelectionPositions[i], txtY, mPaint);
            }
            bottom = txtY - mSelectionSize;
        }

        float barY = bottom - mThumbRadius;
        mThumbY = barY;
        float trackX = mProgress*(xRight - xLeft) + xLeft;
        mPaint.setStrokeWidth(mBarHeight);
        mPaint.setColor(mUntrackColor);
        canvas.drawLine(xLeft, barY, xRight, barY, mPaint);
        mPaint.setColor(mTrackColor);
        canvas.drawLine(xLeft, barY, trackX, barY, mPaint);

        if(mSelectionCount > 0) {
//            mPaint.setColor(mBgColor);
//            for (int i = 0; i < mSelectionCount; i++) {
//                float pos = i * 1.0f / (mSelectionCount - 1);
//                if(mSelectionPositions != null && mSelectionPositions.length > 0) {
//                    pos = mSelectionPositions[i];
//                }
//                canvas.drawCircle(xLeft + width * pos, barY, mThumbRadius, mPaint);
//            }

            for (int i = 0; i < mSelectionCount; i++) {
                float pos = i* 1.0f / (mSelectionCount - 1);
                if(mSelectionPositions != null && mSelectionPositions.length > 0) {
                    pos = mSelectionPositions[i];
                }
                mPaint.setColor(pos > mProgress ? mUntrackColor : mTrackColor);
                canvas.drawCircle(xLeft + width * pos, barY, mBarHeight, mPaint);
            }
        }


        mPaint.setColor(mTrackColor);
        canvas.drawCircle(mThumbX, barY, mThumbRadius, mPaint);
        mPaint.setColor(Color.WHITE);
        canvas.drawCircle(mThumbX, barY, mBarHeight, mPaint);

        if(isShowingBubble){
            mPaint.setColor(mTrackColor);
            mPaint.setTextSize(mBubbleTextSize);
            float w = mPaint.measureText(mBubbleText) + dp2px(getContext(), 10);
            float h = mBubbleTextSize * 1.4f;
            float r = dp2px(getContext(), 2);
            float bubleBottomY = barY - mThumbRadius * 1.2f;
            mBubblePath.reset();
            float x0 = mThumbX;
            float y0 = bubleBottomY;
            mBubblePath.moveTo(x0, y0);
            float x1 = x0 - mBubbleTextSize/2;
            float y1 = y0 - mBubbleTextSize/2;
            mBubblePath.lineTo(x1, y1);
            float x2 = mThumbX - w / 2 + r;
            float y2 = y1;
            mBubblePath.lineTo(x2, y2);
            RectF rect = new RectF(x2-r, y2-r*2, x2+r, y2);
            mBubblePath.arcTo(rect, 90, 90);
            float x3 = x2 - r;
            float y3 = y2 - h + r ;
            mBubblePath.lineTo(x3, y3);
            RectF rect1 = new RectF(x3, y3-r, x2+2*r, y3+r);
            mBubblePath.arcTo(rect1, 180, 90);
            float x4 = mThumbX + w/2 - r;
            float y4 = y3 - r;
            mBubblePath.lineTo(x4, y4);
            RectF rect2 = new RectF(x4 - r, y4, x4 + r, y4+2*r);
            mBubblePath.arcTo(rect2, -90, 90);
            float x5 = x4 + r;
            float y5 = y1 - r;
            mBubblePath.lineTo(x5, y5);
            RectF rect3 = new RectF(x5 - 2*r, y5 - r, x5, y5+r);
            mBubblePath.arcTo(rect3, 0, 90);
            float x6 = mThumbX + mBubbleTextSize/2;
            float y6 = y1;
            mBubblePath.lineTo(x6, y6);
            mBubblePath.close();

            canvas.drawPath(mBubblePath, mPaint);

            mPaint.setColor(mBubbleTextColor);
            //计算baseline
            Paint.FontMetrics fontMetrics=mPaint.getFontMetrics();
            float distance=(fontMetrics.bottom - fontMetrics.top)/2 - fontMetrics.bottom;
            float baseline= (y1 + y2) / 2 - distance;
            canvas.drawText(mBubbleText, mThumbX, baseline, mPaint);
            Log.e("hanhan", " " + mBubbleText + ", " + w + ", " + h + "," + baseline + ", " + distance);
        }

    }
    
    public void setSelections(float[] positions, String[] selectionTitles){
        mSelectionCount = positions.length;
        mSelectionPositions = positions;
        mSelectionTitles = selectionTitles;
        invalidate();
    }

    public void setIsCustomBubbleText(){
        mBubbleTextByCustom = true;
    }

    public void setBubbleText(String text){
        mBubbleText = text;
        invalidate();
    }

    public void setOnProgressUpdate(ProgressCallback callback){
        progressCallback = callback;
    }

    private boolean isThumbOnDragged = false;
    private boolean isShowingBubble = false;
    private boolean mBubbleTextByCustom = false;
    private ProgressCallback progressCallback;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean handled = false;
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                performClick();
                getParent().requestDisallowInterceptTouchEvent(true);
                isThumbOnDragged = isThumbTouched(event);
                Log.e("hanhan", "action_down " + isThumbOnDragged);
                if(isThumbOnDragged){
                    handled = true;
                } else {
                    handled = clickToSeek(event);
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                Log.e("hanhan", "action is = " + event.getAction());
                if(mDuration > 0) {
                    postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            isShowingBubble = false;
                            invalidate();
                        }
                    }, mDuration);
                }
                handled = false;
                break;
            case MotionEvent.ACTION_MOVE:
                Log.e("hanhan", "action_move isThumbOnDragged = " + isThumbOnDragged);
                handled = moveToSeek(event);
                break;
        }
        return handled || super.onTouchEvent(event);
    }

    private boolean isThumbTouched(MotionEvent event) {
        if (!isEnabled())
            return false;

        return Math.abs(event.getX() - mThumbX) <= mThumbRadius && Math.abs(event.getY() - mThumbY) <= mThumbRadius;
    }

    private boolean clickToSeek(MotionEvent event){
        float offset = event.getX() - xLeft;
        float length = xRight - xLeft;
        offset = Math.max(0, offset);
        offset = Math.min(length, offset);
        mProgress = offset / length;
        isShowingBubble = true;
        float cval = (mProgress * (mMax -  mMin) + mMin);
        Log.e("hanhan", "clickToSeek  = " + mProgress);

        if(!mBubbleTextByCustom){
            mBubbleText = ("" + cval);
        }
        invalidate();
        if(progressCallback != null){
            progressCallback.onProgressChanged(cval);
        }
        return true;
    }

    private boolean moveToSeek(MotionEvent event){
        return clickToSeek(event);
    }

    private int dp2px(Context context, float dipValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }
}
