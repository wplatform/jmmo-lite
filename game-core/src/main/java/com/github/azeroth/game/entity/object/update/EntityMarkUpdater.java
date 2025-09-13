package com.github.azeroth.game.entity.object.update;


import com.github.azeroth.common.Assert;
import com.github.azeroth.common.Pair;

import com.github.azeroth.game.domain.object.enums.TypeId;
import com.github.azeroth.game.entity.object.GenericObject;
import lombok.Getter;
import lombok.Setter;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.BiFunction;

@Getter
public class EntityMarkUpdater implements PropertyChangeListener {

    private static final int MAX_IDS = 8;
    private static final int MAX_UPDATABLE_IDS = 2;
    public static final byte CG_OBJECT_CHANGED_MASK = 0x01; // Assuming this value based on common patterns

    private final EntityFragment[] ids = new EntityFragment[MAX_IDS];
    @Getter
    private byte count = 0;
    @Getter
    @Setter
    private boolean idsChanged = false;
    private final EntityFragment[] updatableIds = new EntityFragment[MAX_UPDATABLE_IDS];
    private final byte[] updatableMasks = new byte[MAX_UPDATABLE_IDS];
    @Getter
    private byte updatableCount = 0;
    @Getter
    private byte contentsChangedMask = 0;
    private int changesMask;
    private final GenericObject owner;

    public EntityMarkUpdater(GenericObject owner) {
        this.owner = owner;
        // Initialize all ids to EntityFragment.End
        Arrays.fill(ids, EntityFragment.End);
        Arrays.fill(updatableIds, EntityFragment.End);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        var source = evt.getSource();
        getEntityFragment(source).ifPresent(entityFragment -> {
            this.contentsChangedMask |= (byte) (this.contentsChangedMask | getUpdateMaskFor(entityFragment.first()));
            if (entityFragment.first() == EntityFragment.CGObject) {
                changesMask |= UpdateMask.getBlockFlag(entityFragment.second().ordinal());
            }
        });

    }


    public void clearChangesMask(UpdateMaskObject source) {
        getEntityFragment(source).ifPresent(entityFragment -> {
            contentsChangedMask &= (byte) ~getUpdateMaskFor(entityFragment.first());
            if (entityFragment.first() == EntityFragment.CGObject) {

                changesMask &= ~UpdateMask.getBlockFlag(entityFragment.second().ordinal());
                if (changesMask == 0)
                    contentsChangedMask &= (byte) ~getUpdateMaskFor(entityFragment.first());
            } else {
                contentsChangedMask &= (byte) ~getUpdateMaskFor(entityFragment.first());
                source.clearChangesMask();
            }
        });
    }

    private Optional<Pair<EntityFragment, TypeId>> getEntityFragment(Object source) {
        UpdateMaskObject object = (UpdateMaskObject)source;
        var fragment = switch (source) {
            case ObjectData ignored -> Pair.of(EntityFragment.CGObject, TypeId.OBJECT);
            case ItemData ignored -> Pair.of(EntityFragment.Tag_Item, TypeId.ITEM);
            case ContainerData ignored -> Pair.of(EntityFragment.Tag_Container, TypeId.CONTAINER);
            case UnitData ignored -> Pair.of(EntityFragment.Tag_Unit, TypeId.UNIT);
            case PlayerData ignored -> Pair.of(EntityFragment.Tag_Player, TypeId.PLAYER);
            case ActivePlayerData ignored -> Pair.of(EntityFragment.Tag_ActivePlayer, TypeId.ACTIVE_PLAYER);
            case GameObjectData ignored -> Pair.of(EntityFragment.Tag_GameObject, TypeId.GAME_OBJECT);
            case DynamicObjectData ignored -> Pair.of(EntityFragment.Tag_DynamicObject, TypeId.DYNAMIC_OBJECT);
            case CorpseData ignored -> Pair.of(EntityFragment.Tag_Corpse, TypeId.CORPSE);
            case AreaTriggerData ignored -> Pair.of(EntityFragment.Tag_AreaTrigger, TypeId.AREA_TRIGGER);
            case SceneObjectData ignored -> Pair.of(EntityFragment.Tag_SceneObject, TypeId.SCENE_OBJECT);
            case ConversationData ignored -> Pair.of(EntityFragment.Tag_Conversation, TypeId.CONVERSATION);
            case VendorData ignored -> Pair.of(EntityFragment.Tag_Vendor, TypeId.VENDOR);
        };
        EntityFragment blockBit = null;
        TypeId bit = null;
        if (source instanceof ObjectData) {
            blockBit = EntityFragment.CGObject;
            bit = TypeId.OBJECT;
        } else if (source instanceof ItemData) {
            blockBit = EntityFragment.Tag_Item;
            bit = TypeId.ITEM;
        } else if (source instanceof ContainerData) {
            blockBit = EntityFragment.Tag_Container;
            bit = TypeId.CONTAINER;
        } else if (source instanceof UnitData ) {
            blockBit = EntityFragment.Tag_Unit;
            bit = TypeId.UNIT;
        } else if (source instanceof PlayerData) {
            blockBit = EntityFragment.Tag_Player;
            bit = TypeId.PLAYER;
        } else if (source instanceof ActivePlayerData) {
            blockBit = EntityFragment.Tag_ActivePlayer;
            bit = TypeId.ACTIVE_PLAYER;
        } else if (source instanceof GameObjectData) {
            blockBit = EntityFragment.Tag_GameObject;
            bit = TypeId.GAME_OBJECT;
        } else if (source instanceof DynamicObjectData) {
            blockBit = EntityFragment.Tag_DynamicObject;
            bit = TypeId.DYNAMIC_OBJECT;
        } else if (source instanceof CorpseData) {
            blockBit = EntityFragment.Tag_Corpse;
            bit = TypeId.CORPSE;
        } else if (source instanceof AreaTriggerData) {
            blockBit = EntityFragment.Tag_AreaTrigger;
            bit = TypeId.AREA_TRIGGER;
        } else if (source instanceof SceneObjectData) {
            blockBit = EntityFragment.Tag_SceneObject;
            bit = TypeId.SCENE_OBJECT;
        } else if (source instanceof ConversationData) {
            blockBit = EntityFragment.Tag_Conversation;
            bit = TypeId.CONVERSATION;
        } else if (source instanceof VendorData) {
            blockBit = EntityFragment.Tag_Vendor;
        }
        return Optional.ofNullable(blockBit != null ? Pair.of(blockBit, bit) : null);
    }

