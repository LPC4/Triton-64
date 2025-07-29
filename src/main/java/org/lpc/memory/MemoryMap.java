package org.lpc.memory;

public class MemoryMap {
    public static final long ROM_BASE = 0x0000_0000_0000_0000L;
    public static final long ROM_SIZE = 128 * 1024;                      // 128 KB ROM

    public static final long RAM_BASE = ROM_BASE + ROM_SIZE;             // RAM starts after ROM
    public static final long RAM_SIZE = 512 * 1024 * 1024;               // 512 MB RAM

    public static final long MMIO_BASE = RAM_BASE + RAM_SIZE;            // MMIO starts after RAM
    public static final long MMIO_SIZE = 2 * 1024 * 1024;                // 2 MB MMIO

    public static final long FB_BASE = MMIO_BASE + MMIO_SIZE;            // Framebuffer starts after MMIO
    public static final long FB_SIZE = 16 * 1024 * 1024;                 // 16 MB framebuffer

    // The stack and heap share a combined space in RAM
    public static final long STACK_HEAP_SIZE = 128L * 1024 * 1024;       // 128 MB stack+heap
    public static final long STACK_BASE = RAM_BASE + RAM_SIZE;           // stack grows downward
    public static final long HEAP_BASE = STACK_BASE - STACK_HEAP_SIZE;   // heap grows upward
}
