package io.github.luigeneric.core.protocols.ranking;

import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.core.protocols.ProtocolID;
import io.github.luigeneric.core.protocols.WriteOnlyProtocol;

public class RankingProtocolWriteOnly extends WriteOnlyProtocol
{
    public RankingProtocolWriteOnly()
    {
        super(ProtocolID.Ranking);
    }


    public BgoProtocolWriter writeRankingGroup(final RankingGroupRecord rankingGroupRecord)
    {
        final BgoProtocolWriter bw = newMessage();
        bw.writeMsgType(ServerMessage.ReplyRankingCounter.shortValue);

        bw.writeDesc(rankingGroupRecord);

        return bw;
    }

    public BgoProtocolWriter writeReplyRankingTournamentPlayer(final long rank, final long position,
                                                               final double score1, final double score2, final double score3)
    {
        final BgoProtocolWriter bw = newMessage();
        bw.writeMsgType(ServerMessage.ReplyRankingTournamentPlayer.shortValue);

        bw.writeUInt32(rank);
        bw.writeUInt32(position);
        bw.writeDouble(score1);
        bw.writeDouble(score2);
        bw.writeDouble(score3);

        return bw;
    }
}
