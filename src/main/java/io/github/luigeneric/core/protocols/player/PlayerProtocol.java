package io.github.luigeneric.core.protocols.player;

import io.github.luigeneric.MicrometerRegistry;
import io.github.luigeneric.binaryreaderwriter.BgoProtocolReader;
import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.core.ProtocolContext;
import io.github.luigeneric.core.User;
import io.github.luigeneric.core.UsersContainer;
import io.github.luigeneric.core.community.guild.Guild;
import io.github.luigeneric.core.community.guild.GuildInfoMessage;
import io.github.luigeneric.core.community.guild.GuildRegistry;
import io.github.luigeneric.core.community.party.IParty;
import io.github.luigeneric.core.database.DbProvider;
import io.github.luigeneric.core.dradisverification.DradisData;
import io.github.luigeneric.core.gameplayalgorithms.ExperienceToLevelAlgo;
import io.github.luigeneric.core.player.*;
import io.github.luigeneric.core.player.container.*;
import io.github.luigeneric.core.player.container.containerids.MailContainerID;
import io.github.luigeneric.core.player.container.visitors.*;
import io.github.luigeneric.core.player.counters.Mission;
import io.github.luigeneric.core.player.counters.MissionBook;
import io.github.luigeneric.core.player.factors.Factor;
import io.github.luigeneric.core.player.factors.Factors;
import io.github.luigeneric.core.player.location.AvatarLocation;
import io.github.luigeneric.core.player.location.CICLocation;
import io.github.luigeneric.core.player.location.Location;
import io.github.luigeneric.core.player.skills.SkillBook;
import io.github.luigeneric.core.protocols.BgoProtocol;
import io.github.luigeneric.core.protocols.ProtocolID;
import io.github.luigeneric.core.protocols.ProtocolMessageHandler;
import io.github.luigeneric.core.protocols.ProtocolRegistryWriteOnly;
import io.github.luigeneric.core.protocols.community.CommunityProtocol;
import io.github.luigeneric.core.protocols.debug.DebugProtocol;
import io.github.luigeneric.core.protocols.notification.NotificationProtocol;
import io.github.luigeneric.core.protocols.notification.NotificationProtocolWriteOnly;
import io.github.luigeneric.core.protocols.player.handlers.ChangeFactionHandler;
import io.github.luigeneric.core.protocols.player.handlers.ReadMailHandler;
import io.github.luigeneric.core.protocols.player.handlers.SendDradisDataHandler;
import io.github.luigeneric.core.protocols.scene.SceneProtocol;
import io.github.luigeneric.core.protocols.setting.SettingProtocol;
import io.github.luigeneric.core.spaceentities.bindings.StickerBinding;
import io.github.luigeneric.core.spaceentities.statsinfo.buffer.BasePropertyBuffer;
import io.github.luigeneric.core.spaceentities.statsinfo.stats.SpaceSubscribeInfo;
import io.github.luigeneric.core.spaceentities.statsinfo.stats.StatsProtocolSubscriber;
import io.github.luigeneric.enums.*;
import io.github.luigeneric.templates.augments.AugmentFactorTemplate;
import io.github.luigeneric.templates.augments.AugmentLootItemTemplate;
import io.github.luigeneric.templates.augments.AugmentTemplate;
import io.github.luigeneric.templates.augments.AugmentTemplates;
import io.github.luigeneric.templates.cards.*;
import io.github.luigeneric.templates.catalogue.Catalogue;
import io.github.luigeneric.templates.catalogue.MissionCardsFetchResult;
import io.github.luigeneric.templates.loot.LootEntryInfo;
import io.github.luigeneric.templates.shipitems.ItemCountable;
import io.github.luigeneric.templates.shipitems.ItemType;
import io.github.luigeneric.templates.shipitems.ShipItem;
import io.github.luigeneric.templates.shipitems.ShipSystem;
import io.github.luigeneric.templates.utils.ObjectStat;
import io.github.luigeneric.templates.utils.ObjectStats;
import io.github.luigeneric.templates.utils.Price;
import jakarta.enterprise.inject.spi.CDI;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Slf4j
public class PlayerProtocol extends BgoProtocol implements StatsProtocolSubscriber
{
    private final EnumMap<ClientMessage, ProtocolMessageHandler> handlers;
    private final ExperienceToLevelAlgo experienceToLevelAlgo;
    private final DbProvider dbProviderProvider;
    private final DradisData dradisData;
    private final UsersContainer usersContainer;
    private final GuildRegistry guildRegistry;
    private final PlayerProtocolWriteOnly writer;
    private CharacterServices characterServices;
    public static final int REPAIR_DOCK_DELAY_TIME_SECONDS = 30;
    private final Catalogue catalogue;
    private final MicrometerRegistry micrometerRegistry;


    public PlayerProtocol(final ProtocolContext ctx,
                          final DbProvider dbProvider,
                          final ExperienceToLevelAlgo experienceToLevelAlgo,
                          final UsersContainer usersContainer,
                          final GuildRegistry guildRegistry,
                          final CharacterServices characterServices
    )
    {
        super(ProtocolID.Player, ctx);
        this.handlers = new EnumMap<>(ClientMessage.class);
        this.catalogue = ctx.catalogue();
        this.micrometerRegistry = ctx.micrometerRegistry();
        this.experienceToLevelAlgo = experienceToLevelAlgo;
        this.dbProviderProvider = dbProvider;
        this.dradisData = new DradisData();
        this.usersContainer = usersContainer;
        this.guildRegistry = guildRegistry;
        this.writer = ProtocolRegistryWriteOnly.getProtocol(ProtocolID.Player);
        this.characterServices = characterServices;
    }

    @Override
    protected void setupHandlers()
    {
        this.handlers.put(ClientMessage.ChangeFaction, new ChangeFactionHandler(user(), characterServices, catalogue, ctx.rng()));
        this.handlers.put(ClientMessage.ReadMail, new ReadMailHandler(user(), writer));
        this.handlers.put(ClientMessage.SendDradisData, new SendDradisDataHandler(user(), dradisData));
    }

    @Override
    public void injectUser(User user)
    {
        super.injectUser(user);
    }

    public final PlayerProtocolWriteOnly writer()
    {
        return writer;
    }

