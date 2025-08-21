package io.github.luigeneric.core.sector.timers;

import io.github.luigeneric.core.User;
import io.github.luigeneric.core.protocols.ProtocolRegistryWriteOnly;
import io.github.luigeneric.core.protocols.game.GameProtocolWriteOnly;
import io.github.luigeneric.core.sector.Tick;
import io.github.luigeneric.core.sector.management.*;
import io.github.luigeneric.enums.Faction;
import io.github.luigeneric.templates.sectortemplates.OutpostProgressTemplate;

public class OutpostDecreaseTimer extends DelayedTimer
{
    private final OutPostStates outPostStates;
    private final SectorUsers users;
    private final SectorSender sender;
    private final GameProtocolWriteOnly gameProtocolWriteOnly;

    public OutpostDecreaseTimer(Tick tick, SectorSpaceObjects sectorSpaceObjects, final long delayTicks,
                                final OutPostStates outPostStates, final SectorUsers users,
                                final SectorSender sender)
    {
        super(tick, sectorSpaceObjects, delayTicks);
        this.outPostStates = outPostStates;
        this.users = users;
        this.sender = sender;
        this.gameProtocolWriteOnly = ProtocolRegistryWriteOnly.game();
    }

    @Override
    protected void delayedUpdate()
    {
        autoDecrease(this.outPostStates.colonialOutpostState(), Faction.Colonial);
        autoDecrease(this.outPostStates.cylonOutpostState(), Faction.Cylon);
        for (final User usr : users.getUsers())
        {
            final float colonialDelta = outPostStates.colonialOutpostState().getDelta();
            final float cylonDelta = outPostStates.cylonOutpostState().getDelta();
            sender.sendToClient(gameProtocolWriteOnly.writeOutpostStateBroadcast(outPostStates.colonialOutpostState().getOpPoints(),
                    colonialDelta, outPostStates.cylonOutpostState().getOpPoints(), cylonDelta), usr);
        }

        //now check if there is first new outpost!
        final int currentColoPts = outPostStates.colonialOutpostState().getOpPoints();
        final int currentCyloPts = outPostStates.cylonOutpostState().getOpPoints();
    }

    private void autoDecrease(final OutpostState op, final Faction faction)
    {
        if (outPostStates == null)
        {
            return;
        }
        final OutpostProgressTemplate opProgressTemplate = outPostStates.getTemplateFromFaction(faction);
        if (opProgressTemplate == null)
            return;
        //Log.info("Decrease OP " + opProgressTemplate.ptsDrainPerSecond());
        final boolean isBlocked = op.decreasePoints(opProgressTemplate.ptsDrainPerSecond());
    }
}
