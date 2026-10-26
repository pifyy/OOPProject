package com.smarthome.command;

import com.smarthome.device.Switchable;

import java.util.Objects;

public class TurnOnCommand implements Command {
    private final Switchable deviceToTurnOn;
    private boolean deviceWasOnBeforeExecute;

    public TurnOnCommand(Switchable deviceToTurnOn) {
        this.deviceToTurnOn = Objects.requireNonNull(deviceToTurnOn, "deviceToTurnOn");
    }

    @Override
    public void execute() {
        deviceWasOnBeforeExecute = deviceToTurnOn.isOn();
        deviceToTurnOn.turnOn();
    }

    @Override
    public void undo() {
        if (!deviceWasOnBeforeExecute) {
            deviceToTurnOn.turnOff();
        }
    }

    @Override
    public String getDescription() {
        return "Turn on " + deviceToTurnOn.getName();
    }
}