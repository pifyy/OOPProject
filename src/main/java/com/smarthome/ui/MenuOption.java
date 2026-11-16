package com.smarthome.ui;

import java.util.Objects;

/** One line of the console menu: the text it shows and what it does when picked. */
class MenuOption {
    private final String label;
    private final Runnable action;

    MenuOption(String label, Runnable action) {
        this.label = Objects.requireNonNull(label, "label");
        this.action = Objects.requireNonNull(action, "action");
    }

    String getLabel() {
        return label;
    }

    void run() {
        action.run();
    }
}
