package org.lpc.io;

import java.util.ArrayList;
import java.util.List;

public class IODeviceManager {
    private final List<IODevice> devices;

    public IODeviceManager() {
        this.devices = new ArrayList<>();
    }

    public IODevice getDeviceByAddress(long address) {
        for (IODevice device : devices) {
            if (device.getAddress() == address) {
                return device;
            }
        }
        throw new IllegalArgumentException("No device found at address: " + address);
    }

    public IODevice getDeviceByName(String name) {
        for (IODevice device : devices) {
            if (device.getName().equals(name)) {
                return device;
            }
        }
        throw new IllegalArgumentException("No device found with name: " + name);
    }

    public List<IODevice> getAllDevices() {
        return devices;
    }
}
