package org.lpc.io;

public interface IODevice {
    long getAddress();
    long getSize();
    String getName();

    default boolean handleWrite(long relativeAddress, int size, long value) {
        return false;
    }

    default long handleRead(long relativeAddress, int size) {
        return 0;
    }
}