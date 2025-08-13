package org.lpc.memory;

import lombok.Getter;
import org.lpc.memory.io.IODevice;
import org.lpc.memory.io.IODeviceManager;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.locks.StampedLock;

public class Memory {
    private final ByteBuffer buffer;
    private final StampedLock lock = new StampedLock();
    private final IODeviceManager ioDeviceManager;
    @Getter
    private volatile boolean initialized = false;

    @SuppressWarnings("ConstantValue")
    public Memory(IODeviceManager ioDeviceManager) {
        this.ioDeviceManager = Objects.requireNonNull(ioDeviceManager,
                "IODeviceManager cannot be null");

        if (MemoryMap.TOTAL_SIZE > Integer.MAX_VALUE) {
            throw new IllegalStateException(
                    "Memory size too large: " + MemoryMap.TOTAL_SIZE);
        }

        this.buffer = ByteBuffer.allocate((int) MemoryMap.TOTAL_SIZE)
                .order(ByteOrder.LITTLE_ENDIAN);
    }

    private int toInt(long address) {
        if (address < 0 || address >= MemoryMap.TOTAL_SIZE) {
            throw new MemoryException("Invalid address: 0x" + Long.toHexString(address));
        }
        return (int) address;
    }

    private void validateRange(long address, int size) {
        if (size < 0) throw new MemoryException("Negative size");
        if (address < 0 || address > MemoryMap.TOTAL_SIZE - size) {
            throw new MemoryException("Address range overflow: 0x" + Long.toHexString(address) +
                    " with size " + size);
        }
    }

    private void validateWrite(long address, int size) {
        if (address < MemoryMap.ROM_END && address + size > MemoryMap.ROM_BASE) {
            throw new MemoryException("Cannot write to ROM: " + Long.toHexString(address));
        }
    }

    private MmioResult handleMmioRead(long address, int size) {
        if (!MemoryMap.isMmioAddress(address)) return MmioResult.NOT_HANDLED;

        IODevice device = ioDeviceManager.getDeviceByAddress(address);
        if (device == null) return MmioResult.NOT_HANDLED;

        long deviceOffset = address - device.getBaseAdress();
        return new MmioResult(true, device.handleRead(deviceOffset, size));
    }

    private boolean handleMmioWrite(long address, long value) {
        if (!MemoryMap.isMmioAddress(address)) return false;

        IODevice device = ioDeviceManager.getDeviceByAddress(address);
        if (device == null) return true; // Ignore writes to unmapped MMIO

        long deviceOffset = address - device.getBaseAdress();
        return device.handleWrite(deviceOffset, value);
    }

    // Read operations
    public byte readByte(long address) {
        return (byte) readWithLock(address, this::readByteUnsafe);
    }

    public short readShort(long address) {
        return (short) readWithLock(address, this::readShortUnsafe);
    }

    public int readInt(long address) {
        return (int) readWithLock(address, this::readIntUnsafe);
    }

    public long readLong(long address) {
        return readWithLock(address, this::readLongUnsafe);
    }

    private long readWithLock(long address, ReadFunction readFunc) {
        long stamp = lock.tryOptimisticRead();
        long result = readFunc.read(address);

        if (!lock.validate(stamp)) {
            stamp = lock.readLock();
            try {
                result = readFunc.read(address);
            } finally {
                lock.unlockRead(stamp);
            }
        }
        return result;
    }

    private long readByteUnsafe(long address) {
        MmioResult mmio = handleMmioRead(address, Byte.BYTES);
        if (mmio.handled) return mmio.value & 0xFF;
        return buffer.get(toInt(address)) & 0xFF;
    }

    private long readShortUnsafe(long address) {
        validateRange(address, Short.BYTES);
        MmioResult mmio = handleMmioRead(address, Short.BYTES);
        if (mmio.handled) return mmio.value & 0xFFFF;
        return buffer.getShort(toInt(address)) & 0xFFFF;
    }

    private long readIntUnsafe(long address) {
        validateRange(address, Integer.BYTES);
        MmioResult mmio = handleMmioRead(address, Integer.BYTES);
        if (mmio.handled) return mmio.value;
        return buffer.getInt(toInt(address));
    }

    private long readLongUnsafe(long address) {
        validateRange(address, Long.BYTES);
        MmioResult mmio = handleMmioRead(address, Long.BYTES);
        if (mmio.handled) return mmio.value;
        return buffer.getLong(toInt(address));
    }

    public void readBytes(long address, byte[] dest, int offset, int length) {
        Objects.requireNonNull(dest, "Destination array is null");
        if (offset < 0 || length < 0 || offset + length > dest.length) {
            throw new MemoryException("Invalid array bounds");
        }
        if (length == 0) return;

        long stamp = lock.readLock();
        try {
            readBytesUnsafe(address, dest, offset, length);
        } finally {
            lock.unlockRead(stamp);
        }
    }

