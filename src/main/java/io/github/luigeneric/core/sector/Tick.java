package io.github.luigeneric.core.sector;

import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.binaryreaderwriter.IProtocolWrite;
import io.github.luigeneric.utils.ICopy;
import io.github.luigeneric.utils.Utils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

@Slf4j
public class Tick implements IProtocolWrite, Comparable<Tick>, ICopy<Tick>
{
    public static final long TICK_RATE = 10;
    public static final long TIME_DELAY_MS = 100;
    @Getter
    private int value;
    /**
     * -- GETTER --
     *  The creation timestamp
     *
     * @return the originTime when the sector got started
     */
    @Getter
    private final long originTime;
    private long currentTime;
    private long previousTime;

    public Tick(final long originTime)
    {
        this(0, originTime, 0, 0);
    }

    private Tick(final int value, final long originTime, final long currentTime, final long previousTime)
    {
        this.value = value;
        this.originTime = originTime;
        this.currentTime = currentTime;
        this.previousTime = previousTime;
    }

    public static Tick valueOf(final int value)
    {
        return new Tick(value, 0, 0, 0);
    }
    private Tick(final Tick tickToCopy)
    {
        this.originTime = tickToCopy.originTime;
        this.value = tickToCopy.value;
        this.currentTime = tickToCopy.currentTime;
        this.previousTime = tickToCopy.previousTime;
    }

    @Override
    public Tick copy()
    {
        return new Tick(this);
    }

    /**
     * Waits until the current tick is not equal the old tick and increments the current value by 1
     * to ensure each tick is given its iteration, use value++ instead of assigning the current tick value considern the server might lag
     */
    public void waitForNextTick()
    {
        //time setup
        this.previousTime = currentTime;
        this.currentTime = System.currentTimeMillis();
        long diff = this.currentTime - this.originTime;
        long currentTick = diff / TIME_DELAY_MS;

        while (this.value == currentTick)
        {
            final long timeLeft = TIME_DELAY_MS - (diff % TIME_DELAY_MS);
            waitForDelay(timeLeft);
            //time refresh
            //this.previousTime = currentTime;
            this.currentTime = System.currentTimeMillis();
            diff = this.currentTime - this.originTime;
            currentTick = diff / TIME_DELAY_MS;
        }

        this.value++;
    }


    private void waitForDelay(final long msToWait)
    {
        try
        {
            TimeUnit.MILLISECONDS.sleep(msToWait);
        } catch (InterruptedException e)
        {
            throw new RuntimeException(e);
        }
    }

    public double getCurrentTimeDouble()
    {
        return this.currentTime * 0.001;
    }

    public long getTimePassedSinceStart(final TimeUnit timeUnit)
    {
        final long passedMs = getMsPassedSinceStart();
        return timeUnit.convert(passedMs, TimeUnit.MILLISECONDS);
    }
    public long getMsPassedSinceStart()
    {
        return this.currentTime - this.originTime;
    }

    /**
     * Returns the actual delta time passed in seconds (may be 0)
     * @return dt in seconds
     */
    public float getActualDeltaTime()
    {
        return (this.currentTime - this.previousTime) * 0.001f;
    }

    /**
     * Returns a static dt, always 0.1 seconds at the current tickrate.
     * @return
     */
    public float getDeltaTime()
    {
        return (float) TICK_RATE / (float) TIME_DELAY_MS;
    }

    public final long getTimeStamp()
    {
        return ((long) this.value) * TIME_DELAY_MS + this.originTime;
    }

    @Override
    public void write(final BgoProtocolWriter bw)
    {
        bw.writeInt32(this.value);
    }

    @Override
    public int compareTo(final Tick other)
    {
        return Integer.compare(this.value, other.value);
    }


    @Override
    public int hashCode()
    {
        return value;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Tick tick = (Tick) o;

        return value == tick.value;
    }

    @Override
    public String toString()
    {
        return "tick{" +
                "{"+ value +
                '}';
    }

    public boolean isBehindBy(final Tick other, final TimeUnit timeUnit, final long value)
    {
        final long delta = this.getTickDiff(other);
        if (delta >= 0) return false;

        final long behindValue = Utils.timeToTicks(timeUnit, value);
        return -delta >= behindValue;
    }

    /**
     * Calculates the difference in ticks
     * @param other the tick to compare
     * @return first positive value if this is greater(advanced) than other and first negative value if this is less(post) other.
     * 0 if the ticks are the same
     */
    public long getTickDiff(final Tick other)
    {
        return this.value - other.value;
    }
}