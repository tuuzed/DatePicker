package com.tuuzed.androidx.datepicker;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.tuuzed.androidx.datepicker.internal.NumericWheelAdapter;
import com.tuuzed.androidx.datepicker.internal.Utils;
import com.tuuzed.androidx.datepicker.internal.WheelView;

import java.util.Calendar;
import java.util.Date;

public class DatePicker extends FrameLayout {

    private WheelView mWvYear;
    private WheelView mWvMonth;
    private WheelView mWvDay;
    private WheelView mWvHour;
    private WheelView mWvMinute;
    private TextView mTvYear;
    private TextView mTvMonth;
    private TextView mTvDay;
    private TextView mTvHour;
    private TextView mTvMinute;
    @Nullable
    private OnDateChangedListener mOnDateChangedListener;

    private Calendar mCalendar;

    // 自定义属性
    private int mMinYear;
    private int mMaxYear;
    private int mType;
    private int mSelectColor = Color.BLACK;
    private int mTextSize;

    public DatePicker(Context context) {
        this(context, null);
    }

    public DatePicker(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DatePicker(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void init(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        mCalendar = Calendar.getInstance();

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.DatePicker);
        mMinYear = a.getInteger(R.styleable.DatePicker_dp_minYear, 1970);
        mMaxYear = a.getInteger(R.styleable.DatePicker_dp_maxYear, 2100);
        mType = a.getInteger(R.styleable.DatePicker_dp_type, DatePickerType.TYPE_ALL);
        mSelectColor = a.getColor(R.styleable.DatePicker_dp_selectColor, Color.BLACK);
        mTextSize = (int) a.getDimension(R.styleable.DatePicker_dp_textSize, Utils.sp2px(context, 14));
        a.recycle();

        inflate(context, R.layout.widget_datepicker, this);
        mWvYear = findViewById(R.id.wv_year);
        mWvMonth = findViewById(R.id.wv_month);
        mWvDay = findViewById(R.id.wv_day);
        mWvHour = findViewById(R.id.wv_hour);
        mWvMinute = findViewById(R.id.wv_minute);

        mTvYear = findViewById(R.id.tv_year);
        mTvMonth = findViewById(R.id.tv_month);
        mTvDay = findViewById(R.id.tv_day);
        mTvHour = findViewById(R.id.tv_hour);
        mTvMinute = findViewById(R.id.tv_minute);

        // 年
        mWvYear.setCyclic(false);
        mWvYear.setSelectColor(mSelectColor);
        mWvYear.setTextSize(mTextSize);
        mWvYear.addChangingListener((wheel, oldValue, newValue) -> {
            notifyDayAdapterChange();
            mCalendar.set(Calendar.YEAR, getYear());
            if (mOnDateChangedListener != null) {
                mOnDateChangedListener.onDateChanged(mCalendar.getTime());
            }
        });
        mWvYear.setAdapter(new NumericWheelAdapter(mMinYear, mMaxYear));
        // 月
        mWvMonth.setCyclic(true);
        mWvMonth.setSelectColor(mSelectColor);
        mWvMonth.setTextSize(mTextSize);
        mWvMonth.addChangingListener((wheel, oldValue, newValue) -> {
            notifyDayAdapterChange();
            mCalendar.set(Calendar.MONTH, getMonth() - 1);
            if (mOnDateChangedListener != null) {
                mOnDateChangedListener.onDateChanged(mCalendar.getTime());
            }
        });
        mWvMonth.setAdapter(new NumericWheelAdapter(1, 12));
        // 日
        mWvDay.setCyclic(true);
        mWvDay.setSelectColor(mSelectColor);
        mWvDay.setTextSize(mTextSize);
        mWvDay.addChangingListener((wheel, oldValue, newValue) -> {
            mCalendar.set(Calendar.DATE, getDay());
            if (mOnDateChangedListener != null) {
                mOnDateChangedListener.onDateChanged(mCalendar.getTime());
            }
        });
        // 时
        mWvHour.setAdapter(new NumericWheelAdapter(0, 23));
        mWvHour.setCyclic(true);
        mWvHour.setSelectColor(mSelectColor);
        mWvHour.setTextSize(mTextSize);
        mWvHour.addChangingListener((wheel, oldValue, newValue) -> {
            mCalendar.set(Calendar.HOUR_OF_DAY, getHour());
            if (mOnDateChangedListener != null) {
                mOnDateChangedListener.onDateChanged(mCalendar.getTime());
            }
        });
        mWvDay.setAdapter(new NumericWheelAdapter(1, Utils.getLastDayByYearMonth(getYear(), getMonth())));
        // 分
        mWvMinute.setCyclic(true);
        mWvMinute.setSelectColor(mSelectColor);
        mWvMinute.setTextSize(mTextSize);
        mWvMinute.addChangingListener((wheel, oldValue, newValue) -> {
            mCalendar.set(Calendar.MINUTE, getMinute());
            if (mOnDateChangedListener != null) {
                mOnDateChangedListener.onDateChanged(mCalendar.getTime());
            }
        });
        mWvMinute.setAdapter(new NumericWheelAdapter(0, 59));

        // 设置日期
        setDate(new Date());
        setType(mType);
    }

    public void setMaxYear(int maxYear) {
        mMaxYear = maxYear;
        mWvYear.setAdapter(new NumericWheelAdapter(mMinYear, mMaxYear));
        notifyDayAdapterChange();
    }

    public void setMinYear(int minYear) {
        mMinYear = minYear;
        mWvYear.setAdapter(new NumericWheelAdapter(mMinYear, mMaxYear));
        notifyDayAdapterChange();
    }

    public void setType(@DatePickerType int type) {
        switch (type) {
            case DatePickerType.TYPE_ALL:
                mWvYear.setVisibility(VISIBLE);
                mWvMonth.setVisibility(VISIBLE);
                mWvDay.setVisibility(VISIBLE);
                mWvHour.setVisibility(VISIBLE);
                mWvMinute.setVisibility(VISIBLE);

                mTvYear.setVisibility(VISIBLE);
                mTvMonth.setVisibility(VISIBLE);
                mTvDay.setVisibility(VISIBLE);
                mTvHour.setVisibility(VISIBLE);
                mTvMinute.setVisibility(VISIBLE);
                break;
            case DatePickerType.TYPE_YMDH:
                mWvYear.setVisibility(VISIBLE);
                mWvMonth.setVisibility(VISIBLE);
                mWvDay.setVisibility(VISIBLE);
                mWvHour.setVisibility(VISIBLE);
                mWvMinute.setVisibility(GONE);

                mTvYear.setVisibility(VISIBLE);
                mTvMonth.setVisibility(VISIBLE);
                mTvDay.setVisibility(VISIBLE);
                mTvHour.setVisibility(VISIBLE);
                mTvMinute.setVisibility(GONE);
                break;
            case DatePickerType.TYPE_YMD:
                mWvYear.setVisibility(VISIBLE);
                mWvMonth.setVisibility(VISIBLE);
                mWvDay.setVisibility(VISIBLE);
                mWvHour.setVisibility(GONE);
                mWvMinute.setVisibility(GONE);

                mTvYear.setVisibility(VISIBLE);
                mTvMonth.setVisibility(VISIBLE);
                mTvDay.setVisibility(VISIBLE);
                mTvHour.setVisibility(GONE);
                mTvMinute.setVisibility(GONE);
                break;
            case DatePickerType.TYPE_YM:
                mWvYear.setVisibility(VISIBLE);
                mWvMonth.setVisibility(VISIBLE);
                mWvDay.setVisibility(GONE);
                mWvHour.setVisibility(GONE);
                mWvMinute.setVisibility(GONE);

                mTvYear.setVisibility(VISIBLE);
                mTvMonth.setVisibility(VISIBLE);
                mTvDay.setVisibility(GONE);
                mTvHour.setVisibility(GONE);
                mTvMinute.setVisibility(GONE);
                break;
            case DatePickerType.TYPE_Y:
                mWvYear.setVisibility(VISIBLE);
                mWvMonth.setVisibility(GONE);
                mWvDay.setVisibility(GONE);
                mWvHour.setVisibility(GONE);
                mWvMinute.setVisibility(GONE);

                mTvYear.setVisibility(VISIBLE);
                mTvMonth.setVisibility(GONE);
                mTvDay.setVisibility(GONE);
                mTvHour.setVisibility(GONE);
                mTvMinute.setVisibility(GONE);
                break;
            case DatePickerType.TYPE_HM:
                mWvYear.setVisibility(GONE);
                mWvMonth.setVisibility(GONE);
                mWvDay.setVisibility(GONE);
                mWvHour.setVisibility(VISIBLE);
                mWvMinute.setVisibility(VISIBLE);

                mTvYear.setVisibility(GONE);
                mTvMonth.setVisibility(GONE);
                mTvDay.setVisibility(GONE);
                mTvHour.setVisibility(VISIBLE);
                mTvMinute.setVisibility(VISIBLE);
                break;
        }
    }

    @DatePickerType
    public int getType() {
        return mType;
    }

    public void setDate(@NonNull Date date) {
        mCalendar.setTime(date);
        setDate(mCalendar.get(Calendar.YEAR),
                mCalendar.get(Calendar.MONTH) + 1,
                mCalendar.get(Calendar.DATE),
                mCalendar.get(Calendar.HOUR_OF_DAY),
                mCalendar.get(Calendar.MINUTE)
        );
    }

    @NonNull
    public Date getDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(getYear(), getMonth() - 1, getDay(), getHour(), getMinute());
        return calendar.getTime();
    }

