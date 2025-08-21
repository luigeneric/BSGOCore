package io.github.luigeneric.templates.cards;

import com.google.gson.annotations.SerializedName;
import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.enums.Faction;
import io.github.luigeneric.templates.utils.Price;
import io.github.luigeneric.templates.utils.ShopCategory;
import io.github.luigeneric.templates.utils.ShopItemType;
import io.github.luigeneric.templates.utils.UnmodifiablePrice;

import java.util.Arrays;

public class ShopItemCard extends Card
{
    @SerializedName("Category")
    private ShopCategory shopCategory;
    @SerializedName("ItemType")
    private ShopItemType shopItemType;
    @SerializedName("Tier")
    private byte tier;
    @SerializedName("Faction")
    private Faction faction;
    @SerializedName("SortingNames")
    private final String[] sortingNames;
    @SerializedName("SortingWeight")
    private final int sortingWeight;
    @SerializedName("BuyPrice")
    private final Price buyPrice;
    @SerializedName("UpgradePrice")
    private final Price upgradePrice;
    @SerializedName("SellPrice")
    private final Price sellPrice;
    @SerializedName("CanBeSold")
    private final boolean canBeSold;


    public ShopItemCard(long cardGuid, ShopCategory shopCategory, ShopItemType shopItemType, byte tier, Faction faction, String[] sortingNames,
                        int sortingWeight, Price buyPrice, Price upgradePrice, Price sellPrice, boolean canBeSold)
    {
        super(cardGuid, CardView.Price);
        this.shopCategory = shopCategory;
        this.shopItemType = shopItemType;
        this.tier = tier;
        this.faction = faction;
        this.sortingNames = sortingNames;
        this.sortingWeight = sortingWeight;
        this.buyPrice = buyPrice;
        this.upgradePrice = upgradePrice;
        this.sellPrice = sellPrice;
        this.canBeSold = canBeSold;
    }


    @Override
    public String toString()
    {
        return "ShopItemCard{" +
                "shopCategory=" + shopCategory +
                ", shopItemCategory=" + shopItemType +
                ", tier=" + tier +
                ", faction=" + faction +
                ", sortingNames=" + Arrays.toString(sortingNames) +
                ", sortingWeight=" + sortingWeight +
                ", buyPrice=" + buyPrice +
                ", upgradePrice=" + upgradePrice +
                ", sellPrice=" + sellPrice +
                ", canBeSold=" + canBeSold +
                ", cardGuid=" + cardGuid +
                ", cardView=" + cardView +
                '}';
    }

    @Override
    public void write(BgoProtocolWriter bw)
    {
        super.write(bw);
        bw.writeByte(shopCategory.value);
        bw.writeByte(shopItemType.value);
        bw.writeByte(tier);
        bw.writeByte(faction.value);
        bw.writeStringArray(sortingNames);
        bw.writeUInt16(sortingWeight);
        bw.writeDesc(buyPrice);
        bw.writeDesc(upgradePrice);
        bw.writeDesc(sellPrice);
        bw.writeBoolean(canBeSold);
    }


    public ShopCategory getShopCategory()
    {
        return shopCategory;
    }

    public ShopItemType getShopItemType()
    {
        return shopItemType;
    }

    public byte getTier()
    {
        return tier;
    }

    public Faction getFaction()
    {
        return faction;
    }

    public String[] getSortingNames()
    {
        return sortingNames;
    }

    public int getSortingWeight()
    {
        return sortingWeight;
    }

    public Price getBuyPrice()
    {
        return new UnmodifiablePrice(this.buyPrice);
    }

    public Price getUpgradePrice()
    {
        return new UnmodifiablePrice(this.upgradePrice);
    }

    public Price getSellPrice()
    {
        return new UnmodifiablePrice(this.sellPrice);
    }

    public boolean isCanBeSold()
    {
        return canBeSold;
    }
}
