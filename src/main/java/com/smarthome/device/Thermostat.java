package com.smarthome.device;

public class Thermostat extends AbstractDevice implements Switchable, TemperatureAdjustable {
    public static final int MIN_TEMP = 50;
    public static final int MAX_TEMP = 90;
    public static final int DEFAULT_TEMP = 70;

    private boolean on = true;
    private int targetTemperature = DEFAULT_TEMP;

    public Thermostat(String id, String name) {
        super(id, name);
    }

    @Override
    public void turnOn() {
        if (!on) {
            on = true;
            notifyListeners("turned on");
        }
    }

    @Override
    public void turnOff() {
        if (on) {
            on = false;
            notifyListeners("turned off");
        }
    }

    @Override
    public boolean isOn() {
        return on;
    }

    @Override
    public void setTargetTemperature(int degrees) {
        if (degrees < MIN_TEMP || degrees > MAX_TEMP) {
            throw new IllegalArgumentException(
                    "Target must be between " + MIN_TEMP + " and " + MAX_TEMP + "°F, got " + degrees);
        }
        if (degrees != targetTemperature) {
            targetTemperature = degrees;
            notifyListeners("target set to " + degrees + "°F");
        }
    }

    @Override
    public int getTargetTemperature() {
        return targetTemperature;
    }

    @Override
    public String getStatus() {
        return (on ? "ON" : "OFF") + ", target " + targetTemperature + "°F";
    }
}