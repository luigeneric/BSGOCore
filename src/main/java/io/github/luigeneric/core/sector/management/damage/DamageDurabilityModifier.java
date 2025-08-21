package io.github.luigeneric.core.sector.management.damage;


import io.github.luigeneric.core.User;
import io.github.luigeneric.core.player.HangarShip;
import io.github.luigeneric.core.player.container.ShipSlot;
import io.github.luigeneric.core.player.container.ShipSlots;
import io.github.luigeneric.core.protocols.ProtocolID;
import io.github.luigeneric.core.protocols.ProtocolRegistryWriteOnly;
import io.github.luigeneric.core.protocols.player.PlayerProtocolWriteOnly;
import io.github.luigeneric.core.sector.management.SectorSender;
import io.github.luigeneric.core.sector.management.SectorUsers;
import io.github.luigeneric.core.spaceentities.SpaceObject;
import io.github.luigeneric.templates.shipitems.ShipSystem;

import java.util.Optional;

public class DamageDurabilityModifier
{
    private final SectorUsers users;
    private final SectorSender sender;

    private final PlayerProtocolWriteOnly playerProtocolWriteOnly;

    public DamageDurabilityModifier(final SectorUsers users, final SectorSender sender)
    {
        this.users = users;
        this.sender = sender;
        this.playerProtocolWriteOnly = ProtocolRegistryWriteOnly.getProtocol(ProtocolID.Player);
    }

    public void damageReceived(final DamageRecord damageRecord)
    {
        final SpaceObject targetObject = damageRecord.to();
        if (!targetObject.isPlayer())
            return;

        final Optional<User> optUser = users.getUser(targetObject.getPlayerId());
        if (optUser.isEmpty())
            return;

        final User user = optUser.get();
        final HangarShip activeShip = user.getPlayer().getHangar().getActiveShip();
        final ShipSlots slots = activeShip.getShipSlots();
        final float durabilityToReduceBy = damageRecord.damage();

        long slotsWithSystem = slots.values()
                .stream()
                .filter(slot -> slot.getShipSystem() != null)
                .count();
        slotsWithSystem = slotsWithSystem == 0 ? 1 : slotsWithSystem;
        final float dividedDamage = durabilityToReduceBy / slotsWithSystem;

        activeShip.reduceDurability(dividedDamage);

        for (final ShipSlot slot : slots.values())
        {
            final ShipSystem system = slot.getShipSystem();
            if (system == null)
                continue;
            system.reduceDurability(dividedDamage);
        }

        sender.sendToClient(playerProtocolWriteOnly.writeShipInfoDurability(activeShip), user);
        sender.sendToClient(playerProtocolWriteOnly.writeShipSlots(activeShip), user);
    }
}
