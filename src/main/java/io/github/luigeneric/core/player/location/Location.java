package io.github.luigeneric.core.player.location;

import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.binaryreaderwriter.IProtocolWrite;
import io.github.luigeneric.core.player.PlayerFaction;
import io.github.luigeneric.core.player.subscribesystem.InfoPublisher;
import io.github.luigeneric.core.protocols.subscribe.InfoType;
import io.github.luigeneric.enums.Faction;
import io.github.luigeneric.enums.GameLocation;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Slf4j
public class Location extends InfoPublisher<LocationState> implements IProtocolWrite
{
    /**
     * the current sectorID the user is in
     */
    private long sectorID;
    private final PlayerFaction faction;
    private GameLocation previousLocation;
    private long sectorGUID;
    private long zoneGUID;

    public Location(final long playerID, final PlayerFaction faction)
    {
        super(InfoType.Location, playerID, null);
        this.faction = faction;
        this.changeState(new StarterLocation(this));
    }

    public Faction getFaction()
    {
        return faction.get();
    }

    protected void setSectorIdGuid(final long sectorID, final long sectorGUID, final long zoneGUID)
    {
        this.sectorID = sectorID;
        this.sectorGUID = sectorGUID;
        this.zoneGUID = zoneGUID;
    }

    @Override
    public void write(final BgoProtocolWriter bw)
    {
        this.currentInfo.process(bw);
    }

    public void changeState(final LocationState locationState)
    {
        this.set(locationState);
    }

    @Override
    public void set(final LocationState newInfo)
    {
        if (this.currentInfo != null)
        {
            this.setPreviousLocation(this.currentInfo.getGameLocation());
        }

        super.set(newInfo);
    }

    public GameLocation getGameLocation()
    {
        return this.currentInfo.getGameLocation();
    }
    public GameLocation getNonDisconnectLocation()
    {
        final GameLocation current = this.getGameLocation();
        if (current == GameLocation.Disconnect)
        {
            return this.previousLocation;
        }
        return current;
    }

    private void setPreviousLocation(final GameLocation gameLocation)
    {
        if (gameLocation != GameLocation.Disconnect)
            this.previousLocation = gameLocation;
    }

    public void setLocation(final GameLocation gameLocation, final long sectorID, final long sectorGUID)
    {
        setLocation(gameLocation, sectorID, sectorGUID, 0);
    }
    public void setLocation(final GameLocation gameLocation, final long sectorID, final long sectorGUID, final long zoneGUID)
    {
        this.setSectorIdGuid(sectorID, sectorGUID, zoneGUID);
        switch (gameLocation)
        {
            case Room ->
            {
                //alpha ceti or tartalon
                if (sectorID == 6 || sectorID == 0)
                {
                    this.changeState(new CICLocation(this));
                }
                else
                {
                    this.changeState(new OutpostLocation(this));
                }
            }
            case Space ->
            {
                this.changeState(new SpaceLocation(this));
            }
            case Starter ->
            {
                this.changeState(new StarterLocation(this));
            }
            case Avatar ->
            {
                this.changeState(new AvatarLocation(this));
            }
            case Zone ->
            {
                this.changeState(new ZoneLocation(this));
            }
            default -> throw new IllegalArgumentException("GameLocation " + gameLocation + " not implemented");
        }
    }

    public long getRoomGUID()
    {
        if (this.currentInfo instanceof RoomLocation room)
        {
            return room.getRoomGUID();
        }
        return 0;
    }

    public void processGuildLocation(final BgoProtocolWriter bw)
    {
        this.currentInfo.processLocationCommunitySubscriber(bw);
    }

    public boolean isSpaceAndSameSector(final Location location)
    {
        final var isSpace = location.getGameLocation() == GameLocation.Space;
        if (!isSpace)
            return false;

        return this.sectorID == location.getSectorID();
    }

    public boolean isInRoom()
    {
        return this.getGameLocation() == GameLocation.Room;
    }
}
