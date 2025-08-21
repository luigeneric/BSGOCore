package io.github.luigeneric.core.player.skills;


import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.binaryreaderwriter.IProtocolWrite;
import io.github.luigeneric.core.gameplayalgorithms.ExperienceToLevelAlgo;
import io.github.luigeneric.core.gameplayalgorithms.MathVersionOriginal;
import io.github.luigeneric.core.player.subscribesystem.InfoPublisher;
import io.github.luigeneric.core.protocols.subscribe.InfoType;
import io.github.luigeneric.templates.cards.SkillCard;
import io.github.luigeneric.templates.catalogue.Catalogue;
import io.github.luigeneric.templates.utils.AbilityActionType;
import io.github.luigeneric.templates.utils.ObjectStat;
import io.github.luigeneric.templates.utils.ObjectStats;
import io.github.luigeneric.templates.utils.SkillGroup;
import jakarta.enterprise.inject.spi.CDI;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * This is first container for xp, spent-xp, level, skills
 */
public class SkillBook extends InfoPublisher<Short> implements IProtocolWrite
{
    private final Map<Integer, PlayerSkill> skills;
    private final AtomicLong experience;
    private final AtomicLong spentExperience;
    private final ExperienceToLevelAlgo experienceToLevelAlgo;
    private final ReadWriteLock readWriteLock;
    private final Catalogue catalogue;


    /**
     * SkillBook already there?
     * @param skills the skills
     */
    public SkillBook(final Map<Integer, PlayerSkill> skills, final long experience, final long spentExperience,
                     final ExperienceToLevelAlgo experienceToLevelAlgo, final long playerID)
    {
        super(InfoType.Level, playerID, experienceToLevelAlgo.getLevelBasedOnExp(experience));
        this.skills = skills;
        this.experience = new AtomicLong(experience);
        this.spentExperience = new AtomicLong(spentExperience);
        this.experienceToLevelAlgo = experienceToLevelAlgo;
        this.readWriteLock = new ReentrantReadWriteLock();
        this.catalogue = CDI.current().select(Catalogue.class).get();
        setupStarterSkills();
    }

    /**
     * Set up basic starter SkillBook
     */
    public SkillBook(final long playerID)
    {
        this(new HashMap<>(), 0, 0, new MathVersionOriginal(), playerID);
    }

    private void writeLock()
    {
        readWriteLock.writeLock().lock();
    }
    private void writeUnlock()
    {
        readWriteLock.writeLock().unlock();
    }
    private void readLock()
    {
        readWriteLock.readLock().lock();
    }
    private void readUnlock()
    {
        readWriteLock.readLock().unlock();
    }
    private void setupStarterSkills()
    {
        readWriteLock.writeLock().lock();
        try
        {
            int serverID = 0;
            final List<SkillCard> skillCards = catalogue.getAllSkilLCardsOfLevel((short) 0);
            for (final SkillCard skillCard : skillCards)
            {
                final PlayerSkill playerSkill = new PlayerSkill(serverID, skillCard.getCardGuid());
                this.skills.put(serverID, playerSkill);
                serverID++;
            }
        }
        finally
        {
            readWriteLock.writeLock().unlock();
        }
    }

    @Override
    public void write(final BgoProtocolWriter bw)
    {
        readWriteLock.readLock().lock();
        try
        {
            bw.writeLength(this.skills.size());
            for (final PlayerSkill skill : skills.values())
            {
                bw.writeDesc(skill);
            }
        }
        finally
        {
            readWriteLock.readLock().unlock();
        }
    }

    public long getExperience()
    {
        return experience.get();
    }

    public void setExperience(final long experience)
    {
        this.experience.set(experience);
    }

    public long getSpentExperience()
    {
        return spentExperience.get();
    }

    public long getFreeExperience()
    {
        return this.experience.get() - this.spentExperience.get();
    }

    public void setSpentExperience(final long spentExperience)
    {
        this.spentExperience.set(spentExperience);
    }

    public void addSpentExperience(final long experienceToSpent)
    {
        if (experienceToSpent < 0) throw new IllegalArgumentException("experience to spent is less than 0");
        this.spentExperience.addAndGet(experienceToSpent);
    }