    public void parseMessage(final int msgType, final BgoProtocolReader br) throws IOException
    {
        final ClientMessage clientMessage = ClientMessage.valueOf(msgType);
        final SceneProtocol sceneProtocol = user().getProtocol(ProtocolID.Scene);
        final ProtocolMessageHandler handler = handlers.get(clientMessage);
        if (handler != null)
        {
            handler.handle(br);
            return;
        }
        switch (clientMessage)
        {
            case PopupSeen ->
            {
                final long popupId = br.readUint32();
                log.info("PopupSeen noticed popupId " + popupId + " " + user().getUserLog());
            }
            case SelectFaction ->
            {
                final Faction faction = Faction.valueOf(br.readByte());
                log.info(user().getUserLog() + " SelectFaction " + faction);
                if (faction != Faction.Colonial && faction != Faction.Cylon)
                {
                    user().setConnection(null);
                    return;
                }


                final Player player = this.user().getPlayer();
                if (player.getLocation().getGameLocation() == GameLocation.Starter)
                {
                    player.setFaction(faction);
                    player.setupBasicHangar();
                    user().send(writer.writeFaction(faction));
                    player.getLocation().changeState(new AvatarLocation(player.getLocation()));
                    sceneProtocol.sendLoadNextScene();
                    /*
                    PrometheusMetrics.INSTANCE.getPlayersOnline()
                            .labels(player.getFaction().name())
                            .inc();
                     */
                }
                else
                {
                    ///TODO ban?
                    log.warn(user().getUserLog() +" CHEAT! send faction while not in Starter! current:"+ player.getLocation().getGameLocation());
                }
            }
            case SelectTitle ->
            {
                //need to select duty by id ... so not by guid
                final int dutyId = br.readUint16();

            }
            case CheckNameAvailability ->
            {
                final String requestedName = br.readString();
                log.info(user().getUserLog() + " Name-Request for Name: " + requestedName);
                final boolean isFree = usersContainer.checkNameFree(requestedName, this.user().getPlayer().getUserID());
                user().send(writeNameAvailability(isFree));
            }
            case ChooseName ->
            {
                final String nameChosen = br.readString();
                final Player player = this.user().getPlayer();
                if (!player.getName().isEmpty())
                {
                    log.error(user().getUserLog() + " Cheat Player tried to change name even though the name was already set! " + nameChosen);
                    return;
                }
                final boolean isPresent = usersContainer.chooseNameIfPresentInReservation(nameChosen, player.getUserID());

                if (isPresent)
                {
                    player.setName(nameChosen);
                    user().send(writer.writeName(nameChosen));
                }
            }
            case ChangeAvatar ->
            {
                final Player player = this.user().getPlayer();
                AvatarDescription avatarDescription = new AvatarDescription();
                avatarDescription.read(br);
                player.getAvatarDescription().set(avatarDescription);
                user().send(writer.writeAvatarDescription(player.getAvatarDescription().get()));
                dbProviderProvider.updateAvatarDescription(player.getUserID(), avatarDescription);
            }
            case CreateAvatar ->
            {
                try
                {
                    final AvatarDescription avatarDescription = br.readDesc(AvatarDescription.class);
                    final Player player = this.user().getPlayer();


                    if (avatarDescription != null)
                    {
                        this.dbProviderProvider.updateAvatarDescription(player.getUserID(), avatarDescription);
                        player.getAvatarDescription().set(avatarDescription);
                    }

                    final SettingProtocol settingProtocol = user().getProtocol(ProtocolID.Setting);
                    //settingProtocol.writeDefaultBindings();
                    settingProtocol.sendSettings();


                    //send Avatar
                    user().send(writer.writeAvatarDescription(player.getAvatarDescription().get()));

                    //send playerId
                    user().send(writer.writeId(player.getUserID()));


                    //experience, spentXP, prev and nextLvLXP, Level
                    sendExperienceCollective();


                    //somehow I need to resend the faction because of race conditions I guess?
                    user().send(writer.writeFaction(player.getFaction()));

                    //sendPlayerShips
                    sendPlayerHangar();
                    user().send(writer.writeActivePlayerShip(player.getHangar().getActiveShip().getServerId()));
                    sendAllShipInfoDurability();
                    sendAllShipSlots();
                    sendAllShipNames();


                    //chatsession TODO

                    user().send(writer.writeSkills(player.getSkillBook()));

                    //factors test
                    final Factors factors = player.getFactors();
                    user().send(writer.writeFactors(factors));

                    //counters
                    player.getCounterFacade().counters().initAllUpdate();
                    user().send(writer.writeCounters(player.getCounterFacade().counters()));

                    //start resources
                    final ItemCountable startCubits =
                            ItemCountable.fromGUID(ResourceType.Cubits.guid, ctx.gameServerParams().starterParams().startCubits());
                    final ItemCountable startTylium =
                            ItemCountable.fromGUID(ResourceType.Tylium.guid, ctx.gameServerParams().starterParams().startTylium());
                    final ItemCountable startTitanium =
                            ItemCountable.fromGUID(ResourceType.Titanium.guid, ctx.gameServerParams().starterParams().startTitanium());
                    final ItemCountable startMerits =
                            ItemCountable.fromGUID(ResourceType.Token.guid, ctx.gameServerParams().starterParams().startToken());

                    player.getHold().addShipItem(startTylium);
                    player.getHold().addShipItem(startCubits);
                    player.getHold().addShipItem(startTitanium);
                    player.getHold().addShipItem(startMerits);

                    user().send(writer.writeAllContainerItems(player.getHold()));

                    if (ctx.gameServerParams().starterParams().testingMode())
                    {
                        final long exp = 10_000_000;
                        this.addExperience(exp);
                    }

                    //test with dradis mission reward
                    /*
                    var startTime = LocalDateTime.now(Clock.systemUTC()).toEpochSecond(ZoneOffset.UTC);
                    var endTime = startTime + 10;

                    List<ShipItem> missionItems = new ArrayList<>();
                    missionItems.add(itemCountable);
                    List<ShipItem> shipItems = new ArrayList<>();
                    shipItems.addAll(missionItems);
                    client.send(sendDradisMissionStatisticsRewards(startTime, endTime, 10,
                            9, 20, FtlRanks.Gold, shipItems));
                     */


                    //send the active hangarship
                    final HangarShip activeShip = player.getHangar().getActiveShip();
                    user().send(writeHangarShipStats(activeShip));

                    player.getLocation().changeState(new CICLocation(player.getLocation()));
                    sceneProtocol.sendLoadNextScene();
                } catch (InvocationTargetException | InstantiationException | IllegalAccessException |
                         NoSuchMethodException e)
                {
                    throw new RuntimeException(e);
                }


            }
            case RepairShip ->
            {
                final int shipId = br.readUint16();
                final float repairValue = br.readSingle();
                final boolean useCubits = br.readBoolean();

                //check if user is docked
                if (user().getPlayer().getLocation().getGameLocation() != GameLocation.Room)
                {
                    log.warn("Cheat {} cheats RepairShip, wrong location for repair process location={}",
                            user().getUserLog(), user().getPlayer().getLocation().getGameLocation());
                    return;
                }

                final DurabilityCostCalculator durabilityCostCalculator =
                        new DurabilityCostCalculator(catalogue.globalCard());
                final HangarShip hangarShipToRepair = user().getPlayer().getHangar().getByServerId(shipId);
                final float costs = durabilityCostCalculator.getShipHullRepairCosts(hangarShipToRepair, useCubits);

                final ResourceType resourceType = ResourceType.repairType(useCubits);

                final HoldVisitor holdVisitor = new HoldVisitor(user(), ctx.rng());
                final boolean reductionSuccessfully =
                        holdVisitor.reduceItemCountableByCount(resourceType, (long) costs);
                if (!reductionSuccessfully)
                {
                    log.warn(user().getUserLog() + " Reduction in RepairShip was not correct!");
                    return;
                }

                hangarShipToRepair.setDurabilityToMax();

                user().send(writer.writeShipInfoDurability(hangarShipToRepair));
                user().send(writer.writeShipSlots(hangarShipToRepair));
                hangarShipToRepair.getShipStats().setMaxHpPp();

                sendCapabilityUndockDelay(REPAIR_DOCK_DELAY_TIME_SECONDS);
            }
            case RepairSystem ->
            {
                MoveItemParser moveItemParser = new MoveItemParser(br, user().getPlayer());
                moveItemParser.parseRepairSystem();

                //check if user is docked
                if (user().getPlayer().getLocation().getGameLocation() != GameLocation.Room)
                {
                    log.warn("Cheat {} cheats, repairSystem, wrong location for repair process location={}",
                            user().getUserLog(), user().getPlayer().getLocation().getGameLocation());
                    return;
                }

                final IContainer fromContainer = moveItemParser.getFrom();
                final int serverID = br.readUint16();
                final float repairValue = br.readSingle();
                final boolean useCubits = br.readBoolean();
                switch (fromContainer.getContainerID().getContainerType())
                {
                    case Hold, Locker ->
                    {
                        log.info("RepairSystem-Hold/Locker " + serverID + " " + useCubits + " " + repairValue);
                    }
                    case ShipSlot ->
                    {
                        log.info("RepairSystem-ShipSlot " + serverID + " " + useCubits + " " + repairValue);
                    }
                    default ->
                    {
                        log.warn("Cheating user " + user().getPlayer().getPlayerLog() +
                                " attempt to repair item in " + fromContainer.getContainerID().getContainerType());
                    }
                }
                if (1==1)
                {
                    sendDebugMsg("Not implemented");
                    return;
                }


                final Hangar hangar = user().getPlayer().getHangar();
                final HangarShip activeShip = hangar.getActiveShip();
                final ShipSlot slot = activeShip.getShipSlots().getSlot(serverID);
                final DurabilityCostCalculator durabilityCostCalculator =
                        new DurabilityCostCalculator(catalogue.globalCard());
                final int costs = (int) durabilityCostCalculator.getCostOfSystem(slot.getShipSystem(), useCubits);
                final ResourceType resourceType = ResourceType.repairType(useCubits);

                final HoldVisitor holdVisitor = new HoldVisitor(user(), ctx.rng());
                final boolean reductionSuccessfully =
                        holdVisitor.reduceItemCountableByCount(resourceType, costs);
                if (!reductionSuccessfully)
                    return;

                durabilityCostCalculator.repairSystem(slot);

                user().send(writer.writeShipInfoDurability(activeShip));
                user().send(writer.writeShipSlots(activeShip));
            }
            case RepairAll ->
            {
                final int shipID = br.readUint16();
                final boolean useCubits = br.readBoolean();
                final var player = user().getPlayer();
                final HangarShip hangarShip = player.getHangar().getByServerId(shipID);
                if (hangarShip == null)
                    return;

                //check if user is docked
                if (user().getPlayer().getLocation().getGameLocation() != GameLocation.Room)
                {
                    log.warn("Cheat {} cheats RepairAll, wrong location for repair process location={}",
                            user().getUserLog(), user().getPlayer().getLocation().getGameLocation());
                    return;
                }

                final DurabilityCostCalculator durabilityCostCalculator =
                        new DurabilityCostCalculator(catalogue.globalCard());
                final long totalRepairCosts = durabilityCostCalculator.getRepairAllCosts(hangarShip, useCubits);

                final ResourceType resourceType = ResourceType.repairType(useCubits);
                final String dbgMsg = user().getPlayer().getName() + "TotalPrice " + totalRepairCosts + " type " + resourceType;
                //sendDebugMsg(dbgMsg);
                //Log.info(dbgMsg);


                final HoldVisitor holdVisitor = new HoldVisitor(user(), ctx.rng());
                try
                {
                    final boolean reductionSuccessfully =
                            holdVisitor.reduceItemCountableByCount(resourceType, totalRepairCosts);
                    if (!reductionSuccessfully)
                        return;
                }
                catch (IllegalArgumentException illegalArgumentException)
                {
                    log.warn(user().getUserLog() + " IllegalArgument exception in RepairAll" + illegalArgumentException.getMessage());
                    return;
                }


                hangarShip.setDurabilityToMax();
                for (final ShipSlot slot : hangarShip.getShipSlots().values())
                {
                    durabilityCostCalculator.repairSystem(slot);
                }

                user().send(writer.writeShipInfoDurability(hangarShip));
                user().send(writer.writeShipSlots(hangarShip));
                hangarShip.getShipStats().setMaxHpPp();

                if (!catalogue.galaxyMapCard().isBaseSector(player.getFaction(), player.getLocation().getSectorID()))
                {
                    sendCapabilityUndockDelay(REPAIR_DOCK_DELAY_TIME_SECONDS);
                }
            }
            case RequestCharacterServices ->
            {
                //user().send(writer.writeCharacterServicesDummy(user));
                user().send(writer.writeCharacterServices(this.characterServices));
            }
            case ResourceHardcap ->
            {
                final long guid = br.readUint32();
                final boolean isToken = ResourceType.Token.guid == guid;
                if (!isToken)
                {
                    log.error(user().getUserLog() + " RequestResourceHardcap was not token! -> client modification");
                    return;
                }
                final ResourceCap tokenCap = user().getPlayer().getMeritsCapFarmed();
                final boolean isSameDate = tokenCap.getLastReset().getLocalDate().getDayOfYear() == LocalDateTime.now(Clock.systemUTC()).getDayOfYear();
                if (!isSameDate)
                    tokenCap.resetCap();
                user().send(writer.writeMeritsCap(tokenCap));
            }
            case MoveItem ->
            {
                log.debug("move item");
                moveItem(br);
            }
            case BindSticker ->
            {
                int shipId = br.readUint16();
                try
                {
                    final Player player = this.user().getPlayer();
                    log.warn("Sticker binding request on ship " + shipId);
                    StickerBinding stickerBinding = br.readDesc(StickerBinding.class);
                    HangarShip hangarShip = player.getHangar().getByServerId(shipId);
                    hangarShip.addSticker(stickerBinding);
                    user().send(writer.writeShipStickerBinding(hangarShip));
                }
                catch (Exception ignored) {}
            }
            case UnbindSticker ->
            {
                int shipID = br.readUint16();
                int spotID = br.readUint16();
                final Player player = this.user().getPlayer();

                HangarShip hangarShip = player.getHangar().getByServerId(shipID);
                for (var sticker : hangarShip.getStickers())
                {
                    if (sticker.getObjectPointHash() == spotID)
                    {
                        hangarShip.removeSticker(sticker);
                    }
                }
            }
            case SetShipName ->
            {
                final int shipId = br.readUint16();
                final String shipName = br.readString();
                final Player player = this.user().getPlayer();
                final HangarShip hangarShip = player.getHangar().getByServerId(shipId);
                if (hangarShip != null)
                {
                    log.info("User sets shipname as {}", shipName);
                    hangarShip.setName(shipName);
                    user().send(writeShipName(hangarShip));
                }
            }
            case ScrapShip ->
            {
                final int shipId = br.readUint16();
                log.warn(user().getUserLog() + " Cheat ScrapShip(should never happen), id:" + shipId);
            }
            case BuySkill ->
            {
                final int skillId = br.readUint16();
                final Player player = this.user().getPlayer();
                final SkillBook skillBook = player.getSkillBook();

                final boolean isEnough = skillBook.checkExperienceEnoughForNextLevel(skillId);
                if (!isEnough)
                {
                    log.warn(user().getUserLog() + " Request for SkillBuy but not enough free experience " +
                            "or the skill was null(id wrong, not mentioned in the map)");
                    return;
                }

                this.upgradeSkill(skillId);
            }
            case InstantSkillBuy ->
            {
                final int skillID = br.readUint16();
                log.error(user().getUserLog() + " InstantSkillBuy should never happen: " + skillID);
                DebugProtocol debugProtocol = user().getProtocol(ProtocolID.Debug);
                debugProtocol.sendEzMsg("Instant SkillBuy should never happen");
            }

            case MoveAll ->
            {
                final Player player = this.user().getPlayer();
                MoveItemParser moveItemParser = new MoveItemParser(br, player);
                try
                {
                    moveItemParser.parseMoveAll();
                }
                catch (IOException ioException)
                {
                    return;
                }
                catch (IllegalArgumentException illegalArgumentException)
                {
                    log.warn(user().getUserLog() + " MoveAll mail items: " + illegalArgumentException.getMessage());
                    return;
                }

                //cast is okay since moveall can only be called by "ItemList" in client
                final ItemList from = (ItemList)moveItemParser.getFrom();
                final ItemList to = (ItemList)moveItemParser.getTo();

                List<ShipItem> itemsToSend = new ArrayList<>();

                if (from.getContainerID() instanceof MailContainerID mailContainerID)
                {
                    Mail mailRemoved = player.getMailBox().removeItem(mailContainerID.getMailID());
                    user().send(writeRemoveItemsFromContainer(from, from.getAllItemsIDs()));
                    itemsToSend.addAll(mailRemoved.getMailContainer().getAllShipItems());
                }
                for (final ShipItem itemToSend : itemsToSend)
                {
                    if (itemToSend instanceof ItemCountable countable)
                    {
                        var existingCountableOpt = to.hasItemCountable(countable);
                        if (existingCountableOpt.isPresent())
                        {
                            ItemCountable existingCountable = existingCountableOpt.get();
                            user().send(writeRemoveItem(to, existingCountable.getServerID()));
                            to.removeShipItem(existingCountable.getServerID());
                            countable.incrementCount(existingCountable.getCount());
                        }

                    }
                    to.addShipItem(itemToSend);
                }

                user().send(writeAddItems(to, itemsToSend));
            }
            case AddShip ->
            {
                final long shipGuid = br.readGUID();
                this.addShip(shipGuid);
            }
            case SelectShip ->
            {
                final int shipID = br.readUint16();
                selectShip(shipID);
            }
            case UpgradeShip ->
            {
                final int shipID = br.readUint16();
                final Player player = this.user().getPlayer();
                final Location location = player.getLocation();
                final boolean isInRoom = location.getGameLocation() == GameLocation.Room;
                if (!isInRoom)
                {
                    log.warn("User " + user().getUserLog() + " used UpgradeShip without Room, cheat!");
                    return;
                }

                final Hangar hangar = player.getHangar();
                final HangarShip shipToUpgrade = hangar.getByServerId(shipID);
                if (shipToUpgrade == null)
                {
                    log.warn("ShipToUpgrade is null");
                    return;
                }
                final ShopItemCard shopItemCard = shipToUpgrade.getShopItemCard();
                final ShipCard shipCard = shipToUpgrade.getShipCard();
                if (shipCard.getNextShipCardGuid() == 0)
                {
                    log.warn("Ship cannot be upgraded!");
                    return;
                }
                final Optional<ShipCard> optUpgradedShipCard = catalogue.fetchCard(shipCard.getNextShipCardGuid(), CardView.Ship);

                if (optUpgradedShipCard.isEmpty())
                {
                    log.warn("Upgrade ShipCard is missing: " + shipCard.getCardGuid());
                    return;
                }
                final ShipCard upgradedShipCard = optUpgradedShipCard.get();


                ShopVisitor shopVisitor = new ShopVisitor(user(), null, ctx.rng());
                boolean isEnoughInHangar = ShopVisitor.isEnoughInContainer(shopItemCard.getUpgradePrice(), player.getHold(), 1);
                if (!isEnoughInHangar)
                {
                    log.warn(user().getUserLog() + " request for shipupgrade but not enough ressources in hangar? --> cheat indicator");
                    return;
                }
                shopVisitor.removeBuyResources(shopItemCard.getUpgradePrice(), player.getHold(), 1);

                //give all existing slots to the ship to upgrade
                final HangarShip upgradedHangarShip = new HangarShip(player.getUserID(),
                        upgradedShipCard.getHangarId(), upgradedShipCard.getCardGuid(), shipToUpgrade.getName());
                upgradedHangarShip.setSlots(shipToUpgrade.getShipSlots());
                upgradedHangarShip.getShipStats().setHp(upgradedHangarShip.getShipStats().getStat(ObjectStat.MaxHullPoints));
                upgradedHangarShip.getShipStats().setPp(upgradedHangarShip.getShipStats().getStat(ObjectStat.MaxPowerPoints));
                upgradedHangarShip.getShipStats().applyStats();


                final Map<StatsProtocolSubscriber, BasePropertyBuffer> oldSubscribers = shipToUpgrade.getShipStats().getSubscribers();
                Set<StatsProtocolSubscriber> failedSubsribers = new HashSet<>();
                for (Map.Entry<StatsProtocolSubscriber, BasePropertyBuffer> subscribers : oldSubscribers.entrySet())
                {
                    upgradedHangarShip.getShipStats().injectOldSubscriber(subscribers.getValue());
                    final boolean sendSuccessfull = subscribers.getKey().sendSpacePropertyBuffer(subscribers.getValue());
                    if (!sendSuccessfull)
                        failedSubsribers.add(subscribers.getKey());
                }
                oldSubscribers.clear();
                failedSubsribers.forEach(sub -> upgradedHangarShip.getShipStats().removeSubscriber(sub));


                hangar.addHangarShip(upgradedHangarShip);

                user().send(writeAddShip(upgradedHangarShip));
                user().send(writer.writeShipSlots(upgradedHangarShip));
                user().send(writer.writeShipInfoDurability(upgradedHangarShip));
                this.selectShip(shipID);
            }
            case SelectConsumable ->
            {
                final int shipID = br.readUint16();
                final long consumableGUID = br.readGUID();
                final int slotID = br.readUint16();
                final Player player = this.user().getPlayer();

                Hangar hangar = player.getHangar();
                HangarShip ship = hangar.getByServerId(shipID);
                if (ship == null)
                    return;

                Hold hold = player.getHold();
                Optional<ShipItem> consumable = hold.getByGUID(consumableGUID);
                if (consumable.isPresent())
                {
                    ItemCountable itemCountable = (ItemCountable) consumable.get();
                    final ShipSlot slot = ship.getShipSlots().getSlot(slotID);
                    if (slot == null) return;
                    slot.setCurrentConsumable(itemCountable);
                    final SpaceSubscribeInfo spaceSubscribeInfo = ship.getShipStats();
                    spaceSubscribeInfo.applyStats();

                    user().send(writer.writeShipSlots(ship));
                }
            }
            case UpgradeSystem ->
            {
                final Player player = this.user().getPlayer();
                final MoveItemParser moveItemParser = new MoveItemParser(br, player);
                final IContainer container = moveItemParser.parseContainer();
                final int itemID = br.readUint16();
                final int newLevel = Byte.toUnsignedInt(br.readByte());

                ShipItem itemToUpgrade = container.getByID(itemID);

                if (itemToUpgrade == null)
                {
                    log.info(user().getUserLog() + " Upgrade item not found in container");
                    return;
                }
                if (itemToUpgrade.getItemType() != ItemType.System)
                {
                    log.info(user().getUserLog() + " Upgrade Item was not of type System!");
                    return;
                }
                final ShipSystem shipSystemToUpgrade = (ShipSystem) itemToUpgrade;
                final ShipSystemCard itemCard = shipSystemToUpgrade.getShipSystemCard();
                if (itemCard == null)
                {
                    log.info(user().getUserLog() + " item card is null");
                    return;
                }

                log.info(user().getUserLog() + " UpgradeSystem: " + container.getContainerID().getContainerType() + " " + itemID + " " + newLevel);
                log.info("UpgradeSystem [{}] container: {}, itemID: {}, currentLevel: {}, newLevel: {}",
                        user().getUserLog(),
                        container.getContainerID().getContainerType(),
                        itemID,
                        shipSystemToUpgrade.getShipSystemCard().getLevel(),
                        newLevel
                );


                if (!itemCard.isUserUpgradeable())
                {
                    log.info(user().getUserLog() + " Cheat Try to upgrade but not upgradeable");
                    return;
                }
                if (itemCard.getNextCardGuid() == 0)
                {
                    log.info(user().getUserLog() + " Cheat Try to upgrade but has no next Card");
                    return;
                }
                if (itemCard.getLevel() >= newLevel)
                {
                    log.info(user().getUserLog() + " Cheat ShipSystem upgrade level is lower or equal to current levl");
                    return;
                }
                if (newLevel > 10)
                {
                    log.info(user().getUserLog() + " Cheat ShipSystem upgrade level call higher than 10! -> cheat");
                    return;
                }
                if (container.getContainerID().getContainerType() == ContainerType.Shop)
                {
                    log.info(user().getUserLog() + " Update ShopSystem tried");
                    return;
                }

                final long[] skillHashes = shipSystemToUpgrade.getShipSystemCard().getSkillHashes();
                final boolean skillsForUpgradeSatisfied = user().getPlayer()
                        .getSkillBook()
                        .skillsForUpgradeSatisfied(skillHashes, itemCard.getLevel()+1);
                if (!skillsForUpgradeSatisfied)
                {
                    log.warn("Cheat ({}) cannot update item because skill is not present!", user().getUserLog());
                    return;
                }

                final ContainerVisitor containerVisitor = ContainerVisitorFactory.
                        createVisitor(container.getContainerID().getContainerType(), user(), moveItemParser, ctx.rng());

                final boolean successfull = containerVisitor.upgradeSystem(container, player.getHold(),
                        shipSystemToUpgrade, newLevel);

                log.info("UPGRADE SYSTEM Successfull: " + successfull);

            }
            case UpgradeSystemByPack ->
            {
                final Player player = this.user().getPlayer();
                MoveItemParser moveItemParser = new MoveItemParser(br, player);
                final IContainer container = moveItemParser.parseContainer();
                final int itemID = br.readUint16();
                final long packCount = br.readUint32();

                final ShipItem itemToUpgrade = container.getByID(itemID);
                if (itemToUpgrade == null) return;
                if (itemToUpgrade.getItemType() != ItemType.System) return;

                final ShipSystem shipSystemToUpgrade = (ShipSystem) itemToUpgrade;
                final ShipSystemCard itemCard = shipSystemToUpgrade.getShipSystemCard();

                if (!itemCard.isUserUpgradeable())
                {
                    log.info(user().getUserLog() + " Try to upgrade but not upgradeable");
                    return;
                }
                if (itemCard.getNextCardGuid() == 0)
                {
                    log.info(user().getUserLog() + " Try to upgrade but has no next Card");
                    return;
                }
                if (container.getContainerID().getContainerType() == ContainerType.Shop)
                {
                    log.info(user().getUserLog() + " Update ShopSystem tried");
                    return;
                }

                final long[] skillHashes = shipSystemToUpgrade.getShipSystemCard().getSkillHashes();
                final boolean skillsForUpgradeSatisfied = user().getPlayer()
                        .getSkillBook()
                        .skillsForUpgradeSatisfied(skillHashes, itemCard.getLevel()+1);
                if (!skillsForUpgradeSatisfied)
                {
                    log.warn("Cheat ({}) cannot update item because skill is not present!", user().getUserLog());
                    return;
                }

                log.info("UpgradeSystemByPack system {} current {} packCount {}",
                        shipSystemToUpgrade.getCardGuid(),
                        shipSystemToUpgrade.getShipSystemCard().getLevel(),
                        packCount
                );

                final ContainerVisitor containerVisitor = ContainerVisitorFactory.
                        createVisitor(container.getContainerID().getContainerType(), user(), moveItemParser, ctx.rng());
                final boolean upgradeResult = containerVisitor.upgradeSystemByPack(container, player.getHold(),
                        shipSystemToUpgrade, packCount);

            }
            case UseAugment ->
            {
                final Player player = this.user().getPlayer();
                MoveItemParser moveItemParser = new MoveItemParser(br, player);
                moveItemParser.parseUseAugment();
                final IContainer container = moveItemParser.getFrom();

                final ShipItem item = container.getByID(moveItemParser.getItemID());
                if (item == null)
                    return;

                final Optional<AugmentTemplate> optAugmentTemplate = AugmentTemplates.getTemplateForId(item.getCardGuid());
                if (optAugmentTemplate.isEmpty())
                {
                    log.error("Activated augment but no template! " + item.getCardGuid());
                    return;
                }
                final AugmentTemplate augmentTemplate = optAugmentTemplate.get();
                if (!(augmentTemplate instanceof AugmentFactorTemplate augmentFactorTemplate))
                {
                    log.error("No factor template");
                    return;
                }

                final Collection<Factor> newFactorsLst = Factor.fromTemplate(augmentFactorTemplate);
                final Factors factors = player.getFactors();
                final Map<FactorType, Float> limits = factors
                        .getMultiplicatorsForLimitLeft(augmentFactorTemplate.getFactorSource());
                boolean canActivate = true;
                for (Factor factor : newFactorsLst)
                {
                    final float limit = limits.getOrDefault(factor.getFactorType(), factors.getBoostLimiter());
                    if (factor.getValue() > limit)
                    {
                        canActivate = false;
                        break;
                    }
                }
                if (!canActivate)
                {
                    user().send(writer.writeCannotStackBoosters());
                    return;
                }


                ContainerVisitor containerVisitor = ContainerVisitorFactory.createVisitor(
                        container.getContainerID().getContainerType(),
                        user(),
                        moveItemParser,
                        ctx.rng()
                );
                final boolean operationIsOkay = containerVisitor.useAugment();
                if (!operationIsOkay)
                    return;

                activateAugment(item.getCardGuid());
            }
            case SubmitMission ->
            {
                final int missionId = br.readUint16();
                final MissionBook missionBook = user().getPlayer().getCounterFacade().missionBook();
                log.info("Submit mission {}", missionId);
                final Optional<Mission> optMission = missionBook.getByID(missionId);
                if (optMission.isEmpty())
                {
                    //log.warn("Cheat User {} tried to submit a mission that is not present anymore!", user().getUserLog());
                    return;
                }
                final Mission mission = optMission.get();
                if (mission.getMissionState() == null || mission.getMissionState() != Mission.MissionState.Completed)
                {
                    log.warn("Cheat, user {} tried to submit a mission in a wrong state {}", user().getUserLog(), mission.getMissionState());
                    return;
                }
                final MissionCardsFetchResult missionCardsFetchResult = catalogue.fetchMissionCards(mission.getMissionCardGUID());
                if (!missionCardsFetchResult.isValid())
                {
                    log.warn("MissionCardsFetchResult was invalid!");
                    return;
                }
                mission.setMissionState(Mission.MissionState.Submitting);
                final RewardCard rewardCard = missionCardsFetchResult.rewardCard();
                final int exp = rewardCard.getExperience();
                final List<ShipItem> lootAll = rewardCard.getShipItems();
                final List<ShipItem> factionLoot = rewardCard.getShipItems(user().getPlayer().getFaction());
                final List<ShipItem> resultItemLst = new ArrayList<>();
                resultItemLst.addAll(lootAll);
                resultItemLst.addAll(factionLoot);

                final NotificationProtocolWriteOnly notificationWriter = ProtocolRegistryWriteOnly.getProtocol(ProtocolID.Notification);
                user().send(notificationWriter.writeMissionCompleted(missionId));
                user().send(notificationWriter.writeMissionReward(mission.getMissionCardGUID(), resultItemLst));
                user().send(writer.writeRemoveMissions(List.of(missionId)));
                missionBook.removeItem(missionId);
                addExperience(exp);
                micrometerRegistry.missionSubmitted(user().getPlayer().getFaction());
                user().getPlayer().getCounterFacade().incrementCounter(CounterCardType.missions_completed, 0);
                resultItemLst.forEach(item -> ContainerVisitor.addShipItem(user(), item, user().getPlayer().getHold()));
            }
            case AugmentMassActivation ->
            {
                final Player player = this.user().getPlayer();
                MoveItemParser moveItemParser = new MoveItemParser(br, player);
                final long iterations = moveItemParser.parseAugmentMassActivation();
                log.info("augmentMassAct: " + moveItemParser.getFrom() + " " + moveItemParser.getItemID() + " " + iterations);
                final ShipItem itemToAnalyse = moveItemParser.getFrom().getByID(moveItemParser.getItemID());

                ContainerVisitor containerVisitor =
                        ContainerVisitorFactory
                                .createVisitor(moveItemParser.getFrom().getContainerID().getContainerType(), user(), moveItemParser, ctx.rng());

                final boolean operationIsOkay = containerVisitor.augmentMassActivationIsFineAndRemove(iterations);
                if (!operationIsOkay) return;

                //okay, analyse all not ident
                analyseNotIdentifiedObject(itemToAnalyse.getCardGuid(), iterations);
            }
            default -> {
                log.error("PlayerProtocol Could not handle replyType: " + clientMessage);
            }
        }
    }

