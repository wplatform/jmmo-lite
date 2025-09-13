package com.github.azeroth.game.entity.gobject.model;

import com.github.azeroth.world.entities.gobject.enums.GameObjectTypes;

public class GameObjectTemplate {

    int entry;
    GameObjectTypes type;
    int displayId;
    String name;
    String iconName;
    String castBarCaption;
    String unk1;
    float size;
    int requiredLevel;
    // 0 GAMEOBJECT_TYPE_DOOR
    Door door;
    Button button;
    QuestGiver questgiver;
    Chest chest;
    Binder binder;
    Generic generic;
    Trap trap;
    Chair chair;
    SpellFocus spellFocus;
    Text text;
    Goober goober;
    Transport transport;
    AreaDamage areaDamage;
    Camera camera;
    Mapobject mapobject;
    MoTransport moTransport;
    DuelFlag duelFlag;
    FishingNode fishingNode;
    Ritual ritual;
    Mailbox mailbox;
    DoNotUse DONOTUSE;
    GuardPost guardPost;
    SpellCaster spellCaster;
    MeetingStone meetingStone;
    FlagStand flagStand;
    FishingHole fishingHole;
    FlagDrop flagDrop;
    MiniGame miniGame;
    DoNotUse DONOTUSE2;
    ControlZone controlZone;
    AuraGenerator auraGenerator;
    DungeonDifficulty dungeonDifficulty;
    BarberChair barberChair;
    DestructibleBuilding destructibleBuilding;
    GuildBank guildbank;
    TrapDoor trapdoor;
    NewFlag newflag;
    NewFlagDrop newflagdrop;
    GarrisonBuilding garrisonBuilding;
    GarrisonPlot garrisonPlot;
    ClientCreature clientCreature;
    ClientItem clientItem;
    CapturePoint capturePoint;
    PhaseableMO phaseableMO;
    GarrisonMonument garrisonMonument;
    GarrisonShipment garrisonShipment;
    GarrisonMonumentPlaque garrisonMonumentPlaque;
    ItemForge itemForge;
    UILink UILink;
    KeystoneReceptacle keystoneReceptacle;
    GatheringNode gatheringNode;
    ChallengeModeReward challengeModeReward;
    String aiName;
    int scriptId;
    // 1 GAMEOBJECT_TYPE_BUTTON
    String stringId;
    // 2 GAMEOBJECT_TYPE_QUESTGIVER

    // helpers
    boolean isDespawnAtAction() {
        return switch (type) {
            case TYPE_CHEST -> chest.consumable != 0;
            case TYPE_GOOBER -> goober.consumable != 0;
            default -> false;
        };
    }
    // 3 GAMEOBJECT_TYPE_CHEST

    boolean isUsableMounted() {
        return switch (type) {
            case TYPE_MAILBOX -> true;
            case TYPE_BARBER_CHAIR -> false;
            case TYPE_QUESTGIVER -> questgiver.allowMounted != 0;
            case TYPE_TEXT -> text.allowMounted != 0;
            case TYPE_GOOBER -> goober.allowMounted != 0;
            case TYPE_SPELLCASTER -> spellCaster.allowMounted != 0;
            case TYPE_UI_LINK -> UILink.allowMounted != 0;
            default -> false;
        };
    }
    // 4 GAMEOBJECT_TYPE_BINDER

    int getConditionID1() {
        return switch (type) {
            case TYPE_DOOR -> door.conditionID1;
            case TYPE_BUTTON -> button.conditionID1;
            case TYPE_QUESTGIVER -> questgiver.conditionID1;
            case TYPE_CHEST -> chest.conditionID1;
            case TYPE_GENERIC -> generic.conditionID1;
            case TYPE_TRAP -> trap.conditionID1;
            case TYPE_CHAIR -> chair.conditionID1;
            case TYPE_SPELL_FOCUS -> spellFocus.conditionID1;
            case TYPE_TEXT -> text.conditionID1;
            case TYPE_GOOBER -> goober.conditionID1;
            case TYPE_CAMERA -> camera.conditionID1;
            case TYPE_RITUAL -> ritual.conditionID1;
            case TYPE_MAILBOX -> mailbox.conditionID1;
            case TYPE_SPELLCASTER -> spellCaster.conditionID1;
            case TYPE_FLAGSTAND -> flagStand.conditionID1;
            case TYPE_AURA_GENERATOR -> auraGenerator.conditionID1;
            case TYPE_GUILD_BANK -> guildbank.conditionID1;
            case TYPE_NEW_FLAG -> newflag.conditionID1;
            case TYPE_ITEM_FORGE -> itemForge.conditionID1;
            case TYPE_GATHERING_NODE -> gatheringNode.conditionID1;
            default -> 0;
        };
    }
    // 5 GAMEOBJECT_TYPE_GENERIC

    int getInteractRadiusOverride() {
        return switch (type) {
            case TYPE_DOOR -> door.interactRadiusOverride;
            case TYPE_BUTTON -> button.interactRadiusOverride;
            case TYPE_QUESTGIVER -> questgiver.interactRadiusOverride;
            case TYPE_CHEST -> chest.interactRadiusOverride;
            case TYPE_BINDER -> binder.interactRadiusOverride;
            case TYPE_GENERIC -> generic.interactRadiusOverride;
            case TYPE_TRAP -> trap.interactRadiusOverride;
            case TYPE_CHAIR -> chair.interactRadiusOverride;
            case TYPE_SPELL_FOCUS -> spellFocus.interactRadiusOverride;
            case TYPE_TEXT -> text.interactRadiusOverride;
            case TYPE_GOOBER -> goober.interactRadiusOverride;
            case TYPE_TRANSPORT -> transport.interactRadiusOverride;
            case TYPE_AREADAMAGE -> areaDamage.interactRadiusOverride;
            case TYPE_CAMERA -> camera.interactRadiusOverride;
            case TYPE_MAP_OBJ_TRANSPORT -> moTransport.interactRadiusOverride;
            case TYPE_DUEL_ARBITER -> duelFlag.interactRadiusOverride;
            case TYPE_FISHINGNODE -> fishingNode.interactRadiusOverride;
            case TYPE_RITUAL -> ritual.interactRadiusOverride;
            case TYPE_MAILBOX -> mailbox.interactRadiusOverride;
            case TYPE_GUARDPOST -> guardPost.interactRadiusOverride;
            case TYPE_SPELLCASTER -> spellCaster.interactRadiusOverride;
            case TYPE_MEETINGSTONE -> meetingStone.interactRadiusOverride;
            case TYPE_FLAGSTAND -> flagStand.interactRadiusOverride;
            case TYPE_FISHINGHOLE -> fishingHole.interactRadiusOverride;
            case TYPE_FLAGDROP -> flagDrop.interactRadiusOverride;
            case TYPE_CONTROL_ZONE -> controlZone.interactRadiusOverride;
            case TYPE_AURA_GENERATOR -> auraGenerator.interactRadiusOverride;
            case TYPE_DUNGEON_DIFFICULTY -> dungeonDifficulty.interactRadiusOverride;
            case TYPE_BARBER_CHAIR -> barberChair.interactRadiusOverride;
            case TYPE_DESTRUCTIBLE_BUILDING -> destructibleBuilding.interactRadiusOverride;
            case TYPE_GUILD_BANK -> guildbank.interactRadiusOverride;
            case TYPE_TRAPDOOR -> trapdoor.interactRadiusOverride;
            case TYPE_NEW_FLAG -> newflag.interactRadiusOverride;
            case TYPE_NEW_FLAG_DROP -> newflagdrop.interactRadiusOverride;
            case TYPE_GARRISON_BUILDING -> garrisonBuilding.interactRadiusOverride;
            case TYPE_GARRISON_PLOT -> garrisonPlot.interactRadiusOverride;
            case TYPE_CAPTURE_POINT -> capturePoint.interactRadiusOverride;
            case TYPE_PHASEABLE_MO -> phaseableMO.interactRadiusOverride;
            case TYPE_GARRISON_MONUMENT -> garrisonMonument.interactRadiusOverride;
            case TYPE_GARRISON_SHIPMENT -> garrisonShipment.interactRadiusOverride;
            case TYPE_GARRISON_MONUMENT_PLAQUE -> garrisonMonumentPlaque.interactRadiusOverride;
            case TYPE_ITEM_FORGE -> itemForge.interactRadiusOverride;
            case TYPE_UI_LINK -> UILink.interactRadiusOverride;
            case TYPE_KEYSTONE_RECEPTACLE -> keystoneReceptacle.interactRadiusOverride;
            case TYPE_GATHERING_NODE -> gatheringNode.interactRadiusOverride;
            case TYPE_CHALLENGE_MODE_REWARD -> challengeModeReward.interactRadiusOverride;
            default -> 0;
        };
    }
    // 6 GAMEOBJECT_TYPE_TRAP

    int getRequireLOS() {
        return switch (type) {
            case TYPE_BUTTON -> button.requireLOS;
            case TYPE_QUESTGIVER -> questgiver.requireLOS;
            case TYPE_CHEST -> chest.requireLOS;
            case TYPE_TRAP -> trap.requireLOS;
            case TYPE_GOOBER -> goober.requireLOS;
            case TYPE_FLAGSTAND -> flagStand.requireLOS;
            case TYPE_NEW_FLAG -> newflag.requireLOS;
            case TYPE_GATHERING_NODE -> gatheringNode.requireLOS;
            default -> 0;
        };
    }
    // 7 GAMEOBJECT_TYPE_CHAIR

