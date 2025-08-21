package io.github.luigeneric.database.fetchresults;


import io.github.luigeneric.enums.GameLocation;

public record GameLocationFetchResult(long sectorID, GameLocation gameLocation, GameLocation previousLocation)
{
}
