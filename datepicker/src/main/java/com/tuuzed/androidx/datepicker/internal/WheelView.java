package com.tuuzed.androidx.datepicker.internal;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Interpolator;
import android.widget.Scroller;
import androidx.annotation.Nullable;
import com.tuuzed.androidx.datepicker.R;

import java.util.LinkedList;
import java.util.List;

public class WheelView extends View {
    /**
     * 滚动持续的时间
     */
    private static final int SCROLLING_DURATION = 300;
    /**
     * 最少滚动的位置
     */
    private static final int MIN_DELTA_FOR_SCROLLING = 1;
    /**
     * 附加的item的高度
     */
    private static final int ADDITIONAL_ITEM_HEIGHT = 60;
    /**
     * item布局的附加宽度
     */
    private static final int ADDITIONAL_ITEMS_SPACE = 12;
    /**
     * 标签偏移值
     */
    private static final int LABEL_OFFSET = 22;
    /**
     * 左右padding值
     */
    private static final int PADDING = 0;
    /**
     * WheelAdapter
     */
    private WheelAdapter adapter = null;
    /**
     * item宽度
     */
    private int itemsWidth = 0;
    /**
     * 标签宽度
     */
    private int labelWidth = 0;
    /**
     * item高度
     */
    private int itemHeight = 0;
    /**
     * item的字符串属性对象
     */
    private TextPaint itemsPaint;
    /**
     * value的字符串属性对象
     */
    private TextPaint valuePaint;
    // Layouts
    private StaticLayout itemsLayout, labelLayout, valueLayout;
    /**
     * 滚动动作是否执行
     */
    private boolean isScrollingPerformed;
    /**
     * 滚动偏移量
     */
    private int scrollingOffset;
    /**
     * 手势侦测对象
     */
    private GestureDetector gestureDetector;
    private Scroller scroller;
    private int lastScrollY;
    /**
     * 实例化OnWheelChangedListener
     */
    private List<OnWheelChangedListener> changingListeners = new LinkedList<>();
    /**
     * 实例化OnWheelScrollListener
     */
    private List<OnWheelScrollListener> scrollingListeners = new LinkedList<>();
    /**
     * 当前item位置
     */
    private int mPosition = 0;
    // 自定义属性
    private String mLabel;
    private boolean mIsCyclic;
    private int mColor;
    private int mSelectColor;
    private int mTextSize;
    private int mVisibleCount;
    @Nullable
    private Drawable mTopShadow;
    @Nullable
    private Drawable mBottomShadow;
    @Nullable
    private Drawable mSelectedBg;

    public WheelView(Context context) {
        this(context, null);
    }

