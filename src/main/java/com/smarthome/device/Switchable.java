package com.smarthome.device;

public interface Switchable extends Device {
    void turnOn();

    void turnOff();

    boolean isOn();
}