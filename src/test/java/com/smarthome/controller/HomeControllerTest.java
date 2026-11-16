package com.smarthome.controller;

import com.smarthome.command.CommandInvoker;
import com.smarthome.device.Lockable;
import com.smarthome.device.Switchable;
import com.smarthome.device.Thermostat;
import com.smarthome.factory.DeviceFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HomeControllerTest {
    private HomeController homeController;
    private Switchable livingRoomLight;
    private Switchable porchLight;
    private Lockable frontDoorLock;
    private Thermostat hallwayThermostat;

    @BeforeEach
    void createHouseWithEveryKindOfDevice() {
        homeController = new HomeController(
                new DeviceFactory(), new CommandInvoker(), (changedDevice, changeDescription) -> { });
        livingRoomLight = homeController.addLight("light-1", "Living Room Light");
        porchLight = homeController.addLight("light-2", "Porch Light");
        frontDoorLock = homeController.addLock("lock-1", "Front Door");
        hallwayThermostat = homeController.addThermostat("thermo-1", "Hallway");
    }

    @Test
    void goodnightTurnsOffLightsLocksDoorsAndLowersTemperature() {
        livingRoomLight.turnOn();
        porchLight.turnOn();
        frontDoorLock.unlock();

        homeController.goodnight();

        assertFalse(livingRoomLight.isOn());
        assertFalse(porchLight.isOn());
        assertTrue(frontDoorLock.isLocked());
        assertEquals(HomeController.GOODNIGHT_TEMPERATURE, hallwayThermostat.getTargetTemperature());
    }

    @Test
    void oneUndoReversesTheWholeGoodnightRoutine() {
        livingRoomLight.turnOn();
        frontDoorLock.unlock();
        hallwayThermostat.setTargetTemperature(72);

        homeController.goodnight();
        assertTrue(homeController.undo());

        assertTrue(livingRoomLight.isOn());
        assertFalse(porchLight.isOn());
        assertFalse(frontDoorLock.isLocked());
        assertEquals(72, hallwayThermostat.getTargetTemperature());
    }

    @Test
    void leaveHomeTurnsTheThermostatOff() {
        homeController.leaveHome();

        assertFalse(hallwayThermostat.isOn());
        assertTrue(frontDoorLock.isLocked());
    }

    @Test
    void welcomeHomeTurnsLightsOnWithoutUnlockingDoors() {
        hallwayThermostat.turnOff();

        homeController.welcomeHome();

        assertTrue(livingRoomLight.isOn());
        assertTrue(porchLight.isOn());
        assertTrue(hallwayThermostat.isOn());
        assertEquals(HomeController.WELCOME_HOME_TEMPERATURE, hallwayThermostat.getTargetTemperature());
        assertTrue(frontDoorLock.isLocked());
    }
}
