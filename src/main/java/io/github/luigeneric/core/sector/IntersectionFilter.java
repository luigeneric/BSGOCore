package io.github.luigeneric.core.sector;


import io.github.luigeneric.core.sector.management.relation.Relation;
import io.github.luigeneric.core.sector.management.relation.RelationUtil;
import io.github.luigeneric.core.spaceentities.PlayerShip;
import io.github.luigeneric.core.spaceentities.SpaceObject;
import io.github.luigeneric.enums.SpaceEntityType;
import io.github.luigeneric.templates.cards.RegulationCard;

public class IntersectionFilter
{
    private final RegulationCard regulationCard;

    public IntersectionFilter(final RegulationCard regulationCard)
    {
        this.regulationCard = regulationCard;
    }

    public boolean needsIntersectionTest(final SpaceObject first, final SpaceObject second)
    {
        //dont test against self
        if (first == second)
        {
            return false;
        }
        //if no collider, no test will be made
        if (first.getCollider() == null || second.getCollider() == null)
            return false;

        final SpaceEntityType firstType = first.getSpaceEntityType();
        final SpaceEntityType secondType = second.getSpaceEntityType();

        //missile tests not doing this here anymore
        /*
        if (firstType == SpaceEntityType.Missile || secondType == SpaceEntityType.Missile)
        {
            return false;
        }
         */

        //planetoidxplanetoid not needed
        if (firstType == SpaceEntityType.Planetoid && secondType == SpaceEntityType.Planetoid)
            return false;

        //planetoidXother -> test
        if (firstType == SpaceEntityType.Planetoid || secondType == SpaceEntityType.Planetoid)
        {
            final SpaceObject other = firstType == SpaceEntityType.Planetoid ? second : first;
            return planetoidXSpaceObjectNonPrimitive(other.getSpaceEntityType());
        }

        if (firstType == SpaceEntityType.WeaponPlatform && secondType == SpaceEntityType.WeaponPlatform)
            return false;

        if (first instanceof PlayerShip firstPl)
        {
            final boolean firstVisibilityFlag = firstPl.getPlayerVisibility().isVisible();
            if (!firstVisibilityFlag)
                return false;

            if (second instanceof PlayerShip secondPl)
            {
                return secondPl.getPlayerVisibility().isVisible();
            }
        }




        return true;
    }

    private boolean planetoidXSpaceObjectNonPrimitive(final SpaceEntityType other)
    {
        if (other == SpaceEntityType.Planetoid)
            return false;

        if (other == SpaceEntityType.Asteroid)
            return false;

        if (other == SpaceEntityType.WeaponPlatform)
            return false;

        if (other == SpaceEntityType.MiningShip)
            return false;

        return true;
    }

    /**
     * missile x planetoid -> T
     * missile x asteroid -> F
     * missile x ship && FactionM != FactionS
     * @param nonMissile
     * @return
     */
    public boolean testMissilePrimitive(final SpaceObject missile, final SpaceObject nonMissile)
    {
        final Relation relation = RelationUtil.getRelation(missile, nonMissile, regulationCard.getTargetBracketMode());
        return nonMissile.isVisible() && relation != Relation.Self && relation != Relation.Friend;
    }
}
