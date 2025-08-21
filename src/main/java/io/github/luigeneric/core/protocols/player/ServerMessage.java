package io.github.luigeneric.core.protocols.player;

import java.util.HashMap;
import java.util.Map;

public enum ServerMessage
{
    Reset(1),
    PlayerInfo(2),
    Skills(3),
    Missions(4),
    RemoveMissions(5),
    Duties(6),
    HoldItems(7),
    RemoveHoldItems(8),
    LockerItems(9),
    RemoveLockerItems(10),
    ShipInfo(11),
    Slots(12),
    Stickers(13),
    RemoveStickers(14),
    AddShip(15),
    RemoveShip(16),
    ActiveShip(17),
    ShipName(19),
    NameAvailable(20),
    NameNotAvailable(21),
    ID(22),
    Name(23),
    Faction(24),
    Experience(25),
    SpentExperience(26),
    Level(27),
    NormalExperience(28),
    Avatar(29),
    Loot(30),
    RemoveLootItems(31),
    Stats(32),
    PaymentInfo(34),
    Counters(35),
    Title(36),
    ResetDuties(37),
    @Deprecated
    AllowFactionSwitch(38),
    Factors(39),
    RemoveFactors(40),
    Mail(41),
    RemoveMail(42),
    Capability(43),
    AnswerUserBonus(44),
    FactorModify(45),
    UpdatePopupSeenList(50),
    CannotStackBoosters(51),
    Anchor(52),
    Unanchor(53),
    CarrierDradis(54),
    HoldOverflow(55),
    @Deprecated
    SettingsInfo(56),
    ActivateOnByDefaultSlots(59),
    WaterExchangeValues(60),
    BonusMapParts(61),
    Statistics(62),
    CharacterServices(63),
    FactionChangeSuccess(64),
    NameChangeSuccess(65),
    AvatarChangeSuccess(66),
    CharacterServiceError(67),
    ResourceHardcap(68);

    public final int value;
    private static final Map<Integer, ServerMessage> map = new HashMap<>();

    ServerMessage(final int value)
    {
        this.value = value;
    }

    static
    {
        for (final ServerMessage pageType : ServerMessage.values())
        {
            map.put(pageType.value, pageType);
        }
    }

    public static ServerMessage valueOf(int pageType)
    {
        return map.get(pageType);
    }

    public int getValue()
    {
        return value;
    }
}
