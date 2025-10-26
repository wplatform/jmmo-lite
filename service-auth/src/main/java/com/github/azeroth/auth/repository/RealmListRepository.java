package com.github.azeroth.auth.repository;

import com.github.azeroth.auth.domain.RealmList;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.stream.Stream;

public interface RealmListRepository extends CrudRepository<RealmList, Integer> {

    @Query("from Realmlist a where a.flag <> 3 ORDER BY a.name")
    Stream<RealmList> stream();


    @Query("SELECT id, name FROM `realmlist`")
    Stream<RealmList> streamNameAll();
}