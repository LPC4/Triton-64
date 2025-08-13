package org.lpc.memory.io;

public interface IODevice {
    long getBaseAdress();
    long getSize();
    String getName();

    boolean handleWrite(long relativeAddress, long value);
    long handleRead(long relativeAddress, int size);
}