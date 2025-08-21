package io.github.luigeneric.core.spaceentities;


import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.binaryreaderwriter.IProtocolWrite;
import io.github.luigeneric.core.spaceentities.statsinfo.stats.SpaceSubscribeInfo;
import io.github.luigeneric.enums.Faction;
import io.github.luigeneric.enums.FactionGroup;
import io.github.luigeneric.enums.SpaceEntityType;
import io.github.luigeneric.linearalgebra.base.Transform;
import io.github.luigeneric.templates.cards.OwnerCard;
import io.github.luigeneric.templates.cards.WorldCard;

public class CargoObject extends SpaceObject
{
    private final float range;
    private final Interaction interaction;
    public CargoObject(long objectID, OwnerCard ownerCard, WorldCard worldCard,
                       Faction faction, FactionGroup factionGroup, SpaceSubscribeInfo spaceSubscribeInfo, float range, Interaction interaction)
    {
        super(objectID, ownerCard, worldCard, SpaceEntityType.CargoObject, faction, factionGroup, spaceSubscribeInfo);
        this.range = range;
        this.interaction = interaction;
    }

    @Override
    public void write(final BgoProtocolWriter bw)
    {
        super.write(bw);
        final Transform transform = this.getTransform();
        bw.writeVector3(transform.getPosition());
        bw.writeQuaternion(transform.getRotation());

        bw.writeSingle(this.range);
        bw.writeDesc(interaction);
    }


    public enum Interaction implements IProtocolWrite
    {
        None,
        Pickup,
        Dropoff,
        Loot;

        public final byte getValue()
        {
            return (byte)this.ordinal();
        }

        @Override
        public void write(final BgoProtocolWriter bw)
        {
            bw.writeByte(this.getValue());
        }

        public static Interaction forValue(final byte value)
        {
            for (Interaction interaction : values())
            {
                if (interaction.getValue() == value)
                    return interaction;
            }
            return null;
        }
    }
}