    public void setOnDateChangedListener(OnDateChangedListener listener) {
        mOnDateChangedListener = listener;
    }

    private int getYear() {
        return mMinYear + mWvYear.getPosition();
    }

    private int getMonth() {
        return 1 + mWvMonth.getPosition();
    }

    private int getDay() {
        return 1 + mWvDay.getPosition();
    }

    private int getHour() {
        //noinspection PointlessArithmeticExpression
        return 0 + mWvHour.getPosition();
    }

    private int getMinute() {
        //noinspection PointlessArithmeticExpression
        return 0 + mWvMinute.getPosition();
    }

    private void setDate(int year, int month, int day, int hour, int minute) {
        mWvYear.setPosition(year - mMinYear);
        mWvMonth.setPosition(month - 1);
        mWvDay.setPosition(day - 1);
        mWvHour.setPosition(hour);
        mWvMinute.setPosition(minute);
        notifyDayAdapterChange();
    }


    private void notifyDayAdapterChange() {
        int itemsCount = mWvDay.getAdapter().getItemsCount();
        int currentItem = mWvDay.getPosition();
        int year = getYear();
        int month = getMonth();
        int maxDay = Utils.getLastDayByYearMonth(year, month);
        mWvDay.setAdapter(new NumericWheelAdapter(1, maxDay));
        if (mWvDay.getAdapter().getItemsCount() != itemsCount) {
            if (currentItem > mWvDay.getAdapter().getItemsCount()) {
                mWvDay.setPosition(mWvDay.getAdapter().getItemsCount());
            } else {
                mWvDay.setPosition(currentItem);
            }
        }
    }

    public interface OnDateChangedListener {
        void onDateChanged(@NonNull Date newDate);
    }

}
