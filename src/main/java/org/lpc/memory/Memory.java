package org.lpc.memory;

import org.lpc.Globals;

import static org.lpc.memory.MemoryMap.*;

/**
 * Simulates physical memory layout consisting of ROM, RAM, MMIO, and framebuffer.
 * All memory is backed by a single byte array.
 *
 * Thread-safe implementation using synchronized methods to handle concurrent access
 * from multiple I/O threads.
 *
 * Addresses must be within defined memory regions.
 *
 * Reads and writes multi-byte values using little-endian byte order:
 *  - Least significant byte stored at lowest address.
 *  - Most significant byte stored at highest address.
 */
public class Memory {
    public final byte[] memory;

    public Memory() {
        long totalSize = ROM_SIZE + RAM_SIZE + MMIO_SIZE + FB_SIZE;
        //noinspection ConstantValue
        if (totalSize > Integer.MAX_VALUE) {
            throw new IllegalStateException("Memory size too large for a Java array");
        }
        memory = new byte[(int) totalSize];
    }

    // Converts a memory address to an offset in the byte array.
    // Throws IllegalArgumentException if address is out of bounds.
    private int toOffset(long address) {
        if (address < 0 || address >= ROM_SIZE + RAM_SIZE + MMIO_SIZE + FB_SIZE)
            throw new IllegalArgumentException("Invalid memory address: " + Long.toHexString(address));
        return (int) address;  // direct mapping
    }

    public synchronized byte readByte(long address) {
        return memory[toOffset(address)];
    }

    public synchronized void writeByte(long address, byte value) {
        if (address >= ROM_BASE && address < ROM_BASE + ROM_SIZE)
            throw new IllegalArgumentException("Cannot write to ROM at address: " + Long.toHexString(address));
        memory[toOffset(address)] = value;
    }

    public synchronized int readInt(long address) {
        int b0 = Byte.toUnsignedInt(readByte(address));
        int b1 = Byte.toUnsignedInt(readByte(address + 1));
        int b2 = Byte.toUnsignedInt(readByte(address + 2));
        int b3 = Byte.toUnsignedInt(readByte(address + 3));
        return (b3 << 24) | (b2 << 16) | (b1 << 8) | b0;
    }

    public synchronized void writeInt(long address, int value) {
        writeByte(address, (byte) (value & 0xFF));
        writeByte(address + 1, (byte) ((value >> 8) & 0xFF));
        writeByte(address + 2, (byte) ((value >> 16) & 0xFF));
        writeByte(address + 3, (byte) ((value >> 24) & 0xFF));
    }

    public synchronized long readLong(long address) {
        long lo = Integer.toUnsignedLong(readInt(address));
        long hi = Integer.toUnsignedLong(readInt(address + 4));
        return (hi << 32) | lo;
    }

    public synchronized void writeLong(long address, long value) {
        writeInt(address, (int) (value & 0xFFFFFFFFL));
        writeInt(address + 4, (int) ((value >> 32) & 0xFFFFFFFFL));
    }
}