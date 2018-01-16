package com.accloud.ac_service_android_demo.utils;

import android.content.Context;
import android.widget.Toast;

public class Pop {
    static Toast toast = null;

    public static void popToast(Context context, String title) {
        if (toast == null)
            toast = Toast.makeText(context, title, Toast.LENGTH_SHORT);
        else
            toast.setText(title);
        toast.show();
    }
}
