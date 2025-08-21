package io.github.luigeneric.core.protocols.notification;

import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.binaryreaderwriter.IProtocolWrite;
import io.github.luigeneric.enums.SectorEventTaskType;

import java.time.LocalDateTime;


public record SectorEventProtectTask(byte index, SectorEventTaskType sectorEventTaskType, SectorEventTaskSubType sectorEventTaskSubType,
                                     SectorEventState sectorEventState, long vipObjectId, LocalDateTime endTime
) implements IProtocolWrite
{
    @Override
    public void write(final BgoProtocolWriter bw)
    {
        bw
                .writeByte(index)
                .writeDesc(sectorEventState)
                .writeDesc(sectorEventTaskType)
                .writeDesc(sectorEventTaskSubType)
                .writeUInt32(vipObjectId)
                .writeDateTime(endTime);
    }
}