    public WheelView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WheelView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        gestureDetector = new GestureDetector(context, gestureListener);
        gestureDetector.setIsLongpressEnabled(false);// 设置手势长按不起作用
        scroller = new Scroller(context);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.WheelView);
        mLabel = a.getString(R.styleable.WheelView_wv_label);
        mIsCyclic = a.getBoolean(R.styleable.WheelView_wv_cyclic, false);
        mColor = a.getColor(R.styleable.WheelView_wv_color, 0xFF919191);
        mSelectColor = a.getColor(R.styleable.WheelView_wv_selectColor, 0xFF000000);
        mTextSize = (int) a.getDimension(R.styleable.WheelView_wv_textSize, Utils.sp2px(context, 16));
        mVisibleCount = a.getInt(R.styleable.WheelView_wv_visibleCount, 3);
        mTopShadow = a.getDrawable(R.styleable.WheelView_wv_topShadow);
        mBottomShadow = a.getDrawable(R.styleable.WheelView_wv_topShadow);
        mSelectedBg = a.getDrawable(R.styleable.WheelView_wv_selectedBg);
        a.recycle();
    }

    private int getItemOffset() {
        return mTextSize / 3 - 10;
    }


    /**
     * 获取滚轮适配器
     *
     * @return
     */
    public WheelAdapter getAdapter() {
        return adapter;
    }

    /**
     * 设置滚轮适配器
     *
     * @param adapter
     */
    public void setAdapter(WheelAdapter adapter) {
        this.adapter = adapter;
        invalidateLayouts();
        invalidate();// 是视图无效
    }

    /**
     * 设置指定的滚轮动画变化率
     *
     * @param interpolator
     */
    public void setInterpolator(Interpolator interpolator) {
        scroller.forceFinished(true);
        scroller = new Scroller(getContext(), interpolator);
    }

    /**
     * 得到可见item的数目
     *
     * @return the count of visible items
     */
    public int getVisibleCount() {
        return mVisibleCount;
    }

    /**
     * 设置可见item的数目
     *
     * @param count the new count
     */
    public void setVisibleCount(int count) {
        mVisibleCount = count;
        invalidate();
    }

    /**
     * 得到标签
     *
     * @return
     */
    public String getLabel() {
        return mLabel;
    }

    /**
     * 设置标签
     *
     * @param newLabel
     */
    public void setLabel(String newLabel) {
        if (mLabel == null || !mLabel.equals(newLabel)) {
            mLabel = newLabel;
            labelLayout = null;
            invalidate();
        }
    }

    /**
     * 增加滚轮变化监听器
     *
     * @param listener
     */
    public void addChangingListener(OnWheelChangedListener listener) {
        changingListeners.add(listener);
    }

    /**
     * 移除滚轮变化监听器
     *
     * @param listener
     */
    public void removeChangingListener(OnWheelChangedListener listener) {
        changingListeners.remove(listener);
    }

    /**
     * 通知改变的监听器
     *
     * @param oldValue
     * @param newValue
     */
    protected void notifyChangingListeners(int oldValue, int newValue) {
        for (OnWheelChangedListener listener : changingListeners) {
            listener.onChanged(this, oldValue, newValue);
        }
    }

    /**
     * 增加滚轮监听器
     *
     * @param listener the listener
     */
    public void addScrollingListener(OnWheelScrollListener listener) {
        scrollingListeners.add(listener);
    }

    /**
     * 移除滚轮监听器
     *
     * @param listener the listener
     */
    public void removeScrollingListener(OnWheelScrollListener listener) {
        scrollingListeners.remove(listener);
    }

    /**
     * 通知监听器开始滚动
     */
    protected void notifyScrollingListenersAboutStart() {
        for (OnWheelScrollListener listener : scrollingListeners) {
            listener.onScrollingStarted(this);
        }
    }

    /**
     * 通知监听器结束滚动
     */
    protected void notifyScrollingListenersAboutEnd() {
        for (OnWheelScrollListener listener : scrollingListeners) {
            listener.onScrollingFinished(this);
        }
    }

    /**
     * 取得当前item
     *
     * @return
     */
    public int getPosition() {
        return mPosition;
    }

    /**
     * 设置当前item
     *
     * @param position
     * @param animated
     */
    public void setPosition(int position, boolean animated) {
        if (adapter == null || adapter.getItemsCount() == 0) {
            return;
        }
        if (position < 0 || position >= adapter.getItemsCount()) {
            if (mIsCyclic) {
                while (position < 0) {
                    position += adapter.getItemsCount();
                }
                position %= adapter.getItemsCount();
            } else {
                return;
            }
        }
        if (position != mPosition) {
            if (animated) {
                scroll(position - mPosition, SCROLLING_DURATION);
            } else {
                invalidateLayouts();

                int old = mPosition;
                mPosition = position;

                notifyChangingListeners(old, mPosition);
                invalidate();
            }
        }
    }

    /**
     * 设置当前item w/o 动画. 当index有误是不做任何响应.
     *
     * @param position the item position
     */
    public void setPosition(int position) {
        setPosition(position, false);
    }

    /**
     * 测试滚轮是否可循环.
     *
     * @return true if wheel is cyclic
     */
    public boolean ssCyclic() {
        return mIsCyclic;
    }

    /**
     * 设置滚轮循环标志
     *
     * @param isCyclic the flag to set
     */
    public void setCyclic(boolean isCyclic) {
        this.mIsCyclic = isCyclic;
        invalidate();
        invalidateLayouts();
    }

    public void setColor(int color) {
        mColor = color;
        invalidate();
    }

    public void setSelectColor(int selectColor) {
        mSelectColor = selectColor;
        invalidate();
    }

    public void setTextSize(int textSize) {
        this.mTextSize = textSize;
        invalidate();
    }

    /**
     * 使布局无效
     */
    private void invalidateLayouts() {
        itemsLayout = null;
        valueLayout = null;
        scrollingOffset = 0;
    }

    /**
     * 初始化资源信息
     */
    private void initResourceIfNecessary() {
        if (itemsPaint == null) {
            itemsPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
            itemsPaint.setTextSize(mTextSize);
        }
        if (valuePaint == null) {
            valuePaint = new TextPaint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
            valuePaint.setTextSize(mTextSize);
            valuePaint.setShadowLayer(0.1f, 0, 0.1f, 0xFFC0C0C0);
        }
    }


    /**
     * 计算layout所需的高度
     *
     * @param layout
     * @return
     */
    private int getDesiredHeight(Layout layout) {
        if (layout == null) return 0;
        int desired = getItemHeight() * mVisibleCount - getItemOffset() * 2 - ADDITIONAL_ITEM_HEIGHT;
        desired = Math.max(desired, getSuggestedMinimumHeight());
        return desired;
    }


    /**
     * 通过index得到text
     *
     * @param index
     * @return
     */
    private String getTextItem(int index) {
        if (adapter == null || adapter.getItemsCount() == 0) {
            return null;
        }
        int count = adapter.getItemsCount();
        if ((index < 0 || index >= count) && !mIsCyclic) {
            return null;
        } else {
            while (index < 0) {
                index += count;
            }
        }
        index %= count;
        return adapter.getItem(index);
    }

    /**
     * 根据当前值构建text
     *
     * @param useCurrentValue
     * @return the text
     */
    private String buildText(boolean useCurrentValue) {
        StringBuilder itemsText = new StringBuilder();
        int addItems = mVisibleCount / 2 + 1;

        for (int i = mPosition - addItems; i <= mPosition + addItems; i++) {
            if (useCurrentValue || i != mPosition) {
                String text = getTextItem(i);
                if (text != null) {
                    itemsText.append(text);
                }
            }
            if (i < mPosition + addItems) {
                itemsText.append("\n");
            }
        }

        return itemsText.toString();
    }

    /**
     * 返回可以表示的item的最大长度
     *
     * @return the max length
     */
    private int getMaxTextLength() {
        WheelAdapter adapter = getAdapter();
        if (adapter == null) {
            return 0;
        }

        int adapterLength = adapter.getMaximumLength();
        if (adapterLength > 0) {
            return adapterLength;
        }

        String maxText = null;
        int addItems = mVisibleCount / 2;
        for (int i = Math.max(mPosition - addItems, 0);
             i < Math.min(mPosition + mVisibleCount, adapter.getItemsCount()); i++) {
            String text = adapter.getItem(i);
            if (text != null && (maxText == null || maxText.length() < text.length())) {
                maxText = text;
            }
        }

        return maxText != null ? maxText.length() : 0;
    }

    /**
     * 返回滚轮item的高度
     *
     * @return the item height
     */
    private int getItemHeight() {
        if (itemHeight != 0) {
            return itemHeight;
        } else if (itemsLayout != null && itemsLayout.getLineCount() > 2) {
            itemHeight = itemsLayout.getLineTop(2) - itemsLayout.getLineTop(1);
            return itemHeight;
        }

        return getHeight() / mVisibleCount;
    }

    /**
     * 计算控制宽度和创建text布局
     *
     * @param widthSize the input layout width
     * @param mode      the layout mode
     * @return the calculated control width
     */
    private int calculateLayoutWidth(int widthSize, int mode) {
        initResourceIfNecessary();

        int width;

        int maxLength = getMaxTextLength();
        if (maxLength > 0) {
            float textWidth = (float) Math.ceil(Layout.getDesiredWidth("0", itemsPaint));
            itemsWidth = (int) (maxLength * textWidth);
        } else {
            itemsWidth = 0;
        }
        itemsWidth += ADDITIONAL_ITEMS_SPACE; // make it some more
        labelWidth = 0;
        if (!TextUtils.isEmpty(mLabel)) {
            labelWidth = (int) Math.ceil(Layout.getDesiredWidth(mLabel, valuePaint));

        }

        boolean recalculate = false;
        if (mode == MeasureSpec.EXACTLY) {
            width = widthSize;
            recalculate = true;
        } else {
            width = itemsWidth + labelWidth + 2 * PADDING;
            if (labelWidth > 0) {
                width += LABEL_OFFSET;
            }

            // Check against our minimum width
            width = Math.max(width, getSuggestedMinimumWidth());

            if (mode == MeasureSpec.AT_MOST && widthSize < width) {
                width = widthSize;
                recalculate = true;
            }
        }

        if (recalculate) {
            // recalculate width
            int pureWidth = width - LABEL_OFFSET - 2 * PADDING;
            if (pureWidth <= 0) {
                itemsWidth = labelWidth = 0;
            }
            if (labelWidth > 0) {
                double newWidthItems = (double) itemsWidth * pureWidth
                        / (itemsWidth + labelWidth);
                itemsWidth = (int) newWidthItems;
                labelWidth = pureWidth - itemsWidth;
            } else {
                itemsWidth = pureWidth + LABEL_OFFSET; // no label
            }
        }

        if (itemsWidth > 0) {
            createLayouts(itemsWidth, labelWidth);
        }

        return width;
    }

    /**
     * 创建布局
     *
     * @param widthItems width of items layout
     * @param widthLabel width of label layout
     */
    private void createLayouts(int widthItems, int widthLabel) {
        if (itemsLayout == null || itemsLayout.getWidth() > widthItems) {
            itemsLayout = new StaticLayout(buildText(isScrollingPerformed), itemsPaint, widthItems,
                    widthLabel > 0 ? Layout.Alignment.ALIGN_OPPOSITE : Layout.Alignment.ALIGN_CENTER,
                    1, ADDITIONAL_ITEM_HEIGHT, false);
        } else {
            itemsLayout.increaseWidthTo(widthItems);
        }

        if (!isScrollingPerformed && (valueLayout == null || valueLayout.getWidth() > widthItems)) {
            String text = getAdapter() != null ? getAdapter().getItem(mPosition) : null;
            valueLayout = new StaticLayout(text != null ? text : "",
                    valuePaint, widthItems, widthLabel > 0 ?
                    Layout.Alignment.ALIGN_OPPOSITE : Layout.Alignment.ALIGN_CENTER,
                    1, ADDITIONAL_ITEM_HEIGHT, false);
        } else if (isScrollingPerformed) {
            valueLayout = null;
        } else {
            valueLayout.increaseWidthTo(widthItems);
        }

        if (widthLabel > 0) {
            if (labelLayout == null || labelLayout.getWidth() > widthLabel) {
                labelLayout = new StaticLayout(mLabel, valuePaint,
                        widthLabel, Layout.Alignment.ALIGN_NORMAL, 1,
                        ADDITIONAL_ITEM_HEIGHT, false);
            } else {
                labelLayout.increaseWidthTo(widthLabel);
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int width = calculateLayoutWidth(widthSize, widthMode);

        int height;
        if (heightMode == MeasureSpec.EXACTLY) {
            height = heightSize;
        } else {
            height = getDesiredHeight(itemsLayout);

            if (heightMode == MeasureSpec.AT_MOST) {
                height = Math.min(height, heightSize);
            }
        }

        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (itemsLayout == null) {
            if (itemsWidth == 0) {
                calculateLayoutWidth(getWidth(), MeasureSpec.EXACTLY);
            } else {
                createLayouts(itemsWidth, labelWidth);
            }
        }

        drawCenterRect(canvas);
        if (itemsWidth > 0) {
            canvas.save();
            // Skip padding space and hide a part of top and bottom items
            canvas.translate(PADDING, -getItemOffset());
            drawItems(canvas);
            drawValue(canvas);
            canvas.restore();
        }

        drawShadows(canvas);
    }

    /**
     * 在顶部和底部画阴影的控制
     *
     * @param canvas the canvas for drawing
     */
    private void drawShadows(Canvas canvas) {
        if (mTopShadow != null) {
            mTopShadow.setBounds(0, 0, getWidth(), getHeight() / mVisibleCount);
            mTopShadow.draw(canvas);
        }
        if (mBottomShadow != null) {
            mBottomShadow.setBounds(0, getHeight() - getHeight() / mVisibleCount,
                    getWidth(), getHeight());
            mBottomShadow.draw(canvas);
        }
    }

    /**
     * 画value和标签的布局
     *
     * @param canvas the canvas for drawing
     */
    private void drawValue(Canvas canvas) {
        valuePaint.setColor(mSelectColor);
        valuePaint.drawableState = getDrawableState();

        Rect bounds = new Rect();
        itemsLayout.getLineBounds(mVisibleCount / 2, bounds);

        // draw label
        if (labelLayout != null) {
            canvas.save();
            canvas.translate(itemsLayout.getWidth() + LABEL_OFFSET, bounds.top);
            labelLayout.draw(canvas);
            canvas.restore();
        }

        // draw current value
        if (valueLayout != null) {
            canvas.save();
            canvas.translate(0, bounds.top + scrollingOffset);
            valueLayout.draw(canvas);
            canvas.restore();
        }
    }

    /**
     * 画items
     *
     * @param canvas the canvas for drawing
     */
    private void drawItems(Canvas canvas) {
        canvas.save();

        int top = itemsLayout.getLineTop(1);
        canvas.translate(0, -top + scrollingOffset);

        itemsPaint.setColor(mColor);
        itemsPaint.drawableState = getDrawableState();
        itemsLayout.draw(canvas);

        canvas.restore();
    }

    /**
     * 画当前值的矩形
     *
     * @param canvas the canvas for drawing
     */
    private void drawCenterRect(Canvas canvas) {
        if (mSelectedBg != null) {
            int center = getHeight() / 2;
            int offset = getItemHeight() / 2;
            mSelectedBg.setBounds(0, center - offset, getWidth(), center + offset);
            mSelectedBg.draw(canvas);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        WheelAdapter adapter = getAdapter();
        if (adapter == null) {
            return true;
        }

        if (!gestureDetector.onTouchEvent(event) && event.getAction() == MotionEvent.ACTION_UP) {
            justify();
        }
        return true;
    }

    /**
     * 滚动滚轮
     *
     * @param delta the scrolling value
     */
    private void doScroll(int delta) {
        scrollingOffset += delta;

        int count = scrollingOffset / getItemHeight();
        int pos = mPosition - count;
        if (mIsCyclic && adapter.getItemsCount() > 0) {
            // fix position by rotating
            while (pos < 0) {
                pos += adapter.getItemsCount();
            }
            pos %= adapter.getItemsCount();
        } else if (isScrollingPerformed) {
            //
            if (pos < 0) {
                count = mPosition;
                pos = 0;
            } else if (pos >= adapter.getItemsCount()) {
                count = mPosition - adapter.getItemsCount() + 1;
                pos = adapter.getItemsCount() - 1;
            }
        } else {
            // fix position
            pos = Math.max(pos, 0);
            pos = Math.min(pos, adapter.getItemsCount() - 1);
        }

        int offset = scrollingOffset;
        if (pos != mPosition) {
            setPosition(pos, false);
        } else {
            invalidate();
        }

        // update offset
        scrollingOffset = offset - count * getItemHeight();
        if (scrollingOffset > getHeight()) {
            scrollingOffset = scrollingOffset % getHeight() + getHeight();
        }
    }

    // gesture listener
    private SimpleOnGestureListener gestureListener = new SimpleOnGestureListener() {
        public boolean onDown(MotionEvent e) {
            if (isScrollingPerformed) {
                scroller.forceFinished(true);
                clearMessages();
                return true;
            }
            return false;
        }

        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            startScrolling();
            doScroll((int) -distanceY);
            return true;
        }

        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            lastScrollY = mPosition * getItemHeight() + scrollingOffset;
            int maxY = mIsCyclic ? 0x7FFFFFFF : adapter.getItemsCount() * getItemHeight();
            int minY = mIsCyclic ? -maxY : 0;
            scroller.fling(0, lastScrollY, 0, (int) -velocityY / 2, 0, 0, minY, maxY);
            setNextMessage(MESSAGE_SCROLL);
            return true;
        }
    };


    // Messages
    private final int MESSAGE_SCROLL = 0;
    private final int MESSAGE_JUSTIFY = 1;

    /**
     * Set next message to queue. Clears queue before.
     *
     * @param message the message to set
     */
    private void setNextMessage(int message) {
        clearMessages();
        animationHandler.sendEmptyMessage(message);
    }

    /**
     * Clears messages from queue
     */
    private void clearMessages() {
        animationHandler.removeMessages(MESSAGE_SCROLL);
        animationHandler.removeMessages(MESSAGE_JUSTIFY);
    }

    // animation handler
    @SuppressLint("HandlerLeak")
    private Handler animationHandler = new Handler() {
        public void handleMessage(Message msg) {
            scroller.computeScrollOffset();
            int currY = scroller.getCurrY();
            int delta = lastScrollY - currY;
            lastScrollY = currY;
            if (delta != 0) {
                doScroll(delta);
            }

            // scrolling is not finished when it comes to final Y
            // so, finish it manually
            if (Math.abs(currY - scroller.getFinalY()) < MIN_DELTA_FOR_SCROLLING) {
                currY = scroller.getFinalY();
                scroller.forceFinished(true);
            }
            if (!scroller.isFinished()) {
                animationHandler.sendEmptyMessage(msg.what);
            } else if (msg.what == MESSAGE_SCROLL) {
                justify();
            } else {
                finishScrolling();
            }
        }
    };

    /**
     * Justifies wheel
     */
    private void justify() {
        if (adapter == null) {
            return;
        }

        lastScrollY = 0;
        int offset = scrollingOffset;
        int itemHeight = getItemHeight();
        boolean needToIncrease = offset > 0 ? mPosition < adapter.getItemsCount() : mPosition > 0;
        if ((mIsCyclic || needToIncrease) && Math.abs((float) offset) > (float) itemHeight / 2) {
            if (offset < 0)
                offset += itemHeight + MIN_DELTA_FOR_SCROLLING;
            else
                offset -= itemHeight + MIN_DELTA_FOR_SCROLLING;
        }
        if (Math.abs(offset) > MIN_DELTA_FOR_SCROLLING) {
            scroller.startScroll(0, 0, 0, offset, SCROLLING_DURATION);
            setNextMessage(MESSAGE_JUSTIFY);
        } else {
            finishScrolling();
        }
    }

    /**
     * 开始滚动
     */
    private void startScrolling() {
        if (!isScrollingPerformed) {
            isScrollingPerformed = true;
            notifyScrollingListenersAboutStart();
        }
    }

    /**
     * 停止滚动
     */
    void finishScrolling() {
        if (isScrollingPerformed) {
            notifyScrollingListenersAboutEnd();
            isScrollingPerformed = false;
        }
        invalidateLayouts();
        invalidate();
    }

    public void scroll(int itemsToScroll, int time) {
        scroller.forceFinished(true);

        lastScrollY = scrollingOffset;

        int offset = itemsToScroll * getItemHeight();

        scroller.startScroll(0, lastScrollY, 0, offset - lastScrollY, time);
        setNextMessage(MESSAGE_SCROLL);

        startScrolling();
    }


    public interface WheelAdapter {
        /**
         * Gets items count
         *
         * @return the count of wheel items
         */
        int getItemsCount();

        /**
         * Gets a wheel item by index.
         *
         * @param index the item index
         * @return the wheel item text or null
         */
        String getItem(int index);

        /**
         * Gets maximum item length. It is used to determine the wheel width.
         * If -1 is returned there will be used the default wheel width.
         *
         * @return the maximum item length or -1
         */
        int getMaximumLength();
    }

    public interface OnWheelChangedListener {
        /**
         * Callback method to be invoked when current item changed
         *
         * @param wheel    the wheel view whose state has changed
         * @param oldValue the old value of current item
         * @param newValue the new value of current item
         */
        void onChanged(WheelView wheel, int oldValue, int newValue);
    }

    public interface OnWheelScrollListener {
        /**
         * Callback method to be invoked when scrolling started.
         *
         * @param wheel the wheel view whose state has changed.
         */
        void onScrollingStarted(WheelView wheel);

        /**
         * Callback method to be invoked when scrolling ended.
         *
         * @param wheel the wheel view whose state has changed.
         */
        void onScrollingFinished(WheelView wheel);
    }

}