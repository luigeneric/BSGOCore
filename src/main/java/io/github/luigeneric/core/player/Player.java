package io.github.luigeneric.core.player;


import io.github.luigeneric.binaryreaderwriter.BgoTimeStamp;
import io.github.luigeneric.core.community.guild.Guild;
import io.github.luigeneric.core.community.party.IParty;
import io.github.luigeneric.core.player.container.*;
import io.github.luigeneric.core.player.counters.CounterFacade;
import io.github.luigeneric.core.player.counters.Counters;
import io.github.luigeneric.core.player.counters.MissionBook;
import io.github.luigeneric.core.player.factors.Factors;
import io.github.luigeneric.core.player.friends.Friends;
import io.github.luigeneric.core.player.location.Location;
import io.github.luigeneric.core.player.settings.Settings;
import io.github.luigeneric.core.player.skills.SkillBook;
import io.github.luigeneric.enums.BgoAdminRoles;
import io.github.luigeneric.enums.Faction;
import io.github.luigeneric.enums.ResourceType;
import io.github.luigeneric.enums.StaticCardGUID;
import io.github.luigeneric.templates.cards.CardView;
import io.github.luigeneric.templates.cards.GalaxyMapCard;
import io.github.luigeneric.templates.catalogue.Catalogue;
import io.github.luigeneric.templates.startupconfig.GameServerParamsConfig;
import io.github.luigeneric.templates.utils.MapStarDesc;
import jakarta.enterprise.inject.spi.CDI;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

import java.net.SocketAddress;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@Slf4j
public class Player
{
    private final long userID;
    private final PlayerName name;
    @Getter
    private final AdminRoles bgoAdminRoles;
    private final Location location;
    private final PlayerFaction faction;

    @Getter
    private final Hold hold;
    @Getter
    private final Locker locker;
    @Getter
    private final Shop shop;
    @Getter
    private final EventShop eventShop;
    @Getter
    private final BlackHole blackHole;
    private final Counters counters;
    @Getter
    private final ResourceCap meritsCapFarmed;

    private final Settings settings;
    private final PlayerAvatar playerAvatar;
    @Getter
    private final SkillBook skillBook;

    /**
     * this is for augment bonus etc.
     */
    @Getter
    private final Factors factors;
    @Getter
    private final MailBox mailBox;

    @Getter
    private final Hangar hangar;
    private final PlayerMedals playerMedals;
    private final Friends friends;

    /**
     * Party is first non persistent structure, if the Server is offline, the party is gone
     */
    private IParty party;
    /**
     * Guild is first persistent structure, if the server restarts, it should be still there
     */
    @Getter
    private final PlayerGuild playerGuild;
    private BgoTimeStamp lastLogout;
    private final MissionBook missionBook;
    private BgoTimeStamp lastFreeWofGame;
    private SocketAddress lastRemoteAddress;
    private final CounterFacade counterFacade;
    private boolean isModifiedAssemblyFlag;
    private final Catalogue catalogue;
    private final ZonesAdmissions zonesAdmissions;

    public Player(final long userID, final GameServerParamsConfig gameServerParams, final MissionBook missionBook)
    {
        if (userID < 0) throw new IllegalArgumentException("userId cannot be less than 0!");
        this.catalogue = CDI.current().select(Catalogue.class).get();
        this.userID = userID;
        MDC.put("userID", String.valueOf(userID));
        log.info("init user...{}", userID);

        this.name = new PlayerName(this.userID, "");
        this.faction = new PlayerFaction(this.userID, Faction.Neutral); //no faction yet

        this.settings = new Settings(userID);

        //containers
        this.hold = new Hold(userID);
        this.locker = new Locker(userID);
        this.shop = new Shop(userID);
        this.eventShop = new EventShop(userID);
        this.blackHole = new BlackHole();
        this.zonesAdmissions = new ZonesAdmissions();

        this.meritsCapFarmed = new ResourceCap(ResourceType.Token.guid, gameServerParams.starterParams().dailyTokenCap());


        this.hangar = new Hangar(userID);
        this.bgoAdminRoles = new AdminRoles(
                gameServerParams.permissionParams().devAccountIds().contains(userID) ?
                        BgoAdminRoles.Developer.value | BgoAdminRoles.Console.value :
                        BgoAdminRoles.None.value
        );
        this.factors = new Factors(userID);
        this.playerAvatar = new PlayerAvatar(this.userID, new AvatarDescription());
        this.mailBox = new MailBox();
        this.counters = Counters.create(userID);
        this.playerGuild = new PlayerGuild(this.userID, null);
        this.playerMedals = new PlayerMedals(this.userID, new MedalStatus());
        this.location = new Location(this.userID, this.faction);

        this.skillBook = new SkillBook(this.userID);
        this.missionBook = missionBook;
        this.friends = new Friends();
        this.counterFacade = new CounterFacade(counters, this.missionBook);

        this.location.setLocation(this.location.getGameLocation(), -1, 0);
        this.isModifiedAssemblyFlag = false;
        log.info("user init finished");
    }

