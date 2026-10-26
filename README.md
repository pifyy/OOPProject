# SmartHome Simulator

A console app for controlling a simulated smart home: toggle lights, set
thermostats, lock doors, and run scenes like "Goodnight". House setup is loaded
from a config file and device state is saved between runs.

**Team:** Pierce Le, Johnny Sainbayar
**Language:** Java 21 (Maven, JUnit 5)

## Build and run

```
mvn test            # run the tests
mvn compile exec:java
```

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
injection) and replaced in tests. Later in the project, the config loader will
use the factory to build the house from the setup file.

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
information. A device can have a logger, a future "save state on change"
listener and a UI display all subscribed at once, and none of them requires
changes to the device classes.

### 4. State (planned)

**Where:** We plan to use this for the `Thermostat`, with a `ThermostatMode`
interface and one class for each mode (like `HeatingMode`, `CoolingMode`,
`EcoMode` and `OffMode`).

**How:** The thermostat will hold onto its current mode object and let that
object decide the mode-specific stuff, like what `getStatus()` shows and what
temperature range is allowed. Changing modes just means swapping in a
different mode object, instead of flipping a flag that every method has to
check.

**Why:** The thermostat should act differently depending on its mode. Without
State, every method would need an `if` or `switch` on the current mode. With a
class per mode, each mode's rules live in one place, and adding a new mode
doesn't mean touching the existing ones. This isn't implemented yet.

## OO principles

**Coding to abstractions:** Outside of the factory, the code works with
interfaces instead of concrete classes. `DeviceFactory` hands back a
`Switchable` or a `Lockable`, the commands take `Switchable`, `Lockable` and
`TemperatureAdjustable`, `HomeController`