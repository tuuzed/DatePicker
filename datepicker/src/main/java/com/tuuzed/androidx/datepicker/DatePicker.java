package com.tuuzed.androidx.datepicker;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.tuuzed.androidx.datepicker.internal.NumericWheelAdapter;
import com.tuuzed.androidx.datepicker.internal.Utils;
import com.tuuzed.androidx.datepicker.internal.WheelView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DatePicker extends FrameLayout {

    @NonNull
    public static DateFormat getDateFormat(@DatePickerType int type) {
        switch (type) {
            case DatePickerType.TYPE_HM:
                return new SimpleDateFormat("hh:mm", Locale.getDefault());
            case DatePickerType.TYPE_YMDHM:
                return new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            case DatePickerType.TYPE_YMDH:
                return new SimpleDateFormat("yyyy-MM-dd HH", Locale.getDefault());
            case DatePickerType.TYPE_YMD:
                return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            case DatePickerType.TYPE_YM:
                return new SimpleDateFormat("yyyy-MM", Locale.getDefault());
            case DatePickerType.TYPE_Y:
                return new SimpleDateFormat("yyyy", Locale.getDefault());
            default:
                return new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        }
    }

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
    private int mDatePickerType;
    private int mTextColor = Color.BLACK;

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
        mDatePickerType = a.getInteger(R.styleable.DatePicker_dp_datePickerType, DatePickerType.TYPE_YMDHM);
        mTextColor = a.getColor(R.styleable.DatePicker_dp_textColor, Color.BLACK);
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
        mWvYear.addChangingListener(new WheelView.OnWheelChangedListener() {
            @Override
            public void onChanged(WheelView wheel, int oldValue, int newValue) {
                notifyDayAdapterChange();
                mCalendar.set(Calendar.YEAR, getYear());
                if (mOnDateChangedListener != null) {
                    mOnDateChangedListener.onDateChanged(mCalendar.getTime());
                }
            }
        });
        mWvYear.setAdapter(new NumericWheelAdapter(mMinYear, mMaxYear));
        // 月
        mWvMonth.setCyclic(true);
        mWvMonth.addChangingListener(new WheelView.OnWheelChangedListener() {
            @Override
            public void onChanged(WheelView wheel, int oldValue, int newValue) {
                notifyDayAdapterChange();
                mCalendar.set(Calendar.MONTH, getMonth() - 1);
                if (mOnDateChangedListener != null) {
                    mOnDateChangedListener.onDateChanged(mCalendar.getTime());
                }
            }
        });
        mWvMonth.setAdapter(new NumericWheelAdapter(1, 12));
        // 日
        mWvDay.setCyclic(true);
        mWvDay.addChangingListener(new WheelView.OnWheelChangedListener() {
            @Override
            public void onChanged(WheelView wheel, int oldValue, int newValue) {
                mCalendar.set(Calendar.DATE, getDay());
                if (mOnDateChangedListener != null) {
                    mOnDateChangedListener.onDateChanged(mCalendar.getTime());
                }
            }
        });
        // 时
        mWvHour.setAdapter(new NumericWheelAdapter(0, 23));
        mWvHour.setCyclic(true);
        mWvHour.addChangingListener(new WheelView.OnWheelChangedListener() {
            @Override
            public void onChanged(WheelView wheel, int oldValue, int newValue) {
                mCalendar.set(Calendar.HOUR_OF_DAY, getHour());
                if (mOnDateChangedListener != null) {
                    mOnDateChangedListener.onDateChanged(mCalendar.getTime());
                }
            }
        });

        mWvDay.setAdapter(new NumericWheelAdapter(1, Utils.getLastDayByYearMonth(getYear(), getMonth())));
        // 分
        mWvMinute.setCyclic(true);
        mWvMinute.addChangingListener(new WheelView.OnWheelChangedListener() {
            @Override
            public void onChanged(WheelView wheel, int oldValue, int newValue) {
                mCalendar.set(Calendar.MINUTE, getMinute());
                if (mOnDateChangedListener != null) {
                    mOnDateChangedListener.onDateChanged(mCalendar.getTime());
                }
            }
        });
        mWvMinute.setAdapter(new NumericWheelAdapter(0, 59));

        setTextColor(mTextColor);
        // 设置日期
        setDate(new Date());
        setDatePickerType(mDatePickerType);
    }


    public void setTextColor(@ColorInt int color) {
        mTextColor = color;
        int alphaColor = Utils.getAlphaColor(color, 0x50);
        mWvYear.setColor(alphaColor);
        mWvMonth.setColor(alphaColor);
        mWvDay.setColor(alphaColor);
        mWvHour.setColor(alphaColor);
        mWvMinute.setColor(alphaColor);

        mWvYear.setSelectColor(color);
        mWvMonth.setSelectColor(color);
        mWvDay.setSelectColor(color);
        mWvHour.setSelectColor(color);
        mWvMinute.setSelectColor(color);

        mTvYear.setTextColor(alphaColor);
        mTvMonth.setTextColor(alphaColor);
        mTvDay.setTextColor(alphaColor);
        mTvHour.setTextColor(alphaColor);
        mTvMinute.setTextColor(alphaColor);
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

    public void setDatePickerType(@DatePickerType int type) {
        mDatePickerType = type;
        switch (type) {
            case DatePickerType.TYPE_YMDHM:
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
    public int getDatePickerType() {
        return mDatePickerType;
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
