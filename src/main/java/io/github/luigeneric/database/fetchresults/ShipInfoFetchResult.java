package io.github.luigeneric.database.fetchresults;

import java.util.List;

public record ShipInfoFetchResult(long guid, int serverID, float durability, String name, List<ShipSlotInfoFetchResult> slotInfoWrappers)
{}
