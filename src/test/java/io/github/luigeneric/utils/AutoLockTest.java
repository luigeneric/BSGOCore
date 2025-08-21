package io.github.luigeneric.utils;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Disabled
class AutoLockTest
{
    @Test
    void name()
    {
        Lock lock = new ReentrantLock();
        try(var autoLock = new AutoLock(lock))
        {
            System.out.println("hi");
        }
    }
}