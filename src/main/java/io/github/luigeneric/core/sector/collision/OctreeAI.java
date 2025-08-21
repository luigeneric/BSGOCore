package io.github.luigeneric.core.sector.collision;

import io.github.luigeneric.core.spaceentities.SpaceObject;
import io.github.luigeneric.linearalgebra.base.Vector3;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

public class OctreeAI
{
    // Konstanten für den Octree
    private static final int MAX_POINTS_PER_NODE = 10;
    private static final int MAX_DEPTH = 5;



    // Die Datenstruktur für einen Knoten im Octree
    private static class Node
    {
        final Vector3 center;
        final float halfWidth;
        //each object has first position vector and contains first collider with first sphere indicator
        List<SpaceObject> spaceObjects;
        Node[] children;

        Node(final Vector3 center, final float halfWidth)
        {
            this.center = center;
            this.halfWidth = halfWidth;
            this.spaceObjects = new ArrayList<>();
            this.children = new Node[8];
        }
    }

    // Die Wurzel des Octrees
    private final Node root;

    public OctreeAI(final Vector3 center, final float halfWidth)
    {
        root = new Node(center, halfWidth);
    }

    // Fügt einen Punkt dem Octree hinzu
    public void insert(final SpaceObject spaceObject)
    {
        insert(root, spaceObject, 0);
    }

    public void updatePosition(final SpaceObject objToUpdate)
    {
        Objects.requireNonNull(objToUpdate, "ObjToUpdate in Octree was null!");

        // delete old entry
        remove(root, objToUpdate);

        // add new entry to tree
        insert(objToUpdate);
    }
    public void clearDirty()
    {
        this.root.spaceObjects = new ArrayList<>();
        this.root.children = new Node[8];
    }

    public int size()
    {
        return size(root, 0);
    }
    private int size(final Node node, int size)
    {
        if (node == null)
            return size;
        size += node.spaceObjects.size();
        for (final Node child : node.children)
        {
            if (child != null)
            {
                size = size(child, size);
            }
        }
        return size;
    }

    public void remove(final SpaceObject spaceObject)
    {
        this.remove(this.root, spaceObject);
    }
    /**
     * Removes the given SpaceObject recursively from node and every child of the given node
     * @param node
     * @param spaceObject
     * @return true if the node was inside one of the nodes(the given one or it's childs). If not, returns false.
     */
    private boolean remove(final Node node, final SpaceObject spaceObject)
    {
        // Versuche, den Punkt aus den Punkten des Knotens zu entfernen
        boolean removed = node.spaceObjects.remove(spaceObject);
        if (removed) return true;

        // Wenn der Punkt nicht in den Punkten des Knotens enthalten war, versuche, ihn in den Unterknoten zu entfernen
        for (Node child : node.children)
        {
            if (child == null)
                continue;

            final boolean removedChild = remove(child, spaceObject);
            if (removedChild)
                return true;
        }
        return false;
    }



    // Rekursive Funktion zum Einfügen eines Punkts in den Octree
    private void insert(final Node node, final SpaceObject spaceObject, final int depth)
    {
        final Vector3 point = spaceObject.getMovementController().getPosition();

        // Wenn der Knoten schon die maximale Tiefe erreicht hat oder genug Punkte enthält, füge den Punkt zu den Punkten des Knotens hinzu
        if (depth == MAX_DEPTH || node.spaceObjects.size() < MAX_POINTS_PER_NODE)
        {
            node.spaceObjects.add(spaceObject);
            //node.points.add(point);
            return;
        }

        // Bestimme, in welchen Unterknoten der Punkt eingefügt werden soll
        final int octant = getOctant(node, point);

        // Wenn der Unterknoten noch nicht existiert, erstelle ihn
        if (node.children[octant] == null)
        {
            final Vector3 newCenter = new Vector3(
                    node.center.getX() + (octant & 1) * node.halfWidth * 0.5f,
                    node.center.getY() + (octant & 2) * node.halfWidth * 0.5f,
                    node.center.getZ() + (octant & 4) * node.halfWidth * 0.5f
            );
            node.children[octant] = new Node(newCenter, node.halfWidth * 0.5f);
        }
        final Node child = node.children[octant];
        final boolean nodeContainsObject = this.testNodeCompletelyContainsObject(spaceObject, child);

        if (nodeContainsObject)
        {
            // Add obj to child-node
            insert(child, spaceObject, depth + 1);
        }
        else
        {
            // Add obj to current Node
            node.spaceObjects.add(spaceObject);
        }
    }

    private boolean testNodeCompletelyContainsObject(final SpaceObject spaceObject, final Node currentNode)
    {
        if (spaceObject.getCollider() != null)
        {
            final float currentHalfWidth = currentNode.halfWidth;
            final float sphereRadius = spaceObject.getCollider().getPruneSphereRadius();
            return (currentHalfWidth + sphereRadius) < currentNode.center.distance(spaceObject.getMovementController().getPosition());
        }
        return false;
    }

    // Bestimmt, in welchen Unterknoten ein Punkt eingefügt werden soll
    private int getOctant(final Node node, final Vector3 point)
    {
        int octant = 0;
        if (point.getX() > node.center.getX()) octant |= 1;
        if (point.getY() > node.center.getY()) octant |= 2;
        if (point.getZ() > node.center.getZ()) octant |= 4;
        return octant;
    }

    public SpaceObject getNearestNeighbor(final SpaceObject spaceObject, final float maxDistance, final Predicate<SpaceObject> toSatisfy)
    {
        // Speichert den nächsten Nachbarn und die minimale Distanz zu ihm
        final SpaceObject[] nearest = new SpaceObject[1];

        // Durchläuft den Octree und sucht den nächsten Nachbarn
        getNearestNeighbor(root, spaceObject, nearest, Float.MAX_VALUE, maxDistance, toSatisfy);

        return nearest[0];
    }

    private void getNearestNeighbor(final Node node, final SpaceObject spaceObject, final SpaceObject[] nearest,
                                    float minDistance, final float maxDistance,
                                    final Predicate<SpaceObject> toSatisfy)
    {
        final Vector3 point = spaceObject.getMovementController().getPosition();

        // Prüft, ob der aktuelle Knoten einen näheren Nachbarn enthält
        for (final SpaceObject neighbor : node.spaceObjects)
        {
            //Test for predicate before expensive calculation
            if (!toSatisfy.test(neighbor))
                continue;

            final float distanceSquared  = point.distanceSq(neighbor.getMovementController().getPosition());
            if (distanceSquared < minDistance)
            {
                nearest[0] = neighbor;
                minDistance = distanceSquared;
            }
        }

        // Prüft, ob der aktuelle Knoten einen näheren Nachbarn in einem Unterknoten enthält
        for (final Node child : node.children)
        {
            if (child == null) continue;

            // Prüft, ob der Unterknoten den nächsten Nachbarn enthalten könnte und ob er sich im Suchradius befindet

            if (point.distanceSq(child.center) - child.halfWidth * child.halfWidth < minDistance &&
                    point.distanceSq(child.center) <= maxDistance)
            {
                getNearestNeighbor(child, spaceObject, nearest, minDistance, maxDistance, toSatisfy);
            }
        }
    }

}
