package com.smarthome.device;

public interface TemperatureAdjustable extends Device {
    void setTargetTemperature(int degrees);

    int getTargetTemperature();
}