package com.github.azeroth.game.entity.object.update;

import com.github.azeroth.common.Assert;
import com.github.azeroth.common.Functions;
import com.github.azeroth.game.networking.WorldPacket;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

abstract class UpdateFields {

    final static HashMap<Class<?>, Map<String, ChangeMark>> FIELD_CHANGE_MARKS_BY_CLASS;

    static {
        Class<?>[] permitted = UpdateMaskObject.class.getPermittedSubclasses();
        FIELD_CHANGE_MARKS_BY_CLASS = new HashMap<>(permitted.length, 1f);
        Arrays.stream(permitted).filter(e -> Modifier.isFinal(e.getModifiers())).forEach(clazz -> {
            Field[] fields = clazz.getDeclaredFields();
            for (Field field : fields) {
                ChangeMark changeMark = field.getAnnotation(ChangeMark.class);
                if (changeMark != null) {
                    FIELD_CHANGE_MARKS_BY_CLASS.compute(clazz, Functions.addToMap(field.getName(), changeMark, () -> new HashMap<>(fields.length, 1f)));
                }
            }
        });
    }


    public static <T> List<T> newList(String propertyName, UpdateMaskObject owner) {
        ChangeMark changeMark = FIELD_CHANGE_MARKS_BY_CLASS.get(owner.getClass()).get(propertyName);
        Assert.isTrue(changeMark != null, "Field {} does not have a ChangeMark annotation", propertyName);
        return switch (changeMark.type()) {
            case ARRAY -> new ArrayField<>(changeMark.size(), propertyName, owner);
            case DYNAMIC -> new DynamicField<>(propertyName, owner);
            case OBJECT -> new ListField<>(propertyName, owner);
            case OPTIONAL -> null;
        };
    }

    private static final class ArrayField<T> extends AbstractList<T> {
        private final Object[] array;
        private final String propertyName;
        private final UpdateMaskObject owner;

        public ArrayField(int size, String propertyName, UpdateMaskObject owner) {
            this.array = new Object[size];
            this.propertyName = propertyName;
            this.owner = owner;
        }

        @Override
        public T set(int index, T element) {
            Object oldValue = array[index];
            if (!Objects.equals(oldValue, element)) {
                array[index] = element;
                owner.fireIndexedPropertyChange(propertyName, index, oldValue, element);
                return element;
            }
            return (T) oldValue;
        }

        @Override
        public void add(int index, T element) {
            throw new UnsupportedOperationException("Fixed-size array value does not support add(int, T)");
        }

        @Override
        public T remove(int index) {
            var element = (T) array[index];
            array[index] = null;
            return element;
        }

        @Override
        public T get(int index) {
            return (T) array[index];
        }

        @Override
        public int size() {
            return array.length;
        }
    }


    private static final class ListField<T> extends AbstractList<T> {

        private final String propertyName;
        private final ArrayList<T> values = new ArrayList<>();
        private final UpdateMaskObject owner;

        public ListField(String propertyName, UpdateMaskObject owner) {
            this.propertyName = propertyName;
            this.owner = owner;
        }

        @Override
        public T set(int index, T element) {
            T v = values.set(index, element);
            owner.firePropertyChange(propertyName, this, this);
            return v;
        }

        @Override
        public void add(int index, T element) {
            values.add(index, element);
            owner.firePropertyChange(propertyName, this, this);
        }

        @Override
        public T remove(int index) {
            T v = values.remove(index);
            owner.firePropertyChange(propertyName, this, this);
            return v;
        }

        @Override
        public T get(int index) {
            return values.get(index);
        }

        @Override
        public int size() {
            return values.size();
        }
    }


    private static final class DynamicField<T> extends AbstractList<T> {

        private final String propertyName;
        private final ArrayList<T> values = new ArrayList<>();
        private final ArrayList<Integer> updateMask = new ArrayList<>();
        private final UpdateMaskObject owner;

        public DynamicField(String propertyName, UpdateMaskObject owner) {
            this.propertyName = propertyName;
            this.owner = owner;
        }

        @Override
        public T set(int index, T element) {
            T v = values.set(index, element);
            // mark all fields of old value as changed
            for (var i = index; i < values.size(); ++i) {
                markChanged(i);
                // also mark all fields of value as changed
                markAllUpdateMaskFields(values.get(i));
            }
            return v;
        }

        @Override
        public void add(int index, T element) {
            values.add(index, element);
            markChanged(index);
            markAllUpdateMaskFields(element);
        }

        @Override
        public T remove(int index) {

            // remove by shifting entire container - client might rely on values being sorted for certain fields
            values.remove(index);

            for (var i = index; i < values.size(); ++i) {
                markChanged(i);
                // also mark all fields of value as changed
                markAllUpdateMaskFields(values.get(i));
            }

            if ((values.size() % 32) != 0) {
                updateMask.set(owner.changesMask.getBlockIndex(values.size()), updateMask.get(owner.changesMask.getBlockIndex(values.size())) & (int) ~owner.changesMask.getBlockFlag(values.size()));
            } else {
                updateMask.remove(updateMask.size() - 1);
            }

            return values.remove(index);
        }

        @Override
        public T get(int index) {
            return values.get(index);
        }

        @Override
        public int size() {
            return values.size();
        }


        public final boolean hasChanged(int index) {
            return (updateMask.get(index / 32) & (1 << (index % 32))) != 0;
        }


        public final void writeUpdateMask(WorldPacket data) {
            writeUpdateMask(data, 32);
        }

        public final void writeUpdateMask(WorldPacket data, int bitsForSize) {
            data.writeBits(values.size(), bitsForSize);

            if (values.size() > 32) {
                if (data.hasUnfinishedBitPack()) {
                    for (var block = 0; block < values.size() / 32; ++block) {
                        data.writeBits(updateMask.get(block), 32);
                    }
                } else {
                    for (var block = 0; block < values.size() / 32; ++block) {
                        data.writeInt32(updateMask.get(block));
                    }
                }
            } else if (values.size() == 32) {
                data.writeBits(updateMask.get(updateMask.size() - 1), 32);

                return;
            }

            if ((values.size() % 32) != 0) {
                data.writeBits(updateMask.get(updateMask.size() - 1), values.size() % 32);
            }
        }

        public final void clearChangesMask() {
            for (var i = 0; i < updateMask.size(); ++i) {
                updateMask.set(i, 0);
            }
        }


        public final void clear() {
            values.clear();
            updateMask.clear();
        }

        private void markChanged(int index) {
            var block = owner.changesMask.getBlockIndex(index);

            if (block >= updateMask.size()) {
                updateMask.add(0);
            }

            updateMask.set(block, updateMask.get(block) | UpdateMask.getBlockFlag(index));
        }

        private void clearChanged(int index) {
            var block = UpdateMask.getBlockIndex(index);

            if (block >= updateMask.size()) {
                updateMask.add(0);
            }
            updateMask.set(block, updateMask.get(block) & ~(int) UpdateMask.getBlockFlag(index));
        }


        private void markAllUpdateMaskFields(T value) {
            if (value instanceof UpdateMaskObject maskObject) {
                maskObject.changesMask.setAll();
            }
        }
    }


}