    private void sendCapabilityUndockDelay(final int secondsBlocked)
    {
        user().send(writeCapability(Capability.Undock, LocalDateTime.now(Clock.systemUTC()).plusSeconds(secondsBlocked)));
        final Runnable delayedCapabilityReset = () -> user().send(writeCapabilityReset(Capability.Undock));
        ctx.scheduledExecutorService().schedule(delayedCapabilityReset, secondsBlocked, TimeUnit.SECONDS);
    }

    public void activateAugment(final long itemGUID)
    {
        activateAugment(itemGUID, -1);
    }
    public void activateAugment(final long itemGUID, final int customTimeHours)
    {
        final Factors factors = user().getPlayer().getFactors();
        final Optional<AugmentTemplate> optAugmentTemplate = AugmentTemplates.getTemplateForId(itemGUID);
        if (optAugmentTemplate.isEmpty())
        {
            log.error("Critical in augment activation, cannot find augment {} for user {}", itemGUID, user().getUserLog());
            sendDebugMsg("Critical error in augment activation, cannot find augment!");
            return;
        }
        final AugmentTemplate augmentTemplate = optAugmentTemplate.get();
        if (!(augmentTemplate instanceof AugmentFactorTemplate augmentFactorTemplate))
        {
            sendDebugMsg("Critical error in augment activation, augment is not oft type factor!");
            return;
        }
        log.info("User {} activated augment item {}", user().getUserLog(), augmentFactorTemplate.getAssociatedItemGUID());
        final Collection<Factor> factorLst = Factor.fromTemplate(augmentFactorTemplate, customTimeHours);
        for (Factor factor : factorLst)
        {
            factors.addFactor(factor);
        }
        user().send(writer.writeFactors(factorLst));
    }

