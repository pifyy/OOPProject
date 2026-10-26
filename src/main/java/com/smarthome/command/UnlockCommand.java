package com.smarthome.command;

import com.smarthome.device.Lockable;

import java.util.Objects;

public class UnlockCommand implements Command {
    private final Lockable deviceToUnlock;
    private boolean deviceWasLockedBeforeExecute;

    public UnlockCommand(Lockable deviceToUnlock) {
        this.deviceToUnlock = Objects.requireNonNull(deviceToUnlock, "deviceToUnlock");
    }

    @Override
    public void execute() {
        deviceWasLockedBeforeExecute = deviceToUnlock.isLocked();
        deviceToUnlock.unlock();
    }

    @Override
    public void undo() {
        if (deviceWasLockedBeforeExecute) {
            deviceToUnlock.lock();
        }
    }

    @Override
    public String getDescription() {
        return "Unlock " + deviceToUnlock.getName();
    }
}