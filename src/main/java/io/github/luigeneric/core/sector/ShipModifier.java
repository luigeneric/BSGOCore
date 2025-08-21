package io.github.luigeneric.core.sector;


import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.binaryreaderwriter.IProtocolWrite;
import io.github.luigeneric.core.player.ShipAbility;
import io.github.luigeneric.templates.shipitems.ShipSystem;
import io.github.luigeneric.templates.utils.AbilityActionType;
import io.github.luigeneric.templates.utils.ObjectStat;
import io.github.luigeneric.templates.utils.ObjectStats;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ShipModifier implements IProtocolWrite
{
    private long serverID;
    private final ShipAbility shipAbility;
    private final ShipSystem shipSystem;
    private final ObjectStats itemBuffAdd;
    private final ObjectStats remoteBuffAdd;
    private final ObjectStats remoteBuffMultiply;
    private final double timeOfActivation;
    private final long sourcePlayerId;

    private ShipModifier(
            final long serverID,
            final ShipAbility shipAbility,
            final ShipSystem shipSystem,
            final double startTime,
            final long sourcePlayerId
    )
    {
        this.serverID = serverID;
        this.shipAbility = Objects.requireNonNull(shipAbility, "ShipAbility cannot be null!");
        this.shipSystem = shipSystem;
        this.itemBuffAdd = new ObjectStats(shipAbility.getItemBuffAdd().getCopy());
        this.remoteBuffMultiply = new ObjectStats(shipAbility.getRemoteBuffMultiply().getCopy());
        this.remoteBuffAdd = new ObjectStats(shipAbility.getRemoteBuffAdd().getCopy());
        this.timeOfActivation = startTime * 0.001;
        this.sourcePlayerId = sourcePlayerId;
    }

    public double getTimeOfActivation()
    {
        return timeOfActivation;
    }
    public LocalDateTime getTimeOfActivationLocalDateTime()
    {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli((long) (this.timeOfActivation * 1000)), ZoneId.of("UTC"));
    }

    public static ShipModifier create(final ShipAbility shipAbility, final ShipSystem shipSystem, final double startTime, final long sourcePlayerId)
    {
        return new ShipModifier(0, shipAbility, shipSystem, startTime, sourcePlayerId);
    }


    @Override
    public void write(final BgoProtocolWriter bw)
    {
        bw.writeUInt32(this.serverID);
        bw.writeGUID(this.shipAbility.getShipAbilityCard().getCardGuid());
        bw.writeSingle(this.getItemBuffAdd().getStat(ObjectStat.Duration));
    }

    public AbilityActionType getAbilityType()
    {
        return this.shipAbility.getShipAbilityCard().getAbilityActionType();
    }

    public long getServerID()
    {
        return this.serverID;
    }

    public void setServerID(final long serverID)
    {
        this.serverID = serverID;
    }

    public ShipAbility getShipAbility()
    {
        return shipAbility;
    }

    public ShipSystem getShipSystem()
    {
        return shipSystem;
    }

    public ObjectStats getItemBuffAdd()
    {
        return itemBuffAdd;
    }

    public ObjectStats getRemoteBuffMultiply()
    {
        return remoteBuffMultiply;
    }

    public ObjectStats getRemoteBuffAdd() {
        return remoteBuffAdd;
    }

    public long getSourcePlayerId()
    {
        return sourcePlayerId;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ShipModifier that = (ShipModifier) o;

        return serverID == that.serverID;
    }

    @Override
    public int hashCode()
    {
        return (int) (serverID ^ (serverID >>> 32));
    }

    //I guess this is useless
    public enum BuffType
    {
        Buff(1),
        SectorModifier(2);

        public static final int SIZE = Short.SIZE;

        public final short shortValue;

        BuffType(int i)
        {
            this((short) i);
        }

        private static final class MappingsHolder
        {
            private static final Map<Short, BuffType> mappings = new HashMap<Short, BuffType>();
        }

        private static Map<Short, BuffType> getMappings()
        {
            return MappingsHolder.mappings;
        }

        BuffType(short value)
        {
            shortValue = value;
            getMappings().put(value, this);
        }

        public short getValue()
        {
            return shortValue;
        }

        public static BuffType forValue(short value)
        {
            return getMappings().get(value);
        }
    }

    @Override
    public String toString()
    {
        return "ShipModifier{" +
                "serverID=" + serverID +
                ", shipAbility=" + shipAbility +
                ", shipSystem=" + shipSystem +
                ", itemBuffAdd=" + itemBuffAdd +
                ", remoteBuffAdd=" + remoteBuffAdd +
                ", remoteBuffMultiply=" + remoteBuffMultiply +
                ", timeOfActivation=" + timeOfActivation +
                '}';
    }
}
