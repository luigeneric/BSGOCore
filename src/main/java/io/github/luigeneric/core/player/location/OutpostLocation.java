package io.github.luigeneric.core.player.location;


import io.github.luigeneric.enums.Faction;
import io.github.luigeneric.enums.StaticCardGUID;
import io.github.luigeneric.enums.TransSceneType;

public class OutpostLocation extends RoomLocation
{
    public OutpostLocation(final Location location, final TransSceneType transSceneType)
    {
        super(location, transSceneType);
    }
    public OutpostLocation(final Location location)
    {
        this(location, TransSceneType.Outpost);
    }

    @Override
    public long getRoomGUID()
    {
        final Faction faction = this.location.getFaction();
        return faction == Faction.Colonial ?
                StaticCardGUID.RoomOutpostColonial.getValue() : StaticCardGUID.RoomOutpostCylon.getValue();
    }
}
