package io.github.luigeneric.templates.cards;

import com.google.gson.annotations.SerializedName;
import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.enums.ZoneInfoType;
import io.github.luigeneric.enums.ZonePlugin;
import io.github.luigeneric.templates.utils.ShipRole;
import io.github.luigeneric.templates.utils.ZoneBracketInfo;

import java.util.List;


public class ZoneCard extends Card
{
    /**
     * Used for bgo.jsonkey.Description
     * and .ShortDescription
     * and .Name
     */
    private final String jsonKey;

    /**
     * eg. icon_tournament
     */
    private final String zoneIconFileName;

    /**
     * eg.  event_header_LinerTGB
     */
    private final String zoneImageFileName;

    /**
     * same here event_header_LinerTGB
     */
    private final String zoneImageCylonFileName;

    /**
     * Example from each card is always "None"
     * There is code in the client for "Scavenger" beside "None"
     * It will toggle ui stuff on first entry I guess
     */
    private final ZoneInfoType zoneInfoType;

    private final short minLevel;
    private final short maxLevel;

    /**
     * on true for example with battlespace
     * false for topgun
     */
    private final boolean canJoinWithParty;

    /**
     * tier allow list, mostly only one
     */
    private final List<Byte> tierWhiteList;

    /**
     * almost not used but on strike
     * strike blocklist is for example stealth which is stupid after the nerf but well
     */
    private final List<Long> shipBlackList;

    /**
     * always empty but could be used to block gunships
     */
    private final List<ShipRole> rolesBlackList;

    /**
     * Either FactionBoard, ScoreBoard or FfaScoring
     * FactionBoard is not used anymore in 2016 state
     * Nothing is used in battlespace mode
     */
    @SerializedName("Plugins")
    private final List<ZonePlugin> plugins;
    /**
     * The bracketInfos array provides a structured way to store and access detailed information about
     * different gaming zones and their respective level brackets, admission criteria, and rewards.
     * rewards are left empty
     * admission criteria was n token/merits in topgun, for battlespace it was free
     */
    @SerializedName("BracketInfo")
    private final List<ZoneBracketInfo> bracketInfos;


    public ZoneCard(long cardGUID, String jsonKey, String zoneIconFileName, String zoneImageFileName, String zoneImageCylonFileName,
                    ZoneInfoType zoneInfoType, short minLevel, short maxLevel, boolean canJoinWithParty,
                    List<Byte> tierWhiteList, List<Long> shipBlackList, List<ShipRole> rolesBlackList, List<ZonePlugin> plugins, List<ZoneBracketInfo> bracketInfos)
    {
        super(cardGUID, CardView.Zone);
        this.jsonKey = jsonKey;
        this.zoneIconFileName = zoneIconFileName;
        this.zoneImageFileName = zoneImageFileName;
        this.zoneImageCylonFileName = zoneImageCylonFileName;
        this.zoneInfoType = zoneInfoType;
        this.minLevel = minLevel;
        this.maxLevel = maxLevel;
        this.canJoinWithParty = canJoinWithParty;
        this.tierWhiteList = tierWhiteList;
        this.shipBlackList = shipBlackList;
        this.rolesBlackList = rolesBlackList;
        this.plugins = plugins;
        this.bracketInfos = bracketInfos;
    }

    @Override
    public void write(BgoProtocolWriter bw)
    {
        super.write(bw);
        bw.writeString(jsonKey);
        bw.writeString(zoneIconFileName);
        bw.writeString(zoneImageFileName);
        bw.writeString(zoneImageCylonFileName);
        bw.writeByte(zoneInfoType.value);
        bw.writeInt16(minLevel);
        bw.writeInt16(maxLevel);
        bw.writeBoolean(canJoinWithParty);

        final int tierWhiteListSize = tierWhiteList.size();
        bw.writeUInt16(tierWhiteListSize);
        for (byte tier : tierWhiteList)
        {
            bw.writeByte(tier);
        }

        bw.writeUInt32Collection(shipBlackList);

        bw.writeDescCollection(rolesBlackList);

        bw.writeDescCollection(plugins);

        bw.writeDescCollection(this.bracketInfos);
    }
}
