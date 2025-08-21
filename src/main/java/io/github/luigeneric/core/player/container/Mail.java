package io.github.luigeneric.core.player.container;


import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.binaryreaderwriter.BgoTimeStamp;
import io.github.luigeneric.binaryreaderwriter.IProtocolWrite;
import io.github.luigeneric.core.player.container.containerids.MailContainerID;
import io.github.luigeneric.core.player.container.visitors.ContainerVisitor;
import io.github.luigeneric.templates.shipitems.ShipItem;
import io.github.luigeneric.utils.collections.IServerItem;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

public class Mail implements IServerItem, IProtocolWrite
{
    private int serverID;
    private final long mailTemplateCardGuid;
    private MailStatus mailStatus;
    private final BgoTimeStamp received;
    private final MailContainer mailContainer;
    private final String[] parameters;

    public Mail(int serverID, long mailTemplateCardGuid, MailStatus mailStatus,
                LocalDateTime received, List<ShipItem> items, String[] parameters, final long userID)
    {
        this.serverID = serverID;
        this.mailTemplateCardGuid = mailTemplateCardGuid;
        this.mailStatus = mailStatus;
        this.received = new BgoTimeStamp(received);
        this.mailContainer = new MailContainer(items, userID);
        this.parameters = parameters;
    }
    public Mail(final long mailTemplateCardGuid, List<ShipItem> shipItems, final long userID)
    {
        this(0, mailTemplateCardGuid, MailStatus.Unread, LocalDateTime.now(Clock.systemUTC()), shipItems, new String[0], userID);
    }

    @Override
    public void write(BgoProtocolWriter bw)
    {
        bw.writeUInt16(this.serverID);
        bw.writeGUID(this.mailTemplateCardGuid);
        bw.writeByte(this.mailStatus.getValue());
        bw.writeDateTime(received.getLocalDate());

        bw.writeDescCollection(this.mailContainer.getAllShipItems());

        bw.writeStringArray(this.parameters);
    }

    @Override
    public int getServerID()
    {
        return this.serverID;
    }

    public void setServerID(int freeServerID)
    {
        this.serverID = freeServerID;
    }

    public enum MailStatus
    {
        Normal,
        Unread;

        public byte getValue()
        {
            return (byte) this.ordinal();
        }
    }

    public MailStatus getMailStatus()
    {
        return mailStatus;
    }

    public void setMailStatus(final MailStatus mailStatus)
    {
        this.mailStatus = mailStatus;
    }

    public long getMailTemplateCardGuid()
    {
        return mailTemplateCardGuid;
    }

    public LocalDateTime getReceived()
    {
        return received.getLocalDate();
    }

    public MailContainer getMailContainer()
    {
        return mailContainer;
    }

    public String[] getParameters()
    {
        return parameters;
    }

    public static class MailContainer extends ItemList
    {
        public MailContainer(final List<ShipItem> items, final long userID)
        {
            super(new MailContainerID(), items, userID);
        }

        @Override
        public void accept(final ContainerVisitor containerVisitor)
        {
            containerVisitor.visit(this);
        }
    }
}
