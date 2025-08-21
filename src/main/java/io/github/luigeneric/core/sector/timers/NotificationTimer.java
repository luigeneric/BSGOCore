package io.github.luigeneric.core.sector.timers;

import io.github.luigeneric.core.protocols.notification.MiningShipAttackedType;
import io.github.luigeneric.core.sector.SectorCards;
import io.github.luigeneric.core.sector.Tick;
import io.github.luigeneric.core.sector.management.ObjectLeftSubscriber;
import io.github.luigeneric.core.sector.management.SectorSpaceObjects;
import io.github.luigeneric.core.sector.management.damage.DamageRecord;
import io.github.luigeneric.core.sector.management.damage.ObjectDamageHistory;
import io.github.luigeneric.core.sector.management.damage.SectorDamageHistory;
import io.github.luigeneric.core.sector.management.notifications.NotificationKey;
import io.github.luigeneric.core.sector.management.notifications.NotificationMediator;
import io.github.luigeneric.core.sector.objleft.ObjectLeftDescription;
import io.github.luigeneric.core.spaceentities.MiningShip;
import io.github.luigeneric.core.spaceentities.Outpost;
import io.github.luigeneric.core.spaceentities.SpaceObject;
import io.github.luigeneric.enums.SpaceEntityType;
import io.github.luigeneric.templates.utils.ObjectStat;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NotificationTimer extends DelayedTimer implements ObjectLeftSubscriber
{
    private final NotificationMediator notificationMediator;
    private final SectorDamageHistory sectorDamageHistory;
    private final SectorCards sectorCards;
    //objectID, DamageRecord
    private final Map<Long, DamageRecord> lastDamageRecordForNotification;
    //objectID, Key
    private final Map<Long, NotificationKey> lastNotificationUpdate;
    private final static long TIME_DELAY = 45_000; //1 minute * 0.75
    public NotificationTimer(final Tick tick, final SectorSpaceObjects sectorSpaceObjects, final long delayedTicks,
                             NotificationMediator notificationMediator, SectorDamageHistory sectorDamageHistory,
                             SectorCards sectorCards)
    {
        super(tick, sectorSpaceObjects, delayedTicks);
        this.notificationMediator = notificationMediator;
        this.sectorDamageHistory = sectorDamageHistory;
        this.sectorCards = sectorCards;
        this.lastDamageRecordForNotification = new HashMap<>();
        this.lastNotificationUpdate = new HashMap<>();
    }

    @Override
    protected void delayedUpdate()
    {
        final List<Outpost> outposts = sectorSpaceObjects.getSpaceObjectsOfEntityType(SpaceEntityType.Outpost);
        final List<MiningShip> miningShips = sectorSpaceObjects.getSpaceObjectsOfEntityType(SpaceEntityType.MiningShip);

        outposts.forEach(this::notify);
        miningShips.forEach(this::notify);
    }

    private void notify(final SpaceObject spaceObject)
    {
        switch (spaceObject.getSpaceEntityType())
        {
            case Outpost -> notifyOutpost(spaceObject);
            case MiningShip -> notifyMiningShip(spaceObject);
        }
    }
    private void notifyMiningShip(final SpaceObject miningShip)
    {
        if (!miningShip.getSpaceSubscribeInfo().isInCombat())
            return;

        final ObjectDamageHistory dmgHistory = sectorDamageHistory.getDamageHistory(miningShip);
        if (dmgHistory == null)
            return;
        final long outpostObjectID = miningShip.getObjectID();
        final DamageRecord lastTime = lastDamageRecordForNotification.get(outpostObjectID);
        final DamageRecord lastDamage = dmgHistory.getLastDamage();
        final boolean isKill = dmgHistory.getKillShotDealer().isPresent();
        if (lastDamage == null)
            return;

        final NotificationKey lastNotificationKey = lastNotificationUpdate.get(outpostObjectID);
        final NotificationKey notificationKey = notificationMediator.getNotificationKey(miningShip);
        final boolean isNotificalEqual = notificationKey.equals(lastNotificationKey);
        final boolean flagLastTimeIsNull = lastTime == null;

        if (flagLastTimeIsNull || !isNotificalEqual || (lastTime.timeStamp() + TIME_DELAY) < this.tick.getTimeStamp())
        {
            if (!isKill && notificationKey.miningShipAttackedType() == MiningShipAttackedType.ShipDrivenOff)
            {
                return;
            }
            notificationMediator.notifySpaceObjectUnderAttack(notificationKey, sectorCards.sectorCard().getCardGuid());
            lastDamageRecordForNotification.put(miningShip.getObjectID(), lastDamage);
            lastNotificationUpdate.put(outpostObjectID, notificationKey);
        }
    }
    private void notifyOutpost(final SpaceObject outpost)
    {
        if (!outpost.getSpaceSubscribeInfo().isInCombat())
        {
            return;
        }

        if (outpost.getSpaceSubscribeInfo().getHp() == outpost.getSpaceSubscribeInfo().getStat(ObjectStat.MaxHullPoints))
        {
            return;
        }

        //if it has more hp than max - n, ignore notifications!
        if (outpost.getSpaceSubscribeInfo().getHp() > (outpost.getSpaceSubscribeInfo().getStat(ObjectStat.MaxHullPoints) - 200))
        {
            return;
        }

        final ObjectDamageHistory dmgHistory = sectorDamageHistory.getDamageHistory(outpost);
        if (dmgHistory == null)
            return;
        final long outpostObjectID = outpost.getObjectID();
        final DamageRecord lastTime = lastDamageRecordForNotification.get(outpostObjectID);
        final DamageRecord lastDamageByPlayer = dmgHistory.getLastDamageByPlayer();
        if (lastTime == null && lastDamageByPlayer == null)
            return;

        final NotificationKey lastNotificationKey = lastNotificationUpdate.get(outpostObjectID);
        final NotificationKey notificationKey = notificationMediator.getNotificationKey(outpost);
        final boolean isNotificalEqual = notificationKey.equals(lastNotificationKey);
        final boolean flagLastTimeIsNull = lastTime == null;

        if (flagLastTimeIsNull || !isNotificalEqual || (lastTime.timeStamp() + TIME_DELAY) < this.tick.getTimeStamp())
        {
            notificationMediator.notifySpaceObjectUnderAttack(notificationKey, sectorCards.sectorCard().getCardGuid());
            lastDamageRecordForNotification.put(outpost.getObjectID(), lastDamageByPlayer);
            lastNotificationUpdate.put(outpostObjectID, notificationKey);
        }
    }

    @Override
    public void onUpdate(final ObjectLeftDescription arg)
    {
        notify(arg.getRemovedSpaceObject());

        //remove from map
        this.lastNotificationUpdate.remove(arg.getRemovedSpaceObject().getObjectID());
        this.lastDamageRecordForNotification.remove(arg.getRemovedSpaceObject().getObjectID());
    }
}
