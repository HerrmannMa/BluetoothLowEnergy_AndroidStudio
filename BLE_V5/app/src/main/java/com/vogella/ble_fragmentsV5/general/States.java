package com.vogella.ble_fragmentsV5.general;

/**
 * Created by Matthias on 26.12.2015.
 */
public enum States {
    NOT_CONNECTED(0), CONNECTED_SAME_DEVICE_SELECTED(1), CONNECTED_NEW_DEVICE_SELECTED(2), CONNECTED(3);

    //Get and Set-methods are only necessary to save currentState with the primitive methods of SharedPreferences and not use overhead from Json
    private final int value;

    private States(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static States fromInteger(int x) {
        switch (x) {
            case 0:
                return NOT_CONNECTED;
            case 1:
                return CONNECTED_SAME_DEVICE_SELECTED;
            case 2:
                return CONNECTED_NEW_DEVICE_SELECTED;
            case 3:
                return CONNECTED;
            default:
                return null;
        }
    }
}
