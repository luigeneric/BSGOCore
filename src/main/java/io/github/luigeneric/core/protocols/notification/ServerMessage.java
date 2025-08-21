package io.github.luigeneric.core.protocols.notification;

import java.util.HashMap;
import java.util.Map;

enum ServerMessage
{
    Message((short)1),
    MiningShipUnderAttack(2),
    OutpostAttacked(5),
    HeavyFight(6),
    Experience(7),
    DutyUpdated(8),
    OreMined(9),
    Reward(10),
    MissionCompleted(11),
    DailyLoginBonus(12),
    SystemUpgradeResult(13),
    EmergencyMessage(14),
    AugmentItem(15),
    @Deprecated
    DeathPaymentBonus(16),
    JumpBeaconAttacked(19),
    MonthlyShipSale(20),
    SectorEventReward(21),
    SectorEventState(22),
    SectorEventTasks(23),
    FtlMissionsOff(24),
    ErrorMessage(25),
    SectorFortificationLevel(26),
    LootMessage(27),
    InboxMailLimit(28),
    JumpNotification(29),
    ConversionCampaignOffer(30);

    public static final int SIZE = Short.SIZE;

    public final short shortValue;

    ServerMessage(final int i)
    {
        this((short) i);
    }

    private static final class MappingsHolder
    {
        private static final Map<Short, ServerMessage> mappings = new HashMap<>();
    }

    private static Map<Short, ServerMessage> getMappings()
    {
        return MappingsHolder.mappings;
    }

    ServerMessage(short value)
    {
        shortValue = value;
        getMappings().put(value, this);
    }

    public short getValue()
    {
        return shortValue;
    }

    public static ServerMessage forValue(short value)
    {
        return getMappings().get(value);
    }
}
