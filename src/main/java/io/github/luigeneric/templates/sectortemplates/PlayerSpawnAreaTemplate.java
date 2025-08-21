package io.github.luigeneric.templates.sectortemplates;


import io.github.luigeneric.enums.Faction;
import io.github.luigeneric.linearalgebra.base.Euler3;
import io.github.luigeneric.linearalgebra.base.Vector3;

/**
 * A SpawnArea consists of 2 Vectors that shows the limit of the area and a direction
 */
public record PlayerSpawnAreaTemplate(int id, Vector3 a, Vector3 b, Euler3 direction, Faction faction)
{
}
