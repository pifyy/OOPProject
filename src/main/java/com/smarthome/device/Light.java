package com.smarthome.device;

public class Light extends AbstractDevice implements Switchable {
    private boolean on;

    public Light(String id, String name) {
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
    public String getStatus() {
        return on ? "ON" : "OFF";
    }
}