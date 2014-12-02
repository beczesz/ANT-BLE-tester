package com.example.antplus_ble_tester.bluetoothlegatt.model;

import android.os.Parcel;
import android.os.Parcelable;

public class BLEDevice implements Parcelable {
    public String macAddress, passkey, friendlyName;

    public static BLEDevice DEFAULT_FIT_DEVICE = new BLEDevice("10:C6:FC:63:B2:5D", "VivoFit (Cached)", "8394");
    public static BLEDevice DEFAULT__SAMRT_DEVICE = new BLEDevice("F5:87:FC:5C:02:AF", "VivoSmart (Cached)", "8394");

    public static final Creator<BLEDevice> CREATOR = new Creator<BLEDevice>() {

        @Override
        public BLEDevice createFromParcel(Parcel source) {
            return new BLEDevice(source);
        }

        @Override
        public BLEDevice[] newArray(int size) {
            return new BLEDevice[size];
        }
    };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(macAddress);
        dest.writeString(friendlyName);
        dest.writeString(passkey);
    }

    /**
     * @param source {@link android.os.Parcel}
     */
    public BLEDevice(Parcel source) {
        macAddress = source.readString();
        friendlyName = source.readString();
        passkey = source.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * @param aMacAddress String
     */
    public BLEDevice(String aMacAddress) {
        macAddress = aMacAddress;
    }

    /**
     * @param aMacAddress   String
     * @param aFriendlyName String
     * @param aPasskey      String
     */
    public BLEDevice(String aMacAddress, String aFriendlyName, String aPasskey) {
        macAddress = aMacAddress;
        friendlyName = aFriendlyName;
        passkey = aPasskey;
    }

    /**
     * @return boolean (true if credentials already satisfied)
     */
    public boolean hasPassKey() {
        if (passkey == null) {
            return false;
        }
        try {
            Integer.parseInt(passkey);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * @return boolean (true if current credentials are NOT satisfied)
     */
    public boolean areFurtherCredentialsNeeded() {
        return !hasPassKey();
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof BLEDevice) {
            return equals((BLEDevice) o);
        } else {
            return false;
        }
    }

    public boolean equals(BLEDevice d) {
        return (macAddress.equalsIgnoreCase(d.macAddress));
    }

    @Override
    public String toString() {
        StringBuffer buff = new StringBuffer(friendlyName);
        buff.append(" (").append(macAddress).append(")");
        return buff.toString();
    }
}