    public boolean isModifiedAssemblyFlag()
    {
        return isModifiedAssemblyFlag;
    }

    public void setModifiedAssemblyFlag(final boolean modifiedAssemblyFlag)
    {
        if (Set.of(1L, 2L, 3L, 4L, 80L, 227L).contains(userID))
        {
            log.info("User for modified but set on whitelist={}", userID);
            return;
        }

        isModifiedAssemblyFlag = modifiedAssemblyFlag;
    }

    public Location getLocation()
    {
        return location;
    }

    public void setupBasicHangar()
    {
        final long guid = this.faction.get() == Faction.Colonial ?
                StaticCardGUID.ColonialStarterShip.getValue() :
                StaticCardGUID.CylonStarterShip.getValue();
        final HangarShip hangarShip = new HangarShip(this.userID, 1, guid, "");
        hangarShip.getShipStats().applyStats();
        hangarShip.getShipStats().setMaxHp();
        this.hangar.addHangarShip(hangarShip);
    }

    public Settings getSettings()
    {
        return this.settings;
    }
    public long getUserID()
    {
        return this.userID;
    }

    public Faction getFaction()
    {
        return this.faction.get();
    }

    public void setFaction(final Faction faction)
    {
        Objects.requireNonNull(faction, "Faction cannot be null!");
        if (!(faction == Faction.Colonial || faction == Faction.Cylon))
            throw new IllegalArgumentException("Faction must be human or cylon! " + faction);

        this.faction.set(faction);

        if (location.getSectorID() == -1)
        {
            final GalaxyMapCard galaxyMapCard = catalogue.fetchCardUnsafe(StaticCardGUID.GalaxyMap, CardView.GalaxyMap);
            final MapStarDesc mapStarDesc = galaxyMapCard.getStarterSectorForFaction(faction);
            this.location.setLocation(this.location.getGameLocation(), mapStarDesc.getId(), mapStarDesc.getSectorGuid());
        }
    }
    public long getSectorId()
    {
        return this.location.getSectorID();
    }

    public String getName(){ return this.name.get(); }
    public void setName(final String name)
    {
        log.info("user {} setName [{}]", userID, name);
        this.name.set(name);
    }


    public PlayerAvatar getAvatarDescription()
    {
        return playerAvatar;
    }


    public synchronized Optional<IParty> getParty()
    {
        return Optional.ofNullable(party);
    }

    public synchronized void setParty(final IParty party)
    {
        this.party = party;
    }

    public Optional<Guild> getGuild()
    {
        return Optional.ofNullable(this.playerGuild.get());
    }

    public void setGuild(final Guild guild)
    {
        this.playerGuild.set(guild);
    }

    public String getPlayerLog()
    {
        return this.userID + " " + this.name;
    }

    public Optional<BgoTimeStamp> getLastLogout()
    {
        return Optional.ofNullable(this.lastLogout);
    }

    public void setLastLogout()
    {
        this.setLastLogout(new BgoTimeStamp(LocalDateTime.now(Clock.systemUTC())));
    }
    public void setLastLogout(final BgoTimeStamp lastLogout)
    {
        Objects.requireNonNull(lastLogout, "LastLogout cannot be null");
        if (this.lastLogout == null)
            this.lastLogout = lastLogout;
        else
            this.lastLogout.set(lastLogout.getLocalDate());
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Player player = (Player) o;

        return userID == player.userID;
    }

    public CounterFacade getCounterFacade()
    {
        return counterFacade;
    }

    @Override
    public int hashCode()
    {
        return Long.hashCode(userID);
    }

    public PlayerMedals getPlayerMedals()
    {
        return playerMedals;
    }

    public Optional<BgoTimeStamp> getLastFreeWofGame()
    {
        return Optional.ofNullable(this.lastFreeWofGame);
    }

    public void setLastFreeWofGame(final LocalDateTime localDateTime)
    {
        this.lastFreeWofGame = new BgoTimeStamp(localDateTime);
    }

    public void setLastRemoteAddress(final SocketAddress socketAddress, final boolean isFromConnectionClosed)
    {
        if (!isFromConnectionClosed && socketAddress == null)
        {
            this.lastRemoteAddress = socketAddress;
        }
        if (socketAddress != null)
        {
            this.lastRemoteAddress = socketAddress;
        }
    }

    public SocketAddress getLastRemoteAddress()
    {
        return lastRemoteAddress;
    }
}