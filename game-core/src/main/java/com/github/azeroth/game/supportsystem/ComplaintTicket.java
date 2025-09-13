package com.github.azeroth.game.supportsystem;


import com.github.azeroth.game.entity.player.Player;
import com.github.azeroth.game.networking.packet.SupportTicketSubmitComplaint;

public class ComplaintTicket extends Ticket {
    private float facing;
    private ObjectGuid targetCharacterGuid = ObjectGuid.EMPTY;
    private ReportMajorCategory majorCategory = ReportMajorCategory.values()[0];    private ReportType reportType = reportType.values()[0];
    private ReportMinorCategory minorCategoryFlags = ReportMinorCategory.TextChat;
    private SupportTicketSubmitComplaint.SupportTicketChatLog chatLog;
    private String note;
    public ComplaintTicket() {
        note = "";
    }

    public ComplaintTicket(Player player) {
        super(player);
        note = "";
        idProtected = global.getSupportMgr().generateComplaintId();
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
        targetCharacterGuid = ObjectGuid.create(HighGuid.Player, fields.<Long>Read(++idx));
        reportType = reportType.forValue(fields.<Integer>Read(++idx));
        majorCategory = ReportMajorCategory.forValue(fields.<Integer>Read(++idx));
        minorCategoryFlags = ReportMinorCategory.forValue(fields.<Integer>Read(++idx));
        var reportLineIndex = fields.<Integer>Read(++idx);

        if (reportLineIndex != -1) {
            chatLog.reportLineIndex = (int) reportLineIndex;
        }

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

    public final void loadChatLineFromDB(SQLFields fields) {
        chatLog.lines.add(new SupportTicketSubmitComplaint.SupportTicketChatLine(fields.<Long>Read(0), fields.<String>Read(1)));
    }

    @Override
    public void saveToDB() {
        var trans = new SQLTransaction();

        byte idx = 0;
        var stmt = DB.characters.GetPreparedStatement(CharStatements.REP_GM_COMPLAINT);
        stmt.AddValue(idx, idProtected);
        stmt.AddValue(++idx, playerGuidProtected.getCounter());
        stmt.AddValue(++idx, note);
        stmt.AddValue(++idx, createTimeProtected);
        stmt.AddValue(++idx, mapIdProtected);
        stmt.AddValue(++idx, posProtected.X);
        stmt.AddValue(++idx, posProtected.Y);
        stmt.AddValue(++idx, posProtected.Z);
        stmt.AddValue(++idx, facing);
        stmt.AddValue(++idx, targetCharacterGuid.getCounter());
        stmt.AddValue(++idx, reportType.getValue());
        stmt.AddValue(++idx, majorCategory.getValue());
        stmt.AddValue(++idx, minorCategoryFlags.getValue());

        if (chatLog.reportLineIndex != null) {
            stmt.AddValue(++idx, chatLog.reportLineIndex.intValue());
        } else {
            stmt.AddValue(++idx, -1); // empty ReportLineIndex
        }

        stmt.AddValue(++idx, closedByProtected.getCounter());
        stmt.AddValue(++idx, assignedToProtected.getCounter());
        stmt.AddValue(++idx, commentProtected);
        trans.append(stmt);

        int lineIndex = 0;

        for (var c : chatLog.lines) {
            idx = 0;
            stmt = DB.characters.GetPreparedStatement(CharStatements.INS_GM_COMPLAINT_CHATLINE);
            stmt.AddValue(idx, idProtected);
            stmt.AddValue(++idx, lineIndex);
            stmt.AddValue(++idx, c.timestamp);
            stmt.AddValue(++idx, c.text);

            trans.append(stmt);
            ++lineIndex;
        }

        DB.characters.CommitTransaction(trans);
    }

    @Override
    public void deleteFromDB() {
        var stmt = DB.characters.GetPreparedStatement(CharStatements.DEL_GM_COMPLAINT);
        stmt.AddValue(0, idProtected);
        DB.characters.execute(stmt);

        stmt = DB.characters.GetPreparedStatement(CharStatements.DEL_GM_COMPLAINT_CHATLOG);
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

    public final void setChatLog(SupportTicketSubmitComplaint.SupportTicketChatLog log) {
        chatLog = log;
    }

    private ObjectGuid getTargetCharacterGuid() {
        return targetCharacterGuid;
    }

    public final void setTargetCharacterGuid(ObjectGuid targetCharacterGuid) {
        targetCharacterGuid = targetCharacterGuid;
    }

    private ReportType getReportType() {
        return reportType;
    }

    public final void setReportType(ReportType reportType) {
        reportType = reportType;
    }

    private ReportMajorCategory getMajorCategory() {
        return majorCategory;
    }

    public final void setMajorCategory(ReportMajorCategory majorCategory) {
        majorCategory = majorCategory;
    }

    private ReportMinorCategory getMinorCategoryFlags() {
        return minorCategoryFlags;
    }

    public final void setMinorCategoryFlags(ReportMinorCategory minorCategoryFlags) {
        minorCategoryFlags = minorCategoryFlags;
    }

    private String getNote() {
        return note;
    }

    public final void setNote(String note) {
        note = note;
    }


}
