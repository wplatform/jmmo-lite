package com.github.azeroth.auth.realm;

import com.github.azeroth.auth.domain.BuildInfo;

import java.util.Set;

public interface RealmManager {

    BuildInfo getBuildInfo(int build);


    Set<RealmKey> realmKeys();


    RealmKey getCurrentRealmId();

    default Realm getCurrentRealm() {
        if (getCurrentRealmId() != null)
            return getRealmByKey(getCurrentRealmId());
        return null;
    }


    Realm getRealmByKey(RealmKey key);



}
