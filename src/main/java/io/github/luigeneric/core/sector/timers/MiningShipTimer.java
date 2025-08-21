package io.github.luigeneric.core.sector.timers;

import io.github.luigeneric.core.User;
import io.github.luigeneric.core.community.party.IParty;
import io.github.luigeneric.core.player.Player;
import io.github.luigeneric.core.protocols.ProtocolID;
import io.github.luigeneric.core.protocols.debug.DebugProtocol;
import io.github.luigeneric.core.sector.Sector;
import io.github.luigeneric.core.sector.Tick;
import io.github.luigeneric.core.sector.creation.SectorContext;
import io.github.luigeneric.core.sector.management.*;
import io.github.luigeneric.core.sector.management.lootsystem.LootDistributor;
import io.github.luigeneric.core.sector.management.lootsystem.loot.Loot;
import io.github.luigeneric.core.sector.management.lootsystem.loot.LootAssociations;
import io.github.luigeneric.core.spaceentities.MiningShip;
import io.github.luigeneric.enums.*;
import io.github.luigeneric.templates.sectortemplates.MiningShipConfig;
import io.github.luigeneric.templates.sectortemplates.OutpostProgressTemplate;
import io.github.luigeneric.templates.shipitems.ItemCountable;

import java.util.*;
import java.util.stream.Collectors;

public class MiningShipTimer extends UpdateTimer
{
    private final SectorContext ctx;
    private final MiningShipConfig miningShipConfig;
    private final GalaxyBonus galaxyBonus;
    private final LootDistributor lootDistributor;
    private final ISpaceObjectRemover remover;

    public MiningShipTimer(final SectorContext ctx, final MiningShipConfig miningShipConfig,
                           final GalaxyBonus galaxyBonus, final LootDistributor lootDistributor,
                           final ISpaceObjectRemover remover)
    {
        super(ctx.spaceObjects());
        this.ctx = ctx;
        this.miningShipConfig = miningShipConfig;
        this.galaxyBonus = galaxyBonus;
        this.lootDistributor = lootDistributor;
        this.remover = remover;
    }

    @Override
    public void update(final float dt)
    {
        final List<MiningShip> ships = this.sectorSpaceObjects.getSpaceObjectsOfEntityType(SpaceEntityType.MiningShip);
        final long currentTickTimeStamp = ctx.tick().getTimeStamp();

        for (final MiningShip miningShip : ships)
        {
            handleMiningShipUpdate(miningShip, currentTickTimeStamp);
        }
    }
    private void handleMiningShipUpdate(final MiningShip miningShip, final long currentTickTimeStamp)
    {
        //Check if mining delay is already over
        final boolean isDelayOver = checkIsMiningDelayOver(miningShip, currentTickTimeStamp);
        if (!isDelayOver)
            return;

        //check if owner is still ingame
        final User owner = miningShip.getOwner();
        if (owner.getPlayer().getFaction() != miningShip.getFaction())
        {
            remover.notifyRemovingCauseAdded(miningShip, RemovingCause.JumpOut);
            remover.notifyRemovingCauseAdded(miningShip.getAttachedToPlanetoid(), RemovingCause.Death);
            return;
        }

        //UPDATE MINING
        long income = (long) miningShipConfig.extractPerSecond() * miningShipConfig.extractDelay();
        final float mapBonus = miningShip.getFaction().equals(Faction.Colonial) ?
                galaxyBonus.getColoMiningBonus() : galaxyBonus.getCyloMiningBonus();
        income = income + (long) ((float)income * mapBonus);


        miningShip.setLastTimeMining(currentTickTimeStamp);

        final MiningUsersOfInterest miningUsersOfInterest = findUsersOfInterest(owner);

        //adjust income based on party
        income = calculateIncome(miningUsersOfInterest, income);

        //remove Resources from Planetoid
        final Optional<Loot> loot = ctx.lootAssociations().get(miningShip.getAttachedToPlanetoid());
        if (loot.isEmpty())
        {
            final DebugProtocol debugProtocol = owner.getProtocol(ProtocolID.Debug);
            debugProtocol.sendEzMsg("ERROR: MiningShipTimer loot missing");
            return;
        }
        final Loot existingLoot = loot.get();
        final ItemCountable resource = (ItemCountable) existingLoot.getLootItems().getFirst().shipItem();
        final long tyliumTitaniumMultiplier = resource.getCardGuid() == ResourceType.Water.guid ? 1 : 2;
        income = income * tyliumTitaniumMultiplier;
        final long farmed = (resource.getCount() - income) > 0 ? income : resource.getCount();
        resource.decrementCount(farmed);

        //give ressources either to full party or to owner only
        //provideUsersOfInterestsWithIncome(miningUsersOfInterest, farmed);
        for (MiningPartyMember miningPartyMember : miningUsersOfInterest.getAllPartyMembers())
        {
            final ItemCountable miningReward = ItemCountable.fromGUID(resource.getCardGuid(), farmed / miningUsersOfInterest.fullInterestCount());
            lootDistributor.oreMined(miningPartyMember.member(), miningReward);
        }

        //if no more Resources, remove Planetoid and miningship
        if (farmed < income)
        {
            remover.notifyRemovingCauseAdded(miningShip, RemovingCause.JumpOut);
            remover.notifyRemovingCauseAdded(miningShip.getAttachedToPlanetoid(), RemovingCause.Death);
        }
        this.updateOpProgressCount(miningShip.getFaction());
    }


