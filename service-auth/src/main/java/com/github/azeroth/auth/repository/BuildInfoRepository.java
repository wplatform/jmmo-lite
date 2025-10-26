package com.github.azeroth.auth.repository;


import com.github.azeroth.auth.domain.BuildAuthKey;
import com.github.azeroth.auth.domain.BuildInfo;
import org.springframework.data.jdbc.repository.query.Query;


import java.util.stream.Stream;

public interface BuildInfoRepository {
    @Query("SELECT majorVersion, minorVersion, bugfixVersion, hotfixVersion, build FROM build_info ORDER BY build ASC")
    Stream<BuildInfo> stream();


    @Query("SELECT `build`, `platform`, `arch`, `type`, `key` FROM `build_auth_key`")
    Stream<BuildAuthKey> streamBuildAuthKey();

}