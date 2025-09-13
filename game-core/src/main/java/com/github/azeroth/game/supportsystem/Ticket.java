package com.github.azeroth.game.supportsystem;


import com.github.azeroth.game.entity.ObjectGuid;
import com.github.azeroth.game.entity.player.Player;

public class Ticket {

    protected int idProtected;
    protected ObjectGuid playerGuidProtected = ObjectGuid.EMPTY;

    protected int mapIdProtected;
    protected Vector3 posProtected;

    protected long createTimeProtected;
    protected ObjectGuid closedByProtected = ObjectGuid.EMPTY; // 0 = Open, -1 = Console, playerGuid = player abandoned ticket, other = GM who closed it.
    protected ObjectGuid assignedToProtected = ObjectGuid.EMPTY;
    protected String commentProtected;

    public ticket() {
    }

    public ticket(Player player) {
        createTimeProtected = (long) gameTime.GetGameTime();
        playerGuidProtected = player.getGUID();
    }

    public final boolean isClosed() {
        return !closedByProtected.isEmpty();
    }

    public final boolean isAssigned() {
        return !assignedToProtected.isEmpty();
    }


    public final int getId() {
        return idProtected;
    }

    public final ObjectGuid getPlayerGuid() {
        return playerGuidProtected;
    }

    public final Player getPlayer() {
        return global.getObjAccessor().findConnectedPlayer(playerGuidProtected);
    }

    public final String getPlayerName() {
        var name = "";

        if (!playerGuidProtected.isEmpty()) {
            tangible.OutObject<String> tempOut_name = new tangible.OutObject<String>();
            global.getCharacterCacheStorage().getCharacterNameByGuid(playerGuidProtected, tempOut_name);
            name = tempOut_name.outArgValue;
        }

        return name;
    }

    public final Player getAssignedPlayer() {
        return global.getObjAccessor().findConnectedPlayer(assignedToProtected);
    }

    public final ObjectGuid getAssignedToGUID() {
        return assignedToProtected;
    }

    public final String getAssignedToName() {
        if (!assignedToProtected.isEmpty()) {
            String name;
            tangible.OutObject<String> tempOut_name = new tangible.OutObject<String>();
            if (global.getCharacterCacheStorage().getCharacterNameByGuid(assignedToProtected, tempOut_name)) {
                name = tempOut_name.outArgValue;
                return name;
            } else {
                name = tempOut_name.outArgValue;
            }
        }

        return "";
    }

    public final String getComment() {
        return commentProtected;
    }

    public final void setComment(String comment) {
        commentProtected = comment;
    }

    public final void teleportTo(Player player) {
        player.teleportTo(mapIdProtected, posProtected.X, posProtected.Y, posProtected.Z, 0.0f, 0);
    }

    public String formatViewMessageString(CommandHandler handler) {
        return formatViewMessageString(handler, false);
    }

    public String formatViewMessageString(CommandHandler handler, boolean detailed) {
        return "";
    }

    public String formatViewMessageString(CommandHandler handler, String closedName, String assignedToName, String unassignedName, String deletedName) {
        StringBuilder ss = new StringBuilder();
        ss.append(handler.getParsedString(SysMessage.CommandTicketlistguid, idProtected));
        ss.append(handler.getParsedString(SysMessage.CommandTicketlistname, getPlayerName()));

        if (!StringUtil.isEmpty(closedName)) {
            ss.append(handler.getParsedString(SysMessage.CommandTicketclosed, closedName));
        }

        if (!StringUtil.isEmpty(assignedToName)) {
            ss.append(handler.getParsedString(SysMessage.CommandTicketlistassignedto, assignedToName));
        }

        if (!StringUtil.isEmpty(unassignedName)) {
            ss.append(handler.getParsedString(SysMessage.CommandTicketlistunassigned, unassignedName));
        }

        if (!StringUtil.isEmpty(deletedName)) {
            ss.append(handler.getParsedString(SysMessage.CommandTicketdeleted, deletedName));
        }

        return ss.toString();
    }

    public final boolean isAssignedTo(ObjectGuid guid) {
        return Objects.equals(guid, assignedToProtected);
    }

    public final boolean isAssignedNotTo(ObjectGuid guid) {
        return isAssigned() && !isAssignedTo(guid);
    }

    public void setAssignedTo(ObjectGuid guid) {
        setAssignedTo(guid, false);
    }

    public void setAssignedTo(ObjectGuid guid, boolean isAdmin) {
        assignedToProtected = guid;
    }

    public void setUnassigned() {
        assignedToProtected.clear();
    }

    public final void setClosedBy(ObjectGuid value) {
        closedByProtected = value;
    }

    public final void setPosition(int mapId, Vector3 pos) {
        mapIdProtected = mapId;
        posProtected = pos;
    }

    public void loadFromDB(SQLFields fields) {
    }

    public void saveToDB() {
    }

    public void deleteFromDB() {
    }

    private boolean isFromPlayer(ObjectGuid guid) {
        return Objects.equals(guid, playerGuidProtected);
    }
}
