package com.example.antplus_ble_tester.bluetoothlegatt.business;

import com.example.antplus_ble_tester.R;
import com.example.antplus_ble_tester.bluetoothlegatt.model.BLEDevice;
import com.garmin.android.deviceinterface.connection.ConnectionManager;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * This class manages the list of discovered bluetooth devices, found by the GFDI layer. It displays a dialog for the user to confirm the device. A
 * callback interface is provided so that the caller can respond to events.
 *
 * @author Doug Turner, 2014
 */
@SuppressWarnings("serial")
public class DeviceDiscoveryManager extends java.util.LinkedList<BLEDevice> {

    private static final String TAG = DeviceDiscoveryManager.class.getSimpleName();
    private Context mContext = null;
    private BLEDevice mDevice = null;
    private AlertDialog mDialog = null;
    private Callback mCallback = null;
    private List<String> mMacAddressesRejectedByUser = null;

    /**
     * Used to inform caller of events.
     */
    public interface Callback {
        void onShowConfirmDialog();
        void onDeviceConfirmed(BLEDevice bleDevice);
        void onNoMoreDevicesToConfirm();
    }

    /**
     * @param context {@link android.content.Context}
     * @param callback {@link Callback}
     */
    public DeviceDiscoveryManager(final Context context, final Callback callback) {
        super();
        this.mContext = context;
        this.mCallback = callback;
        mMacAddressesRejectedByUser = new ArrayList<String>();
    }

    /**
     * Clears the queue and sets the current device to null.
     */
    public void reset() {
        clear();
        mMacAddressesRejectedByUser.clear();
        mDevice = null;
    }

    public void dismissDialog() {
        if (mDialog != null) {
            mDialog.dismiss();
        }
    }

    /**
     * @param intent {@link android.content.Intent}
     */
    public void addDevice(Intent intent) {
        // @formatter:off
        String macAddress = intent.getStringExtra(ConnectionManager.Extras.NAME_REMOTE_DEVICE_MAC_ADDRESS);
        String friendlyName = intent.getStringExtra(ConnectionManager.Extras.NAME_FRIENDLY_BLUETOOTH_NAME);
        String passkey = intent.getStringExtra(ConnectionManager.Extras.NAME_PAIRING_PASSKEY);
        if (!TextUtils.isEmpty(macAddress)) {
            if (!mMacAddressesRejectedByUser.contains(macAddress)) {
                BLEDevice d = new BLEDevice(macAddress, friendlyName, passkey);
                if (!contains(d)) {
                    add(d);
                    Log.d(TAG, ".addDevice() -- added [" + friendlyName + ", " + macAddress + "] to queue. Size now [" + size() + "].");
                    if (mDevice == null) {
                        confirmNextDevice();
                    }
                }
            } else {
                // This fills up the logcat rather quickly, so comment out when not needed.
                // Log.d(TAG, ".addDevice() -- " + friendlyName + ", " + macAddress + " previously rejected by user.");
            }
        } else {
            Log.w(TAG, ".addDevice() -- No MAC address for device friendly name [" + friendlyName + "], therefore not adding to queue.");
        }
        // @formatter:on
    }

    /**
     * Displays a dialog for the user to confirm the device to be paired.
     */
    private void confirmNextDevice() {
        if (mDevice == null && !isEmpty()) {
            Log.d(TAG, ".confirmNextDevice()");
            mDevice = peek(); // does not remove from list
            if (mDevice == null) {
                return;
            }

            // User previously rejected this device (this is a second check).
            if (mMacAddressesRejectedByUser.contains(mDevice.macAddress)) {
                removeFirst(); // now remove from list
                mDevice = null;
                return;
            }

            String msg = null;
            if (mDevice.hasPassKey()) {
                // Example: fitness device
                msg = mContext.getString(R.string.pairing_ble_passkey_phrase_a, mDevice.passkey);
            } else {
                // Example: outdoor device
                msg = mContext.getString(R.string.pair_ble_friendly_name_pair_confirm_help, mDevice.friendlyName);
            }

            //@formatter:off
            mDialog = new AlertDialog.Builder(mContext)
                .setCancelable(false)
                .setMessage(msg)
                .setPositiveButton(R.string.lbl_yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mCallback.onDeviceConfirmed(mDevice);
                        // THIS IS WHAT IT DID IN OLD FLOW: connectDevice(!discoveryMgr.mCurrentDevice.hasPassKey(), false);
                    }
                })
                .setNegativeButton(R.string.pairing_search_again, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d(TAG, "User rejected " + mDevice.toString());
                        mMacAddressesRejectedByUser.add(mDevice.macAddress);
                        removeFirst(); // now remove from list
                        mDevice = null;

                        if (!isEmpty()) {
                            confirmNextDevice();
                        } else {
                            mCallback.onNoMoreDevicesToConfirm();
                            // THIS IS WHAT IT DID IN OLD FLOW: showScanningUI();
                        }
                    }
                })
                .create();
            //@formatter:on

            mDialog.show();
            mCallback.onShowConfirmDialog();
        }
    }
}
