package com.tuuzed.androidx.datepicker;


import android.annotation.SuppressLint;
import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@SuppressLint("ShiftFlags")
@IntDef(flag = true, value = {
        DatePickerType.TYPE_YMDHM,
        DatePickerType.TYPE_YMDH,
        DatePickerType.TYPE_YMD,
        DatePickerType.TYPE_YM,
        DatePickerType.TYPE_Y,
        DatePickerType.TYPE_HM,
})
@Retention(RetentionPolicy.SOURCE)
public @interface DatePickerType {
    int TYPE_YMDHM = 1;
    int TYPE_YMDH = 2;
    int TYPE_YMD = 3;
    int TYPE_YM = 4;
    int TYPE_Y = 5;
    int TYPE_HM = 6;
}
