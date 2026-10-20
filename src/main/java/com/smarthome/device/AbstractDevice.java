package com.smarthome.device;

import com.smarthome.observer.DeviceListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public abstract class AbstractDevice implements Device {
    private final String id;
    private final String name;
    private final List<DeviceListener> listeners = new ArrayList<>();

    protected AbstractDevice(String id, String name) {
        this.id = Objects.requireNonNull(id, "id");
        this.name = Objects.requireNonNull(name, "name");
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void addListener(DeviceListener listener) {
        Objects.requireNonNull(listener, "listener");
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    @Override
    public void removeListener(DeviceListener listener) {
        listeners.remove(listener);
    }

    protected void notifyListeners(String change) {
        for (DeviceListener listener : List.copyOf(listeners)) {
            listener.onDeviceChanged(this, change);
        }
    }

    @Override
    public String toString() {
        return getName() + " [" + getStatus() + "]";
    }
}

