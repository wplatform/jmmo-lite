package com.github.azeroth.game.map;

public final class ScriptAction {
    public ObjectGuid ownerGUID = ObjectGuid.EMPTY;

    // owner of source if source is item
    public scriptInfo script = new scriptInfo();

    public ObjectGuid sourceGUID = ObjectGuid.EMPTY;
    public ObjectGuid targetGUID = ObjectGuid.EMPTY;

    public ScriptAction clone() {
        ScriptAction varCopy = new ScriptAction();

        varCopy.ownerGUID = this.ownerGUID;
        varCopy.script = this.script;
        varCopy.sourceGUID = this.sourceGUID;
        varCopy.targetGUID = this.targetGUID;

        return varCopy;
    }
}
