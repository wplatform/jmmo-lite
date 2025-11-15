package com.github.azeroth.game.repository;



import com.github.azeroth.game.domain.reputation.RepRewardRate;
import com.github.azeroth.game.domain.reputation.RepSpilloverTemplate;
import com.github.azeroth.game.domain.reputation.ReputationOnKill;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Stream;

@Repository
public interface ReputationRepository {

    
    @Query(value = "SELECT faction, faction1, rate_1, rank_1, faction2, rate_2, rank_2, faction3, rate_3, rank_3, faction4, rate_4, rank_4, faction5, rate_5, rank_5 FROM reputation_spillover_template")
    Stream<RepSpilloverTemplate> streamAllReputationSpilloverTemplate();
    
    @Query("SELECT creature_id, RewOnKillRepFaction1, RewOnKillRepFaction2, IsTeamAward1, MaxStanding1, RewOnKillRepValue1, isTeamAward2, MaxStanding2, RewOnKillRepValue2, TeamDependent FROM creature_onkill_reputation")
    Stream<ReputationOnKill> streamAllCreatureOnKillReputation();


    
    @Query("SELECT faction, quest_rate, quest_daily_rate, quest_weekly_rate, quest_monthly_rate, quest_repeatable_rate, creature_rate, spell_rate FROM reputation_reward_rate")
    Stream<RepRewardRate> streamReputationRewardRate();
}
