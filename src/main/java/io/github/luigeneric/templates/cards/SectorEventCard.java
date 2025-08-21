package io.github.luigeneric.templates.cards;


import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.enums.SectorEventTaskType;

public class SectorEventCard extends Card
{
    private final String nameCylon;
    private final String nameColonial;
    private final String descriptionCylon;
    private final String descriptionColonial;
    /**
     * WTF this is only unknown or protect WHERE ATTACK :(
     */
    private final SectorEventTaskType taskType;
    /**
     * DEAD Property, @Deprecated
     */
    @Deprecated
    private final boolean isElite;
    private final float radius;

    public SectorEventCard(long cardGuid, String nameCylon, String nameColonial, String descriptionCylon,
                           String descriptionColonial, SectorEventTaskType taskType, boolean isElite, float radius)
    {
        super(cardGuid, CardView.SectorEvent);
        this.nameCylon = nameCylon;
        this.nameColonial = nameColonial;
        this.descriptionCylon = descriptionCylon;
        this.descriptionColonial = descriptionColonial;
        this.taskType = taskType;
        this.isElite = isElite;
        this.radius = radius;
    }

    @Override
    public void write(BgoProtocolWriter bw)
    {
        //super.write(bw);
        bw
                .writeString(nameCylon)
                .writeString(nameColonial)
                .writeString(descriptionCylon)
                .writeString(descriptionColonial)
                .writeByte(taskType.getValue())
                .writeBoolean(false)
                .writeSingle(radius);
    }
}
