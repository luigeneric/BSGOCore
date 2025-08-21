package io.github.luigeneric.core.protocols.ranking;


import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.binaryreaderwriter.IProtocolWrite;
import io.github.luigeneric.enums.Faction;

/**
 * Wrapper around player scores associated to a rank type
 * @param rank
 * @param playerID
 * @param name
 * @param faction
 * @param score1
 * @param score2
 * @param score3
 */
public record RankDescription(
        long rank, long playerID, String name, Faction faction,
        double score1, double score2, double score3
) implements IProtocolWrite
{

    @Override
    public void write(final BgoProtocolWriter bw)
    {
        bw.writeUInt32(rank);
        bw.writeUInt32(playerID);
        bw.writeString(name);
        bw.writeUInt32(faction.value);
        bw.writeDouble(score1);
        bw.writeDouble(score2);
        bw.writeDouble(score3);
    }
}
