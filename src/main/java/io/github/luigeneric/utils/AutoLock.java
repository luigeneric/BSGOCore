package io.github.luigeneric.utils;

import java.util.concurrent.locks.Lock;

public class AutoLock implements AutoCloseable
{
    private final Lock lock;

    public AutoLock(final Lock lock)
    {
        this.lock = lock;
        this.lock.lock();
    }


    @Override
    public void close()
    {
        this.lock.unlock();
    }
}