    int getLockId() {
        return switch (type) {
            case TYPE_DOOR -> door.open;
            case TYPE_BUTTON -> button.open;
            case TYPE_QUESTGIVER -> questgiver.open;
            case TYPE_CHEST -> chest.open;
            case TYPE_TRAP -> trap.open;
            case TYPE_GOOBER -> goober.open;
            case TYPE_AREADAMAGE -> areaDamage.open;
            case TYPE_CAMERA -> camera.open;
            case TYPE_FLAGSTAND -> flagStand.open;
            case TYPE_FISHINGHOLE -> fishingHole.open;
            case TYPE_FLAGDROP -> flagDrop.open;
            case TYPE_NEW_FLAG -> newflag.open;
            case TYPE_NEW_FLAG_DROP -> newflagdrop.open;
            case TYPE_CAPTURE_POINT -> capturePoint.open;
            case TYPE_GATHERING_NODE -> gatheringNode.open;
            case TYPE_CHALLENGE_MODE_REWARD -> challengeModeReward.open;
            default -> 0;
        };
    }
    // 8 GAMEOBJECT_TYPE_SPELL_FOCUS

    boolean getDespawnPossibility()                      // despawn at targeting of cast?

    {
        return switch (type) {
            case TYPE_DOOR -> door.noDamageImmune != 0;
            case TYPE_BUTTON -> button.noDamageImmune != 0;
            case TYPE_QUESTGIVER -> questgiver.noDamageImmune != 0;
            case TYPE_GOOBER -> goober.noDamageImmune != 0;
            case TYPE_FLAGSTAND -> flagStand.noDamageImmune != 0;
            case TYPE_FLAGDROP -> flagDrop.noDamageImmune != 0;
            default -> true;
        };
    }
    // 9 GAMEOBJECT_TYPE_TEXT

    // Cannot be used/activated/looted by players under immunity effects (example: Divine Shield)
    int getNoDamageImmune() {
        return switch (type) {
            case TYPE_DOOR -> door.noDamageImmune;
            case TYPE_BUTTON -> button.noDamageImmune;
            case TYPE_QUESTGIVER -> questgiver.noDamageImmune;
            case TYPE_CHEST -> 1;
            case TYPE_GOOBER -> goober.noDamageImmune;
            case TYPE_FLAGSTAND -> flagStand.noDamageImmune;
            case TYPE_FLAGDROP -> flagDrop.noDamageImmune;
            default -> 0;
        };
    }
    // 10 GAMEOBJECT_TYPE_GOOBER

    int getNotInCombat() {
        return switch (type) {
            case TYPE_CHEST -> chest.notInCombat;
            case TYPE_GATHERING_NODE -> gatheringNode.notInCombat;
            default -> 0;
        };
    }
    // 11 GAMEOBJECT_TYPE_TRANSPORT

    int getCharges()                               // despawn at uses amount

    {
        return switch (type) {
            //case GAMEOBJECT_TYPE_TRAP:        return trap.charges;
            case TYPE_GUARDPOST -> guardPost.charges;
            case TYPE_SPELLCASTER -> spellCaster.charges;
            default -> 0;
        };
    }
    // 12 GAMEOBJECT_TYPE_AREADAMAGE

    int getLinkedGameObjectEntry() {
        return switch (type) {
            case TYPE_BUTTON -> button.linkedTrap;
            case TYPE_CHEST -> chest.linkedTrap;
            case TYPE_SPELL_FOCUS -> spellFocus.linkedTrap;
            case TYPE_GOOBER -> goober.linkedTrap;
            case TYPE_GATHERING_NODE -> gatheringNode.linkedTrap;
            default -> 0;
        };
    }
    // 13 GAMEOBJECT_TYPE_CAMERA

    int getAutoCloseTime() {
        return switch (type) {
            case TYPE_DOOR -> door.autoClose;
            case TYPE_BUTTON -> button.autoClose;
            case TYPE_TRAP -> trap.autoClose;
            case TYPE_GOOBER -> goober.autoClose;
            case TYPE_TRANSPORT -> transport.autoClose;
            case TYPE_AREADAMAGE -> areaDamage.autoClose;
            case TYPE_TRAPDOOR -> trapdoor.autoClose;
            default -> 0;
        };
    }
    // 14 GAMEOBJECT_TYPE_MAP_OBJECT

    int getLootId() {
        return switch (type) {
            case TYPE_CHEST -> chest.chestLoot;
            case TYPE_FISHINGHOLE -> fishingHole.chestLoot;
            case TYPE_GATHERING_NODE -> gatheringNode.chestLoot;
            default -> 0;
        };
    }
    // 15 GAMEOBJECT_TYPE_MAP_OBJ_TRANSPORT

    int getGossipMenuId() {
        return switch (type) {
            case TYPE_QUESTGIVER -> questgiver.gossipID;
            case TYPE_GOOBER -> goober.gossipID;
            default -> 0;
        };
    }
    // 16 GAMEOBJECT_TYPE_DUEL_ARBITER

    int getEventScriptId() {
        return switch (type) {
            case TYPE_GOOBER -> goober.eventID;
            case TYPE_CHEST -> chest.triggeredEvent;
            case TYPE_CHAIR -> chair.triggeredEvent;
            case TYPE_CAMERA -> camera.eventID;
            case TYPE_GATHERING_NODE -> gatheringNode.triggeredEvent;
            default -> 0;
        };
    }
    // 17 GAMEOBJECT_TYPE_FISHINGNODE

    int getTrivialSkillHigh() {
        return switch (type) {
            case TYPE_CHEST -> chest.trivialSkillHigh;
            case TYPE_GATHERING_NODE -> gatheringNode.trivialSkillHigh;
            default -> 0;
        };
    }
    // 18 GAMEOBJECT_TYPE_RITUAL

    int getTrivialSkillLow() {
        return switch (type) {
            case TYPE_CHEST -> chest.trivialSkillLow;
            case TYPE_GATHERING_NODE -> gatheringNode.trivialSkillLow;
            default -> 0;
        };
    }
    // 19 GAMEOBJECT_TYPE_MAILBOX

    int getCooldown()                              // Cooldown preventing goober and traps to cast spell

    {
        return switch (type) {
            case TYPE_TRAP -> trap.cooldown;
            case TYPE_GOOBER -> goober.cooldown;
            default -> 0;
        };
    }
    // 20 GAMEOBJECT_TYPE_DO_NOT_USE

    boolean isInfiniteGameObject() {
        return switch (type) {
            case TYPE_DOOR -> door.infiniteAOI != 0;
            case TYPE_FLAGSTAND -> flagStand.infiniteAOI != 0;
            case TYPE_FLAGDROP -> flagDrop.infiniteAOI != 0;
            case TYPE_DESTRUCTIBLE_BUILDING -> true;
            case TYPE_TRAPDOOR -> trapdoor.infiniteAOI != 0;
            case TYPE_NEW_FLAG -> newflag.infiniteAOI != 0;
            case TYPE_GARRISON_BUILDING -> true;
            case TYPE_PHASEABLE_MO -> true;
            default -> false;
        };
    }
    // 21 GAMEOBJECT_TYPE_GUARDPOST

    boolean isGiganticGameObject() {
        return switch (type) {
            case TYPE_DOOR -> door.giganticAOI != 0;
            case TYPE_BUTTON -> button.giganticAOI != 0;
            case TYPE_QUESTGIVER -> questgiver.giganticAOI != 0;
            case TYPE_CHEST -> chest.giganticAOI != 0;
            case TYPE_GENERIC -> generic.giganticAOI != 0;
            case TYPE_TRAP -> trap.giganticAOI != 0;
            case TYPE_SPELL_FOCUS -> spellFocus.giganticAOI != 0;
            case TYPE_GOOBER -> goober.giganticAOI != 0;
            case TYPE_TRANSPORT -> true;
            case TYPE_SPELLCASTER -> spellCaster.giganticAOI != 0;
            case TYPE_FLAGSTAND -> flagStand.giganticAOI != 0;
            case TYPE_FLAGDROP -> flagDrop.giganticAOI != 0;
            case TYPE_CONTROL_ZONE -> controlZone.giganticAOI != 0;
            case TYPE_DUNGEON_DIFFICULTY -> dungeonDifficulty.giganticAOI != 0;
            case TYPE_TRAPDOOR -> trapdoor.giganticAOI != 0;
            case TYPE_NEW_FLAG -> newflag.giganticAOI != 0;
            case TYPE_GARRISON_PLOT -> true;
            case TYPE_CAPTURE_POINT -> capturePoint.giganticAOI != 0;
            case TYPE_GARRISON_SHIPMENT -> garrisonShipment.giganticAOI != 0;
            case TYPE_UI_LINK -> UILink.giganticAOI != 0;
            case TYPE_GATHERING_NODE -> gatheringNode.giganticAOI != 0;
            default -> false;
        };
    }
    // 22 GAMEOBJECT_TYPE_SPELLCASTER

    boolean isLargeGameObject() {
        return switch (type) {
            case TYPE_CHEST -> chest.largeAOI != 0;
            case TYPE_GENERIC -> generic.largeAOI != 0;
            case TYPE_GOOBER -> goober.largeAOI != 0;
            case TYPE_DUNGEON_DIFFICULTY -> dungeonDifficulty.largeAOI != 0;
            case TYPE_GARRISON_SHIPMENT -> garrisonShipment.largeAOI != 0;
            case TYPE_ITEM_FORGE -> itemForge.largeAOI != 0;
            case TYPE_GATHERING_NODE -> gatheringNode.largeAOI != 0;
            default -> false;
        };
    }
    // 23 GAMEOBJECT_TYPE_MEETINGSTONE

