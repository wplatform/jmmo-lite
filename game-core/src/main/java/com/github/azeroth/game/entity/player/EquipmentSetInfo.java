package com.github.azeroth.game.entity.player;

public class EquipmentSetInfo {
    private EquipmentSetUpdatestate state = EquipmentSetUpdateState.values()[0];
    private EquipmentSetdata data;

    public EquipmentSetInfo() {
        setState(EquipmentSetUpdateState.New);
        setData(new EquipmentSetData());
    }

    public final EquipmentSetUpdateState getState() {
        return state;
    }

    public final void setState(EquipmentSetUpdateState value) {
        state = value;
    }

    public final EquipmentSetData getData() {
        return data;
    }

    public final void setData(EquipmentSetData value) {
        data = value;
    }

    public enum EquipmentSetType {
        Equipment(0),
        transmog(1);

        public static final int SIZE = Integer.SIZE;
        private static java.util.HashMap<Integer, EquipmentSetType> mappings;
        private final int intValue;

        EquipmentSetType(int value) {
            intValue = value;
            getMappings().put(value, this);
        }

        private static java.util.HashMap<Integer, EquipmentSetType> getMappings() {
            if (mappings == null) {
                synchronized (EquipmentSetType.class) {
                    if (mappings == null) {
                        mappings = new java.util.HashMap<Integer, EquipmentSetType>();
                    }
                }
            }
            return mappings;
        }

        public static EquipmentSetType forValue(int value) {
            return getMappings().get(value);
        }

        public int getValue() {
            return intValue;
        }
    }

    // Data sent in EquipmentSet related packets
    public static class EquipmentSetData {
        private EquipmentSettype type = EquipmentSetType.values()[0];

        private long guid;

        private int setId;

        private int ignoreMask;
        private int assignedSpecIndex = -1;
        private String setName = "";
        private String setIcon = "";
        private ObjectGuid[] pieces = new ObjectGuid[EquipmentSlot.End];
        private int[] appearances = new int[EquipmentSlot.End];
        private int[] enchants = new int[2];
        private int secondaryShoulderApparanceId;
        private int secondaryShoulderSlot;
        private int secondaryWeaponAppearanceId;
        private int secondaryWeaponSlot;

        public final EquipmentSetType getType() {
            return type;
        }

        public final void setType(EquipmentSetType value) {
            type = value;
        }


        public final long getGuid() {
            return guid;
        }


        public final void setGuid(long value) {
            guid = value;
        }


        public final int getSetId() {
            return setId;
        }


        public final void setId(int value) {
            setId = value;
        }


        public final int getIgnoreMask() {
            return ignoreMask;
        }


        public final void setIgnoreMask(int value) {
            ignoreMask = value;
        }

        public final int getAssignedSpecIndex() {
            return assignedSpecIndex;
        }

        public final void setAssignedSpecIndex(int value) {
            assignedSpecIndex = value;
        }

        public final String getSetName() {
            return setName;
        }

        public final void setName(String value) {
            setName = value;
        }

        public final String getSetIcon() {
            return setIcon;
        }

        public final void setIcon(String value) {
            setIcon = value;
        }

        public final ObjectGuid[] getPieces() {
            return pieces;
        }

        public final void setPieces(ObjectGuid[] value) {
            pieces = value;
        }

        public final int[] getAppearances() {
            return appearances;
        }

        public final void setAppearances(int[] value) {
            appearances = value;
        }

        public final int[] getEnchants() {
            return enchants;
        }

        public final void setEnchants(int[] value) {
            enchants = value;
        }

        public final int getSecondaryShoulderApparanceId() {
            return secondaryShoulderApparanceId;
        }

        public final void setSecondaryShoulderApparanceId(int value) {
            secondaryShoulderApparanceId = value;
        }

        public final int getSecondaryShoulderSlot() {
            return secondaryShoulderSlot;
        }

        public final void setSecondaryShoulderSlot(int value) {
            secondaryShoulderSlot = value;
        }

        public final int getSecondaryWeaponAppearanceId() {
            return secondaryWeaponAppearanceId;
        }

        public final void setSecondaryWeaponAppearanceId(int value) {
            secondaryWeaponAppearanceId = value;
        }

        public final int getSecondaryWeaponSlot() {
            return secondaryWeaponSlot;
        }

        public final void setSecondaryWeaponSlot(int value) {
            secondaryWeaponSlot = value;
        }
    }
}
