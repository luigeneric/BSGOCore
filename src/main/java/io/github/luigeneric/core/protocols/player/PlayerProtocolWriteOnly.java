package io.github.luigeneric.core.protocols.player;


import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.core.User;
import io.github.luigeneric.core.player.*;
import io.github.luigeneric.core.player.container.IContainer;
import io.github.luigeneric.core.player.container.ShipSlot;
import io.github.luigeneric.core.player.counters.Counters;
import io.github.luigeneric.core.player.counters.Mission;
import io.github.luigeneric.core.player.counters.MissionBook;
import io.github.luigeneric.core.player.factors.Factor;
import io.github.luigeneric.core.player.factors.Factors;
import io.github.luigeneric.core.player.skills.SkillBook;
import io.github.luigeneric.core.protocols.ProtocolID;
import io.github.luigeneric.core.protocols.WriteOnlyProtocol;
import io.github.luigeneric.core.spaceentities.statsinfo.buffer.BasePropertyBuffer;
import io.github.luigeneric.enums.Faction;
import io.github.luigeneric.enums.FtlRanks;
import io.github.luigeneric.enums.ResourceType;
import io.github.luigeneric.templates.shipitems.ShipItem;
import io.github.luigeneric.templates.shipitems.ShipItemWriterWithoutID;
import io.github.luigeneric.templates.utils.ObjectStat;
import io.github.luigeneric.templates.utils.ObjectStats;
import jakarta.enterprise.context.Dependent;

import java.util.*;

@Dependent
public class PlayerProtocolWriteOnly extends WriteOnlyProtocol
{
    public PlayerProtocolWriteOnly()
    {
        super(ProtocolID.Player);
    }

    public BgoProtocolWriter writeHangarShipStats(final HangarShip hangarShip)
    {
        BgoProtocolWriter bw = newMessage();
        bw.writeMsgType(ServerMessage.Stats.value);

        final ObjectStats shipStats = hangarShip.getShipStats().getStats();
        bw.writeLength(shipStats.getAllStats().size());
        for (final Map.Entry<ObjectStat, Float> statEntry : shipStats.getAllStats().entrySet())
        {
            bw.writeByte((byte) 1);
            bw.writeUInt16(statEntry.getKey().value);
            bw.writeSingle(statEntry.getValue());
        }

        return bw;
    }
    public BgoProtocolWriter writeShipInfoDurability(final HangarShip ship)
    {
        final int shipID = ship.getServerId();
        final float durability = ship.getDurability();

        final BgoProtocolWriter bw = newMessage();
        bw.writeMsgType(ServerMessage.ShipInfo.value);
        bw.writeUInt16(shipID);
        bw.writeSingle(durability);

        return bw;
    }
    public BgoProtocolWriter writeShipStickerBinding(final HangarShip hangarShip)
    {
        final BgoProtocolWriter bw = newMessage();
        bw.writeMsgType(ServerMessage.Stickers.value);
        bw.writeUInt16(hangarShip.getServerId());
        bw.writeDescCollection(hangarShip.getStickers());

        return bw;
    }
    public BgoProtocolWriter writeShipSlots(final HangarShip hangarShip)
    {
        final BgoProtocolWriter bw = newMessage();
        bw.writeMsgType(ServerMessage.Slots.value);
        bw.writeUInt16(hangarShip.getServerId());

        final Set<Map.Entry<Integer, ShipSlot>> slots = hangarShip.getShipSlots().entrySet();
        bw.writeLength(slots.size());

        for (final Map.Entry<Integer, ShipSlot> slot : slots)
        {
            bw.writeDesc(slot.getValue());
        }
        return bw;
    }

    public BgoProtocolWriter writeMailBox(final MailBox mailBox)
    {
        Objects.requireNonNull(mailBox);
        BgoProtocolWriter bw = newMessage();
        bw.writeMsgType(ServerMessage.Mail.value);
        bw.writeDesc(mailBox);

        return bw;
    }

    public BgoProtocolWriter writeMissions(final Collection<Mission> missions)
    {
        final BgoProtocolWriter bw = newMessage();
        bw.writeMsgType(ServerMessage.Missions.value);
        bw.writeDescCollection(missions);

        return bw;
    }
    public BgoProtocolWriter writeMissions(final MissionBook missionBook)
    {
        final BgoProtocolWriter bw = newMessage();
        bw.writeMsgType(ServerMessage.Missions.value);
        bw.writeDesc(missionBook);

        return bw;
    }

