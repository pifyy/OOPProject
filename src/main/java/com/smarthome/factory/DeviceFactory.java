package com.smarthome.factory;

import com.smarthome.device.Lockable;
import com.smarthome.device.Light;
import com.smarthome.device.Lock;
import com.smarthome.device.Switchable;
import com.smarthome.device.Thermostat;


public class DeviceFactory {
    public Switchable createLight(String id, String name) {
        return new Light(id, name);
    }

    public Lockable createLock(String id, String name) {
        return new Lock(id, name);
    }

    public Thermostat createThermostat(String id, String name) {
        return new Thermostat(id, name);
    }
}
