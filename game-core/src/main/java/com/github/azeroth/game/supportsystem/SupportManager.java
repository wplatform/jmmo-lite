package com.github.azeroth.game.supportsystem;


import game.WorldConfig;

import java.util.HashMap;
import java.util.Map;


public class SupportManager {
    private final HashMap<Integer, BugTicket> bugTicketList = new HashMap<Integer, BugTicket>();
    private final HashMap<Integer, ComplaintTicket> complaintTicketList = new HashMap<Integer, ComplaintTicket>();
    private final HashMap<Integer, SuggestionTicket> suggestionTicketList = new HashMap<Integer, SuggestionTicket>();

    private boolean supportSystemStatus;
    private boolean ticketSystemStatus;
    private boolean bugSystemStatus;
    private boolean complaintSystemStatus;
    private boolean suggestionSystemStatus;
    private int lastBugId;
    private int lastComplaintId;
    private int lastSuggestionId;
    private int openBugTicketCount;
    private int openComplaintTicketCount;
    private int openSuggestionTicketCount;
    private long lastChange;

    private SupportManager() {
    }

    public final void initialize() {
        setSupportSystemStatus(WorldConfig.getBoolValue(WorldCfg.SupportEnabled));
        setTicketSystemStatus(WorldConfig.getBoolValue(WorldCfg.SupportTicketsEnabled));
        setBugSystemStatus(WorldConfig.getBoolValue(WorldCfg.SupportBugsEnabled));
        setComplaintSystemStatus(WorldConfig.getBoolValue(WorldCfg.SupportComplaintsEnabled));
        setSuggestionSystemStatus(WorldConfig.getBoolValue(WorldCfg.SupportSuggestionsEnabled));
    }

    public final <T extends ticket> T getTicket(int id) {
        switch (T.class.name) {
            case "BugTicket":
                return (T) bugTicketList.get(id);
            case "ComplaintTicket":
                return (T) complaintTicketList.get(id);
            case "SuggestionTicket":
                return (T) suggestionTicketList.get(id);
        }

        return null;
    }

    public final <T extends ticket> int getOpenTicketCount() {
        switch (T.class.name) {
            case "BugTicket":
                return openBugTicketCount;
            case "ComplaintTicket":
                return openComplaintTicketCount;
            case "SuggestionTicket":
                return openSuggestionTicketCount;
        }

        return 0;
    }

    public final void loadBugTickets() {
        var oldMSTime = System.currentTimeMillis();
        bugTicketList.clear();

        lastBugId = 0;
        openBugTicketCount = 0;

        var stmt = DB.characters.GetPreparedStatement(CharStatements.SEL_GM_BUGS);
        var result = DB.characters.query(stmt);

        if (result.isEmpty()) {
            Log.outInfo(LogFilter.ServerLoading, "Loaded 0 GM bugs. DB table `gm_bug` is empty!");

            return;
        }

        int count = 0;

        do {
            BugTicket bug = new BugTicket();
            bug.loadFromDB(result.GetFields());

            if (!bug.isClosed()) {
                ++openBugTicketCount;
            }

            var id = bug.getId();

            if (lastBugId < id) {
                lastBugId = id;
            }

            bugTicketList.put(id, bug);
            ++count;
        } while (result.NextRow());

        Log.outInfo(LogFilter.ServerLoading, "Loaded {0} GM bugs in {1} ms", count, time.GetMSTimeDiffToNow(oldMSTime));
    }

    public final void loadComplaintTickets() {
        var oldMSTime = System.currentTimeMillis();
        complaintTicketList.clear();

        lastComplaintId = 0;
        openComplaintTicketCount = 0;

        var stmt = DB.characters.GetPreparedStatement(CharStatements.SEL_GM_COMPLAINTS);
        var result = DB.characters.query(stmt);

        if (result.isEmpty()) {
            Log.outInfo(LogFilter.ServerLoading, "Loaded 0 GM complaints. DB table `gm_complaint` is empty!");

            return;
        }

        int count = 0;
        PreparedStatement chatLogStmt;
        SQLResult chatLogResult;

        do {
            ComplaintTicket complaint = new ComplaintTicket();
            complaint.loadFromDB(result.GetFields());

            if (!complaint.isClosed()) {
                ++openComplaintTicketCount;
            }

            var id = complaint.getId();

            if (lastComplaintId < id) {
                lastComplaintId = id;
            }

            chatLogStmt = DB.characters.GetPreparedStatement(CharStatements.SEL_GM_COMPLAINT_CHATLINES);
            chatLogStmt.AddValue(0, id);
            chatLogResult = DB.characters.query(stmt);

            if (!chatLogResult.isEmpty()) {
                do {
                    complaint.loadChatLineFromDB(chatLogResult.GetFields());
                } while (chatLogResult.NextRow());
            }

            complaintTicketList.put(id, complaint);
            ++count;
        } while (result.NextRow());

        Log.outInfo(LogFilter.ServerLoading, "Loaded {0} GM complaints in {1} ms", count, time.GetMSTimeDiffToNow(oldMSTime));
    }