    private void readBytesUnsafe(long address, byte[] dest, int offset, int length) {
        validateRange(address, length);

        if (!MemoryMap.isMmioAddress(address) &&
                !MemoryMap.isMmioAddress(address + length - 1)) {
            // Fast path: Entire block in normal memory
            int memOffset = toInt(address);
            System.arraycopy(buffer.array(), memOffset, dest, offset, length);
        } else {
            // Slow path: MMIO region
            for (int i = 0; i < length; i++) {
                dest[offset + i] = (byte) readByteUnsafe(address + i);
            }
        }
    }

    // Write operations
    public void writeByte(long address, byte value) {
        long stamp = lock.writeLock();
        try {
            writeByteUnsafe(address, value);
        } finally {
            lock.unlockWrite(stamp);
        }
    }

    public void writeShort(long address, short value) {
        long stamp = lock.writeLock();
        try {
            writeShortUnsafe(address, value);
        } finally {
            lock.unlockWrite(stamp);
        }
    }

    public void writeInt(long address, int value) {
        long stamp = lock.writeLock();
        try {
            writeIntUnsafe(address, value);
        } finally {
            lock.unlockWrite(stamp);
        }
    }

    public void writeLong(long address, long value) {
        long stamp = lock.writeLock();
        try {
            writeLongUnsafe(address, value);
        } finally {
            lock.unlockWrite(stamp);
        }
    }

    private void writeByteUnsafe(long address, byte value) {
        validateWrite(address, Byte.BYTES);
        if (!handleMmioWrite(address, value & 0xFF)) {
            buffer.put(toInt(address), value);
        }
    }

    private void writeShortUnsafe(long address, short value) {
        validateWrite(address, Short.BYTES);
        validateRange(address, Short.BYTES);
        if (!handleMmioWrite(address, value & 0xFFFF)) {
            buffer.putShort(toInt(address), value);
        }
    }

    private void writeIntUnsafe(long address, int value) {
        validateWrite(address, Integer.BYTES);
        validateRange(address, Integer.BYTES);
        if (!handleMmioWrite(address, value)) {
            buffer.putInt(toInt(address), value);
        }
    }

    private void writeLongUnsafe(long address, long value) {
        validateWrite(address, Long.BYTES);
        validateRange(address, Long.BYTES);
        if (!handleMmioWrite(address, value)) {
            buffer.putLong(toInt(address), value);
        }
    }

    public void writeBytes(long address, byte[] src, int offset, int length) {
        Objects.requireNonNull(src, "Source array is null");
        if (offset < 0 || length < 0 || offset + length > src.length) {
            throw new MemoryException("Invalid array bounds");
        }
        if (length == 0) return;

        long stamp = lock.writeLock();
        try {
            writeBytesUnsafe(address, src, offset, length);
        } finally {
            lock.unlockWrite(stamp);
        }
    }

    private void writeBytesUnsafe(long address, byte[] src, int offset, int length) {
        validateWrite(address, length);
        validateRange(address, length);

        if (!MemoryMap.isMmioAddress(address) &&
                !MemoryMap.isMmioAddress(address + length - 1)) {
            // Fast path: Entire block in normal memory
            int memOffset = toInt(address);
            System.arraycopy(src, offset, buffer.array(), memOffset, length);
        } else {
            // Slow path: MMIO region
            for (int i = 0; i < length; i++) {
                writeByteUnsafe(address + i, src[offset + i]);
            }
        }
    }

    public void fill(long address, int length, byte value) {
        if (length <= 0) return;

        long stamp = lock.writeLock();
        try {
            validateWrite(address, length);
            validateRange(address, length);

            if (!MemoryMap.isMmioAddress(address) &&
                    !MemoryMap.isMmioAddress(address + length - 1)) {
                // Fast path: Entire block in normal memory
                int memOffset = toInt(address);
                byte[] array = buffer.array();
                Arrays.fill(array, memOffset, memOffset + length, value);
            } else {
                // Slow path: MMIO region
                for (int i = 0; i < length; i++) {
                    writeByteUnsafe(address + i, value);
                }
            }
        } finally {
            lock.unlockWrite(stamp);
        }
    }

    public void initializeROM(byte[] romData) {
        Objects.requireNonNull(romData, "ROM data is null");
        if (romData.length > MemoryMap.ROM_SIZE) {
            throw new MemoryException("ROM data too large");
        }

        long stamp = lock.writeLock();
        try {
            if (initialized) throw new MemoryException("ROM already initialized");
            System.arraycopy(romData, 0, buffer.array(), 0, romData.length);
            initialized = true;
        } finally {
            lock.unlockWrite(stamp);
        }
    }

    // Helper classes
    @FunctionalInterface
    private interface ReadFunction {
        long read(long address);
    }

    private record MmioResult(boolean handled, long value) {
        static final MmioResult NOT_HANDLED = new MmioResult(false, 0);
    }

    public static class MemoryException extends RuntimeException {
        public MemoryException(String message) {
            super(message);
        }
    }
}