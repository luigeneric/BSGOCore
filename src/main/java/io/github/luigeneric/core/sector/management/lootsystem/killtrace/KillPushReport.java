package io.github.luigeneric.core.sector.management.lootsystem.killtrace;

import java.util.List;

public record KillPushReport(long killerId, String killerName, List<KilledObject> killerList)
{}
