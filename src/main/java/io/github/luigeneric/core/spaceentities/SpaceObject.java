package io.github.luigeneric.core.spaceentities;

import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.binaryreaderwriter.IProtocolWrite;
import io.github.luigeneric.core.movement.MovementController;
import io.github.luigeneric.core.movement.StaticMovementController;
import io.github.luigeneric.core.spaceentities.bindings.SpaceObjectState;
import io.github.luigeneric.core.spaceentities.statsinfo.stats.SpaceSubscribeInfo;
import io.github.luigeneric.enums.*;
import io.github.luigeneric.linearalgebra.base.Transform;
import io.github.luigeneric.linearalgebra.collidershapes.Collider;
import io.github.luigeneric.templates.cards.OwnerCard;
import io.github.luigeneric.templates.cards.WorldCard;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

@Slf4j
public abstract class SpaceObject implements IProtocolWrite
{
    protected final long objectID;
    protected CreatingCause creatingCause;
    protected RemovingCause removingCause;
    protected final WorldCard worldCard;
    protected final OwnerCard ownerCard;

    protected final SpaceEntityType spaceEntityType;
    protected final Faction faction;
    protected final FactionGroup factionGroup;

    protected MovementController movementController;

    protected Collider collider;

    protected final SpaceSubscribeInfo spaceSubscribeInfo;
    protected final SpaceObjectState spaceObjectState;

    public SpaceObject(long objectID, final OwnerCard ownerCard, final WorldCard worldCard,
                       final SpaceEntityType spaceEntityType, final Faction faction, final FactionGroup factionGroup,
                       SpaceSubscribeInfo spaceSubscribeInfo)
    {
        this.objectID = objectID;
        this.spaceEntityType = spaceEntityType;
        this.faction = faction;
        this.factionGroup = factionGroup;

        this.worldCard = worldCard;
        this.ownerCard = ownerCard;
        this.spaceSubscribeInfo = spaceSubscribeInfo;

        this.spaceObjectState = SpaceObjectState.createForObjectID(objectID);

        this.creatingCause = CreatingCause.JumpIn;
    }

    public void setRemovingCause(final RemovingCause removingCause)
    {
        if (removingCause == null)
        {
            log.error("Removing cause was null!");
            return;
        }
        if (this.removingCause != null)
        {
            log.error("SpaceObject: Setting removing cause but cause is already set! previous: " + this.removingCause + " now " + removingCause);
            return;
        }
        this.removingCause = removingCause;
    }

    public long getPlayerId()
    {
        return -1;
    }

    public Optional<RemovingCause> getRemovingCause()
    {
        return Optional.ofNullable(this.removingCause);
    }
    public RemovingCause getRemovingCauseDirect()
    {
        return this.removingCause;
    }

    public boolean isRemoved()
    {
        return this.removingCause != null;
    }

    public void createMovementController(final Transform transform)
    {
        this.movementController = new StaticMovementController(transform);
    }

    public MovementController getMovementController()
    {
        return movementController;
    }

    static SpaceEntityType getObjectType(final long objectId)
    {
        return SpaceEntityType.fromValue((objectId & 520093696L));
    }

    public boolean isPlayer()
    {
        return false;
    }
    public boolean isShip()
    {
        return false;
    }

    @Override
    public void write(final BgoProtocolWriter bw)
    {
        bw.writeByte(creatingCause.getValue());
        bw.writeUInt32(ownerCard.getCardGuid());
        bw.writeUInt32(worldCard.getCardGuid());
    }

    public boolean spawnedBy(final SpaceObject spawner)
    {
        return false;
    }

    public long getObjectID()
    {
        return objectID;
    }

    public CreatingCause getCreatingCause()
    {
        return creatingCause;
    }

    public void setCreatingCause(CreatingCause creatingCause)
    {
        this.creatingCause = creatingCause;
    }

    public WorldCard getWorldCard()
    {
        return worldCard;
    }

    public OwnerCard getOwnerCard()
    {
        return ownerCard;
    }

    public SpaceEntityType getSpaceEntityType()
    {
        return spaceEntityType;
    }

    public Faction getFaction()
    {
        return faction;
    }

    public FactionGroup getFactionGroup()
    {
        return factionGroup;
    }

    public boolean isDead()
    {
        return RemovingCause.Death == this.removingCause;
    }

    public Collider getCollider()
    {
        return collider;
    }
    public boolean hasCollider()
    {
        return collider != null;
    }

    public void setCollider(final Collider collider)
    {
        this.collider = collider;
    }

    public SpaceSubscribeInfo getSpaceSubscribeInfo()
    {
        return spaceSubscribeInfo;
    }

    public SpaceObjectState getSpaceObjectState()
    {
        return spaceObjectState;
    }

    public String getPrefabName()
    {
        return this.worldCard.getPrefabName();
    }

    public boolean isVisible()
    {
        return true;
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        final SpaceObject that = (SpaceObject) o;

        return this.objectID == that.objectID;
    }

    public Transform getTransform()
    {
        return this.movementController.getTransform();
    }

    @Override
    public int hashCode()
    {
        return Long.hashCode(this.objectID);
    }

    @Override
    public String toString()
    {
        return "SpaceObject{" +
                "objectID=" + objectID +
                ", creatingCause=" + creatingCause +
                ", removingCause=" + removingCause +
                ", spaceEntityType=" + spaceEntityType +
                ", faction=" + faction +
                ", factionGroup=" + factionGroup +
                ", movementController=" + movementController +
                ", collider=" + collider +
                ", spaceSubscribeInfo=" + spaceSubscribeInfo +
                ", spaceObjectState=" + spaceObjectState +
                '}';
    }
}
