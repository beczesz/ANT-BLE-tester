package com.example.BLEPeriphericalCounter;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.UUID;

/**
 * A simple example in which we are
 * 1. Advertise a custom characteristics (A counter value)
 * 2. Send the response
 * <p/>
 * Note: Thw application is based on the project https://github.com/geoaxis/BluetoothTest
 */
public class Counter extends Activity {

    // ------------------------------------------------------------------------
    // TYPES
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // STATIC FIELDS
    // ------------------------------------------------------------------------

    private static final String TAG = Counter.class.getSimpleName();

    /**
     * This is a custom uuid it can be also generated randomly.
     */
    private static final String CUSTOM_SERVICE_UUID_1 = "00001802-0000-1000-8000-9876543214fb";
    private static final String CUSTOM_UUID = "0000180a-0000-1000-8000-1234567834fb";


    // ------------------------------------------------------------------------
    // STATIC INITIALIZERS
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // STATIC METHODS
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // FIELDS
    // ------------------------------------------------------------------------

    private AdvertiseCallback advertiseCallback = new AdvertiseCallback() {

        @Override
        public void onStartFailure(int errorCode) {
            super.onStartFailure(errorCode);
            Toast.makeText(Counter.this, "Advertisement Failed", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onStartSuccess(AdvertiseSettings advertiseSettings) {
            Toast.makeText(Counter.this, "Advertisement Started", Toast.LENGTH_SHORT).show();
        }

    };

    public BluetoothGattServerCallback gattServerCallback = new BluetoothGattServerCallback() {

        @Override
        public void onConnectionStateChange(BluetoothDevice device, int status, int newState) {
            super.onConnectionStateChange(device, status, newState);
            Log.d(TAG, "onConnectionStateChange status=" + status + "->" + newState);
        }

        @Override
        public void onServiceAdded(int status, BluetoothGattService service) {
            super.onServiceAdded(status, service);
        }

        @Override
        public void onCharacteristicReadRequest(BluetoothDevice device, int requestId, int offset,
                BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicReadRequest(device, requestId, offset, characteristic);
            Log.d(TAG, "onCharacteristicReadRequest requestId=" + requestId + " offset=" + offset);

            if (characteristic.getUuid().equals(UUID.fromString(CUSTOM_UUID))) {
                characteristic.setValue(mCounter + "");

                bleBluetoothUtility.getGattServer().sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset,
                                                                 characteristic.getValue());
            }

        }

        @Override
        public void onCharacteristicWriteRequest(BluetoothDevice device, int requestId,
                BluetoothGattCharacteristic characteristic, boolean preparedWrite, boolean responseNeeded, int offset,
                byte[] value) {

            super.onCharacteristicWriteRequest(device, requestId, characteristic, preparedWrite, responseNeeded, offset,
                                               value);
            Log.d(TAG, "onCharacteristicWriteRequest requestId=" + requestId + " preparedWrite="
                    + Boolean.toString(preparedWrite) + " responseNeeded="
                    + Boolean.toString(responseNeeded) + " offset=" + offset);
        }
    };

    private ScanCallback scanCallback = new ScanCallback() {

        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            // TODO implement it
        }



        @Override
        public void onScanFailed(int i) {
            Log.e(TAG, "Scan attempt failed");
        }
    };


    private TextView mCounterTextView;
    private int mCounter;
    private BluetoothUtility bleBluetoothUtility;

    // ------------------------------------------------------------------------
    // INITIALIZERS
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // CONSTRUCTORS
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // METHODS
    // ------------------------------------------------------------------------

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        mCounterTextView = (TextView) findViewById(R.id.counter_text_field);
        incrementCounter(null);

        bleBluetoothUtility = new BluetoothUtility(this);
        bleBluetoothUtility.setAdvertiseCallback(advertiseCallback);
        bleBluetoothUtility.setGattServerCallback(gattServerCallback);
        bleBluetoothUtility.setScanCallback(scanCallback);
        addServiceToGattServer();
        bleBluetoothUtility.startAdvertise();
    }

    /**
     * Adds a gatt service to the advertizin service.
     */
    private void addServiceToGattServer() {

        BluetoothGattService customService = new BluetoothGattService(
                UUID.fromString(CUSTOM_SERVICE_UUID_1),
                BluetoothGattService.SERVICE_TYPE_PRIMARY);

        // alert level char.
        BluetoothGattCharacteristic firstServiceChar = new BluetoothGattCharacteristic(
                UUID.fromString(CUSTOM_UUID),
                BluetoothGattCharacteristic.PROPERTY_READ | BluetoothGattCharacteristic.PROPERTY_WRITE,
                BluetoothGattCharacteristic.PERMISSION_READ | BluetoothGattCharacteristic.PERMISSION_WRITE);

        customService.addCharacteristic(firstServiceChar);
        bleBluetoothUtility.addService(customService);
    }

    public void incrementCounter(View view) {
        // increment the counter
        mCounter++;
        mCounterTextView.setText(mCounter + "");
        Log.w(TAG, "Counter: " + mCounter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        bleBluetoothUtility.stopAdvertise();
    }
}