    int getServerOnly() {
        return switch (type) {
            case TYPE_GENERIC -> generic.serverOnly;
            case TYPE_TRAP -> trap.serverOnly;
            case TYPE_SPELL_FOCUS -> spellFocus.serverOnly;
            case TYPE_AURA_GENERATOR -> auraGenerator.serverOnly;
            default -> 0;
        };
    }
    // 24 GAMEOBJECT_TYPE_FLAGSTAND

    int getSpellFocusType() {
        return switch (type) {
            case TYPE_SPELL_FOCUS -> spellFocus.spellFocusType;
            case TYPE_UI_LINK -> UILink.spellFocusType;
            default -> 0;
        };
    }
    // 25 GAMEOBJECT_TYPE_FISHINGHOLE

    int getSpellFocusRadius() {
        return switch (type) {
            case TYPE_SPELL_FOCUS -> spellFocus.radius;
            case TYPE_UI_LINK -> UILink.radius;
            default -> 0;
        };
    }
    // 26 GAMEOBJECT_TYPE_FLAGDROP

    public static class Door {
        int startOpen;                               // 0 startOpen, enum { false, true, }; Default: false
        int open;                                    // 1 open, References: Lock_, NOVALUE = 0
        int autoClose;                               // 2 autoClose (ms), int, Min value: 0, Max value: 2147483647, Default value: 3000
        int noDamageImmune;                          // 3 noDamageImmune, enum { false, true, }; Default: false
        int openTextID;                              // 4 openTextID, References: BroadcastText, NOVALUE = 0
        int closeTextID;                             // 5 closeTextID, References: BroadcastText, NOVALUE = 0
        int IgnoredByPathing;                        // 6 Ignored By Pathing, enum { false, true, }; Default: false
        int conditionID1;                            // 7 conditionID1, References: PlayerCondition, NOVALUE = 0
        int doorisOpaque;                            // 8 Door is Opaque (Disable portal on close), enum { false, true, }; Default: true
        int giganticAOI;                             // 9 Gigantic AOI, enum { false, true, }; Default: false
        int infiniteAOI;                             // 10 Infinite AOI, enum { false, true, }; Default: false
        int notLOSBlocking;                          // 11 Not LOS Blocking, enum { false, true, }; Default: false
        int interactRadiusOverride;                  // 12 Interact Radius override (in hundredths), int, Min value: 0, Max value: 2147483647, Default value: 0
        int collisionupdatedelayafteropen;           // 13 Collision update delay(ms) after open, int, Min value: 0, Max value: 2147483647, Default value: 0
    }
    // 27 GAMEOBJECT_TYPE_MINI_GAME

    public static class Button {
        int startOpen;                               // 0 startOpen, enum { false, true, }; Default: false
        int open;                                    // 1 open, References: Lock_, NOVALUE = 0
        int autoClose;                               // 2 autoClose (ms), int, Min value: 0, Max value: 2147483647, Default value: 3000
        int linkedTrap;                              // 3 linkedTrap, References: gameObjects, NOVALUE = 0
        int noDamageImmune;                          // 4 noDamageImmune, enum { false, true, }; Default: false
        int giganticAOI;                             // 5 Gigantic AOI, enum { false, true, }; Default: false
        int openTextID;                              // 6 openTextID, References: BroadcastText, NOVALUE = 0
        int closeTextID;                             // 7 closeTextID, References: BroadcastText, NOVALUE = 0
        int requireLOS;                              // 8 require LOS, enum { false, true, }; Default: false
        int conditionID1;                            // 9 conditionID1, References: PlayerCondition, NOVALUE = 0
        int interactRadiusOverride;                  // 10 Interact Radius override (in hundredths), int, Min value: 0, Max value: 2147483647, Default value: 0
    }
    // 28 GAMEOBJECT_TYPE_DO_NOT_USE_2

    public static class QuestGiver {
        int open;                                    // 0 open, References: Lock_, NOVALUE = 0
        int questGiver;                              // 1 questGiver, References: questGiver, NOVALUE = 0
        int pageMaterial;                            // 2 pageMaterial, References: PageTextMaterial, NOVALUE = 0
        int gossipID;                                // 3 gossipID, References: Gossip, NOVALUE = 0
        int customAnim;                              // 4 customAnim, int, Min value: 0, Max value: 4, Default value: 0
        int noDamageImmune;                          // 5 noDamageImmune, enum { false, true, }; Default: false
        int openTextID;                              // 6 openTextID, References: BroadcastText, NOVALUE = 0
        int requireLOS;                              // 7 require LOS, enum { false, true, }; Default: false
        int allowMounted;                            // 8 allowMounted, enum { false, true, }; Default: false
        int giganticAOI;                             // 9 Gigantic AOI, enum { false, true, }; Default: false
        int conditionID1;                            // 10 conditionID1, References: PlayerCondition, NOVALUE = 0
        int neverUsableWhileMounted;                 // 11 Never Usable While Mounted, enum { false, true, }; Default: false
        int interactRadiusOverride;                  // 12 Interact Radius override (in hundredths), int, Min value: 0, Max value: 2147483647, Default value: 0
    }
    // 29 GAMEOBJECT_TYPE_CONTROL_ZONE

    public static class Chest {
        int open;                                    // 0 open, References: Lock_, NOVALUE = 0
        int chestLoot;                               // 1 chestLoot (legacy/classic), References: treasure, NOVALUE = 0
        int chestRestockTime;                        // 2 chestRestockTime, int, Min value: 0, Max value: 1800000, Default value: 0
        int consumable;                              // 3 consumable, enum { false, true, }; Default: false
        int minRestock;                              // 4 minRestock, int, Min value: 0, Max value: 65535, Default value: 0
        int maxRestock;                              // 5 maxRestock, int, Min value: 0, Max value: 65535, Default value: 0
        int triggeredEvent;                          // 6 triggeredEvent, References: GameEvents, NOVALUE = 0
        int linkedTrap;                              // 7 linkedTrap, References: gameObjects, NOVALUE = 0
        int questID;                                 // 8 questID, References: QuestV2, NOVALUE = 0
        int interactRadiusOverride;                  // 9 Interact Radius override (in hundredths), int, Min value: 0, Max value: 2147483647, Default value: 0
        int requireLOS;                              // 10 require LOS, enum { false, true, }; Default: false
        int leaveLoot;                               // 11 leaveLoot, enum { false, true, }; Default: false
        int notInCombat;                             // 12 notInCombat, enum { false, true, }; Default: false
        int logloot;                                 // 13 log loot, enum { false, true, }; Default: false
        int openTextID;                              // 14 openTextID, References: BroadcastText, NOVALUE = 0
        int usegrouplootrules;                       // 15 use group loot rules, enum { false, true, }; Default: false
        int floatingTooltip;                         // 16 floatingTooltip, enum { false, true, }; Default: false
        int conditionID1;                            // 17 conditionID1, References: PlayerCondition, NOVALUE = 0
        int unused;                                   // 18 unused, int, Min value: -2147483648, Max value: 2147483647, Default value: 0
        int xpDifficulty;                            // 19 xpDifficulty, enum { No exp, Trivial, Very Small, Small, Substandard, Standard, High, Epic, Dungeon, 5, }; Default: No Exp
        int unused2;                                 // 20 unused, int, Min value: 0, Max value: 123, Default value: 0
        int groupXP;                                 // 21 Group XP, enum { false, true, }; Default: false
        int damageImmuneOK;                          // 22 Damage Immune OK, enum { false, true, }; Default: false
        int trivialSkillLow;                         // 23 trivialSkillLow, int, Min value: 0, Max value: 65535, Default value: 0
        int trivialSkillHigh;                        // 24 trivialSkillHigh, int, Min value: 0, Max value: 65535, Default value: 0
        int dungeonEncounter;                        // 25 Dungeon Encounter, References: dungeonEncounter, NOVALUE = 0
        int spell;                                   // 26 spell, References: spell, NOVALUE = 0
        int giganticAOI;                             // 27 Gigantic AOI, enum { false, true, }; Default: false
        int largeAOI;                                // 28 Large AOI, enum { false, true, }; Default: false
        int spawnVignette;                           // 29 Spawn Vignette, References: vignette, NOVALUE = 0
        int chestPersonalLoot;                       // 30 chest Personal loot, References: treasure, NOVALUE = 0
        int turnpersonallootsecurityoff;             // 31 turn personal loot security off, enum { false, true, }; Default: false
        int chestProperties;                         // 32 Chest Properties, References: chestProperties, NOVALUE = 0
        int chestPushLoot;                           // 33 chest Push loot, References: treasure, NOVALUE = 0
        int forceSingleLooter;                       // 34 Force Single Looter, enum { false, true, }; Default: false
    }
    // 30 GAMEOBJECT_TYPE_AURA_GENERATOR

    public static class Binder {
        int interactRadiusOverride;                  // 0 Interact Radius override (in hundredths), int, Min value: 0, Max value: 2147483647, Default value: 0
    }
    // 31 GAMEOBJECT_TYPE_DUNGEON_DIFFICULTY

    public static class Generic {
        int floatingTooltip;                         // 0 floatingTooltip, enum { false, true, }; Default: false
        int highlight;                               // 1 highlight, enum { false, true, }; Default: true
        int serverOnly;                              // 2 serverOnly, enum { false, true, }; Default: false
        int giganticAOI;                             // 3 Gigantic AOI, enum { false, true, }; Default: false
        int floatOnWater;                            // 4 floatOnWater, enum { false, true, }; Default: false
        int questID;                                 // 5 questID, References: QuestV2, NOVALUE = 0
        int conditionID1;                            // 6 conditionID1, References: PlayerCondition, NOVALUE = 0
        int largeAOI;                                // 7 Large AOI, enum { false, true, }; Default: false
        int useGarrisonOwnerGuildColors;             // 8 Use Garrison Owner Guild Colors, enum { false, true, }; Default: false
        int interactRadiusOverride;                  // 9 Interact Radius override (in hundredths), int, Min value: 0, Max value: 2147483647, Default value: 0
    }
    // 32 GAMEOBJECT_TYPE_BARBER_CHAIR

