package com.example.birthday;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;

import com.example.birthday.dateselector.CustomDatePicker;
import com.example.birthday.dateselector.DateFormatUtils;
import com.example.birthday.dateselector.DrawClock;
import com.example.birthday.surprise.StarsFallView;

public class StartActivity extends Activity implements View.OnClickListener {

    private CustomDatePicker mDatePicker;
    private TextView mTvSelectedDate;
    private DrawClock mClock;
    private Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start_layout);

        findViewById(R.id.choose_date_btn).setOnClickListener(this);
        findViewById(R.id.next_btn).setOnClickListener(this);
        mTvSelectedDate = findViewById(R.id.input_date);
        initDateSelector();

        mClock = new DrawClock(this,null);
        mHandler = new Handler();
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.choose_date_btn:
                //日期格式为yyyy-MM-dd
                mDatePicker.show(mTvSelectedDate.getText().toString());
                break;

            case R.id.next_btn:
                //日期格式为yyyy-MM-dd
                Intent i = new Intent(StartActivity.this , StarsFallView.class);
                //启动
                startActivity(i);
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDatePicker.onDestroy();
    }

    private void initDateSelector() {
        long beginTimestamp = DateFormatUtils.str2Long("1971-01-01", false);
        long endTimestamp = System.currentTimeMillis();

        mTvSelectedDate.setText(DateFormatUtils.long2Str(endTimestamp, false));

        //通过时间戳初始化日期
        mDatePicker = new CustomDatePicker(this, new CustomDatePicker.Callback() {
            @Override
            public void onTimeSelected(long timestamp) {
                mTvSelectedDate.setText(DateFormatUtils.long2Str(timestamp, false));
            }
        }, beginTimestamp, endTimestamp);

        //不允许点击屏幕或物理返回键关闭
        mDatePicker.setCancelable(false);
        //不允许循环滚动
        mDatePicker.setScrollLoop(false);
        //不允许滚动动画
        mDatePicker.setCanShowAnim(false);
    }

    private Runnable updateTime = new Runnable() {
        @Override
        public void run() {
            mClock.refreshClock();
            mHandler.postDelayed(updateTime, 1000);
        }
    };
}
