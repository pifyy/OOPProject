package com.smarthome.command;

import com.smarthome.device.Switchable;

import java.util.Objects;

public class TurnOffCommand implements Command {
    private final Switchable deviceToTurnOff;
    private boolean deviceWasOnBeforeExecute;

    public TurnOffCommand(Switchable deviceToTurnOff) {
        this.deviceToTurnOff = Objects.requireNonNull(deviceToTurnOff, "deviceToTurnOff");
    }

    @Override
    public void execute() {
        deviceWasOnBeforeExecute = deviceToTurnOff.isOn();
        deviceToTurnOff.turnOff();
    }

    @Override
    public void undo() {
        if (deviceWasOnBeforeExecute) {
            deviceToTurnOff.turnOn();
        }
    }

    @Override
    public String getDescription() {
        return "Turn off " + deviceToTurnOff.getName();
    }
}