package com.smarthome.command;

import java.util.List;
import java.util.Objects;

public class Scene implements Command {
    private final String sceneName;
    private final List<Command> commandsInRunOrder;

    public Scene(String sceneName, List<Command> commandsInRunOrder) {
        this.sceneName = Objects.requireNonNull(sceneName, "sceneName");
        this.commandsInRunOrder = List.copyOf(commandsInRunOrder);
    }

    @Override
    public void execute() {
        for (int commandIndex = 0; commandIndex < commandsInRunOrder.size(); commandIndex++) {
            try {
                commandsInRunOrder.get(commandIndex).execute();
            } catch (RuntimeException failure) {
                undoCommandsBeforeIndexInReverseOrder(commandIndex);
                throw failure;
            }
        }
    }

    @Override
    public void undo() {
        undoCommandsBeforeIndexInReverseOrder(commandsInRunOrder.size());
    }

    private void undoCommandsBeforeIndexInReverseOrder(int stopIndexExclusive) {
        for (int commandIndex = stopIndexExclusive - 1; commandIndex >= 0; commandIndex--) {
            commandsInRunOrder.get(commandIndex).undo();
        }
    }

    @Override
    public String getDescription() {
        return "Scene: " + sceneName;
    }
}