    public BgoProtocolWriter writeRemoveMissions(final Collection<Integer> removeIds)
    {
        final BgoProtocolWriter bw = newMessage();
        bw.writeMsgType(ServerMessage.RemoveMissions.value);
        bw.writeUint16Collection(removeIds);

        return bw;
    }


    public BgoProtocolWriter writeCounters(final Counters counters)
    {
        final BgoProtocolWriter bw = newMessage();
        bw.writeMsgType(ServerMessage.Counters.value);
        bw.writeDesc(counters);

        return bw;
    }

    public BgoProtocolWriter writeMeritsCap(final ResourceCap resourceCap)
    {
        if (resourceCap.getGuid() != ResourceType.Token.guid)
            throw new IllegalArgumentException("Token cap resource is not token!");

        final BgoProtocolWriter bw = newMessage();

        bw.writeMsgType(ServerMessage.ResourceHardcap.value);
        bw.writeGUID(ResourceType.Token.guid); //merrits guid
        bw.writeInt32(resourceCap.getFarmed()); //first, merrits already farmed
        bw.writeInt32(resourceCap.getMax()); //second, max value to farm per limit hardcap

        return bw;
    }

    public BgoProtocolWriter writeExperience(final long exp)
    {
        final BgoProtocolWriter bw = newMessage();
        bw.writeMsgType(ServerMessage.Experience.value);
        bw.writeUInt32(exp);
        return bw;
    }
    public BgoProtocolWriter writeSpentExperience(final long spentExp)
    {
        final BgoProtocolWriter bw = newMessage();
        bw.writeMsgType(ServerMessage.SpentExperience.value);
        bw.writeUInt32(spentExp);
        return bw;
    }

    public BgoProtocolWriter writeActivePlayerShip(final int shipId)
    {
        final BgoProtocolWriter bw = newMessage();
        bw.writeUInt16(ServerMessage.ActiveShip.value);
        bw.writeUInt16(shipId);
        return bw;
    }

    public BgoProtocolWriter writeSkills(final SkillBook skillBook)
    {
        BgoProtocolWriter bw = newMessage();
        bw.writeMsgType(ServerMessage.Skills.value);
        bw.writeBoolean(false);
        bw.writeDesc(skillBook);

        return bw;
    }

    public BgoProtocolWriter writeCannotStackBoosters()
    {
        final BgoProtocolWriter bw = newMessage();
        bw.writeMsgType(ServerMessage.CannotStackBoosters.value);
        return bw;
    }

    public BgoProtocolWriter writeAllContainerItems(final IContainer container)
    {
        BgoProtocolWriter bw = newMessage();
        bw.writeDesc(container);

        return bw;
    }

    public BgoProtocolWriter writeFactors(final Factors factors)
    {
        final BgoProtocolWriter bw = newMessage();
        bw.writeMsgType(ServerMessage.Factors.value);
        bw.writeDesc(factors);

        return bw;
    }
    public BgoProtocolWriter writeFactors(final Collection<Factor> factors)
    {
        final BgoProtocolWriter bw = newMessage();
        bw.writeMsgType(ServerMessage.Factors.value);
        bw.writeDescCollection(factors);

        return bw;
    }

    public BgoProtocolWriter writeRemoveFactorIds(final Collection<Integer> ids)
    {
        final BgoProtocolWriter bw = newMessage();
        bw.writeMsgType(ServerMessage.RemoveFactors.value);
        bw.writeUint16Collection(ids);

        return bw;
    }

    public BgoProtocolWriter writeFaction(final Faction faction)
    {
        BgoProtocolWriter bw = newMessage();
        bw.writeUInt16(ServerMessage.Faction.value);
        bw.writeByte(faction.value);
        return bw;
    }

    public BgoProtocolWriter writeReset()
    {
        BgoProtocolWriter bw = newMessage();
        bw.writeUInt16(ServerMessage.Reset.value);
        return bw;
    }

    public BgoProtocolWriter writeName(final String nameChosen)
    {
        BgoProtocolWriter bw = newMessage();
        bw.writeUInt16(ServerMessage.Name.value);
        bw.writeString(nameChosen);
        return bw;
    }

    public BgoProtocolWriter writeId(final long id)
    {
        BgoProtocolWriter bw = newMessage();
        bw.writeUInt16(ServerMessage.ID.getValue());
        bw.writeUInt32(id);
        return bw;
    }