    private void sendDebugMsg(final String msg)
    {
        final DebugProtocol debugProtocol = user().getProtocol(ProtocolID.Debug);
        debugProtocol.sendEzMsg(msg);
    }

    public void addShip(final long shipGuid)
    {
        final Player player = this.user().getPlayer();
        if (!player.getLocation().isInRoom())
        {
            log.warn("Cheat={}, not in room while adding ship", user().getUserLog());
            return;
        }

        final Optional<ShipCard> optShipCard = catalogue.fetchCard(shipGuid, CardView.Ship);
        final Optional<ShopItemCard> optShopItemCard = catalogue.fetchCard(shipGuid, CardView.Price);

        if (optShipCard.isEmpty() || optShopItemCard.isEmpty())
        {
            log.error("Player " + player.getUserID() + " requested missing shipGUID or shopGUID: " + shipGuid);
            return;
        }
        final ShipCard shipCard = optShipCard.get();
        final ShopItemCard shopItemCard = optShopItemCard.get();

        if (shipCard.getLevel() > 1)
        {
            log.error("ShipAddShip, level higher than 1: " + shipCard.getLevel());
            return;
        }

        if (!player.getBgoAdminRoles().hasOneRole(BgoAdminRoles.Developer))
        {
            final Short currentPlayerLevel = player.getSkillBook().get();
            if (shipCard.getLevelRequirement() > currentPlayerLevel)
            {
                log.warn("Cheat, user {} tried to buy ship without the required level current:{} required:{}",
                        user().getUserLog(), currentPlayerLevel, shipCard.getLevelRequirement());
                return;
            }
            if (shopItemCard.getFaction() != player.getFaction())
            {
                log.warn("Cheat, user {} tried to buy ship without the correct faction current:{} required:{}",
                        user().getUserLog(), player.getFaction(), shopItemCard.getFaction());
                return;
            }
        }

        //check buy price
        final Price buyPrice = shopItemCard.getBuyPrice();
        final ShopVisitor shopVisitor = new ShopVisitor(user(), null, ctx.rng());
        final boolean isEnoughInHangar = ContainerVisitor.isEnoughInContainer(buyPrice, player.getHold(), 1);
        //remove buy ressources if enough in hangar
        if (isEnoughInHangar)
        {
            log.info("User adds ship " + user().getUserLog() + " " + buyPrice);
            shopVisitor.removeBuyResources(buyPrice, player.getHold(), 1);
            final Hangar hangar = player.getHangar();
            final HangarShip newShip = new HangarShip(player.getUserID(), shipCard.getHangarId(), shipGuid, "");
            newShip.getShipStats().setSkillBook(player.getSkillBook());
            newShip.getShipStats().applyStats();
            newShip.getShipStats().setHp(newShip.getShipStats().getStatOrDefault(ObjectStat.MaxHullPoints));
            newShip.getShipStats().setPp(newShip.getShipStats().getStatOrDefault(ObjectStat.MaxPowerPoints));
            hangar.addHangarShip(newShip);
            user().send(writeAddShip(newShip));
            user().send(writer.writeShipSlots(newShip));
            user().send(writer.writeShipInfoDurability(newShip));
        }
    }

