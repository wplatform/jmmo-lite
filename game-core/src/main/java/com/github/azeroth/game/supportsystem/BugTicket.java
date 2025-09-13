package com.github.azeroth.game.supportsystem;


import com.github.azeroth.game.chat.CommandHandler;
import com.github.azeroth.game.entity.player.Player;

public class BugTicket extends Ticket {
    private float facing;
    private String note;

    public BugTicket() {
        note = "";
    }

    public BugTicket(Player player) {
        super(player);
        note = "";
        idProtected = global.getSupportMgr().generateBugId();
    }

    @Override
    public void loadFromDB(SQLFields fields) {
        byte idx = 0;
        idProtected = fields.<Integer>Read(idx);
        playerGuidProtected = ObjectGuid.create(HighGuid.Player, fields.<Long>Read(++idx));
        note = fields.<String>Read(++idx);
        createTimeProtected = fields.<Long>Read(++idx);
        mapIdProtected = fields.<SHORT>Read(++idx);
        posProtected = new Vector3(fields.<Float>Read(++idx), fields.<Float>Read(++idx), fields.<Float>Read(++idx));
        facing = fields.<Float>Read(++idx);

        var closedBy = fields.<Long>Read(++idx);

        if (closedBy == 0) {
            closedByProtected = ObjectGuid.Empty;
        } else if (closedBy < 0) {
            closedByProtected.SetRawValue(0, (long) closedBy);
        } else {
            closedByProtected = ObjectGuid.create(HighGuid.Player, (long) closedBy);
        }

        var assignedTo = fields.<Long>Read(++idx);

        if (assignedTo == 0) {
            assignedToProtected = ObjectGuid.Empty;
        } else {
            assignedToProtected = ObjectGuid.create(HighGuid.Player, assignedTo);
        }

        commentProtected = fields.<String>Read(++idx);
    }

    @Override
    public void saveToDB() {
        byte idx = 0;
        var stmt = DB.characters.GetPreparedStatement(CharStatements.REP_GM_BUG);
        stmt.AddValue(idx, idProtected);
        stmt.AddValue(++idx, playerGuidProtected.getCounter());
        stmt.AddValue(++idx, note);
        stmt.AddValue(++idx, createTimeProtected);
        stmt.AddValue(++idx, mapIdProtected);
        stmt.AddValue(++idx, posProtected.X);
        stmt.AddValue(++idx, posProtected.Y);
        stmt.AddValue(++idx, posProtected.Z);
        stmt.AddValue(++idx, facing);
        stmt.AddValue(++idx, closedByProtected.getCounter());
        stmt.AddValue(++idx, assignedToProtected.getCounter());
        stmt.AddValue(++idx, commentProtected);

        DB.characters.execute(stmt);
    }

    @Override
    public void deleteFromDB() {
        var stmt = DB.characters.GetPreparedStatement(CharStatements.DEL_GM_BUG);
        stmt.AddValue(0, idProtected);
        DB.characters.execute(stmt);
    }


    @Override
    public String formatViewMessageString(CommandHandler handler) {
        return formatViewMessageString(handler, false);
    }

    @Override
    public String formatViewMessageString(CommandHandler handler, boolean detailed) {
        var curTime = (long) gameTime.GetGameTime();

        StringBuilder ss = new StringBuilder();
        ss.append(handler.getParsedString(SysMessage.CommandTicketlistguid, idProtected));
        ss.append(handler.getParsedString(SysMessage.CommandTicketlistname, getPlayerName()));
        ss.append(handler.getParsedString(SysMessage.CommandTicketlistagecreate, time.secsToTimeString(curTime - createTimeProtected, TimeFormat.ShortText, false)));

        if (!assignedToProtected.isEmpty()) {
            ss.append(handler.getParsedString(SysMessage.CommandTicketlistassignedto, getAssignedToName()));
        }

        if (detailed) {
            ss.append(handler.getParsedString(SysMessage.CommandTicketlistmessage, note));

            if (!StringUtil.isEmpty(commentProtected)) {
                ss.append(handler.getParsedString(SysMessage.CommandTicketlistcomment, commentProtected));
            }
        }

        return ss.toString();
    }

    public final void setFacing(float facing) {
        facing = facing;
    }

    private String getNote() {
        return note;
    }

    public final void setNote(String note) {
        note = note;
    }
}
