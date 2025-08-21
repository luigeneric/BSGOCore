package io.github.luigeneric.templates.npcbehaviour;


import io.github.luigeneric.templates.sectortemplates.spaceobjectttemplates.WeaponPlatformTemplate;

public class NpcBehaviourTemplates
{
    private NpcBehaviourTemplates(){}

    public static NpcBehaviourTemplate createTemplateForTier(final byte tier, final long lifeTimeSeconds, final boolean jumpOutIfInCombat,
                                                   final float speedZeroDistance) throws IllegalArgumentException
    {
        switch (tier)
        {
            case 1 ->
            {
                return new NpcBehaviourTemplate(1, 1000, 4000, lifeTimeSeconds, jumpOutIfInCombat, speedZeroDistance);
            }
            case 2 ->
            {
                return new NpcBehaviourTemplate(2, 1500, 4000, lifeTimeSeconds, jumpOutIfInCombat, speedZeroDistance);
            }
            case 3 ->
            {
                return new NpcBehaviourTemplate(3, 2000, 4000, lifeTimeSeconds, jumpOutIfInCombat, speedZeroDistance);
            }
            default -> throw new IllegalArgumentException("Tier " + tier + " not implemented");
        }
    }

    public static NpcBehaviourTemplate createOutpostTemplate()
    {
        return new NpcBehaviourTemplate(1, 3300, 3300, Float.MAX_VALUE, false, 0);
    }

    public static NpcBehaviourTemplate createPlatFormTemplate(final byte tier, WeaponPlatformTemplate weaponPlatformTemplate)
    {
        if (weaponPlatformTemplate.getMaximumAggroDistance() > 0)
        {
            return new NpcBehaviourTemplate(0, weaponPlatformTemplate.getAutoAggroDistance(), weaponPlatformTemplate.getMaximumAggroDistance(),
                    Float.MAX_VALUE, false, 0);
        }

        switch (tier)
        {
            case 1,2 ->
            {
                return new NpcBehaviourTemplate(1, 500, 2000,
                        Float.MAX_VALUE, false, 0);
            }
            case 3 ->
            {
                return new NpcBehaviourTemplate(1, 1000, 3000,
                        Float.MAX_VALUE, false, 0);
            }
        }
        throw new IllegalArgumentException("Tier not implemented for behaviour constructor: weaponPlatformTier " + tier);
    }
}
