package com.hard.function.tool;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;

public class Utils {

    public static void startActivity(Context context, Class<?> cls){
        context.startActivity(new Intent(context,cls));
    }

    public static int getColor(String color){
        return Color.parseColor(color);
    }

    public static void logiJerry(String strLog){
        Log.i("Jerry debug", strLog);
    }

}
