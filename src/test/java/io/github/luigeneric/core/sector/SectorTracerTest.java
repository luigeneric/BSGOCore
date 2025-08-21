package io.github.luigeneric.core.sector;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;

class SectorTracerTest
{
    private SectorTracer sectorTracer;
    @BeforeEach
    void setupEach()
    {
        sectorTracer = new SectorTracer();
    }

    @Test
    void startTimer()
    {
        sectorTracer.startTimer("start");
        boolean actual = sectorTracer.isTimerStarted("start");
        assertTrue(actual);
    }

    @Test
    void endTimer()
    {
        sectorTracer.startTimer("start");
        sectorTracer.endTimer("start");
        boolean actual = sectorTracer.hasTimedEvent("start");
        assertTrue(actual);
    }

    @Test
    void isEventTimeAboveTick() throws InterruptedException
    {
        sectorTracer.startTimer("test");
        TimeUnit.SECONDS.sleep(1);
        sectorTracer.endTimer("test");
        boolean actual = sectorTracer.isEventTimeAboveMs(100);
        assertTrue(actual);
    }
}