package com.example.birthday.surprise;

import android.content.ContentValues;
import android.content.Context;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.WindowManager;

/**
 * @class name：com.example.birthday.surprise
 * @description:
 * @anthor: snow
 * @time: 2019/8/4 22:51
 * @modification_time:
 * @modifier:
 */
public class Util {
    public static ContentValues videoContentValues = null;

    /**
     * px = dp * (dpi / 160)
     *
     * @param ctx
     * @param dip
     * @return
     */
    public static int dipToPX(final Context ctx, float dip) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                dip, ctx.getResources().getDisplayMetrics());
    }

    public static int getScreenWidth(Context context) {
        WindowManager wm = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);
        return wm.getDefaultDisplay().getWidth();
    }

    public static int getScreenHeight(Context context) {
        WindowManager wm = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);
        return dm.heightPixels;
    }
}
