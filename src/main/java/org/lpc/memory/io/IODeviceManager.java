package org.lpc.memory.io;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class IODeviceManager {
    private final List<IODevice> devices;
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    public IODeviceManager() {
        this.devices = new ArrayList<>();
    }

    public void addDevice(IODevice device) {
        if (device == null) {
            throw new IllegalArgumentException("Device cannot be null");
        }

        lock.writeLock().lock();
        try {
            // Check for overlapping address ranges
            for (IODevice existingDevice : devices) {
                if (addressRangesOverlap(existingDevice, device)) {
                    throw new IllegalArgumentException(
                        String.format("Device address range [0x%X - 0x%X] overlaps with existing device '%s' [0x%X - 0x%X]",
                                device.getBaseAdress(),
                                device.getBaseAdress() + device.getSize() - 1,
                                existingDevice.getName(),
                                existingDevice.getBaseAdress(),
                                existingDevice.getBaseAdress() + existingDevice.getSize() - 1)
                    );
                }
            }
            devices.add(device);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void addDevices(IODevice... newDevices) {
        for (IODevice device : newDevices) {
            addDevice(device);
        }
    }

    /**
     * Get the device that contains the specified address within its address range.
     * This is the correct method for MMIO lookups.
     */
    public IODevice getDeviceByAddress(long address) {
        lock.readLock().lock();
        try {
            for (IODevice device : devices) {
                long deviceStart = device.getBaseAdress();
                long deviceEnd = deviceStart + device.getSize() - 1;

                if (address >= deviceStart && address <= deviceEnd) {
                    return device;
                }
            }
            return null; // No device found - this is normal for unmapped addresses
        } finally {
            lock.readLock().unlock();
        }
    }

    private boolean addressRangesOverlap(IODevice device1, IODevice device2) {
        long start1 = device1.getBaseAdress();
        long end1 = start1 + device1.getSize() - 1;
        long start2 = device2.getBaseAdress();
        long end2 = start2 + device2.getSize() - 1;

        // Two ranges overlap if: start1 <= end2 && start2 <= end1
        return start1 <= end2 && start2 <= end1;
    }
}