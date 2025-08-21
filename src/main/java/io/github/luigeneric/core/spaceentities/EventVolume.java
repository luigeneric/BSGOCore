package io.github.luigeneric.core.spaceentities;


import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.core.spaceentities.statsinfo.stats.SpaceSubscribeInfo;
import io.github.luigeneric.enums.Faction;
import io.github.luigeneric.enums.FactionGroup;
import io.github.luigeneric.enums.SpaceEntityType;
import io.github.luigeneric.templates.cards.OwnerCard;
import io.github.luigeneric.templates.cards.WorldCard;

/**
 * For SectorBorder (a sphere) and Capture(???)
 */
public class EventVolume extends SpaceObject
{
    private final float radius;
    private final boolean inverted;
    private final VolumeNotificationType volumeNotificationType;

    public EventVolume(long objectID, OwnerCard ownerCard, WorldCard worldCard, SpaceSubscribeInfo spaceSubscribeInfo, float radius, boolean inverted, VolumeNotificationType volumeNotificationType)
    {
        super(objectID, ownerCard, worldCard, SpaceEntityType.Volume, Faction.Neutral, FactionGroup.Group0, spaceSubscribeInfo);
        this.radius = radius;
        this.inverted = inverted;
        this.volumeNotificationType = volumeNotificationType;
    }

    @Override
    public void write(final BgoProtocolWriter bw)
    {
        super.write(bw);

        bw.writeVector3(this.getMovementController().getPosition());
        bw.writeSingle(radius);
        bw.writeBoolean(inverted);
        bw.writeByte(this.volumeNotificationType.getValue());
    }

    private RadiationLevel getCurrentRadiationLevel(final float distance)
    {
        if (this.inverted || distance <= 0f)
        {
            return RadiationLevel.None;
        }
        float num = distance / this.radius;
        if (num > 1f)
        {
            return RadiationLevel.None;
        }
        if (num > 0.75f)
        {
            return RadiationLevel.Low;
        }
        if (num > 0.5f)
        {
            return RadiationLevel.Medium;
        }
        if (num > 0.25f)
        {
            return RadiationLevel.High;
        }
        return RadiationLevel.Critical;
    }

    public enum RadiationLevel
    {
        None,
        Low,
        Medium,
        High,
        Critical
    }

    /**
     * Only used for SectorBorder in state of 2019
     * Capture seems to be dead code on the client's end
     */
    public enum VolumeNotificationType
    {
        None,
        SectorBorder,
        Capture;

        public final byte getValue()
        {
            return (byte) this.ordinal();
        }
    }
}
