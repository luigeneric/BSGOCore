package io.github.luigeneric.core.player;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Each zone has an admission(free or payment required)
 */
public class ZonesAdmissions
{
    private final Map<Long, ZoneAdmission> zoneAdmissionMap;

    public ZonesAdmissions()
    {
        this.zoneAdmissionMap = new ConcurrentHashMap<>();
    }

    public void add(final ZoneAdmission zoneAdmission)
    {
        this.zoneAdmissionMap.put(zoneAdmission.zoneGuid(), zoneAdmission);
    }

    public Optional<ZoneAdmission> get(final long guid)
    {
        return Optional.ofNullable(this.zoneAdmissionMap.get(guid));
    }
}

