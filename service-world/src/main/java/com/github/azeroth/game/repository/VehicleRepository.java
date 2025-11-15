package com.github.azeroth.game.repository;


import com.github.azeroth.game.domain.misc.RaceUnlockRequirement;
import com.github.azeroth.game.domain.misc.SystemText;
import com.github.azeroth.game.domain.misc.ClassExpansionRequirement;
import com.github.azeroth.game.domain.vehicle.VehicleAccessory;
import com.github.azeroth.game.domain.vehicle.VehicleSeatAddon;
import com.github.azeroth.game.domain.vehicle.VehicleTemplate;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Stream;

@Repository
public interface VehicleRepository {



    @Query("SELECT creatureId, despawnDelayMs FROM vehicle_template")
    Stream<VehicleTemplate> streamAllVehicleTemplates();

    @Query("SELECT `entry`, `accessory_entry`, `seat_id`, `minion`, `summontype`, `summontimer` FROM `vehicle_template_accessory`")
    Stream<VehicleAccessory> streamAllVehicleTemplateAccessories();

    @Query("SELECT `guid`, `accessory_entry`, `seat_id`, `minion`, `summontype`, `summontimer`, `RideSpellID` FROM `vehicle_accessory`")
    Stream<VehicleAccessory> streamAllVehicleAccessories();

    @Query("SELECT `SeatEntry`, `SeatOrientation`, `ExitParamX`, `ExitParamY`, `ExitParamZ`, `ExitParamO`, `ExitParamValue` FROM `vehicle_seat_addon`")
    Stream<VehicleSeatAddon> streamAllVehicleSeatAddons();
}
