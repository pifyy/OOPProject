package com.smarthome.factory;

import com.smarthome.device.Lockable;
import com.smarthome.device.Switchable;
import com.smarthome.device.Thermostat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DeviceFactoryTest {
    private DeviceFactory deviceFactory;

    @BeforeEach
    void createFreshFactory() {
        deviceFactory = new DeviceFactory();
    }

    @Test
    void createLightReturnsSwitchableThatStartsOff() {
        Switchable kitchenLight = deviceFactory.createLight("light-1", "Kitchen Light");

        assertEquals("light-1", kitchenLight.getId());
        assertEquals("Kitchen Light", kitchenLight.getName());
        assertFalse(kitchenLight.isOn());
    }

    @Test
    void createLockReturnsLockableThatStartsLocked() {
        Lockable frontDoorLock = deviceFactory.createLock("lock-1", "Front Door");

        assertEquals("Front Door", frontDoorLock.getName());
        assertTrue(frontDoorLock.isLocked());
    }

    @Test
    void createThermostatStartsOnAtDefaultTemperature() {
        Thermostat hallwayThermostat = deviceFactory.createThermostat("thermo-1", "Hallway");

        assertTrue(hallwayThermostat.isOn());
        assertEquals(Thermostat.DEFAULT_TEMP, hallwayThermostat.getTargetTemperature());
    }

    @Test
    void eachCallCreatesANewIndependentDevice() {
        Switchable firstLight = deviceFactory.createLight("light-1", "A");
        Switchable secondLight = deviceFactory.createLight("light-1", "A");

        assertNotSame(firstLight, secondLight);
        firstLight.turnOn();
        assertFalse(secondLight.isOn());
    }
}