package com.smarthome.device;

import com.smarthome.observer.DeviceListener;

/**
 * Common abstraction for every simulated device. The controller and commands
 * work only with this interface, never with concrete device classes.
 */
public interface Device {
    String getId();

    String getName();

    /** Human-readable summary of the current state, e.g. "ON" or "LOCKED". */
    String getStatus();

    void addListener(DeviceListener listener);

    void removeListener(DeviceListener listener);
}
