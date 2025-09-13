package com.github.azeroth.game.networking.packet.party;


import com.github.azeroth.game.entity.player.Player;
import com.github.azeroth.game.networking.ServerPacket;
import com.github.azeroth.game.spell.AuraFlags;
import game.*;

class PartyMemberFullState extends ServerPacket {
    public boolean forEnemy;
    public ObjectGuid memberGuid = ObjectGuid.EMPTY;
    public PartymemberStats memberStats = new partyMemberStats();

    public PartyMemberFullState() {
        super(ServerOpcode.PartyMemberFullState);
    }

    @Override
    public void write() {
        this.writeBit(forEnemy);
        this.flushBits();

        memberStats.write(this);
        this.writeGuid(memberGuid);
    }

    public final void initialize(Player player) {
        forEnemy = false;

        memberGuid = player.getGUID();

        // Status
        memberStats.status = GroupMemberOnlineStatus.online;

        if (player.isPvP()) {
            memberStats.status = GroupMemberOnlineStatus.forValue(memberStats.status.getValue() | GroupMemberOnlineStatus.PVP.getValue());
        }

        if (!player.isAlive()) {
            if (player.hasPlayerFlag(playerFlags.Ghost)) {
                memberStats.status = GroupMemberOnlineStatus.forValue(memberStats.status.getValue() | GroupMemberOnlineStatus.Ghost.getValue());
            } else {
                memberStats.status = GroupMemberOnlineStatus.forValue(memberStats.status.getValue() | GroupMemberOnlineStatus.Dead.getValue());
            }
        }

        if (player.isFFAPvP()) {
            memberStats.status = GroupMemberOnlineStatus.forValue(memberStats.status.getValue() | GroupMemberOnlineStatus.PVPFFA.getValue());
        }

        if (player.isAFK()) {
            memberStats.status = GroupMemberOnlineStatus.forValue(memberStats.status.getValue() | GroupMemberOnlineStatus.AFK.getValue());
        }

        if (player.isDND()) {
            memberStats.status = GroupMemberOnlineStatus.forValue(memberStats.status.getValue() | GroupMemberOnlineStatus.DND.getValue());
        }

        if (player.getVehicle1()) {
            memberStats.status = GroupMemberOnlineStatus.forValue(memberStats.status.getValue() | GroupMemberOnlineStatus.vehicle.getValue());
        }

        // Level
        memberStats.level = (short) player.getLevel();

        // Health
        memberStats.currentHealth = (int) player.getHealth();
        memberStats.maxHealth = (int) player.getMaxHealth();

        // Power
        memberStats.powerType = (byte) player.getDisplayPowerType().getValue();
        memberStats.powerDisplayID = 0;
        memberStats.currentPower = (short) player.getPower(player.getDisplayPowerType());
        memberStats.maxPower = (short) player.getMaxPower(player.getDisplayPowerType());

        // Position
        memberStats.zoneID = (short) player.getZoneId();
        memberStats.positionX = (short) player.getLocation().getX();
        memberStats.positionY = (short) (player.getLocation().getY());
        memberStats.positionZ = (short) (player.getLocation().getZ());

        memberStats.specID = (short) player.getPrimarySpecialization();
        memberStats.PartyType[0] = (byte) (player.getPlayerData().partyType & 0xF);
        memberStats.PartyType[1] = (byte) (player.getPlayerData().partyType >> 4);
        memberStats.wmoGroupID = 0;
        memberStats.wmoDoodadPlacementID = 0;

        // Vehicle
        var vehicle = player.getVehicle1();

        if (vehicle != null) {
            var vehicleSeat = vehicle.GetSeatForPassenger(player);

            if (vehicleSeat != null) {
                memberStats.vehicleSeat = (int) vehicleSeat.id;
            }
        }

        // Auras
        for (var aurApp : player.getVisibleAuras()) {
            PartyMemberAuraStates aura = new PartyMemberAuraStates();
            aura.spellID = (int) aurApp.getBase().getId();
            aura.activeFlags = aurApp.getEffectMask().ToUMask();
            aura.flags = (byte) aurApp.getFlags().getValue();

            if (aurApp.getFlags().hasFlag(AuraFlags.SCALABLE)) {
                for (var aurEff : aurApp.getBase().getAuraEffects().entrySet()) {
                    if (aurApp.hasEffect(aurEff.getValue().effIndex)) {
                        aura.points.add((float) aurEff.getValue().amount);
                    }
                }
            }

            memberStats.auras.add(aura);
        }

        // Phases
        PhasingHandler.fillPartyMemberPhase(memberStats.phases, player.getPhaseShift());

        // Pet
        if (player.getCurrentPet()) {
            var pet = player.getCurrentPet();

            memberStats.petStats = new PartyMemberPetStats();

            memberStats.petStats.GUID = pet.getGUID();
            memberStats.petStats.name = pet.getName();
            memberStats.petStats.modelId = (short) pet.getDisplayId();

            memberStats.petStats.currentHealth = (int) pet.getHealth();
            memberStats.petStats.maxHealth = (int) pet.getMaxHealth();

            for (var aurApp : pet.getVisibleAuras()) {
                PartyMemberAuraStates aura = new PartyMemberAuraStates();

                aura.spellID = (int) aurApp.getBase().getId();
                aura.activeFlags = aurApp.getEffectMask().ToUMask();
                aura.flags = (byte) aurApp.getFlags().getValue();

                if (aurApp.getFlags().hasFlag(AuraFlags.SCALABLE)) {
                    for (var aurEff : aurApp.getBase().getAuraEffects().entrySet()) {
                        if (aurApp.hasEffect(aurEff.getValue().effIndex)) {
                            aura.points.add((float) aurEff.getValue().amount);
                        }
                    }
                }

                memberStats.petStats.auras.add(aura);
            }
        }
    }
}
