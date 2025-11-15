package com.github.azeroth.world.handler;

public class TokenHandler {

    void HandleCommerceTokenGetLog(WorldPackets::Token::CommerceTokenGetLog& commerceTokenGetLog)
    {
        WorldPackets::Token::CommerceTokenGetLogResponse response;

        /// @todo: 6.x fix implementation
        response.UnkInt = commerceTokenGetLog.UnkInt;
        response.Result = TOKEN_RESULT_SUCCESS;

        SendPacket(response.Write());
    }

    void HandleCommerceTokenGetMarketPrice(WorldPackets::Token::CommerceTokenGetMarketPrice& commerceTokenGetMarketPrice)
    {
        WorldPackets::Token::CommerceTokenGetMarketPriceResponse response;

        /// @todo: 6.x fix implementation
        response.CurrentMarketPrice = 300000000;
        response.UnkInt = commerceTokenGetMarketPrice.UnkInt;
        response.Result = TOKEN_RESULT_SUCCESS;
        //packet.ReadUInt32("UnkInt32");

        SendPacket(response.Write());
    }
}
