package io.github.luigeneric.core.sector.management.lootsystem.loot;



import io.github.luigeneric.core.sector.management.ObjectLeftSubscriber;
import io.github.luigeneric.core.sector.objleft.ObjectLeftDescription;
import io.github.luigeneric.core.spaceentities.SpaceObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class LootAssociations implements ObjectLeftSubscriber
{
    //SpaceObject-ID, Loot
    private final Map<Long, Loot> lootMap;

    public LootAssociations(final Map<Long, Loot> lootMap)
    {
        this.lootMap = lootMap;
    }
    public LootAssociations()
    {
        this(new HashMap<>());
    }

    public boolean hasLoot(final SpaceObject spaceObject)
    {
        final Loot loot = this.lootMap.get(spaceObject.getObjectID());
        if (loot == null) return false;

        return loot.hasLoot();
    }
    public void addLoot(final SpaceObject spaceObject, final Loot loot)
    {
        this.lootMap.put(spaceObject.getObjectID(), loot);
    }
    public Optional<Loot> get(final SpaceObject spaceObject)
    {
        return Optional.ofNullable(this.lootMap.get(spaceObject.getObjectID()));
    }
    public Optional<Loot> getAndRemove(final SpaceObject spaceObject)
    {
        return this.getAndRemove(spaceObject.getObjectID());
    }
    public Optional<Loot> getAndRemove(final long objectID)
    {
        return Optional.ofNullable(this.lootMap.remove(objectID));
    }
    public boolean remove(final long objectID)
    {
        final Loot value = this.lootMap.remove(objectID);
        return value != null;
    }

    @Override
    public void onUpdate(final ObjectLeftDescription arg)
    {
        this.remove(arg.getRemovedSpaceObject().getObjectID());
    }
}
