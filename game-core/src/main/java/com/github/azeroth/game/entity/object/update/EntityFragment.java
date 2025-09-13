package com.github.azeroth.game.entity.object.update;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EntityFragment {
    CGObject                    (0), //  UPDATEABLE, INDIRECT,
    Tag_Item                    (1), //  TAG,
    Tag_Container               (2), //  TAG,
    Tag_AzeriteEmpoweredItem    (3), //  TAG,
    Tag_AzeriteItem             (4), //  TAG,
    Tag_Unit                    (5), //  TAG,
    Tag_Player                  (6), //  TAG,
    Tag_GameObject              (7), //  TAG,
    Tag_DynamicObject           (8), //  TAG,
    Tag_Corpse                  (9), //  TAG,
    Tag_AreaTrigger             (10), //  TAG,
    Tag_SceneObject             (11), //  TAG,
    Tag_Conversation            (12), //  TAG,
    Tag_AIGroup                 (13), //  TAG,
    Tag_Scenario                (14), //  TAG,
    Tag_LootObject              (15), //  TAG,
    Tag_ActivePlayer            (16), //  TAG,
    Tag_ActiveClient_S          (17), //  TAG,
    Tag_ActiveObject_C          (18), //  TAG,
    Tag_VisibleObject_C         (19), //  TAG,
    Tag_UnitVehicle             (20), //  TAG,
    FEntityPosition             (112),
    FEntityLocalMatrix          (113),
    FEntityWorldMatrix          (114),
    CActor                      (115), //  INDIRECT,
    FVendor_C                   (117), //  UPDATEABLE, INDIRECT,
    FMirroredObject_C           (119),
    End                         (255);

    public final int value;


    public boolean isUpdatableFragment() {
        return this == CGObject || this == FVendor_C;
    }

    public boolean isIndirectFragment() {
        return this == CGObject || this == CActor || this == FVendor_C;
    }


}