    public boolean hasChanged(int index) {
        return (changesMask & UpdateMask.getBlockFlag(index)) != 0;
    }

    private void markChanged(int blockBit, int bit) {
        changesMask |= UpdateMask.getBlockFlag(blockBit, bit);
    }

    private void clearMaskBit(int blockBit, int bit) {
        changesMask &= ~UpdateMask.getBlockFlag(blockBit, bit);
    }


    public EntityFragment[] getIds() {
        return Arrays.copyOf(ids, count);
    }

    public EntityFragment[] getUpdatableIds() {
        return Arrays.copyOf(updatableIds, updatableCount);
    }

    public byte getUpdateMaskFor(EntityFragment fragment) {
        // Common case optimization for CGObject
        if (fragment == EntityFragment.CGObject) {
            return CG_OBJECT_CHANGED_MASK;
        }

        for (byte i = 1; i < updatableCount; i++) {
            if (updatableIds[i] == fragment) {
                return updatableMasks[i];
            }
        }

        return 0;
    }


    public void add(EntityFragment fragment, boolean update) {

        Assert.state(count < MAX_IDS, "Count exceeds maximum Ids size");


        record InsertResult(int insertIndex, byte newCount, boolean inserted) {
        }

        BiFunction<EntityFragment[], Byte, InsertResult> addFunc = (array, count) -> {
            int insertPos = Arrays.binarySearch(array, 0, count, fragment);
            if (insertPos >= 0) {
                return new InsertResult(insertPos, count, false);
            }

            insertPos = -(insertPos + 1); // Convert to insertion point
            // Shift elements to the right
            System.arraycopy(array, insertPos, array, insertPos + 1, count - insertPos);
            array[insertPos] = fragment;
            return new InsertResult(insertPos, (byte) (count + 1), true);
        };

        // Insert into main ids array
        InsertResult result = addFunc.apply(ids, count);
        if (!result.inserted) {
            return; // Already exists
        }
        count = result.newCount;

        if (fragment.isUpdatableFragment()) {

            Assert.state(updatableCount < MAX_UPDATABLE_IDS, "UpdatableCount exceeds maximum size");

            // Insert into updateable ids array
            InsertResult updateableResult = addFunc.apply(updatableIds, updatableCount);
            if (updateableResult.inserted) {
                updatableCount = updateableResult.newCount;
                int index = updateableResult.insertIndex;

                // Update contents changed mask
                byte maskLowPart = (byte) (contentsChangedMask & ((1 << index) - 1));
                byte maskHighPart = (byte) ((contentsChangedMask & -(1 << index)) << (1 + (fragment.isIndirectFragment() ? 1 : 0)));
                contentsChangedMask = (byte) (maskLowPart | maskHighPart);

                // Update updateable masks
                for (byte i = 0, maskIndex = 0; i < updatableCount; i++) {
                    updatableMasks[i] = (byte) (1 << maskIndex++);
                    if (updatableIds[i].isIndirectFragment()) {
                        contentsChangedMask |= updatableMasks[i];
                        maskIndex++;
                        updatableMasks[i] <<= 1;
                    }
                }
            }
        }

        if (update) {
            idsChanged = true;
        }
    }

    public void remove(EntityFragment fragment) {

        record RemoveResult(boolean removed, byte newCount, int removeIndex) {
        }
        ;

        BiFunction<EntityFragment[], Byte, RemoveResult> removeFunc = (array, count) -> {
            int index = Arrays.binarySearch(array, 0, count, fragment);
            if (index < 0) {
                return new RemoveResult(false, count, -1);
            }
            // Shift elements to the left
            System.arraycopy(array, index + 1, array, index, count - index - 1);
            // Fill the last position with End
            array[count - 1] = EntityFragment.End;
            return new RemoveResult(true, (byte) (count - 1), index);
        };

        // Remove from main ids array
        RemoveResult result = removeFunc.apply(ids, count);
        if (!result.removed) {
            return;
        }
        count = result.newCount;

        if (fragment.isUpdatableFragment()) {
            RemoveResult updatableResult = removeFunc.apply(updatableIds, updatableCount);
            if (updatableResult.removed) {
                updatableCount = updatableResult.newCount;
                int index = updatableResult.removeIndex;

                // Update contents changed mask
                byte maskLowPart = (byte) (contentsChangedMask & ((1 << index) - 1));
                byte maskHighPart = (byte) ((contentsChangedMask & -(1 << index)) >>> (1 + (fragment.isIndirectFragment() ? 1 : 0)));
                contentsChangedMask = (byte) (maskLowPart | maskHighPart);

                // Rebuild updateable masks
                for (byte i = 0, maskIndex = 0; i < updatableCount; i++) {
                    updatableMasks[i] = (byte) (1 << maskIndex++);
                    if (updatableIds[i].isIndirectFragment()) {
                        maskIndex++;
                        updatableMasks[i] <<= 1;
                    }
                }
            }
        }

        idsChanged = true;
    }
}
