package com.smarthome;

import com.smarthome.command.Command;
import com.smarthome.command.CommandInvoker;
import com.smarthome.command.LockCommand;
import com.smarthome.command.Scene;
import com.smarthome.command.SetTemperatureCommand;
import com.smarthome.command.TurnOffCommand;
import com.smarthome.command.TurnOnCommand;
import com.smarthome.command.UnlockCommand;
import com.smarthome.controller.HomeController;
import com.smarthome.device.Lockable;
import com.smarthome.device.Switchable;
import com.smarthome.device.Thermostat;
import com.smarthome.factory.DeviceFactory;
import com.smarthome.observer.ConsoleLogger;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        System.out.println("SmartHome Simulator");

        HomeController homeController = new HomeController(
                new DeviceFactory(),
                new CommandInvoker(),
                new ConsoleLogger(System.out));

        Switchable livingRoomLight = homeController.addLight("light-1", "Living Room Light");
        Switchable porchLight = homeController.addLight("light-2", "Porch Light");
        Lockable frontDoorLock = homeController.addLock("lock-1", "Front Door");
        Thermostat hallwayThermostat = homeController.addThermostat("thermo-1", "Hallway Thermostat");

        System.out.println("\nStarting state:");
        homeController.printStatus();

        System.out.println("\nEvening:");
        printDescriptionAndRun(homeController, new TurnOnCommand(livingRoomLight));
        printDescriptionAndRun(homeController, new TurnOnCommand(porchLight));
        printDescriptionAndRun(homeController, new UnlockCommand(frontDoorLock));
        printDescriptionAndRun(homeController, new SetTemperatureCommand(hallwayThermostat, 72));

        Scene goodnightScene = new Scene("Goodnight", List.of(
                new TurnOffCommand(livingRoomLight),
                new TurnOffCommand(porchLight),
                new LockCommand(frontDoorLock),
                new SetTemperatureCommand(hallwayThermostat, 65)));

        System.out.println();
        printDescriptionAndRun(homeController, goodnightScene);
        System.out.println("\nAfter Goodnight:");
        homeController.printStatus();

        System.out.println("\n> Undo (reverses the whole Goodnight scene)");
        homeController.undo();
        System.out.println("\nAfter undo:");
        homeController.printStatus();
    }

    private static void printDescriptionAndRun(HomeController homeController, Command commandToRun) {
        System.out.println("> " + commandToRun.getDescription());
        homeController.run(commandToRun);
    }
}