    public static class Trap {
        int open;                                    // 0 open, References: Lock_, NOVALUE = 0
        int unused;                                  // 1 unused, int, Min value: 0, Max value: 65535, Default value: 0
        int radius;                                  // 2 radius, int, Min value: 0, Max value: 100, Default value: 0
        int spell;                                   // 3 spell, References: spell, NOVALUE = 0
        int charges;                                 // 4 charges, int, Min value: 0, Max value: 65535, Default value: 1
        int cooldown;                                // 5 cooldown, int, Min value: 0, Max value: 65535, Default value: 0
        int autoClose;                               // 6 autoClose (ms), int, Min value: 0, Max value: 2147483647, Default value: 0
        int startDelay;                              // 7 startDelay, int, Min value: 0, Max value: 65535, Default value: 0
        int serverOnly;                              // 8 serverOnly, enum { false, true, }; Default: false
        int stealthed;                               // 9 stealthed, enum { false, true, }; Default: false
        int giganticAOI;                             // 10 Gigantic AOI, enum { false, true, }; Default: false
        int stealthAffected;                         // 11 stealthAffected, enum { false, true, }; Default: false
        int openTextID;                              // 12 openTextID, References: BroadcastText, NOVALUE = 0
        int closeTextID;                             // 13 closeTextID, References: BroadcastText, NOVALUE = 0
        int ignoreTotems;                            // 14 Ignore totems, enum { false, true, }; Default: false
        int conditionID1;                            // 15 conditionID1, References: PlayerCondition, NOVALUE = 0
        int playerCast;                              // 16 playerCast, enum { false, true, }; Default: false
        int summonerTriggered;                       // 17 Summoner Triggered, enum { false, true, }; Default: false
        int requireLOS;                              // 18 require LOS, enum { false, true, }; Default: false
        int triggerCondition;                        // 19 Trigger condition, References: PlayerCondition, NOVALUE = 0
        int checkallunits;                           // 20 Check all units (spawned traps only check players), enum { false, true, }; Default: false
        int interactRadiusOverride;                  // 21 Interact Radius override (in hundredths), int, Min value: 0, Max value: 2147483647, Default value: 0
    }
    // 33 GAMEOBJECT_TYPE_DESTRUCTIBLE_BUILDING

    public static class Chair {
        int chairslots;                              // 0 chairslots, int, Min value: 1, Max value: 5, Default value: 1
        int chairheight;                             // 1 chairheight, int, Min value: 0, Max value: 2, Default value: 1
        int onlyCreatorUse;                          // 2 onlyCreatorUse, enum { false, true, }; Default: false
        int triggeredEvent;                          // 3 triggeredEvent, References: GameEvents, NOVALUE = 0
        int conditionID1;                            // 4 conditionID1, References: PlayerCondition, NOVALUE = 0
        int interactRadiusOverride;                  // 5 Interact Radius override (in hundredths), int, Min value: 0, Max value: 2147483647, Default value: 0
    }
    // 34 GAMEOBJECT_TYPE_GUILD_BANK

    public static class SpellFocus {
        int spellFocusType;                          // 0 spellFocusType, References: SpellFocusObject, NOVALUE = 0
        int radius;                                  // 1 radius, int, Min value: 0, Max value: 50, Default value: 10
        int linkedTrap;                              // 2 linkedTrap, References: gameObjects, NOVALUE = 0
        int serverOnly;                              // 3 serverOnly, enum { false, true, }; Default: false
        int questID;                                 // 4 questID, References: QuestV2, NOVALUE = 0
        int giganticAOI;                             // 5 Gigantic AOI, enum { false, true, }; Default: false
        int floatingTooltip;                         // 6 floatingTooltip, enum { false, true, }; Default: false
        int floatOnWater;                            // 7 floatOnWater, enum { false, true, }; Default: false
        int conditionID1;                            // 8 conditionID1, References: PlayerCondition, NOVALUE = 0
        int interactRadiusOverride;                  // 9 Interact Radius override (in hundredths), int, Min value: 0, Max value: 2147483647, Default value: 0
        int gossipID;                                // 10 gossipID, References: Gossip, NOVALUE = 0
        int spellFocusType2;                         // 11 spellFocusType 2, References: SpellFocusObject, NOVALUE = 0
        int spellFocusType3;                         // 12 spellFocusType 3, References: SpellFocusObject, NOVALUE = 0
        int spellFocusType4;                         // 13 spellFocusType 4, References: SpellFocusObject, NOVALUE = 0
        int profession;                              // 14 profession, enum { First Aid, Blacksmithing, Leatherworking, Alchemy, Herbalism, Cooking, Mining, Tailoring, Engineering, Enchanting, FISHING, SKINNING, Jewelcrafting, Inscription, Archaeology, }; Default: Blacksmithing
        int profession2;                             // 15 Profession 2, enum { First Aid, Blacksmithing, Leatherworking, Alchemy, Herbalism, Cooking, Mining, Tailoring, Engineering, Enchanting, FISHING, SKINNING, Jewelcrafting, Inscription, Archaeology, }; Default: Blacksmithing
        int profession3;                             // 16 Profession 3, enum { First Aid, Blacksmithing, Leatherworking, Alchemy, Herbalism, Cooking, Mining, Tailoring, Engineering, Enchanting, FISHING, SKINNING, Jewelcrafting, Inscription, Archaeology, }; Default: Blacksmithing
    }
    // 35 GAMEOBJECT_TYPE_TRAPDOOR

    public static class Text {
        int pageID;                                  // 0 pageID, References: PageText, NOVALUE = 0
        int language;                                // 1 language, References: Languages, NOVALUE = 0
        int pageMaterial;                            // 2 pageMaterial, References: PageTextMaterial, NOVALUE = 0
        int allowMounted;                            // 3 allowMounted, enum { false, true, }; Default: false
        int conditionID1;                            // 4 conditionID1, References: PlayerCondition, NOVALUE = 0
        int neverUsableWhileMounted;                 // 5 Never Usable While Mounted, enum { false, true, }; Default: false
        int interactRadiusOverride;                  // 6 Interact Radius override (in hundredths), int, Min value: 0, Max value: 2147483647, Default value: 0
    }
    // 36 GAMEOBJECT_TYPE_NEW_FLAG

    public static class Goober {
        int open;                                    // 0 open, References: Lock_, NOVALUE = 0
        int questID;                                 // 1 questID, References: QuestV2, NOVALUE = 0
        int eventID;                                 // 2 eventID, References: GameEvents, NOVALUE = 0
        int autoClose;                               // 3 autoClose (ms), int, Min value: 0, Max value: 2147483647, Default value: 3000
        int customAnim;                              // 4 customAnim, int, Min value: 0, Max value: 4, Default value: 0
        int consumable;                              // 5 consumable, enum { false, true, }; Default: false
        int cooldown;                                // 6 cooldown, int, Min value: 0, Max value: 65535, Default value: 0
        int pageID;                                  // 7 pageID, References: PageText, NOVALUE = 0
        int language;                                // 8 language, References: Languages, NOVALUE = 0
        int pageMaterial;                            // 9 pageMaterial, References: PageTextMaterial, NOVALUE = 0
        int spell;                                   // 10 spell, References: spell, NOVALUE = 0
        int noDamageImmune;                          // 11 noDamageImmune, enum { false, true, }; Default: false
        int linkedTrap;                              // 12 linkedTrap, References: gameObjects, NOVALUE = 0
        int giganticAOI;                             // 13 Gigantic AOI, enum { false, true, }; Default: false
        int openTextID;                              // 14 openTextID, References: BroadcastText, NOVALUE = 0
        int closeTextID;                             // 15 closeTextID, References: BroadcastText, NOVALUE = 0
        int requireLOS;                              // 16 require LOS, enum { false, true, }; Default: false
        int allowMounted;                            // 17 allowMounted, enum { false, true, }; Default: false
        int floatingTooltip;                         // 18 floatingTooltip, enum { false, true, }; Default: false
        int gossipID;                                // 19 gossipID, References: Gossip, NOVALUE = 0
        int allowMultiInteract;                      // 20 Allow Multi-Interact, enum { false, true, }; Default: false
        int floatOnWater;                            // 21 floatOnWater, enum { false, true, }; Default: false
        int conditionID1;                            // 22 conditionID1, References: PlayerCondition, NOVALUE = 0
        int playerCast;                              // 23 playerCast, enum { false, true, }; Default: false
        int spawnVignette;                           // 24 Spawn Vignette, References: vignette, NOVALUE = 0
        int startOpen;                               // 25 startOpen, enum { false, true, }; Default: false
        int dontPlayOpenAnim;                        // 26 Dont Play Open Anim, enum { false, true, }; Default: false
        int ignoreBoundingBox;                       // 27 Ignore Bounding Box, enum { false, true, }; Default: false
        int neverUsableWhileMounted;                 // 28 Never Usable While Mounted, enum { false, true, }; Default: false
        int sortFarZ;                                // 29 Sort Far Z, enum { false, true, }; Default: false
        int syncAnimationtoObjectLifetime;           // 30 Sync Animation to Object lifetime (global track only), enum { false, true, }; Default: false
        int noFuzzyHit;                              // 31 No Fuzzy hit, enum { false, true, }; Default: false
        int largeAOI;                                // 32 Large AOI, enum { false, true, }; Default: false
        int interactRadiusOverride;                  // 33 Interact Radius override (in hundredths), int, Min value: 0, Max value: 2147483647, Default value: 0
    }
    // 37 GAMEOBJECT_TYPE_NEW_FLAG_DROP