    public boolean checkExperienceEnoughForNextLevel(final int skillID)
    {
        final PlayerSkill skill = this.skills.get(skillID);
        if (skill == null) return false;
        if (skill.isMaxLevel()) return false;


        return this.getFreeExperience() >= skill.upgradePrice();
    }
    public void upgradeSkill(final int skillID)
    {
        writeLock();
        try
        {
            final PlayerSkill skillToUpgrade = this.skills.get(skillID);
            if (skillToUpgrade.isMaxLevel())
                return;
            addSpentExperience(skillToUpgrade.upgradePrice());
            skillToUpgrade.upgradeSkill();
        }
        finally
        {
            writeUnlock();
        }
    }
    public void resetSkill(final int skillID)
    {
        writeLock();
        try
        {
            final PlayerSkill skill = this.skills.get(skillID);
            if (skill == null)
                return;
            final long skillHash = skill.getSkillCard().getHash();
            final List<SkillCard> skillCards = catalogue.getAllSkilLCardsOfLevel((short) 0);
            final Optional<SkillCard> optZero = skillCards.stream()
                    .filter(s -> s.getHash() == skillHash)
                    .findAny();
            if (optZero.isEmpty())
                return;
            final SkillCard zero = optZero.get();
            skill.injectSkillCard(zero.getCardGuid());
        }
        finally
        {
            writeUnlock();
        }
    }

    public void addExperience(final long experienceToAdd) throws IllegalArgumentException
    {
        if (experienceToAdd < 0) throw new IllegalArgumentException("experienceToAdd cannot be null!");
        //nothing more to add
        final long currentExperience = this.getExperience();
        if (currentExperience == Integer.MAX_VALUE) return;
        final long newExperience = currentExperience + experienceToAdd;

        //overflow
        if (newExperience < currentExperience)
        {
            this.setExperience(Integer.MAX_VALUE);
        }
        else
        {
            this.setExperience(newExperience);
        }
        final short newLevel = this.experienceToLevelAlgo.getLevelBasedOnExp(newExperience);
        this.set(newLevel);
    }

    public Map<Integer, PlayerSkill> getAllSkills()
    {
        return this.skills;
    }

    public ObjectStats mapToObjectStats(final AbilityActionType abilityActionType)
    {
        final ObjectStats rv = new ObjectStats();

        switch (abilityActionType)
        {
            case FireCannon, Flak, PointDefence ->
            {
                for (PlayerSkill skill : this.skills.values())
                {
                    if (!skill.isOfGroup(SkillGroup.Weapon)) continue;

                    this.setSkillForStat(skill, rv, ObjectStat.CannonCriticalOffense, ObjectStat.CriticalOffense);
                    this.setSkillForStat(skill, rv, ObjectStat.OptimalRange, ObjectStat.OptimalRange);
                    this.setSkillForStat(skill, rv, ObjectStat.Accuracy, ObjectStat.Accuracy);
                }
            }
            case FireMining ->
            {
                for (PlayerSkill skill : this.skills.values())
                {
                    if (!skill.isOfGroup(SkillGroup.Weapon)) continue;

                    this.setSkillForStat(skill, rv, ObjectStat.MiningCooldown, ObjectStat.Cooldown);
                    this.setSkillForStat(skill, rv, ObjectStat.DamageMining, ObjectStat.DamageLow);
                    this.setSkillForStat(skill, rv, ObjectStat.DamageMining, ObjectStat.DamageHigh);
                    this.setSkillForStat(skill, rv, ObjectStat.MiningAccuracy, ObjectStat.Accuracy);
                    this.setSkillForStat(skill, rv, ObjectStat.MiningArmorPiercing, ObjectStat.ArmorPiercing);
                }
            }
            case FireMissle ->
            {
                for (PlayerSkill skill : this.skills.values())
                {
                    if (!skill.isOfGroup(SkillGroup.Weapon)) continue;

                    this.setSkillForStat(skill, rv, ObjectStat.MissileCriticalOffense, ObjectStat.CriticalOffense);
                    this.setSkillForStat(skill, rv, ObjectStat.MissileCooldown, ObjectStat.Cooldown);
                    this.setSkillForStat(skill, rv, ObjectStat.MissileMaxRange, ObjectStat.MaxRange);
                    this.setSkillForStat(skill, rv, ObjectStat.MissileMaxRange, ObjectStat.MaxRange);
                }
            }
            case RestoreBuff ->
            {
                for (PlayerSkill skill : this.skills.values())
                {
                    if (!skill.isOfGroup(SkillGroup.Hull)) continue;

                    this.setSkillForStat(skill, rv, ObjectStat.RestorePowerPointCost, ObjectStat.PowerPointCost);
                    this.setSkillForStat(skill, rv, ObjectStat.RestoreCooldown, ObjectStat.Cooldown);
                }
            }
            case Buff ->
            {
                for (PlayerSkill skill : this.skills.values())
                {
                    if (!skill.isOfGroup(SkillGroup.Computer)) continue;
                    this.setSkillForStat(skill, rv, ObjectStat.BuffPowerPointCost, ObjectStat.PowerPointCost);
                    this.setSkillForStat(skill, rv, ObjectStat.BuffDuration, ObjectStat.Duration);
                    this.setSkillForStat(skill, rv, ObjectStat.BuffMaxRange, ObjectStat.MaxRange);
                }
            }
            case Debuff ->
            {
                for (PlayerSkill skill : this.skills.values())
                {
                    if (!skill.isOfGroup(SkillGroup.Computer)) continue;

                    this.setSkillForStat(skill, rv, ObjectStat.DebuffPowerPointCost, ObjectStat.PowerPointCost);
                    this.setSkillForStat(skill, rv, ObjectStat.DebuffCooldown, ObjectStat.Cooldown);
                }
            }
            case ResourceScan ->
            {
                for (PlayerSkill skill : this.skills.values())
                {
                    if (!skill.isOfGroup(SkillGroup.Computer)) continue;

                    this.setSkillForStat(skill, rv, ObjectStat.BuffMaxRange, ObjectStat.MaxRange);
                    this.setSkillForStat(skill, rv, ObjectStat.BuffPowerPointCost, ObjectStat.PowerPointCost);
                }
            }
            case Slide ->
            {
                for (PlayerSkill skill : this.skills.values())
                {
                    if (!skill.isOfGroup(SkillGroup.Engine)) continue;

                    this.setSkillForStat(skill, rv, ObjectStat.ManeuverCooldown, ObjectStat.Cooldown);
                    this.setSkillForStat(skill, rv, ObjectStat.BuffPowerPointCost, ObjectStat.PowerPointCost);
                }
            }
        }
        return rv;
    }

