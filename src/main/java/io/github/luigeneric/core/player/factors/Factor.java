package io.github.luigeneric.core.player.factors;



import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.binaryreaderwriter.IProtocolWrite;
import io.github.luigeneric.enums.FactorSource;
import io.github.luigeneric.enums.FactorType;
import io.github.luigeneric.templates.augments.AugmentFactorTemplate;
import io.github.luigeneric.templates.augments.FactorTypeRecord;
import io.github.luigeneric.utils.ICopy;
import io.github.luigeneric.utils.collections.IServerItem;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class Factor implements IProtocolWrite, IServerItem, ICopy<Factor>
{
    private int serverID;
    private final FactorType factorType;
    private final FactorSource factorSource;
    private final float value;
    private final LocalDateTime endTime;

    public Factor(final int serverID, final FactorType factorType, final FactorSource factorSource,
                  final float value, final LocalDateTime endTime) throws NullPointerException
    {
        final BigDecimal bd = new BigDecimal(value).setScale(2, RoundingMode.HALF_UP);
        this.serverID = serverID;
        this.factorType = factorType;
        this.factorSource = factorSource;
        this.value = bd.floatValue();
        this.endTime = endTime;
    }

    @Override
    public Factor copy()
    {
        return new Factor(0, this.factorType, this.factorSource, this.value, this.endTime);
    }

    public static Factor fromTimeUnit(final FactorType type, final FactorSource source, final float value,
                                      final LocalDateTime now, final TimeUnit timeUnit, final long duration)
    {
        return new Factor(
                0,
                type,
                source,
                value,
                now.plusNanos(timeUnit.toNanos(duration))
        );
    }
    public static Factor fromEndTime(final FactorType factorType, final FactorSource factorSource, final float value,
                                     final LocalDateTime endTime)
    {
        return new Factor(0, factorType, factorSource, value, endTime);
    }
    public static Factor fromStartTime(final FactorType factorType, final FactorSource factorSource, final float value,
                                final LocalDateTime startTime, final long durationHours)
    {
        return new Factor(0, factorType, factorSource, value, startTime.plusHours(durationHours));
    }

    public static Collection<Factor> fromTemplate(final AugmentFactorTemplate augmentFactorTemplate)
    {
        return fromTemplate(augmentFactorTemplate, -1);
    }
    public static Collection<Factor> fromTemplate(final AugmentFactorTemplate augmentFactorTemplate, final int customDurationHours)
    {
        final List<Factor> factors = new ArrayList<>();

        for (final FactorTypeRecord factorTypeRecord : augmentFactorTemplate.getFactorTypeRecords())
        {
            final int duration = customDurationHours == -1 ? augmentFactorTemplate.getActiveTimeInHours() : customDurationHours;
            final Factor factor = Factor
                    .fromStartTime(
                            factorTypeRecord.type(), augmentFactorTemplate.getFactorSource(), factorTypeRecord.value(),
                            LocalDateTime.now(Clock.systemUTC()),
                            duration
                    );
            factors.add(factor);
        }
        return factors;
    }


    @Override
    public void write(final BgoProtocolWriter bw)
    {
        bw.writeUInt16(this.serverID);
        bw.writeByte((byte) this.factorType.intValue);
        bw.writeByte((byte) this.factorSource.intValue);
        bw.writeSingle(this.value);
        bw.writeUInt32(endTime.toEpochSecond(ZoneOffset.UTC));
    }



    public FactorType getFactorType()
    {
        return factorType;
    }

    public FactorSource getFactorSource()
    {
        return factorSource;
    }

    public float getValue()
    {
        return value;
    }

    @Override
    public int getServerID()
    {
        return this.serverID;
    }
    public void setServerID(final int serverID)
    {
        this.serverID = serverID;
    }

    public LocalDateTime getEndTime()
    {
        return endTime;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Factor factor = (Factor) o;

        if (serverID != factor.serverID) return false;
        if (Float.compare(factor.value, value) != 0) return false;
        if (factorType != factor.factorType) return false;
        if (factorSource != factor.factorSource) return false;
        return Objects.equals(endTime, factor.endTime);
    }

    @Override
    public int hashCode()
    {
        int result = serverID;
        result = 31 * result + (factorType != null ? factorType.hashCode() : 0);
        result = 31 * result + (factorSource != null ? factorSource.hashCode() : 0);
        result = 31 * result + (value != +0.0f ? Float.floatToIntBits(value) : 0);
        result = 31 * result + (endTime != null ? endTime.hashCode() : 0);
        return result;
    }

    @Override
    public String toString()
    {
        return "Factor{" +
                "factorType=" + factorType +
                ", factorSource=" + factorSource +
                ", value=" + value +
                ", endTime=" + endTime +
                '}';
    }
}