    public static class MiniGame {
    }
    // 38 GAMEOBJECT_TYPE_GARRISON_BUILDING

    public static class Transport {
        int timeto2ndfloor;                          // 0 Time to 2nd floor (ms), int, Min value: 0, Max value: 2147483647, Default value: 0
        int startOpen;                               // 1 startOpen, enum { false, true, }; Default: false
        int autoClose;                               // 2 autoClose (ms), int, Min value: 0, Max value: 2147483647, Default value: 0
        int reached1stfloor;                         // 3 Reached 1st floor, References: GameEvents, NOVALUE = 0
        int reached2ndfloor;                         // 4 Reached 2nd floor, References: GameEvents, NOVALUE = 0
        int spawnMap;                                 // 5 Spawn Map, References: Map, NOVALUE = -1
        int timeto3rdfloor;                          // 6 Time to 3rd floor (ms), int, Min value: 0, Max value: 2147483647, Default value: 0
        int reached3rdfloor;                         // 7 Reached 3rd floor, References: GameEvents, NOVALUE = 0
        int timeto4thfloor;                          // 8 Time to 4th floor (ms), int, Min value: 0, Max value: 2147483647, Default value: 0
        int reached4thfloor;                         // 9 Reached 4th floor, References: GameEvents, NOVALUE = 0
        int timeto5thfloor;                          // 10 Time to 5th floor (ms), int, Min value: 0, Max value: 2147483647, Default value: 0
        int reached5thfloor;                         // 11 Reached 5th floor, References: GameEvents, NOVALUE = 0
        int timeto6thfloor;                          // 12 Time to 6th floor (ms), int, Min value: 0, Max value: 2147483647, Default value: 0
        int reached6thfloor;                         // 13 Reached 6th floor, References: GameEvents, NOVALUE = 0
        int timeto7thfloor;                          // 14 Time to 7th floor (ms), int, Min value: 0, Max value: 2147483647, Default value: 0
        int reached7thfloor;                         // 15 Reached 7th floor, References: GameEvents, NOVALUE = 0
        int timeto8thfloor;                          // 16 Time to 8th floor (ms), int, Min value: 0, Max value: 2147483647, Default value: 0
        int reached8thfloor;                         // 17 Reached 8th floor, References: GameEvents, NOVALUE = 0
        int timeto9thfloor;                          // 18 Time to 9th floor (ms), int, Min value: 0, Max value: 2147483647, Default value: 0
        int reached9thfloor;                         // 19 Reached 9th floor, References: GameEvents, NOVALUE = 0
        int timeto10thfloor;                         // 20 Time to 10th floor (ms), int, Min value: 0, Max value: 2147483647, Default value: 0
        int reached10thfloor;                        // 21 Reached 10th floor, References: GameEvents, NOVALUE = 0
        int onlychargeheightcheck;                   // 22 only charge height check. (yards), int, Min value: 0, Max value: 65535, Default value: 0
        int onlychargetimecheck;                     // 23 only charge time check, int, Min value: 0, Max value: 65535, Default value: 0
        int interactRadiusOverride;                  // 24 Interact Radius override (in hundredths), int, Min value: 0, Max value: 2147483647, Default value: 0
    }
    // 39 GAMEOBJECT_TYPE_GARRISON_PLOT

    public static class AreaDamage {
        int open;                                    // 0 open, References: Lock_, NOVALUE = 0
        int radius;                                  // 1 radius, int, Min value: 0, Max value: 50, Default value: 3
        int damageMin;                               // 2 damageMin, int, Min value: 0, Max value: 65535, Default value: 0
        int damageMax;                               // 3 damageMax, int, Min value: 0, Max value: 65535, Default value: 0
        int damageSchool;                            // 4 damageSchool, int, Min value: 0, Max value: 65535, Default value: 0
        int autoClose;                               // 5 autoClose (ms), int, Min value: 0, Max value: 2147483647, Default value: 0
        int openTextID;                              // 6 openTextID, References: BroadcastText, NOVALUE = 0
        int closeTextID;                             // 7 closeTextID, References: BroadcastText, NOVALUE = 0
        int interactRadiusOverride;                  // 8 Interact Radius override (in hundredths), int, Min value: 0, Max value: 2147483647, Default value: 0
    }
    // 40 GAMEOBJECT_TYPE_CLIENT_CREATURE

    public static class Camera {
        int open;                                    // 0 open, References: Lock_, NOVALUE = 0
        int camera;                                  // 1 camera, References: CinematicSequences, NOVALUE = 0
        int eventID;                                 // 2 eventID, References: GameEvents, NOVALUE = 0
        int openTextID;                              // 3 openTextID, References: BroadcastText, NOVALUE = 0
        int conditionID1;                            // 4 conditionID1, References: PlayerCondition, NOVALUE = 0
        int interactRadiusOverride;                  // 5 Interact Radius override (in hundredths), int, Min value: 0, Max value: 2147483647, Default value: 0
    }
    // 41 GAMEOBJECT_TYPE_CLIENT_ITEM

    public static class Mapobject {
    }
    // 42 GAMEOBJECT_TYPE_CAPTURE_POINT

    public static class MoTransport {
        int taxiPathID;                              // 0 taxiPathID, References: taxiPath, NOVALUE = 0
        int moveSpeed;                               // 1 moveSpeed, int, Min value: 1, Max value: 60, Default value: 1
        int accelRate;                               // 2 accelRate, int, Min value: 1, Max value: 20, Default value: 1
        int startEventID;                            // 3 startEventID, References: GameEvents, NOVALUE = 0
        int stopEventID;                             // 4 stopEventID, References: GameEvents, NOVALUE = 0
        int transportPhysics;                        // 5 transportPhysics, References: TransportPhysics, NOVALUE = 0
        int spawnMap;                                 // 6 Spawn Map, References: Map, NOVALUE = -1
        int worldState1;                             // 7 worldState1, References: worldState, NOVALUE = 0
        int allowstopping;                           // 8 allow stopping, enum { false, true, }; Default: false
        int initStopped;                             // 9 Init Stopped, enum { false, true, }; Default: false
        int trueInfiniteAOI;                         // 10 True Infinite AOI (programmer only!), enum { false, true, }; Default: false
        int interactRadiusOverride;                  // 11 Interact Radius override (in hundredths), int, Min value: 0, Max value: 2147483647, Default value: 0
        int allowareaexplorationwhileonthistransport;// 12 Allow area exploration while on this transport, enum { false, true, }; Default: false
    }
    // 43 GAMEOBJECT_TYPE_PHASEABLE_MO

    public static class DuelFlag {
        int interactRadiusOverride;                  // 0 Interact Radius override (in hundredths), int, Min value: 0, Max value: 2147483647, Default value: 0
    }
    // 44 GAMEOBJECT_TYPE_GARRISON_MONUMENT

    public static class FishingNode {
        int interactRadiusOverride;                  // 0 Interact Radius override (in hundredths), int, Min value: 0, Max value: 2147483647, Default value: 0
    }
    // 45 GAMEOBJECT_TYPE_GARRISON_SHIPMENT

    public static class Ritual {
        int casters;                                 // 0 casters, int, Min value: 1, Max value: 10, Default value: 1
        int spell;                                   // 1 spell, References: spell, NOVALUE = 0
        int animSpell;                               // 2 animSpell, References: spell, NOVALUE = 0
        int ritualPersistent;                        // 3 ritualPersistent, enum { false, true, }; Default: false
        int casterTargetSpell;                       // 4 casterTargetSpell, References: spell, NOVALUE = 0
        int casterTargetSpellTargets;                // 5 casterTargetSpellTargets, int, Min value: 1, Max value: 10, Default value: 1
        int castersGrouped;                          // 6 castersGrouped, enum { false, true, }; Default: true
        int ritualNoTargetCheck;                     // 7 ritualNoTargetCheck, enum { false, true, }; Default: true
        int conditionID1;                            // 8 conditionID1, References: PlayerCondition, NOVALUE = 0
        int interactRadiusOverride;                  // 9 Interact Radius override (in hundredths), int, Min value: 0, Max value: 2147483647, Default value: 0
        int allowunfriendlycrossfactionpartymemberstocollaborateonaritual;// 10 Allow unfriendly cross faction party members to collaborate on a ritual, enum { false, true, }; Default: false
    }
    // 46 GAMEOBJECT_TYPE_GARRISON_MONUMENT_PLAQUE

    public static class Mailbox {
        int conditionID1;                            // 0 conditionID1, References: PlayerCondition, NOVALUE = 0
        int interactRadiusOverride;                  // 1 Interact Radius override (in hundredths), int, Min value: 0, Max value: 2147483647, Default value: 0
    }
    // 47 GAMEOBJECT_TYPE_ITEM_FORGE

    public static class DoNotUse {
    }
    // 48 GAMEOBJECT_TYPE_UI_LINK

    public static class GuardPost {
        int creatureID;                              // 0 creatureID, References: CREATURE, NOVALUE = 0
        int charges;                                 // 1 charges, int, Min value: 0, Max value: 65535, Default value: 1
        int preferonlyifinlineofsight;               // 2 Prefer only if in line of sight (expensive), enum { false, true, }; Default: false
        int interactRadiusOverride;                  // 3 Interact Radius override (in hundredths), int, Min value: 0, Max value: 2147483647, Default value: 0
    }
    // 49 GAMEOBJECT_TYPE_KEYSTONE_RECEPTACLE

