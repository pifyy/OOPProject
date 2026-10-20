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
_TODO_

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

### 4. State
_TODO_

## OO principles
_TODO: coding to abstractions, polymorphism, dependency injection._
