package com.smarthome;

import com.smarthome.command.CommandInvoker;
import com.smarthome.controller.HomeController;
import com.smarthome.factory.DeviceFactory;
import com.smarthome.observer.ConsoleLogger;
import com.smarthome.ui.ConsoleMenu;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        System.out.println("SmartHome Simulator");

        // Everything is created here and passed in (dependency injection).
        HomeController homeController = new HomeController(
                new DeviceFactory(),
                new CommandInvoker(),
                new ConsoleLogger(System.out));

        homeController.addLight("light-1", "Living Room Light");
        homeController.addLight("light-2", "Porch Light");
        homeController.addLock("lock-1", "Front Door");
        homeController.addThermostat("thermo-1", "Hallway Thermostat");

        new ConsoleMenu(homeController, new Scanner(System.in), System.out).start();
    }
}
