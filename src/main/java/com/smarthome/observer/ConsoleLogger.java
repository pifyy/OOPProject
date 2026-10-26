package com.smarthome.observer;

import com.smarthome.device.Device;

import java.io.PrintStream;
import java.util.Objects;

public class ConsoleLogger implements DeviceListener {
    private final PrintStream logOutput;

    public ConsoleLogger(PrintStream logOutput) {
        this.logOutput = Objects.requireNonNull(logOutput, "logOutput");
    }

    @Override
    public void onDeviceChanged(Device changedDevice, String changeDescription) {
        logOutput.println("[log] " + changedDevice.getName() + " " + changeDescription);
    }
}