package io.github.luigeneric.core.sector.management;


import io.github.luigeneric.core.sector.Tick;
import io.github.luigeneric.core.spaceentities.PlayerShip;
import io.github.luigeneric.core.spaceentities.SpaceObject;

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

public class JumpRegistry
{
    private final Map<Long, SpaceObject> objects;
    private final PriorityQueue<JumpScheduleItem> jumpingObjects;
    private final SectorUsers users;
    private final Tick tick;
    private final Lock lock;

    public JumpRegistry(final Map<Long, SpaceObject> objects,
                        final PriorityQueue<JumpScheduleItem> jumpingObjects,
                        final SectorUsers users,
                        final Tick tick)
    {
        this.objects = objects;
        this.jumpingObjects = jumpingObjects;
        this.users = users;
        this.tick = tick;
        this.lock = new ReentrantLock();
    }
    public JumpRegistry(final SectorUsers users, final Tick tick)
    {
        this(new HashMap<>(), new PriorityQueue<>(), users, tick);
    }

    private void enqueueJump(final SpaceObject spaceObject, final long targetSector, final float chargeTime, final long[] playerIds)
    {
        Objects.requireNonNull(spaceObject);
        if (spaceObject instanceof PlayerShip playerShip)
        {
            playerShip.getPlayerVisibility().finishGhostJumpInIfNotFinished();
        }
        lock.lock();
        try
        {
            //remove jumping object if present AND already jumping
            if (this.objects.remove(spaceObject.getObjectID()) != null)
            {
                jumpingObjects.removeIf(spaceObjectJumpScheduleItem ->
                {
                    return spaceObjectJumpScheduleItem.getEntry().getPlayerId() == spaceObject.getPlayerId();
                });
            }

            this.jumpingObjects.offer(new JumpScheduleItem(tick, chargeTime, spaceObject, targetSector, playerIds));
            this.objects.put(spaceObject.getObjectID(), spaceObject);
        }
        finally
        {
            lock.unlock();
        }
    }

    public void addJumpOutRequest(final long userID, final long targetSectorID, final float chargeTime, final long[] playerIds)
    {
        final Optional<PlayerShip> optPlayerShip = this.users.getPlayerShipByUserID(userID);
        if (optPlayerShip.isEmpty())
            return;

        final PlayerShip playerShip = optPlayerShip.get();
        //playerShip.getSpaceSubscribeInfo().setPp(0);
        this.enqueueJump(playerShip, targetSectorID, chargeTime, playerIds);
    }

    public void removeJump(final long userID)
    {
        lock.lock();
        try
        {
            final Set<Long> usersMatching = this.objects.values().stream()
                    .filter(obj -> obj.getPlayerId() == userID)
                    .map(SpaceObject::getObjectID)
                    .collect(Collectors.toSet());

            this.jumpingObjects.removeIf(jumpingObject -> usersMatching.contains(jumpingObject.getEntry().getObjectID()));
            for (long objID : usersMatching)
            {
                this.objects.remove(objID);
            }
        }
        finally
        {
            lock.unlock();
        }
    }

    public boolean containsSpaceObject(final SpaceObject spaceObject)
    {
        return this.containsSpaceObject(spaceObject.getObjectID());
    }
    public boolean containsSpaceObject(final long objectID)
    {
        lock.lock();
        try
        {
            return this.objects.containsKey(objectID);
        }
        finally
        {
            lock.unlock();
        }
    }

    public boolean hasItemsWithTimeout(final Tick current)
    {
        lock.lock();
        try
        {
            if (this.jumpingObjects.isEmpty()) return false;

            final JumpScheduleItem jumpScheduleItem = this.jumpingObjects.peek();
            final long delta = jumpScheduleItem.getTimeStamp() - current.getTimeStamp();
            return delta <= 0;
        }
        finally
        {
            lock.unlock();
        }
    }

    public JumpScheduleItem getItem()
    {
        lock.lock();
        try
        {
            if (this.jumpingObjects.isEmpty()) throw new IllegalStateException("Could not call getItem because queue is empty!");
            final JumpScheduleItem val = this.jumpingObjects.poll();
            final SpaceObject entry = val.getEntry();
            this.objects.remove(entry.getObjectID());
            return val;
        }
        finally
        {
            lock.unlock();
        }
    }
}
