package com.smarthome.device;

public interface Lockable extends Device {
    void lock();

    void unlock();

    boolean isLocked();
}