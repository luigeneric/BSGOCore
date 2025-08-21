package io.github.luigeneric.templates.colliderstemplates;


import io.github.luigeneric.linearalgebra.utility.Face;

import java.util.List;

public class MeshColliderTemplate extends ColliderTemplate
{
    private final List<Face> faces;

    public MeshColliderTemplate(String prefabName, List<Face> faces)
    {
        super(prefabName, ColliderType.Mesh);
        this.faces = faces;
    }

    public List<Face> getFaces()
    {
        return faces;
    }

    @Override
    public String toString()
    {
        return "MeshColliderTemplate{" +
                "faces=" + faces +
                ", prefabName='" + prefabName + '\'' +
                ", type=" + type +
                '}';
    }
}
