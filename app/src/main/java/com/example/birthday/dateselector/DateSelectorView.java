package com.example.birthday.dateselector;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.core.content.ContextCompat;

import com.example.birthday.R;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @class name：com.example.birthday
 * @description: 内容选择器
 * @anthor: snow
 * @time: 2019/7/2 18:53
 * @modification_time:
 * @modifier:
 */
public class DateSelectorView extends View {

    private Context mContext;

    private Paint mPaint;
    private int mLightColor, mDarkColor;

    private boolean mCanScroll = true;    //可滚动
    private boolean mCanScrollLoop = true;    //可循环滚动
    private boolean mCanShowAnim = true;    //可播放滚动动画
    private ObjectAnimator mScrollAnim;    //滚动动画
    private List<String> mDataList = new ArrayList<>();
    private int mSelectedIndex;
    private float mScrollDistance;
    private TimerTask mTimerTask;
    private Timer mTimer = new Timer();
    private OnSelectListener mOnSelectListener;
    private Handler mHandler = new ScrollHandler(this);
    private float mLastTouchY;
    private float mTextSpacing, mHalfTextSpacing;
    private float mHalfWidth, mHalfHeight, mQuarterHeight;
    private float mMinTextSize, mTextSizeRange;
    /**
     * 自动回滚到中间的速度
     */
    private static final float AUTO_SCROLL_SPEED = 10;

    /**
     * 透明度：最小 120，最大 255，极差135
     */
    private static final int TEXT_ALPHA_MIN = 120;
    private static final int TEXT_ALPHA_RANGE = 135;

    /**
    * @description 是否允许循环滚动
    * @param
    * @return
    * @author snow
    * @time 2019/7/16 19:14
    */
    public void setCanScrollLoop(boolean canLoop) {
        mCanScrollLoop = canLoop;
    }

    /**
    * @description 是否允许滚动动画
    * @param
    * @return
    * @author snow
    * @time 2019/7/16 19:16
    */
    public void setCanShowAnim(boolean canShowAnim) {
        mCanShowAnim = canShowAnim;
    }

    /**
    * @description 销毁资源
    * @param 
    * @return 
    * @author snow
    * @time 2019/7/17 10:22
    */
    public void onDestroy() {
        mOnSelectListener = null;
        mHandler.removeCallbacksAndMessages(null);
        if (mScrollAnim != null && mScrollAnim.isRunning()) {
            mScrollAnim.cancel();
        }
        cancelTimerTask();
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
    }

    public interface OnSelectListener{
        void onSelect(View view, String selected);
    }

    public DateSelectorView(Context context, AttributeSet attrs) {
        super(context, attrs);

        mContext = context;
        initPaint();
    }

    private void initPaint() {
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setTextAlign(Paint.Align.CENTER);
        mLightColor = ContextCompat.getColor(mContext, R.color.skyBlue1);
        mDarkColor = ContextCompat.getColor(mContext, R.color.grey21);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        mHalfWidth = getMeasuredWidth() / 2f;
        int height = getMeasuredHeight();
        mHalfHeight = height / 2f;
        mQuarterHeight = height / 4f;
        float maxTextSize = height / 7f;
        mMinTextSize = maxTextSize / 2.2f;
        mTextSizeRange = maxTextSize - mMinTextSize;
        mTextSpacing = mMinTextSize * 2.8f;
        mHalfTextSpacing = mTextSpacing / 2f;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mSelectedIndex >= mDataList.size()) return;

        // 绘制选中的 text
        drawText(canvas, mLightColor, mScrollDistance, mDataList.get(mSelectedIndex));

        // 绘制选中上方的 text
        for (int i = 1; i <= mSelectedIndex; i++) {
            drawText(canvas, mDarkColor, mScrollDistance - i * mTextSpacing,
                    mDataList.get(mSelectedIndex - i));
        }

