package com.smarthome.command;

import com.smarthome.device.Lockable;

import java.util.Objects;

public class LockCommand implements Command {
    private final Lockable deviceToLock;
    private boolean deviceWasLockedBeforeExecute;

    public LockCommand(Lockable deviceToLock) {
        this.deviceToLock = Objects.requireNonNull(deviceToLock, "deviceToLock");
    }

    @Override
    public void execute() {
        deviceWasLockedBeforeExecute = deviceToLock.isLocked();
        deviceToLock.lock();
    }

    @Override
    public void undo() {
        if (!deviceWasLockedBeforeExecute) {
            deviceToLock.unlock();
        }
    }

    @Override
    public String getDescription() {
        return "Lock " + deviceToLock.getName();
    }
}