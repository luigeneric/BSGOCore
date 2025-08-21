package io.github.luigeneric.templates.cards;

import com.google.gson.annotations.SerializedName;
import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.enums.ShipRoleDeprecated;
import io.github.luigeneric.templates.utils.ShipRole;

public class ShipCardLight extends Card
{
    @SerializedName("ShipObjectKey")
    private final long shipObjectKey;
    @SerializedName("Tier")
    private final byte tier;
    @SerializedName("ShipRoles")
    private final ShipRole[] shipRoles;
    @SerializedName("ShipRoleDeprecated")
    private final ShipRoleDeprecated shipRoleDeprecated;

    public ShipCardLight(long cardGuid, long shipObjectKey, byte tier, ShipRole[] shipRoles, ShipRoleDeprecated shipRoleDeprecated)
    {
        super(cardGuid, CardView.ShipLight);
        this.shipObjectKey = shipObjectKey;
        this.tier = tier;
        this.shipRoles = shipRoles;
        this.shipRoleDeprecated = shipRoleDeprecated;
    }

    @Override
    public void write(BgoProtocolWriter bw)
    {
        super.write(bw);
        bw.writeGUID(shipObjectKey);
        bw.writeByte(tier);
        bw.writeLength(shipRoles.length);
        for (ShipRole shipRole : shipRoles)
        {
            bw.writeByte(shipRole.value);
        }
        bw.writeByte(shipRoleDeprecated.getValue());
    }

    public long getShipObjectKey()
    {
        return shipObjectKey;
    }

    public byte getTier()
    {
        return tier;
    }

    public ShipRole[] getShipRoles()
    {
        return shipRoles;
    }

    public ShipRoleDeprecated getShipRoleDeprecated()
    {
        return shipRoleDeprecated;
    }
}
