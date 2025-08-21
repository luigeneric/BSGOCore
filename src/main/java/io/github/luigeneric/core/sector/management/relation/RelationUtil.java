package io.github.luigeneric.core.sector.management.relation;

import io.github.luigeneric.core.spaceentities.SpaceObject;
import io.github.luigeneric.enums.Faction;
import io.github.luigeneric.enums.FactionGroup;
import io.github.luigeneric.templates.cards.RegulationCard;
import io.github.luigeneric.templates.cards.ShipAbilityCard;
import io.github.luigeneric.templates.utils.TargetBracketMode;

public final class RelationUtil
{
    private RelationUtil(){}

    public static Relation getRelation(final SpaceObject thisObject, final SpaceObject otherObject,
                                       final TargetBracketMode targetBracketMode)
    {
        if (thisObject.equals(otherObject))
        {
            return Relation.Self;
        }

        //if it's my missile or mine, it's a friend
        if (thisObject.spawnedBy(otherObject))
        {
            return Relation.Friend;
        }

        //neutral check
        if (otherObject.getFaction() == Faction.Neutral)
        {
            return Relation.Neutral;
        }
        if (thisObject.getFaction() == Faction.Neutral)
        {
            return Relation.Neutral;
        }

        if (targetBracketMode == TargetBracketMode.AllEnemy)
        {
            return Relation.Enemy;
        }

        if (otherObject.getFaction() == thisObject.getFaction() &&
                otherObject.getFactionGroup() == thisObject.getFactionGroup())
        {
            return Relation.Friend;
        }
        return Relation.Enemy;
    }

    /**
     * Checks if the two objects are hostile to each other while on the same faction
     * @param spaceObject "me"
     * @param other the one to check against
     * @param regulationCard the regulationCard active for the current Sector
     * @return true if the two objects are hostile while they have the same faction
     */
    public static boolean isHostileCongener(final SpaceObject spaceObject, final SpaceObject other, final RegulationCard regulationCard)
    {
        return isHostileCongener(spaceObject.getFaction(), spaceObject.getFactionGroup(), other.getFaction(), other.getFactionGroup(),
                regulationCard.getTargetBracketMode());
    }

    /**
     * Checks if the two objects are hostile to each other while on the same faction
     * @param thisFaction faction of object first
     * @param thisGroup group of object first
     * @param otherFaction faction of object second
     * @param otherGroup group of object second
     * @param targetBracketMode target bracked mode of the current sector inside it's regulation card
     * @return true if the two objects are hostile while they have the same faction
     */
    public static boolean isHostileCongener(final Faction thisFaction, final FactionGroup thisGroup,
                                     final Faction otherFaction, final FactionGroup otherGroup,
                                     final TargetBracketMode targetBracketMode)
    {
        return thisFaction == otherFaction &&
                targetBracketMode == TargetBracketMode.AllEnemy ||
                thisGroup != otherGroup;
    }


    /// TODO
    public static boolean targetTypeCheck(final RegulationCard regulationCard, final ShipAbilityCard shipAbilityCard, final SpaceObject target)
    {
        return false;
    }
}
