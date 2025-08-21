package io.github.luigeneric.templates.colliderstemplates;

public abstract class ColliderTemplate
{
    protected final String prefabName;
    protected final ColliderType type;

    public ColliderTemplate(String prefabName, ColliderType type)
    {
        this.prefabName = prefabName;
        this.type = type;
    }
}
