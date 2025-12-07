package com.github.azeroth.character.service;

import com.github.azeroth.character.repository.CharacterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
public class CharacterService {

    private final CharacterRepository characterRepo;

    @Transactional
    public void deleteItem(int itemGuid) {
        characterRepo.deleteItemInstance(itemGuid);
        characterRepo.deleteItemInstanceGems(itemGuid);
        characterRepo.deleteItemInstanceTransmog(itemGuid);
        characterRepo.deleteItemInstanceArtifact(itemGuid);
        characterRepo.deleteItemInstanceArtifactPowers(itemGuid);
        characterRepo.deleteItemInstanceModifiers(itemGuid);
        characterRepo.deleteGift(itemGuid);
    }

}
