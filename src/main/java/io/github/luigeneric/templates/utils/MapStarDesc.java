package io.github.luigeneric.templates.utils;

import com.google.gson.annotations.SerializedName;
import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.binaryreaderwriter.IProtocolWrite;
import io.github.luigeneric.enums.Faction;
import io.github.luigeneric.linearalgebra.base.Vector2;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MapStarDesc implements IProtocolWrite
{
    @SerializedName("Id")
    private final long id;
    @SerializedName("Position")
    private final Vector2 position;
    @SerializedName("GUIIndex")
    private final byte guiIndex;
    @SerializedName("StarFaction")
    private final Faction starFaction;
    @SerializedName("ColonialThreatLevel")
    private final int colonialThreatLevel;
    @SerializedName("CylonThreatLevel")
    private final int cylonThreatLevel;
    @SerializedName("SectorGUID")
    private final long sectorGuid;
    @SerializedName("CanColonialOutpost")
    private final boolean canColonialOutpost;
    @SerializedName("CanCylonOutpost")
    private final boolean canCylonOutpost;
    @SerializedName("CanColonialJumpBeacon")
    private final boolean canColonialJumpBeacon;
    @SerializedName("CanCylonJumpBeacon")
    private final boolean canCylonJumpBeacon;
    @SerializedName("CanJumpColonial")
    private final boolean canJumpColonial;
    @SerializedName("CanJumpCylon")
    private final boolean canJumpCylon;



    @Override
    public void write(BgoProtocolWriter bw)
    {
        bw.writeUInt32(id);
        bw.writeVector2(position);
        bw.writeByte(guiIndex);
        bw.writeByte((byte) starFaction.value);
        bw.writeInt16((short) colonialThreatLevel);
        bw.writeInt16((short) cylonThreatLevel);
        bw.writeGUID(sectorGuid);
        bw.writeBoolean(canColonialOutpost);
        bw.writeBoolean(canCylonOutpost);
        bw.writeBoolean(canColonialJumpBeacon);
        bw.writeBoolean(canColonialJumpBeacon);
    }

    @Override
    public String toString()
    {
        return "MapStarDesc{" +
                "id=" + id +
                ", position=" + position +
                ", guiIndex=" + guiIndex +
                ", starFaction=" + starFaction +
                ", colonialThreatLevel=" + colonialThreatLevel +
                ", cylonThreatLevel=" + cylonThreatLevel +
                ", sectorGuid=" + sectorGuid +
                ", canColonialOutpost=" + canColonialOutpost +
                ", canCylonOutpost=" + canCylonOutpost +
                ", canColonialJumpBeacon=" + canColonialJumpBeacon +
                ", canCylonJumpBeacon=" + canCylonJumpBeacon +
                ", canJumpColonial=" + canJumpColonial +
                ", canJumpCylon=" + canJumpCylon +
                '}';
    }

    public long getId()
    {
        return id;
    }

    public Vector2 getPosition()
    {
        return position;
    }

    public byte getGuiIndex()
    {
        return guiIndex;
    }

    public Faction getStarFaction()
    {
        return starFaction;
    }

    public int getColonialThreatLevel()
    {
        return colonialThreatLevel;
    }

    public int getCylonThreatLevel()
    {
        return cylonThreatLevel;
    }

    public long getSectorGuid()
    {
        return sectorGuid;
    }

    public boolean isCanColonialOutpost()
    {
        return canColonialOutpost;
    }

    public boolean isCanCylonOutpost()
    {
        return canCylonOutpost;
    }

    public boolean isCanColonialJumpBeacon()
    {
        return canColonialJumpBeacon;
    }

    public boolean isCanCylonJumpBeacon()
    {
        return canCylonJumpBeacon;
    }

    public boolean isCanJumpColonial()
    {
        return canJumpColonial;
    }

    public boolean isCanJumpCylon()
    {
        return canJumpCylon;
    }

    public boolean canJumpFaction(final Faction faction)
    {
        return faction == Faction.Colonial ? isCanJumpColonial() : isCanJumpCylon();
    }
}
