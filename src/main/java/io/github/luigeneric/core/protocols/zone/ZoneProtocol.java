package io.github.luigeneric.core.protocols.zone;

import io.github.luigeneric.binaryreaderwriter.BgoProtocolReader;
import io.github.luigeneric.core.player.location.Location;
import io.github.luigeneric.core.player.location.ZoneLocation;
import io.github.luigeneric.core.protocols.BgoProtocol;
import io.github.luigeneric.core.protocols.ProtocolID;
import io.github.luigeneric.core.protocols.scene.SceneProtocol;
import io.github.luigeneric.enums.GameLocation;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
public class ZoneProtocol extends BgoProtocol
{
    private final ZoneProtocolWriteOnly writer;
    public ZoneProtocol()
    {
        super(ProtocolID.Zone);
        this.writer = new ZoneProtocolWriteOnly();
    }

    public ZoneProtocolWriteOnly getWriter()
    {
        return writer;
    }

    @Override
    public void parseMessage(final int msgType, final BgoProtocolReader br) throws IOException
    {
        final ClientMessage clientMessage = ClientMessage.valueOf(msgType);
        if (clientMessage == null)
            return;

        log.info("ZoneProtocol: {}", clientMessage);

        switch (clientMessage)
        {
            case AdmissionStatus ->
            {
                final long zoneGuid = br.readGUID();
                log.info("RequestAdmissionStatus {} {}", user.getUserLogSimple(), zoneGuid);
            }
            case Join ->
            {
                final long zoneGuid = br.readGUID();
                log.info("Join zone {} {}", user.getUserLogSimple(), zoneGuid);


                if (user.getPlayer().getLocation().getGameLocation() != GameLocation.Room)
                {
                    log.warn("RoomProtocol, quit while not in room! " + user.getPlayer().getLocation().getGameLocation() + " " +
                            user.getPlayer().getPlayerLog());
                    return;
                }

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
                ///TODO add hasPaidAdmission check

                final Location location = user.getPlayer().getLocation();
                //location.setLocation(GameLocation.Zone, sectorId, sectorGuid, zoneGuid);
                location.changeState(new ZoneLocation(location));
                final SceneProtocol sceneProtocol = user.getProtocol(ProtocolID.Scene);
                sceneProtocol.sendLoadNextScene();
            }
            case Leave ->
            {
                log.info("Leave zone {}", user.getUserLogSimple());
            }
            case ScoreboardSubscribe, ScoreboardUnsubscribe -> scoreboardHandling(clientMessage == ClientMessage.ScoreboardSubscribe);
            default ->
            {
                log.warn("ZoneProtocol: could not find msgType: {}", msgType);
            }
        }
    }

    private void scoreboardHandling(final boolean isSubscribe)
    {
        if (isSubscribe)
        {
            //sub logic
        }
        else
        {
            //unsub logic
        }
    }

}
