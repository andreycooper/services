package com.weezlabs.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.IBinder;
import android.preference.PreferenceManager;

public class CalculatingPiService extends Service {
    private static final String ACTION_START_CALCULATING = "com.weezlabs.services.START_CALCULATING";
    private static final String ACTION_STOP_CALCULATING = "com.weezlabs.services.STOP_CALCULATING";

    SharedPreferences mSharedPrefs;

    private CalculatingTask mCalculatingTask;

    public CalculatingPiService() {
    }

    public static void startActionStartCalculating(Context context) {
        Intent intent = new Intent(context, CalculatingPiService.class);
        intent.setAction(ACTION_START_CALCULATING);
        context.startService(intent);
    }

    public static void startActionStopCalculating(Context context) {
        Intent intent = new Intent(context, CalculatingPiService.class);
        intent.setAction(ACTION_STOP_CALCULATING);
        context.startService(intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        // TODO: read state from SharedPrefs
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            final String action = intent.getAction();
            switch (action) {
                case ACTION_START_CALCULATING:
                    handleActionStartCalculating();
                    break;
                case ACTION_STOP_CALCULATING:
                    handleActionStopCalculating();
                    break;
                default:
                    break;
            }
        }
        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void handleActionStartCalculating() {
        SharedPreferences.Editor editor = mSharedPrefs.edit();
        editor.putBoolean(getString(R.string.key_service_running), true);
        editor.commit();
    }

    private void handleActionStopCalculating() {
        SharedPreferences.Editor editor = mSharedPrefs.edit();
        editor.putBoolean(getString(R.string.key_service_running), false);
        editor.commit();
        mCalculatingTask.cancel(true);
    }

    private static class CalculatingTask extends AsyncTask<Integer, Integer, Void> {
        private boolean mIsRunning;

        @Override
        protected void onPreExecute() {
            mIsRunning = true;
        }

        @Override
        protected Void doInBackground(Integer... params) {
            if (!isCancelled() && mIsRunning) {
                // TODO: calculate PI
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            // TODO: update progress
            super.onProgressUpdate(values);
        }
    }
}
