package io.github.luigeneric.core.protocols.community;

import io.github.luigeneric.binaryreaderwriter.BgoProtocolReader;
import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.chatapi.ChatApi;
import io.github.luigeneric.core.ChatAccessBlocker;
import io.github.luigeneric.core.User;
import io.github.luigeneric.core.UsersContainer;
import io.github.luigeneric.core.community.guild.GuildRegistry;
import io.github.luigeneric.core.community.party.PartyRegistry;
import io.github.luigeneric.core.player.Player;
import io.github.luigeneric.core.protocols.BgoProtocol;
import io.github.luigeneric.core.protocols.ProtocolID;
import io.github.luigeneric.core.protocols.notification.NotificationProtocol;
import jakarta.enterprise.inject.spi.CDI;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
public class CommunityProtocol extends BgoProtocol
{
    private final GuildProcessing guildProcessing;
    private final PartyProcessing partyProcessing;
    private final CommunityProtocolWriteOnly writer;
    private boolean chatSessionInitialized;
    private boolean chatConnected;
    private final ChatApi chatApi;
    private final ChatAccessBlocker chatAccessBlocker;

    public CommunityProtocol(final UsersContainer usersContainer,
                             final PartyRegistry partyRegistry,
                             final GuildRegistry guildRegistry,
                             final ChatAccessBlocker chatAccessBlocker
    )
    {
        super(ProtocolID.Community);
        this.chatApi = CDI.current().select(ChatApi.class).get();
        this.writer = new CommunityProtocolWriteOnly();
        this.guildProcessing = new GuildProcessing(usersContainer, guildRegistry, writer);
        this.partyProcessing = new PartyProcessing(usersContainer, partyRegistry, writer);
        this.chatSessionInitialized = false;
        this.chatAccessBlocker = chatAccessBlocker;
    }

    public CommunityProtocolWriteOnly writer()
    {
        return this.writer;
    }

    @Override
    public void injectUser(final User user)
    {
        super.injectUser(user);
        this.guildProcessing.injectUser(user);
        this.partyProcessing.injectUser(user);
        this.chatSessionInitialized = false;
    }

    @Override
    public void onDisconnect()
    {
        partyProcessing.clearInvites();
    }

    public GuildProcessing getGuildProcessing()
    {
        return guildProcessing;
    }

    public PartyProcessing getPartyProcessing()
    {
        return partyProcessing;
    }

    @Override
    public void parseMessage(final int msgType, final BgoProtocolReader br) throws IOException
    {
        final ClientMessage clientMessage = ClientMessage.forValue((short) msgType);
        final Player userChar = this.user.getPlayer();
        partyProcessing.processMessage(clientMessage, br);
        guildProcessing.processMessage(clientMessage, br);
        switch (clientMessage)
        {
            case RecruitLevel ->
            {
                final long level = 0;
                user.send(writer.writeRequiredRecruitLevel(level));
            }
            case FriendInvite ->
            {
                //request add friend
                final long friendId = br.readUint32();
            }
            case FriendAccept ->
            {
                final long playerId = br.readUint32();
                //how the fuck bigpoint... who came to this glorious idea
                // that 1 is true and 2 is false WHY JUST NOT USE THE STUFF YOU ALREADY USE (readBoolean/writeBoolean)
                final byte value = br.readByte();
                if (!(value == 2 || value == 1))
                {
                    //TODO error
                }
                final boolean accepted = value != 2;
            }
            case FriendRemove ->
            {
                final long playerID = br.readUint32();
            }
            case IgnoreAdd ->
            {
                final String playerName = br.readString();
            }
            case ChatFleetAllowed ->
            {
                br.readUint16(); //this value is always 1
            }
            case ChatAuthFailed ->
            {
                final String sessionId = br.readString();
                log.info("ChatAuthFailed! " + user.getUserLogSimple());
                this.chatConnected = false;
            }
            case ChatConnected ->
            {
                final String sessionId = br.readString();
                this.chatConnected = true;
                chatApi.sendUserPosition(user.getPlayer().getUserID(), user.getPlayer().getLocation().getSectorID());
            }
        }
    }

    public boolean isChatConnected()
    {
        return chatConnected;
    }

    public void sendChatSessionId(final String chatSessionID, final long chatProjectID,
                                  final String chatLanguage, final String chatServerUrl)
    {

        final boolean canAccessChat = this.chatAccessBlocker.checkUserCanAccessChat(this.userId);
        if (!canAccessChat)
        {
            final NotificationProtocol notificationProtocol = user.getProtocol(ProtocolID.Notification);
            user.send(notificationProtocol.writer().writeDebugMessage("User chat blacklist"));
            return;
        }
        final BgoProtocolWriter bw = writer.writeChatSessionId(chatSessionID, chatProjectID, chatLanguage, chatServerUrl);
        this.chatSessionInitialized = true;
        user.send(bw);
    }

    public boolean isChatSessionInitialized()
    {
        return chatSessionInitialized;
    }

    public enum GuildJoinError
    {
        Guild_doesnt_exist,
        Guild_already_joined,
        Guild_wrong_faction;

        public byte getByte()
        {
            return (byte) (this.ordinal() + 1);
        }
    }
}
