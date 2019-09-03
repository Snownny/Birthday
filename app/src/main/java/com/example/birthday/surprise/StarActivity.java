package com.example.birthday.surprise;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.example.birthday.R;

public class StarActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_star);


        lyContent = (LinearLayout) findViewById(R.id.stars_content);

        starView = new StarView(this);
        lyContent.addView(starView);

        handler.postDelayed(runnable, 0);

        findViewById(R.id.click_try).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(starView.isRunning()){
                    starView.pause();
                }else{
                    starView.start();
                }
            }
        });
    }

    private StarView starView;
    private LinearLayout lyContent;

    Handler handler = new Handler();
    Runnable runnable = new Runnable() {

        @Override
        public void run() {

            Log.d("123","-------run------"+starView.getStarNums() );
            // TODO Auto-generated method stub
            if(!starView.isRunning()){
                starView.start();
            }
            starView.addStars(25);
            handler.postDelayed(runnable, 200);
            if(starView.getStarNums() >= StarView.MAX_NUM)
            {
                handler.removeCallbacksAndMessages(null);
            }
        }

    };
}
