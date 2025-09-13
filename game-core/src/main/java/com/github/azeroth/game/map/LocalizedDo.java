package com.github.azeroth.game.map;


import com.github.azeroth.game.chat.MessageBuilder;
import com.github.azeroth.game.entity.player.Player;

public class LocalizedDo implements IDoWork<Player> {
    private final MessageBuilder localizer;
    private IDoWork<Player>[] localizedCache = new IDoWork<Player>[Locale.Total.getValue()]; // 0 = default, i => i-1 locale index

    public LocalizedDo(MessageBuilder localizer) {
        localizer = localizer;
    }

    public final void invoke(Player player) {
        var loc_idx = player.getSession().getSessionDbLocaleIndex();
        var cache_idx = loc_idx.getValue() + 1;
        IDoWork<Player> action;

        // create if not cached yet
        if (localizedCache.length < cache_idx + 1 || _localizedCache[cache_idx] == null) {
            if (localizedCache.length < cache_idx + 1) {
                tangible.RefObject<T[]> tempRef__localizedCache = new tangible.RefObject<T[]>(localizedCache);
                Array.Resize(tempRef__localizedCache, cache_idx + 1);
                localizedCache = tempRef__localizedCache.refArgValue;
            }

            action = localizer.invoke(loc_idx);
            _localizedCache[cache_idx] = action;
        } else {
            action = _localizedCache[cache_idx];
        }

        action.invoke(player);
    }
}
