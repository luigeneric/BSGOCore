package io.github.luigeneric.core.sector.management.notifications;


import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.core.User;
import io.github.luigeneric.core.UsersContainer;
import io.github.luigeneric.core.community.party.IParty;
import io.github.luigeneric.core.protocols.ProtocolID;
import io.github.luigeneric.core.protocols.ProtocolRegistryWriteOnly;
import io.github.luigeneric.core.protocols.notification.MiningShipAttackedType;
import io.github.luigeneric.core.protocols.notification.NotificationProtocolWriteOnly;
import io.github.luigeneric.core.protocols.notification.OutpostAttackedType;
import io.github.luigeneric.core.spaceentities.MiningShip;
import io.github.luigeneric.core.spaceentities.Outpost;
import io.github.luigeneric.core.spaceentities.SpaceObject;
import io.github.luigeneric.enums.Faction;
import io.github.luigeneric.templates.utils.ObjectStat;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class NotificationMediator
{
    private final UsersContainer usersContainer;
    private final NotificationProtocolWriteOnly notificationProtocolWriteOnly;

    public NotificationMediator(final UsersContainer usersContainer)
    {
        this.usersContainer = usersContainer;
        this.notificationProtocolWriteOnly = ProtocolRegistryWriteOnly.getProtocol(ProtocolID.Notification);
    }

    public void notifySpaceObjectUnderAttack(final NotificationKey notificationKey, final long sectorGUID)
    {
        final SpaceObject spaceObject = notificationKey.spaceObject();
        switch (spaceObject.getSpaceEntityType())
        {
            case Outpost ->
            {
                notifyOutpostAttackType((Outpost) spaceObject, sectorGUID, notificationKey.outpostAttackedType());
            }
            case MiningShip ->
            {
                notifyMiningShipUnderAttack((MiningShip) spaceObject, sectorGUID, notificationKey.miningShipAttackedType());
            }
        }
    }
    public NotificationKey getNotificationKey(final SpaceObject spaceObject)
    {
        switch (spaceObject.getSpaceEntityType())
        {
            case Outpost ->
            {
                final OutpostAttackedType outpostAttackedType = outPostAttacKType((Outpost) spaceObject);
                return new NotificationKey(spaceObject, outpostAttackedType);
            }
            case MiningShip ->
            {
                final MiningShipAttackedType miningShipAttackedType = miningShipAttackedType((MiningShip) spaceObject);
                return new NotificationKey(spaceObject, miningShipAttackedType);
            }
            default -> throw new IllegalArgumentException("Not implemented spaceObject! " + spaceObject.getSpaceEntityType());
        }
    }

    private OutpostAttackedType outPostAttacKType(final Outpost outpost)
    {
        final float currentHp = outpost.getSpaceSubscribeInfo().getHp();
        final float maxHp = outpost.getSpaceSubscribeInfo().getStat(ObjectStat.MaxHullPoints);

        final boolean isAbove50Percent = (currentHp / maxHp) > 0.5f;
        final boolean isDead = outpost.isRemoved();

        OutpostAttackedType outpostAttackedType;

        if (!isAbove50Percent && !isDead)
        {
            outpostAttackedType = OutpostAttackedType.OutpostHeavyDamage;
        }
        else if (isDead)
        {
            outpostAttackedType = OutpostAttackedType.OutpostDied;
        }
        else
        {
            outpostAttackedType = OutpostAttackedType.OutpostUnderAttack;
        }

        return outpostAttackedType;
    }


    private void notifyOutpostAttackType(final Outpost outpost, final long sectorGUID, final OutpostAttackedType outpostAttackedType)
    {
        final Faction outpostFaction = outpost.getFaction();
        final BgoProtocolWriter bwOutpostAttacked = notificationProtocolWriteOnly
                .writeOutpostAttacked(outpostFaction, sectorGUID, outpostAttackedType);
        sendToAllUsersOfFaction(outpostFaction, bwOutpostAttacked);
    }

    public MiningShipAttackedType miningShipAttackedType(final MiningShip miningShip)
    {
        final float currentHp = miningShip.getSpaceSubscribeInfo().getHp();
        final float maxHp = miningShip.getSpaceSubscribeInfo().getStat(ObjectStat.MaxHullPoints);

        final boolean isAbove50Percent = (currentHp / maxHp) > 0.5f;
        final boolean isDead = miningShip.isRemoved();

        MiningShipAttackedType miningShipAttackedType;

        if (!isAbove50Percent && !isDead)
        {
            miningShipAttackedType = MiningShipAttackedType.ShipDamaged;
        }
        else if (isDead)
        {
            miningShipAttackedType = MiningShipAttackedType.ShipDrivenOff;
        }
        else
        {
            miningShipAttackedType = MiningShipAttackedType.ShipUnderAttack;
        }
        return miningShipAttackedType;
    }

    public void notifyMiningShipUnderAttack(final MiningShip miningShip, final long sectorGUID,
                                            final MiningShipAttackedType miningShipAttackedType
    )
    {
        final User owner = miningShip.getOwner();
        final Optional<IParty> optParty = owner.getPlayer().getParty();
        final List<User> usersOfInterest = new ArrayList<>();
        if (optParty.isPresent())
        {
            final IParty party = optParty.get();
            usersOfInterest.addAll(party.getMembers());
        }
        else
        {
            usersOfInterest.add(owner);
        }


        for (User user : usersOfInterest)
        {
            final BgoProtocolWriter bw = notificationProtocolWriteOnly
                    .writeMiningShipUnderAttack(sectorGUID, miningShipAttackedType, owner.equals(user));
            user.send(bw);
        }

    }


    public void sendToAllUsersOfFaction(final Faction faction, final BgoProtocolWriter bw)
    {
        final List<User> usersToSendTo = this.usersContainer.userList(user -> user.isConnected() &&
                user.getPlayer().getFaction() == faction);

        sendToAllUsers(usersToSendTo, bw);
    }
    public void sendToAllUsers(final Collection<User> users, final BgoProtocolWriter bw)
    {
        for (final User user : users)
        {
            user.send(bw);
        }
    }
}
