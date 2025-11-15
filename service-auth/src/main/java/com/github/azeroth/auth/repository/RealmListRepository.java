package com.github.azeroth.auth.repository;

import com.github.azeroth.auth.domain.RealmList;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.stream.Stream;

public interface RealmListRepository extends CrudRepository<RealmList, Integer> {

    @Query("SELECT id, name, address, localAddress, address3, address4, port, icon, flag, timezone, allowedSecurityLevel, population, gamebuild, Region, Battlegroup FROM realmlist WHERE flag <> 3 ORDER BY name")
    Stream<RealmList> streamAll();

}