package com.weezlabs.services.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

import com.weezlabs.services.MainActivity;
import com.weezlabs.services.R;

public class CalculatingPiService extends Service {
    private static final String ACTION_START_CALCULATING = "com.weezlabs.services.START_CALCULATING";
    private static final String ACTION_STOP_CALCULATING = "com.weezlabs.services.STOP_CALCULATING";
    private static final String ACTION_BOOT_COMPLETED = "com.weezlabs.services.BOOT_COMPLETED";
    private static final String LOG_TAG = CalculatingPiService.class.getSimpleName();

    private SharedPreferences mSharedPrefs;

    private CalculatingPiTask mCalculatingPiTask;

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

    public static void startActionBootCompleted(Context context) {
        Intent intent = new Intent(context, CalculatingPiService.class);
        intent.setAction(ACTION_BOOT_COMPLETED);
        context.startService(intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
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
                case ACTION_BOOT_COMPLETED:
                    handleActionBootCompleted();
                default:
                    break;
            }
        }
        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(LOG_TAG, "Service destroyed");
        if (mCalculatingPiTask != null) {
            mCalculatingPiTask.cancelTask();
        }
    }

    private void handleActionBootCompleted() {
        if (isServiceRunning()) {
            int maxDigit = mSharedPrefs.getInt(getString(R.string.key_count_to_calculate), MainActivity.PRECISION_32K);
            int currentDigit = mSharedPrefs.getInt(getString(R.string.key_last_calculated), 1);
            mCalculatingPiTask = new CalculatingPiTask(getApplicationContext());
            mCalculatingPiTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, currentDigit, maxDigit);
        }
    }

    private void handleActionStartCalculating() {
        SharedPreferences.Editor editor = mSharedPrefs.edit();
        editor.putBoolean(getString(R.string.key_service_running), true);
        editor.commit();

        int maxDigit = mSharedPrefs.getInt(getString(R.string.key_count_to_calculate), MainActivity.PRECISION_32K);
        mCalculatingPiTask = new CalculatingPiTask(getApplicationContext(), true);
        mCalculatingPiTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, 1, maxDigit);
    }

    private void handleActionStopCalculating() {
        SharedPreferences.Editor editor = mSharedPrefs.edit();
        editor.putBoolean(getString(R.string.key_service_running), false);
        editor.putInt(getString(R.string.key_current_progress), 0);
        editor.putInt(getString(R.string.key_last_calculated), 0);
        editor.commit();
        if (mCalculatingPiTask != null) {
            mCalculatingPiTask.cancelTask();
        }
    }

    private boolean isServiceRunning() {
        return mSharedPrefs.getBoolean(getString(R.string.key_service_running), false);
    }

}
