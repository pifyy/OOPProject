package com.smarthome.ui;

import com.smarthome.controller.HomeController;
import com.smarthome.device.Device;

import java.io.PrintStream;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;

/**
 * Text menu for controlling the house. Each choice is looked up in a map of
 * MenuOptions instead of a switch statement, and every option goes through
 * the HomeController facade.
 */
public class ConsoleMenu {
    private final HomeController homeController;
    private final Scanner userInput;
    private final PrintStream menuOutput;
    private final Map<String, MenuOption> optionsByChoice = new LinkedHashMap<>();
    private boolean running;

    public ConsoleMenu(HomeController homeController, Scanner userInput, PrintStream menuOutput) {
        this.homeController = Objects.requireNonNull(homeController, "homeController");
        this.userInput = Objects.requireNonNull(userInput, "userInput");
        this.menuOutput = Objects.requireNonNull(menuOutput, "menuOutput");

        optionsByChoice.put("1", new MenuOption("Show status", this::showStatus));
        optionsByChoice.put("2", new MenuOption("Turn a device on",
                () -> homeController.turnOn(ask("Device id: "))));
        optionsByChoice.put("3", new MenuOption("Turn a device off",
                () -> homeController.turnOff(ask("Device id: "))));
        optionsByChoice.put("4", new MenuOption("Lock a door",
                () -> homeController.lock(ask("Lock id: "))));
        optionsByChoice.put("5", new MenuOption("Unlock a door",
                () -> homeController.unlock(ask("Lock id: "))));
        optionsByChoice.put("6", new MenuOption("Set a thermostat",
                () -> homeController.setTemperature(ask("Thermostat id: "),
                        Integer.parseInt(ask("Temperature (°F): ")))));
        optionsByChoice.put("7", new MenuOption("Goodnight", homeController::goodnight));
        optionsByChoice.put("8", new MenuOption("Leave home", homeController::leaveHome));
        optionsByChoice.put("9", new MenuOption("Welcome home", homeController::welcomeHome));
        optionsByChoice.put("u", new MenuOption("Undo last action", this::undo));
        optionsByChoice.put("q", new MenuOption("Quit", () -> running = false));
    }

    /** Shows the menu and handles choices until the user quits or input runs out. */
    public void start() {
        running = true;
        showStatus();
        while (running) {
            printMenu();
            String choice = ask("> ");
            if (choice == null) {
                return;
            }
            MenuOption chosenOption = optionsByChoice.get(choice.toLowerCase());
            if (chosenOption == null) {
                menuOutput.println("Unknown choice '" + choice + "'");
                continue;
            }
            try {
                chosenOption.run();
            } catch (IllegalArgumentException badInput) {
                // Covers unknown ids, out-of-range temperatures and non-numbers.
                menuOutput.println("Error: " + badInput.getMessage());
            }
        }
        menuOutput.println("Goodbye!");
    }

    private void printMenu() {
        menuOutput.println();
        for (Map.Entry<String, MenuOption> option : optionsByChoice.entrySet()) {
            menuOutput.println("  " + option.getKey() + ") " + option.getValue().getLabel());
        }
    }

    private void showStatus() {
        menuOutput.println("Devices:");
        for (Device device : homeController.getAllDevices()) {
            menuOutput.println("  [" + device.getId() + "] " + device.getName() + ": " + device.getStatus());
        }
    }

    private void undo() {
        if (!homeController.undo()) {
            menuOutput.println("Nothing to undo");
        }
    }

    /** Prints a prompt and returns the trimmed line typed, or null if input has ended. */
    private String ask(String prompt) {
        menuOutput.print(prompt);
        if (!userInput.hasNextLine()) {
            running = false;
            return null;
        }
        return userInput.nextLine().trim();
    }
}
