package com.smarthome.command;

import com.smarthome.controller.HomeController;
import com.smarthome.device.Lockable;
import com.smarthome.device.Switchable;
import com.smarthome.device.Thermostat;
import com.smarthome.factory.DeviceFactory;
import com.smarthome.observer.ConsoleLogger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CommandTest {
    private DeviceFactory deviceFactory;
    private CommandInvoker commandInvoker;
    private Switchable porchLight;
    private Lockable frontDoorLock;
    private Thermostat hallwayThermostat;

    @BeforeEach
    void createFreshDevicesAndInvoker() {
        deviceFactory = new DeviceFactory();
        commandInvoker = new CommandInvoker();
        porchLight = deviceFactory.createLight("light-1", "Porch Light");
        frontDoorLock = deviceFactory.createLock("lock-1", "Front Door");
        hallwayThermostat = deviceFactory.createThermostat("thermo-1", "Hallway");
    }

    @Test
    void turnOnThenUndoTurnsLightBackOff() {
        commandInvoker.run(new TurnOnCommand(porchLight));
        assertTrue(porchLight.isOn());

        commandInvoker.undo();
        assertFalse(porchLight.isOn());
    }

    @Test
    void undoOfTurnOnLeavesLightOnIfItWasAlreadyOn() {
        porchLight.turnOn();

        commandInvoker.run(new TurnOnCommand(porchLight));
        commandInvoker.undo();

        assertTrue(porchLight.isOn());
    }

    @Test
    void turnOffThenUndoTurnsLightBackOn() {
        porchLight.turnOn();

        commandInvoker.run(new TurnOffCommand(porchLight));
        assertFalse(porchLight.isOn());

        commandInvoker.undo();
        assertTrue(porchLight.isOn());
    }

    @Test
    void unlockAndLockCanBothBeUndone() {
        commandInvoker.run(new UnlockCommand(frontDoorLock));
        assertFalse(frontDoorLock.isLocked());

        commandInvoker.run(new LockCommand(frontDoorLock));
        assertTrue(frontDoorLock.isLocked());

        commandInvoker.undo();
        assertFalse(frontDoorLock.isLocked());
        commandInvoker.undo();
        assertTrue(frontDoorLock.isLocked());
    }

    @Test
    void setTemperatureUndoRestoresPreviousTarget() {
        commandInvoker.run(new SetTemperatureCommand(hallwayThermostat, 65));
        assertEquals(65, hallwayThermostat.getTargetTemperature());

        commandInvoker.undo();
        assertEquals(Thermostat.DEFAULT_TEMP, hallwayThermostat.getTargetTemperature());
    }

    @Test
    void invalidTemperatureIsNotAddedToHistory() {
        assertThrows(IllegalArgumentException.class,
                () -> commandInvoker.run(new SetTemperatureCommand(hallwayThermostat, 200)));

        assertEquals(Thermostat.DEFAULT_TEMP, hallwayThermostat.getTargetTemperature());
        assertFalse(commandInvoker.hasCommandToUndo());
    }

    @Test
    void undoWithEmptyHistoryReturnsNull() {
        assertNull(commandInvoker.undo());
    }

    @Test
    void sceneRunsInOrderAndUndoesInReverse() {
        List<String> recordedCalls = new ArrayList<>();
        Scene threeStepScene = new Scene("Test", List.of(
                new CallOrderRecordingCommand("A", recordedCalls),
                new CallOrderRecordingCommand("B", recordedCalls),
                new CallOrderRecordingCommand("C", recordedCalls)));

        commandInvoker.run(threeStepScene);
        commandInvoker.undo();

        assertEquals(List.of("execute A", "execute B", "execute C", "undo C", "undo B", "undo A"), recordedCalls);
    }

    @Test
    void goodnightSceneIsUndoneAsOneStep() {
        porchLight.turnOn();
        frontDoorLock.unlock();
        Scene goodnightScene = new Scene("Goodnight", List.of(
                new TurnOffCommand(porchLight),
                new LockCommand(frontDoorLock),
                new SetTemperatureCommand(hallwayThermostat, 65)));

        commandInvoker.run(goodnightScene);
        assertFalse(porchLight.isOn());
        assertTrue(frontDoorLock.isLocked());
        assertEquals(65, hallwayThermostat.getTargetTemperature());
        assertEquals(1, commandInvoker.getExecutedCommandCount());

        commandInvoker.undo();
        assertTrue(porchLight.isOn());
        assertFalse(frontDoorLock.isLocked());
        assertEquals(Thermostat.DEFAULT_TEMP, hallwayThermostat.getTargetTemperature());
    }

    @Test
    void sceneRollsBackIfACommandFails() {
        Scene sceneWithInvalidTemperature = new Scene("Broken", List.of(
                new TurnOnCommand(porchLight),
                new SetTemperatureCommand(hallwayThermostat, 200)));

        assertThrows(IllegalArgumentException.class, () -> commandInvoker.run(sceneWithInvalidTemperature));

        assertFalse(porchLight.isOn());
        assertFalse(commandInvoker.hasCommandToUndo());
    }

    @Test
    void controllerCommandsAreLoggedThroughInjectedListener() {
        ByteArrayOutputStream capturedLogBytes = new ByteArrayOutputStream();
        PrintStream capturedLogOutput = new PrintStream(capturedLogBytes, true, StandardCharsets.UTF_8);
        HomeController homeController = new HomeController(
                deviceFactory, commandInvoker, new ConsoleLogger(capturedLogOutput));
        Switchable kitchenLight = homeController.addLight("light-9", "Kitchen Light");

        homeController.run(new TurnOnCommand(kitchenLight));
        assertTrue(homeController.undo());

        String capturedLogText = capturedLogBytes.toString(StandardCharsets.UTF_8);
        assertTrue(capturedLogText.contains("Kitchen Light turned on"));
        assertTrue(capturedLogText.contains("Kitchen Light turned off"));
    }

    private static class CallOrderRecordingCommand implements Command {
        private final String commandLabel;
        private final List<String> sharedCallLog;

        CallOrderRecordingCommand(String commandLabel, List<String> sharedCallLog) {
            this.commandLabel = commandLabel;
            this.sharedCallLog = sharedCallLog;
        }

        @Override
        public void execute() {
            sharedCallLog.add("execute " + commandLabel);
        }

        @Override
        public void undo() {
            sharedCallLog.add("undo " + commandLabel);
        }

        @Override
        public String getDescription() {
            return commandLabel;
        }
    }
}