    public final void loadSuggestionTickets() {
        var oldMSTime = System.currentTimeMillis();
        suggestionTicketList.clear();

        lastSuggestionId = 0;
        openSuggestionTicketCount = 0;

        var stmt = DB.characters.GetPreparedStatement(CharStatements.SEL_GM_SUGGESTIONS);
        var result = DB.characters.query(stmt);

        if (result.isEmpty()) {
            Log.outInfo(LogFilter.ServerLoading, "Loaded 0 GM suggestions. DB table `gm_suggestion` is empty!");

            return;
        }

        int count = 0;

        do {
            SuggestionTicket suggestion = new SuggestionTicket();
            suggestion.loadFromDB(result.GetFields());

            if (!suggestion.isClosed()) {
                ++openSuggestionTicketCount;
            }

            var id = suggestion.getId();

            if (lastSuggestionId < id) {
                lastSuggestionId = id;
            }

            suggestionTicketList.put(id, suggestion);
            ++count;
        } while (result.NextRow());

        Log.outInfo(LogFilter.ServerLoading, "Loaded {0} GM suggestions in {1} ms", count, time.GetMSTimeDiffToNow(oldMSTime));
    }

    public final <T extends ticket> void addTicket(T ticket) {
        switch (T.class.name) {
            case "BugTicket":
                bugTicketList.put(ticket.getId(), ticket instanceof BugTicket ? (BugTicket) ticket : null);

                if (!ticket.isClosed()) {
                    ++openBugTicketCount;
                }

                break;
            case "ComplaintTicket":
                complaintTicketList.put(ticket.getId(), ticket instanceof ComplaintTicket ? (ComplaintTicket) ticket : null);

                if (!ticket.isClosed()) {
                    ++openComplaintTicketCount;
                }

                break;
            case "SuggestionTicket":
                suggestionTicketList.put(ticket.getId(), ticket instanceof SuggestionTicket ? (SuggestionTicket) ticket : null);

                if (!ticket.isClosed()) {
                    ++openSuggestionTicketCount;
                }

                break;
        }

        ticket.saveToDB();
    }

    public final <T extends ticket> void removeTicket(int ticketId) {
        var ticket = this.<T>GetTicket(ticketId);

        if (ticket != null) {
            ticket.deleteFromDB();

            switch (T.class.name) {
                case "BugTicket":
                    bugTicketList.remove(ticketId);

                    break;
                case "ComplaintTicket":
                    complaintTicketList.remove(ticketId);

                    break;
                case "SuggestionTicket":
                    suggestionTicketList.remove(ticketId);

                    break;
            }
        }
    }

    public final <T extends ticket> void closeTicket(int ticketId, ObjectGuid closedBy) {
        var ticket = this.<T>GetTicket(ticketId);

        if (ticket != null) {
            ticket.setClosedBy(closedBy);

            if (!closedBy.isEmpty()) {
                switch (T.class.name) {
                    case "BugTicket":
                        --_openBugTicketCount;

                        break;
                    case "ComplaintTicket":
                        --_openComplaintTicketCount;

                        break;
                    case "SuggestionTicket":
                        --_openSuggestionTicketCount;

                        break;
                }
            }

            ticket.saveToDB();
        }
    }

