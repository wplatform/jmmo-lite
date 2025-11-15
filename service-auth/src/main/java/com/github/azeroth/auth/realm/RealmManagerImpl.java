package com.github.azeroth.auth.realm;

import com.github.azeroth.auth.domain.BuildInfo;
import com.github.azeroth.auth.repository.BuildInfoRepository;
import com.github.azeroth.auth.repository.RealmListRepository;
import com.github.azeroth.common.Logs;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class RealmManagerImpl implements RealmManager {


    private final BuildInfoRepository buildInfoRepo;
    private final RealmListRepository realmListRepo;

    private Map<Integer, BuildInfo> clientBuildMap;
    private Map<RealmKey, Realm> realmMap;

    @Getter
    @Setter
    private RealmKey currentRealmId;






    public BuildInfo getBuildInfo(int build) {
        return clientBuildMap.get(build);
    }

    @Override
    public Set<RealmKey> realmKeys() {
        return realmMap.keySet();
    }

    @Override
    public Realm getRealmByKey(RealmKey key) {
        return realmMap.get(key);
    }

    public void loadBuildInfo() {

        List<BuildInfo> builds = new ArrayList<>();
        try (var items = buildInfoRepo.stream()) {
            items.forEach(buildInfo -> {
                if (buildInfo.getHotfixVersion().length() > 3) {
                    Logs.SQL.error("LoadBuildInfo: invalid hotfix version for `build` {} in `build_info`, skipped.", buildInfo.getBuild());
                    return;
                }
                builds.add(buildInfo);
            });
        }

        try (var items = buildInfoRepo.streamBuildAuthKey()) {
            items.forEach(buildAuthKey -> {
                Optional<BuildInfo> buildInfo = builds.stream().filter(e -> Objects.equals(e.getBuild(), buildAuthKey.getBuild())).findFirst();
                if (buildInfo.isEmpty()) {
                    Logs.SQL.error("LoadBuildInfo: Unknown `build` {} in `build_auth_key` - missing from `build_info`, skipped.", buildAuthKey.getBuild());
                    return;
                }
                if (PlatformType.isValid(buildAuthKey.getPlatform())) {
                    Logs.SQL.error("LoadBuildInfo: Invalid platform {} for `build` {} in `build_auth_key`, skipped.", buildAuthKey.getPlatform(), buildAuthKey.getBuild());
                    return;
                }
                if (Arch.isValid(buildAuthKey.getArch())) {
                    Logs.SQL.error("LoadBuildInfo: Invalid `arch` {} for `build` {} in `build_auth_key`, skipped.", buildAuthKey.getArch(), buildAuthKey.getBuild());
                    return;
                }
                if (!Type.isValid(buildAuthKey.getType())) {
                    Logs.SQL.error("LoadBuildInfo: Invalid `type` {} for `build` {} in `build_auth_key`, skipped.", buildAuthKey.getType(), buildAuthKey.getBuild());
                    return;
                }

                ArrayList<AuthKey> authKeys = new ArrayList<>();
                buildInfo.get().setAuthKeys(authKeys);
                authKeys.add(new AuthKey(VariantId.of(buildAuthKey.getPlatform(), buildAuthKey.getArch(), buildAuthKey.getType()), buildAuthKey.getKey()));
            });
        }


        this.clientBuildMap = builds.stream().collect(Collectors.toMap(BuildInfo::getBuild, Function.identity()));

    }


    public void loadRealmList() {
        Logs.REALM_LIST.info("Updating Realm List...");
        realmListRepo.streamAll().map(v -> {
            Realm realm = new Realm();
            realm.setId(RealmKey.createRealmKey(v.getRegion(), v.getBattlegroup(), v.getId()));
            realm.setBuild(v.getGameBuild());
            realm.setName(v.getName());
            realm.setAddresses(Stream.of(v.getAddress(), v.getLocalAddress(), v.getAddress3(), v.getAddress4()).map(this::toInetAddress).filter(Objects::nonNull).toList());
            realm.setLocalSubnetMask(toInetAddress(v.getLocalSubnetMask()));
            int icon = v.getIcon() == null ? 0 : v.getIcon();
            icon = icon == Realm.REALM_TYPE_FFA_PVP ? Realm.REALM_TYPE_PVP : icon;
            icon = icon >= Realm.REALM_TYPE_FFA_PVP ? Realm.REALM_TYPE_NORMAL : icon;
            realm.setType(icon);
            realm.setTimezone(v.getTimezone());
            realm.setAllowedSecurityLevel(v.getAllowedSecurityLevel());
            realm.setPort(v.getPort());
            realm.setPopulationLevel(convertLegacyPopulationState(v.getFlag(), v.getPopulation()));
            return realm;
        }).filter(realm -> {
            if (realm.getAddresses().isEmpty()) {
                Logs.REALM_LIST.error("Could not resolve any address for realm \"{}\" id {}", realm.getName(), realm.getId());
                return false;
            }
            return true;
        }).forEach(realm -> {
            realmMap.put(realm.getId(), realm);
        });
    }

    private InetAddress toInetAddress(String hostName) {
        try {

            return InetAddress.getByName(hostName);
        } catch (UnknownHostException e) {
            throw new IllegalArgumentException(e);
        }
    }


    private RealmPopulationState convertLegacyPopulationState(int legacyRealmFlags, float population) {
        if ((legacyRealmFlags & Realm.REALM_FLAG_OFFLINE) != 0)
            return RealmPopulationState.Offline;
        if ((legacyRealmFlags & Realm.REALM_FLAG_RECOMMENDED) != 0)
            return RealmPopulationState.Recommended;
        if ((legacyRealmFlags & Realm.REALM_FLAG_NEW) != 0)
            return RealmPopulationState.New;
        if ((legacyRealmFlags & Realm.REALM_FLAG_FULL) != 0 || population > 0.95f)
            return RealmPopulationState.Full;
        if (population > 0.66f)
            return RealmPopulationState.High;
        if (population > 0.33f)
            return RealmPopulationState.Medium;
        return RealmPopulationState.Low;
    }


}
