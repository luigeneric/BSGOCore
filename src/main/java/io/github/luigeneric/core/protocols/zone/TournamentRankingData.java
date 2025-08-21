package io.github.luigeneric.core.protocols.zone;

import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.binaryreaderwriter.IProtocolWrite;

/**
 * RankingData to send
 * @param playerId the corresponding player
 * @param joinRank ???? only used in comparable
 * @param score total pts (on client side displayed as int
 * @param kills kill counter inside the event
 * @param deaths death counter inside the event
 */
public record TournamentRankingData(long playerId, long joinRank, float score, long kills,
                                    long deaths) implements IProtocolWrite
{

    @Override
    public void write(BgoProtocolWriter bw)
    {
        bw
                .writeUInt32(playerId)
                .writeUInt32(joinRank)
                .writeSingle(score)
                .writeUInt32(kills)
                .writeUInt32(deaths);
    }
}
