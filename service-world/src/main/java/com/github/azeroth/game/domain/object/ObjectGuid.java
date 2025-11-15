package com.github.azeroth.game.domain.object;

import com.github.azeroth.game.domain.object.enums.HighGuid;
import com.github.azeroth.utils.SecureUtils;
import lombok.EqualsAndHashCode;

import java.util.Arrays;
import java.util.Objects;

@EqualsAndHashCode
public final class ObjectGuid implements Comparable<ObjectGuid>{

    public static final ObjectGuid EMPTY = new ObjectGuid();
    public static final ObjectGuid FROM_STRING_FAILED = new ObjectGuid();
    public static final ObjectGuid TRADE_ITEM = new ObjectGuid();
    private static final int OBJECT_GUID_DATA_SIZE = 16;
    private final byte[] data;


    public ObjectGuid(long high, long low) {
        this.data = new byte[OBJECT_GUID_DATA_SIZE];
        bytes(high, low);
    }


    ObjectGuid() {
        this(0L, 0L);
    }

    public static long maxCounter(HighGuid high) {
        if (Objects.requireNonNull(high) == HighGuid.Transport) {
            return 0xFFFFFL;
        }
        return 0xFFFFFFFFFFL;
    }

    public static ObjectGuid create(HighGuid type, int realmId, int dbId) {
        return switch (type) {
            case HighGuid.Null -> createNull();
            case HighGuid.Uniq -> createUniq(dbId);
            case HighGuid.Player -> createPlayer(realmId, dbId);
            case HighGuid.Item -> createItem(realmId, dbId);
            case HighGuid.StaticDoor, HighGuid.Transport -> createTransport(type, dbId);
            case HighGuid.Party, HighGuid.WowAccount, HighGuid.BNetAccount, HighGuid.GMTask, HighGuid.RaidGroup,
                    HighGuid.Spell, HighGuid.Mail, HighGuid.UserRouter, HighGuid.PVPQueueGroup, HighGuid.UserClient,
                    HighGuid.BattlePet, HighGuid.CommerceObj -> createGlobal(type, 0, dbId);
            case HighGuid.Guild -> createGuild(type, 0, dbId);
            default -> EMPTY;
        };
    }

    public static ObjectGuid create(HighGuid type, short ownerType, short ownerId, int counter) {
        if (type != HighGuid.ClientActor)
            return EMPTY;

        return createClientActor(ownerType, ownerId, counter);
    }

    public static ObjectGuid create(HighGuid type, int realmId, boolean builtIn, boolean trade, short zoneId, byte factionGroupMask, long counter) {
        if (type != HighGuid.ChatChannel)
            return EMPTY;

        return createChatChannel(realmId, builtIn, trade, zoneId, factionGroupMask, counter);
    }

    public static ObjectGuid create(HighGuid type, int realmId, short arg1, long counter) {
        if (type != HighGuid.MobileSession)
            return EMPTY;

        return createMobileSession(realmId, arg1, counter);
    }

    public static ObjectGuid create(HighGuid type, int realmId, byte arg1, byte arg2, long counter) {
        if (type != HighGuid.WebObj)
            return EMPTY;

        return createWebObj(realmId, arg1, arg2, counter);
    }

    public static ObjectGuid create(HighGuid type, byte arg1, byte arg2, byte arg3, byte arg4, boolean arg5, byte arg6, long counter) {
        if (type != HighGuid.LFGObject)
            return EMPTY;

        return createLFGObject(arg1, arg2, arg3, arg4, arg5, arg6, counter);
    }

    public static ObjectGuid create(HighGuid type, byte arg1, long counter) {
        if (type != HighGuid.LFGList)
            return EMPTY;

        return createLFGList(arg1, counter);
    }

    public static ObjectGuid create(HighGuid type, int realmId, int arg1, long counter) {
        return switch (type) {
            case HighGuid.PetBattle, HighGuid.UniqUserClient, HighGuid.ClientSession ->
                    createClient(type, realmId, arg1, counter);
            default -> EMPTY;
        };
    }

    public static ObjectGuid create(HighGuid type, int realmId, short mapId, int entry, long counter) {
        return switch (type) {
            case HighGuid.WorldTransaction, HighGuid.Conversation, HighGuid.Creature, HighGuid.Vehicle, HighGuid.Pet,
                    HighGuid.GameObject, HighGuid.DynamicObject, HighGuid.AreaTrigger, HighGuid.Corpse,
                    HighGuid.LootObject, HighGuid.SceneObject, HighGuid.Scenario, HighGuid.AIGroup, HighGuid.DynamicDoor,
                    HighGuid.Vignette, HighGuid.CallForHelp, HighGuid.AIResource, HighGuid.AILock, HighGuid.AILockTicket ->
                    createWorldObject(type, (byte) 0, realmId, mapId, 0, entry, counter);
            default -> EMPTY;
        };
    }

