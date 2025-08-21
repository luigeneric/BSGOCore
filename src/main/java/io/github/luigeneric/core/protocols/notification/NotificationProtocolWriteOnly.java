package io.github.luigeneric.core.protocols.notification;

import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.core.protocols.ProtocolID;
import io.github.luigeneric.core.protocols.WriteOnlyProtocol;
import io.github.luigeneric.core.sector.management.lootsystem.ItemCountableBonusType;
import io.github.luigeneric.enums.*;
import io.github.luigeneric.templates.shipitems.ItemCountable;
import io.github.luigeneric.templates.shipitems.ShipItem;
import io.github.luigeneric.templates.shipitems.ShipItemWriterWithoutID;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class NotificationProtocolWriteOnly extends WriteOnlyProtocol
{
    public NotificationProtocolWriteOnly()
    {
        super(ProtocolID.Notification);
    }

    /**
     * Sends the reward-items for completing first mission
     * @param guiCard first gui card to display "x-assignment completed"
     * @param shipItems ShipItems which have to be countables ... bruh
     * @return first buffer
     */
    public BgoProtocolWriter writeMissionReward(final long guiCard, final List<ShipItem> shipItems)
    {
        final BgoProtocolWriter bw = newMessage();
        bw.writeMsgType(ServerMessage.Reward.shortValue);
        bw.writeGUID(guiCard); //first gui card to display "<information>-assignment completed"
        ShipItemWriterWithoutID.write(bw, shipItems);
        return bw;
    }

    public BgoProtocolWriter writeMissionCompleted(final int missionID)
    {
        final BgoProtocolWriter bw = newMessage();
        bw.writeMsgType(ServerMessage.MissionCompleted.shortValue);
        bw.writeUInt16(missionID);

        return bw;
    }

    public BgoProtocolWriter writeAugmentItem(final List<ShipItem> itemList)
    {
        BgoProtocolWriter bw = newMessage();
        bw.writeMsgType(ServerMessage.AugmentItem.shortValue);
        //bw.writeDescList(itemList);
        ShipItemWriterWithoutID.write(bw, itemList);

        return bw;
    }

    //Loot MUST be of type ItemCountable
    public BgoProtocolWriter writeLootMessageDeprecated(final List<ItemCountable> itemCountables)
    {
        final BgoProtocolWriter bw = newMessage();
        bw.writeMsgType(ServerMessage.LootMessage.shortValue);

        bw.writeLength(itemCountables.size());
        for (final ItemCountable itemCountable : itemCountables)
        {
            ShipItemWriterWithoutID.write(bw, itemCountable);
            bw.writeLength(0);
        }
        bw.writeLength(0);
        return bw;
    }

    public BgoProtocolWriter writeLootMessage(final List<ItemCountableBonusType> items, final List<SpecialAction> specialActions)
    {
        final BgoProtocolWriter bw = newMessage();
        bw.writeMsgType(ServerMessage.LootMessage.shortValue);

        bw.writeLength(items.size());
        for (final ItemCountableBonusType item : items)
        {
            ShipItemWriterWithoutID.write(bw, item.itemCountable());
            //bw.writeLength(0);
            bw.writeLength(item.lootBonusTypeLongMap().size());
            for (final Map.Entry<LootBonusType, Long> lootBonusTypeLongEntry : item.lootBonusTypeLongMap().entrySet())
            {
                bw.writeUInt16(lootBonusTypeLongEntry.getKey().getValue());
                bw.writeUInt32(lootBonusTypeLongEntry.getValue());
            }
        }

        // guard is important, client wont filter none
        if (specialActions.size() == 1 && specialActions.getFirst() == SpecialAction.None)
            bw.writeLength(0);
        else
            bw.writeDescCollection(specialActions);

        bw.writeBoolean(false);

        return bw;
    }

    public BgoProtocolWriter writeOreMined(final ItemCountable itemCountable)
    {
        BgoProtocolWriter bw = newMessage();
        bw.writeMsgType(ServerMessage.OreMined.shortValue);
        ShipItemWriterWithoutID.write(bw, itemCountable); //write without serverID

        return bw;
    }

    /**
     * @param jumpErrorSeverity other makes the color normal, error is red
     * @param jumpErrorReason
     * @return
     */
    public BgoProtocolWriter writeJumpNotification(final JumpErrorSeverity jumpErrorSeverity, final JumpErrorReason jumpErrorReason)
    {
        final BgoProtocolWriter bw = newMessage();
        bw.writeMsgType(ServerMessage.JumpNotification.getValue());
        bw.writeByte(jumpErrorSeverity.getValue());
        bw.writeByte(jumpErrorReason.getValue());

        return bw;
    }




    /**
     * This seems to be not used anymore :-(
     */
    @Deprecated(since = "unknown reason but now used anymore")
    public BgoProtocolWriter writeFtlMissionOff()
    {
        BgoProtocolWriter bw = newMessage();
        bw.writeMsgType(ServerMessage.FtlMissionsOff.shortValue);
        return bw;
    }

    public BgoProtocolWriter writeEmergencyMessage(final String message, final float timeInSeconds)
    {
        final BgoProtocolWriter bw = newMessage();
        bw.writeMsgType(ServerMessage.EmergencyMessage.shortValue);
        bw.writeString(message);
        bw.writeUInt16(0); //does nothing
        bw.writeSingle(timeInSeconds);

        return bw;
    }
    public BgoProtocolWriter writeDailyLoginBonus(final long bonusLevel, final Set<Long> rewarGuids)
    {
        BgoProtocolWriter bw = newMessage();
        bw.writeMsgType(ServerMessage.DailyLoginBonus.shortValue);
        bw.writeUInt32(bonusLevel);
        bw.writeUInt32Collection(rewarGuids);

        return bw;
    }

    public BgoProtocolWriter writeExperienceGained(final int exp)
    {
        BgoProtocolWriter bw = newMessage();
        bw.writeMsgType(ServerMessage.Experience.shortValue);
        bw.writeByte((byte) 0); //expType but its useless imho
        bw.writeInt32(exp);

        return bw;
    }

    public BgoProtocolWriter writeSectorEventTask(final long eventObjectId, final List<SectorEventProtectTask> sectorEventTasks)
    {
        BgoProtocolWriter bw = newMessage();
        bw.writeMsgType(ServerMessage.SectorEventTasks.shortValue);

        bw.writeUInt32(eventObjectId);
        bw.writeDescCollection(sectorEventTasks);

        return bw;
    }
    public BgoProtocolWriter writeSectorFortification(final Faction faction,
                                                      final long sectorGUID,
                                                      final short level,
                                                      final SectorFortificationChangeType sectorFortificationChangeType)
    {
        final BgoProtocolWriter bw = newMessage();
        bw.writeMsgType(ServerMessage.SectorFortificationLevel.shortValue);

        bw.writeByte(faction.value);
        bw.writeGUID(sectorGUID);
        bw.writeByte((byte) level);
        bw.writeByte(sectorFortificationChangeType.getValue());

        return bw;
    }

    public BgoProtocolWriter writeOutpostAttacked(final Faction faction, final long sectorGUID,
                                                  final OutpostAttackedType outpostAttackedType)
    {
        BgoProtocolWriter bw = newMessage();
        bw.writeMsgType(ServerMessage.OutpostAttacked.shortValue);
        bw.writeByte(faction.value);
        bw.writeGUID(sectorGUID);
        bw.writeByte(outpostAttackedType.value);

        return bw;
    }

    public BgoProtocolWriter writeHeavyFight(final long sectorGUID)
    {
        BgoProtocolWriter bw = newMessage();
        bw.writeMsgType(ServerMessage.HeavyFight.shortValue);
        bw.writeGUID(sectorGUID);

        return bw;
    }

    public BgoProtocolWriter writeDebugMessage(final String message)
    {
        BgoProtocolWriter bw = newMessage();
        bw.writeMsgType(ServerMessage.Message.shortValue);
        bw.writeString(message);
        return bw;
    }

    public BgoProtocolWriter writeMiningShipUnderAttack(final long sectorGUID, final MiningShipAttackedType miningShipAttackType,
                                                        final boolean isYourMiningShip)
    {
        BgoProtocolWriter bw = newMessage();
        bw.writeMsgType(ServerMessage.MiningShipUnderAttack.shortValue);
        bw.writeGUID(sectorGUID);
        bw.writeByte(miningShipAttackType.value);
        bw.writeBoolean(isYourMiningShip);

        return bw;
    }

    public BgoProtocolWriter writeSystemUpgradeResult(final boolean upgradeSuccessful)
    {
        BgoProtocolWriter bw = newMessage();
        bw.writeMsgType(ServerMessage.SystemUpgradeResult.shortValue);
        bw.writeBoolean(upgradeSuccessful);

        return bw;
    }
}
