package io.github.luigeneric.templates.zonestemplates;

import java.util.Objects;

public record ZoneObjectivePt(ZoneObjectivePtType zoneObjectivePtType, long pts)
{
    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ZoneObjectivePt that = (ZoneObjectivePt) o;
        return zoneObjectivePtType == that.zoneObjectivePtType;
    }

    @Override
    public int hashCode()
    {
        return Objects.hashCode(zoneObjectivePtType);
    }
}
