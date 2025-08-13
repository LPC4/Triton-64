// VirtualMachine.java
package org.lpc;

import lombok.Getter;
import org.lpc.cpu.Cpu;
import org.lpc.memory.io.IODeviceManager;
import org.lpc.memory.io.devices.KeyboardDevice;
import org.lpc.memory.io.devices.TimerDevice;
import org.lpc.memory.Memory;
import org.lpc.memory.MemoryMap;

@Getter
public class VirtualMachine {
    private final Memory memory;
    private final Cpu cpu;
    private final KeyboardDevice keyboardDevice;
    private final TimerDevice timerDevice;

    public VirtualMachine() {
        IODeviceManager ioDeviceManager = new IODeviceManager();
        memory = new Memory(ioDeviceManager);
        cpu = new Cpu(memory);
        keyboardDevice = new KeyboardDevice(MemoryMap.MMIO_BASE);
        timerDevice = new TimerDevice(MemoryMap.MMIO_BASE + KeyboardDevice.SIZE);
        ioDeviceManager.addDevices(keyboardDevice, timerDevice);
    }

}