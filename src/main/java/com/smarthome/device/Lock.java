package com.smarthome.device;

public class Lock extends AbstractDevice implements Lockable {
    private boolean locked = true;

    public Lock(String id, String name) {
        super(id, name);
    }

    @Override
    public void lock() {
        if (!locked) {
            locked = true;
            notifyListeners("locked");
        }
    }

    @Override
    public void unlock() {
        if (locked) {
            locked = false;
            notifyListeners("unlocked");
        }
    }

    @Override
    public boolean isLocked() {
        return locked;
    }

    @Override
    public String getStatus() {
        return locked ? "LOCKED" : "UNLOCKED";
    }
}