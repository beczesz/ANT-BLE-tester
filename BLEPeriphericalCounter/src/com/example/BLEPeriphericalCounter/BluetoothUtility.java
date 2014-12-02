package com.example.BLEPeriphericalCounter;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.AdvertisementData;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.os.ParcelUuid;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Bluetooth LE advertising and scanning utilities.
 * <p/>
 * Created by micah on 7/16/14.
 */
public class BluetoothUtility {

    // ------------------------------------------------------------------------
    // TYPES
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // STATIC FIELDS
    // ------------------------------------------------------------------------
    private static final int REQUEST_ENABLE_BT = 1;

    /**
     * String Constants
     */
    private static final String TAG = "MyActivity";
    private static final String BLUETOOTH_ADAPTER_NAME = "Garmin Android 5.0 device";

    // ------------------------------------------------------------------------
    // STATIC INITIALIZERS
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // STATIC METHODS
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // FIELDS
    // ------------------------------------------------------------------------
    /**
     * Advertising + Scanning Constants
     */
    private boolean scanning;
    private boolean advertising;
    private AdvertiseCallback advertiseCallback; //Must implement and set
    private BluetoothGattServerCallback gattServerCallback; //Must implement and set
    private ScanCallback scanCallback; //Must implement and set
    private List<ParcelUuid> serviceUuids;

    /**
     * Bluetooth Objects
     */
    Activity activity;
    private BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeAdvertiser bluetoothLeAdvertiser;
    private BluetoothGattServer gattServer;
    private ArrayList<BluetoothGattService> advertisingServices;
    private BluetoothLeScanner bluetoothLeScanner;
    // ------------------------------------------------------------------------
    // INITIALIZERS
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // CONSTRUCTORS
    // ------------------------------------------------------------------------

    public BluetoothUtility(Activity a) {
        activity = a;
        scanning = false;
        advertising = false;
        advertisingServices = new ArrayList<BluetoothGattService>();

        bluetoothManager = (BluetoothManager) activity.getApplicationContext().getSystemService(
                Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
        bluetoothAdapter.setName(BLUETOOTH_ADAPTER_NAME);

        bluetoothLeAdvertiser = bluetoothAdapter.getBluetoothLeAdvertiser();
        bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
        serviceUuids = new ArrayList<ParcelUuid>();
    }

    // ------------------------------------------------------------------------
    // METHODS
    // ------------------------------------------------------------------------

    public void cleanUp() {
        if (getAdvertising()) {
            stopAdvertise();
        }
        if (getScanning()) {
            stopBleScan();
        }
        if (gattServer != null) {
            gattServer.close();
        }
    }

    /**
     * Check if bluetooth is enabled, if not, then request enable
     */
    private void enableBluetooth() {
        if (bluetoothAdapter == null) {
            //bluetoothState.setText("Bluetooth NOT supported");
        } else if (!bluetoothAdapter.isEnabled()) {
            //bluetoothAdapter.enable();
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            activity.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
    }


    public boolean getAdvertising() {
        return advertising;
    }

    public void setAdvertiseCallback(AdvertiseCallback callback) {
        advertiseCallback = callback;
    }

    public void setGattServerCallback(BluetoothGattServerCallback callback) {
        gattServerCallback = callback;
    }

    /**
     * BLE Advertising
     */
    public void startAdvertise() {
        if (getAdvertising()) {
            return;
        }
        enableBluetooth();
        startGattServer();

        AdvertisementData.Builder dataBuilder = new AdvertisementData.Builder();
        AdvertiseSettings.Builder settingsBuilder = new AdvertiseSettings.Builder();


        dataBuilder.setIncludeTxPowerLevel(false); //necessity to fit in 31 byte advertisement
        dataBuilder.setServiceUuids(serviceUuids);

        settingsBuilder.setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED);
        settingsBuilder.setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH);
        settingsBuilder.setType(AdvertiseSettings.ADVERTISE_TYPE_CONNECTABLE);

        bluetoothLeAdvertiser.startAdvertising(settingsBuilder.build(), dataBuilder.build(), advertiseCallback);
        advertising = true;
    }

    /**
     * Stop ble advertising and clean up
     */
    public void stopAdvertise() {

        if (!getAdvertising()) {
            return;
        }

        bluetoothLeAdvertiser.stopAdvertising(advertiseCallback);
        gattServer.clearServices();
        gattServer.close();
        advertisingServices.clear();
        advertising = false;
    }

    public BluetoothGattServer getGattServer() {
        return gattServer;
    }

    private void startGattServer() {
        gattServer = bluetoothManager.openGattServer(activity, gattServerCallback);

        for (int i = 0; i < advertisingServices.size(); i++) {
            gattServer.addService(advertisingServices.get(i));
        }
    }

    public void addService(BluetoothGattService service) {
        advertisingServices.add(service);
        serviceUuids.add(new ParcelUuid(service.getUuid()));
    }

    /*-------------------------------------------------------------------------------*/

    public boolean getScanning() {
        //TODO check lescanning boolean
        return scanning;
    }

    public void setScanCallback(ScanCallback callback) {
        scanCallback = callback;
    }

    /**
     * BLE Scanning
     */
    public void startBleScan() {
        if (getScanning()) {
            return;
        }
        enableBluetooth();
        scanning = true;
        ScanFilter.Builder filterBuilder = new ScanFilter.Builder(); //TODO currently default, scans all devices
        ScanSettings.Builder settingsBuilder = new ScanSettings.Builder();
        List<ScanFilter> filters = new ArrayList<ScanFilter>();
        filters.add(filterBuilder.build());
        bluetoothLeScanner.startScan(filters, settingsBuilder.build(), scanCallback);

        Log.d(TAG, "Bluetooth is currently scanning...");
    }

    public void stopBleScan() {
        if (!getScanning()) {
            return;
        }
        scanning = false;
        bluetoothLeScanner.stopScan(scanCallback);
        Log.d(TAG, "Scanning has been stopped");
    }
}
