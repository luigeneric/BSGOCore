package io.github.luigeneric.core.galaxy;

import io.github.luigeneric.core.User;
import io.github.luigeneric.core.UsersContainer;
import io.github.luigeneric.core.player.factors.Factor;
import io.github.luigeneric.core.player.factors.Factors;
import io.github.luigeneric.core.protocols.ProtocolID;
import io.github.luigeneric.core.protocols.player.PlayerProtocol;
import io.github.luigeneric.core.protocols.player.PlayerProtocolWriteOnly;
import io.github.luigeneric.enums.Faction;
import io.github.luigeneric.enums.FactorSource;
import io.github.luigeneric.enums.FactorType;
import io.github.luigeneric.linearalgebra.utility.Mathf;
import io.github.luigeneric.utils.BgoRandom;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
public class EloBalancer implements FactionBalancer
{
    private final UsersContainer usersContainer;
    private long colonialCounter;
    private long cylonCounter;

    private final List<Factor> lastColonialFactors;
    private final List<Factor> lastCylonFactors;
    private final TimeUnit timeUnit;
    private final long duration;
    private final PlayerProtocolWriteOnly writer;

    public EloBalancer(final UsersContainer usersContainer, final TimeUnit timeUnit, final long duration)
    {
        this.usersContainer = usersContainer;
        this.writer = new PlayerProtocolWriteOnly();
        this.colonialCounter = 0;
        this.cylonCounter = 0;
        this.duration = duration;
        this.timeUnit = timeUnit;

        this.lastColonialFactors = new ArrayList<>();
        this.lastCylonFactors = new ArrayList<>();
    }

    @Override
    public void setCountFaction(final Faction faction, final long count)
    {
        if (count < 0) throw new IllegalArgumentException("Count cannot be less than 0!");

        if (faction == Faction.Colonial)
        {
            this.colonialCounter = count;
        }
        else if (faction == Faction.Cylon)
        {
            this.cylonCounter = count;
        }
        else
        {
            throw new IllegalArgumentException("Faction is not colonial or cylon! " + faction);
        }
    }

    @Override
    public float getFactionBonus(final Faction forFaction)
    {
        final long first = forFaction.equals(Faction.Colonial) ? this.colonialCounter : this.cylonCounter;
        final long second = forFaction.equals(Faction.Cylon) ? this.colonialCounter : this.cylonCounter;
        final float elo = eloCalc(first, second);

        final float clampedValue = Mathf.clamp((0.5f - elo) * 2.f, 0, 1);

        BigDecimal bd = new BigDecimal(clampedValue).setScale(2, RoundingMode.HALF_UP);
        return bd.floatValue();
    }

    @Override
    public long time()
    {
        return this.duration;
    }

    @Override
    public TimeUnit timeUnit()
    {
        return this.timeUnit;
    }

    private float eloCalc(final long playerOneRating, final long playerTwoRating)
    {
        return (float) (1 / (1 + Math.pow(10, (playerTwoRating - playerOneRating) / 100.)));
    }

    //@Scheduled
    public void run()
    {
        final List<User> colonials = this.usersContainer
                .userList(user -> user.getPlayer().getFaction() == Faction.Colonial && user.isConnected());
        final List<User> cylons = this.usersContainer
                .userList(user -> user.getPlayer().getFaction() == Faction.Cylon && user.isConnected());
        this.setCountFaction(Faction.Colonial, colonials.size());
        this.setCountFaction(Faction.Cylon, cylons.size());

        final float colonialBonus = getFactionBonus(Faction.Colonial);
        final float cylonBonus = getFactionBonus(Faction.Cylon);

        removeOldFactors();

        var rnd = new BgoRandom();
        var rndBoni = rnd.getRndBetween(0.1, 2);
        log.info("RndBoni " + rndBoni);
        setupBonusForFaction(Faction.Colonial, colonialBonus);
        setupBonusForFaction(Faction.Cylon, (float) rndBoni);

        addNewFactors(colonials, this.lastColonialFactors);
        addNewFactors(cylons, this.lastCylonFactors);

        log.info("FactionBalancer updated for " + colonials.size() + " colonials, " + colonialBonus + " and "
                + cylons.size() + " cylons, " + cylonBonus);
        removeOldFactors();
    }

    private void removeOldFactors()
    {
        final LocalDateTime now = LocalDateTime.now(Clock.systemUTC());
        for (final User user : this.usersContainer.values())
        {
            final Factors factors = user.getPlayer().getFactors();
            final List<Factor> ofSource = factors.getItemsOfSource(FactorSource.Faction);
            ofSource.forEach(factor ->
            {
                if (factor.getEndTime().isBefore(now))
                    factors.removeItem(factor.getServerID());
            });
            final Set<Integer> idsRemoved = ofSource.stream().map(Factor::getServerID)
                    .collect(Collectors.toSet());
            user.send(writer.writeRemoveFactorIds(idsRemoved));
        }
    }

    private void setupBonusForFaction(final Faction faction, final float bonus)
    {
        final List<Factor> lstToUse = faction == Faction.Colonial ? this.lastColonialFactors : this.lastCylonFactors;
        lstToUse.clear();

        if (bonus <= 0)
            return;

        final LocalDateTime now = LocalDateTime.now(Clock.systemUTC());
        final Factor loot = Factor
                .fromTimeUnit(FactorType.Loot, FactorSource.Faction, bonus, now, this.timeUnit, this.duration);
        final Factor asteroidYield = Factor
                .fromTimeUnit(FactorType.AsteroidYield, FactorSource.Faction, bonus, now, this.timeUnit, this.duration);


        lstToUse.add(loot);
        lstToUse.add(asteroidYield);
    }

    private void addNewFactors(final List<User> users, final List<Factor> factorsToAdd)
    {
        if (factorsToAdd.isEmpty())
            return;

        for (final User user : users)
        {
            final Factors factors = user.getPlayer().getFactors();
            factorsToAdd.forEach(factors::addFactor);

            final PlayerProtocol playerProtocol = user.getProtocol(ProtocolID.Player);
            user.send(playerProtocol.writer().writeFactors(factors));
        }
    }
}
