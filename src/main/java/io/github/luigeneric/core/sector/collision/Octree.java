package io.github.luigeneric.core.sector.collision;


import io.github.luigeneric.core.spaceentities.SpaceObject;
import io.github.luigeneric.enums.ManeuverType;
import io.github.luigeneric.linearalgebra.base.Vector3;
import io.github.luigeneric.linearalgebra.utility.Mathf;

import java.util.ArrayList;
import java.util.List;

public class Octree
{
    private static final int OCTREE_CONST = 8;
    private final Node head;

    // THIS IS FOR COLLISION DETECTION
    // Keep track of all ancestor object lists in first stack
    int depth = 0; // ’Depth == 0’ is invariant over calls
    final int MAX_DEPTH = 40;
    Node[] ancestorStack = new Node[MAX_DEPTH];


    public Octree(final Vector3 center, final float halfWidth, final int stopDepth)
    {
        //this.head = buildOctree(center, halfWidth, stopDepth);
        this.head = buildOctree(center, halfWidth, stopDepth);
    }

    private Node buildOctree(final Vector3 center, final float halfWidth, final int stopDepth)
    {
        if (stopDepth < 0) return null;

        final Node parentNode = new Node(stopDepth, center, halfWidth, new Node[OCTREE_CONST], new ArrayList<>());
        final Vector3 offset = new Vector3();
        final float step = halfWidth * 0.5f;
        for (int i = 0; i < OCTREE_CONST; i++)
        {
            offset.set(
                    ((i & 1) > 0 ? step : -step),
                    ((i & 2) > 0 ? step : -step),
                    ((i & 4) > 0 ? step : -step)
            );
            parentNode.children[i] = buildOctree(Vector3.add(center, offset), step, stopDepth - 1);
        }
        return parentNode;
    }


    public void insertObject(final NodeObject obj)
    {
        this.insertObject(this.head, obj);
    }
    private void insertObject(final Node pTree, final NodeObject obj)
    {
        int index = 0;
        boolean straddle = false;
        // Compute the octant number [0..7] the object sphere center is in
        // If straddling any of the dividing x, y, or z planes, exit directly
        for (int i = 0; i < 3; i++) {
            final float delta = obj.center.getIndex(i) - pTree.center.getIndex(i);
            if (Mathf.abs(delta) < pTree.halfWidth + obj.radius) {
                straddle = true;
                break;
            }
            if (delta > 0.0f) index |= (1 << i); // ZYX
        }
        if (!straddle && pTree.children[index] != null)
        {
            // Fully contained in existing child node; insert in that subtree
            insertObject(pTree.children[index], obj);
            //InsertObject(pTree->pChild[index], pObject);
        }
        else
        {
            // Straddling, or no child node to descend into, so
            // link object into linked list at this node
            //pObject->pNextObject = pTree->pObjList;
            //pTree->pObjList = pObject;
            pTree.spaceObjects.add(obj);
        }
    }

    public static boolean isStaticObject(final SpaceObject spaceObject)
    {
        return spaceObject.getMovementController().getCurrentManeuver().getManeuverType().equals(ManeuverType.Rest);
    }


    public void testAllCollisions()
    {
        testAllCollisions(this.head);
    }
    /**
     * Tests all objects that could possibly overlap due to cell ancestry and coexistence
     * in the same cell. Assumes objects exist in first single cell only, and fully inside it
     * @param node
     */
    private void testAllCollisions(final Node node)
    {
        // Check collision between all objects on this level and all
        // ancestor objects. The current level is included as its own
        // ancestor so all necessary pairwise tests are done
        ancestorStack[depth++] = node;
        for (int n = 0; n < depth; n++) {
            Node pA, pB;

            List<NodeObject> spaceObjects = ancestorStack[n].spaceObjects;
            int size = spaceObjects.size();

            for (int i = 0; i < size; i++)
            {
                NodeObject obj1 = spaceObjects.get(i);
                for (int j = i; j < size; j++)
                {
                    var obj2 = spaceObjects.get(j);
                    if (obj1 == obj2) continue;
                    testCollision(obj1, obj2);
                }
            }
        }
        // Recursively visit all existing children
        for (int i = 0; i < 8; i++)
        {
            if (node.children[i] != null)
            {
                testAllCollisions(node.children[i]);
            }
        }
        // Remove current node from ancestor stack before returning
        depth--;
    }
    private void testCollision(NodeObject obj1, NodeObject obj2)
    {

    }

    public void print()
    {
        print(head);
    }
    private void print(Node node)
    {
        StringBuilder sb = new StringBuilder();
        final int tmp = node.depth;
        sb.append("\t".repeat(Math.max(0, tmp)));
        sb.append("Node[").append(node.depth)
                .append("] ").append(node.center)
                .append(" halfWidth: ").append(node.halfWidth);
        System.out.println(sb);
        for (final Node child : node.children)
        {
            if (child != null)
                print(child);
        }
    }


    static class Node
    {
        private final int depth;
        private final Vector3 center;
        private final float halfWidth;
        private final Node[] children;
        private final List<NodeObject> spaceObjects;

        Node(final int depth, final Vector3 center, final float halfWidth, final Node[] children, final List<NodeObject> spaceObjects)
        {
            this.depth = depth;
            this.center = center;
            this.halfWidth = halfWidth;
            this.children = children;
            this.spaceObjects = spaceObjects;
        }


    }

    static class NodeObject
    {
        private final SpaceObject spaceObject;
        private final Vector3 center; //center pt for object
        private final float radius; // radius of object bounding sphere

        NodeObject(SpaceObject spaceObject, Vector3 center, float radius)
        {
            this.spaceObject = spaceObject;
            this.center = center;
            this.radius = radius;
        }
    }
}
