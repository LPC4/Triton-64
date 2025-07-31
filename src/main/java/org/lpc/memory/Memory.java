package org.lpc.memory;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Objects;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Simulates physical memory layout consisting of ROM, RAM, MMIO, and framebuffer.
 * All memory is backed by a single byte array with ByteBuffer for endianness handling.
 * Thread-safe implementation using ReadWriteLock for better concurrent performance.
 * Addresses must be within defined memory regions.
 * All multi-byte values use little-endian byte order.
 */
public class Memory {
    private final ByteBuffer buffer;
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    public Memory() {
        if (MemoryMap.TOTAL_SIZE > Integer.MAX_VALUE) {
            throw new IllegalStateException(
                    String.format("Memory size (%d bytes) exceeds maximum array size", MemoryMap.TOTAL_SIZE)
            );
        }

        // Use ByteBuffer for better endianness handling
        this.buffer = ByteBuffer.allocate((int) MemoryMap.TOTAL_SIZE)
                .order(ByteOrder.LITTLE_ENDIAN);
    }

    /**
     * Converts a memory address to an offset in the byte array.
     * @param address the memory address
     * @return the array offset
     * @throws IllegalArgumentException if address is out of bounds
     */
    private int toOffset(long address) {
        if (!MemoryMap.isValidAddress(address)) {
            throw new IllegalArgumentException(
                    String.format("Invalid memory address: 0x%016X", address)
            );
        }
        return (int) address;  // Direct mapping since we start at 0
    }

    /**
     * Validates that a memory range doesn't exceed bounds.
     */
    private void validateRange(long address, int size) {
        if (address < 0 || address + size > MemoryMap.TOTAL_SIZE) {
            throw new IllegalArgumentException(
                    String.format("Memory range [0x%016X, 0x%016X) exceeds bounds",
                            address, address + size)
            );
        }
    }

    /**
     * Checks if writing to the given address is allowed.
     */
    private void validateWrite(long address) {
        if (MemoryMap.isRomAddress(address)) {
            throw new IllegalArgumentException(
                    String.format("Cannot write to ROM at address: 0x%016X", address)
            );
        }
    }

    // Read operations use read lock for better concurrency

    public byte readByte(long address) {
        lock.readLock().lock();
        try {
            return buffer.get(toOffset(address));
        } finally {
            lock.readLock().unlock();
        }
    }

    public short readShort(long address) {
        lock.readLock().lock();
        try {
            validateRange(address, Short.BYTES);
            return buffer.getShort(toOffset(address));
        } finally {
            lock.readLock().unlock();
        }
    }

    public int readInt(long address) {
        lock.readLock().lock();
        try {
            validateRange(address, Integer.BYTES);
            return buffer.getInt(toOffset(address));
        } finally {
            lock.readLock().unlock();
        }
    }

    public long readLong(long address) {
        lock.readLock().lock();
        try {
            validateRange(address, Long.BYTES);
            return buffer.getLong(toOffset(address));
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Reads a block of bytes from memory.
     * @param address starting address
     * @param dest destination array
     * @param offset offset in destination array
     * @param length number of bytes to read
     * @throws IllegalArgumentException if parameters are invalid
     */
    public void readBytes(long address, byte[] dest, int offset, int length) {
        Objects.requireNonNull(dest, "Destination array cannot be null");
        if (offset < 0 || length < 0 || offset + length > dest.length) {
            throw new IllegalArgumentException("Invalid destination array bounds");
        }

        lock.readLock().lock();
        try {
            validateRange(address, length);
            int memOffset = toOffset(address);
            System.arraycopy(buffer.array(), memOffset, dest, offset, length);
        } finally {
            lock.readLock().unlock();
        }
    }

    // Write operations use write lock

    public void writeByte(long address, byte value) {
        lock.writeLock().lock();
        try {
            validateWrite(address);
            buffer.put(toOffset(address), value);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void writeShort(long address, short value) {
        lock.writeLock().lock();
        try {
            validateWrite(address);
            validateRange(address, Short.BYTES);
            buffer.putShort(toOffset(address), value);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void writeInt(long address, int value) {
        lock.writeLock().lock();
        try {
            validateWrite(address);
            validateRange(address, Integer.BYTES);
            buffer.putInt(toOffset(address), value);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void writeLong(long address, long value) {
        lock.writeLock().lock();
        try {
            validateWrite(address);
            validateRange(address, Long.BYTES);
            buffer.putLong(toOffset(address), value);
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Writes a block of bytes to memory.
     * @param address starting address
     * @param src source array
     * @param offset offset in source array
     * @param length number of bytes to write
     * @throws IllegalArgumentException if parameters are invalid
     */
    public void writeBytes(long address, byte[] src, int offset, int length) {
        Objects.requireNonNull(src, "Source array cannot be null");
        if (offset < 0 || length < 0 || offset + length > src.length) {
            throw new IllegalArgumentException("Invalid source array bounds");
        }

        lock.writeLock().lock();
        try {
            validateWrite(address);
            validateRange(address, length);
            int memOffset = toOffset(address);
            System.arraycopy(src, offset, buffer.array(), memOffset, length);
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Fills a memory region with a specific byte value.
     * @param address starting address
     * @param length number of bytes to fill
     * @param value the byte value to fill with
     */
    public void fill(long address, int length, byte value) {
        lock.writeLock().lock();
        try {
            validateWrite(address);
            validateRange(address, length);
            int offset = toOffset(address);
            for (int i = 0; i < length; i++) {
                buffer.put(offset + i, value);
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Returns the total size of allocated memory.
     */
    public long getTotalSize() {
        return MemoryMap.TOTAL_SIZE;
    }

    /**
     * Initializes ROM with the provided data. This method bypasses the normal
     * ROM write protection and should only be called during system initialization.
     * @param romData the ROM data to write
     * @throws IllegalArgumentException if ROM data is too large or null
     */
    public void initializeROM(byte[] romData) {
        Objects.requireNonNull(romData, "ROM data cannot be null");
        if (romData.length > MemoryMap.ROM_SIZE) {
            throw new IllegalArgumentException(
                    String.format("ROM data too large: %d bytes, maximum %d bytes",
                            romData.length, MemoryMap.ROM_SIZE)
            );
        }

        lock.writeLock().lock();
        try {
            // Direct access to underlying array for ROM initialization
            System.arraycopy(romData, 0, buffer.array(), 0, romData.length);
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Creates a read-only view of a memory region.
     * The returned ByteBuffer shares the same underlying data but prevents modifications.
     */
    public ByteBuffer getReadOnlyView(long address, int length) {
        lock.readLock().lock();
        try {
            validateRange(address, length);
            int offset = toOffset(address);
            ByteBuffer slice = buffer.asReadOnlyBuffer();
            slice.position(offset).limit(offset + length);
            return slice.slice();
        } finally {
            lock.readLock().unlock();
        }
    }
}