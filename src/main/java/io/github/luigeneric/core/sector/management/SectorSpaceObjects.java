package io.github.luigeneric.core.sector.management;

import io.github.luigeneric.core.spaceentities.Missile;
import io.github.luigeneric.core.spaceentities.Ship;
import io.github.luigeneric.core.spaceentities.SpaceObject;
import io.github.luigeneric.enums.RemovingCause;
import io.github.luigeneric.enums.SpaceEntityType;
import io.github.luigeneric.linearalgebra.base.Transform;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Stream;

@Slf4j
public class SectorSpaceObjects
{
    static final SpaceEntityType[] ALL_TYPES = SpaceEntityType.values();

    private final Map<Long, SpaceObject> spaceObjectsByObjectId;
    private final Map<SpaceEntityType, Map<Long, SpaceObject>> spaceEntityTypeObjects;
    private final Set<Long> staticAddedSpaceObjects;
    private final Map<OldUserPositionKey, Transform> transformByOldUserPosition;

    public SectorSpaceObjects(final Map<Long, SpaceObject> spaceObjectsByObjectId,
                              final Map<SpaceEntityType, Map<Long, SpaceObject>> spaceEntityTypeObjects,
                              final Map<OldUserPositionKey, Transform> transformByOldUserPosition,
                              final Set<Long> staticAddedSpaceObjects
    )
    {
        this.spaceObjectsByObjectId = spaceObjectsByObjectId;
        this.spaceEntityTypeObjects = spaceEntityTypeObjects;
        this.transformByOldUserPosition = transformByOldUserPosition;

        for (final SpaceEntityType spaceEntityType : ALL_TYPES)
        {
            this.spaceEntityTypeObjects.put(spaceEntityType, new LinkedHashMap<>());
        }
        this.staticAddedSpaceObjects = staticAddedSpaceObjects;
    }


    public Optional<SpaceObject> getByPlayerId(final long playerId)
    {
        return this.spaceObjectsByObjectId.values().stream().filter(obj -> obj.getPlayerId() == playerId).findAny();
    }

    public SectorSpaceObjects()
    {
        //this.spaceEntityTypeObjects = new HashMap<>();
        //https://richardstartin.github.io/posts/5-java-mundane-performance-tricks#use-enums-instead-of-constant-strings
        this(new LinkedHashMap<>(), new EnumMap<>(SpaceEntityType.class), new HashMap<>(), new HashSet<>());
    }

    public void add(final SpaceObject spaceObject)
    {
        final SpaceObject previous = this.spaceObjectsByObjectId.put(spaceObject.getObjectID(), spaceObject);
        if (previous != null)
        {
            log.error("Previous SpaceObject was not null, critical error! " + previous.getObjectID() + " " + previous.getSpaceEntityType());
        }
        final Map<Long, SpaceObject> existing = this.spaceEntityTypeObjects.get(spaceObject.getSpaceEntityType());
        existing.put(spaceObject.getObjectID(), spaceObject);
        if (!spaceObject.getMovementController().isMovingObject())
        {
            staticAddedSpaceObjects.add(spaceObject.getObjectID());
        }
    }

    public Optional<SpaceObject> get(final long id)
    {
        return Optional.ofNullable(this.spaceObjectsByObjectId.get(id));
    }

    public Collection<SpaceObject> values()
    {
        return this.spaceObjectsByObjectId.values();
    }


    public Set<Map.Entry<Long, SpaceObject>> entrySet()
    {
        return this.spaceObjectsByObjectId.entrySet();
    }

    public SpaceObject remove(final SpaceObject spaceObject, final RemovingCause removingCause)
    {
        return this.remove(spaceObject.getObjectID(), removingCause);
    }

    public SpaceObject remove(final long objectID, final RemovingCause removingCause)
    {
        staticAddedSpaceObjects.remove(objectID);
        final SpaceObject containingObj = this.spaceObjectsByObjectId.get(objectID);
        if (containingObj != null)
        {
            this.spaceObjectsByObjectId.remove(objectID);
            this.spaceEntityTypeObjects.get(containingObj.getSpaceEntityType()).remove(objectID);
        }

        if (containingObj != null && containingObj.isPlayer())
        {
            this.transformByOldUserPosition.remove(new OldUserPositionKey(containingObj.getPlayerId(), containingObj.getFaction()));
            if (removingCause == RemovingCause.Dock)
            {
                this.transformByOldUserPosition.put(
                        new OldUserPositionKey(containingObj.getPlayerId(), containingObj.getFaction()), new Transform(
                                containingObj.getMovementController().getPosition(),
                                containingObj.getMovementController().getRotation(),
                                true));
            }
        }

        return containingObj;
    }


