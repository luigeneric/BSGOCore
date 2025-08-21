package io.github.luigeneric.templates.augments;


import io.github.luigeneric.templates.utils.AugmentActionType;

public class AugmentTeleportTemplate extends AugmentTemplate
{

    public AugmentTeleportTemplate(final long associatedItemGUID)
    {
        super(AugmentActionType.Teleport, associatedItemGUID);
    }
}
