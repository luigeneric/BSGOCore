package io.github.luigeneric.core.protocols.room;


import io.github.luigeneric.binaryreaderwriter.BgoProtocolReader;
import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.core.player.location.Location;
import io.github.luigeneric.core.player.location.SpaceLocation;
import io.github.luigeneric.core.protocols.BgoProtocol;
import io.github.luigeneric.core.protocols.ProtocolID;
import io.github.luigeneric.core.protocols.dialog.DialogProtocol;
import io.github.luigeneric.core.protocols.dialog.Remark;
import io.github.luigeneric.core.protocols.scene.SceneProtocol;
import io.github.luigeneric.enums.GameLocation;
import io.github.luigeneric.templates.startupconfig.GameServerParamsConfig;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Objects;

@Slf4j
public class RoomProtocol extends BgoProtocol
{
    private final GameServerParamsConfig gameServerParams;
    public RoomProtocol(final GameServerParamsConfig gameServerParams)
    {
        super(ProtocolID.Room);
        this.gameServerParams = gameServerParams;
    }

    @Override
    public void parseMessage(final int msgType, final BgoProtocolReader br) throws IOException
    {
        final ClientMessage clientMessage =
                Objects.requireNonNull(ClientMessage.valueOf(msgType), "ClientMessage was " + msgType);

        final SceneProtocol sceneProtocol = user.getProtocol(ProtocolID.Scene);
        switch (clientMessage)
        {
            case Talk ->
            {
                final String npc = br.readString();
                log.info("User requested talk to npc {}", npc);
                final boolean npcActivated = npc.equals("Leoben") || npc.equals("Apollo");
                if (!npcActivated)
                    return;

                //Log.infoIn("Talk: " + npc);
                user.send(writeTalk(npc));
                Remark remark = new Remark((byte) 1, "%$bgo.npc_no8.Phrase__2382939f-71b5-4822-8549-a64bd9f47a6d__0%", "");
                final DialogProtocol dialogProtocol = user.getProtocol(ProtocolID.Dialog);
                user.send(dialogProtocol.writeNpcRemark(remark));
            }
            case Quit ->
            {
                if (user.getPlayer().getLocation().getGameLocation() != GameLocation.Room)
                {
                    log.error("RoomProtocol, quit while not in room! " + user.getPlayer().getLocation().getGameLocation() + " " +
                            user.getPlayer().getPlayerLog());
                    return;
                }

                log.info("User requested RoomProtocol quit");
                try
                {
                    var ship = user.getPlayer().getHangar().getActiveShip();
                    if (ship.getShipStats().getHp() == 0)
                        ship.getShipStats().setHp(1);
                }
                catch (Exception exception)
                {
                    log.error("Uncaught exception", exception);
                }

                final Location location = user.getPlayer().getLocation();
                location.changeState(new SpaceLocation(location));
                sceneProtocol.sendLoadNextScene();
            }
            case Enter ->
            {
                //Log.infoIn("ENTERING ROOM");
            }
            default -> log.warn("RoomProtocol not implemented: " + msgType);
        }
    }

    public BgoProtocolWriter writeTalk(final String npc)
    {
        final BgoProtocolWriter bw = newMessage();
        bw.writeMsgType(ServerMessage.Talk.value);

        bw.writeString(npc);

        return bw;
    }

}
