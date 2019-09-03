package com.example.birthday.dateselector;

import android.app.Dialog;
import android.content.Context;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.example.birthday.R;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class CustomDatePicker implements View.OnClickListener, DateSelectorView.OnSelectListener {

    private boolean mCanDialogShow;
    private Callback mCallback;
    private Context mContext;
    //初始化时用，自定义的时间边界
    private Calendar mBeginTime, mEndTime, mSelectedTime;

    private Dialog mPickerDialog;
    private DateSelectorView mDpvYear, mDpvMonth, mDpvDay ,mDpvDaymDpvHour, mDpvMinute;   //选择器中被选中的值
    private TextView mTvHourUnit, mTvMinuteUnit;

    //自定义时间边界，由上面得出
    private int mBeginYear, mBeginMonth, mBeginDay, mBeginHour, mBeginMinute,
            mEndYear, mEndMonth, mEndDay, mEndHour, mEndMinute;
    private List<String> mYearUnits = new ArrayList<>(), mMonthUnits = new ArrayList<>(), mDayUnits = new ArrayList<>(),
            mHourUnits = new ArrayList<>(), mMinuteUnits = new ArrayList<>();
    //格式化十进制数字，以下用于占位
    private DecimalFormat mDecimalFormat = new DecimalFormat("00");

    private boolean mCanShowPreciseTime;

    private int mScrollUnits = SCROLL_UNIT_HOUR + SCROLL_UNIT_MINUTE;

    private static final long LINKAGE_DELAY_DEFAULT = 100L;    //连级滚动延迟时间
    private static final int MAX_MONTH_UNIT = 12;    //最大显示值
    private static final int MAX_HOUR_UNIT = 23;
    private static final int MAX_MINUTE_UNIT = 59;

    /**
     * 时间单位：时、分
     */
    private static final int SCROLL_UNIT_HOUR = 0b1;
    private static final int SCROLL_UNIT_MINUTE = 0b10;

    @Override
    public void onSelect(View view, String selected) {
        if(view == null || TextUtils.isEmpty(selected)) return;

        int timeUnit;
        try{
            timeUnit = Integer.parseInt(selected);
        }catch(Throwable ignored){
            return;
        }

        switch(view.getId()){
            case R.id.ds_year:
                mSelectedTime.set(Calendar.YEAR, timeUnit);
                linkageMonthUnit(true, LINKAGE_DELAY_DEFAULT);
                break;

            case R.id.ds_month:
                //防止类似2000/12/31 滚动到11月时因溢出变成 2010/12/01
                int lastSelectedMonth = mSelectedTime.get(Calendar.MONTH) + 1;
                mSelectedTime.add(Calendar.MONTH, timeUnit - lastSelectedMonth);
                linkageDayUnit(true, LINKAGE_DELAY_DEFAULT);
                break;

            case R.id.ds_day:
                mSelectedTime.set(Calendar.DAY_OF_MONTH, timeUnit);
                break;
        }
    }

    public void onDestroy() {
        if (mPickerDialog != null) {
            mPickerDialog.dismiss();
            mPickerDialog = null;

            mDpvYear.onDestroy();
            mDpvMonth.onDestroy();
            mDpvDay.onDestroy();
        }
    }

    /**
    * @description 时间选择结果回掉接口
    * @param
    * @return
    * @author snow
    * @time 2019/7/16 12:21
    */
    public interface Callback{
        void onTimeSelected(long timestamp);
    }

    /**
    * @description 设置是否允许点击屏幕或物理返回键关闭
    * @param cancelable
    * @return
    * @author snow
    * @time 2019/7/16 17:35
    */
    public void setCancelable(boolean cancelable) {
        if(!canShow()) return;

        mPickerDialog.setCancelable(cancelable);
    }

    /**
    * @description 设置日期控件是否可以循环滚动
    * @param
    * @return
    * @author snow
    * @time 2019/7/16 19:11
    */
    public void setScrollLoop(boolean canLoop){
        if (!canShow()) return;

        mDpvYear.setCanScrollLoop(canLoop);
        mDpvMonth.setCanScrollLoop(canLoop);
        mDpvDay.setCanScrollLoop(canLoop);
    }

    /**
    * @description 设置日期控件是否展示滚动动画
    * @param
    * @return
    * @author snow
    * @time 2019/7/16 19:15
    */
    public void setCanShowAnim(boolean canShowAnim){
        if (!canShow()) return;

        mDpvYear.setCanShowAnim(canShowAnim);
        mDpvMonth.setCanShowAnim(canShowAnim);
        mDpvDay.setCanShowAnim(canShowAnim);
    }

    /**
    * @description 通过日期字符串初始化时间选择器
    * @param context Activity Context
     * @param callback 选择结果回调
     * @param beginDateStr 日期字符串，格式为yyyy-MM-dd HH:mm
     * @param endDateStr 日期字符串，格式为yyyy-MM-dd HH:mm
    * @return
    * @author snow
    * @time 2019/7/16 12:25
    */
    public CustomDatePicker(Context context, Callback callback, String beginDateStr, String endDateStr){
        this(context, callback, DateFormatUtils.str2Long(beginDateStr, true), DateFormatUtils.str2Long(endDateStr, true));
    }

    /**
    * @description 通过时间戳初始化时间选择器
     * @param context Activity Context
     * @param callback 选择结果回调
     * @param beginTimestamp 毫秒级时间戳
     * @param endTimestamp 毫秒级时间戳
    * @return
    * @author snow
    * @time 2019/7/16 15:43
    */
    public CustomDatePicker(Context context, Callback callback, long beginTimestamp, long endTimestamp) {
        if(context == null || callback == null || beginTimestamp <= 0 || beginTimestamp >= endTimestamp){
            mCanDialogShow = false;
            return;
        }

        mContext = context;
        mCallback = callback;
        mBeginTime = Calendar.getInstance();
        mBeginTime.setTimeInMillis(beginTimestamp);
        mEndTime = Calendar.getInstance();
        mEndTime.setTimeInMillis(endTimestamp);
        mSelectedTime = Calendar.getInstance();

        initView();
        initData();
        mCanDialogShow = true;
    }

    private void initData() {
        mSelectedTime.setTimeInMillis(mBeginTime.getTimeInMillis());

        mBeginYear = mBeginTime.get(Calendar.YEAR);
        // Calendar.MONTH 值为 0-11
        mBeginMonth = mBeginTime.get(Calendar.MONTH) + 1;
        mBeginDay = mBeginTime.get(Calendar.DAY_OF_MONTH);

        mEndYear = mEndTime.get(Calendar.YEAR);
        mEndMonth = mEndTime.get(Calendar.MONTH) + 1;
        mEndDay = mEndTime.get(Calendar.DAY_OF_MONTH);

        boolean canSpanYear = mBeginYear != mEndYear;
        boolean canSpanMon = !canSpanYear && mBeginMonth != mEndMonth;
        boolean canSpanDay = !canSpanMon && mBeginDay != mEndDay;

        if (canSpanYear) {
            initDateUnits(MAX_MONTH_UNIT, mBeginTime.getActualMaximum(Calendar.DAY_OF_MONTH), MAX_HOUR_UNIT, MAX_MINUTE_UNIT);
        } else if (canSpanMon) {
            initDateUnits(mEndMonth, mBeginTime.getActualMaximum(Calendar.DAY_OF_MONTH), MAX_HOUR_UNIT, MAX_MINUTE_UNIT);
        } else if (canSpanDay) {
            initDateUnits(mEndMonth, mEndDay, MAX_HOUR_UNIT, MAX_MINUTE_UNIT);
        }
    }

    private void initDateUnits(int endMonth, int endDay, int endHour, int endMinute){
        for (int i = mBeginYear; i <= mEndYear; i++) {
            mYearUnits.add(String.valueOf(i));
        }

        for (int i = mBeginMonth; i <= endMonth; i++) {
            mMonthUnits.add(mDecimalFormat.format(i));
        }

        for (int i = mBeginDay; i <= endDay; i++) {
            mDayUnits.add(mDecimalFormat.format(i));
        }

        mDpvYear.setDataList(mYearUnits);
        mDpvYear.setSelected(0);
        mDpvMonth.setDataList(mMonthUnits);
        mDpvMonth.setSelected(0);
        mDpvDay.setDataList(mDayUnits);
        mDpvDay.setSelected(0);

        setCanScroll();
    }

    private void initView() {
        mPickerDialog = new Dialog(mContext, R.style.dialog);
        mPickerDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mPickerDialog.setContentView(R.layout.date_selector);

        //Dialog的设置
        Window window = mPickerDialog.getWindow();
        if(window != null){
            WindowManager.LayoutParams lp = window.getAttributes();
            lp.gravity = Gravity.CENTER;
            lp.width = WindowManager.LayoutParams.MATCH_PARENT;
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
            window.setAttributes(lp);
        }

        mPickerDialog.findViewById(R.id.ds_cancel).setOnClickListener(this);
        mPickerDialog.findViewById(R.id.ds_commit).setOnClickListener(this);

        mDpvYear = mPickerDialog.findViewById(R.id.ds_year);
        mDpvYear.setOnSelectListener(this);
        mDpvMonth = mPickerDialog.findViewById(R.id.ds_month);
        mDpvMonth.setOnSelectListener(this);
        mDpvDay = mPickerDialog.findViewById(R.id.ds_day);
        mDpvDay.setOnSelectListener(this);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.ds_cancel:
                break;

            case R.id.ds_commit:
                if(mCallback != null){
                    mCallback.onTimeSelected(mSelectedTime.getTimeInMillis());
                }
                break;
        }

        if(mPickerDialog != null && mPickerDialog.isShowing()){
            mPickerDialog.dismiss();
        }
    }
    
    /**
    * @description 展示时间选择器
    * @param dateStr 日期字符串，格式为yyyy-MM-dd 或 yyyy-MM-dd HH:mm
    * @return null
    * @author snow
    * @time 2019/7/2 11:15
    */
    public void show(String dateStr) {
        if(!canShow() || TextUtils.isEmpty(dateStr)) return;

        //弹窗
        if(setSelectedTime(dateStr, false)){
            mPickerDialog.show();
        }
    }

    /**
    * @description 设置日期选择器的选中时间
    * @param dateStr 日期字符串
     *  @param  showAnim 是否展示动画
    * @return 是否设置成功
    * @author snow
    * @time 2019/7/2 11:25
    */
    private boolean setSelectedTime(String dateStr, boolean showAnim) {
        return canShow() && !TextUtils.isEmpty(dateStr) && setSelectedTime(DateFormatUtils.str2Long(dateStr, mCanShowPreciseTime), showAnim);
    }

    private boolean canShow() {
        return mCanDialogShow && mPickerDialog != null;
    }

    /**
    * @description 设置日期选择器的选中时间
    * @param timestamp 毫秒级时间戳
     * @param showAnim 是否展示动画
    * @return 是否设置成功
    * @author snow
    * @time 2019/7/2 17:50
    */
    public boolean setSelectedTime(long timestamp, boolean showAnim){
        if(!canShow()) return false;

        //时间戳区间
        if(timestamp < mBeginTime.getTimeInMillis()){
            timestamp = mBeginTime.getTimeInMillis();
        }else if(timestamp > mEndTime.getTimeInMillis()){
            timestamp = mEndTime.getTimeInMillis();
        }
        mSelectedTime.setTimeInMillis(timestamp);

        mYearUnits.clear();
        for(int i = mBeginYear; i <= mEndYear; i++){
            mYearUnits.add(String.valueOf(i));
        }

        mDpvYear.setDataList(mYearUnits);
        mDpvYear.setSelected(mSelectedTime.get(Calendar.YEAR) - mBeginYear);
        linkageMonthUnit(showAnim, showAnim ? LINKAGE_DELAY_DEFAULT : 0);
        return true;
    }

    /**
    * @description 联动“月”变化
    * @param showAnim 是否展示滚动动画
     * @param delay 联动下一级延迟时间
    * @return
    * @author snow
    * @time 2019/7/15 11:58
    */
    private void linkageMonthUnit(final boolean showAnim, final long delay){
        int minMonth;
        int maxMonth;
        int selectedYear = mSelectedTime.get(Calendar.YEAR);
        if(mBeginYear == mEndYear){    //同一年之间选择
            minMonth = mBeginMonth;
            maxMonth = mEndMonth;
        }else if(selectedYear == mBeginYear){    //上边界
            minMonth = mBeginMonth;
            maxMonth = MAX_MONTH_UNIT;
        }else if(selectedYear == mEndYear){    //下边界
            minMonth = 1;
            maxMonth = mEndMonth;
        }else{
            minMonth = 1;
            maxMonth = MAX_MONTH_UNIT;
        }

        //重新初始化时间单元容器
        mMonthUnits.clear();
        for(int i = minMonth; i <= maxMonth; i++){
            mMonthUnits.add(mDecimalFormat.format(i));
        }
        mDpvMonth.setDataList(mMonthUnits);

        //确保联动时不会溢出或改变关联选中值
        int selectedMonth = getValueInRange(mSelectedTime.get(Calendar.MONTH) + 1, minMonth, maxMonth);
        mSelectedTime.set(Calendar.MONTH, selectedMonth - 1);
        mDpvMonth.setSelected(selectedMonth - minMonth);   //传入索引值
        if(showAnim){
            mDpvMonth.startAnim();
        }

        //联动“日”变化
        mDpvMonth.postDelayed(() ->{linkageDayUnit(showAnim, delay);},delay);
    }

    /**
    * @description 联动“日”变化
    * @param showAnim 是否展示滚动动画
     * @param delay 联动下一级延迟时间
    * @return
    * @author snow
    * @time 2019/7/15 17:58
    */
    private void linkageDayUnit(final boolean showAnim, final long delay) {
        int minDay;
        int maxDay;
        int selectedYear = mSelectedTime.get(Calendar.YEAR);
        int selectedMonth = mSelectedTime.get(Calendar.MONTH) + 1;    //为什么要加一？？？？
        if(mBeginYear == mEndYear){
            minDay = mBeginDay;
            maxDay = mEndDay;
        }else if(selectedYear == mBeginYear && selectedMonth == mBeginMonth){
            minDay = mBeginDay;
            maxDay = mSelectedTime.getActualMaximum(Calendar.DAY_OF_MONTH);
        }else if(selectedYear == mEndYear && selectedMonth == mEndMonth){
            minDay = 1;
            maxDay = mEndDay;
        }else{
            minDay = 1;
            maxDay = mSelectedTime.getActualMaximum(Calendar.DAY_OF_MONTH);
        }

        mDayUnits.clear();
        for(int i = minDay; i <= maxDay; i++){
            mDayUnits.add(mDecimalFormat.format(i));
        }
        mDpvDay.setDataList(mDayUnits);

        int selectedDay = getValueInRange(mSelectedTime.get(Calendar.DAY_OF_MONTH), minDay, maxDay);
        mSelectedTime.set(Calendar.DAY_OF_MONTH, selectedDay);
        mDpvDay.setSelected(selectedDay - minDay);
        if(showAnim){
            mDpvDay.startAnim();
        }

        setCanScroll();
    }

    private void setCanScroll() {
        mDpvYear.setCanScroll(mYearUnits.size() > 1);
        mDpvMonth.setCanScroll(mMonthUnits.size() > 1);
        mDpvDay.setCanScroll(mDayUnits.size() > 1);
    }

    private int getValueInRange(int value, int minValue, int maxValue){
        if(value < minValue){
            return minValue;
        }else if(value > maxValue){
            return maxValue;
        }else{
            return value;
        }
    }
}
