package io.github.luigeneric.core.protocols.ranking;

import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.binaryreaderwriter.IProtocolWrite;
import io.github.luigeneric.enums.RankingGroup;
import io.github.luigeneric.enums.RankingType;

import java.time.LocalDateTime;
import java.util.List;

/**
 *
 * @param rankingGroup the group might be kills, damage, arena, ...
 * @param rankingType delta or regular
 * @param rankDescriptions
 * @param totalEntries
 * @param lastUpdate
 */
public record RankingGroupRecord(
        RankingGroup rankingGroup,
        RankingType rankingType,
        List<RankDescription> rankDescriptions,
        long totalEntries,
        LocalDateTime lastUpdate
) implements IProtocolWrite
{
    @Override
    public void write(BgoProtocolWriter bw)
    {
        bw.writeUInt16(rankingGroup.value);
        bw.writeUInt16(rankingType.value);
        bw.writeDescCollection(rankDescriptions);
        bw.writeUInt32(totalEntries);
        bw.writeDateTime(lastUpdate);
    }
}
