package com.nebuxe.cse535assignment.utilities;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.widget.Toast;

public class ToastX {
    public static void show(Activity activity, String message, int duration) {
        try {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(activity, message, duration).show();
                }
            });
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
    }
}
