package com.weezlabs.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.weezlabs.services.service.CalculatingPiService;

public class BootCompletedReceiver extends BroadcastReceiver {
    private static final String LOG_TAG = BootCompletedReceiver.class.getSimpleName();

    public BootCompletedReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(LOG_TAG, "receive BOOT_COMPLETED");
        CalculatingPiService.startActionBootCompleted(context);
    }
}