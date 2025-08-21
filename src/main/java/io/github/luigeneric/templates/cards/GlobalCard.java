package io.github.luigeneric.templates.cards;

import com.google.gson.annotations.SerializedName;
import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;

import java.util.Map;

public class GlobalCard extends Card
{
    @SerializedName("TitaniumRepairCard")
    private final float titaniumRepairCard;
    @SerializedName("CubitsRepairCard")
    private final float cubitsRepairCard;
    @SerializedName("CapitalShipPrice")
    private final long captialShipPrice; //not used anymore
    @SerializedName("UndockTimeout")
    private final float undockTimeout; //not used anymore
    private final long friendBonusRewardGuid;
    private final Map<Integer, Long> specialFriendBonus;

    public GlobalCard(long cardGuid, float titaniumRepairCard, float cubitsRepairCard, long captialShipPrice, float undockTimeout, long friendBonusRewardGuid, Map<Integer, Long> specialFriendBonus)
    {
        super(cardGuid, CardView.Global);
        this.titaniumRepairCard = titaniumRepairCard;
        this.cubitsRepairCard = cubitsRepairCard;
        this.captialShipPrice = captialShipPrice;
        this.undockTimeout = undockTimeout;
        this.friendBonusRewardGuid = friendBonusRewardGuid;
        this.specialFriendBonus = specialFriendBonus;
    }

    @Override
    public void write(BgoProtocolWriter bw)
    {
        super.write(bw);
        bw.writeSingle(titaniumRepairCard);
        bw.writeSingle(cubitsRepairCard);
        bw.writeUInt32(captialShipPrice);
        bw.writeSingle(undockTimeout);
        bw.writeGUID(friendBonusRewardGuid);

        int size = specialFriendBonus.size();
        bw.writeLength(size);
        for (Map.Entry<Integer, Long> entry : specialFriendBonus.entrySet())
        {
            bw.writeByte(entry.getKey().byteValue());
            bw.writeGUID(entry.getValue());
        }
    }

    @Override
    public String toString()
    {
        return "GlobalCard{" +
                "titaniumRepairCard=" + titaniumRepairCard +
                ", cubitsReapirCard=" + cubitsRepairCard +
                ", captialShipPrice=" + captialShipPrice +
                ", undockTimeout=" + undockTimeout +
                ", friendBonusRewardGuid=" + friendBonusRewardGuid +
                ", specialFriendBonus=" + specialFriendBonus +
                ", cardGuid=" + cardGuid +
                ", cardView=" + cardView +
                '}';
    }

    public float getRepairCard(final boolean useCubits)
    {
        return useCubits ? cubitsRepairCard : titaniumRepairCard;
    }
}