    @SuppressWarnings("unchecked")
    public <T extends SpaceObject> List<T> getSpaceObjectsOfEntityType(final SpaceEntityType spaceEntityType)
    {
        final Collection<SpaceObject> tmp = this.spaceEntityTypeObjects.get(spaceEntityType).values();
        final List<T> returnLst = new ArrayList<>(tmp.size());
        for (final SpaceObject spaceObject : tmp)
        {
            returnLst.add((T)spaceObject);
        }
        return returnLst;
    }

    @SuppressWarnings("unchecked")
    public <T> Collection<T> getSpaceObjectsCollectionOfEntityType(final SpaceEntityType spaceEntityType)
    {
        return (Collection<T>) this.spaceEntityTypeObjects.get(spaceEntityType).values();
    }


    @SuppressWarnings("unchecked")
    public <T extends SpaceObject> List<T> getSpaceObjectsOfEntityTypes(final SpaceEntityType... types)
    {
        final List<T> returnLst = new ArrayList<>();
        for (final SpaceEntityType type : types)
        {
            final Map<Long, SpaceObject> map = this.spaceEntityTypeObjects.get(type);
            returnLst.addAll((Collection<? extends T>) map.values());
        }
        return returnLst;
    }

    public Stream<SpaceObject> getSpaceObjectsOfEntityTypesStream(final SpaceEntityType... types)
    {
        Stream<SpaceObject> stream = Stream.empty();

        for (final SpaceEntityType type : types)
        {
            final Map<Long, SpaceObject> map = this.spaceEntityTypeObjects.get(type);
            stream = Stream.concat(stream, map.values().stream());
        }
        return stream;
    }

    public List<SpaceObject> getSpaceObjectsOfTypeShip()
    {
        return this.getSpaceObjectsOfEntityTypes(
                SpaceEntityType.getShipTypes()
        );
    }
    public Stream<SpaceObject> getShipsStream()
    {
        Stream<SpaceObject> stream = Stream.empty();
        for (SpaceEntityType shipType : SpaceEntityType.getShipTypes())
        {
            stream = Stream.concat(stream, this.spaceEntityTypeObjects.get(shipType).values().stream());
        }

        return stream;
    }

    @SuppressWarnings("Unchecked")
    public <T extends SpaceObject> List<T> getSpaceObjectsNotOfEntityType(final SpaceEntityType... spaceEntityTypes)
    {
        if (spaceEntityTypes.length == 0)
        {
            return List.of();
        }

        final List<T> returnValues = new ArrayList<>();

        final Set<SpaceEntityType> tmpTypes = Set.of(spaceEntityTypes);

        for (final SpaceEntityType spaceEntityType : ALL_TYPES)
        {
            if (tmpTypes.contains(spaceEntityType))
                continue;
            returnValues.addAll(this.getSpaceObjectsOfEntityType(spaceEntityType));
        }

        return returnValues;
    }


    public int size()
    {
        return this.spaceObjectsByObjectId.size();
    }

    public Optional<Ship> getShipByObjectID(final long objectID)
    {
        final Optional<SpaceObject> spObj = this.get(objectID);
        if (spObj.isEmpty()) return Optional.empty();
        if (spObj.get() instanceof Ship ship)
        {
            return Optional.of(ship);
        }
        return Optional.empty();
    }

    public Map<OldUserPositionKey, Transform> getTransformByOldUserPosition()
    {
        return transformByOldUserPosition;
    }

    public List<Missile> getFollowMissiles(final SpaceObject spaceObject)
    {
        return this.getSpaceObjectsOfEntityType(SpaceEntityType.Missile)
                .stream()
                .map(spaceObject1 -> (Missile) spaceObject1)
                .filter(m -> m.getMissileLaunchedOnObject() != null)
                .filter(m -> m.getMissileLaunchedOnObject().equals(spaceObject))
                .toList();
    }

    /**
     *
     * @param objectId
     * @return true if the static object was actually present
     */
    public boolean removeAndIfContainedStaticSpaceObjectsToAdd(final long objectId)
    {
        return this.staticAddedSpaceObjects.remove(objectId);
    }
}
