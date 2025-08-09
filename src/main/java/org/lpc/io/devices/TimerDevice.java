package org.lpc.io.devices;

import org.lpc.io.IODevice;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class TimerDevice implements IODevice {
    private static final int SIZE = 8;
    private final long baseAddress;
    private final ByteBuffer buffer;

    public TimerDevice(long baseAddress) {
        this.baseAddress = baseAddress;
        this.buffer = ByteBuffer.allocate(SIZE).order(ByteOrder.LITTLE_ENDIAN);
        startTimerThread();
    }

    private void startTimerThread() {
        Thread timerThread = new Thread(() -> {
            while (true) {
                long now = System.currentTimeMillis();
                synchronized (buffer) {
                    buffer.position(0);
                    buffer.putLong(now);
                }
            }
        }, "TimerDeviceThread");

        timerThread.setDaemon(true);
        timerThread.start();
    }

    @Override
    public long getBaseAdress() {
        return baseAddress;
    }

    @Override
    public long getSize() {
        return SIZE;
    }

    @Override
    public String getName() {
        return "TimerDevice";
    }

    @Override
    public boolean handleWrite(long relativeAddress, long value) {
        return false;
    }

    @Override
    public long handleRead(long relativeAddress, int size) {
        if (relativeAddress < 0 || relativeAddress + size > SIZE) {
            throw new IllegalArgumentException("Invalid read address or size");
        }

        byte[] data = new byte[size];
        synchronized (buffer) {
            buffer.position((int) relativeAddress);
            buffer.get(data);
        }

        long result = 0;
        for (int i = 0; i < size; i++) {
            result |= ((long) (data[i] & 0xFF)) << (i * 8);
        }

        return result;
    }
}
