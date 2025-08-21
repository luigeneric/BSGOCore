package io.github.luigeneric.core.protocols.game;

import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.core.movement.Maneuver;
import io.github.luigeneric.core.movement.MovementFrame;
import io.github.luigeneric.core.protocols.ProtocolID;
import io.github.luigeneric.core.protocols.WriteOnlyProtocol;
import io.github.luigeneric.core.sector.Tick;
import io.github.luigeneric.core.sector.objleft.ObjectLeftDescription;
import io.github.luigeneric.core.spaceentities.SpaceObject;
import io.github.luigeneric.core.spaceentities.bindings.PlayerVisibility;
import io.github.luigeneric.core.spaceentities.bindings.SpaceObjectState;
import io.github.luigeneric.core.spaceentities.statsinfo.buffer.BasePropertyBuffer;
import io.github.luigeneric.enums.*;
import io.github.luigeneric.templates.shipitems.ItemCountable;
import io.github.luigeneric.templates.shipitems.ShipItemWriterWithoutID;
import io.github.luigeneric.templates.utils.Price;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

public class GameProtocolWriteOnly extends WriteOnlyProtocol
{
    public GameProtocolWriteOnly()
    {
        super(ProtocolID.Game);
    }

    public BgoProtocolWriter writeDockingDelay(final float delay)
    {
        final BgoProtocolWriter bw = newMessage();
        bw.writeMsgType(ServerMessage.DockingDelay.value);
        bw.writeSingle(delay);
        return bw;
    }
    public BgoProtocolWriter writeSpaceObjectState(final SpaceObjectState spaceObjectState)
    {
        final BgoProtocolWriter bw = newMessage();
        bw.writeMsgType(ServerMessage.ObjectState.value);
        bw.writeDesc(spaceObjectState);
        return bw;
    }

