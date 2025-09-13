package com.github.azeroth.game.battlepet;


import com.github.azeroth.game.networking.packet.battlepet.BattlePetStruct;

public class BattlePet {
    public BattlePetStruct packetInfo = new BattlePetStruct();
    public long nameTimestamp;
    public com.github.azeroth.game.entity.unit.declinedName declinedName;
    public BattlePetsaveInfo saveInfo = BattlePetSaveInfo.values()[0];

    public final void calculateStats() {
        // get base breed stats
        var breedState = BattlePetMgr.BATTLEPETBREEDSTATES.get(packetInfo.breed);

        if (breedState == null) // non existing breed id
        {
            return;
        }

        float health = breedState[BattlePetState.StatStamina];
        float power = breedState[BattlePetState.StatPower];
        float speed = breedState[BattlePetState.StatSpeed];

        // modify stats depending on species - not all pets have this
        var speciesState = BattlePetMgr.BATTLEPETSPECIESSTATES.get(packetInfo.species);

        if (speciesState != null) {
            health += speciesState[BattlePetState.StatStamina];
            power += speciesState[BattlePetState.StatPower];
            speed += speciesState[BattlePetState.StatSpeed];
        }

        // modify stats by quality
        for (var battlePetBreedQuality : CliDB.BattlePetBreedQualityStorage.values()) {
            if (battlePetBreedQuality.QualityEnum == packetInfo.quality) {
                health *= battlePetBreedQuality.StateMultiplier;
                power *= battlePetBreedQuality.StateMultiplier;
                speed *= battlePetBreedQuality.StateMultiplier;

                break;
            }
        }

        // TOOD: add check if pet has existing quality
        // scale stats depending on level
        health *= packetInfo.level;
        power *= packetInfo.level;
        speed *= packetInfo.level;

        // set stats
        // round, ceil or floor? verify this
        packetInfo.maxHealth = (int) ((Math.rint(health / 20) + 100));
        packetInfo.power = (int) (Math.rint(power / 100));
        packetInfo.speed = (int) (Math.rint(speed / 100));
    }
}
