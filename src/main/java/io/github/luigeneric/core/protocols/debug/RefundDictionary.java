package io.github.luigeneric.core.protocols.debug;

import lombok.RequiredArgsConstructor;

import java.util.Map;

@RequiredArgsConstructor
public class RefundDictionary
{
    private final long initialLevel1Guid;
    private final Map<Byte, Float> summedPrices;

    /**
     * Returns the price for a given level
     * @param level the level to fetch the price for
     * @return if not found it will always return 0
     */
    public float getPriceForLevel(final byte level)
    {
        return this.summedPrices.getOrDefault(level, 0F);
    }
}
