package io.github.luigeneric.templates.cards;

import com.google.gson.annotations.SerializedName;
import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.enums.Faction;
import io.github.luigeneric.templates.shipitems.ItemProvider;
import io.github.luigeneric.templates.shipitems.ShipItem;
import io.github.luigeneric.templates.shipitems.ShipItemWriterWithoutID;
import io.github.luigeneric.templates.utils.AugmentActionType;

import java.util.List;

public class RewardCard extends Card
{
    private final int experience;
    private final List<ShipItem> shipItems;
    @SerializedName("action")
    private final AugmentActionType action;
    /**
     * Always empty
     */
    private final long packagedCubits;
    @SerializedName("packageName")
    private final String packageName;
    private final long itemGroup;
    private final List<ShipItem> colonialItems;
    private final List<ShipItem> cylonItems;


    public RewardCard(long cardGUID, int experience, List<ShipItem> shipItems, AugmentActionType actualAction, long packagedCubits,
                      String packageName, long itemGroup, List<ShipItem> colonialItems, List<ShipItem> cylonItems)
    {
        super(cardGUID, CardView.Reward);
        this.experience = experience;
        this.shipItems = shipItems;
        this.action = actualAction;
        this.packagedCubits = packagedCubits;
        this.packageName = packageName;
        this.itemGroup = itemGroup;
        this.colonialItems = colonialItems;
        this.cylonItems = cylonItems;
    }

    @Override
    public void write(BgoProtocolWriter bw)
    {
        super.write(bw);

        bw.writeInt32(experience);
        ShipItemWriterWithoutID.write(bw, shipItems);
        bw.writeByte(action.value);
        bw.writeUInt32(packagedCubits);
        bw.writeString(packageName);
        bw.writeUInt32(itemGroup);

        ItemProvider.writeItems(bw, colonialItems);
        ItemProvider.writeItems(bw, cylonItems);
    }

    @Override
    public String toString()
    {
        return "RewardCard{" +
                "experience=" + experience +
                ", shipItems=" + shipItems +
                ", actualAction=" + action +
                ", packagedCubits=" + packagedCubits +
                ", packageName='" + packageName + '\'' +
                ", itemGroup=" + itemGroup +
                ", colonialItems=" + colonialItems +
                ", cylonItems=" + cylonItems +
                ", cardGuid=" + cardGuid +
                ", cardView=" + cardView +
                '}';
    }

    public int getExperience()
    {
        return experience;
    }

    public List<ShipItem> getShipItems(final Faction faction)
    {
        switch (faction)
        {
            case Colonial ->
            {
                return getColonialItems();
            }
            case Cylon ->
            {
                return getCylonItems();
            }
            default ->
            {
                return List.of();
            }
        }
    }
    public List<ShipItem> getShipItems()
    {
        return shipItems.stream().map(ShipItem::copy).toList();
    }

    private List<ShipItem> getColonialItems()
    {
        return colonialItems.stream().map(ShipItem::copy).toList();
    }

    private List<ShipItem> getCylonItems()
    {
        return cylonItems.stream().map(ShipItem::copy).toList();
    }
}
