package io.github.luigeneric.binaryreaderwriter;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Objects;

public class BgoTimeStamp
{
    public static ZoneOffset zoneOffset = ZoneOffset.UTC;

    private long epochMillis;

    public static BgoTimeStamp now()
    {
        return new BgoTimeStamp(System.currentTimeMillis());
    }

    public BgoTimeStamp(final long epochMillis)
    {
        this.epochMillis = epochMillis;
    }

    public BgoTimeStamp(final double epochSeconds)
    {
        this((long) epochSeconds * 1000);
    }

    public BgoTimeStamp(final LocalDateTime localDateTime)
    {
        this(localDateTime.toInstant(zoneOffset).toEpochMilli());
    }


    public void set(final LocalDateTime localDateTime) throws NullPointerException
    {
        Objects.requireNonNull(localDateTime);
        this.epochMillis = localDateTime.toInstant(zoneOffset).toEpochMilli();
    }

    public void set(final long ms)
    {
        this.epochMillis = ms;
    }

    public void setFromSeconds(final double seconds)
    {
        this.epochMillis = (long) (seconds * 1000);
    }

    public double getInSeconds()
    {
        return this.epochMillis * 0.001;
    }

    public long getEpochMillis()
    {
        return epochMillis;
    }

    public LocalDateTime getLocalDate()
    {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(epochMillis), zoneOffset);
    }

    public boolean isMinutesBeforeDate(final long minutes, final LocalDateTime date)
    {
        return this.getLocalDate().plusMinutes(minutes).isBefore(date);
    }

    /**
     * Duration between this and the given end
     *
     * @param end must be higher than this to produce a result greater 0
     * @return duration object
     */
    public Duration getDuration(final LocalDateTime end)
    {
        return Duration.between(this.getLocalDate(), end).abs();
    }

    public Duration getDuration(final BgoTimeStamp end)
    {
        return getDuration(end.getLocalDate());
    }

    public long totalDurationMsBetween(final BgoTimeStamp end)
    {
        return Math.abs(getDuration(end).toMillis());
    }

    @Override
    public String toString()
    {
        return this.getLocalDate().toString();
    }
}