    /**
     * Sends the avatar description into first protocolwriter
     * @param avatarDescription may be null, if null it will send an empty avatar-description
     * @return
     */
    public BgoProtocolWriter writeAvatarDescription(final AvatarDescription avatarDescription)
    {
        //Objects.requireNonNull(avatarDescription, "avatarDescription cannot be null");
        if (avatarDescription == null)
        {
            return writeEmptyAvatarDescription();
        }
        BgoProtocolWriter bw = newMessage();
        bw.writeUInt16(ServerMessage.Avatar.value);
        bw.writeDesc(avatarDescription);
        return bw;
    }
    private BgoProtocolWriter writeEmptyAvatarDescription()
    {
        BgoProtocolWriter bw = newMessage();
        bw.writeUInt16(29);
        bw.writeMsgType(ServerMessage.Avatar.value);
        bw.writeUInt16(0);
        bw.writeUInt16(0);
        bw.writeByte((byte) 0);
        return bw;
    }

    public BgoProtocolWriter writeSpacePropertyBuffer(final BasePropertyBuffer spacePropertyBuffer)
    {
        final BgoProtocolWriter bw = newMessage();
        bw.writeMsgType(ServerMessage.Stats.value);
        bw.writeDesc(spacePropertyBuffer);

        return bw;
    }

    public BgoProtocolWriter writeCharacterServices(
            final CharacterServices characterServices
            )
    {
        final BgoProtocolWriter bw = newMessage();
        bw.writeMsgType(ServerMessage.CharacterServices.value);

        bw.writeDesc(characterServices);

        return bw;
    }
    public BgoProtocolWriter writeCharacterServicesDummy(final User user) throws IllegalArgumentException
    {
        final Player player = user.getPlayer();
        final BgoProtocolWriter bw = newMessage();
        bw.writeMsgType(ServerMessage.CharacterServices.value);

        long currentSectorId = player.getSectorId();
        bw.writeByte((byte) currentSectorId);
        boolean eligible = false;
        bw.writeBoolean(eligible);
        bw.writeInt64(0); //does nothing

        long cooldown = System.currentTimeMillis() - 100000;
        long lastUsed = System.currentTimeMillis();

        bw.writeInt64(cooldown);
        bw.writeInt64(cooldown);
        bw.writeInt64(lastUsed);
        bw.writeInt64(lastUsed);

        int cubitsPrice = 100000;
        bw.writeSingle(cubitsPrice);
        bw.writeSingle(cubitsPrice);
        bw.writeLength(0); //cancel

        return bw;
    }

    public BgoProtocolWriter writeAnchor(final long objectIdToAnchorOn)
    {
        final BgoProtocolWriter bw = newMessage();
        bw.writeMsgType(ServerMessage.Anchor.value);
        bw.writeUInt32(objectIdToAnchorOn);

        return bw;
    }

    public BgoProtocolWriter writeUnAnchor(final long spaceObjectIdOfPlayerShip, final UnanchorReason unanchorReason)
    {
        final BgoProtocolWriter bw = newMessage();
        bw.writeMsgType(ServerMessage.Unanchor.value);
        bw.writeUInt32(spaceObjectIdOfPlayerShip);
        bw.writeDesc(unanchorReason);

        return bw;
    }


    /**
     * Dradis mission statistics rewards
     * Wof dradis mission, dradismission
     * @param missionStartTime in seconds
     * @param missionEndTime in seconds
     * @param maxWaves the maximum number of waves the mission has
     * @param wavesCompleted the actual number the player acomplished
     * @param enemiesKilled the count of all enemies the player killed
     * @param ftlRanks gold, platin and so on
     * @param itemList items to receive from this mission
     */
    public BgoProtocolWriter writeDradisMissionStatisticsRewards(final long missionStartTime, final long missionEndTime,
                                                                 final long maxWaves, final long wavesCompleted, final long enemiesKilled,
                                                                 final FtlRanks ftlRanks, final List<ShipItem> itemList)
    {
        BgoProtocolWriter bw = newMessage();
        bw.writeMsgType(ServerMessage.Statistics.value);

        bw.writeUInt32(missionStartTime);
        bw.writeUInt32(missionEndTime);
        bw.writeUInt32(maxWaves);
        bw.writeUInt32(wavesCompleted);
        bw.writeUInt32(0); //nothing
        bw.writeUInt32(enemiesKilled);
        bw.writeUInt32(ftlRanks.getValue());
        bw.writeGUID(0); //nothing
        ShipItemWriterWithoutID.write(bw, itemList);

        return bw;
    }
}
