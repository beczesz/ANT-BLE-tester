package com.example.antplus_ble_tester;

import com.example.antplus_ble_tester.bluetoothlegatt.DeviceScanActivity;
import com.example.antplus_ble_tester.heartrate.Activity_AsyncScanHeartRateSampler;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends Activity implements View.OnClickListener {
    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        // set the action bar icon
        getActionBar().setIcon(R.drawable.icn);

        findViewById(R.id.ant_plus_tester).setOnClickListener(this);
        findViewById(R.id.ble_tester).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId() ) {
            case R.id.ant_plus_tester:
                startActivity(new Intent(this, Activity_AsyncScanHeartRateSampler.class));
                break;

            case R.id.ble_tester:
                startActivity(new Intent(this, DeviceScanActivity.class));
                break;
        }
    }

}