    public Optional<PlayerSkill> getSkillFor(final SkillGroup skillGroup, final ObjectStat stat)
    {
        return this.skills.values().stream()
                .filter(skill -> skill.isOfGroup(skillGroup))
                .filter(skill -> skill.getMultiplyBuff().containsStat(stat))
                .findFirst();
    }
    public void setSkillForStat(final PlayerSkill skill, final ObjectStats stats, final ObjectStat skillStat,
                                final ObjectStat overwriteStat)
    {
        if (skill.getMultiplyBuff().containsStat(skillStat))
        {
            final float penStrength = skill.getMultiplyBuff().getStat(skillStat);
            stats.setStat(overwriteStat, penStrength);
        }
    }

    public Optional<PlayerSkill> getSkillByHash(final long hash)
    {
        readLock();
        try
        {
            return getSkillByHashInternal(hash);
        }
        finally
        {
            readUnlock();
        }
    }

    private Optional<PlayerSkill> getSkillByHashInternal(final long hash)
    {
        return this.skills.values().stream()
                .filter(playerSkill -> playerSkill.getSkillCard().getHash() == hash)
                .findAny();
    }



    public boolean skillsForUpgradeSatisfied(final long[] skillHashes, final int systemLevel)
    {
        int requiredSkillLevel;
        if (systemLevel <= 2)
        {
            requiredSkillLevel = 0;
        }
        else if (systemLevel <= 4)
        {
            requiredSkillLevel = 1;
        }
        else if (systemLevel <= 6)
        {
            requiredSkillLevel = 2;
        }
        else if (systemLevel <= 8)
        {
            requiredSkillLevel = 3;
        }
        else if (systemLevel == 9)
        {
            requiredSkillLevel = 4;
        }
        else
        {
            requiredSkillLevel = 5;
        }

        readLock();
        try
        {
            for (final long skillHash : skillHashes)
            {
                final Optional<PlayerSkill> optionalPlayerSkill = getSkillByHashInternal(skillHash);
                if (optionalPlayerSkill.isPresent())
                {
                    if (optionalPlayerSkill.get().getSkillCard().getLevel() < requiredSkillLevel)
                    {
                        return false;
                    }
                }
            }
            return true;
        }
        finally
        {
            readUnlock();
        }
    }
}
