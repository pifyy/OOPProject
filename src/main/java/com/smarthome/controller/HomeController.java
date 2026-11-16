package com.smarthome.controller;

import com.smarthome.command.Command;
import com.smarthome.command.CommandInvoker;
import com.smarthome.command.LockCommand;
import com.smarthome.command.Scene;
import com.smarthome.command.SetTemperatureCommand;
import com.smarthome.command.TurnOffCommand;
import com.smarthome.command.TurnOnCommand;
import com.smarthome.command.UnlockCommand;
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
 * one-call methods like turnOn("light-1") or goodnight() and never deal with
 * the factory, the individual commands, scenes, the invoker or the listeners
 * behind them.
 */
public class HomeController {
    public static final int GOODNIGHT_TEMPERATURE = 65;
    public static final int WELCOME_HOME_TEMPERATURE = 72;

    private final DeviceFactory deviceFactory;
    private final CommandInvoker commandInvoker;
    private final DeviceListener deviceChangeListener;
    private final Map<String, Device> devicesById = new LinkedHashMap<>();
    private final Map<String, Switchable> lightsById = new LinkedHashMap<>();
    private final Map<String, Lockable> locksById = new LinkedHashMap<>();
    private final Map<String, Thermostat> thermostatsById = new LinkedHashMap<>();

    public HomeController(DeviceFactory deviceFactory, CommandInvoker commandInvoker,
                          DeviceListener deviceChangeListener) {
        this.deviceFactory = Objects.requireNonNull(deviceFactory, "deviceFactory");
        this.commandInvoker = Objects.requireNonNull(commandInvoker, "commandInvoker");
        this.deviceChangeListener = Objects.requireNonNull(deviceChangeListener, "deviceChangeListener");
    }

    public Switchable addLight(String lightId, String lightName) {
        Switchable newLight = registerDeviceAndAttachListener(deviceFactory.createLight(lightId, lightName));
        lightsById.put(lightId, newLight);
        return newLight;
    }

    public Lockable addLock(String lockId, String lockName) {
        Lockable newLock = registerDeviceAndAttachListener(deviceFactory.createLock(lockId, lockName));
        locksById.put(lockId, newLock);
        return newLock;
    }

    public Thermostat addThermostat(String thermostatId, String thermostatName) {
        Thermostat newThermostat = registerDeviceAndAttachListener(
                deviceFactory.createThermostat(thermostatId, thermostatName));
        thermostatsById.put(thermostatId, newThermostat);
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

    public void turnOn(String deviceId) {
        run(new TurnOnCommand(findSwitchable(deviceId)));
    }

    public void turnOff(String deviceId) {
        run(new TurnOffCommand(findSwitchable(deviceId)));
    }

    public void lock(String lockId) {
        run(new LockCommand(findLock(lockId)));
    }

    public void unlock(String lockId) {
        run(new UnlockCommand(findLock(lockId)));
    }

    public void setTemperature(String thermostatId, int degrees) {
        run(new SetTemperatureCommand(findThermostat(thermostatId), degrees));
    }

    /** Lights off, doors locked, thermostats down to GOODNIGHT_TEMPERATURE. */
    public void goodnight() {
        List<Command> goodnightCommands = new ArrayList<>();
        for (Switchable light : lightsById.values()) {
            goodnightCommands.add(new TurnOffCommand(light));
        }
        for (Lockable lock : locksById.values()) {
            goodnightCommands.add(new LockCommand(lock));
        }
        for (Thermostat thermostat : thermostatsById.values()) {
            goodnightCommands.add(new SetTemperatureCommand(thermostat, GOODNIGHT_TEMPERATURE));
        }
        run(new Scene("Goodnight", goodnightCommands));
    }

    /** Lights off, doors locked, thermostats off to save energy. */
    public void leaveHome() {
        List<Command> leaveHomeCommands = new ArrayList<>();
        for (Switchable light : lightsById.values()) {
            leaveHomeCommands.add(new TurnOffCommand(light));
        }
        for (Lockable lock : locksById.values()) {
            leaveHomeCommands.add(new LockCommand(lock));
        }
        for (Thermostat thermostat : thermostatsById.values()) {
            leaveHomeCommands.add(new TurnOffCommand(thermostat));
        }
        run(new Scene("Leave Home", leaveHomeCommands));
    }

    /** Lights on, thermostats on and set to WELCOME_HOME_TEMPERATURE. Doors stay as they are. */
    public void welcomeHome() {
        List<Command> welcomeHomeCommands = new ArrayList<>();
        for (Switchable light : lightsById.values()) {
            welcomeHomeCommands.add(new TurnOnCommand(light));
        }
        for (Thermostat thermostat : thermostatsById.values()) {
            welcomeHomeCommands.add(new TurnOnCommand(thermostat));
            welcomeHomeCommands.add(new SetTemperatureCommand(thermostat, WELCOME_HOME_TEMPERATURE));
        }
        run(new Scene("Welcome Home", welcomeHomeCommands));
    }

    /** Lights and thermostats can both be switched on and off. */
    private Switchable findSwitchable(String deviceId) {
        Switchable light = lightsById.get(deviceId);
        if (light != null) {
            return light;
        }
        return findIn(thermostatsById, deviceId, "light or thermostat");
    }

    private Lockable findLock(String lockId) {
        return findIn(locksById, lockId, "lock");
    }

    private Thermostat findThermostat(String thermostatId) {
        return findIn(thermostatsById, thermostatId, "thermostat");
    }

    private static <T> T findIn(Map<String, T> devicesOfOneKind, String deviceId, String kindName) {
        T device = devicesOfOneKind.get(deviceId);
        if (device == null) {
            throw new IllegalArgumentException("No " + kindName + " with id '" + deviceId + "'");
        }
        return device;
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