package io.github.luigeneric.core.protocols.setting;


import io.github.luigeneric.binaryreaderwriter.BgoProtocolReader;
import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.core.ProtocolContext;
import io.github.luigeneric.core.player.HelpScreenType;
import io.github.luigeneric.core.player.Player;
import io.github.luigeneric.core.player.settings.*;
import io.github.luigeneric.core.player.settings.values.UserSettingBoolean;
import io.github.luigeneric.core.protocols.BgoProtocol;
import io.github.luigeneric.core.protocols.ProtocolID;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class SettingProtocol extends BgoProtocol
{
    private final SettingProtocolWriteOnly writer;
    public SettingProtocol(ProtocolContext ctx)
    {
        super(ProtocolID.Setting, ctx);
        this.writer = new SettingProtocolWriteOnly();
    }

    public SettingProtocolWriteOnly writer()
    {
        return writer;
    }

    @Override
    public void parseMessage(final int msgType, final BgoProtocolReader br) throws IOException
    {
        final ClientMessage clientMessage = ClientMessage.valueOf(msgType);
        if (clientMessage == null)
        {
            log.warn("ClientMessage was null in SettingProtocol " + user().getPlayer().getPlayerLog());
            return;
        }
        //Log.serverInfo("SettingProtocol: " + clientMessage.name());
        final Player player = user().getPlayer();
        switch (clientMessage)
        {
            case SaveSettings ->
            {
                //final Map<UserSetting, Object> readSet = readSettings(br);
                final Settings settings = player.getSettings();
                settings.getServerSavedUserSettings().read(br);
            }
            case SaveKeys ->
            {
                final List<InputBinding> controls = readControls(br);
                final Settings settings = player.getSettings();
                log.info(user().getUserLog() + "UserKeys: " + controls.size() + " " + controls);
                settings.getInputBindings().set(controls);
                //settings.setControls(controls);
            }
            case SetFullScreen ->
            {
                final Settings settings = player.getSettings();
                //settings.setFullscreen(true);
                settings.getServerSavedUserSettings().put(UserSetting.Fullscreen, new UserSettingBoolean(true));
            }
            case SetSyfyShip ->
            {
                final boolean isOn = br.readBoolean();
                log.warn(user().getUserLog() + "Received set syfy with stat: " + isOn);
            }
            default -> log.error(user().getUserLog() + "Unknown messageType in SettingProtocol: " + msgType);
        }
    }



    private List<InputBinding> readControls(final BgoProtocolReader br) throws IOException
    {
        int count = br.readLength();
        List<InputBinding> inputBindings = new ArrayList<>(count);
        for (int i = 0; i < count; i++)
        {
            try
            {
                InputBinding inputBinding = br.readDesc(InputBinding.class);
                inputBindings.add(inputBinding);
            } catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        return inputBindings;
    }

    public static UserSettingValueType getValueType(final UserSetting setting)
    {
        switch (setting)
        {
            case CompletedTutorials:
                return UserSettingValueType.HelpScreenType;
            case LootPanelPosition:
            case InventoryPanelPosition:
                return UserSettingValueType.Float2;
            case MusicVolume:
            case SoundVolume:
            case ViewDistance:
            case FlakFieldDensity:
            case DeadZoneMouse:
            case DeadZoneJoystick:
            case SensitivityJoystick:
            case CameraZoom:
            case HudIndicatorMinimizeDistance:
            case HudIndicatorTextSize:
            case HudIndicatorDescriptionDisplayDistance:
                return UserSettingValueType.Float;
            case MouseWheelBinding:
            case SystemMap3DTransitionMode:
            case SystemMap3DCameraView:
            case HudIndicatorColorScheme:
                return UserSettingValueType.Byte;
            case CameraMode:
            case GraphicsQuality:
            case Layout:
            case AntiAliasing:
            case FogQuality:
                return UserSettingValueType.Integer;
            case CombatGui:
            case ShowTutorial:
            case InvertedVertical:
            case Fullscreen:
            case StatsIndication:
            case HudIndicatorShowShipNames:
            case AssignmentsCollapsed:
            case ShowStarDust:
            case ShowStarFog:
            case ShowGlowEffect:
            case ShowChangeFaction:
            case ChatShowPrefix:
            case ChatViewLocal:
            case ChatViewGlobal:
            case AutoLoot:
            case Fullframe:
            case ShowPopups:
            case ShowOutpostMessages:
            case ShowHeavyFightingMessages:
            case ShowAugmentMessages:
            case ShowMiningShipMessages:
            case ShowExperienceMessages:
            case HudIndicatorShowWingNames:
            case AdvancedFlightControls:
            case UseProceduralTextures:
            case ShowFpsAndPing:
            case ShowBulletImpactFx:
            case CombatText:
            case ShowEnemyIndication:
            case ShowFriendIndication:
            case HudIndicatorShowMissionArrow:
            case AutomaticAmmoReload:
            case ShowWofConfirmation:
            case JoystickGamepadEnabled:
            case ShowXbox360Buttons:
            case HudIndicatorShowTitles:
            case HighResModels:
            case HighResTextures:
            case HighQualityParticles:
            case AnisotropicFiltering:
            case ShowCutscenes:
            case MuteSound:
            case ShowDamageOverlay:
            case ShowShipSkins:
            case ShowWeaponModules:
            case SystemMap3DShowAsteroids:
            case SystemMap3DShowDynamicMissions:
            case SystemMap3DFormAsteroidGroups:
            case HudIndicatorShowTargetNames:
            case HudIndicatorShowShipTierIcon:
            case HudIndicatorBracketResizing:
            case HudIndicatorSelectionCrosshair:
            case HudIndicatorHealthBar:
            case ShowAssignmentMessages:
            case ShowXpBar:
            case VSync:
            case FramerateCapping:
                return UserSettingValueType.Boolean;

            default:
                log.warn("UserSettingValueType: no value for type " + setting + " found, assuming bool");
                return UserSettingValueType.Boolean;
        }
    }


    public void sendSettings()
    {
        final Player player = user().getPlayer();
        final UserSettings serverSavedSettings = player.getSettings().getServerSavedUserSettings();
        final BgoProtocolWriter buffer = writer.writeSettings(serverSavedSettings);
        user().send(writer.writeInputBindings(player.getSettings().getInputBindings()));
        user().send(buffer);

    }

    private Map<UserSetting, Object> readSettings(final BgoProtocolReader br) throws IOException
    {
        int num = br.readLength();
        final Map<UserSetting, Object> values = new HashMap<>();
        for (int i = 0; i < num; i++)
        {
            UserSetting userSetting = UserSetting.forValue(br.readByte());
            UserSettingValueType valueType = UserSettingValueType.forValue(br.readByte());

            switch (valueType)
            {
                case Float ->
                {
                    float f = br.readSingle();
                    values.put(userSetting, f);
                }
                case Boolean ->
                {
                    boolean b = br.readBoolean();
                    values.put(userSetting, b);
                }
                case Integer ->
                {
                    int int32 = br.readInt32();
                    values.put(userSetting, int32);
                }
                case Float2 ->
                {
                    final float f1 = br.readSingle();
                    final float f2 = br.readSingle();
                    final float[] fs = new float[2];
                    fs[0] = f1;
                    fs[1] = f2;
                    values.put(userSetting, fs);
                }
                case HelpScreenType ->
                {
                    int numToRead = br.readLength();
                    List<HelpScreenType> helpScreenTypes = new ArrayList<>(numToRead);
                    for (int j = 0; j < numToRead; j++)
                    {
                        helpScreenTypes.add(HelpScreenType.forValue(br.readUint16()));
                    }
                    values.put(userSetting, helpScreenTypes);
                }
                case Byte ->
                {
                    byte b = br.readByte();
                    values.put(userSetting, b);
                }
                default ->
                {
                    byte unknownDefault =  br.readByte();
                }
            }
        }

        return values;
    }

    enum ClientMessage
    {
        SaveSettings(1),
        SaveKeys(2),
        SetSyfyShip(5),
        SetFullScreen(6);

        public final int value;
        private static final Map<Integer, ClientMessage> map = new HashMap<>();

        ClientMessage(int value)
        {
            this.value = value;
        }

        static
        {
            for (ClientMessage pageType : ClientMessage.values())
            {
                map.put(pageType.value, pageType);
            }
        }

        public static ClientMessage valueOf(int pageType)
        {
            return map.get(pageType);
        }
    }


}
