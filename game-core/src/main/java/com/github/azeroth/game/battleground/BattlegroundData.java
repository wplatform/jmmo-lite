package com.github.azeroth.game.battleground;


import Framework.Threading.*;

import java.util.ArrayList;
import java.util.HashMap;

public class BattlegroundData {
    public HashMap<Integer, Battleground> m_Battlegrounds = new HashMap<Integer, Battleground>();
    public ArrayList<Integer>[] m_ClientBattlegroundIds = new ArrayList<Integer>[BattlegroundBracketId.max.getValue()];
    public Battleground template;

    public BattlegroundData() {
        for (var i = 0; i < BattlegroundBracketId.max.getValue(); ++i) {
            m_ClientBattlegroundIds[i] = new ArrayList<>();
        }
    }
}
