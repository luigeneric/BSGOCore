package io.github.luigeneric.templates.augments;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class AugmentTemplates
{
    private static final Map<Long, AugmentTemplate> augmentTemplates = new HashMap<>();
    private AugmentTemplates(){}

    public static void inject(final Map<Long, AugmentTemplate> map)
    {
        augmentTemplates.putAll(map);
    }

    public static Optional<AugmentTemplate> getTemplateForId(final long forItemGUID)
    {
        return Optional.ofNullable(augmentTemplates.get(forItemGUID));
    }
}