    public static class SpellCaster {
        int spell;                                   // 0 spell, References: spell, NOVALUE = 0
        int charges;                                  // 1 charges, int, Min value: -1, Max value: 65535, Default value: 1
        int partyOnly;                               // 2 partyOnly, enum { false, true, }; Default: false
        int allowMounted;                            // 3 allowMounted, enum { false, true, }; Default: false
        int giganticAOI;                             // 4 Gigantic AOI, enum { false, true, }; Default: false
        int conditionID1;                            // 5 conditionID1, References: PlayerCondition, NOVALUE = 0
        int playerCast;                              // 6 playerCast, enum { false, true, }; Default: false
        int neverUsableWhileMounted;                 // 7 Never Usable While Mounted, enum { false, true, }; Default: false
        int interactRadiusOverride;                  // 8 Interact Radius override (in hundredths), int, Min value: 0, Max value: 2147483647, Default value: 0
    }
    // 50 GAMEOBJECT_TYPE_GATHERING_NODE

    public static class MeetingStone {
        int unused;                                  // 0 unused, int, Min value: 0, Max value: 65535, Default value: 1
        int unused2;                                 // 1 unused, int, Min value: 1, Max value: 65535, Default value: 60
        int areaID;                                  // 2 areaID, References: AreaTable, NOVALUE = 0
        int interactRadiusOverride;                  // 3 Interact Radius override (in hundredths), int, Min value: 0, Max value: 2147483647, Default value: 0
        int preventmeetingstonefromtargetinganunfriendlypartymemberoutsideofinstances;// 4 Prevent meeting stone from targeting an unfriendly party member outside of instances, enum { false, true, }; Default: false
    }
    // 51 GAMEOBJECT_TYPE_CHALLENGE_MODE_REWARD

    public static class FlagStand {
        int open;                                    // 0 open, References: Lock_, NOVALUE = 0
        int pickupSpell;                             // 1 pickupSpell, References: spell, NOVALUE = 0
        int radius;                                  // 2 radius, int, Min value: 0, Max value: 50, Default value: 0
        int returnAura;                              // 3 returnAura, References: spell, NOVALUE = 0
        int returnSpell;                             // 4 returnSpell, References: spell, NOVALUE = 0
        int noDamageImmune;                          // 5 noDamageImmune, enum { false, true, }; Default: false
        int openTextID;                              // 6 openTextID, References: BroadcastText, NOVALUE = 0
        int requireLOS;                              // 7 require LOS, enum { false, true, }; Default: true
        int conditionID1;                            // 8 conditionID1, References: PlayerCondition, NOVALUE = 0
        int playerCast;                              // 9 playerCast, enum { false, true, }; Default: false
        int giganticAOI;                             // 10 Gigantic AOI, enum { false, true, }; Default: false
        int infiniteAOI;                             // 11 Infinite AOI, enum { false, true, }; Default: false
        int cooldown;                                // 12 cooldown, int, Min value: 0, Max value: 2147483647, Default value: 3000
        int interactRadiusOverride;                  // 13 Interact Radius override (in hundredths), int, Min value: 0, Max value: 2147483647, Default value: 0
    }

    public static class FishingHole {
        int radius;                                  // 0 radius, int, Min value: 0, Max value: 50, Default value: 0
        int chestLoot;                               // 1 chestLoot (legacy/classic), References: treasure, NOVALUE = 0
        int minRestock;                              // 2 minRestock, int, Min value: 0, Max value: 65535, Default value: 0
        int maxRestock;                              // 3 maxRestock, int, Min value: 0, Max value: 65535, Default value: 0
        int open;                                    // 4 open, References: Lock_, NOVALUE = 0
        int interactRadiusOverride;                  // 5 Interact Radius override (in hundredths), int, Min value: 0, Max value: 2147483647, Default value: 0
    }

    public static class FlagDrop {
        int open;                                    // 0 open, References: Lock_, NOVALUE = 0
        int eventID;                                 // 1 eventID, References: GameEvents, NOVALUE = 0
        int pickupSpell;                             // 2 pickupSpell, References: spell, NOVALUE = 0
        int noDamageImmune;                          // 3 noDamageImmune, enum { false, true, }; Default: false
        int openTextID;                              // 4 openTextID, References: BroadcastText, NOVALUE = 0
        int playerCast;                              // 5 playerCast, enum { false, true, }; Default: false
        int expireDuration;                          // 6 Expire duration, int, Min value: 0, Max value: 60000, Default value: 10000
        int giganticAOI;                             // 7 Gigantic AOI, enum { false, true, }; Default: false
        int infiniteAOI;                             // 8 Infinite AOI, enum { false, true, }; Default: false
        int cooldown;                                // 9 cooldown, int, Min value: 0, Max value: 2147483647, Default value: 3000
        int interactRadiusOverride;                  // 10 Interact Radius override (in hundredths), int, Min value: 0, Max value: 2147483647, Default value: 0
    }

    public static class ControlZone {
        int radius;                                  // 0 radius, int, Min value: 0, Max value: 100, Default value: 10
        int spell;                                   // 1 spell, References: spell, NOVALUE = 0
        int worldState1;                             // 2 worldState1, References: worldState, NOVALUE = 0
        int worldstate2;                             // 3 worldstate2, References: worldState, NOVALUE = 0
        int captureEventHorde;                       // 4 Capture event (Horde), References: GameEvents, NOVALUE = 0
        int captureEventAlliance;                    // 5 Capture event (Alliance), References: GameEvents, NOVALUE = 0
        int contestedEventHorde;                     // 6 Contested event (Horde), References: GameEvents, NOVALUE = 0
        int contestedEventAlliance;                  // 7 Contested event (Alliance), References: GameEvents, NOVALUE = 0
        int progressEventHorde;                      // 8 Progress event (Horde), References: GameEvents, NOVALUE = 0
        int progressEventAlliance;                   // 9 Progress event (Alliance), References: GameEvents, NOVALUE = 0
        int neutralEventHorde;                       // 10 Neutral event (Horde), References: GameEvents, NOVALUE = 0
        int neutralEventAlliance;                    // 11 Neutral event (Alliance), References: GameEvents, NOVALUE = 0
        int neutralPercent;                          // 12 neutralPercent, int, Min value: 0, Max value: 100, Default value: 0
        int worldstate3;                             // 13 worldstate3, References: worldState, NOVALUE = 0
        int minSuperiority;                          // 14 minSuperiority, int, Min value: 1, Max value: 65535, Default value: 1
        int maxSuperiority;                          // 15 maxSuperiority, int, Min value: 1, Max value: 65535, Default value: 1
        int minTime;                                 // 16 minTime, int, Min value: 1, Max value: 65535, Default value: 1
        int maxTime;                                 // 17 maxTime, int, Min value: 1, Max value: 65535, Default value: 1
        int giganticAOI;                             // 18 Gigantic AOI, enum { false, true, }; Default: false
        int highlight;                               // 19 highlight, enum { false, true, }; Default: true
        int startingValue;                           // 20 startingValue, int, Min value: 0, Max value: 100, Default value: 50
        int unidirectional;                          // 21 unidirectional, enum { false, true, }; Default: false
        int killbonustime;                           // 22 kill bonus time %, int, Min value: 0, Max value: 100, Default value: 0
        int speedWorldState1;                        // 23 speedWorldState1, References: worldState, NOVALUE = 0
        int speedWorldState2;                        // 24 speedWorldState2, References: worldState, NOVALUE = 0
        int uncontestedTime;                         // 25 Uncontested time, int, Min value: 0, Max value: 65535, Default value: 0
        int frequentHeartbeat;                       // 26 Frequent Heartbeat, enum { false, true, }; Default: false
        int enablingWorldStateExpression;            // 27 Enabling World State Expression, References: WorldStateExpression, NOVALUE = 0
        int interactRadiusOverride;                  // 28 Interact Radius override (in hundredths), int, Min value: 0, Max value: 2147483647, Default value: 0
    }

    public static class AuraGenerator {
        int startOpen;                               // 0 startOpen, enum { false, true, }; Default: true
        int radius;                                  // 1 radius, int, Min value: 0, Max value: 100, Default value: 10
        int auraID1;                                 // 2 auraID1, References: spell, NOVALUE = 0
        int conditionID1;                            // 3 conditionID1, References: PlayerCondition, NOVALUE = 0
        int auraID2;                                 // 4 auraID2, References: spell, NOVALUE = 0
        int conditionID2;                            // 5 conditionID2, References: PlayerCondition, NOVALUE = 0
        int serverOnly;                              // 6 serverOnly, enum { false, true, }; Default: false
        int interactRadiusOverride;                  // 7 Interact Radius override (in hundredths), int, Min value: 0, Max value: 2147483647, Default value: 0
    }

    public static class DungeonDifficulty {
        int instanceType;                            // 0 Instance type, enum { Not Instanced, Party Dungeon, Raid Dungeon, PVP Battlefield, Arena Battlefield, Scenario, WoWLabs, }; Default: Party Dungeon
        int difficultyNormal;                        // 1 Difficulty NORMAL, References: animationdata, NOVALUE = 0
        int difficultyHeroic;                        // 2 Difficulty Heroic, References: animationdata, NOVALUE = 0
        int difficultyEpic;                          // 3 Difficulty Epic, References: animationdata, NOVALUE = 0
        int difficultyLegendary;                     // 4 Difficulty Legendary, References: animationdata, NOVALUE = 0
        int heroicAttachment;                        // 5 Heroic Attachment, References: gameobjectdisplayinfo, NOVALUE = 0
        int challengeAttachment;                     // 6 Challenge Attachment, References: gameobjectdisplayinfo, NOVALUE = 0
        int difficultyAnimations;                    // 7 Difficulty Animations, References: GameObjectDiffAnim, NOVALUE = 0
        int largeAOI;                                // 8 Large AOI, enum { false, true, }; Default: false
        int giganticAOI;                             // 9 Gigantic AOI, enum { false, true, }; Default: false
        int legacy;                                  // 10 legacy, enum { false, true, }; Default: false
        int interactRadiusOverride;                  // 11 Interact Radius override (in hundredths), int, Min value: 0, Max value: 2147483647, Default value: 0
    }

