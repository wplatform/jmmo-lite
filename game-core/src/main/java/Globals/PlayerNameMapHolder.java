package Globals;


import com.github.azeroth.game.entity.player.Player;
import game.*;

import java.util.HashMap;

class PlayerNameMapHolder {
    private static final HashMap<String, Player> _playerNameMap = new HashMap<String, Player>();

    public static void Insert(Player p) {
        _playerNameMap.put(p.GetName(), p);
    }

    public static void Remove(Player p) {
        _playerNameMap.remove(p.GetName());
    }

    public static Player Find(String name) {
        tangible.RefObject<String> tempRef_name = new tangible.RefObject<String>(name);
        if (!ObjectManager.NormalizePlayerName(tempRef_name)) {
            name = tempRef_name.refArgValue;
            return null;
        } else {
            name = tempRef_name.refArgValue;
        }

        return _playerNameMap.get(name);
    }
}
