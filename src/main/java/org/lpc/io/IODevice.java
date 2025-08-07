package org.lpc.io;

public interface IODevice {
    long getAddress();
    long getSize();
    String getName();

    boolean handleWrite(long relativeAddress, long value);
    long handleRead(long relativeAddress);
}