# SmartHome Simulator

A console app for controlling a simulated smart home: toggle lights, set
thermostats, lock doors, run routines like "Goodnight", and undo any of it.

**Team:** Pierce Le, Johnny Sainbayar
**Language:** Java 23 (Gradle, JUnit 5)

## Build and run

In IntelliJ, open the project folder (it imports as a Gradle project), then run
`Main` to start the app. To run the tests, right-click `src/test/java` and pick
**Run 'All Tests'**.

From a terminal:

```
./gradlew test   # run the tests
./gradlew run    # start the app
```

## Test coverage

To measure coverage in IntelliJ, right-click `src/test/java` and pick
**More Run/Debug > Run 'All Tests' with Coverage**.

![Test coverage](images/TestCoverage.png)

## Using the app

The app starts with a sample house (two lights, a front door lock and a
thermostat), shows each device's id and status, then shows a menu:

```
  1) Show status
  2) Turn a device on
  3) Turn a device off
  4) Lock a door
  5) Unlock a door
  6) Set a thermostat
  7) Goodnight
  8) Leave home
  9) Welcome home
  u) Undo last action
  q) Quit
```

Type a choice and press Enter. Options 2 to 6 then ask for a device id, such as
`light-1`, and option 6 also asks for a temperature (50 to 90°F). Each change is
logged as it happens, and `u` undoes the last action, including a whole routine
like Goodnight. Bad input such as an unknown id or a temperature out of range
prints an error and returns to the menu.

The menu is `com.smarthome.ui.ConsoleMenu`. Each choice maps to a `MenuOption`
in a map instead of a `switch`, and every option goes through the
`HomeController` facade.

## Design patterns

### 1. Factory

**Where:** `com.smarthome.factory.DeviceFactory`

**How:** `DeviceFactory` is the only class that creates concrete devices. It has
one method per device type (`createLight`, `createLock`, `createThermostat`),
and each returns an abstraction instead of the concrete class: `createLight`
returns a `Switchable` and `createLock` returns a `Lockable`. The rest of the
program asks the factory for devices and never calls `new Light(...)` or
`new Lock(...)` itself.

**Why:** Only the factory depends on the concrete device classes, so adding a
new device type, or changing how one is built, touches a single class.
`DeviceFactory` is a normal object rather than a set of static methods, so it
can be passed into `HomeController` through the constructor (dependency
injection) and replaced in tests.

### 2. Command

**Where:**
- **Command interface:** `com.smarthome.command.Command`, with `execute()`,
  `undo()` and `getDescription()`
- **Concrete commands:** `TurnOnCommand`, `TurnOffCommand`, `LockCommand`,
  `UnlockCommand` and `SetTemperatureCommand`
- **Scenes:** `Scene`, which bundles several commands together, like "Goodnight"
- **Invoker:** `com.smarthome.command.CommandInvoker`, which keeps track of what
  can be undone

**How:** Every action on a device is its own command object. When a command
runs, it first remembers what the device looked like (was the light on? was
the door locked? what was the old temperature?) and then makes the change.
That way `undo()` can put things back exactly how they were. For example, if
a light was already on and you run "turn on" and then undo it, the light stays
on. The commands work with `Switchable`, `Lockable` and `TemperatureAdjustable`
instead of `Light` or `Lock`, so the same `TurnOffCommand` works on a light or
a thermostat.

A `Scene` is just a command made of other commands. It runs them in order, and
when you undo it, it reverses them backwards so the last change gets undone
first. If something in the middle of a scene fails, it undoes the steps it
already did, so the house never ends up half-changed.

`CommandInvoker` keeps a stack (`Deque<Command>`) of everything that's been
run. `run()` executes a command and then adds it to the stack. If the command
throws an error, like a temperature that's out of range, it never gets added.
`undo()` takes the most recent command off the stack and undoes it.
`HomeController` just passes `run()` and `undo()` along to the invoker.

**Why:** It keeps the "what should happen" part separate from the devices
themselves. Since each action is an object, we can save it, undo it, group it
into a scene or print what it does. Adding a new action later just means
writing one new command class, with no changes to the invoker, controller or
devices. And because a scene is also a command, undoing all of "Goodnight"
takes one undo.

### 3. Observer

**Where:**
- **Observer interface:** `com.smarthome.observer.DeviceListener`, with the
  single method `onDeviceChanged(Device device, String change)`
- **Subject:** `com.smarthome.device.AbstractDevice`, the base class of every
  device. It keeps the list of listeners and provides `addListener`,
  `removeListener` and `notifyListeners`.
- **Concrete observer:** `ConsoleLogger`, which prints each change to the console

**How:** `Light`, `Lock` and `Thermostat` call `notifyListeners(...)` whenever
their state actually changes, for example `"turned on"` or
`"target set to 68°F"`. If nothing changed, such as turning on a light that is
already on or setting a temperature out of range, no notification is sent, so
listeners only hear about real changes. Because the subject logic lives in
`AbstractDevice`, every device supports listeners without repeating that code.

**Why:** Devices don't know who is listening or what listeners do with the
information. A device could have the logger, a sound alert and a status
display all subscribed at once, and none of them would require changes to the
device classes.

### 4. Facade

**Where:** `com.smarthome.controller.HomeController`

**How:** `HomeController` is the one class `Main` and the console menu talk to. Behind it are the
`DeviceFactory`, the `CommandInvoker`, the listeners and every command class,
but callers never touch those directly. `addLight(...)` builds the device
through the factory, attaches the listener and keeps track of it, all in one
call. Single actions take just a device id: `turnOn("light-1")` finds the
device, wraps the action in the right command and runs it through the invoker,
so it can be undone. The routines `goodnight()`, `leaveHome()` and `welcomeHome()` each build a
`Scene` out of commands for every device in the house and run it through the
invoker. For example, `goodnight()` turns off every light, locks every door and
sets every thermostat to 65°F. Because a routine runs as a single scene, one
`undo()` reverses all of it.

**Why:** Without the facade, running "Goodnight" means creating a command for
each device, putting them in a `Scene` and handing that to the invoker, and
`Main` would have to know about all of those classes. With the facade it's one
method call. If the house gets new devices, the routines pick them up
automatically, and callers don't change.

## OO principles

**Coding to abstractions:** Outside of the factory, the code works with
interfaces instead of concrete classes. `DeviceFactory` hands back a
`Switchable` or a `Lockable`, the commands take `Switchable`, `Lockable` and
`TemperatureAdjustable`, `HomeController` stores
devices as `Device`, and `Main` only ever holds interface types.

**Polymorphism:** `printStatus()` loops over every device and calls
`getStatus()`, and each class answers in its own way (`ON`, `LOCKED`,
`ON, target 65°F`) with no `instanceof` or `switch`. The same goes for
`CommandInvoker`, which calls `execute()` and `undo()` without knowing whether it
has a single command or a whole `Scene`.

**Dependency injection:** `HomeController` gets its `DeviceFactory`,
`CommandInvoker` and `DeviceListener` through its constructor instead of
creating them, and `ConsoleLogger` gets the `PrintStream` it writes to. Tests use
this to pass in a listener that does nothing, or a `PrintStream` they can read
back.