    private void provideUsersOfInterestsWithIncome(final MiningUsersOfInterest miningUsersOfInterest, final long farmedArg, final ItemCountable resource)
    {
        final long adjustedFarmed = farmedArg / miningUsersOfInterest.fullInterestCount();
        for (final MiningPartyMember partyMember : miningUsersOfInterest.getAllPartyMembers())
        {
            final ItemCountable miningReward = ItemCountable.fromGUID(resource.getCardGuid(), adjustedFarmed);
            lootDistributor.oreMined(partyMember.member, miningReward);
        }
    }

    private long calculateIncome(final MiningUsersOfInterest miningUsersOfInterest, final long oldIncome)
    {
        if (!miningUsersOfInterest.hasParty())
            return oldIncome;

        return oldIncome * miningUsersOfInterest.fullInterestCount();
    }

    private boolean checkIsMiningDelayOver(final MiningShip miningShip, final long currentTickTimeStamp)
    {
        final long lastTimeTimeStamp = miningShip.getLastTimeMining();
        final long delayInMs = miningShipConfig.extractDelay() * 1000L;


        final long diff = currentTickTimeStamp - (lastTimeTimeStamp + delayInMs);
        return diff >= 0;
    }

    private MiningUsersOfInterest findUsersOfInterest(final User user)
    {
        final Player player = user.getPlayer();
        final Optional<IParty> optParty = player.getParty();
        if (optParty.isEmpty())
        {
            return new MiningUsersOfInterest(MiningPartyMember.createOwner(user));
        }
        final IParty party = optParty.get();
        final Collection<User> memberCopy = party.getMembersCopy();
        final Set<MiningPartyMember> membersOfInterest = memberCopy
                .stream()
                //user has to be online
                .filter(User::isConnected)
                //user has to be not the owner
                .filter(partyMember -> !partyMember.equals(user))
                //user has to be in sector
                .filter(partyMember -> partyMember.getPlayer().getLocation().getGameLocation() == GameLocation.Space)
                .map(member -> MiningPartyMember.createMember(member, member.getPlayer().getLocation().getSectorID()))
                .collect(Collectors.toSet());


        return new MiningUsersOfInterest(MiningPartyMember.createOwner(user), membersOfInterest);
    }

    private void updateOpProgressCount(final Faction faction)
    {
        var outPostStates = ctx.outPostStates();
        final OutpostState opState = faction.equals(Faction.Colonial) ?
                outPostStates.colonialOutpostState() : outPostStates.cylonOutpostState();
        final OutpostProgressTemplate progressTemplate = outPostStates.getTemplateFromFaction(faction);
        final boolean isBlocked = opState.increasePoints(progressTemplate.ptsMiningShipIncome());
    }


    private record MiningPartyMember(User member, boolean isOwner, long sectorId)
    {
        public static MiningPartyMember createOwner(final User owner)
        {
            return new MiningPartyMember(owner, true, owner.getPlayer().getSectorId());
        }
        public static MiningPartyMember createMember(final User member, final long sectorId)
        {
            return new MiningPartyMember(member, false, sectorId);
        }
    }
    record MiningUsersOfInterest(MiningPartyMember owner, Set<MiningPartyMember> partyMembers)
    {
        MiningUsersOfInterest(MiningPartyMember owner)
        {
            this(owner, Set.of());
        }

        public boolean hasParty()
        {
            return !partyMembers.isEmpty();
        }

        public long fullInterestCount()
        {
            return this.partyMembers.size() + 1;
        }
        public Collection<MiningPartyMember> getAllPartyMembers()
        {
            final List<MiningPartyMember> fullMembers = new ArrayList<>();
            fullMembers.add(owner);
            fullMembers.addAll(this.partyMembers);
            return fullMembers;

        }
    }
}
