package com.smarthome.command;

import com.smarthome.device.TemperatureAdjustable;

import java.util.Objects;

public class SetTemperatureCommand implements Command {
    private final TemperatureAdjustable deviceToAdjust;
    private final int requestedTargetTemperature;
    private int targetTemperatureBeforeExecute;

    public SetTemperatureCommand(TemperatureAdjustable deviceToAdjust, int requestedTargetTemperature) {
        this.deviceToAdjust = Objects.requireNonNull(deviceToAdjust, "deviceToAdjust");
        this.requestedTargetTemperature = requestedTargetTemperature;
    }

    @Override
    public void execute() {
        targetTemperatureBeforeExecute = deviceToAdjust.getTargetTemperature();
        deviceToAdjust.setTargetTemperature(requestedTargetTemperature);
    }

    @Override
    public void undo() {
        deviceToAdjust.setTargetTemperature(targetTemperatureBeforeExecute);
    }

    @Override
    public String getDescription() {
        return "Set " + deviceToAdjust.getName() + " to " + requestedTargetTemperature + "°F";
    }
}