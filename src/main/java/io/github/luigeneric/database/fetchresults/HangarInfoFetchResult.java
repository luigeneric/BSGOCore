package io.github.luigeneric.database.fetchresults;


import java.util.List;

public record HangarInfoFetchResult(int activeIndex, List<ShipInfoFetchResult> shipInfoFetchResults)
{
}