    public static boolean isMapSpecific(HighGuid high) {
        return switch (high) {
            case HighGuid.Conversation, HighGuid.Creature, HighGuid.Vehicle, HighGuid.Pet, HighGuid.GameObject,
                    HighGuid.DynamicObject, HighGuid.AreaTrigger, HighGuid.Corpse, HighGuid.LootObject,
                    HighGuid.SceneObject, HighGuid.Scenario, HighGuid.AIGroup, HighGuid.DynamicDoor, HighGuid.Vignette,
                    HighGuid.CallForHelp, HighGuid.AIResource, HighGuid.AILock, HighGuid.AILockTicket -> true;
            default -> false;
        };
    }

    public static boolean isRealmSpecific(HighGuid high) {
        return switch (high) {
            case HighGuid.Player, HighGuid.Item, HighGuid.ChatChannel, HighGuid.Transport, HighGuid.Guild -> true;
            default -> false;
        };
    }

    private static ObjectGuid createNull() {
        return ObjectGuid.EMPTY;
    }

    private static ObjectGuid createUniq(long id) {
        return new ObjectGuid(((long) HighGuid.Uniq.value << 58), id);
    }

    private static ObjectGuid createPlayer(int realmId, int dbId) {
        return new ObjectGuid(((long) HighGuid.Player.value << 58) | ((long) (realmId) << 42), dbId);
    }

    private static ObjectGuid createItem(int realmId, long dbId) {
        return new ObjectGuid(((long) HighGuid.Item.value << 58) | ((long) (realmId) << 42), dbId);
    }

    private static ObjectGuid createWorldObject(HighGuid type, byte subType, int realmId, short mapId, int serverId, int entry, long counter) {
        return new ObjectGuid((((long) type.value << 58)
                | ((long) (realmId & 0x1FFF) << 42)
                | ((long) (mapId & 0x1FFF) << 29)
                | ((long) (entry & 0x7FFFFF) << 6)
                | ((long) (subType) & 0x3F)),
                ((long) (serverId & 0xFFFFFF) << 40)
                        | (counter & 0xFFFFFFFFFFL));
    }

    private static ObjectGuid createTransport(HighGuid type, int counter) {
        return new ObjectGuid(((long) (type.value) << 58)
                | ((long) (counter) << 38),
                0L);
    }

    private static ObjectGuid createClientActor(short ownerType, short ownerId, int counter) {
        return new ObjectGuid(((long) (HighGuid.ClientActor.value) << 58)
                | ((long) (ownerType & 0x1FFF) << 42)
                | ((long) (ownerId & 0xFFFFFF) << 26),
                counter);
    }

    private static ObjectGuid createChatChannel(int realmId, boolean builtIn, boolean trade, short zoneId, byte factionGroupMask, long counter) {
        return new ObjectGuid(((long) HighGuid.ChatChannel.value << 58)
                | ((long) (realmId) & 0x1FFF) << 42
                | ((long) (builtIn ? 1 : 0) << 25)
                | ((long) (trade ? 1 : 0) << 24)
                | ((long) (zoneId & 0x3FFF) << 10)
                | ((long) (factionGroupMask & 0x3F) << 4),
                counter);
    }

    private static ObjectGuid createGlobal(HighGuid type, long dbIdHigh, long dbId) {
        return new ObjectGuid(((long) (type.value) << 58)
                | ((long) (dbIdHigh & 0x3FFFFFFFFFFFFFFL)),
                dbId);
    }

    private static ObjectGuid createGuild(HighGuid type, int realmId, long dbId) {
        return new ObjectGuid(((long) (type.value) << 58)
                | ((long) (realmId) << 42),
                dbId);
    }

    private static ObjectGuid createMobileSession(int realmId, short arg1, long counter) {
        return new ObjectGuid(((long) (HighGuid.MobileSession.value) << 58)
                | ((long) (realmId) << 42)
                | ((long) (arg1 & 0x1FF) << 33),
                counter);
    }

    private static ObjectGuid createWebObj(int realmId, byte arg1, byte arg2, long counter) {
        return new ObjectGuid(((long) (HighGuid.WebObj.value) << 58)
                | ((long) (realmId & 0x1FFF) << 42)
                | ((long) (arg1 & 0x1F) << 37)
                | ((long) (arg2 & 0x3) << 35),
                counter);
    }

    private static ObjectGuid createLFGObject(byte arg1, byte arg2, byte arg3, byte arg4, boolean arg5, byte arg6, long counter) {
        return new ObjectGuid(((long) (HighGuid.LFGObject.value) << 58)
                | ((long) (arg1 & 0xF) << 54)
                | ((long) (arg2 & 0xF) << 50)
                | ((long) (arg3 & 0xF) << 46)
                | ((long) (arg4 & 0xFF) << 38)
                | ((long) (arg5 ? 1 : 0) << 37)
                | ((long) (arg6 & 0x3) << 35),
                counter);
    }

    private static ObjectGuid createLFGList(byte arg1, long counter) {
        return new ObjectGuid(((long) (HighGuid.LFGObject.value) << 58)
                | ((long) (arg1 & 0xF) << 54),
                counter);
    }

