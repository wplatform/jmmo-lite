package com.github.azeroth.world.handler;

public class TicketHandler {

    void HandleGMTicketGetCaseStatusOpcode(WorldPackets::Ticket::GMTicketGetCaseStatus& /*packet*/)
    {
        // TODO: Implement GmCase and handle this packet properly
        WorldPackets::Ticket::GMTicketCaseStatus status;
        SendPacket(status.Write());
    }

    void HandleGMTicketSystemStatusOpcode(WorldPackets::Ticket::GMTicketGetSystemStatus& /*packet*/)
    {
        // Note: This only disables the ticket UI at client side and is not fully reliable
        // Note: This disables the whole customer support UI after trying to send a ticket in disabled state (MessageBox: "GM Help Tickets are currently unavaiable."). UI remains disabled until the character relogs.
        WorldPackets::Ticket::GMTicketSystemStatus response;
        response.Status = sSupportMgr->GetSupportSystemStatus() ? GMTICKET_QUEUE_STATUS_ENABLED : GMTICKET_QUEUE_STATUS_DISABLED;
        SendPacket(response.Write());
    }

    void HandleSubmitUserFeedback(WorldPackets::Ticket::SubmitUserFeedback& userFeedback)
    {
        if (userFeedback.IsSuggestion)
        {
            if (!sSupportMgr->GetSuggestionSystemStatus())
                return;

            SuggestionTicket* ticket = new SuggestionTicket(GetPlayer());
            ticket->SetPosition(userFeedback.Header.MapID, userFeedback.Header.Position);
            ticket->SetFacing(userFeedback.Header.Facing);
            ticket->SetNote(userFeedback.Note);

            sSupportMgr->AddTicket(ticket);
        }
        else
        {
            if (!sSupportMgr->GetBugSystemStatus())
                return;

            BugTicket* ticket = new BugTicket(GetPlayer());
            ticket->SetPosition(userFeedback.Header.MapID, userFeedback.Header.Position);
            ticket->SetFacing(userFeedback.Header.Facing);
            ticket->SetNote(userFeedback.Note);

            sSupportMgr->AddTicket(ticket);
        }
    }

    void HandleSupportTicketSubmitComplaint(WorldPackets::Ticket::SupportTicketSubmitComplaint& packet)
    {
        if (!sSupportMgr->GetComplaintSystemStatus())
            return;

        ComplaintTicket* comp = new ComplaintTicket(GetPlayer());
        comp->SetPosition(packet.Header.MapID, packet.Header.Position);
        comp->SetFacing(packet.Header.Facing);
        comp->SetChatLog(packet.ChatLog);
        comp->SetTargetCharacterGuid(packet.TargetCharacterGUID);
        comp->SetReportType(ReportType(packet.ReportType));
        comp->SetMajorCategory(ReportMajorCategory(packet.MajorCategory));
        comp->SetMinorCategoryFlags(ReportMinorCategory(packet.MinorCategoryFlags));
        comp->SetNote(packet.Note);

        sSupportMgr->AddTicket(comp);
    }

    void HandleBugReportOpcode(WorldPackets::Ticket::BugReport& bugReport)
    {
        // Note: There is no way to trigger this with standard UI except /script ReportBug("text")
        if (!sSupportMgr->GetBugSystemStatus())
            return;

        CharacterDatabasePreparedStatement* stmt = CharacterDatabase.GetPreparedStatement(CHAR_INS_BUG_REPORT);
        stmt->setString(0, bugReport.Text);
        stmt->setString(1, bugReport.DiagInfo);
        CharacterDatabase.Execute(stmt);
    }

    void HandleComplaint(WorldPackets::Ticket::Complaint& packet)
    {    // NOTE: all chat messages from this spammer are automatically ignored by the spam reporter until logout in case of chat spam.
        // if it's mail spam - ALL mails from this spammer are automatically removed by client

        WorldPackets::Ticket::ComplaintResult result;
        result.ComplaintType = packet.ComplaintType;
        result.Result = 0;
        SendPacket(result.Write());
    }
}