    public static class BarberChair {
        int chairheight;                             // 0 chairheight, int, Min value: 0, Max value: 2, Default value: 1
        int heightOffset;                             // 1 Height offset (inches), int, Min value: -100, Max value: 100, Default value: 0
        int sitAnimKit;                              // 2 Sit Anim Kit, References: animKit, NOVALUE = 0
        int interactRadiusOverride;                  // 3 Interact Radius override (in hundredths), int, Min value: 0, Max value: 2147483647, Default value: 0
        int customizationScope;                      // 4 Customization Scope, int, Min value: 0, Max value: 2147483647, Default value: 0
    }

    public static class DestructibleBuilding {
        int unused;                                   // 0 unused, int, Min value: -2147483648, Max value: 2147483647, Default value: 0
        int creditProxyCreature;                     // 1 Credit Proxy CREATURE, References: CREATURE, NOVALUE = 0
        int healthRec;                               // 2 Health Rec, References: Depublic static classibleHitpoint, NOVALUE = 0
        int intactEvent;                             // 3 Intact event, References: GameEvents, NOVALUE = 0
        int PVPEnabling;                             // 4 PVP Enabling, enum { false, true, }; Default: false
        int interiorVisible;                         // 5 Interior Visible, enum { false, true, }; Default: false
        int interiorLight;                           // 6 Interior Light, enum { false, true, }; Default: false
        int unused1;                                  // 7 unused, int, Min value: -2147483648, Max value: 2147483647, Default value: 0
        int unused2;                                  // 8 unused, int, Min value: -2147483648, Max value: 2147483647, Default value: 0
        int damagedEvent;                            // 9 Damaged event, References: GameEvents, NOVALUE = 0
        int unused3;                                  // 10 unused, int, Min value: -2147483648, Max value: 2147483647, Default value: 0
        int unused4;                                  // 11 unused, int, Min value: -2147483648, Max value: 2147483647, Default value: 0
        int unused5;                                  // 12 unused, int, Min value: -2147483648, Max value: 2147483647, Default value: 0
        int unused6;                                  // 13 unused, int, Min value: -2147483648, Max value: 2147483647, Default value: 0
        int destroyedEvent;                          // 14 Destroyed event, References: GameEvents, NOVALUE = 0
        int unused7;                                  // 15 unused, int, Min value: -2147483648, Max value: 2147483647, Default value: 0
        int rebuildingTime;                          // 16 Rebuilding: time (secs), int, Min value: 0, Max value: 65535, Default value: 0
        int unused8;                                  // 17 unused, int, Min value: -2147483648, Max value: 2147483647, Default value: 0
        int destructibleModelRec;                    // 18 Destructible Model Rec, References: DestructibleModelData, NOVALUE = 0
        int rebuildingEvent;                         // 19 Rebuilding: event, References: GameEvents, NOVALUE = 0
        int unused9;                                  // 20 unused, int, Min value: -2147483648, Max value: 2147483647, Default value: 0
        int unused10;                                 // 21 unused, int, Min value: -2147483648, Max value: 2147483647, Default value: 0
        int damageEvent;                             // 22 Damage event, References: GameEvents, NOVALUE = 0
        int displaymouseoverasanameplate;            // 23 Display mouseover as a nameplate, enum { false, true, }; Default: false
        int thexoffsetofthedestructiblenameplateifitisenabled;// 24 The x offset (in hundredths) of the destructible nameplate, if it is enabled, int, Min value: -2147483648, Max value: 2147483647, Default value: 0
        int theyoffsetofthedestructiblenameplateifitisenabled;// 25 The y offset (in hundredths) of the destructible nameplate, if it is enabled, int, Min value: -2147483648, Max value: 2147483647, Default value: 0
        int thezoffsetofthedestructiblenameplateifitisenabled;// 26 The z offset (in hundredths) of the destructible nameplate, if it is enabled, int, Min value: -2147483648, Max value: 2147483647, Default value: 0
        int interactRadiusOverride;                  // 27 Interact Radius override (in hundredths), int, Min value: 0, Max value: 2147483647, Default value: 0
    }

    public static class TrapDoor {
        int autoLink;                                // 0 Auto link, enum { false, true, }; Default: false
        int startOpen;                               // 1 startOpen, enum { false, true, }; Default: false
        int autoClose;                               // 2 autoClose (ms), int, Min value: 0, Max value: 2147483647, Default value: 0
        int blocksPathsDown;                         // 3 Blocks Paths Down, enum { false, true, }; Default: false
        int pathBlockerBump;                          // 4 Path Blocker Bump (ft), int, Min value: -2147483648, Max value: 2147483647, Default value: 0
        int giganticAOI;                             // 5 Gigantic AOI, enum { false, true, }; Default: false
        int infiniteAOI;                             // 6 Infinite AOI, enum { false, true, }; Default: false
        int doorisOpaque;                            // 7 Door is Opaque (Disable portal on close), enum { false, true, }; Default: false
        int interactRadiusOverride;                  // 8 Interact Radius override (in hundredths), int, Min value: 0, Max value: 2147483647, Default value: 0
    }

    public static class NewFlag {
        int open;                                    // 0 open, References: Lock_, NOVALUE = 0
        int pickupSpell;                             // 1 pickupSpell, References: spell, NOVALUE = 0
        int openTextID;                              // 2 openTextID, References: BroadcastText, NOVALUE = 0
        int requireLOS;                              // 3 require LOS, enum { false, true, }; Default: true
        int conditionID1;                            // 4 conditionID1, References: PlayerCondition, NOVALUE = 0
        int giganticAOI;                             // 5 Gigantic AOI, enum { false, true, }; Default: false
        int infiniteAOI;                             // 6 Infinite AOI, enum { false, true, }; Default: false
        int expireDuration;                          // 7 Expire duration, int, Min value: 0, Max value: 3600000, Default value: 10000
        int respawnTime;                             // 8 Respawn time, int, Min value: 0, Max value: 3600000, Default value: 20000
        int flagDrop;                                // 9 Flag Drop, References: gameObjects, NOVALUE = 0
        int exclusiveCategory;                        // 10 Exclusive category (BGs Only), int, Min value: -2147483648, Max value: 2147483647, Default value: 0
        int worldState1;                             // 11 worldState1, References: worldState, NOVALUE = 0
        int returnonDefenderInteract;                // 12 Return on Defender Interact, enum { false, true, }; Default: false
        int spawnVignette;                           // 13 Spawn Vignette, References: vignette, NOVALUE = 0
        int interactRadiusOverride;                  // 14 Interact Radius override (in hundredths), int, Min value: 0, Max value: 2147483647, Default value: 0
    }

    public static class GuildBank {
        int conditionID1;                            // 0 conditionID1, References: PlayerCondition, NOVALUE = 0
        int interactRadiusOverride;                  // 1 Interact Radius override (in hundredths), int, Min value: 0, Max value: 2147483647, Default value: 0
    }

    public static class NewFlagDrop {
        int open;                                    // 0 open, References: Lock_, NOVALUE = 0
        int spawnVignette;                           // 1 Spawn Vignette, References: vignette, NOVALUE = 0
        int interactRadiusOverride;                  // 2 Interact Radius override (in hundredths), int, Min value: 0, Max value: 2147483647, Default value: 0
    }

    public static class GarrisonBuilding {
        int spawnMap;                                 // 0 Spawn Map, References: Map, NOVALUE = -1
        int interactRadiusOverride;                  // 1 Interact Radius override (in hundredths), int, Min value: 0, Max value: 2147483647, Default value: 0
    }

    public static class GarrisonPlot {
        int plotInstance;                            // 0 Plot instance, References: GarrPlotInstance, NOVALUE = 0
        int spawnMap;                                 // 1 Spawn Map, References: Map, NOVALUE = -1
        int interactRadiusOverride;                  // 2 Interact Radius override (in hundredths), int, Min value: 0, Max value: 2147483647, Default value: 0
    }

    public static class ClientCreature {
        int creatureDisplayInfo;                     // 0 Creature Display info, References: creatureDisplayInfo, NOVALUE = 0
        int animKit;                                 // 1 Anim Kit, References: animKit, NOVALUE = 0
        int creatureID;                              // 2 creatureID, References: CREATURE, NOVALUE = 0
    }

    public static class ClientItem {
        int item;                                    // 0 item, References: item, NOVALUE = 0
    }