    public final <T extends ticket> void resetTickets() {
        PreparedStatement stmt;

        switch (T.class.name) {
            case "BugTicket":
                bugTicketList.clear();

                lastBugId = 0;

                stmt = DB.characters.GetPreparedStatement(CharStatements.DEL_ALL_GM_BUGS);
                DB.characters.execute(stmt);

                break;
            case "ComplaintTicket":
                complaintTicketList.clear();

                lastComplaintId = 0;

                SQLTransaction trans = new SQLTransaction();
                trans.append(DB.characters.GetPreparedStatement(CharStatements.DEL_ALL_GM_COMPLAINTS));
                trans.append(DB.characters.GetPreparedStatement(CharStatements.DEL_ALL_GM_COMPLAINT_CHATLOGS));
                DB.characters.CommitTransaction(trans);

                break;
            case "SuggestionTicket":
                suggestionTicketList.clear();

                lastSuggestionId = 0;

                stmt = DB.characters.GetPreparedStatement(CharStatements.DEL_ALL_GM_SUGGESTIONS);
                DB.characters.execute(stmt);

                break;
        }
    }

    public final <T extends ticket> void showList(CommandHandler handler) {
        handler.sendSysMessage(SysMessage.CommandTicketshowlist);

        switch (T.class.name) {
            case "BugTicket":
                for (var ticket : bugTicketList.values()) {
                    if (!ticket.IsClosed) {
                        handler.sendSysMessage(ticket.formatViewMessageString(handler));
                    }
                }

                break;
            case "ComplaintTicket":
                for (var ticket : complaintTicketList.values()) {
                    if (!ticket.IsClosed) {
                        handler.sendSysMessage(ticket.formatViewMessageString(handler));
                    }
                }

                break;
            case "SuggestionTicket":
                for (var ticket : suggestionTicketList.values()) {
                    if (!ticket.IsClosed) {
                        handler.sendSysMessage(ticket.formatViewMessageString(handler));
                    }
                }

                break;
        }
    }

    public final <T extends ticket> void showClosedList(CommandHandler handler) {
        handler.sendSysMessage(SysMessage.CommandTicketshowclosedlist);

        switch (T.class.name) {
            case "BugTicket":
                for (var ticket : bugTicketList.values()) {
                    if (ticket.IsClosed) {
                        handler.sendSysMessage(ticket.formatViewMessageString(handler));
                    }
                }

                break;
            case "ComplaintTicket":
                for (var ticket : complaintTicketList.values()) {
                    if (ticket.IsClosed) {
                        handler.sendSysMessage(ticket.formatViewMessageString(handler));
                    }
                }

                break;
            case "SuggestionTicket":
                for (var ticket : suggestionTicketList.values()) {
                    if (ticket.IsClosed) {
                        handler.sendSysMessage(ticket.formatViewMessageString(handler));
                    }
                }

                break;
        }
    }

    public final boolean getSupportSystemStatus() {
        return supportSystemStatus;
    }

    public final void setSupportSystemStatus(boolean status) {
        supportSystemStatus = status;
    }

    public final boolean getTicketSystemStatus() {
        return supportSystemStatus && ticketSystemStatus;
    }

    public final void setTicketSystemStatus(boolean status) {
        ticketSystemStatus = status;
    }

    public final boolean getBugSystemStatus() {
        return supportSystemStatus && bugSystemStatus;
    }

    public final void setBugSystemStatus(boolean status) {
        bugSystemStatus = status;
    }

    public final boolean getComplaintSystemStatus() {
        return supportSystemStatus && complaintSystemStatus;
    }

    public final void setComplaintSystemStatus(boolean status) {
        complaintSystemStatus = status;
    }

    public final boolean getSuggestionSystemStatus() {
        return supportSystemStatus && suggestionSystemStatus;
    }

    public final void setSuggestionSystemStatus(boolean status) {
        suggestionSystemStatus = status;
    }

    public final long getLastChange() {
        return lastChange;
    }

    public final void updateLastChange() {
        lastChange = (long) gameTime.GetGameTime();
    }

    public final int generateBugId() {
        return ++lastBugId;
    }

    public final int generateComplaintId() {
        return ++lastComplaintId;
    }

    public final int generateSuggestionId() {
        return ++lastSuggestionId;
    }

    private long getAge(long t) {
        return (gameTime.GetGameTime() - (long) t) / time.Day;
    }

    private Iterable<Map.entry<Integer, ComplaintTicket>> getComplaintsByPlayerGuid(ObjectGuid playerGuid) {
        return complaintTicketList.where(ticket -> Objects.equals(ticket.value.playerGuid, playerGuid));
    }
}
