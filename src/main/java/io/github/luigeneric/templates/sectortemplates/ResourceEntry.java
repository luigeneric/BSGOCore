package io.github.luigeneric.templates.sectortemplates;


import io.github.luigeneric.enums.ResourceType;

public record ResourceEntry(ResourceType resourceType, int chance, float hpToResourceFactor, float variation)
{
}
