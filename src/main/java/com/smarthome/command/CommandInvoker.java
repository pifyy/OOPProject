package com.smarthome.command;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;

public class CommandInvoker {
    private final Deque<Command> executedCommandHistory = new ArrayDeque<>();

    public void run(Command commandToRun) {
        Objects.requireNonNull(commandToRun, "commandToRun");
        commandToRun.execute();
        executedCommandHistory.push(commandToRun);
    }

    public Command undo() {
        if (executedCommandHistory.isEmpty()) {
            return null;
        }
        Command mostRecentCommand = executedCommandHistory.pop();
        mostRecentCommand.undo();
        return mostRecentCommand;
    }

    public boolean hasCommandToUndo() {
        return !executedCommandHistory.isEmpty();
    }

    public int getExecutedCommandCount() {
        return executedCommandHistory.size();
    }
}