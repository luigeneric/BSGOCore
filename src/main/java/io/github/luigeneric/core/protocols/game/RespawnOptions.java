package io.github.luigeneric.core.protocols.game;

import java.util.List;

public record RespawnOptions(List<Long> sectorIds, List<Long> carrierIds)
{
}