    public BgoProtocolWriter writeFlareReleased(final long objectID)
    {
        final BgoProtocolWriter bw = newMessage();
        bw.writeMsgType(ServerMessage.FlareReleased.value);
        bw.writeUInt32(objectID);

        return bw;
    }
    /**
     * Puts the given SpaceObject into the buffer for sending
     * @param spaceObject the SpaceObject to send
     * @return The BgoProtocolWriter-Buffer
     */
    public BgoProtocolWriter writeWhoIs(final SpaceObject spaceObject)
    {
        final BgoProtocolWriter bw = newMessage();
        bw.writeMsgType(ServerMessage.WhoIs.value);
        bw.writeUInt32(spaceObject.getObjectID());
        bw.writeDesc(spaceObject);
        return bw;
    }
    public BgoProtocolWriter writeMove(final SpaceObject spaceObject) throws NullPointerException
    {
        return this.writeMove(spaceObject.getObjectID(), spaceObject.getMovementController().getCurrentManeuver());
    }
    public BgoProtocolWriter writeMove(final long objectID, final Maneuver maneuver) throws NullPointerException
    {
        Objects.requireNonNull(maneuver, "Maneuver provided in writeMove is null!");

        final BgoProtocolWriter bw = newMessage();
        bw.writeMsgType(ServerMessage.Move.value);
        bw.writeUInt32(objectID);
        bw.writeDesc(maneuver);

        return bw;
    }
    public BgoProtocolWriter writeMissileDecoyed(final long missileObjectID)
    {
        final BgoProtocolWriter bw = newMessage();
        bw.writeMsgType(ServerMessage.MissileDecoyed.value);
        bw.writeUInt32(missileObjectID);
        return bw;
    }
    /**
     * Triggers first colliding sound for the associated spaceObject
     * @return first new BgoProtocolWriter Buffer
     */
    public BgoProtocolWriter writeCollide()
    {
        final BgoProtocolWriter bw = newMessage();
        bw.writeMsgType(ServerMessage.Collide.value);
        return bw;
    }
    public BgoProtocolWriter writeTimeOrigin(final long ms)
    {
        final BgoProtocolWriter bw = newMessage();
        bw.writeMsgType(ServerMessage.TimeOrigin.value);
        bw.writeInt64(ms);
        return bw;
    }
    public BgoProtocolWriter writeSyncMove(final SpaceObject spaceObject) throws IllegalArgumentException
    {
        return this.writeSyncMove(spaceObject.getObjectID(), spaceObject.getMovementController().getFrameTick(),
                spaceObject.getMovementController().getFrame(), spaceObject.getMovementController().getCurrentManeuver());
    }
    public BgoProtocolWriter writeSyncMove(final long objectID, final Tick tick, final MovementFrame movementFrame,
                                           final Maneuver maneuver) throws IllegalArgumentException
    {
        if (tick == null)
            throw new IllegalArgumentException("Tick is null");

        if (movementFrame == null)
            throw new IllegalArgumentException("MovementFrame is null");

        if (maneuver == null)
            throw new IllegalArgumentException("Maneuver is null");

        final BgoProtocolWriter bw = newMessage();
        bw.writeMsgType(ServerMessage.SyncMove.value);

        bw.writeUInt32(objectID);
        bw.writeDesc(tick);
        bw.writeDesc(movementFrame);
        bw.writeDesc(maneuver);

        return bw;
    }
    protected BgoProtocolWriter writeScan(final long objectID, final ItemCountable itemInMinable, final boolean isMinable,
                                       final Price miningPrice, final LocalDateTime coolDown)
    {
        final BgoProtocolWriter bw = newMessage();
        bw.writeMsgType(ServerMessage.Scan.value);

        bw.writeUInt32(objectID);

        if (itemInMinable == null || itemInMinable.getCardGuid() == ResourceType.None.guid)
        {
            ShipItemWriterWithoutID.writeNone(bw);
        }
        else
        {
            ShipItemWriterWithoutID.write(bw, itemInMinable);
        }

        bw.writeBoolean(isMinable);
        bw.writeDesc(miningPrice);
        bw.writeDateTime(coolDown);

        return bw;
    }
    /**
     * Creates the buffer that needs to be send to every player inside first sector so he can see the fire animation
     * @param fromObjID
     * @param objPointHash
     * @param targetObjID
     * @param weaponFxType
     * @return the ProtocolWriter
     */
    public BgoProtocolWriter writeWeaponShot(final long fromObjID, final int objPointHash,
                                             final long targetObjID, final WeaponFxType weaponFxType)
    {
        return newMessage()
                .writeMsgType(ServerMessage.WeaponShot.value)
                .writeUInt32(fromObjID)
                .writeUInt16(objPointHash)
                .writeUInt32(targetObjID)
                .writeByte(weaponFxType.getValue());
    }
    public BgoProtocolWriter writeObjectLeft(final List<ObjectLeftDescription> objectLeftDescriptions)
    {
        final BgoProtocolWriter bw = newMessage();
        bw.writeMsgType(ServerMessage.ObjectLeft.value);

        bw.writeDescCollection(objectLeftDescriptions);

        return bw;
    }



    public BgoProtocolWriter writeUpdateRoles(final long userID, final BgoAdminRoles roles)
    {
        final BgoProtocolWriter bw = newMessage();
        bw.writeMsgType(ServerMessage.UpdateRoles.value);
        bw.writeUInt32(userID);
        bw.writeUInt32(roles.value);

        return bw;
    }

    public BgoProtocolWriter writeCombatInfo(final boolean dmgIsFromMe, final long objectID, final float damage,
                                             final boolean isDestroyed, final boolean isCriticalHit)
    {
        final BgoProtocolWriter bw = newMessage();
        bw.writeMsgType(ServerMessage.CombatInfo.value);
        bw.writeBoolean(dmgIsFromMe);
        bw.writeUInt32(objectID);
        //damage needs to be negative
        bw.writeSingle(-damage);
        byte destroyed_and_critical = 0;
        destroyed_and_critical |= isDestroyed ? 1 : 0;
        destroyed_and_critical |= isCriticalHit ? 2 : 0;
        bw.writeByte(destroyed_and_critical);

        return bw;
    }

    public BgoProtocolWriter writeSpacePropertyBuffer(final BasePropertyBuffer spacePropertyBuffer)
    {
        final BgoProtocolWriter bw = newMessage();
        bw.writeMsgType(ServerMessage.Info.getValue());
        bw.writeUInt32(spacePropertyBuffer.getOwner().ownerID()); //objectID
        bw.writeDesc(spacePropertyBuffer);

        return bw;
    }

