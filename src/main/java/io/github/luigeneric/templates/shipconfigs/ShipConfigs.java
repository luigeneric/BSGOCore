package io.github.luigeneric.templates.shipconfigs;

import java.util.*;

public class ShipConfigs
{
    private ShipConfigs(){
    }

    private static final Map<Integer, ShipConfigTemplate> shipConfigTemplateMap = new HashMap<>();

    public static void putAll(final List<ShipConfigTemplate> shipConfigTemplates)
    {
        for (final ShipConfigTemplate shipConfigTemplate : shipConfigTemplates)
        {
            shipConfigTemplateMap.put(shipConfigTemplate.id, shipConfigTemplate);
        }
    }

    public static Set<Integer> getFreeIds()
    {
        int num = 0;
        final Set<Integer> ids = new HashSet<>();
        for (int i = 0; ids.size() < 100; i++)
        {
            var contains = shipConfigTemplateMap.containsKey(i);
            if (!contains)
                ids.add(i);
        }
        return ids;
    }

    public static Optional<ShipConfigTemplate> getConfigForID(final int id)
    {
        return Optional.ofNullable(shipConfigTemplateMap.get(id));
    }

    public static List<ShipConfigTemplate> getConfigsForShipGUID(final long guid)
    {
        return shipConfigTemplateMap.values()
                .stream()
                .filter(config -> config.shipGUID == guid)
                .toList();
    }
    public static Optional<ShipConfigTemplate> getFirstBestConfigForGUID(final long guid)
    {
        return shipConfigTemplateMap.values().stream()
                .filter(config -> config.shipGUID == guid)
                .findAny();
    }
    public static Optional<ShipConfigTemplate> getFirstBestConfigForGUIDAndLevel(final long guid, final short level)
    {
        return shipConfigTemplateMap.values().stream()
                .filter(config -> config.shipGUID == guid)
                .filter(config -> config.level == level)
                .findAny();
    }
}
