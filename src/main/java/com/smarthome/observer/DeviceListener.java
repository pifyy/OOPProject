package com.smarthome.observer;

import com.smarthome.device.Device;

/** Observer notified whenever a device changes state. */
public interface DeviceListener {
    void onDeviceChanged(Device device, String change);
}
