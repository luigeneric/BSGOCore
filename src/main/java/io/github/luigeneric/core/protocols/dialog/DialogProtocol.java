package io.github.luigeneric.core.protocols.dialog;

import io.github.luigeneric.binaryreaderwriter.BgoProtocolReader;
import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.core.ProtocolContext;
import io.github.luigeneric.core.User;
import io.github.luigeneric.core.galaxy.Galaxy;
import io.github.luigeneric.core.player.Player;
import io.github.luigeneric.core.protocols.BgoProtocol;
import io.github.luigeneric.core.protocols.ProtocolID;
import io.github.luigeneric.core.protocols.ProtocolRegistryWriteOnly;
import io.github.luigeneric.core.protocols.player.PlayerProtocolWriteOnly;
import io.github.luigeneric.utils.BgoRandom;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.List;

@Slf4j
public class DialogProtocol extends BgoProtocol
{
    private MissionDistributor missionDistributor;
    public DialogProtocol(ProtocolContext ctx)
    {
        super(ProtocolID.Dialog, ctx);
    }

    @Override
    public void injectUser(User user)
    {
        super.injectUser(user);
        this.missionDistributor = new MissionDistributor(user().getPlayer(), ctx.galaxy(), ctx.rng());
    }

    @Override
    public void parseMessage(final int msgType, final BgoProtocolReader br) throws IOException
    {
        final ClientMessage clientMessage = ClientMessage.forValue(msgType);
        if (clientMessage == null)
            return;

        switch (clientMessage)
        {
            case Say ->
            {
                final byte index = br.readByte();
                log.warn(user().getUserLog() + "Dialog say " + index);
                if (index != 1)
                {
                    log.warn(user().getUserLog() + "Wrong index given " + index);
                    return;
                }
                final Player player = user().getPlayer();
                //doesn't matter, just by default get assignments
                user().send(writeStopped());

                final boolean updated = missionDistributor.updateMissionBook();
                log.debug("User updated missions ? {} {}", updated, player.getCounterFacade().missionBook());
                if (updated)
                {
                    PlayerProtocolWriteOnly playerProtocolWriteOnly = ProtocolRegistryWriteOnly.getProtocol(ProtocolID.Player);
                    user().send(playerProtocolWriteOnly.writeMissions(player.getCounterFacade().missionBook()));
                }
            }
            case Advance ->
            {
                log.warn(user().getUserLog() + "Advance daylies");
                Remark remark1 = new Remark((byte) 1, "%$bgo.npc_no2.Phrase__04a77746-d78a-4e4e-98bf-68f988d63b0e__0%", "");

                final List<Remark> remarks = List.of(remark1);
                user().send(writePcRemarks(remarks));
            }
            case Stop ->
            {
                user().send(writeStopped());
            }

            default ->
            {
                log.warn(user().getUserLog() + "DialogProtocol not implemented for version: " + msgType);
            }
        }
    }
    public BgoProtocolWriter writeStopped()
    {
        final BgoProtocolWriter bw = newMessage();
        bw.writeMsgType(ServerMessage.Stopped.getValue());
        return bw;
    }
    public BgoProtocolWriter writeNpcRemark(final Remark remark)
    {
        final BgoProtocolWriter bw = newMessage();
        bw.writeMsgType(ServerMessage.NpcRemark.getValue());
        bw.writeDesc(remark);
        return bw;
    }
    public BgoProtocolWriter writePcRemarks(final List<Remark> remarks)
    {
        final BgoProtocolWriter bw = newMessage();
        bw.writeMsgType(ServerMessage.PcRemarks.getValue());

        bw.writeDescCollection(remarks);

        return bw;
    }

    public enum ServerMessage
    {
        NpcRemark,
        PcRemarks,
        Stopped,
        Action;

        public static final int SIZE = Short.SIZE;

        public short getValue()
        {
            return (short) this.ordinal();
        }

        public static ServerMessage forValue(final short value)
        {
            return values()[value];
        }
    }

    enum ClientMessage
    {
        Say,
        Advance,
        Stop;

        public static final int SIZE = Short.SIZE;

        public short getValue()
        {
            return (short) this.ordinal();
        }

        public static ClientMessage forValue(final int value)
        {
            final ClientMessage[] _values = values();
            if (value < 0 || value > 2)
                return null;
            return _values[value];
        }
    }

}