    public static class CapturePoint {
        int captureTime;                             // 0 Capture time (ms), int, Min value: 0, Max value: 2147483647, Default value: 60000
        int giganticAOI;                             // 1 Gigantic AOI, enum { false, true, }; Default: false
        int highlight;                               // 2 highlight, enum { false, true, }; Default: true
        int open;                                    // 3 open, References: Lock_, NOVALUE = 0
        int assaultBroadcastHorde;                   // 4 Assault Broadcast (Horde), References: BroadcastText, NOVALUE = 0
        int captureBroadcastHorde;                   // 5 Capture Broadcast (Horde), References: BroadcastText, NOVALUE = 0
        int defendedBroadcastHorde;                  // 6 Defended Broadcast (Horde), References: BroadcastText, NOVALUE = 0
        int assaultBroadcastAlliance;                // 7 Assault Broadcast (Alliance), References: BroadcastText, NOVALUE = 0
        int captureBroadcastAlliance;                // 8 Capture Broadcast (Alliance), References: BroadcastText, NOVALUE = 0
        int defendedBroadcastAlliance;               // 9 Defended Broadcast (Alliance), References: BroadcastText, NOVALUE = 0
        int worldState1;                             // 10 worldState1, References: worldState, NOVALUE = 0
        int contestedEventHorde;                     // 11 Contested event (Horde), References: GameEvents, NOVALUE = 0
        int captureEventHorde;                       // 12 Capture event (Horde), References: GameEvents, NOVALUE = 0
        int defendedEventHorde;                      // 13 Defended event (Horde), References: GameEvents, NOVALUE = 0
        int contestedEventAlliance;                  // 14 Contested event (Alliance), References: GameEvents, NOVALUE = 0
        int captureEventAlliance;                    // 15 Capture event (Alliance), References: GameEvents, NOVALUE = 0
        int defendedEventAlliance;                   // 16 Defended event (Alliance), References: GameEvents, NOVALUE = 0
        int spellVisual1;                            // 17 Spell Visual 1, References: spellVisual, NOVALUE = 0
        int spellVisual2;                            // 18 Spell Visual 2, References: spellVisual, NOVALUE = 0
        int spellVisual3;                            // 19 Spell Visual 3, References: spellVisual, NOVALUE = 0
        int spellVisual4;                            // 20 Spell Visual 4, References: spellVisual, NOVALUE = 0
        int spellVisual5;                            // 21 Spell Visual 5, References: spellVisual, NOVALUE = 0
        int spawnVignette;                           // 22 Spawn Vignette, References: vignette, NOVALUE = 0
        int interactRadiusOverride;                  // 23 Interact Radius override (in hundredths), int, Min value: 0, Max value: 2147483647, Default value: 0
    }

    public static class PhaseableMO {
        int spawnMap;                                 // 0 Spawn Map, References: Map, NOVALUE = -1
        int areaNameSet;                              // 1 Area Name set (index), int, Min value: -2147483648, Max value: 2147483647, Default value: 0
        int doodadSetA;                              // 2 Doodad Set A, int, Min value: 0, Max value: 2147483647, Default value: 0
        int doodadSetB;                              // 3 Doodad Set B, int, Min value: 0, Max value: 2147483647, Default value: 0
        int interactRadiusOverride;                  // 4 Interact Radius override (in hundredths), int, Min value: 0, Max value: 2147483647, Default value: 0
    }

    public static class GarrisonMonument {
        int trophyTypeID;                            // 0 Trophy Type ID, References: TrophyType, NOVALUE = 0
        int trophyInstanceID;                        // 1 Trophy Instance ID, References: TrophyInstance, NOVALUE = 0
        int interactRadiusOverride;                  // 2 Interact Radius override (in hundredths), int, Min value: 0, Max value: 2147483647, Default value: 0
    }

    public static class GarrisonShipment {
        int shipmentContainer;                       // 0 Shipment Container, References: CharShipmentContainer, NOVALUE = 0
        int giganticAOI;                             // 1 Gigantic AOI, enum { false, true, }; Default: false
        int largeAOI;                                // 2 Large AOI, enum { false, true, }; Default: false
        int interactRadiusOverride;                  // 3 Interact Radius override (in hundredths), int, Min value: 0, Max value: 2147483647, Default value: 0
    }

    public static class GarrisonMonumentPlaque {
        int trophyInstanceID;                        // 0 Trophy Instance ID, References: TrophyInstance, NOVALUE = 0
        int interactRadiusOverride;                  // 1 Interact Radius override (in hundredths), int, Min value: 0, Max value: 2147483647, Default value: 0
    }

    public static class ItemForge {
        int conditionID1;                            // 0 conditionID1, References: PlayerCondition, NOVALUE = 0
        int largeAOI;                                // 1 Large AOI, enum { false, true, }; Default: false
        int ignoreBoundingBox;                       // 2 Ignore Bounding Box, enum { false, true, }; Default: false
        int cameraMode;                              // 3 Camera mode, References: cameraMode, NOVALUE = 0
        int fadeRegionRadius;                        // 4 Fade Region radius, int, Min value: 0, Max value: 2147483647, Default value: 0
        int forgeType;                               // 5 Forge type, enum { Artifact Forge, Relic Forge, Heart Forge, Soulbind Forge, Anima Reservoir, }; Default: Relic Forge
        int interactRadiusOverride;                  // 6 Interact Radius override (in hundredths), int, Min value: 0, Max value: 2147483647, Default value: 0
        int garrTalentTreeID;                        // 7 GarrTalentTree ID, References: garrTalentTree, NOVALUE = 0
    }

    public static class UILink {
        int UILinkType;                              // 0 UI Link type, enum { Adventure Journal, Obliterum Forge, Scrapping Machine, Item Interaction, }; Default: Adventure Journal
        int allowMounted;                            // 1 allowMounted, enum { false, true, }; Default: false
        int giganticAOI;                             // 2 Gigantic AOI, enum { false, true, }; Default: false
        int spellFocusType;                          // 3 spellFocusType, References: SpellFocusObject, NOVALUE = 0
        int radius;                                  // 4 radius, int, Min value: 0, Max value: 50, Default value: 10
        int interactRadiusOverride;                  // 5 Interact Radius override (in hundredths), int, Min value: 0, Max value: 2147483647, Default value: 0
        int itemInteractionID;                       // 6 Item Interaction ID, References: UiItemInteraction, NOVALUE = 0
    }

    public static class KeystoneReceptacle {
        int interactRadiusOverride;                  // 0 Interact Radius override (in hundredths), int, Min value: 0, Max value: 2147483647, Default value: 0
    }

    public static class GatheringNode {
        int open;                                    // 0 open, References: Lock_, NOVALUE = 0
        int chestLoot;                               // 1 chestLoot (legacy/classic), References: treasure, NOVALUE = 0
        int unused;                                  // 2 unused, int, Min value: 0, Max value: 65535, Default value: 0
        int notInCombat;                             // 3 notInCombat, enum { false, true, }; Default: false
        int trivialSkillLow;                         // 4 trivialSkillLow, int, Min value: 0, Max value: 65535, Default value: 0
        int trivialSkillHigh;                        // 5 trivialSkillHigh, int, Min value: 0, Max value: 65535, Default value: 0
        int objectDespawnDelay;                      // 6 Object Despawn delay, int, Min value: 0, Max value: 600, Default value: 15
        int triggeredEvent;                          // 7 triggeredEvent, References: GameEvents, NOVALUE = 0
        int requireLOS;                              // 8 require LOS, enum { false, true, }; Default: false
        int openTextID;                              // 9 openTextID, References: BroadcastText, NOVALUE = 0
        int floatingTooltip;                         // 10 floatingTooltip, enum { false, true, }; Default: false
        int conditionID1;                            // 11 conditionID1, References: PlayerCondition, NOVALUE = 0
        int unused2;                                  // 12 unused, int, Min value: -2147483648, Max value: 2147483647, Default value: 0
        int xpDifficulty;                            // 13 xpDifficulty, enum { No exp, Trivial, Very Small, Small, Substandard, Standard, High, Epic, Dungeon, 5, }; Default: No Exp
        int spell;                                   // 14 spell, References: spell, NOVALUE = 0
        int giganticAOI;                             // 15 Gigantic AOI, enum { false, true, }; Default: false
        int largeAOI;                                // 16 Large AOI, enum { false, true, }; Default: false
        int spawnVignette;                           // 17 Spawn Vignette, References: vignette, NOVALUE = 0
        int maxNumberofLoots;                        // 18 Max Number of Loots, int, Min value: 1, Max value: 40, Default value: 10
        int logloot;                                 // 19 log loot, enum { false, true, }; Default: false
        int linkedTrap;                              // 20 linkedTrap, References: gameObjects, NOVALUE = 0
        int playOpenAnimationonOpening;              // 21 Play Open Animation on Opening, enum { false, true, }; Default: false
        int turnpersonallootsecurityoff;             // 22 turn personal loot security off, enum { false, true, }; Default: false
        int clearObjectVignetteonOpening;            // 23 Clear Object Vignette on Opening, enum { false, true, }; Default: false
        int interactRadiusOverride;                  // 24 Interact Radius override (in hundredths), int, Min value: 0, Max value: 2147483647, Default value: 0
    }

    public static class ChallengeModeReward {
        int unused;                                   // 0 unused, int, Min value: -2147483648, Max value: 2147483647, Default value: 0
        int whenAvailable;                           // 1 When Available, References: GameObjectDisplayInfo, NOVALUE = 0
        int open;                                    // 2 open, References: Lock_, NOVALUE = 0
        int openTextID;                              // 3 openTextID, References: BroadcastText, NOVALUE = 0
        int interactRadiusOverride;                  // 4 Interact Radius override (in hundredths), int, Min value: 0, Max value: 2147483647, Default value: 0
    }

    public static class Multi {
        int multiProperties;                         // 0 Multi Properties, References: multiProperties, NOVALUE = 0
    }

    public static class SiegeableMulti {
        int multiProperties;                         // 0 Multi Properties, References: multiProperties, NOVALUE = 0
        int initialDamage;                           // 1 Initial damage, enum { NONE, raw, Ratio, }; Default: None
    }


}
