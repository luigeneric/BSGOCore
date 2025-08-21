package io.github.luigeneric.core.protocols.zone;


import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.binaryreaderwriter.IProtocolWrite;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

public record ZoneDesc(long zoneCardGuid, LocalDateTime endDate) implements IProtocolWrite
{
    public static ZoneDesc createZoneWithEnd(final long zoneCardGuid, final LocalDateTime endDate)
    {
        return new ZoneDesc(zoneCardGuid, endDate);
    }
    public static ZoneDesc createZoneInfiniteTime(final long zoneCardGuid)
    {
        return new ZoneDesc(zoneCardGuid, null);
    }

    @Override
    public void write(final BgoProtocolWriter bw)
    {
        bw.writeGUID(zoneCardGuid);
        final long localEndSecondsUnix = endDate == null ? 0 :
                endDate.toEpochSecond(ZoneOffset.UTC);
        bw.writeUInt32(localEndSecondsUnix);
    }
}
