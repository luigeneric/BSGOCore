package io.github.luigeneric.core.sector.management.spawn;


import io.github.luigeneric.linearalgebra.base.Quaternion;
import io.github.luigeneric.linearalgebra.base.Vector3;
import io.github.luigeneric.templates.sectortemplates.PlayerSpawnAreaTemplate;
import io.github.luigeneric.utils.BgoRandom;
import lombok.Getter;

public class SpawnArea
{
    @Getter
    private final Quaternion rotation;
    @Getter
    private final BgoRandom rnd;
    private final PlayerSpawnAreaTemplate spawnAreaTemplate;

    public SpawnArea(final PlayerSpawnAreaTemplate spawnAreaTemplate)
    {
        this.spawnAreaTemplate = spawnAreaTemplate;
        this.rnd = new BgoRandom();
        this.rotation = spawnAreaTemplate.direction().quaternion();
    }


    public Vector3 getRandomPosition()
    {
        return new Vector3(rnd.getInsideVectors(spawnAreaTemplate.a().toArray(), spawnAreaTemplate.b().toArray()));
    }
}