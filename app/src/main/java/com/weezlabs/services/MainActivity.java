package com.weezlabs.services;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import com.daimajia.numberprogressbar.NumberProgressBar;
import com.weezlabs.services.service.CalculatingPiService;


public class MainActivity extends AppCompatActivity {

    public static final int PRECISION_32K = 32000;
    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private int[] mPrecisionValues;
    private SharedPreferences mSharedPrefs;
    private Spinner mSpinner;
    private NumberProgressBar mCalculationProgressBar;
    private Button mStartCalculatingButton;
    private SharedPreferences.OnSharedPreferenceChangeListener mPrefsChangeListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        mPrefsChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                if (key.equals(getString(R.string.key_service_running))) {
                    updateViews();
                    mStartCalculatingButton.setEnabled(true);
                } else if (key.equals(getString(R.string.key_current_progress))) {
                    updateProgress();
                }
            }
        };
        mSharedPrefs.registerOnSharedPreferenceChangeListener(mPrefsChangeListener);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.precision_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        initPrecisionValues(adapter.getCount());

        mCalculationProgressBar = (NumberProgressBar) findViewById(R.id.progress_calculation);
        mStartCalculatingButton = (Button) findViewById(R.id.calculating_button);
        mSpinner = (Spinner) findViewById(R.id.precision_spinner);
        mSpinner.setAdapter(adapter);

        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                SharedPreferences.Editor editor = mSharedPrefs.edit();
                editor.putInt(getString(R.string.key_count_to_calculate), mPrecisionValues[position]);
                editor.putString(getString(R.string.key_current_precision_string),
                        (String) parent.getItemAtPosition(position));
                editor.commit();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        mStartCalculatingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isServiceRunning()) {
                    CalculatingPiService.startActionStartCalculating(getApplicationContext());
                } else {
                    CalculatingPiService.startActionStopCalculating(getApplicationContext());
                }
                mStartCalculatingButton.setEnabled(false);
            }
        });

        updateViews();
        updateProgress();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void updateViews() {
        int count = mSharedPrefs.getInt(getString(R.string.key_count_to_calculate), PRECISION_32K);
        int position = findPosition(mPrecisionValues, count);
        mSpinner.setSelection(position);

        if (!isServiceRunning()) {
            mSpinner.setEnabled(true);
            mStartCalculatingButton.setText(getString(R.string.label_button_start));
            mCalculationProgressBar.setVisibility(View.GONE);
        } else {
            mSpinner.setEnabled(false);
            mStartCalculatingButton.setText(getString(R.string.label_button_cancel));
            mCalculationProgressBar.setVisibility(View.VISIBLE);
        }
    }

    private void updateProgress() {
        int progress = mSharedPrefs.getInt(getString(R.string.key_current_progress), 0);
        mCalculationProgressBar.setProgress(progress);
        if (isServiceRunning()) {
            mCalculationProgressBar.setVisibility(View.VISIBLE);
        } else {
            mCalculationProgressBar.setVisibility(View.GONE);
        }
    }

    private boolean isServiceRunning() {
        return mSharedPrefs.getBoolean(getString(R.string.key_service_running), false);
    }

    private int findPosition(int[] array, int value) {
        for (int i = 0; i < array.length; i++) {
            if (array[i] == value) {
                return i;
            }
        }
        return 0;
    }

    private void initPrecisionValues(int count) {
        int precision1m = 1000000;
        mPrecisionValues = new int[count];

        mPrecisionValues[0] = PRECISION_32K;
        int i = 1;
        while (i < count) {
            if (i == 5) {
                mPrecisionValues[i] = precision1m;
            } else {
                mPrecisionValues[i] = mPrecisionValues[i - 1] * 2;
            }
            i++;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        // not implemented
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
