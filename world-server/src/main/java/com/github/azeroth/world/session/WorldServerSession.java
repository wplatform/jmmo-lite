package com.github.azeroth.world.session;

import com.github.azeroth.auth.dto.AccountType;
import com.github.azeroth.auth.dto.RBACData;
import com.github.azeroth.auth.dto.RBACPermissions;
import com.github.azeroth.auth.realm.VariantId;
import com.github.azeroth.auth.repository.AccountRepository;
import com.github.azeroth.auth.repository.BNetAccountRepository;
import com.github.azeroth.character.domain.AccountData;
import com.github.azeroth.character.domain.AccountTutorial;
import com.github.azeroth.game.networking.packet.clientconfig.AccountDataType;
import com.github.azeroth.character.repository.CharacterRepository;
import com.github.azeroth.common.Locale;
import com.github.azeroth.common.Logs;
import com.github.azeroth.defines.Expansion;
import com.github.azeroth.defines.SharedDefine;
import com.github.azeroth.game.domain.object.ObjectGuid;
import com.github.azeroth.game.entity.player.Collector;
import com.github.azeroth.game.entity.player.Player;
import com.github.azeroth.game.world.WorldContext;
import com.github.azeroth.game.world.WorldSession;
import com.github.azeroth.time.StopWatch;
import com.github.azeroth.utils.StringUtil;
import com.github.azeroth.utils.Utils;
import com.github.azeroth.world.World;

import com.github.azeroth.world.network.ConnectionType;
import com.github.azeroth.world.network.WorldConnection;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.time.ZoneOffset;
import java.util.*;

@Slf4j
@Getter
@Setter
@RequiredArgsConstructor
public class WorldServerSession implements WorldSession {

    private static final int ALL_ACCOUNT_DATA_CACHE_MASK = 0x000FFFFF;
    private static final int GLOBAL_CACHE_MASK = 0x000BA515;
    private static final int PER_CHARACTER_CACHE_MASK = 0x00045AEA;

    private final World world;
    private final int accountId;
    private final String accountName;
    private final int battleNetAccountId;
    private final ObjectGuid battleNetAccountGuid;
    private final AccountType security;
    private final Expansion accountExpansion;
    private final Expansion expansion;
    private final String os;
    private final int clientBuild;
    private final VariantId clientBuildVariant;
    private final Locale locale;
    private final ZoneOffset zoneOffset;
    private final int recruiterId;

    private Player player;
    private int latency;

    private byte[] realmListSecret;
    private HashMap<Integer, Byte> realmCharacterCounts;
    private int battleNetRequestToken;


    private long logoutTime;
    private boolean playerLogout;                                // code processed in LogoutPlayer
    private boolean playerRecentlyLogout;
    private boolean playerSave;
    private int[] tutorials = new int[SharedDefine.MAX_ACCOUNT_TUTORIAL_VALUES];
    private byte tutorialsChanged;

    private List<String> registeredAddonPrefixes;
    private boolean filterAddonMessages;
    private boolean isRecruiter;
    private RBACData rbacData;
    private int expireTime;
    private boolean forceExit;

    private final Map<String, Object> attributes = new HashMap<>();

    private final Collector collector = new Collector(this);
    private final EnumMap<AccountDataType, AccountData> accountData = new EnumMap<>(AccountDataType.class);
    private final EnumMap<ConnectionType, WorldConnection> connections = new EnumMap<>(ConnectionType.class);
    private final StopWatch stopWatch = new StopWatch();


    @Override
    public WorldContext getWorldContext() {
        return world;
    }


    @Override
    public boolean hasPermission(RBACPermissions rbacPermissions) {
        return false;
    }


