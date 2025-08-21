package io.github.luigeneric.core.protocols.player;

import java.util.HashMap;
import java.util.Map;

enum ClientMessage
{
    MoveItem(1),
    BuySkill(2),
    SelectTitle(3),
    BindSticker(4),
    SelectConsumable(5),
    UnbindSticker(6),
    AddShip(7),
    RemoveShip(8),
    SelectShip(9),
    UpgradeShip(10),
    RepairSystem(11),
    RepairShip(12),
    ScrapShip(13),
    CreateAvatar(14),
    SelectFaction(15),
    UpgradeSystem(16),
    SetShipName(17),
    UseAugment(18),
    ChooseDailyBonus(20),
    UpgradeSystemByPack(21),
    MoveAll(22),
    ReadMail(23),
    RemoveMail(24),
    MailAction(25),
    RepairAll(26),
    CheckNameAvailability(28),
    PopupSeen(30),
    ChooseName(32),
    CreateAvatarFactionChange(33),
    InstantSkillBuy(35),
    ReduceSkillLearnTime(36),
    SubmitMission(37),
    AugmentMassActivation(38),
    SendDradisData(39),
    RequestCharacterServices(40),
    ChangeFaction(41),
    ChangeName(42),
    ChangeAvatar(43),
    ResourceHardcap(44),
    DeselectTitle(45);

    public final int value;
    private static final Map<Integer, ClientMessage> map = new HashMap<>();

    ClientMessage(int value)
    {
        this.value = value;
    }

    static
    {
        for (final ClientMessage pageType : ClientMessage.values())
        {
            map.put(pageType.value, pageType);
        }
    }

    public static ClientMessage valueOf(int pageType)
    {
        return (ClientMessage) map.get(pageType);
    }

    public int getValue()
    {
        return value;
    }
}
