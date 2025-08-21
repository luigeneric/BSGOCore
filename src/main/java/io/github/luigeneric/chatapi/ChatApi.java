package io.github.luigeneric.chatapi;

public interface ChatApi
{
    void start();
    void stop();


    void sendUserPosition(final long playerId, final long sectorId);

    void userJoinedPartyId(final long playerId, final long partyId);
    void userLeftParty(final long playerId);

    void userJoinedGuild(final long playerId, final long guildId);
    void userLeftGuild(final long playerId);
}