    public void addExperience(final long exp)
    {
        if (user() == null)
        {
            log.warn("user was null in playerprotocol for addexperience");
            return;
        }
        user().getPlayer().getSkillBook().addExperience(exp);
        final NotificationProtocol notificationProtocol = user().getProtocol(ProtocolID.Notification);

        user().send(notificationProtocol.writer().writeExperienceGained((int) exp));
        sendExperienceCollective();
    }

    public void analyseNotIdentifiedObject(final long cardGUID, final long iterations)
    {
        final List<ShipItem> augmentItems = new ArrayList<>();
        //analyse object
        final Optional<AugmentTemplate> optItem = AugmentTemplates.getTemplateForId(cardGUID);
        if (optItem.isEmpty() || !(optItem.get() instanceof final AugmentLootItemTemplate augmentLootItemTemplate))
        {
            final DebugProtocol debugProtocol = user().getProtocol(ProtocolID.Debug);
            debugProtocol.sendEzMsg("not implemented!");
            return;
        }
        for (long i = 0; i < iterations; i++)
        {
            final long exp = augmentLootItemTemplate.getExperience();
            final List<LootEntryInfo> entries = augmentLootItemTemplate.getLootEntryInfos();
            for (final LootEntryInfo entry : entries)
            {
                final boolean isOkay = ctx.rng().rollChance(entry.chance());
                if (!isOkay)
                    continue;
                final boolean isInLevelIntervall = entry.isInLevelIntervall(this.user().getPlayer().getSkillBook().get());
                if (!isInLevelIntervall)
                    continue;

                final boolean isAllowedFaction = entry.allowedToReceiveFaction(user().getPlayer().getFaction());
                if (!isAllowedFaction)
                    continue;

                ShipItem shipItem;
                if (entry.shipItem() instanceof ItemCountable itemCountable)
                {
                    final long newCount = ctx.rng().variateByPercentage(itemCountable.getCount(), entry.variationPercentage());
                    final ItemCountable cpyCountable = itemCountable.copy();
                    cpyCountable.updateCount(newCount);
                    shipItem = cpyCountable;
                }
                else
                {
                    shipItem = entry.shipItem().copy();
                }
                augmentItems.add(shipItem);
            }
        }

        final Hold tmpHold = new Hold(user().getPlayer().getUserID());
        tmpHold.addShipItems(augmentItems);

        final NotificationProtocol notificationProtocol = user().getProtocol(ProtocolID.Notification);
        notificationProtocol.sendAugmentItemsAndAdd(tmpHold.getAllShipItems());
    }

