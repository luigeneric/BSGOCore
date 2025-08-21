package io.github.luigeneric.templates.cards;


import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.enums.Faction;

/**
 * Seems to be for the starter ships from very early days...deprecated
 */
public class StarterCard extends Card
{
    private final Faction faction;
    private final String shipTexture;
    private final long connectedShipCardGuid;
    private final byte firePower;
    private final byte toughNess;
    private final byte speed;
    private final byte electronicWarfare;

    public StarterCard(long cardGuid, Faction faction, String shipTexture, long connectedShipCardGuid, byte firePower,
                       byte toughNess, byte speed, byte electronicWarfare)
    {
        super(cardGuid, CardView.Starter);
        this.faction = faction;
        this.shipTexture = shipTexture;
        this.connectedShipCardGuid = connectedShipCardGuid;
        this.firePower = firePower;
        this.toughNess = toughNess;
        this.speed = speed;
        this.electronicWarfare = electronicWarfare;
    }

    @Override
    public void write(BgoProtocolWriter bw)
    {
        super.write(bw);
        //this has to be like that...
        bw.writeString("");
        bw.writeString("");

        bw.writeByte(faction.value);
        bw.writeString(shipTexture);
        bw.writeGUID(connectedShipCardGuid);
        bw.writeByte(firePower);
        bw.writeByte(toughNess);
        bw.writeByte(speed);
        bw.writeByte(electronicWarfare);
    }
}
