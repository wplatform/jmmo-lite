package com.github.azeroth.game.chat;



class PetCommands {
    
    private static boolean handlePetCreateCommand(CommandHandler handler) {
        var player = handler.getSession().getPlayer();
        var creatureTarget = handler.getSelectedCreature();

        if (!creatureTarget || creatureTarget.isPet() || creatureTarget.isTypeId(TypeId.PLAYER)) {
            handler.sendSysMessage(SysMessage.SelectCreature);

            return false;
        }

        var creatureTemplate = creatureTarget.getCreatureTemplate();

        // Creatures with family creatureFamily.None crashes the server
        if (creatureTemplate.family == creatureFamily.NONE) {
            handler.sendSysMessage("This creature cannot be tamed. (Family id: 0).");

            return false;
        }

        if (!player.getPetGUID().isEmpty()) {
            handler.sendSysMessage("You already have a pet");

            return false;
        }

        // Everything looks OK, create new pet
        var pet = player.createTamedPetFrom(creatureTarget);

        // "kill" original creature
        creatureTarget.despawnOrUnsummon();

        // prepare visual effect for levelup
        pet.setLevel(player.getLevel() - 1);

        // add to world
        pet.getMap().addToMap(pet.toCreature());

        // visual effect for levelup
        pet.setLevel(player.getLevel());

        // caster have pet now
        player.setMinion(pet, true);

        pet.SavePetToDB(PetSaveMode.AsCurrent);
        player.petSpellInitialize();

        return true;
    }

    
    private static boolean handlePetLearnCommand(CommandHandler handler, int spellId) {
        var pet = getSelectedPlayerPetOrOwn(handler);

        if (!pet) {
            handler.sendSysMessage(SysMessage.SelectPlayerOrPet);

            return false;
        }

        if (spellId == 0 || !global.getSpellMgr().hasSpellInfo(spellId, Difficulty.NONE)) {
            return false;
        }

        // Check if pet already has it
        if (pet.hasSpell(spellId)) {
            handler.sendSysMessage("Pet already has spell: {0}", spellId);

            return false;
        }

        // Check if spell is valid
        var spellInfo = global.getSpellMgr().getSpellInfo(spellId, Difficulty.NONE);

        if (spellInfo == null || !global.getSpellMgr().isSpellValid(spellInfo)) {
            handler.sendSysMessage(SysMessage.CommandSpellBroken, spellId);

            return false;
        }

        pet.learnSpell(spellId);

        handler.sendSysMessage("Pet has learned spell {0}", spellId);

        return true;
    }

    
    private static boolean handlePetUnlearnCommand(CommandHandler handler, int spellId) {
        var pet = getSelectedPlayerPetOrOwn(handler);

        if (!pet) {
            handler.sendSysMessage(SysMessage.SelectPlayerOrPet);

            return false;
        }

        if (pet.hasSpell(spellId)) {
            pet.removeSpell(spellId, false);
        } else {
            handler.sendSysMessage("Pet doesn't have that spell");
        }

        return true;
    }

    
    private static boolean handlePetLevelCommand(CommandHandler handler, int level) {
        var pet = getSelectedPlayerPetOrOwn(handler);
        var owner = pet ? pet.getOwningPlayer() : null;

        if (!pet || !owner) {
            handler.sendSysMessage(SysMessage.SelectPlayerOrPet);

            return false;
        }

        if (level == 0) {
            level = (int) (owner.getLevel() - pet.getLevel());
        }

        if (level == 0 || level < -SharedConst.StrongMaxLevel || level > SharedConst.StrongMaxLevel) {
            handler.sendSysMessage(SysMessage.BadValue);

            return false;
        }

        var newLevel = (int) pet.getLevel() + level;

        if (newLevel < 1) {
            newLevel = 1;
        } else if (newLevel > owner.getLevel()) {
            newLevel = (int) owner.getLevel();
        }

        pet.GivePetLevel(newLevel);

        return true;
    }

    private static Pet getSelectedPlayerPetOrOwn(CommandHandler handler) {
        var target = handler.getSelectedUnit();

        if (target) {
            if (target.isTypeId(TypeId.PLAYER)) {
                return target.toPlayer().getCurrentPet();
            }

            if (target.isPet()) {
                return target.getAsPet();
            }

            return null;
        }

        var player = handler.getSession().getPlayer();

        return player ? player.getCurrentPet() : null;
    }
}