    public void upgradeSkill(final int skillId)
    {
        final Player player = this.user().getPlayer();
        final SkillBook skillBook = player.getSkillBook();
        skillBook.upgradeSkill(skillId);

        user().send(writer.writeSpentExperience(skillBook.getSpentExperience()));
        user().send(writer.writeSkills(player.getSkillBook()));

        final HangarShip activeShip = player.getHangar().getActiveShip();
        activeShip.getShipStats().setSkillBook(skillBook);
    }
    public void resetSkill(final int skillID)
    {
        final Player player = this.user().getPlayer();
        final SkillBook skillBook = player.getSkillBook();
        skillBook.resetSkill(skillID);

        user().send(writer.writeSpentExperience(skillBook.getSpentExperience()));
        user().send(writer.writeSkills(player.getSkillBook()));

        final HangarShip activeShip = player.getHangar().getActiveShip();
        activeShip.getShipStats().setSkillBook(skillBook);
    }


    private BgoProtocolWriter writeRemoveItemsFromContainer(IContainer from, Set<Integer> itemsToRemove)
    {
        BgoProtocolWriter bw = newMessage();
        switch (from.getContainerID().getContainerType())
        {
            case Hold -> bw.writeMsgType(ServerMessage.RemoveHoldItems.value);
            case Locker -> bw.writeMsgType(ServerMessage.RemoveLockerItems.value);
            case Mail -> bw.writeMsgType(ServerMessage.RemoveMail.value);

            default -> throw new IllegalArgumentException("Could not handle remove items from container: " +
                    from.getContainerID().getContainerType());
        }
        bw.writeUint16Collection(itemsToRemove);

        return bw;
    }

    public void selectShip(final int shipID)
    {
        final Player player = this.user().getPlayer();
        final Hangar hangar = player.getHangar();
        if (!player.getLocation().isInRoom())
        {
            log.info("User={} used selectShip but was not in room!", user().getUserLog());
            return;
        }

        if (hangar.getActiveShip().getServerId() == shipID)
        {
            return;
        }

        final HangarShip oldActiveShip = hangar.getActiveShip();
        hangar.setActiveShipIndex(shipID);
        final Map<StatsProtocolSubscriber, BasePropertyBuffer> oldSubscribers = oldActiveShip.getShipStats().getSubscribers();
        final HangarShip activeShip = hangar.getActiveShip();
        activeShip.getShipStats().applyStats();
        for (Map.Entry<StatsProtocolSubscriber, BasePropertyBuffer> subscribers : oldSubscribers.entrySet())
        {
            activeShip.getShipStats().injectOldSubscriber(subscribers.getValue());
            final boolean sendResultOk = subscribers.getKey().sendSpacePropertyBuffer(subscribers.getValue());
        }


        oldSubscribers.clear();


        user().send(writer.writeActivePlayerShip(activeShip.getServerId()));
        user().send(writeHangarShipStats(activeShip));

        if (!catalogue.galaxyMapCard().isBaseSector(player.getFaction(), player.getLocation().getSectorID()))
        {
            sendCapabilityUndockDelay(REPAIR_DOCK_DELAY_TIME_SECONDS);
        }
    }


    public BgoProtocolWriter writeHpAndPp(final float hp, final float pp)
    {
        BgoProtocolWriter bw = newMessage();
        bw.writeMsgType(ServerMessage.Stats.value);
        bw.writeLength(2);

        bw.writeByte((byte) 6);
        bw.writeSingle(pp);

        bw.writeByte((byte) 7);
        bw.writeSingle(hp);

        return bw;
    }

    public BgoProtocolWriter writeHangarShipStats(final HangarShip hangarShip)
    {
        return writer.writeHangarShipStats(hangarShip);
    }

