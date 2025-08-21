package io.github.luigeneric.templates.utils;


import io.github.luigeneric.templates.shipitems.ItemType;

public enum ShopCategory
{
    None,
    Resource,
    Augment,
    Consumable,
    System,
    StarterKit,
    Ship,
    Unknown;


    public final byte value;
    ShopCategory()
    {
        this.value = (byte) this.ordinal();
    }

    public boolean isItemCountable()
    {
        return this == Resource || this == Augment || this == Consumable;
    }

    public ItemType getType()
    {
        switch (this)
        {
            case Resource, Augment, Consumable ->
            {
                return ItemType.Countable;
            }
            case System ->
            {
                return ItemType.System;
            }
            case Ship ->
            {
                return ItemType.Ship;
            }
            case StarterKit ->
            {
                return ItemType.Starter;
            }
            case None ->
            {
                return ItemType.None;
            }
            default -> throw new IllegalStateException("Could not find ItemType for ShopCategory " + this);
        }
    }
}