    public void initialize() {

        var accountRepo = world.getBean(AccountRepository.class);
        var characterRepo = world.getBean(CharacterRepository.class);
        var bNetAccountRepo = world.getBean(BNetAccountRepository.class);

        var accountData = characterRepo.selectAccountData(this.accountId);
        var accountTutorial = characterRepo.selectAccountTutorial(this.accountId);

        var accountToys = bNetAccountRepo.findAccountToys(this.battleNetAccountId);
        var battlePets = bNetAccountRepo.findBattlePets(this.battleNetAccountId, world.getWorldSettings().realmID);
        var battlePetSlots = bNetAccountRepo.findBattlePetSlots(this.battleNetAccountId);
        var accountHeirlooms = bNetAccountRepo.findAccountHeirlooms(this.battleNetAccountId);
        var accountMounts = bNetAccountRepo.findAccountMounts(this.battleNetAccountId);
        var characterCounts = accountRepo.findCharacterCountsByAccountId(this.accountId);
        var itemAppearances = bNetAccountRepo.findItemAppearances(this.battleNetAccountId);
        var favoriteItemAppearances = bNetAccountRepo.selectFavoriteItemAppearances(this.battleNetAccountId);
        var transmogIllusions = bNetAccountRepo.findTransmogIllusions(this.battleNetAccountId);

        loadAccountData(accountData, GLOBAL_CACHE_MASK);
        loadTutorialsData(accountTutorial);
        collector.load();


    }


    private void loadAccountData(List<AccountData> accountData, int mask) {
        accountData.forEach(accountDate -> {
            if (!Utils.checkEnumIndex(accountDate.getType(), AccountDataType.values())) {
                Logs.MISC.error("Table `{}` have invalid account data type ({}), ignore.",
                        mask == GLOBAL_CACHE_MASK ? "account_data" : "character_account_data", accountDate.getType());
                return;
            }

            if ((mask & (1 << accountDate.getType())) == 0) {
                Logs.MISC.error("Table `{}` have non appropriate for table account data type ({}), ignore.",
                        mask == GLOBAL_CACHE_MASK ? "account_data" : "character_account_data", accountDate.getType());
                return;
            }
            AccountDataType type = AccountDataType.values()[accountDate.getType()];
            this.accountData.put(type, accountDate);
        });
    }


    private void loadTutorialsData(AccountTutorial accountTutorial) {
        if (accountTutorial != null) {
            for (int i = 0; i < SharedDefine.MAX_ACCOUNT_TUTORIAL_VALUES; ++i)
                tutorials[i] = accountTutorial.getTutorials()[i];
            tutorialsChanged |= AccountTutorial.TUTORIALS_FLAG_LOADED_FROM_DB;
        }
        tutorialsChanged &= (byte) ~AccountTutorial.TUTORIALS_FLAG_CHANGED;
    }


    public void kickout(String reason) {
        Logs.NETWORK.info("Account: {} Character: '{}' {} kicked with reason: {}",
                accountId, player != null ? player.getName() : "<none>",
                player != null ? player.getGUID() : "", reason);
        for (ConnectionType connectionType : ConnectionType.values()) {
            WorldConnection connection = connections.get(connectionType);
            if (connection != null) {
                connection.close();
                forceExit = true;
            }
        }
    }

    @Override
    public void setAttribute(String key, Object value) {
        attributes.put(key, value);
    }

    @Override
    public Object getAttribute(String key) {
        return attributes.get(key);
    }

    @Override
    public Object removeAttribute(String key) {
        return attributes.remove(key);
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof WorldServerSession that)) return false;
        return accountId == that.accountId && battleNetAccountId == that.battleNetAccountId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(accountId, battleNetAccountId);
    }

    @Override
    public String toString() {
        if (player != null)
            return StringUtil.format("WorldSession[Player: {} {}, Account: {}]", player.getName(), player.getGUID(), getAccountId());

        if (getAttribute("loadingPlayer") != null)
            return StringUtil.format("WorldSession[Player: Logging in: {}, Account: {}]", getAttribute("loadingPlayer"), getAccountId());

        return StringUtil.format("WorldSession[Player: Account: {}]", getAccountId());
    }
}
