package com.example.birthday.dateselector;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @class name：com.example.birthday.dateselector
 * @description:
 * @anthor: snow
 * @time: 2019/7/31 19:18
 * @modification_time:
 * @modifier:
 */
public class DrawClock extends View {
    DisplayMetrics dm = getResources().getDisplayMetrics();
    int mWidth = dm.widthPixels;
    int mHeight = dm.heightPixels;

    Calendar mCalendar = Calendar.getInstance();
    long mMillisecond = System.currentTimeMillis();    //获取当前时间，单位毫秒
    int mSecond = mCalendar.get(Calendar.SECOND);
    int mMinute = mCalendar.get(Calendar.MINUTE);
    int mHour = mCalendar.get(Calendar.HOUR);

    Paint paintCircle;
    Paint paintPoint;
    Paint paintDegree;
    Paint paintHour;
    Paint paintMinute;
    Paint paintSecond;

    public DrawClock(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void draw(Canvas canvas){
        super.draw(canvas);

        //画圆
        paintCircle = new Paint();
        paintCircle.setStrokeWidth(3);
        paintCircle.setStyle(Paint.Style.STROKE);
        paintCircle.setAntiAlias(true);
        paintCircle.setStrokeWidth(5);
        canvas.drawCircle(mWidth / 2, mHeight/4, mWidth/3, paintCircle);
        paintPoint = new Paint();
        paintPoint.setStrokeWidth(10);
        canvas.drawCircle(mWidth / 2, mHeight / 4, 10, paintPoint);

        //画线
        paintDegree = new Paint();
        for(int i = 0; i < 60 ; i++){
            if( i == 0 || i == 5 || i == 10 || i == 15 || i == 20 || i == 25 || i == 30 || i == 35 || i == 40 || i == 45 || i ==50 || i == 55){
                paintDegree.setStrokeWidth(5);
                paintDegree.setTextSize(40);
                String degree;
                canvas.drawLine(mWidth / 2, mHeight / 4 - mWidth / 3, mWidth / 2, mHeight/4 - mWidth / 3 + 50, paintDegree);
                if(i == 0){
                    degree = String.valueOf(12);
                }else{
                    degree = String.valueOf(i / 5);
                }
                canvas.drawText(degree, mWidth / 2 - paintDegree.measureText(degree) / 2, mHeight / 4 - mWidth / 3 + 90, paintDegree);
            }else{
                paintDegree.setStrokeWidth(3);
                canvas.drawLine(mWidth / 2, mHeight / 4 - mWidth / 3, mWidth / 2, mHeight / 4 - mWidth / 3 + 30, paintDegree);
            }

            //通过旋转简化坐标运算
            canvas.rotate(6, mWidth / 2, mHeight / 4);
        }

        drawTime(canvas);

        Timer timer=new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                postInvalidate();
            }
        },0,1000);
    }

    /**
    * @description 画带箭头的指针
    * @param bottom 箭头三角形的底/2
     * @param height 箭头三角形的高
    * @return
    * @author snow
    * @time 2019/8/2 15:19
    */
    private void drawIndicator(Canvas canvas, float fromX, float fromY, float toX, float toY, int bottom, int height, Paint paint){
        canvas.save();
        canvas.drawLine(fromX, fromY, toX, toY, paint);
        float distance = (float) Math.sqrt((toX - fromX) * (toX - fromX) + (toY - fromY) * (toY - fromY));// 获取线段距离
        float distanceX = toX - fromX;// 有正负
        float distanceY = toY - fromY;// 有正负
        float bottommidpointX = toX - (height / distance * distanceX);    //三角形底边中点的x
        float bottommidpointY = toY - (height / distance * distanceY);    //三角形底边中点的y

        //箭头
        Path path = new Path();
        path.moveTo(toX, toY);    //线段的终点，也是三角形的一个顶点
        //利用相似三角形得出另外两个三角形的顶点
        path.lineTo(bottommidpointX + (bottom / distance * distanceY), bottommidpointY - (bottom / distance * distanceX));
        path.lineTo(bottommidpointX - (bottom / distance * distanceY), bottommidpointY + (bottom / distance * distanceX));
        path.close(); // 使这些点构成封闭的三边形
        canvas.drawPath(path, paint);
        canvas.restore();
    }

    private void drawTime(Canvas canvas){
        float secRot = mSecond * 6 / 1F;
        float minRot = mMinute * 6 + mSecond * 6 / 60F;
        float hrRot = mHour * 30 + mMinute * 6 / 360F;

        Log.d("Tag", "hrRot" + hrRot + ", minRot" + minRot + ", secRot" + secRot);

        //画指针
        paintHour = new Paint();
        paintHour.setStrokeWidth(8);
        paintMinute = new Paint();
        paintMinute.setStrokeWidth(5);
        paintSecond = new Paint();
        paintSecond.setStrokeWidth(3);

        canvas.save();
        canvas.translate(mWidth / 2, mHeight / 4);    //变换坐标原点

        //时针
        canvas.rotate(hrRot, 0, 0);
        drawIndicator(canvas,0,0,0,-70,10,20,paintHour);
        canvas.rotate(-hrRot, 0, 0);

        //分针
        canvas.rotate(minRot, 0, 0);
        drawIndicator(canvas,0,0,0,-160,10,30,paintMinute);
        canvas.rotate(-minRot, 0, 0);

        //秒针
        canvas.rotate(secRot, 0, 0);
        canvas.drawLine(0, 0, 0, -240, paintSecond);
        canvas.rotate(-secRot, 0, 0);

        canvas.restore();
    }

    public void refreshClock(){
        postInvalidate();    //刷新整个View，通过Handler将刷新事件通知发到Handler的handlerMessage中去执行incalidate
    }
}