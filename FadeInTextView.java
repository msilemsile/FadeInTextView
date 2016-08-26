package com.msile.view.playerdemo.videoplayerdemo;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Handler;
import android.os.Message;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;

import java.lang.ref.WeakReference;

/**
 * 渐显textview
 * Created by msilemsile on 16/8/9.
 */
public class FadeInTextView extends View {

    private TextPaint mPaint;                   //画笔
    private int mDuration = 18;                 //每次刷新时间
    private String mText;                       //内容
    private static final int ANIM_START = 0;
    private static final int ANIM_STOP = 1;
    private InnerHandler innerHandler;
    private int clipX;                          //剪切x坐标的位置
    private int clipDistance = 36;              //每次剪切的距离
    private StaticLayout mStaticLayout;
    private int mLineCount, mLineHeight;
    private int mTempLine;
    private Path mTempPath;

    public FadeInTextView(Context context) {
        this(context, null);
    }

    public FadeInTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //计算宽高
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    /**
     * 初始化画笔等
     */
    private void init() {
        mTempPath = new Path();
        mPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(Color.RED);
        setTextSize(18);
        innerHandler = new InnerHandler(this);
        setLayerType(LAYER_TYPE_SOFTWARE, null);
    }

    /**
     * 销毁view 清除回调
     */
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopAnim();
    }

    /**
     * 设置要显示文本 并开始动画
     */
    public void setText(String text) {
        if (!TextUtils.isEmpty(text)) {
            mText = text;
            mStaticLayout = new StaticLayout(mText, mPaint, getResources().getDisplayMetrics().widthPixels, Layout.Alignment.ALIGN_NORMAL, 1.0F, 0.0F, false);
            mLineCount = mStaticLayout.getLineCount();
            mLineHeight = mStaticLayout.getHeight() / mLineCount;
            mTempLine = 0;
            mTempPath.reset();
            requestLayout();
            innerHandler.sendEmptyMessageDelayed(ANIM_START, mDuration);
        }
    }

    /**
     * 设置文本颜色
     */
    public void setTextColor(int color) {
        mPaint.setColor(color);
    }

    /**
     * 设置画笔大小
     */
    public void setTextSize(int spSize) {
        final float fontScale = getResources().getDisplayMetrics().scaledDensity;
        mPaint.setTextSize(spSize * fontScale + 0.5f);
    }

    /**
     * 设置每次剪切的x轴距离
     */
    public void setClipDistance(int clipDistance) {
        this.clipDistance = clipDistance;
    }

    /**
     * 设置每次刷新时间
     */
    public void setAnimDuration(int mDuration) {
        this.mDuration = mDuration;
    }

    /**
     * 开始动画
     */
    public void startAnim() {
        if (TextUtils.isEmpty(mText)) {
            innerHandler.removeCallbacks(null);
            return;
        }
        clipX += clipDistance;
        invalidate();
        innerHandler.sendEmptyMessageDelayed(ANIM_START, mDuration);
    }

    /**
     * 停止动画
     */
    public void stopAnim() {
        innerHandler.removeMessages(ANIM_START);
        innerHandler.removeMessages(ANIM_STOP);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (TextUtils.isEmpty(mText)) {
            return;
        }
        if (clipX >= getWidth()) {
            clipX = getWidth();
        }
        canvas.save(Canvas.CLIP_SAVE_FLAG);
        calculatePath();
        canvas.clipPath(mTempPath);
        mStaticLayout.draw(canvas);
        canvas.restore();
    }

    /**
     * 计算裁剪路径
     */
    private void calculatePath() {
        mTempPath.reset();
        if (mTempLine == mLineCount) {
            mTempPath.addRect(0, 0, getWidth(), getHeight(), Path.Direction.CW);
        } else {
            mTempPath.addRect(0, 0, getWidth(), mTempLine * mLineHeight, Path.Direction.CW);
            mTempPath.addRect(0, mTempLine * mLineHeight, clipX, (mTempLine + 1) * mLineHeight, Path.Direction.CW);
        }
        if (clipX == getWidth()) {
            if (mTempLine == mLineCount) {
                stopAnim();
            } else {
                clipX = 0;
                mTempLine++;
            }
        }
    }

    static class InnerHandler extends Handler {

        WeakReference<FadeInTextView> textViewWeakReference;

        public InnerHandler(FadeInTextView fadeInTextView) {
            textViewWeakReference = new WeakReference<>(fadeInTextView);
        }

        @Override
        public void handleMessage(Message msg) {
            FadeInTextView textView = textViewWeakReference.get();
            if (textView == null) {
                return;
            }
            switch (msg.what) {
                case ANIM_START:
                    textView.startAnim();
                    break;
                case ANIM_STOP:
                    textView.stopAnim();
                    break;
            }
        }
    }

}
