package com.github.azeroth.world.handler;

public class BattlenetHandler {

    void HandleBattlenetChangeRealmTicket(WorldPackets::Battlenet::ChangeRealmTicket& changeRealmTicket)
    {
        SetRealmListSecret(changeRealmTicket.Secret);

        WorldPackets::Battlenet::ChangeRealmTicketResponse realmListTicket;
        realmListTicket.Token = changeRealmTicket.Token;
        realmListTicket.Allow = true;
        realmListTicket.Ticket << "WorldserverRealmListTicket";

        SendPacket(realmListTicket.Write());
    }

    void HandleBattlenetRequest(WorldPackets::Battlenet::Request& request)
    {
        sServiceDispatcher.Dispatch(this, request.Method.GetServiceHash(), request.Method.Token, request.Method.GetMethodId(), std::move(request.Data));
    }

    void SendBattlenetResponse(uint32 serviceHash, uint32 methodId, uint32 token, pb::Message const* response)
    {
        WorldPackets::Battlenet::Response bnetResponse;
        bnetResponse.BnetStatus = ERROR_OK;
        bnetResponse.Method.Type = MAKE_PAIR64(methodId, serviceHash);
        bnetResponse.Method.ObjectId = 1;
        bnetResponse.Method.Token = token;

        if (response->ByteSize())
        {
            bnetResponse.Data.resize(response->ByteSize());
            response->SerializePartialToArray(bnetResponse.Data.data(), response->ByteSize());
        }

        SendPacket(bnetResponse.Write());
    }

    void SendBattlenetResponse(uint32 serviceHash, uint32 methodId, uint32 token, uint32 status)
    {
        WorldPackets::Battlenet::Response bnetResponse;
        bnetResponse.BnetStatus = BattlenetRpcErrorCode(status);
        bnetResponse.Method.Type = MAKE_PAIR64(methodId, serviceHash);
        bnetResponse.Method.ObjectId = 1;
        bnetResponse.Method.Token = token;

        SendPacket(bnetResponse.Write());
    }

    void SendBattlenetRequest(uint32 serviceHash, uint32 methodId, pb::Message const* request, std::function<void(MessageBuffer)> callback)
    {
        _battlenetResponseCallbacks[_battlenetRequestToken] = std::move(callback);
        SendBattlenetRequest(serviceHash, methodId, request);
    }

    void SendBattlenetRequest(uint32 serviceHash, uint32 methodId, pb::Message const* request)
    {
        WorldPackets::Battlenet::Notification notification;
        notification.Method.Type = MAKE_PAIR64(methodId, serviceHash);
        notification.Method.ObjectId = 1;
        notification.Method.Token = _battlenetRequestToken++;

        if (request->ByteSize())
        {
            notification.Data.resize(request->ByteSize());
            request->SerializePartialToArray(notification.Data.data(), request->ByteSize());
        }

        SendPacket(notification.Write());
    }

}
