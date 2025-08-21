package io.github.luigeneric.core.spaceentities.statsinfo.stats;


import io.github.luigeneric.core.player.skills.PlayerSkill;
import io.github.luigeneric.core.player.skills.SkillBook;
import io.github.luigeneric.core.spaceentities.statsinfo.buffer.BasePropertyBuffer;
import io.github.luigeneric.templates.utils.ObjectStats;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

public class PlayerSubscribeInfo extends ShipSubscribeInfo
{
    protected SkillBook skillBook;

    public PlayerSubscribeInfo(Owner owner, ObjectStats objectStats, float hullPoints, float powerPoints,
                               ShipModifiers shipModifiers, CombatInfo combatInfo, AtomicLong targetObjectID)
    {
        super(owner, objectStats, hullPoints, powerPoints, shipModifiers, combatInfo, targetObjectID);
    }

    public PlayerSubscribeInfo(final long ownerID, ObjectStats stats)
    {
        this(new Owner(ownerID, true), stats, 1f, 1f,
                new ShipModifiers(), new CombatInfo(), new AtomicLong(0));
    }

    @Override
    public Optional<SkillBook> getSkillBook()
    {
        return Optional.ofNullable(skillBook);
    }

    @Override
    public void setSkillBook(final SkillBook skillBook)
    {
        lock.lock();
        try
        {
            this.skillBook = skillBook;
        }
        finally
        {
            lock.unlock();
        }
        this.applyStats();
    }

    @Override
    public void applyStats()
    {
        lock.lock();
        try
        {
            //slots + modifiers
            this.resetStats();
            this.applySlotSystemStats();    //slots
            this.applyAbilityStats();
            this.statsFinal.put(this.statsWithSlots);
            this.applyModifiers();

            //slots + modifiers + skill
            this.applySkills();
            for (final BasePropertyBuffer sub : this.subscribers.values())
            {
                sub.onStatInfoChanged(this, StatInfo.Stats);
            }
            this.getMovementUpdateSubscriber().ifPresent(ud -> ud.setMovementOptionsStats(this.getStats()));
        }
        finally
        {
            lock.unlock();
        }
    }


    private void applySkills()
    {
        //base + skill without buffs?
        final Optional<SkillBook> optSkillBook = this.getSkillBook();
        if (optSkillBook.isEmpty()) return;
        final SkillBook skills = optSkillBook.get();
        for (PlayerSkill skill : skills.getAllSkills().values())
        {
            final ObjectStats staticBuffs = skill.getStaticBuff();
            final ObjectStats multBuffs = skill.getMultiplyBuff();
            ObjectStats.applyStatsAddTo(staticBuffs, this.statsFinal);

            final ObjectStats addBonus = ObjectStats.getStatsMultiplyBonus(this.statsWithSlots, multBuffs);
            ObjectStats.applyStatsAddTo(addBonus, this.statsFinal);
        }
    }


}
