package io.github.luigeneric.core.spaceentities;


import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.core.movement.DynamicMovementController;
import io.github.luigeneric.core.spaceentities.statsinfo.stats.SpaceSubscribeInfo;
import io.github.luigeneric.enums.Faction;
import io.github.luigeneric.enums.FactionGroup;
import io.github.luigeneric.enums.SpaceEntityType;
import io.github.luigeneric.linearalgebra.base.Transform;
import io.github.luigeneric.templates.cards.MovementCard;
import io.github.luigeneric.templates.cards.OwnerCard;
import io.github.luigeneric.templates.cards.ShipAbilityCard;
import io.github.luigeneric.templates.cards.WorldCard;
import io.github.luigeneric.templates.utils.AbilityActionType;
import io.github.luigeneric.templates.utils.ObjectStat;

public class Missile extends SpaceObject
{
    private final SpaceObject ownerObject;
    private SpaceObject missileLaunchedOnObject;
    private final byte missileTier;
    private final int objectPointHash;
    private final float effectRadius;
    //tick in double
    private final long tickSpawnTime;
    private final MovementCard movementCard;

    public Missile(long objectID, final OwnerCard ownerCard, final WorldCard worldCard, final MovementCard movementCard,
                   Faction faction, FactionGroup factionGroup, final SpaceSubscribeInfo spaceSubscribeInfo,
                   final SpaceObject ownerObject, final SpaceObject missileLaunchedOnObject, final byte missileTier,
                   final int objectPointHash, final float effectRadius, final long tickSpawnTime)
    {
        super(objectID, ownerCard, worldCard, SpaceEntityType.Missile, faction, factionGroup, spaceSubscribeInfo);
        this.ownerObject = ownerObject;
        this.missileLaunchedOnObject = missileLaunchedOnObject;
        this.missileTier = missileTier;
        this.objectPointHash = objectPointHash;
        this.effectRadius = effectRadius;
        this.tickSpawnTime = tickSpawnTime;
        this.movementCard = movementCard;
    }

    public void setFromAbilityCard(final ShipAbilityCard shipAbilityCard)
    {
        if (shipAbilityCard.getAbilityActionType() != AbilityActionType.FireMissle) return;
    }

    @Override
    public void write(final BgoProtocolWriter bw)
    {
        super.write(bw);
        bw.ensureDeltaCapacity(15);
        bw.writeUInt32(this.ownerObject.getObjectID()); //4
        if (missileLaunchedOnObject == null)
        {
            bw.writeUInt32(0);
        }
        else
        {
            bw.writeUInt32(this.missileLaunchedOnObject.getObjectID()); //4
        }

        bw.writeByte(this.missileTier); // 1
        bw.writeUInt16(this.objectPointHash); //2
        bw.writeSingle(this.effectRadius); // 4
    }

    @Override
    public boolean spawnedBy(final SpaceObject spawner)
    {
        return this.ownerObject.equals(spawner);
    }

    @Override
    public void createMovementController(final Transform transform)
    {
        this.movementController = new DynamicMovementController(transform, this.movementCard);
    }

    public SpaceObject getOwnerObject()
    {
        return ownerObject;
    }

    public SpaceObject getMissileLaunchedOnObject()
    {
        return missileLaunchedOnObject;
    }
    public void invalidateLaunchOnObject()
    {
        this.missileLaunchedOnObject = null;
    }

    public byte getMissileTier()
    {
        return missileTier;
    }

    public int getObjectPointHash()
    {
        return objectPointHash;
    }

    public float getEffectRadius()
    {
        return effectRadius;
    }

    public double getTickSpawnTime()
    {
        return tickSpawnTime;
    }
    public boolean getTickSpawnIsAfter(final long timeStamp)
    {
        long diff = timeStamp - this.tickSpawnTime;
        //lifetime is in seconds
        final float lifeTime = this.spaceSubscribeInfo.getStat(ObjectStat.LifeTime);
        final long lifeTimeLong = (long)lifeTime * 1000;
        return diff >= lifeTimeLong;
    }

    public MovementCard getMovementCard()
    {
        return movementCard;
    }
}
