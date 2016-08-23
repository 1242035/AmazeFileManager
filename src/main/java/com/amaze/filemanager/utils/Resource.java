package com.amaze.filemanager.utils;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;

/**
 * Created by honganh on 23/08/2016.
 */
public class Resource {
    public static Drawable getResource(Context context, int id){
        return ResourcesCompat.getDrawable( context.getResources(),id,null);
    }
    public static int getColor(Context context, int colorId) {
        return ContextCompat.getColor(context, colorId);
    }
}