        // 绘制选中下方的 text
        int size = mDataList.size() - mSelectedIndex;
        for (int i = 1; i < size; i++) {
            drawText(canvas, mDarkColor, mScrollDistance + i * mTextSpacing,
                    mDataList.get(mSelectedIndex + i));
        }
    }

    private void drawText(Canvas canvas, int textColor, float offsetY, String text) {
        if (TextUtils.isEmpty(text)) return;

        float scale = 1 - (float) Math.pow(offsetY / mQuarterHeight, 2);
        scale = scale < 0 ? 0 : scale;
        mPaint.setTextSize(mMinTextSize + mTextSizeRange * scale);
        mPaint.setColor(textColor);
        mPaint.setAlpha(TEXT_ALPHA_MIN + (int) (TEXT_ALPHA_RANGE * scale));

        // text 居中绘制，mHalfHeight + offsetY 是 text 的中心坐标
        Paint.FontMetrics fm = mPaint.getFontMetrics();
        float baseline = mHalfHeight + offsetY - (fm.top + fm.bottom) / 2f;
        canvas.drawText(text, mHalfWidth, baseline, mPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                cancelTimerTask();
                mLastTouchY = event.getY();
                break;

            case MotionEvent.ACTION_MOVE:
                float offsetY = event.getY();
                mScrollDistance += offsetY - mLastTouchY;
                if (mScrollDistance > mHalfTextSpacing) {
                    if (!mCanScrollLoop) {
                        if (mSelectedIndex == 0) {
                            mLastTouchY = offsetY;
                            invalidate();
                            break;
                        } else {
                            mSelectedIndex--;
                        }
                    } else {
                        // 往下滑超过离开距离，将末尾元素移到首位
                        moveTailToHead();
                    }
                    mScrollDistance -= mTextSpacing;
                } else if (mScrollDistance < -mHalfTextSpacing) {
                    if (!mCanScrollLoop) {
                        if (mSelectedIndex == mDataList.size() - 1) {
                            mLastTouchY = offsetY;
                            invalidate();
                            break;
                        } else {
                            mSelectedIndex++;
                        }
                    } else {
                        // 往上滑超过离开距离，将首位元素移到末尾
                        moveHeadToTail();
                    }
                    mScrollDistance += mTextSpacing;
                }
                mLastTouchY = offsetY;
                invalidate();
                break;

            case MotionEvent.ACTION_UP:
                // 抬起手后 mSelectedIndex 由当前位置滚动到中间选中位置
                if (Math.abs(mScrollDistance) < 0.01) {
                    mScrollDistance = 0;
                    break;
                }
                cancelTimerTask();
                mTimerTask = new ScrollTimerTask(mHandler);
                mTimer.schedule(mTimerTask, 0, 10);
                break;
        }
        return true;
    }

    private static class ScrollTimerTask extends TimerTask {
        private WeakReference<Handler> mWeakHandler;

        private ScrollTimerTask(Handler handler) {
            mWeakHandler = new WeakReference<>(handler);
        }

        @Override
        public void run() {
            Handler handler = mWeakHandler.get();
            if (handler == null) return;

            handler.sendEmptyMessage(0);
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event){
        return mCanScroll && super.dispatchTouchEvent(event);
    }

    /**
    * @description 设置数据
    * @param list
    * @return null
    * @author snow
    * @time 2019/7/15 11:25
    */
    public void setDataList(List<String> list){
        if(list == null || list.isEmpty()) return;

        mDataList = list;
        mSelectedIndex = 0;
        invalidate();
    }

    /**
    * @description 选择选中项
    * @param index
    * @return null
    * @author snow
    * @time 2019/7/15 11:29
    */
    public void setSelected(int index){
        if(index >= mDataList.size()) return;

        mSelectedIndex = index;
        if(mCanScrollLoop){
            //可循环滚动，mSelectedIndex 值固定为 mDataList / 2
            int position = mDataList.size() / 2 - mSelectedIndex;
            if(position < 0){
                for(int i = 0; i < -position; i++){
                    moveHeadToTail();
                    mSelectedIndex--;
                }
            } else if(position > 0){
                for(int i = 0; i < position; i++){
                    moveTailToHead();
                    mSelectedIndex++;
                }
            }
        }
    }

    /**
    * @description 从头到尾移动
    * @param
    * @return 
    * @author snow
    * @time 2019/7/15 11:44
    */
    private void moveHeadToTail() {
        if(!mCanScrollLoop || mDataList.isEmpty()) return;

        String head = mDataList.get(0);
        mDataList.remove(0);
        mDataList.add(head);
    }

    /**
    * @description 从尾到头移动
    * @param
    * @return
    * @author snow
    * @time 2019/7/15 11:48
    */
    private void moveTailToHead() {
        if(!mCanScrollLoop || mDataList.isEmpty()) return;

        String tail = mDataList.get(mDataList.size() - 1);
        mDataList.remove(mDataList.size() - 1);
        mDataList.add(tail);
    }

    private void keepScrolling(){
        if(Math.abs(mScrollDistance) < AUTO_SCROLL_SPEED){
            mScrollDistance = 0;
            if(mTimerTask != null){
                cancelTimerTask();

                if(mOnSelectListener != null && mSelectedIndex < mDataList.size()){
                    mOnSelectListener.onSelect(this, mDataList.get(mSelectedIndex));
                }
            }
        }else if(mScrollDistance > 0){    //向下滚动
            mScrollDistance -= AUTO_SCROLL_SPEED;
        }else{    //向上滚动
            mScrollDistance += AUTO_SCROLL_SPEED;
        }
        invalidate();
    }

    private void cancelTimerTask() {
        if(mTimerTask != null){
            mTimerTask.cancel();
            mTimerTask = null;
        }
        if(mTimer != null){
            mTimer.purge();
        }
    }

    /**
    * @description 开始滚动动画
    * @param
    * @return
    * @author snow
    * @time 2019/7/15 16:19
    */
    public void startAnim() {
        if(!mCanShowAnim) return;

        if(mScrollAnim == null){
            PropertyValuesHolder alpha = PropertyValuesHolder.ofFloat("alpha", 1f, 0f,1f);
            PropertyValuesHolder scaleX = PropertyValuesHolder.ofFloat("scaleX", 1f,1.3f,1f);
            PropertyValuesHolder scaleY = PropertyValuesHolder.ofFloat("scaleY", 1f,1.3f,1f);
            mScrollAnim = ObjectAnimator.ofPropertyValuesHolder(this, alpha, scaleX, scaleY).setDuration(200);
        }

        if(!mScrollAnim.isRunning()){
            mScrollAnim.start();
        }
    }

    /**
    * @description 是否允许循环滚动
    * @param canScroll
    * @return
    * @author snow
    * @time 2019/7/15 18:25
    */
    public void setCanScroll(boolean canScroll) {
        mCanScroll = canScroll;
    }

    /**
    * @description 设置选择结果监听
    * @param
    * @return
    * @author snow
    * @time 2019/7/16 16:15
    */
    public void setOnSelectListener(OnSelectListener listener) {
        mOnSelectListener = listener;
    }

    //inner类
    private class ScrollHandler extends Handler {
        private WeakReference<DateSelectorView> mWeakView;

        public ScrollHandler(DateSelectorView view) {
            mWeakView = new WeakReference<>(view);
        }

        @Override
        public void handleMessage(Message msg){
            DateSelectorView view = mWeakView.get();
            if (view == null) return;

            view.keepScrolling();
        }
    }
}
