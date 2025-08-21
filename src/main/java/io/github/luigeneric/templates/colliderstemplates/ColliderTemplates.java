package io.github.luigeneric.templates.colliderstemplates;


import io.github.luigeneric.linearalgebra.base.Vector3;
import io.github.luigeneric.templates.templates.readers.ColliderTemplateReader;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class ColliderTemplates
{
    private final Map<String, AABBColliderTemplate> aabbColliderTemplateMap;
    private final Map<String, MeshColliderTemplate> meshColliderTemplateMap;
    private final Map<String, SphereColliderTemplate> sphereColliderTemplateMap;
    private final Map<String, CapsuleColliderTemplate> capsuleColliderTemplateMap;
    private final ColliderTemplateReader colliderTemplateReader;

    public ColliderTemplates(final Map<String, AABBColliderTemplate> aabbColliderTemplateMap,
                             final Map<String, MeshColliderTemplate> meshColliderTemplateMap,
                             final Map<String, SphereColliderTemplate> sphereColliderTemplateMap,
                             final Map<String, CapsuleColliderTemplate> capsuleColliderTemplateMap,
                             final ColliderTemplateReader colliderTemplateReader)
    {
        this.aabbColliderTemplateMap = aabbColliderTemplateMap;
        this.meshColliderTemplateMap = meshColliderTemplateMap;
        this.sphereColliderTemplateMap = sphereColliderTemplateMap;
        this.capsuleColliderTemplateMap = capsuleColliderTemplateMap;
        this.colliderTemplateReader = colliderTemplateReader;
    }

    @Inject
    public ColliderTemplates(final ColliderTemplateReader colliderTemplateReader)
    {
        this(
                new ConcurrentHashMap<>(),
                new ConcurrentHashMap<>(),
                new ConcurrentHashMap<>(),
                new ConcurrentHashMap<>(),
                colliderTemplateReader
        );
    }

    @PostConstruct
    public void postInit()
    {
        colliderTemplateReader.fetchColliderTemplates().forEach(this::putNewColliderTemplate);
    }

    public void putNewColliderTemplate(final ColliderTemplate colliderTemplate)
    {
        switch (colliderTemplate.type)
        {
            case Mesh -> this.meshColliderTemplateMap.put(colliderTemplate.prefabName, (MeshColliderTemplate) colliderTemplate);
            case AABB -> this.aabbColliderTemplateMap.put(colliderTemplate.prefabName, (AABBColliderTemplate) colliderTemplate);
            case Sphere -> this.sphereColliderTemplateMap.put(colliderTemplate.prefabName, (SphereColliderTemplate)colliderTemplate);
            case Capsule -> this.capsuleColliderTemplateMap.put(colliderTemplate.prefabName, (CapsuleColliderTemplate) colliderTemplate);
        }
    }

    public Optional<MeshColliderTemplate> getMeshColliderTemplate(final String prefabName)
    {
        return Optional.ofNullable(this.meshColliderTemplateMap.get(prefabName));
    }
    public Optional<AABBColliderTemplate> getAABBColliderTemplate(final String prefabName)
    {
        return Optional.ofNullable(this.aabbColliderTemplateMap.get(prefabName));
    }
    public Optional<SphereColliderTemplate> getSphereColliderTemplate(final String prefabName)
    {
        return Optional.ofNullable(this.sphereColliderTemplateMap.get(prefabName));
    }
    public Optional<CapsuleColliderTemplate> getCapsuleColliderTemplate(String prefabName)
    {
        return Optional.ofNullable(this.capsuleColliderTemplateMap.get(prefabName));
    }
    public Optional<ColliderTemplate> getColliderTemplate(final String prefabName)
    {
        final Optional<SphereColliderTemplate> opt1 = this.getSphereColliderTemplate(prefabName);
        if (opt1.isPresent())
            return Optional.of(opt1.get());
        final Optional<CapsuleColliderTemplate> opt2 = this.getCapsuleColliderTemplate(prefabName);
        if (opt2.isPresent())
            return Optional.of(opt2.get());
        final Optional<AABBColliderTemplate> opt3 = this.getAABBColliderTemplate(prefabName);
        if (opt3.isPresent())
            return Optional.of(opt3.get());

        return Optional.empty();
    }
    public void buildAsteroidColliderTemplateDummies(final Set<String> prefabs)
    {
        for (final String prefab : prefabs)
        {
            if (prefab.contains("asteroid_") || prefab.contains("planetoid"))
            {
                //System.out.println("prefab! "  +prefab);
                this.sphereColliderTemplateMap.put(prefab, new SphereColliderTemplate(Vector3.zero(),1));
            }
        }
    }

    @Override
    public String toString()
    {
        final StringBuilder sbAABB = new StringBuilder("{");
        final StringBuilder sbSphere = new StringBuilder("{");
        final StringBuilder sbMesh = new StringBuilder("{");
        final StringBuilder sbCapsule = new StringBuilder("{");

        this.aabbColliderTemplateMap.forEach((str, obj) -> sbAABB.append(str).append(','));
        this.meshColliderTemplateMap.forEach((str, obj) -> sbMesh.append(str).append(','));
        this.sphereColliderTemplateMap.forEach((str, obj) -> sbSphere.append(str).append(','));
        this.capsuleColliderTemplateMap.forEach((str, obj) -> sbCapsule.append(str).append(','));

        sbAABB.append("}");
        sbSphere.append("}");
        sbMesh.append("}");
        sbCapsule.append("}");
        return "ColliderTemplates{" +
                "aabbColliderTemplateMap=" + sbAABB +
                ", meshColliderTemplateMap=" + sbMesh +
                ", sphereColliderTemplateMap=" + sbSphere +
                ", capsuleColliderTemplateMap=" + sbCapsule +
                '}';
    }


}
