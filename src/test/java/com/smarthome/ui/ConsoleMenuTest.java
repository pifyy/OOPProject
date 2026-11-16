package com.smarthome.ui;

import com.smarthome.command.CommandInvoker;
import com.smarthome.controller.HomeController;
import com.smarthome.device.Lockable;
import com.smarthome.device.Switchable;
import com.smarthome.device.Thermostat;
import com.smarthome.factory.DeviceFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConsoleMenuTest {
    private HomeController homeController;
    private Switchable kitchenLight;
    private Lockable frontDoorLock;
    private Thermostat hallwayThermostat;
    private ByteArrayOutputStream capturedMenuBytes;

    @BeforeEach
    void createHouse() {
        homeController = new HomeController(
                new DeviceFactory(), new CommandInvoker(), (changedDevice, changeDescription) -> { });
        kitchenLight = homeController.addLight("light-1", "Kitchen Light");
        frontDoorLock = homeController.addLock("lock-1", "Front Door");
        hallwayThermostat = homeController.addThermostat("thermo-1", "Hallway");
        capturedMenuBytes = new ByteArrayOutputStream();
    }

    /** Runs the menu as if the user typed each line, then returns everything it printed. */
    private String runMenuWithTypedLines(String... typedLines) {
        Scanner fakeKeyboard = new Scanner(String.join("\n", typedLines) + "\n");
        PrintStream capturedMenuOutput = new PrintStream(capturedMenuBytes, true, StandardCharsets.UTF_8);
        new ConsoleMenu(homeController, fakeKeyboard, capturedMenuOutput).start();
        return capturedMenuBytes.toString(StandardCharsets.UTF_8);
    }

    @Test
    void turnOnChoiceTurnsOnTheTypedDevice() {
        runMenuWithTypedLines("2", "light-1", "q");

        assertTrue(kitchenLight.isOn());
    }

    @Test
    void setThermostatChoiceUsesTheTypedTemperature() {
        runMenuWithTypedLines("6", "thermo-1", "68", "q");

        assertEquals(68, hallwayThermostat.getTargetTemperature());
    }

    @Test
    void goodnightChoiceThenUndoPutsTheHouseBack() {
        frontDoorLock.unlock();

        runMenuWithTypedLines("7", "u", "q");

        assertFalse(frontDoorLock.isLocked());
        assertEquals(Thermostat.DEFAULT_TEMP, hallwayThermostat.getTargetTemperature());
    }

    @Test
    void badInputShowsAnErrorAndTheMenuKeepsGoing() {
        String menuText = runMenuWithTypedLines("6", "thermo-1", "200", "2", "light-1", "q");

        assertTrue(menuText.contains("Error: Target must be between"));
        assertTrue(kitchenLight.isOn());
    }

    @Test
    void unknownChoiceAndUnknownDeviceAreReported() {
        String menuText = runMenuWithTypedLines("x", "2", "garage-9", "q");

        assertTrue(menuText.contains("Unknown choice 'x'"));
        assertTrue(menuText.contains("No light or thermostat with id 'garage-9'"));
    }

    @Test
    void menuStopsWhenInputRunsOut() {
        String menuText = runMenuWithTypedLines("1");

        assertTrue(menuText.contains("[light-1] Kitchen Light: OFF"));
    }
}
