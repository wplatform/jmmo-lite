package com.github.azeroth.game.battleground.zones;


final class BattlegroundEYPointIconsStruct {
    public int worldStateControlIndex;
    public int worldStateAllianceControlledIndex;
    public int worldStateHordeControlledIndex;
    public int worldStateAllianceStatusBarIcon;
    public int worldStateHordeStatusBarIcon;
    public BattlegroundEYPointIconsStruct() {
    }
    public BattlegroundEYPointIconsStruct(int worldStateControlIndex, int worldStateAllianceControlledIndex, int worldStateHordeControlledIndex, int worldStateAllianceStatusBarIcon, int worldStateHordeStatusBarIcon) {
        worldStateControlIndex = worldStateControlIndex;
        worldStateAllianceControlledIndex = worldStateAllianceControlledIndex;
        worldStateHordeControlledIndex = worldStateHordeControlledIndex;
        worldStateAllianceStatusBarIcon = worldStateAllianceStatusBarIcon;
        worldStateHordeStatusBarIcon = worldStateHordeStatusBarIcon;
    }

    public BattlegroundEYPointIconsStruct clone() {
        BattlegroundEYPointIconsStruct varCopy = new BattlegroundEYPointIconsStruct();

        varCopy.worldStateControlIndex = this.worldStateControlIndex;
        varCopy.worldStateAllianceControlledIndex = this.worldStateAllianceControlledIndex;
        varCopy.worldStateHordeControlledIndex = this.worldStateHordeControlledIndex;
        varCopy.worldStateAllianceStatusBarIcon = this.worldStateAllianceStatusBarIcon;
        varCopy.worldStateHordeStatusBarIcon = this.worldStateHordeStatusBarIcon;

        return varCopy;
    }
}
