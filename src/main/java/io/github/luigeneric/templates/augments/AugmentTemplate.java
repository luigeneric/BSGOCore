package io.github.luigeneric.templates.augments;


import io.github.luigeneric.templates.utils.AugmentActionType;

public abstract class AugmentTemplate
{
    protected final AugmentActionType augmentActionType;
    protected final long associatedItemGUID;

    public AugmentTemplate(AugmentActionType augmentActionType, long associatedItemGUID)
    {
        this.augmentActionType = augmentActionType;
        this.associatedItemGUID = associatedItemGUID;
    }

    public AugmentActionType getAugmentActionType()
    {
        return augmentActionType;
    }

    public long getAssociatedItemGUID()
    {
        return associatedItemGUID;
    }
}