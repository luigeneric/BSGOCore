package io.github.luigeneric.core.sector.management.lootsystem.killtrace;

import java.time.LocalDateTime;

public record KilledObject(long playerId, String name, LocalDateTime localDateTime)
{
}