    private static ObjectGuid createClient(HighGuid type, int realmId, int arg1, long counter) {
        return new ObjectGuid(((long) (type.value) << 58)
                | ((long) (realmId & 0x1FFF) << 42)
                | ((arg1 & 0xFFFFFFFFL) << 10),
                counter);
    }

    public long highValue() {
        long result = 0;
        for (int i = 15; i > 7; i--) {
            result = (long) (data[i] & 0xff) << (15 - i) | result;
        }
        return result;
    }

    public long lowValue() {
        long result = 0;
        for (int i = 7; i > -1; i--) {
            result = (long) (data[i] & 0xff) << (7 - i) | result;
        }
        return result;
    }

    public String stringValue() {
        return SecureUtils.bytesToHexString(data);
    }

    public byte[] bytes() {
        return data;
    }

    public void bytes(byte[] guid) {
        System.arraycopy(guid, 0, data, 0, OBJECT_GUID_DATA_SIZE);
    }

    private void bytes(long high, long low) {
        data[15] = (byte) (high & 0xff);
        data[14] = (byte) (high >>> 8 & 0xff);
        data[13] = (byte) (high >>> 16 & 0xff);
        data[12] = (byte) (high >>> 24 & 0xff);
        data[11] = (byte) (high >>> 32 & 0xff);
        data[10] = (byte) (high >>> 40 & 0xff);
        data[9] = (byte) (high >>> 48 & 0xff);
        data[8] = (byte) (high >>> 56 & 0xff);
        data[7] = (byte) (low & 0xff);
        data[6] = (byte) (low >>> 8 & 0xff);
        data[5] = (byte) (low >>> 16 & 0xff);
        data[4] = (byte) (low >>> 24 & 0xff);
        data[3] = (byte) (low >>> 32 & 0xff);
        data[2] = (byte) (low >>> 40 & 0xff);
        data[1] = (byte) (low >>> 48 & 0xff);
        data[0] = (byte) (low >>> 56 & 0xff);
    }

    public void clear() {
        Arrays.fill(data, (byte) 0);
    }

    public HighGuid highGuid() {
        long highGuidVal = (highValue() >>> 58) & 0x3F;
        return HighGuid.get((int) highGuidVal);
    }

    public int realmId() {
        return (int) ((highValue() >>> 42) & 0x1FFF);
    }

    public int mapId() {
        return (int) ((highValue() >> 29) & 0x1FFF);
    }

    public int entry() {
        return (int) ((highValue() >> 6) & 0x7FFFFF);
    }

    public int subType() {
        return (int) (highValue() & 0x3F);
    }

    public long counter() {
        if (Objects.requireNonNull(highGuid()) == HighGuid.Transport) {
            return ((highValue() >> 38) & 0xFFFFF);
        }
        return data[0] & 0x000000FFFFFFFFFFL;
    }

    public boolean isEmpty() {
        return lowValue() == 0 && highValue() == 0;
    }

    public boolean isCreature() {
        return highGuid() == HighGuid.Creature;
    }

    public boolean isPet() {
        return highGuid() == HighGuid.Pet;
    }

    public boolean isVehicle() {
        return highGuid() == HighGuid.Vehicle;
    }

    public boolean isCreatureOrPet() {
        return isCreature() || isPet();
    }

    public boolean isCreatureOrVehicle() {
        return isCreature() || isVehicle();
    }

    public boolean isAnyTypeCreature() {
        return isCreature() || isPet() || isVehicle();
    }

    public boolean isPlayer() {
        return !isEmpty() && highGuid() == HighGuid.Player;
    }

    public boolean isUnit() {
        return isAnyTypeCreature() || isPlayer();
    }

    public boolean isItem() {
        return highGuid() == HighGuid.Item;
    }

    public boolean isGameObject() {
        return highGuid() == HighGuid.GameObject;
    }

    public boolean isDynamicObject() {
        return highGuid() == HighGuid.DynamicObject;
    }

    public boolean isCorpse() {
        return highGuid() == HighGuid.Corpse;
    }

    public boolean isAreaTrigger() {
        return highGuid() == HighGuid.AreaTrigger;
    }

    public boolean isMOTransport() {
        return highGuid() == HighGuid.Transport;
    }

    public boolean isAnyTypeGameObject() {
        return isGameObject() || isMOTransport();
    }

    public boolean isParty() {
        return highGuid() == HighGuid.Party;
    }

    public boolean isGuild() {
        return highGuid() == HighGuid.Guild;
    }

    public boolean isSceneObject() {
        return highGuid() == HighGuid.SceneObject;
    }

    public boolean isConversation() {
        return highGuid() == HighGuid.Conversation;
    }

    public boolean isCast() {
        return highGuid() == HighGuid.Cast;
    }


    @Override
    public int compareTo(ObjectGuid o) {
        return Arrays.compare(data, o.data);
    }
}