    private void moveItem(final BgoProtocolReader br)
    {
        final Player player = this.user().getPlayer();
        final MoveItemParser moveItemParser = new MoveItemParser(br, player);
        try
        {
            moveItemParser.parseMoveItem();
        }
        catch (IllegalStateException stateException)
        {
            stateException.printStackTrace();
            log.error("illegal state exception={}", stateException.getMessage());
            return;
        }
        catch (IOException ioException)
        {
            log.error("error in playerprotocol moveitem={}", ioException.getMessage());
            return;
        }

        //from container
        IContainer fromContainer = moveItemParser.getFrom();
        IContainer toContainer = moveItemParser.getTo();
        //ContainerVisitor visitor = new HoldVisitor(client, moveItemParser);
        ContainerVisitor visitor = ContainerVisitorFactory.createVisitor(fromContainer.getContainerID().getContainerType(),
                user(), moveItemParser, ctx.rng());

        try
        {
            toContainer.accept(visitor);
        }catch (IllegalStateException e)
        {
            if (e.getMessage().equals(ContainerVisitor.EXCEPT_MSG_CANNOT_FIND))
            {
                //client modification or unexpected behaviour
                log.error("MoveItem " + e.getMessage() + " " + user().getUserLog());
                DebugProtocol debugProtocol = user().getProtocol(ProtocolID.Debug);
                debugProtocol.sendEzMsg(e.getMessage() + " " + player.getUserID());
            }
        }
    }

    public BgoProtocolWriter writeAddItems(final IContainer container, final Collection<ShipItem> shipItems)
    {
        BgoProtocolWriter bw = newMessage();
        switch (container.getContainerID().getContainerType())
        {
            case Hold ->
            {
                bw.writeMsgType(ServerMessage.HoldItems.value);
            }
            case Locker ->
            {
                bw.writeMsgType(ServerMessage.LockerItems.value);
            }
            default -> throw new IllegalArgumentException();
        }
        bw.writeDescCollection(shipItems);

        return bw;
    }
    public BgoProtocolWriter writeAddItem(final IContainer container, final ShipItem shipItem)
    {
        return writeAddItems(container, Collections.singletonList(shipItem));
    }


    public void factionSwitchProcess(final boolean withPrice, final float cubitsPrice)
    {
        log.info("User faction change triggered " + user().getUserLog());
        if (withPrice)
        {
            final long finalPrice = cubitsPrice == -1 ? (long) characterServices.cubitsPriceFaction() : (long) cubitsPrice;
            final HoldVisitor holdVisitor = new HoldVisitor(user(), ctx.rng());
            final boolean isReduceSuccessfully = holdVisitor
                    .reduceItemCountableByCount(ResourceType.Cubits, finalPrice);
            if (!isReduceSuccessfully)
            {
                log.warn("User used ChangeFaction but not enough cubits for change! " + user().getUserLog());
                return;
            }
        }

        final Location currentLocation = user().getPlayer().getLocation();
        final boolean isInRoom = currentLocation.getGameLocation() == GameLocation.Room;
        if (!isInRoom)
        {
            log.warn(user().getUserLog() + "faction switch but not in room");
            return;
        }

        final Hangar hangar = user().getPlayer().getHangar();
        ShipSlotVisitor shipSlotVisitor = new ShipSlotVisitor(user(), null);
        for (final HangarShip hangarShip : hangar.getAllHangarShips())
        {
            for (ShipSlot slot : hangarShip.getShipSlots().values())
            {
                final ShipItem removedShipItem = slot.removeShipItem();
                if (removedShipItem == null || removedShipItem.getCardGuid() == 0)
                    continue;
                shipSlotVisitor.addShipItem(removedShipItem, user().getPlayer().getLocker());
            }
        }
        final List<ShipCard> shipCards = hangar.getAllHangarShips().stream()
                .map(HangarShip::getShipCard)
                .toList();

        final ShipCardConverter shipCardConverter = new ShipCardConverter(user().getPlayer().getFaction());
        final List<ShipCard> oppositeShipCards = shipCardConverter.convertAllCards(shipCards);
        hangar.removeAllHangarShips();
        for (final ShipCard oppositeShipCard : oppositeShipCards)
        {
            hangar.addHangarShip(new HangarShip(
                    user().getPlayer().getUserID(),
                    oppositeShipCard.getHangarId(),
                    oppositeShipCard.getCardGuid(),
                    ""
                    )
            );
        }
        hangar.setActiveShipIndex(1);
        final Location location = user().getPlayer().getLocation();
        final Faction invertedFaction = Faction.invert(user().getPlayer().getFaction());
        final int baseSectorId = GalaxyMapCard.getStartSector(invertedFaction);
        final GalaxyMapCard galaxyMapCard = catalogue.fetchCardUnsafe(StaticCardGUID.GalaxyMap, CardView.GalaxyMap);
        var optStar = galaxyMapCard.getStar(baseSectorId);
        if (optStar.isEmpty())
        {
            log.error("Error in faction switch, new base system not present");
            return;
        }
        log.info("Remove user from guild");
        if (user().getPlayer().getGuild().isPresent())
        {
            final Guild guild = user().getPlayer().getGuild().get();
            guild.removePlayer(user().getPlayer().getUserID());
            user().getPlayer().setGuild(null);
            CommunityProtocol communityProtocol = user().getProtocol(ProtocolID.Community);
            final BgoProtocolWriter guildLeaveBw = communityProtocol
                    .writer()
                    .writeGuildRemove(user().getPlayer().getUserID(), true);
            communityProtocol.getGuildProcessing().sendToEachGuildMember(guild, guildLeaveBw);
        }
        log.info("remove from party");
        final Optional<IParty> optParty = user().getPlayer().getParty();
        if (optParty.isPresent())
        {
            final IParty party = optParty.get();
            final CommunityProtocol communityProtocol = user().getProtocol(ProtocolID.Community);
            communityProtocol.getPartyProcessing().removeUserFromParty(user(), party);
        }
        //double check
        if (user().getPlayer().getParty().isPresent())
        {
            log.error("warning for faction switch, switch created but still in party, user={}", user().getUserLog());
        }

        log.info("Remove missions");
        user().getPlayer().getCounterFacade().missionBook().resetWithoutTimestamp();


        log.info("Switch faction to " + invertedFaction);
        user().getPlayer().setFaction(invertedFaction);

        log.info("SetLocation to " + baseSectorId + " " + optStar.get().getSectorGuid());
        location.setLocation(GameLocation.Room, baseSectorId, optStar.get().getSectorGuid());
        log.info("setup dummy character");
        final PlayerAvatar avatarDesc = user().getPlayer().getAvatarDescription();

        log.info("Send disconnect!");
        SceneProtocol sceneProtocol = user().getProtocol(ProtocolID.Scene);
        user().send(sceneProtocol.writeDisconnect());
        log.info("Faction switch process finished for user " + user().getUserLog());
    }


    public BgoProtocolWriter writeRemoveItem(final IContainer fromContainer, final int... serverIds)
    {
        final BgoProtocolWriter bw = newMessage();
        switch (fromContainer.getContainerID().getContainerType())
        {
            case Hold ->
            {
                bw.writeMsgType(ServerMessage.RemoveHoldItems.value);
            }
            case Locker ->
            {
                bw.writeMsgType(ServerMessage.RemoveLockerItems.value);
            }
            default -> throw new IllegalArgumentException();
        }
        bw.writeUint16Collection(serverIds);

        return bw;
    }


    public void sendExperienceCollective()
    {
        final Player player = this.user().getPlayer();
        final SkillBook skillBook = player.getSkillBook();
        user().send(writer.writeExperience(skillBook.getExperience()));
        user().send(writer.writeSpentExperience(skillBook.getSpentExperience()));
        user().send(writeNormalExperience(skillBook.getExperience()));
        user().send(writePlayerLevel(skillBook.getExperience()));
    }


    public BgoProtocolWriter writeNormalExperience(final long exp)
    {
        final BgoProtocolWriter bw = newMessage();
        bw.writeMsgType(ServerMessage.NormalExperience.value);
        short level = this.experienceToLevelAlgo.getLevelBasedOnExp(exp);

        final long prevLevelExperience = this.experienceToLevelAlgo.getExpFromLevel(level);
        final long nextLevelExperience = this.experienceToLevelAlgo.getExpFromLevel((short) (level+1));

        bw.writeUInt32(prevLevelExperience);
        bw.writeUInt32(nextLevelExperience);

        return bw;
    }
    public BgoProtocolWriter writePlayerLevel(final long experience)
    {
        final BgoProtocolWriter bw = newMessage();
        bw.writeMsgType(ServerMessage.Level.value);
        final short level = this.experienceToLevelAlgo.getLevelBasedOnExp(experience);
        bw.writeByte((byte) level);

        return bw;
    }

