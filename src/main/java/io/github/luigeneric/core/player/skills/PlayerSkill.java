package io.github.luigeneric.core.player.skills;

import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.binaryreaderwriter.IProtocolWrite;
import io.github.luigeneric.templates.cards.CardView;
import io.github.luigeneric.templates.cards.SkillCard;
import io.github.luigeneric.templates.catalogue.Catalogue;
import io.github.luigeneric.templates.utils.ObjectStats;
import io.github.luigeneric.templates.utils.SkillGroup;
import io.github.luigeneric.utils.AutoLock;
import io.github.luigeneric.utils.collections.IServerItem;
import jakarta.enterprise.inject.spi.CDI;

import java.util.Optional;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class PlayerSkill implements IProtocolWrite, Comparable<PlayerSkill>, IServerItem
{
    private int serverID;
    private SkillCard skillCard;
    private SkillCard nextSkillCard;
    private final Catalogue catalogue;
    private final Lock lock;

    public PlayerSkill(final int serverID, final long skillCardGuid)
    {
        this.catalogue = CDI.current().select(Catalogue.class).get();
        this.serverID = serverID;
        this.lock = new ReentrantLock();
        this.setupSkillCardFromGuid(skillCardGuid);
    }

    @Override
    public void write(final BgoProtocolWriter bw)
    {
        try(var l = new AutoLock(lock))
        {
            bw.writeUInt16(this.serverID);
            bw.writeGUID(this.skillCard.getCardGuid());
        }
    }

    public void upgradeSkill()
    {
        try(var l = new AutoLock(lock))
        {
            if (skillCard.getLevel() < skillCard.getMaxLevel())
            {
                injectSkillCard(skillCard.getNextSkillCardGuid());
            }
        }
    }

    private void setupSkillCardFromGuid(final long guid)
    {
        final Optional<SkillCard> optionalSkillCard = catalogue.fetchCard(guid, CardView.Skill);
        if (optionalSkillCard.isEmpty())
        {
            return;
        }
        this.skillCard = optionalSkillCard.get();
        if (this.skillCard.getNextSkillCardGuid() == 0)
        {
            return;
        }
        final Optional<SkillCard> optNextSkilLCard = catalogue.fetchCard(this.skillCard.getNextSkillCardGuid(), CardView.Skill);
        if (optNextSkilLCard.isEmpty())
            return;
        this.nextSkillCard = optNextSkilLCard.get();
    }

    public void injectSkillCard(final long guid)
    {
        try(var l = new AutoLock(lock))
        {
            setupSkillCardFromGuid(guid);
        }
    }

    public boolean isOfGroup(final SkillGroup skillGroup)
    {
        return this.skillCard.getSkillGroup() == skillGroup;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PlayerSkill that = (PlayerSkill) o;
        return this.serverID == that.serverID;
    }


    public boolean isMaxLevel()
    {
        return this.skillCard.getNextSkillCardGuid() == 0;
    }
    public int upgradePrice() throws IllegalCallerException
    {
        if (this.isMaxLevel())
            throw new IllegalCallerException("This is maxlevel already!");
        return this.nextSkillCard.getPrice();
    }

    @Override
    public int hashCode()
    {
        return this.serverID;
    }

    @Override
    public int compareTo(PlayerSkill o)
    {
        return this.skillCard.getSortWeight() - o.skillCard.getSortWeight();
    }

    public ObjectStats getStaticBuff()
    {
        return this.skillCard.getStaticBuff();
    }
    public ObjectStats getMultiplyBuff()
    {
        return this.skillCard.getMultiplyBuff();
    }

    @Override
    public int getServerID()
    {
        return this.serverID;
    }

    @Override
    public void setServerID(int serverID)
    {
        this.serverID = serverID;
    }

    public SkillCard getSkillCard()
    {
        return skillCard;
    }

    @Override
    public String toString()
    {
        return "PlayerSkill{" +
                "serverID=" + serverID +
                ", skillCard=" + skillCard +
                '}';
    }
}
