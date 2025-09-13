package com.github.azeroth.game.entity.player;


import com.github.azeroth.game.entity.player.enums.ActionButtonType;
import com.github.azeroth.game.entity.player.enums.ActionButtonUpdateState;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ActionButton {
    public ActionButtonUpdateState uState;

    private long packedData;

    public ActionButton() {
        uState = ActionButtonUpdateState.NEW;
    }

    public final ActionButtonType getButtonType() {
        return ActionButtonType.valueOf(((int) getPackedData() & 0xFF000000) >>> 24);
    }

    public final long getAction() {
        return (getPackedData() & 0x00FFFFFF);
    }

    public final void setActionAndType(long action, ActionButtonType type) {
        var newData = action | ((long) type.value << 56);

        if (newData != getPackedData() || uState == ActionButtonUpdateState.DELETED) {
            setPackedData(newData);

            if (uState != ActionButtonUpdateState.NEW) {
                uState = ActionButtonUpdateState.CHANGED;
            }
        }
    }
}
