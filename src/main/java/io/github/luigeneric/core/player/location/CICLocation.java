package io.github.luigeneric.core.player.location;


import io.github.luigeneric.enums.Faction;
import io.github.luigeneric.enums.StaticCardGUID;
import io.github.luigeneric.enums.TransSceneType;

public class CICLocation extends RoomLocation
{
    public CICLocation(final Location location, final TransSceneType transSceneType)
    {
        super(location, transSceneType);
    }
    public CICLocation(final Location location)
    {
        this(location, TransSceneType.CIC);
    }


    @Override
    public long getRoomGUID()
    {
        final Faction faction = this.location.getFaction();
        return faction == Faction.Colonial ?
                StaticCardGUID.CiCColonial.getValue() : StaticCardGUID.CiCCylon.getValue();
    }
}