    public BgoProtocolWriter writeChangeVisibility(final long objectID, final PlayerVisibility playerVisibility)
    {
        return this.writeChangeVisibility(objectID, playerVisibility.isVisible(), playerVisibility.getChangeVisibilityReason());
    }
    public BgoProtocolWriter writeChangeVisibility(final long objectID, final boolean isVisible,
                                                   final ChangeVisibilityReason changeVisibilityReason)
    {
        final BgoProtocolWriter bw = newMessage();
        bw.writeMsgType(ServerMessage.ChangeVisibility.value);

        bw.writeUInt32(objectID);
        bw.writeBoolean(isVisible);
        bw.writeByte(changeVisibilityReason.getValue());

        return bw;
    }

    public BgoProtocolWriter writeSpawnOptions(final List<Long> sectorIDs, final List<Long> carrierPlayerIDs)
    {
        final BgoProtocolWriter bw = newMessage();
        bw.writeMsgType(ServerMessage.RespawnOptions.getValue());

        bw.writeUInt32Collection(sectorIDs);
        bw.writeUInt32Collection(carrierPlayerIDs);

        return bw;
    }

    public BgoProtocolWriter writeUpdateFactionGroup(final long objectID, final FactionGroup newFactionGroup)
    {
        final BgoProtocolWriter bw = newMessage();
        bw.writeMsgType(ServerMessage.UpdateFactionGroup.value);

        bw.writeUInt32(objectID);
        //false = group0, true = group1
        bw.writeBoolean(newFactionGroup == FactionGroup.Group1);

        return bw;
    }

    public BgoProtocolWriter writeStopGroupJump(final long playerID)
    {
        final BgoProtocolWriter bw = newMessage();
        bw.writeMsgType(ServerMessage.StopGroupJump.value);
        bw.writeUInt32(playerID);
        return bw;
    }
    public BgoProtocolWriter writeLeaderStopGroupJump()
    {
        final BgoProtocolWriter bw = newMessage();
        bw.writeMsgType(ServerMessage.LeaderStopGroupJump.value);
        return bw;
    }

    public BgoProtocolWriter writeCast(final int slotID)
    {
        final BgoProtocolWriter bw = newMessage();
        bw.writeMsgType(ServerMessage.Cast.value);
        bw.writeUInt16(slotID);
        bw.writeByte((byte) 0);
        bw.writeByte((byte) 1);
        return bw;
    }

    public BgoProtocolWriter writeJump(final float cooldown, final boolean isSoloJump, final long sectorGuid)
    {
        final BgoProtocolWriter bw = newMessage();
        bw.writeMsgType(ServerMessage.FTLCharge.getValue());
        bw.writeSingle(cooldown);
        bw.writeGUID(sectorGuid);
        bw.writeBoolean(isSoloJump);

        return bw;
    }

    public BgoProtocolWriter writeVirusBlocked()
    {
        BgoProtocolWriter bw = newMessage();
        bw.writeMsgType(ServerMessage.VirusBlocked.value);
        bw.writeUInt32(0);
        return bw;
    }

    public BgoProtocolWriter writeOutpostStateBroadcast(final int colonialOpPoints, final float colonialOpDelta,
                                                        final int cylonOpPoints, final float cylonOpDelta)
    {
        final BgoProtocolWriter bw = newMessage();
        bw.writeMsgType(ServerMessage.OutpostStateBroadcast.getValue());

        bw.writeUInt16(colonialOpPoints);
        bw.writeSingle(colonialOpDelta);

        bw.writeUInt16(cylonOpPoints);
        bw.writeSingle(cylonOpDelta);

        return bw;
    }

    public BgoProtocolWriter writeStopJump()
    {
        BgoProtocolWriter bw = newMessage();
        bw.writeMsgType(ServerMessage.StopJump.value);

        return bw;
    }
    public BgoProtocolWriter writeChangedPlayerSpeed(final float speed)
    {
        final BgoProtocolWriter bw = newMessage();
        bw.writeMsgType(ServerMessage.ChangedPlayerSpeed.value);
        bw.writeSingle(speed);
        return bw;
    }
    public BgoProtocolWriter writeMineFieldExplosions(final long objectID)
    {
        final BgoProtocolWriter bw = newMessage();
        bw.writeMsgType(ServerMessage.MineField.value);
        bw.writeUInt32(0); //read into emptiness
        bw.writeUInt32(objectID);
        return bw;
    }
}
