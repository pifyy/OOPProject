package com.smarthome.controller;

import com.smarthome.command.Command;
import com.smarthome.command.CommandInvoker;
import com.smarthome.device.Device;
import com.smarthome.device.Lockable;
import com.smarthome.device.Switchable;
import com.smarthome.device.Thermostat;
import com.smarthome.factory.DeviceFactory;
import com.smarthome.observer.DeviceListener;

import java.io.PrintStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class HomeController {
    private final DeviceFactory deviceFactory;
    private final CommandInvoker commandInvoker;
    private final DeviceListener deviceChangeListener;
    private final Map<String, Device> devicesById = new LinkedHashMap<>();

    public HomeController(DeviceFactory deviceFactory, CommandInvoker commandInvoker,
                          DeviceListener deviceChangeListener) {
        this.deviceFactory = Objects.requireNonNull(deviceFactory, "deviceFactory");
        this.commandInvoker = Objects.requireNonNull(commandInvoker, "commandInvoker");
        this.deviceChangeListener = Objects.requireNonNull(deviceChangeListener, "deviceChangeListener");
    }

    public Switchable addLight(String lightId, String lightName) {
        return registerDeviceAndAttachListener(deviceFactory.createLight(lightId, lightName));
    }

    public Lockable addLock(String lockId, String lockName) {
        return registerDeviceAndAttachListener(deviceFactory.createLock(lockId, lockName));
    }

    public Thermostat addThermostat(String thermostatId, String thermostatName) {
        return registerDeviceAndAttachListener(deviceFactory.createThermostat(thermostatId, thermostatName));
    }

    private <T extends Device> T registerDeviceAndAttachListener(T newDevice) {
        if (devicesById.containsKey(newDevice.getId())) {
            throw new IllegalArgumentException("Duplicate device id: " + newDevice.getId());
        }
        newDevice.addListener(deviceChangeListener);
        devicesById.put(newDevice.getId(), newDevice);
        return newDevice;
    }

    public void run(Command commandToRun) {
        commandInvoker.run(commandToRun);
    }

    public boolean undo() {
        Command undoneCommand = commandInvoker.undo();
        return undoneCommand != null;
    }

    public Device getDeviceById(String deviceId) {
        return devicesById.get(deviceId);
    }

    public List<Device> getAllDevices() {
        return List.copyOf(devicesById.values());
    }

    public void printStatus() {
        printStatus(System.out);
    }

    public void printStatus(PrintStream statusOutput) {
        for (Device device : devicesById.values()) {
            statusOutput.println("  " + device.getName() + ": " + device.getStatus());
        }
    }
}