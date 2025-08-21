package io.github.luigeneric.core.sector.timers;


import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.core.protocols.ProtocolID;
import io.github.luigeneric.core.protocols.ProtocolRegistryWriteOnly;
import io.github.luigeneric.core.protocols.game.GameProtocolWriteOnly;
import io.github.luigeneric.core.sector.Tick;
import io.github.luigeneric.core.sector.management.SectorSender;
import io.github.luigeneric.core.sector.management.SectorSpaceObjects;
import io.github.luigeneric.core.spaceentities.SpaceObject;
import io.github.luigeneric.core.spaceentities.bindings.SpaceObjectState;
import io.github.luigeneric.templates.utils.ObjectStat;

import java.util.List;

public class SpaceObjectStateTimer extends DelayedTimer
{
    private final SectorSender sender;
    public SpaceObjectStateTimer(Tick tick, SectorSpaceObjects sectorSpaceObjects, long delayedTicks,
                                 final SectorSender sender)
    {
        super(tick, sectorSpaceObjects, delayedTicks);
        this.sender = sender;
    }

    @Override
    protected void delayedUpdate()
    {
        final List<SpaceObject> ships = this.sectorSpaceObjects.getSpaceObjectsOfTypeShip();

        final GameProtocolWriteOnly gameProtocolWriteOnly = ProtocolRegistryWriteOnly.getProtocol(ProtocolID.Game);

        for (final SpaceObject ship : ships)
        {
            if (ship.isRemoved() || !ship.getSpaceObjectState().isChanged())
                continue;

            final BgoProtocolWriter bw = gameProtocolWriteOnly.writeSpaceObjectState(ship.getSpaceObjectState());
            sender.sendToAllClients(bw);
        }
    }

    private void empStuff(final SpaceObject ship)
    {
        final float maxHP = ship.getSpaceSubscribeInfo().getStat(ObjectStat.MaxHullPoints);
        final float currentHP = ship.getSpaceSubscribeInfo().getHp();

        final SpaceObjectState state = ship.getSpaceObjectState();

        if (maxHP > 1 && (maxHP * 0.2f) > currentHP)
        {
            if (state.getIsEmpOn())
                return;
            state.setEmpOn(true);
        }
        else
        {
            if (state.getIsEmpOn())
                return;
            state.setEmpOn(false);
        }
    }
}