    public void sendAllShipInfoDurability()
    {
        final Player player = this.user().getPlayer();
        List<HangarShip> hangarShips = player.getHangar().getAllHangarShips();
        for (final HangarShip ship : hangarShips)
            user().send(writer.writeShipInfoDurability(ship));
    }

    public void sendAllShipNames()
    {
        final Player player = this.user().getPlayer();
        final Hangar hangar = player.getHangar();
        for (final HangarShip hangarShip : hangar.getAllHangarShips())
        {
            if (!hangarShip.getName().isEmpty())
            {
                user().send(writeShipName(hangarShip));
            }
        }
    }
    public BgoProtocolWriter writeShipName(final HangarShip hangarShip)
    {
        final BgoProtocolWriter bw = newMessage();
        bw.writeMsgType(ServerMessage.ShipName.value);
        bw.writeUInt16(hangarShip.getServerId());
        bw.writeString(hangarShip.getName());

        return bw;
    }
    public void sendAllStickerBindings()
    {
        final Player player = this.user().getPlayer();
        for (HangarShip ship : player.getHangar().getAllHangarShips())
        {
            user().send(writer.writeShipStickerBinding(ship));
        }
    }
    public BgoProtocolWriter writeShipStickerBindingsRemove(final HangarShip hangarShip)
    {
        final BgoProtocolWriter bw = newMessage();
        bw.writeMsgType(ServerMessage.RemoveStickers.value);
        bw.writeUInt16(hangarShip.getServerId());

        final List<StickerBinding> stickersToRemove = hangarShip.getStickers();
        final List<Integer> uint16ListObjectPointHashes = stickersToRemove.stream().map(StickerBinding::getObjectPointHash).toList();
        bw.writeUint16Collection(uint16ListObjectPointHashes);

        return bw;
    }


    public void sendAllShipSlots()
    {
        final Player player = this.user().getPlayer();
        for (final HangarShip ship : player.getHangar().getAllHangarShips())
        {
            user().send(writer.writeShipSlots(ship));
        }
    }


    public BgoProtocolWriter writeSlotStats(final int slotID, final ObjectStats objectStats)
    {
        BgoProtocolWriter bw = newMessage();
        bw.writeMsgType(ServerMessage.Stats.value);

        bw.writeLength(objectStats.getAllStats().size());

        for (Map.Entry<ObjectStat, Float> stat : objectStats.getAllStats().entrySet())
        {
            bw.writeByte((byte) 12);
            bw.writeByte((byte) slotID);
            bw.writeUInt16(stat.getKey().value);
            bw.writeSingle(stat.getValue());
        }

        return bw;
    }

    @Override
    public boolean sendSpacePropertyBuffer(final BasePropertyBuffer spacePropertyBuffer)
    {
        if (user() == null)
        {
            log.error("Send result error of BasePropertyBuffer because user is null");
            return false;
        }

        final BgoProtocolWriter bw = writer.writeSpacePropertyBuffer(spacePropertyBuffer);
        return user().send(bw);
    }

    @Override
    public long userId()
    {
        return this.user().getPlayer().getUserID();
    }


    public void sendPlayerHangar()
    {
        final Player player = this.user().getPlayer();
        final List<HangarShip> hangarShips = player.getHangar().getAllHangarShips();
        for (HangarShip hangarShip : hangarShips)
        {
            user().send(writeAddShip(hangarShip));
        }
    }
    public BgoProtocolWriter writeAddShip(final HangarShip hangarShip)
    {
        return this.writeAddShip(hangarShip.getServerId(), hangarShip.getCardGuid());
    }
    private BgoProtocolWriter writeAddShip(final int shipId, final long shipGuid)
    {
        if (shipId < 0) throw new IllegalArgumentException("shipId cannot be null");
        if (shipGuid < 0) throw new IllegalArgumentException("shipGuid cannot be null");

        BgoProtocolWriter bw = newMessage();
        bw.writeUInt16(ServerMessage.AddShip.value);
        bw.writeUInt16(shipId);
        bw.writeGUID(shipGuid);
        return bw;
    }

    public BgoProtocolWriter writeCapabilityReset(final Capability capability)
    {
        final BgoProtocolWriter bw = newMessage();
        bw.writeMsgType(ServerMessage.Capability.value);

        bw.writeByte(capability.getValue());
        bw.writeByte((byte) 0);

        return bw;
    }
    public BgoProtocolWriter writeCapability(final Capability capability, final LocalDateTime localDateTime)
    {
        final BgoProtocolWriter bw = newMessage();
        bw.writeMsgType(ServerMessage.Capability.value);

        bw.writeByte(capability.getValue());
        bw.writeByte((byte) 2);
        bw.writeDateTime(localDateTime);

        return bw;
    }

    public BgoProtocolWriter writeNameAvailability(final boolean isFree)
    {

        final BgoProtocolWriter bw = newMessage();
        if (isFree)
        {
            bw.writeUInt16(ServerMessage.NameAvailable.value);
        } else
        {
            bw.writeUInt16(ServerMessage.NameNotAvailable.value);
        }
        return bw;
    }


    public void sendCharacter()
    {
        if (user() == null)
        {
            log.error("SendCharacter but the given user was null!");
            return;
        }
        final Player player = this.user().getPlayer();
        this.user().send(writer.writeReset());
        this.user().send(writer.writeId(player.getUserID()));
        this.user().send(writer.writeName(player.getName()));
        this.user().send(writer.writeAvatarDescription(player.getAvatarDescription().get()));
        this.user().send(writer.writeFaction(player.getFaction()));
        this.sendExperienceCollective();
        this.user().send(writer.writeSkills(player.getSkillBook()));
        this.sendPlayerHangar();
        this.sendAllShipInfoDurability();
        this.sendAllShipSlots();
        this.sendAllShipNames();
        this.sendAllStickerBindings();
        this.user().send(writer.writeMailBox(player.getMailBox()));
        if (player.getHangar().hasActiveShip())
        {
            this.user().send(writer.writeActivePlayerShip(player.getHangar().getActiveShip().getServerId()));
            this.user().send(this.writeHangarShipStats(player.getHangar().getActiveShip()));
        }

        final LocalDateTime endTime = LocalDateTime.of(2024, 6, 1, 0, 0, 0);

        if (!user().getPlayer().getFactors().hasFactorSource(FactorSource.Holiday)
                && LocalDateTime.now(Clock.systemUTC()).isBefore(endTime))
        {
            user().getPlayer().getFactors().addFactor(Factor.fromEndTime(FactorType.Loot, FactorSource.Holiday, 50, endTime));
            user().getPlayer().getFactors().addFactor(Factor.fromEndTime(FactorType.Experience, FactorSource.Holiday, 10f, endTime));
            user().getPlayer().getFactors().addFactor(Factor.fromEndTime(FactorType.AsteroidYield, FactorSource.Holiday, 30f, endTime));
        }
        this.user().send(writer.writeFactors(player.getFactors()));


        player.getCounterFacade().initAllUpdate();
        this.user().send(writer.writeCounters(player.getCounterFacade().counters()));
        this.user().send(writer.writeAllContainerItems(player.getHold()));
        this.user().send(writer.writeAllContainerItems(player.getLocker()));
        this.user().send(writer.writeMissions(player.getCounterFacade().missionBook()));
        final SettingProtocol settingProtocol = this.user().getProtocol(ProtocolID.Setting);
        settingProtocol.sendSettings();
        final CommunityProtocol communityProtocol = this.user().getProtocol(ProtocolID.Community);
        final Optional<Guild> optFetchedGuild = guildRegistry.getGuildOfPlayerID(player.getUserID());
        optFetchedGuild.ifPresent(g -> user().getPlayer().setGuild(g));

        final Optional<Guild> optGuild = user().getPlayer().getGuild();
        optGuild.ifPresent(guild -> user().send(communityProtocol.writer().writeGuildInfo(new GuildInfoMessage(guild))));
        final Optional<IParty> optParty = user().getPlayer().getParty();
        optParty.ifPresent(party -> user().send(communityProtocol.writer().writeParty(party)));
        final SceneProtocol sceneProtocol = user().getProtocol(ProtocolID.Scene);



        sceneProtocol.sendLoadNextScene();
    }
}
