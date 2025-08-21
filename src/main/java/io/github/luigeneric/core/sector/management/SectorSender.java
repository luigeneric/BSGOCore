package io.github.luigeneric.core.sector.management;



import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.core.User;
import io.github.luigeneric.core.movement.MovementController;
import io.github.luigeneric.core.protocols.ProtocolRegistryWriteOnly;
import io.github.luigeneric.core.protocols.game.GameProtocolWriteOnly;
import io.github.luigeneric.core.spaceentities.SpaceObject;
import io.github.luigeneric.enums.SpaceEntityType;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Slf4j
public class SectorSender
{
    private final SectorUsers users;
    private final GameProtocolWriteOnly gameProtocolWriteOnly;

    public SectorSender(final SectorUsers users)
    {
        this.users = users;
        this.gameProtocolWriteOnly = ProtocolRegistryWriteOnly.game();
    }


    public void sendToAllClients(final List<BgoProtocolWriter> bws)
    {
        for (final User userClient : users.getUsersCollection())
        {
            sendBwsToUser(bws, userClient);
        }
    }

    public void sendToAllClients(final BgoProtocolWriter bw)
    {
        this.sendToClients(bw, users.getUsers());
    }

    public void sendToClients(final BgoProtocolWriter bw, final List<User> userClients)
    {
        for (final User user : userClients)
        {
            sendToClient(bw, user);
        }
    }

    /**
     * Sends all the given bws to the user AND flushes outputbuffer at the last entry
     * @param bws all ProtocolBuffers
     * @param user the user to send to
     */
    public void sendBwsToUser(final List<BgoProtocolWriter> bws, final User user)
    {
        user.send(bws);
    }

    public void sendToClient(final BgoProtocolWriter bw, final User usr)
    {
        usr.send(bw);
    }

    public void sendSpaceObjectsToUser(final Collection<SpaceObject> spaceObjects, final User user)
    {
        final List<BgoProtocolWriter> bws = new ArrayList<>(spaceObjects.size());

        //first add all spaceobjects to who is and send
        for (SpaceObject spaceObject : spaceObjects)
        {
            bws.add(gameProtocolWriteOnly.writeWhoIs(spaceObject));
        }
        sendBwsToUser(bws, user);
        bws.clear();

        //now send all states
        for (SpaceObject spaceObject : spaceObjects)
        {
            bws.add(gameProtocolWriteOnly.writeSpaceObjectState(spaceObject.getSpaceObjectState()));
        }
        sendBwsToUser(bws, user);
        bws.clear();

        //now send all movement information
        for (final SpaceObject spaceObject : spaceObjects)
        {
            final MovementController movementController = spaceObject.getMovementController();
            if (movementController.isMovingObject() && movementController.getFrameTick() != null)
            {
                try
                {
                    var bw = gameProtocolWriteOnly.writeSyncMove(spaceObject);
                    bws.add(bw);
                }
                catch (IllegalArgumentException illegalArgumentException)
                {
                    log.error("in sendAllMovement information", illegalArgumentException);
                }
            }
            else
            {
                //asteroids planetoids, planets dont need to be send since the position is already inside the whois
                if (!spaceObject.getSpaceEntityType().isOfType(
                        SpaceEntityType.Asteroid,
                        SpaceEntityType.Planetoid,
                        SpaceEntityType.Planet))
                {
                    if (spaceObject.getMovementController().getFrameTick() != null)
                    {
                        bws.add(gameProtocolWriteOnly.writeMove(spaceObject));
                    }
                }
            }

        }
        sendBwsToUser(bws, user);
    }

    public SectorUsers getUsers()
    {
        return users;
    }

    public void shutdown()
    {
        //empty for now
    }
}
