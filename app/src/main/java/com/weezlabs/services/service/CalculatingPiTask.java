package com.weezlabs.services.service;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.weezlabs.services.MainActivity;
import com.weezlabs.services.R;
import com.weezlabs.services.util.Bpp;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;


class CalculatingPiTask extends AsyncTask<Integer, Integer, Integer> {
    private static final String LOG_TAG = CalculatingPiTask.class.getSimpleName();
    private static final int NOTIFICATION_ID = 22042015;
    public static final String DEFAULT_PRECISION = "32K";
    public static final int PERCENT_100 = 100;
    public static final int OFFSET_DIGITS = 9;
    public static final String PI_FOLDER = "PI";
    public static final String PI_DIGITS_FILE = "pi_digits.txt";
    public static final String FORMAT_DIGITS_OFFSET = "%09d";
    public static final String PI_START = "3.";

    private boolean mIsRunning;
    private boolean mIsRewriteFile;
    private Context mContext;
    private SharedPreferences mPreferences;
    private PowerManager.WakeLock mWakeLock;
    private NotificationManager mNotifyManager;
    private NotificationCompat.Builder mBuilder;


    public CalculatingPiTask(Context context) {
        mContext = context.getApplicationContext();
        mPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        PowerManager pm = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, getClass().getName());
        mIsRewriteFile = false;
    }

    public CalculatingPiTask(Context context, boolean isRewriteFile) {
        this(context);
        mIsRewriteFile = isRewriteFile;
    }

    @Override
    protected void onPreExecute() {
        mIsRunning = true;
        mWakeLock.acquire();

        String precision = mPreferences.getString(mContext.getString(R.string.key_current_precision_string),
                DEFAULT_PRECISION);
        Intent intent = new Intent(mContext, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent contentIntent = PendingIntent.getActivity(mContext,
                0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        mNotifyManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        mBuilder = new NotificationCompat.Builder(mContext);
        mBuilder.setContentIntent(contentIntent)
                .setContentTitle(mContext.getString(R.string.title_notification_calculating))
                .setContentText(String.format(mContext.getString(R.string.content_notification_calculating), precision))
                .setSmallIcon(R.mipmap.ic_launcher)
                .setAutoCancel(false)
                .setOngoing(true);

    }

    @Override
    protected Integer doInBackground(Integer... params) {
        int currentDigit = params[0];
        int maxDigit = params[1];
        Bpp bpp = new Bpp();
        long digits;
        Log.d(LOG_TAG, "digits count to calculate: " + maxDigit);
        while (currentDigit < maxDigit) {
            if (!isCancelled() && mIsRunning) {
                digits = bpp.getDecimal(currentDigit);
                currentDigit = currentDigit + OFFSET_DIGITS;
                try {
                    saveCalculatedDigits(digits);
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e(LOG_TAG, "Cant save digits to file!");
                }
                publishProgress((int) (currentDigit * PERCENT_100 / maxDigit), currentDigit);
            } else {
                return null;
            }
        }
        return currentDigit;
    }

    private void saveCalculatedDigits(long digits) throws IOException {
        Log.d(LOG_TAG, "calculated digits: " + String.format(FORMAT_DIGITS_OFFSET, digits));
        if (!isExternalStorageWritable()) {
            return;
        }
        File folder = new File(Environment.getExternalStorageDirectory(), PI_FOLDER);
        File piFile;
        FileWriter fileWriter;
        if (folder.mkdirs() || folder.isDirectory()) {
            piFile = new File(folder, PI_DIGITS_FILE);
            if (piFile.exists()) {
                if (mIsRewriteFile) {
                    fileWriter = new FileWriter(piFile);
                    saveToFile(fileWriter, PI_START + String.format(FORMAT_DIGITS_OFFSET, digits));
                    mIsRewriteFile = false;
                } else {
                    // append to the file
                    fileWriter = new FileWriter(piFile, true);
                    saveToFile(fileWriter, String.format(FORMAT_DIGITS_OFFSET, digits));
                }
            } else {
                if (piFile.createNewFile()) {
                    fileWriter = new FileWriter(piFile);
                    saveToFile(fileWriter, PI_START + String.format(FORMAT_DIGITS_OFFSET, digits));
                    mIsRewriteFile = false;
                } else {
                    throw new IOException("File not created!");
                }
            }
        }

    }

    private void saveToFile(FileWriter fileWriter, String writeString) throws IOException {
        BufferedWriter writer = new BufferedWriter(fileWriter);
        writer.write(writeString);
        writer.flush();
        writer.close();
    }

    private boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        Log.d(LOG_TAG, "progressUpdate: progress: " + values[0] + "%; currentDigit: " + values[1]);
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putInt(mContext.getString(R.string.key_current_progress), values[0]);
        editor.putInt(mContext.getString(R.string.key_last_calculated), values[1]);
        editor.commit();

        // Sets the progress indicator to a max value, the
        // current completion percentage, and "determinate"
        // state
        mBuilder.setProgress(PERCENT_100, values[0], false);
        // Displays the progress bar.
        mNotifyManager.notify(NOTIFICATION_ID, mBuilder.build());

    }

    @Override
    protected void onPostExecute(Integer integer) {
        mWakeLock.release();
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putBoolean(mContext.getString(R.string.key_service_running), false);
        if (integer != null) {
            editor.putInt(mContext.getString(R.string.key_current_progress), PERCENT_100);
            editor.putInt(mContext.getString(R.string.key_last_calculated), integer);
        }
        editor.commit();

        String precision = mPreferences.getString(mContext.getString(R.string.key_current_precision_string),
                DEFAULT_PRECISION);
        mBuilder.setContentText(String.format(
                mContext.getString(R.string.content_notification_calculating_completed), precision))
                .setContentTitle(mContext.getString(R.string.title_notification_calculating_completed))
                        // Removes the progress bar
                .setProgress(0, 0, false)
                .setAutoCancel(true)
                .setOngoing(false);
        mNotifyManager.notify(NOTIFICATION_ID, mBuilder.build());
    }

    public void cancelTask() {
        mNotifyManager.cancel(NOTIFICATION_ID);
        mIsRunning = false;
        if (mWakeLock.isHeld()) {
            mWakeLock.release();
        }
        cancel(true);
    }

}
