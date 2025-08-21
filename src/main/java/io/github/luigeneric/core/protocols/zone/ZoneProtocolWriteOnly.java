package io.github.luigeneric.core.protocols.zone;


import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.core.protocols.ProtocolID;
import io.github.luigeneric.core.protocols.WriteOnlyProtocol;

import java.util.Collection;

public class ZoneProtocolWriteOnly extends WriteOnlyProtocol
{
    public ZoneProtocolWriteOnly()
    {
        super(ProtocolID.Zone);
    }

    public BgoProtocolWriter writeActiveZones(final Collection<ZoneDesc> zoneDescs)
    {
        final BgoProtocolWriter bw = newMessage();
        bw.writeDesc(ServerMessage.ActiveZones);

        bw.writeDescCollection(zoneDescs);

        return bw;
    }

    public BgoProtocolWriter writeUpcomingZones(final Collection<ZoneDesc> upcomingZoneDescs)
    {
        final BgoProtocolWriter bw = newMessage();
        bw.writeDesc(ServerMessage.UpcomingZones);

        bw.writeDescCollection(upcomingZoneDescs);

        return bw;
    }

    public BgoProtocolWriter writeScoreKillSpam(final KillSpamServerMessage killSpamServerMessage)
    {
        return newMessage()
                .writeMsgType(ServerMessage.ScoreKillspam.value)
                .writeDesc(killSpamServerMessage);
    }

    /**
     * Will add/remove nemesis indicator above head
     * @param addNemesis
     * @param playerIds
     * @return
     */
    public BgoProtocolWriter writeScoreNemesisUpdate(final boolean addNemesis, final Collection<Long> playerIds)
    {
        return newMessage()
                .writeMsgType(ServerMessage.ScoreNemesisUpdate.value)
                .writeBoolean(addNemesis)
                .writeUInt32Collection(playerIds);
    }

    /**
     * Will add/remove spree indicator above head
     * @param addSpree
     * @param playerIds
     * @return
     */
    public BgoProtocolWriter writeScoreSpreeUpdate(final boolean addSpree, final Collection<Long> playerIds)
    {
        return newMessage()
                .writeMsgType(ServerMessage.ScoreSpreeUpdate.value)
                .writeBoolean(addSpree)
                .writeUInt32Collection(playerIds);
    }

    /**
     * Add topgun indicator above head
     * @param playerTournamentLeaderId
     * @return
     */
    public BgoProtocolWriter writeScoreLeaderUpdate(final long playerTournamentLeaderId)
    {
        return newMessage()
                .writeMsgType(ServerMessage.ScoreLeaderUpdate.value)
                .writeUInt32(playerTournamentLeaderId);
    }

    public BgoProtocolWriter writeScoreboardUpdate(final Collection<TournamentRankingData> tournamentRankingDatas)
    {
        return newMessage()
                .writeMsgType(ServerMessage.ScoreboardUpdate.value)
                .writeDescCollection(tournamentRankingDatas);
    }


    public BgoProtocolWriter writeAdmissionStatus(final long guid, final boolean hasPaidAdmission)
    {
        return newMessage()
                .writeDesc(ServerMessage.AdmissionStatus)
                .writeGUID(guid)
                .writeBoolean(hasPaidAdmission);
    }
}

