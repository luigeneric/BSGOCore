package io.github.luigeneric.templates.augments;


import io.github.luigeneric.enums.FactorType;

/**
 * Wrapper around type and value
 * @param type FactorType
 * @param value value = 1 => 100% bonus, value = 3 => 300% bonus
 */
public record FactorTypeRecord(FactorType type, float value)
{
}
