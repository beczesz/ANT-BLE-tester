/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.antplus_ble_tester.bluetoothlegatt;

import android.util.Log;

import java.util.HashMap;

/**
 * This class includes a small subset of standard GATT attributes for demonstration purposes.
 */
public class KnownGattAttributes {
    private static HashMap<String, String> attributes = new HashMap();
    public static String HEART_RATE_MEASUREMENT = "00002a37-0000-1000-8000-00805f9b34fb";
    public static String CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";

    static {
        // Sample Services.
        attributes.put("00001802-0000-1000-8000-9876543214fb", "Custom Count Service");
        attributes.put("0000180a-0000-1000-8000-1234567834fb", "Counter Value");
        attributes.put("0000180d-0000-1000-8000-00805f9b34fb", "Heart Rate Service");
        attributes.put("0000180a-0000-1000-8000-00805f9b34fb", "Device Information Service");
        attributes.put("9B012401-BC30-CE9A-E111-0F67E491ABDE", "Garmin SERVICE_UUID_FITNESS");
        attributes.put("DF334C80-E6A7-D082-274D-78FC66F85E16", "Garmin WRITE_CHAR.");
        attributes.put("4ACBCD28-7425-868E-F447-915C8F00D0CB", "Garmin READ_CHAR.");
        attributes.put("6A4E3E10-667B-11E3-949A-0800200C9A66", "Garmin SERVICE_GENERIC");
        attributes.put("00002902-0000-1000-8000-00805f9b34fb", "Garmin READ_DESCRIPTOR");
        // Sample Characteristics.
        attributes.put(HEART_RATE_MEASUREMENT, "Heart Rate Measurement");


        attributes.put("00002a43-0000-1000-8000-00805f9b34fb", "Alert Category ID");
        attributes.put("00002a42-0000-1000-8000-00805f9b34fb", "Alert Category ID Bit Mask");
        attributes.put("00002a06-0000-1000-8000-00805f9b34fb", "Alert Level");
        attributes.put("00002a44-0000-1000-8000-00805f9b34fb", "Alert Notification Control Point");
        attributes.put("00002a3f-0000-1000-8000-00805f9b34fb", "Alert Status");
        attributes.put("00002a01-0000-1000-8000-00805f9b34fb", "Appearance");
        attributes.put("00002a49-0000-1000-8000-00805f9b34fb", "Blood Pressure Feature");
        attributes.put("00002a35-0000-1000-8000-00805f9b34fb", "Blood Pressure Measurement");
        attributes.put("00002a38-0000-1000-8000-00805f9b34fb", "Body Sensor Location");
        attributes.put("00002a2b-0000-1000-8000-00805f9b34fb", "Current Time");
        attributes.put("00002a08-0000-1000-8000-00805f9b34fb", "Date Time");
        attributes.put("00002a0a-0000-1000-8000-00805f9b34fb", "Day Date Time");
        attributes.put("00002a09-0000-1000-8000-00805f9b34fb", "Day of Week");
        attributes.put("00002a00-0000-1000-8000-00805f9b34fb", "Device Name");
        attributes.put("00002a0d-0000-1000-8000-00805f9b34fb", "DST Offset");
        attributes.put("00002a0c-0000-1000-8000-00805f9b34fb", "Exact Time 256");
        attributes.put("00002a26-0000-1000-8000-00805f9b34fb", "Firmware Revision String");
        attributes.put("00002a27-0000-1000-8000-00805f9b34fb", "Hardware Revision String");
        attributes.put("00002a39-0000-1000-8000-00805f9b34fb", "Heart Rate Control Point");
        attributes.put("00002a37-0000-1000-8000-00805f9b34fb", "Heart Rate Measurement");
        attributes.put("00002a2a-0000-1000-8000-00805f9b34fb", "IEEE 11073-20601 Regulatory");
        attributes.put("00002a36-0000-1000-8000-00805f9b34fb", "Intermediate Cuff Pressure");
        attributes.put("00002a1e-0000-1000-8000-00805f9b34fb", "Intermediate Temperature");
        attributes.put("00002a0f-0000-1000-8000-00805f9b34fb", "Local Time Information");
        attributes.put("00002a29-0000-1000-8000-00805f9b34fb", "Manufacturer Name String");
        attributes.put("00002a21-0000-1000-8000-00805f9b34fb", "Measurement Interval");
        attributes.put("00002a24-0000-1000-8000-00805f9b34fb", "Model Number String");
        attributes.put("00002a46-0000-1000-8000-00805f9b34fb", "New Alert");
        attributes.put("00002a04-0000-1000-8000-00805f9b34fb", "Peripheral Preferred Connection Parameters");
        attributes.put("00002a02-0000-1000-8000-00805f9b34fb", "Peripheral Privacy Flag");
        attributes.put("00002a03-0000-1000-8000-00805f9b34fb", "Reconnection Address");
        attributes.put("00002a14-0000-1000-8000-00805f9b34fb", "Reference Time Information");
        attributes.put("00002a40-0000-1000-8000-00805f9b34fb", "Ringer Control Point");
        attributes.put("00002a41-0000-1000-8000-00805f9b34fb", "Ringer Setting");
        attributes.put("00002a25-0000-1000-8000-00805f9b34fb", "Serial Number String");
        attributes.put("00002a05-0000-1000-8000-00805f9b34fb", "Service Changed");
        attributes.put("00002a28-0000-1000-8000-00805f9b34fb", "Software Revision String");
        attributes.put("00002a47-0000-1000-8000-00805f9b34fb", "Supported New Alert Category");
        attributes.put("00002a48-0000-1000-8000-00805f9b34fb", "Supported Unread Alert Category");
        attributes.put("00002a23-0000-1000-8000-00805f9b34fb", "System ID");
        attributes.put("00002a1c-0000-1000-8000-00805f9b34fb", "Temperature Measurement");
        attributes.put("00002a1d-0000-1000-8000-00805f9b34fb", "Temperature Type");
        attributes.put("00002a12-0000-1000-8000-00805f9b34fb", "Time Accuracy");
        attributes.put("00002a13-0000-1000-8000-00805f9b34fb", "Time Source");
        attributes.put("00002a16-0000-1000-8000-00805f9b34fb", "Time Update Control Point");
        attributes.put("00002a17-0000-1000-8000-00805f9b34fb", "Time Update State");
        attributes.put("00002a11-0000-1000-8000-00805f9b34fb", "Time with DST");
        attributes.put("00002a0e-0000-1000-8000-00805f9b34fb", "Time Zone");
        attributes.put("00002a07-0000-1000-8000-00805f9b34fb", "Tx Power Level");
        attributes.put("00002a45-0000-1000-8000-00805f9b34fb", "Unread Alert Status");

    }

    public static String lookup(String uuid, String defaultName) {
        String name = attributes.get(uuid);
        if (name == null) {
            name = attributes.get(uuid.toUpperCase());
        }

        Log.w("BSZ", "uuid: " + uuid);

        return name == null ? defaultName : name;
    }
}
