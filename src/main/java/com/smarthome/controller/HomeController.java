package com.smarthome.controller;

import com.smarthome.command.Command;
import com.smarthome.command.CommandInvoker;
import com.smarthome.command.LockCommand;
import com.smarthome.command.Scene;
import com.smarthome.command.SetTemperatureCommand;
import com.smarthome.command.TurnOffCommand;
import com.smarthome.command.TurnOnCommand;
import com.smarthome.device.Device;
import com.smarthome.device.Lockable;
import com.smarthome.device.Switchable;
import com.smarthome.device.Thermostat;
import com.smarthome.factory.DeviceFactory;
import com.smarthome.observer.DeviceListener;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Facade pattern: the one simple entry point to the smart home. Callers use
 * one-call routines like goodnight() and never deal with the factory, the
 * individual commands, scenes, the invoker or the listeners behind them.
 */
public class HomeController {
    public static final int GOODNIGHT_TEMPERATURE = 65;
    public static final int WELCOME_HOME_TEMPERATURE = 72;

    private final DeviceFactory deviceFactory;
    private final CommandInvoker commandInvoker;
    private final DeviceListener deviceChangeListener;
    private final Map<String, Device> devicesById = new LinkedHashMap<>();
    private final List<Switchable> lights = new ArrayList<>();
    private final List<Lockable> locks = new ArrayList<>();
    private final List<Thermostat> thermostats = new ArrayList<>();

    public HomeController(DeviceFactory deviceFactory, CommandInvoker commandInvoker,
                          DeviceListener deviceChangeListener) {
        this.deviceFactory = Objects.requireNonNull(deviceFactory, "deviceFactory");
        this.commandInvoker = Objects.requireNonNull(commandInvoker, "commandInvoker");
        this.deviceChangeListener = Objects.requireNonNull(deviceChangeListener, "deviceChangeListener");
    }

    public Switchable addLight(String lightId, String lightName) {
        Switchable newLight = registerDeviceAndAttachListener(deviceFactory.createLight(lightId, lightName));
        lights.add(newLight);
        return newLight;
    }

    public Lockable addLock(String lockId, String lockName) {
        Lockable newLock = registerDeviceAndAttachListener(deviceFactory.createLock(lockId, lockName));
        locks.add(newLock);
        return newLock;
    }

    public Thermostat addThermostat(String thermostatId, String thermostatName) {
        Thermostat newThermostat = registerDeviceAndAttachListener(
                deviceFactory.createThermostat(thermostatId, thermostatName));
        thermostats.add(newThermostat);
        return newThermostat;
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

    /** Lights off, doors locked, thermostats down to GOODNIGHT_TEMPERATURE. */
    public void goodnight() {
        List<Command> goodnightCommands = new ArrayList<>();
        for (Switchable light : lights) {
            goodnightCommands.add(new TurnOffCommand(light));
        }
        for (Lockable lock : locks) {
            goodnightCommands.add(new LockCommand(lock));
        }
        for (Thermostat thermostat : thermostats) {
            goodnightCommands.add(new SetTemperatureCommand(thermostat, GOODNIGHT_TEMPERATURE));
        }
        run(new Scene("Goodnight", goodnightCommands));
    }

    /** Lights off, doors locked, thermostats off to save energy. */
    public void leaveHome() {
        List<Command> leaveHomeCommands = new ArrayList<>();
        for (Switchable light : lights) {
            leaveHomeCommands.add(new TurnOffCommand(light));
        }
        for (Lockable lock : locks) {
            leaveHomeCommands.add(new LockCommand(lock));
        }
        for (Thermostat thermostat : thermostats) {
            leaveHomeCommands.add(new TurnOffCommand(thermostat));
        }
        run(new Scene("Leave Home", leaveHomeCommands));
    }

    /** Lights on, thermostats on and set to WELCOME_HOME_TEMPERATURE. Doors stay as they are. */
    public void welcomeHome() {
        List<Command> welcomeHomeCommands = new ArrayList<>();
        for (Switchable light : lights) {
            welcomeHomeCommands.add(new TurnOnCommand(light));
        }
        for (Thermostat thermostat : thermostats) {
            welcomeHomeCommands.add(new TurnOnCommand(thermostat));
            welcomeHomeCommands.add(new SetTemperatureCommand(thermostat, WELCOME_HOME_TEMPERATURE));
        }
        run(new Scene("Welcome Home", welcomeHomeCommands));
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